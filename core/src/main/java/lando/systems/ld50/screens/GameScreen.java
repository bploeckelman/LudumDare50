package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisWindow;
import lando.systems.ld50.Config;
import lando.systems.ld50.objects.Landscape;
import lando.systems.ld50.utils.screenshake.ScreenShakeCameraController;

public class GameScreen extends BaseScreen {

    private static final String TAG = GameScreen.class.getSimpleName();

    public static class Stats {
        // TODO - add stats vars
    }

    private final Color background = Color.SKY.cpy();
    private final PerspectiveCamera camera;
    private final ScreenShakeCameraController shaker;
    private final CameraInputController cameraController;
    private final Landscape landscape;

    public GameScreen() {
        camera = new PerspectiveCamera(70f, Config.window_width, Config.window_height);
        camera.position.set(-2, 0, 10);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 1000f;

        // orient manually for doug to see what he's doing
        camera.position.set(4f,-2f,13f);
        camera.up.set(-0.0007141829f,0.7193363f,0.6946612f);
        camera.direction.set(0.00073949544f,0.6946616f,-0.71933526f);
        camera.update();

        cameraController = new CameraInputController(camera);
        shaker = new ScreenShakeCameraController(camera);

        landscape = new Landscape();

        InputMultiplexer mux = new InputMultiplexer(uiStage, this, cameraController);
        Gdx.input.setInputProcessor(mux);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void transitionCompleted() {
        super.transitionCompleted();
        InputMultiplexer mux = new InputMultiplexer(uiStage, this, cameraController);
        Gdx.input.setInputProcessor(mux);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        boolean gameOver = isGameOver();

        camera.update();
        cameraController.update();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            shaker.addDamage(0.5f);
        }
        shaker.update(dt);

        landscape.update(dt);

//        Gdx.app.log(TAG, String.format("pos(%s) up(%s) forward(%s) side(%s)",
//                camera.position, camera.up, camera.direction,
//                side.set(camera.up).crs(camera.direction)));
    }
    private final Vector3 side = new Vector3();

    @Override
    public void render(SpriteBatch batch) {
        ScreenUtils.clear(background, true);

        // draw world
        batch.setProjectionMatrix(shaker.getCombinedMatrix());
        batch.begin();
        {
            // TODO - shaker breaks camera controller rotation,
            //  might not be important if we maintain control of the camera throughout
//            landscape.render(batch, shaker.getViewCamera());
            landscape.render(batch, camera);
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
