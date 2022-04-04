package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import lando.systems.ld50.assets.Assets;

//import lando.systems.ld50.Config;

public class StoryScreen extends BaseScreen {
    private static float textScale = 1.0f;
    float accum = 0;
    PerspectiveCamera perspectiveCamera;
    GlyphLayout layout;

    Rectangle skipRect;
    Rectangle speedRect;
    boolean hoverSkip;
    boolean hoverSpeed;

    String text = "\n\n\n\n\n\n\n\nBro, check it\n\n\n" +
            "Trent and Chad scored a righteous AirBnB\n" +
            "up in the mountains for Spring Break,\n" +
            "and me and the boys are going to be \n"+
            "shredding mad gnars all week long\n"+
            "\n\n" +
            "Sick, right?\n" +
            "\n\n" +
            "Rental dude was serving this \n" +
            "\"it's only a matter of time\" type energy, \n" +
            "which honestly had kind of an ominous vibe, \n" +
            "but the price was too legit to quit\n" +
            "\n" +
            "He mentioned something about a \n" +
            "particularly heavy snowfall this season?\n" +
            " \n" +
            " ..." +
            " \n" +
            " \n" +
            " \n" +
            "As long as we don't end up having to \n" +
            "move up and down the mountain\n" +
            "(by clicking and dragging)\n" +
            "\n" +
            "to select and strategically place\n" +
            "structures on the mountainside,\n" +
            "(with left mouse button),\n" +
            "\n"+
            "to divert and/or speed up the flow\n" +
            "of an unrelenting avalanche,\n"+
            "(that conveniently doesn't begin\n"+
            "until we're done building each day),\n"+
            "\n"+
            "to temporarily stave off\n" +
            "the destruction of houses, \n"+
            "whose debris we can then use\n"+
            "for additional structures,\n"+
            "\n"+

            "to keep disaster at bay \n"+
            "for as long as possible,\n"+
            "(or, if we're feeling totally un-rad,\n"+
            "increase total overall destruction),\n"+
            "\n"+
            "until, despite all our efforts,\n"+
            "our ski lodge AirBnB is inevitably \n"+
            "destroyed in the end anyway,\n"+
            "\n"+

            "this will be the sickest\n"+
            "Spring Break ever\n"+
            "\n\n" +
            "..." +
            "\n\n" +
            "\n\n" +
            "I feel like we can really\n" +
            "trust the flow this week, bro\n" +
            "\n" +
            "\n" +
            "Namaste with us\n\n" +
            "\n\n";

    public StoryScreen() {
//        super(game);
        layout = new GlyphLayout();
        game.assets.font.getData().setScale(textScale);
        layout.setText(game.assets.font, text, Color.WHITE, worldCamera.viewportWidth, Align.center, true);

        game.assets.font.getData().setScale(1.0f);
//        game.audio.playMusic(AudioManager.Musics.introMusic);

        perspectiveCamera = new PerspectiveCamera(90, 1280, 800);
        perspectiveCamera.far=10000;
        perspectiveCamera.position.set(640, 0, 500);
        perspectiveCamera.lookAt(640, 00, 0);
        perspectiveCamera.update();
        skipRect = new Rectangle(windowCamera.viewportWidth - 170, windowCamera.viewportHeight-70, 150, 50);
        speedRect = new Rectangle(windowCamera.viewportWidth - 370, windowCamera.viewportHeight-140, 350, 50);

    }

    Vector3 screenPos = new Vector3();
    public void update(float dt) {
        float speedMultiplier = 1.0f;

        screenPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        windowCamera.unproject(screenPos);

        hoverSkip = false;
        hoverSpeed = false;

        if (skipRect.contains(screenPos.x, screenPos.y)){
            hoverSkip = true;
        }
        if (speedRect.contains(screenPos.x, screenPos.y)){
            hoverSpeed = true;
        }


        if (Gdx.input.isTouched() && hoverSpeed){
            speedMultiplier = 10f;
        }
        accum += 75*dt * speedMultiplier;
//        accum = MathUtils.clamp(accum, 0, layout.height);
        if (accum > layout.height && Gdx.input.justTouched()) {
            launchGame();
        }
        if (accum >= layout.height) {
            launchGame();
        }
        if (Gdx.input.isTouched() && hoverSkip) {
            launchGame();
        }
    }

    private void launchGame() {
        if (!exitingScreen){
            exitingScreen = true;
            game.setScreen(new GameScreen());

        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling?GL20.GL_COVERAGE_BUFFER_BIT_NV:0));

        batch.setProjectionMatrix(perspectiveCamera.combined);
        batch.begin();

//        batch.draw(assets.backgrounds.titleImage, 0,0, Config.window_width, Config.window_height);

        game.assets.font.getData().setScale(textScale);
        game.assets.font.setColor(.3f, .3f, .3f, 1.0f);
        game.assets.font.draw(batch, text, 5, accum-3, worldCamera.viewportWidth, Align.center, true);
        game.assets.font.setColor(Color.WHITE);
        game.assets.font.draw(batch, text, 0, accum, worldCamera.viewportWidth, Align.center, true);
        game.assets.font.getData().setScale(1.0f);
//        batch.draw(textTexture, 0, 0, 1024, layout.height);
        batch.end();

        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();

        BitmapFont font = assets.font;
        if (hoverSkip){
            Assets.Patch.glass_active.ninePatch.draw(batch, skipRect.x, skipRect.y, skipRect.width, skipRect.height);
        } else {
            Assets.Patch.glass_dim.ninePatch.draw(batch, skipRect.x, skipRect.y, skipRect.width, skipRect.height);
        }
        assets.layout.setText(font, "Skip", Color.GRAY, skipRect.width, Align.center, false);
        font.draw(batch, assets.layout, skipRect.x, skipRect.y + (skipRect.height + assets.layout.height)/2f);

        if (hoverSpeed){
            Assets.Patch.glass_active.ninePatch.draw(batch, speedRect.x, speedRect.y, speedRect.width, speedRect.height);
        } else {
            Assets.Patch.glass_dim.ninePatch.draw(batch, speedRect.x, speedRect.y, speedRect.width, speedRect.height);
        }
        assets.layout.setText(font, "Hurry up, bro", Color.WHITE, speedRect.width, Align.center, false);
        font.draw(batch, assets.layout, speedRect.x, speedRect.y + (speedRect.height + assets.layout.height)/2f);

        batch.end();


    }
}
