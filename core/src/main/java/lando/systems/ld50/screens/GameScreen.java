package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.widget.*;
import lando.systems.ld50.Config;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.assets.ImageInfo;
import lando.systems.ld50.assets.InputPrompts;
import lando.systems.ld50.audio.AudioManager;
import lando.systems.ld50.cameras.SimpleCameraController;
import lando.systems.ld50.objects.AnimationDecal;
import lando.systems.ld50.objects.Landscape;
import lando.systems.ld50.objects.Snowball;
import lando.systems.ld50.particles.PhysicsDecal;
import lando.systems.ld50.utils.Time;
import lando.systems.ld50.utils.screenshake.ScreenShakeCameraController;
import text.formic.Stringf;


public class GameScreen extends BaseScreen {

    private static final String TAG = GameScreen.class.getSimpleName();
    private static final int PICKMAP_SCALE = 16;

    public static class Stats {
        // TODO - add stats vars
    }

    private final Color background = Color.SKY.cpy();
    private final PerspectiveCamera camera;
    private final ScreenShakeCameraController shaker;
    private final SimpleCameraController cameraController;
    private final Landscape landscape;

    private final Environment env;
    private final ModelBatch modelBatch;
    private final DecalBatch decalBatch;

    private ModelInstance coords;
    private Array<ModelInstance> houseInstances;
    private Array<ModelInstance> creatureInstances;
    private Array<AnimationDecal> decals;
    private Array<Decal> freeParticleDecals;

    private Vector3 touchPos;
    private Vector3 startPos, endPos;
    private Vector3 startUp, endUp;
    private Vector3 startDir, endDir;

    private Vector3 lightDir;
    public DirectionalLight light;

    public FrameBuffer pickMapFBO;
    public Texture PickMapFBOTex;
    public Pixmap pickPixmap;

    private float cameraMovementT = 0;
    private boolean cameraMovementPaused = false;
    private final Vector3 billboardCameraPos = new Vector3();
    private VisWindow debugWindow;
    private VisProgressBar progressBar;

    public GameScreen() {
        camera = new PerspectiveCamera(70f, Config.window_width, Config.window_height);
        initializeCamera();

        cameraController = new SimpleCameraController(camera);
        shaker = new ScreenShakeCameraController(camera);

        lightDir = new Vector3(-1f, -.8f, -.2f);

        landscape = new Landscape(this);

        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        env.add(light = new DirectionalLight().set(0.8f, 0.8f, 0.8f, lightDir.x, lightDir.y, lightDir.z));
        modelBatch = new ModelBatch();
        decalBatch = new DecalBatch(new CameraGroupStrategy(camera));

        loadModels();
        loadDecals();

        touchPos = new Vector3();

        pickMapFBO =  new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth() / PICKMAP_SCALE, Gdx.graphics.getHeight() / PICKMAP_SCALE, true);
        PickMapFBOTex = pickMapFBO.getColorBufferTexture();

        InputMultiplexer mux = new InputMultiplexer(uiStage, this, cameraController);
        Gdx.input.setInputProcessor(mux);

        game.audio.playMusic(AudioManager.Musics.mainTheme);
        game.audio.musics.get(AudioManager.Musics.mainTheme).setVolume(0.3F);
    }

    @Override
    public void dispose() {
        super.dispose();
        modelBatch.dispose();
        decalBatch.dispose();
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            dumpCameraVecsToLog();
            cameraMovementPaused = !cameraMovementPaused;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            Time.pause_for(0.2f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            shaker.addDamage(0.5f);
            landscape.startAvalanche();
        }
        // toggle debug states
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            boolean wasShown = Config.debug_general;
            Config.debug_general = !Config.debug_general;

            Actor rootActor = debugWindow;
            Action transitionAction = (wasShown)
                    ? Actions.moveTo(0, -windowCamera.viewportHeight, 0.1f, Interpolation.exp10In)
                    : Actions.moveTo(0, 0, 0.2f, Interpolation.exp10Out);
            transitionAction.setActor(rootActor);
            uiStage.addAction(transitionAction);
        }

        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        worldCamera.unproject(touchPos);
        Vector2 tile = getSelectedTile((int)touchPos.x, (int)touchPos.y);
        landscape.setSelectedTile((int)tile.x, (int)tile.y);

        camera.update();
        cameraController.update();
        shaker.update(dt);

        landscape.update(dt);

        billboardCameraPos.set(camera.position).y = 0f;
        for (AnimationDecal decal : decals) {
            //decal.lookAt(billboardCameraPos, camera.up);
            decal.update(dt);
            decalBatch.add(decal.get());
        }

        /*for (Decal decal : particleDecals) {
            decal.setPosition(5f + MathUtils.random(-2f, 2f), 5f + MathUtils.random(-2f, 2f), 5f + MathUtils.random(-2f, 2f));
            decal.lookAt(camera.position, camera.up);
            decalBatch.add(decal);
        }*/
        PhysicsDecal.updateAllDecalParticles(dt);
        for (PhysicsDecal pd : PhysicsDecal.instances) {
            pd.decal.lookAt(camera.position, camera.up);
            decalBatch.add(pd.decal);
        }

        if (pickPixmap != null){
            pickPixmap.dispose();
        }

        // keep the camera focused on the bulk of the avalanche wave
        if (!cameraMovementPaused) {
            float prevCameraMovementT = cameraMovementT;
            float currCameraMovementT = getAvalancheProgress();
            cameraMovementT = MathUtils.lerp(prevCameraMovementT, currCameraMovementT, dt);
            camera.position.set(
                    MathUtils.lerp(startPos.x, endPos.x, cameraMovementT),
                    MathUtils.lerp(startPos.y, endPos.y, cameraMovementT),
                    MathUtils.lerp(startPos.z, endPos.z, cameraMovementT));
        }

        updateDebugElements();
        updateProgressBarValue();
    }

    @Override
    public void render(SpriteBatch batch) {
        ScreenUtils.clear(background, true);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

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

        // TODO - might need to make everything draw with the model batch
        modelBatch.begin(camera);
        {
            modelBatch.render(coords, env);
            modelBatch.render(houseInstances, env);
            modelBatch.render(creatureInstances, env);
            landscape.render(modelBatch, env);
        }
        modelBatch.end();

        decalBatch.flush();

        // NOTE: always draw so the 'hide' transition is visible
        uiStage.draw();

        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            // TODO -
            batch.draw(PickMapFBOTex, 0, 100, 100, -100);
        }
        batch.end();
    }

    public void renderFrameBuffers(SpriteBatch batch) {
        pickMapFBO.begin();
        ScreenUtils.clear(Color.CLEAR, true);
        landscape.renderFBO(camera);
        pickPixmap =  Pixmap.createFromFrameBuffer(0, 0, pickMapFBO.getWidth(), pickMapFBO.getHeight());
        pickMapFBO.end();
    }

    public boolean isGameOver() {
        return false;
    }

    private void initializeCamera() {
        camera.near = 0.1f;
        camera.far = 1000f;

        // orient manually for the swank
        startPos = new Vector3(2f, 4f, 10f);
        endPos   = new Vector3(2f, 4f, 100f);
        startUp  = new Vector3(Vector3.Y.cpy());
        endUp    = new Vector3(Vector3.Y.cpy());
        startDir = new Vector3(0.50008386f,-0.656027f,-0.5652821f);
        endDir   = new Vector3(0.50008386f,-0.656027f,-0.5652821f);

        camera.position.set(startPos);
        camera.up.set(startUp);
        camera.direction.set(startDir);
        camera.update();
    }

    private void loadModels() {
        BoundingBox box = new BoundingBox();
        float extentX, extentY, extentZ, maxExtent;

        ModelInstance houseA = new ModelInstance(Assets.Models.house_a.model);
        houseA.calculateBoundingBox(box);
        extentX = (box.max.x - box.min.x);
        extentY = (box.max.y - box.min.y);
        extentZ = (box.max.z - box.min.z);
        maxExtent = Math.max(Math.max(extentX, extentY), extentZ);
        houseA.transform
                .setToTranslationAndScaling(
                        0.5f, 0f, 0.5f,
                        1f / maxExtent,
                        1f / maxExtent,
                        1f / maxExtent)
        ;

        ModelInstance houseB = new ModelInstance(Assets.Models.house_b.model);
        houseB.calculateBoundingBox(box);
        extentX = (box.max.x - box.min.x);
        extentY = (box.max.y - box.min.y);
        extentZ = (box.max.z - box.min.z);
        maxExtent = Math.max(Math.max(extentX, extentY), extentZ);
        houseB.transform
                .setToTranslationAndScaling(
                        1.5f, 0f, 0.5f,
                        1f / maxExtent,
                        1f / maxExtent,
                        1f / maxExtent)
        ;

        houseInstances = new Array<>();
        houseInstances.addAll(houseA, houseB);

        ModelInstance yeti = new ModelInstance(Assets.Models.yeti.model);
        yeti.calculateBoundingBox(box);
        extentX = (box.max.x - box.min.x);
        extentY = (box.max.y - box.min.y);
        extentZ = (box.max.z - box.min.z);
        maxExtent = Math.max(Math.max(extentX, extentY), extentZ);
        yeti.transform
                .setToTranslationAndScaling(
                        4f, 0f, 3f,
                        1f / maxExtent,
                        1f / maxExtent,
                        1f / maxExtent)
        ;

        creatureInstances = new Array<>();
        creatureInstances.add(yeti);

        coords = new ModelInstance(Assets.Models.coords.model);
        coords.transform.setToTranslation(0f, 0f, 0f);
    }

    private void loadDecals() {
        decals = new Array<>();
        freeParticleDecals = new Array<>();

        float height = 0.25f;

        decals.add(new AnimationDecal(assets, ImageInfo.Babe, 4, height, 8));
        decals.add(new AnimationDecal(assets, ImageInfo.Dude, 4, height, 10));
        decals.add(new AnimationDecal(assets, ImageInfo.Deer, 4, height, 6));
        decals.add(new AnimationDecal(assets, ImageInfo.Plow, 4, 1, -1));

        Decal decal;
        for (int i = 0; i < 10000; i++) {
            decal = Decal.newDecal(assets.particles.smoke, true);
            decal.setColor(1f, 1f, 1f, 0.3f);
            decal.setScale(0.01f);
            freeParticleDecals.add(decal);
        }
        PhysicsDecal.freeDecals = freeParticleDecals;
    }

    private float getAvalancheProgress() {
        float averageZ = 0f;
        for (Snowball ball : landscape.snowBalls) {
            averageZ += ball.position.z;
        }
        averageZ /= landscape.snowBalls.size;
        float progress = averageZ / (Landscape.TILES_LONG * Landscape.TILE_WIDTH);
        if (Float.isNaN(progress) || progress > 1f) {
            progress = 1f;
        }
        return progress;
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

    Vector2 hoverPos = new Vector2();
    Color pickColor = new Color();
    public Vector2 getSelectedTile(int screenX, int screenY) {
        if (pickPixmap == null) return hoverPos.set(-1, -1);
        int x = screenX;
        int y = screenY;
        pickColor.set(pickPixmap.getPixel(x/PICKMAP_SCALE, y/PICKMAP_SCALE));
        if (pickColor.a == 0) return hoverPos.set(-1, -1);
        int col = (int) (pickColor.r * (255f));
        int row = (int) (pickColor.g * (255f));
        return hoverPos.set(col, row);
    }

    private static class DebugElements {
        public static VisLabel fpsLabel;
        public static VisLabel javaHeapLabel;
        public static VisLabel nativeHeapLabel;
        public static VisLabel drawCallLabel;
        public static VisLabel simTimeLabel;
        public static VisLabel gamepadAxisLabel;
        public static VisLabel cameraLabel;
    }

    @Override
    protected void initializeUI() {
        super.initializeUI();
        initializeGameUI();
        //TODO: remove before launch (or just keep hidden)
        initializeDebugUI();
    }

    private void initializeGameUI() {
//        VisTable rootTable = new VisTable();
//        rootTable.setWidth(windowCamera.viewportWidth);
//        rootTable.setHeight(windowCamera.viewportHeight);

        VisProgressBar.ProgressBarStyle horizontalProgressBarStyle = skin.get("default-horizontal", VisProgressBar.ProgressBarStyle.class);
        VisProgressBar.ProgressBarStyle avalancheProgressBarStyle = new VisProgressBar.ProgressBarStyle(horizontalProgressBarStyle);
        avalancheProgressBarStyle.knob = new TextureRegionDrawable(assets.inputPrompts.get(InputPrompts.Type.button_light_tv));
        progressBar = new VisProgressBar(0f, 100f, 1f, false, avalancheProgressBarStyle);
        progressBar.setPosition(windowCamera.viewportWidth / 4f, windowCamera.viewportHeight - 50f);
        progressBar.setValue(0f);
        progressBar.setWidth(windowCamera.viewportWidth / 2f);
        progressBar.setHeight(50f);
        uiStage.addActor(progressBar);
    }

    private void updateProgressBarValue() {
        progressBar.setValue(camera.position.z);
    }

    private void initializeDebugUI() {
        debugWindow = new VisWindow("", true);
        debugWindow.setFillParent(true);
        debugWindow.setColor(1f, 1f, 1f, 0.4f);
        debugWindow.setKeepWithinStage(false);

        VisLabel label;

        label = new VisLabel();
        debugWindow.add(label).growX().row();
        DebugElements.fpsLabel = label;

        label = new VisLabel();
        debugWindow.add(label).growX().row();
        DebugElements.javaHeapLabel = label;

        label = new VisLabel();
        debugWindow.add(label).growX().row();
        DebugElements.nativeHeapLabel = label;

        label = new VisLabel();
        debugWindow.add(label).growX().row();
        DebugElements.drawCallLabel = label;

        label = new VisLabel();
        debugWindow.add(label).growX().row();
        DebugElements.simTimeLabel = label;

        label = new VisLabel();
        debugWindow.add(label).growX().row();
        DebugElements.gamepadAxisLabel = label;

        label = new VisLabel();
        debugWindow.add(label).growX().row();
        DebugElements.cameraLabel = label;

        uiStage.addActor(debugWindow);

        Actor rootActor = debugWindow;
        Action transitionAction = Actions.moveTo(0, -windowCamera.viewportHeight, 0.1f, Interpolation.exp10In);
        transitionAction.setActor(rootActor);
        debugWindow.addAction(transitionAction);
    }

    private void updateDebugElements() {
        DebugElements.fpsLabel.setText(Config.getFpsString());
        DebugElements.javaHeapLabel.setText(Config.getJavaHeapString());
        DebugElements.nativeHeapLabel.setText(Config.getNativeHeapString());
        DebugElements.drawCallLabel.setText(Config.getDrawCallString(batch));
//        DebugElements.cameraLabel.setText(
//                Stringf.format("Camera: up%s dir%s side%s",
//                camera.up, camera.direction, side.set(camera.up).crs(camera.direction).nor()
//        ));
    }

    private final Vector3 side = new Vector3();
    private void dumpCameraVecsToLog() {
        Gdx.app.log(TAG, Stringf.format("Camera: pos%s up%s dir%s side%s",
                camera.position, camera.up, camera.direction,
                side.set(camera.up).crs(camera.direction).nor())); }

}
