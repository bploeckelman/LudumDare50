package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.audio.AudioManager;

public class TitleScreen extends BaseScreen {
    private VisTable rootTable;
    private VisTextButton startGameButton;
    private VisTextButton creditButton;
    private VisTextButton settingsButton;

    private final float BUTTON_WIDTH = 300f;
    private final float BUTTON_HEIGHT = 75f;
    private final float BUTTON_PADDING = 10f;

    public TitleScreen() {
        // NOTE: have to set the input processor here as well as transitionCompleted
        //   so that it's set correctly in desktop mode where there's no transition
        InputMultiplexer mux = new InputMultiplexer(uiStage, this);
        Gdx.input.setInputProcessor(mux);

        game.audio.playMusic(AudioManager.Musics.introMusic);
//        game.audio.playMusic(AudioManager.Musics.outroMusic);
    }

    @Override
    protected void initializeUI() {
        super.initializeUI();

//        rootTable = new VisTable();
//        rootTable.setWidth(windowCamera.viewportWidth);
//        rootTable.setHeight(windowCamera.viewportHeight / 2f);
//        rootTable.setPosition(0, 0f);
//        rootTable.align(Align.center | Align.top);
//        rootTable.pad(5f);

        VisTextButton.VisTextButtonStyle outfitMediumStyle = skin.get("outfit-medium-40px", VisTextButton.VisTextButtonStyle.class);
        VisTextButton.VisTextButtonStyle titleScreenButtonStyle = new VisTextButton.VisTextButtonStyle(outfitMediumStyle);
        titleScreenButtonStyle.up = Assets.Patch.glass.drawable;
        titleScreenButtonStyle.down = Assets.Patch.glass_dim.drawable;
        titleScreenButtonStyle.over = Assets.Patch.glass_dim.drawable;

        startGameButton = new VisTextButton("Start Game", titleScreenButtonStyle);
        Gdx.app.log("startbuttonwidth&height", "width: " + startGameButton.getWidth() + " & height: " + startGameButton.getHeight());
        startGameButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        startGameButton.setPosition(windowCamera.viewportWidth / 2f - startGameButton.getWidth() / 2f, windowCamera.viewportHeight / 3f);
        startGameButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            if (!exitingScreen) {
                game.setScreen(new StoryScreen());
                // TODO Change this back to story screen before publishing
//                game.setScreen(new GameScreen());
                exitingScreen = true;
            }
            }
        });

        settingsButton = new VisTextButton("Settings", titleScreenButtonStyle);
        settingsButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        settingsButton.setPosition(windowCamera.viewportWidth / 2f - settingsButton.getWidth() / 2f, startGameButton.getY() - startGameButton.getHeight() - BUTTON_PADDING);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSettings();
            }
        });


        creditButton = new VisTextButton("Credits", titleScreenButtonStyle);
        creditButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        creditButton.setPosition(windowCamera.viewportWidth / 2f - creditButton.getWidth() / 2f, settingsButton.getY() - settingsButton.getHeight() - BUTTON_PADDING);
        creditButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            if (!exitingScreen) {
                game.setScreen(new EndScreen());
                //game.setScreen(new EpilogueScreen());
                audio.playSound(AudioManager.Sounds.goodKarma);
                audio.playMusic(AudioManager.Musics.outroMusic);
                exitingScreen = true;
            }
            }
        });

//        rootTable.add(startGameButton).padBottom(BUTTON_PADDING).width(BUTTON_WIDTH).height(BUTTON_HEIGHT);
//        rootTable.row();
//        rootTable.add(settingsButton).padBottom(BUTTON_PADDING).width(BUTTON_WIDTH).height(BUTTON_HEIGHT);
//        rootTable.row();
//        rootTable.add(creditButton).padBottom(BUTTON_PADDING).width(BUTTON_WIDTH).height(BUTTON_HEIGHT);

        uiStage.addActor(startGameButton);
        uiStage.addActor(settingsButton);
        uiStage.addActor(creditButton);

//        settingsGroup.setZIndex(settingsGroup.getZIndex()+1);
    }

    @Override
    public void transitionCompleted() {
        super.transitionCompleted();
        InputMultiplexer mux = new InputMultiplexer(uiStage, this);
        Gdx.input.setInputProcessor(mux);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            batch.draw(assets.pixel, 0, 0, windowCamera.viewportWidth, windowCamera.viewportHeight);

            assets.layout.setText(assets.font, "Avalaunch!", Color.LIGHT_GRAY, windowCamera.viewportWidth, Align.center, false);
            assets.font.draw(batch, assets.layout, 0f, windowCamera.viewportHeight * (3f / 4f) + assets.layout.height / 2f);
        }
        batch.end();
        uiStage.draw();
    }

    // ------------------------------------------------------------------------
    // Input handling
    // ------------------------------------------------------------------------

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {

        return true;
    }

    @Override
    public boolean keyUp (int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            exit();
            return true;
        }
        return false;
    }

    @Override
    public boolean buttonUp (Controller controller, int buttonIndex) {
        game.setScreen(new GameScreen());
        return true;
    }

    private void exit() {
        Gdx.app.exit();
    }

}
