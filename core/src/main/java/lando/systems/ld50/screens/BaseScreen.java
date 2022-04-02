package lando.systems.ld50.screens;

import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import lando.systems.ld50.Config;
import lando.systems.ld50.Main;
import lando.systems.ld50.assets.Assets;

public abstract class BaseScreen implements InputProcessor, ControllerListener, Disposable {

    private static final String TAG = BaseScreen.class.getSimpleName();

    public final Main game;
    public final Assets assets;
    public final TweenManager tween;
    public final SpriteBatch batch;
    public final Vector3 pointerPos;

    public boolean exitingScreen;
//    public Particles particles;
//    public AudioManager audio;
    public OrthographicCamera worldCamera;
    public OrthographicCamera windowCamera;

    protected Stage uiStage;

    public BaseScreen() {
        this.game = Main.game;
        this.assets = game.assets;
        this.tween = game.tween;
        this.batch = assets.batch;
//        this.audio = game.audio;
//        this.particles = new Particles(game.assets);
        this.pointerPos = new Vector3();

        this.exitingScreen = false;

        this.worldCamera = new OrthographicCamera();
        this.worldCamera.setToOrtho(false, Config.window_width, Config.window_height);
        this.worldCamera.update();

        this.windowCamera = new OrthographicCamera();
        this.windowCamera.setToOrtho(false, Config.window_width, Config.window_height);
        this.windowCamera.update();

        initializeUI();
    }

    /**
     * Override and call super to setup whatever ui is needed in a screen
     */
    protected void initializeUI() {
        // reset the stage in case it hasn't already been set to the current window camera orientation
        // NOTE - doesn't seem to be a way to directly set the stage camera as the window camera
        //  could go in the other direction, create the uiStage and set windowCam = stage.cam
        StretchViewport viewport = new StretchViewport(windowCamera.viewportWidth, windowCamera.viewportHeight);
        uiStage = new Stage(viewport, batch);
    }

    public void transitionCompleted() {
        Controllers.clearListeners();
        Controllers.addListener(this);
        Gdx.input.setInputProcessor(this);
    }

    public void updateEvenIfPaused(float dt) {
        // ... add things here that need to update even when Time.pause() has been called
    }

    public void update(float dt) {
        worldCamera.update();
        windowCamera.update();
        uiStage.act(dt);

//        audio.update(dt);

        // toggle debug states
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            boolean wasShown = Config.debug_general;
            Config.debug_general = !Config.debug_general;

            Actor rootActor = uiStage.getRoot();
            Action transitionAction = (wasShown)
                    ? Actions.moveTo(0, -windowCamera.viewportHeight, 0.1f, Interpolation.exp10In)
                    : Actions.moveTo(0, 0, 0.2f, Interpolation.exp10Out);
            transitionAction.setActor(rootActor);
            uiStage.addAction(transitionAction);
        }
    }

    public void resize(int width, int height) {}
    public void renderFrameBuffers(SpriteBatch batch) {}

    public abstract void render(SpriteBatch batch);

    @Override
    public void dispose() {
        uiStage.dispose();
    }

    // ------------------------------------------------------------------------
    // InputProcessor default implementation (from InputAdapter)
    // ------------------------------------------------------------------------

    @Override
    public boolean keyDown (int keycode) {
        return false;
    }

    @Override
    public boolean keyUp (int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped (char character) {
        return false;
    }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved (int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        return false;
    }

    // ------------------------------------------------------------------------
    // ControllerListener default implementation (from ControllerAdapter)
    // ------------------------------------------------------------------------

    @Override
    public boolean buttonDown (Controller controller, int buttonIndex) {
        if (Config.debug_general) {
            Gdx.app.log(TAG, "controller " + controller.getName() + " button " + buttonIndex + " down");
        }
        return false;
    }

    @Override
    public boolean buttonUp (Controller controller, int buttonIndex) {
        if (Config.debug_general) {
            Gdx.app.log(TAG, "controller " + controller.getName() + " button " + buttonIndex + " up");
        }
        return false;
    }

    @Override
    public boolean axisMoved (Controller controller, int axisIndex, float value) {
        float deadzone = 0.2f;
        if (Config.debug_general && Math.abs(value) > deadzone) {
            Gdx.app.log(TAG, "controller " + controller.getName() + " axis " + axisIndex + " moved " + value);
        }
        return false;
    }

    @Override
    public void connected (Controller controller) {
        Gdx.app.log(TAG, "controller connected: '" + controller.getName() + "' id:" + controller.getUniqueId());
    }

    @Override
    public void disconnected (Controller controller) {
        // TODO - pause game and wait for reconnect or confirmation?
        Gdx.app.log(TAG, "controller disconnected: '" + controller.getName() + "' id:" + controller.getUniqueId());
    }

}
