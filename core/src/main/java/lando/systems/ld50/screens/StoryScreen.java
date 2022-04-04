package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
//import lando.systems.ld50.Config;

public class StoryScreen extends BaseScreen {
    private static float textScale = 1.0f;
    float accum = 0;
    PerspectiveCamera perspectiveCamera;
    GlyphLayout layout;

    String text = "\n\n\n\n\n\n\n\nBro, check it\n\n\n" +
            "Trent and Chad scored a righteous AirBnB\n" +
            "up in the mountains for Spring Break,\n" +
            "and me and the boys are going to be \n"+
            "shredding mad gnars all week long\n"+
            "\n\n" +
            "Sick, right?\n\n" +
            "Rental dude was serving this \n" +
            "\"it's only a matter of time\" type energy, \n" +
            "which honestly had kind of an ominous vibe, \n" +
            "but the price was too legit to quit\n" +
            "\n" +
            "He mentioned something about a \n" +
            "particularly heavy snowfall this season?\n" +
            " \n" +
            "As long as we don't end up \n" +
            "having to frantically place\n" +
            "ramps and other structures\n" +
            "around the mountainside,\n" +
            "\n"+
            "in a desperate attempt \n" +
            "to divert the flow of an\n" +
//            "an unrelenting and ultimately\n"+
            "unavoidable avalanche,\n"+
            "\n"+
            "to temporarily stave off\n" +
            "the destruction of houses, \n\n"+
            "whose debris we then use\n"+
            "to build additional structures,\n"+
            "\n"+

            "as we continue trying \n"+
            "to keep disaster at bay,\n"+
            "\n"+
            "until, despite all our efforts,\n"+
            "the ski lodge is ultimately \n"+
            "destroyed anyway,\n"+
            "\n"+

            "this will be the sickest\n"+
            "Spring Break ever\n\n"+
            "\n\n" +
            "I feel like we can really\n" +
            "trust in the flow this week, bro\n\n" +
            "Namaste with us\n\n" +
            "\n\n";

    public StoryScreen() {
//        super(game);
        layout = new GlyphLayout();
        game.assets.font.getData().setScale(textScale);
        layout.setText(game.assets.font, text, Color.WHITE, worldCamera.viewportWidth, Align.center, true);

        game.assets.font.getData().setScale(1f);
//        game.audio.playMusic(AudioManager.Musics.introMusic);

        perspectiveCamera = new PerspectiveCamera(90, 1280, 800);
        perspectiveCamera.far=10000;
        perspectiveCamera.position.set(640, 0, 500);
        perspectiveCamera.lookAt(640, 00, 0);
        perspectiveCamera.update();

    }

    public void update(float dt) {
        float speedMultiplier = 1.0f;

        if (Gdx.input.isTouched()){
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
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
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

    }
}
