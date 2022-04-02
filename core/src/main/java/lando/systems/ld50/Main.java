package lando.systems.ld50;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.VisUI;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.screens.BaseScreen;
import lando.systems.ld50.screens.LaunchScreen;
import lando.systems.ld50.screens.TitleScreen;
import lando.systems.ld50.utils.Time;
import lando.systems.ld50.utils.accessors.*;

public class Main extends ApplicationAdapter {

	private static final String TAG = Main.class.getSimpleName();

	public static Main game;

	public Assets assets;
	public TweenManager tween;
//	public AudioManager audio;

	private OrthographicCamera camera;
	private BaseScreen currentScreen;
	private BaseScreen nextScreen;

	@Override
	public void create() {
		Main.game = this;

		if (Config.debug_general || Config.shader_debug_log) {
			Gdx.app.setLogLevel(Application.LOG_DEBUG);
		}

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Config.window_width, Config.window_height);
		camera.update();

		assets = new Assets(Assets.Load.SYNC);
		VisUI.load(game.assets.mgr.get("gui/uiskin.json", Skin.class));

		Time.init();

		tween = new TweenManager();
		Tween.setWaypointsLimit(4);
		Tween.setCombinedAttributesLimit(4);
		Tween.registerAccessor(Color.class, new ColorAccessor());
		Tween.registerAccessor(Rectangle.class, new RectangleAccessor());
		Tween.registerAccessor(Vector2.class, new Vector2Accessor());
		Tween.registerAccessor(Vector3.class, new Vector3Accessor());
		Tween.registerAccessor(OrthographicCamera.class, new CameraAccessor());

		if (Gdx.app.getType() == Application.ApplicationType.WebGL) {
			setScreen(new LaunchScreen());
		} else {
			setScreen(new TitleScreen());
		}
	}

	@Override
	public void dispose() {
		VisUI.dispose(false);
		Transition.dispose();
		if (assets.initialized) {
			assets.dispose();
		}
		Gdx.app.exit();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		currentScreen.resize(width, height);
		camera.setToOrtho(false, width, height);
		camera.update();
		Transition.init(assets);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
		// TODO: handle asset reloading
		Transition.init(assets);
	}

	public void update() {
		// update global timer
		Time.delta = Gdx.graphics.getDeltaTime();

		// update code that always runs (regardless of pause)
		currentScreen.updateEvenIfPaused(Time.delta);

		// handle a pause
		if (Time.pause_timer > 0) {
			Time.pause_timer -= Time.delta;
			if (Time.pause_timer <= -0.0001f) {
				Time.delta = -Time.pause_timer;
			} else {
				// skip updates if we're paused
				return;
			}
		}
		Time.millis += Time.delta;
		Time.previous_elapsed = Time.elapsed_millis();

		// update systems
		camera.update();
		tween.update(Time.delta);
		currentScreen.update(Time.delta);
	}

	@Override
	public void render() {
		update();

		SpriteBatch batch = assets.batch;

		ScreenUtils.clear(Color.DARK_GRAY, true);
		currentScreen.renderFrameBuffers(batch);

		if (nextScreen == null) {
			ScreenUtils.clear(Color.DARK_GRAY, true);
			currentScreen.render(batch);
		} else {
			Transition.render(batch, currentScreen, nextScreen, camera);
		}
	}

	public BaseScreen getScreen() {
		return currentScreen;
	}

	public void setScreen(final BaseScreen screen) {
		setScreen(screen, 0.5f);
	}

	public void setScreen(final BaseScreen newScreen, float transitionSpeed) {
		if (nextScreen != null) return;
		if (Transition.inProgress) return; // only want one transition

		if (currentScreen == null) {
			currentScreen = newScreen;
		} else {
			Transition.inProgress = true;
			Transition.percent.setValue(0);
			Timeline.createSequence()
					.pushPause(.1f)
					.push(Tween.call((i, baseTween) -> nextScreen = newScreen))
					.push(Tween.to(Transition.percent, 1, transitionSpeed).target(1).ease(Linear.INOUT))
					.push(Tween.call((i, baseTween) -> {
						Transition.inProgress = false;
						currentScreen.dispose();
						currentScreen = nextScreen;
						currentScreen.transitionCompleted();
						nextScreen = null;
					}))
					.start(tween);
		}
	}

	public static class Transition {
		static boolean inProgress;
		static MutableFloat percent;
		static ShaderProgram shader;

		static class FrameBuffers {
			public static FrameBuffer source;
			public static FrameBuffer dest;
		}

		static class Textures {
			public static Texture source;
			public static Texture dest;
		}

		public static void set(Assets assets, Assets.Transition transition) {
			Transition.shader = assets.transitionShaders.get(transition);
		}

		public static void init(Assets assets) {
			if (!assets.initialized) return;
			if (Gdx.graphics.getWidth() == 0 || Gdx.graphics.getHeight() == 0) return;
			if (FrameBuffers.source != null) {
				FrameBuffers.source.dispose();
			}
			if (FrameBuffers.dest != null) {
				FrameBuffers.dest.dispose();
			}

			Transition.inProgress = false;
			Transition.percent = new MutableFloat(0);
			Transition.shader = assets.transitionShaders.get(Assets.Transition.dreamy);
			FrameBuffers.source = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			FrameBuffers.dest = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			Textures.source = FrameBuffers.source.getColorBufferTexture();
			Textures.dest = FrameBuffers.dest.getColorBufferTexture();
		}

		static void render(SpriteBatch batch, BaseScreen currentScreen, BaseScreen nextScreen, Camera camera) {
			nextScreen.update(Time.delta);
			nextScreen.renderFrameBuffers(batch);

			// draw next screen to dest framebuffer
			Transition.FrameBuffers.dest.begin();
			{
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				nextScreen.render(batch);
			}
			Transition.FrameBuffers.dest.end();

			// draw current screen to source framebuffer
			Transition.FrameBuffers.source.begin();
			{
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				currentScreen.render(batch);
			}
			Transition.FrameBuffers.source.end();

			// draw composited framebuffer to window
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.setProjectionMatrix(camera.combined);
			batch.setShader(Transition.shader);
			batch.begin();
			{
				Transition.Textures.source.bind(1);
				Transition.shader.setUniformi("u_texture1", 1);

				Transition.Textures.dest.bind(0);
				Transition.shader.setUniformi("u_texture", 0);

				Transition.shader.setUniformf("u_percent", Transition.percent.floatValue());

				batch.setColor(Color.WHITE);
				batch.draw(Transition.Textures.dest, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			}
			batch.end();
			batch.setShader(null);
			if (Config.debug_general){
				batch.begin();
				batch.draw(Textures.dest, 0, 100, 100, -100 );
				batch.draw(Textures.source, 100, 100, 100, -100 );
				batch.end();
			}
		}

		static void dispose() {
			if (Transition.FrameBuffers.source != null) {
				Transition.FrameBuffers.source.dispose();
			}
			if (Transition.FrameBuffers.dest != null) {
				Transition.FrameBuffers.dest.dispose();
			}
			if (Transition.Textures.source != null) {
				Transition.Textures.source.dispose();
			}
			if (Transition.Textures.dest != null) {
				Transition.Textures.dest.dispose();
			}

		}
	}
}