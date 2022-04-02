package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisWindow;

public class GameScreen extends BaseScreen {

    private static final String TAG = GameScreen.class.getSimpleName();

    private final Color background = Color.SKY.cpy();

    public static class Stats {
        // TODO - add stats vars
    }

    public GameScreen() {
        InputMultiplexer mux = new InputMultiplexer(this);
        Gdx.input.setInputProcessor(mux);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        boolean gameOver = isGameOver();
    }

    @Override
    public void render(SpriteBatch batch) {
        ScreenUtils.clear(background, true);

        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        {
            // world
        }
        batch.end();

        // NOTE: always draw so the 'hide' transition is visible
        uiStage.draw();

        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            // TODO -
        }
        batch.end();
    }

    public boolean isGameOver() {
        return false;
    }

    // ------------------------------------------------------------------------
    // InputAdapter overrides
    // ------------------------------------------------------------------------

    private final Vector3 touchStart = new Vector3();
    private final Vector3 touchDrag = new Vector3();

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        if (isGameOver()) return false;
        // ...
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (isGameOver()) return false;
        // ...
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        if (isGameOver()) return false;
        // ...
        return super.touchDragged(screenX, screenY, pointer);
    }

    // ------------------------------------------------------------------------
    // ControllerListener default implementation (from ControllerAdapter)
    // ------------------------------------------------------------------------

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
        return super.buttonUp(controller, buttonIndex);
    }

    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
        return super.axisMoved(controller, axisIndex, value);
    }

    // ------------------------------------------------------------------------
    // Private implementation details
    // ------------------------------------------------------------------------

    private static class DebugElements {
        public static VisLabel fpsLabel;
        public static VisLabel javaHeapLabel;
        public static VisLabel nativeHeapLabel;
        public static VisLabel drawCallLabel;
        public static VisLabel simTimeLabel;
        public static VisLabel gamepadAxisLabel;
    }

    @Override
    protected void initializeUI() {
        super.initializeUI();

        VisWindow window = new VisWindow("", true);
        window.setFillParent(true);
        window.setColor(1f, 1f, 1f, 0.4f);
        window.align(Align.top);

        VisLabel label;

        label = new VisLabel();
        window.add(label).growX().row();
        DebugElements.fpsLabel = label;

        label = new VisLabel();
        window.add(label).growX().row();
        DebugElements.javaHeapLabel = label;

        label = new VisLabel();
        window.add(label).growX().row();
        DebugElements.nativeHeapLabel = label;

        label = new VisLabel();
        window.add(label).growX().row();
        DebugElements.drawCallLabel = label;

        label = new VisLabel();
        window.add(label).growX().row();
        DebugElements.simTimeLabel = label;

        label = new VisLabel();
        window.add(label).growX().row();
        DebugElements.gamepadAxisLabel = label;

        uiStage.addActor(window);

        Action startHidden = Actions.moveTo(0, -windowCamera.viewportHeight, 0f);
        startHidden.setActor(uiStage.getRoot());

        uiStage.addAction(startHidden);
    }

}
