package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import lando.systems.ld50.audio.AudioManager;

public class TitleScreen extends BaseScreen {
    private VisTable rootTable;
    private VisTextButton startGameButton;
    private VisTextButton creditButton;
    private VisTextButton settingsButton;

    private final float BUTTON_WIDTH = 20f;
    private final float BUTTON_PADDING = 10f;

    public TitleScreen() {
        // NOTE: have to set the input processor here as well as transitionCompleted
        //   so that it's set correctly in desktop mode where there's no transition
        InputMultiplexer mux = new InputMultiplexer(uiStage, this);
        Gdx.input.setInputProcessor(mux);

        game.audio.playMusic(AudioManager.Musics.introMusic);
    }

    @Override
    protected void initializeUI() {
        super.initializeUI();

        rootTable = new VisTable();
        rootTable.setWidth(uiStage.getWidth());
        rootTable.setPosition(0, uiStage.getHeight() / 3);
        rootTable.align(Align.center | Align.top);
        rootTable.pad(5f);

        startGameButton = new VisTextButton("Start Game", "outfit-medium-40px");
        startGameButton.setWidth(BUTTON_WIDTH);
        startGameButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            if (!exitingScreen) {
                game.setScreen(new GameScreen());
                exitingScreen = true;
            }
            }
        });

        settingsButton = new VisTextButton("Settings", "outfit-medium-40px");
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSettings();
            }
        });


        creditButton = new VisTextButton("Credits", "outfit-medium-40px");
        creditButton.setWidth(BUTTON_WIDTH);
        creditButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            if (!exitingScreen) {
                game.setScreen(new EndScreen());
                audio.playSound(AudioManager.Sounds.chaching);
                exitingScreen = true;
            }
            }
        });

        rootTable.add(startGameButton).padBottom(BUTTON_PADDING);
        rootTable.row();
        rootTable.add(settingsButton).padBottom(BUTTON_PADDING);
        rootTable.row();
        rootTable.add(creditButton).padBottom(BUTTON_PADDING);

        uiStage.addActor(rootTable);

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
