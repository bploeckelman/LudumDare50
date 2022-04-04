package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
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
import lando.systems.ld50.Main;
import lando.systems.ld50.audio.AudioManager;

public class EpilogueScreen extends BaseScreen {



    private static float textScale = 1.0f;
    float accum = 0;
    PerspectiveCamera perspectiveCamera;
    GlyphLayout layout;

    Rectangle skipRect;
    Rectangle speedRect;
    boolean hoverSkip;
    boolean hoverSpeed;

    String text = "\n\n\n\n\n\n\n\n... \n\nthe fuck was that, bro?\n\n\n" +
            "Spring Break was supposed to be about\n"+
            "shredding powder and spreading chowder\n"+
            "\n\n" +
            "NOT about some Michael-Bay-ass\n" +
            "ecological disaster type shit\n"+
            "\n\n" +
            "We should have been mad partying \n" +
            "with a bevy of righteous babes\n" +
            "\n" +
            "Instead, we were forced to \n" +
            "stare headlong into the \n" +
            "existential uncertainty of our\n" +
            "ever-encroaching mortality\n" +
            "while also confronting the bleakness \n" +
            "of trying to stave off a climate catastrophe\n" +
            "that is now almost certainly unavoidable.\n" +
            "\n" +
            "I don't think any of us were prepared \n" +
            "to process the realities of calamity\n" +
            "on a scale so massive that\n" +
            "our individual choices could \n" +
            "never even begin to avert it\n" +
            "\n" +
            "\n" +
            "The only thing left to do now is\n" +
            "try to reconcile our flawed perception \n" +
            "of existence as an ever-persistent state\n" +
            "with the knowledge that, like all things, \n" +
            "it too will eventually pass from this earth \n" +
            "into the cosmic bliss of impermanence.\n" +
            "\n" +
            "..." +
            "\n" +
            "\n" +
            "Also, the fuck was that yeti's problem?\n" +
            "Keep the cryptid-ass attitude to yourself, brah\n" +
            "\n" +
            "No room at the inn for your toxic vibes\n" +
            "\n\n"+
            "..."+
            "\n\n\n"+

            "This was straight-up NOT a good time, bro.\n" +
            "\n"+
            "Maybe next year\n" +
            "\n";


    public EpilogueScreen() {
//        super(game);
        layout = new GlyphLayout();
        game.assets.font.getData().setScale(textScale);
        layout.setText(game.assets.font, text, Color.WHITE, worldCamera.viewportWidth, Align.center, true);

        game.assets.font.getData().setScale(1.125f);
//        game.audio.playMusic(AudioManager.Musics.introMusic);

        perspectiveCamera = new PerspectiveCamera(90, 1280, 800);
        perspectiveCamera.far=10000;
        perspectiveCamera.position.set(640, 0, 500);
        perspectiveCamera.lookAt(640, 00, 0);
        perspectiveCamera.update();
//        skipRect = new Rectangle(windowCamera.viewportWidth - 170, windowCamera.viewportHeight-70, 150, 50);
        speedRect = new Rectangle(windowCamera.viewportWidth - 370, windowCamera.viewportHeight-70, 350, 50);

    }

    Vector3 screenPos = new Vector3();
    public void update(float dt) {
        float speedMultiplier = 1.0f;

        screenPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        windowCamera.unproject(screenPos);

        hoverSkip = false;
        hoverSpeed = false;

//        if (skipRect.contains(screenPos.x, screenPos.y)){
//            hoverSkip = true;
//        }
        if (speedRect.contains(screenPos.x, screenPos.y)){
            hoverSpeed = true;
        }


        if (Gdx.input.isTouched() && hoverSpeed){
            speedMultiplier = 10f;
        }
        accum += 55*dt * speedMultiplier;
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
            game.setScreen(new EndScreen());

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
//        if (hoverSkip){
//            Assets.Patch.glass_active.ninePatch.draw(batch, skipRect.x, skipRect.y, skipRect.width, skipRect.height);
//        } else {
//            Assets.Patch.glass_dim.ninePatch.draw(batch, skipRect.x, skipRect.y, skipRect.width, skipRect.height);
//        }
//        assets.layout.setText(font, "Skip", Color.GRAY, skipRect.width, Align.center, false);
//        font.draw(batch, assets.layout, skipRect.x, skipRect.y + (skipRect.height + assets.layout.height)/2f);

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
