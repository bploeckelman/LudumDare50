package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;

public class TitleScreen extends BaseScreen {

    @Override
    protected void initializeUI() {
        super.initializeUI();
        Skin skin = VisUI.getSkin();
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
        if (!exitingScreen) {
            game.setScreen(new GameScreen());
            exitingScreen = true;
        }
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
