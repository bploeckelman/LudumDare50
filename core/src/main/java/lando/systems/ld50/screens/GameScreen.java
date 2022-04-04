package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
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
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisWindow;
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
import lando.systems.ld50.utils.Utils;
import lando.systems.ld50.utils.screenshake.ScreenShakeCameraController;
import text.formic.Stringf;

public class GameScreen extends BaseScreen {

    private static final String TAG = GameScreen.class.getSimpleName();

    private static final int PICKMAP_SCALE = 16;

    public static class Stats {
        // TODO - add stats vars
    }

    private final GLProfiler profiler;

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

    private Vector3 touchPos;
    private Vector3 startPos, endPos;
    private Vector3 startUp, endUp;
    private Vector3 startDir, endDir;
    private int cameraPhase = 1;
    private float cameraTransitionDT = 0;

    private Vector3 lightDir;
    public DirectionalLight light;
    public Color ambientColor = new Color(.2f,.2f, .2f, 1f);

    public FrameBuffer pickMapFBO;
    public Texture PickMapFBOTex;
    public Pixmap pickPixmap;

    private float cameraMovementT = 0;
    private boolean cameraMovementPaused = false;
    private final Vector3 billboardCameraPos = new Vector3();
    private VisWindow debugWindow;
    private VisProgressBar progressBar;
    private VisSlider cameraSlider;
    private float accum = 0;

    public GameScreen() {
//        profiler = new GLProfiler(Gdx.graphics);
//        profiler.enable();

        camera = new PerspectiveCamera(70f, Config.window_width, Config.window_height);
        initializeCamera();

        cameraController = new SimpleCameraController(camera);
        shaker = new ScreenShakeCameraController(camera);

        lightDir = new Vector3(-1f, -.8f, -.2f);

        landscape = new Landscape(this);

        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, ambientColor));
        env.add(light = new DirectionalLight().set(0.8f, 0.8f, 0.8f, lightDir.x, lightDir.y, lightDir.z));
        modelBatch = new ModelBatch();
        decalBatch = new DecalBatch(5000, new CameraGroupStrategy(camera));

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

    StringBuilder str = new StringBuilder();

    @Override
    public void update(float dt) {
        super.update(dt);
        accum += dt;

        setGameDayTime(accum);

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

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && cameraPhase == 1) {
            TransitionCamera();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && cameraPhase == 3) {
            UntransitionCamera();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && landscape.highlightedTile != null) {
            landscape.highlightedTile.makeRamp();
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

        PhysicsDecal.updateAllDecalParticles(dt);
        for (PhysicsDecal decal : PhysicsDecal.instances) {
            decal.decal.lookAt(camera.position, camera.up);
            decalBatch.add(decal.decal);
        }

        if (pickPixmap != null){
            pickPixmap.dispose();
        }

        // keep the camera focused on the bulk of the avalanche wave
        if (!cameraMovementPaused && cameraPhase == 1) {
            float prevCameraMovementT = cameraMovementT;
            float currCameraMovementT = getAvalancheProgress();
            cameraMovementT = MathUtils.lerp(prevCameraMovementT, currCameraMovementT, dt);
            camera.position.set(
                    MathUtils.lerp(startPos.x, endPos.x, cameraMovementT),
                    MathUtils.lerp(startPos.y, endPos.y, cameraMovementT),
                    MathUtils.lerp(startPos.z, endPos.z, cameraMovementT));
        } else if (cameraPhase % 2 == 0) {
            cameraTransitionDT += dt;
            double frac = cameraTransitionDT / 2.5;
            if (frac >= 1) {
                frac = 1;
                cameraPhase++;
                cameraTransitionDT = 0;
            }
            double tVal = frac < 0.5 ? 2 * frac * frac : 1 - ((-2 * frac + 2)*(-2*frac + 2)) / 2;//1-((1-frac)*(1-frac)*(1-frac));
            t1.set(camStartPos);
            t2.set(camMidPos);
            t3.set(camEndPos);
            t4.set(camStartDir);
            t5.set(camEndDir);
            camera.position.set(
                    t1.scl((float)((1-tVal)*(1-tVal)))
                            .add(t2.scl((float)(2*tVal*(1-tVal))))
                                    .add(t3.scl((float)(tVal*tVal))));
            camera.direction.set(t4.scl((float)(1-frac)).add(t5.scl((float)frac)));
            double hor = Math.sqrt(camera.direction.x * camera.direction.x + camera.direction.z * camera.direction.z);
            camera.up.set((float) (-camera.direction.x * camera.direction.y / hor), (float) (hor), (float) (-camera.direction.z * camera.direction.y / hor));

        } else if (cameraPhase == 3) {
            float target = Math.max(10f, 100 * getAvalancheProgress() + 6f);
            camera.position.z = MathUtils.lerp(camera.position.z, target, 2*dt);
        }

        updateDebugElements();
        updateProgressBarValue();
    }

    Vector3 t1 = new Vector3();
    Vector3 t2 = new Vector3();
    Vector3 t3 = new Vector3();
    Vector3 t4 = new Vector3();
    Vector3 t5 = new Vector3();

    @Override
    public void render(SpriteBatch batch) {
        ScreenUtils.clear(background, true);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
//        Gdx.gl.glCullFace(GL20.GL_BACK);

        // draw world
//        batch.setProjectionMatrix(shaker.getCombinedMatrix());
//        batch.begin();
//        {
//            // draw normal (non-decal) sprites in the world
//            // NOTE - probably not worth using in our case
//        }
//        batch.end();

        // draw world
        landscape.render(camera);

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

        // draw non Scene2D ui stuff (if there is any)
//        batch.setProjectionMatrix(windowCamera.combined);
//        batch.begin();
//        {
//            // TODO -
//            batch.draw(PickMapFBOTex, 0, 100, 100, -100);
//        }
//        batch.end();
    }

    public void renderFrameBuffers(SpriteBatch batch) {
        pickMapFBO.begin();
        ScreenUtils.clear(Color.CLEAR, true);
        landscape.renderFBO(camera);
        pickPixmap =  Pixmap.createFromFrameBuffer(0, 0, pickMapFBO.getWidth(), pickMapFBO.getHeight());
        pickMapFBO.end();
    }

    Color skyColor = new Color();
    /**
     * Set the day night cycle 0-24 (12 is noon)
     * @param inTime
     */
    public void setGameDayTime(float inTime) {
        while (inTime < 0) inTime += 24;
        float time = inTime % 24f;
        float radTime = time / 24f * MathUtils.PI2;
        light.setDirection(MathUtils.sin(radTime), MathUtils.cos(radTime), -.2f);
        light.direction.nor();
        float duskTime = time > 12 ? 24 - time : time;
        if (duskTime > 6.5){
            float dayLight = Utils.smoothStep(6.5f, 7.5f, duskTime);
            skyColor.set(.5f, .2f, 0, 1).lerp(Color.WHITE, dayLight);
        } else {
            float dayLight = Utils.smoothStep(5.5f, 6.5f, duskTime);
            skyColor.set(0,0,0,1f).lerp(.5f, .2f, 0, 1, dayLight);
        }
        light.setColor(skyColor);

    }

    public boolean isGameOver() {
        return false;
    }

    private void initializeCamera() {
        camera.near = 0.1f;
        camera.far = 1000f;

        // orient manually for the swank
        startPos = new Vector3(1f, 4f, 10f);
        endPos   = new Vector3(1f, 4f, 10f + Landscape.TILE_WIDTH * Landscape.TILES_LONG);
        startUp  = new Vector3(Vector3.Y.cpy());
        endUp    = new Vector3(Vector3.Y.cpy());
        startDir = new Vector3(0.50008386f,-0.656027f,-0.5652821f);
        endDir   = new Vector3(0.50008386f,-0.656027f,-0.5652821f);

        camera.position.set(startPos);
        camera.up.set(startUp);
        camera.direction.set(startDir);
        camera.update();
    }

    Vector3 camStartPos = new Vector3();
    Vector3 camMidPos = new Vector3();
    Vector3 camEndPos = new Vector3();
    Vector3 camStartDir = new Vector3();
    Vector3 camEndDir = new Vector3();

    private void TransitionCamera() {
        cameraPhase = 2;
        camStartPos.set(camera.position);
        camMidPos.set(3f, 2f, camera.position.z + 5);
        camEndPos.set(4f, 1f, 10f);
        camStartDir.set(camera.direction);
        camEndDir.set(0f, -0.27f, -1f);
        camEndDir.nor();
    }

    private void UntransitionCamera() {
        cameraPhase = 0;
        camStartPos.set(camera.position);
        camMidPos.set(1f, 3f, camera.position.z + 5);
        camEndPos.set(1f, 4f, 10f);
        camStartDir.set(camera.direction);
        camEndDir.set(startDir);
        camEndDir.nor();
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

        AnimationDecal babe = new AnimationDecal(assets, ImageInfo.Babe, landscape, 0, 7);
        decals.add(babe);
//        decals.add(new AnimationDecal(assets, ImageInfo.Dude, landscape, 0, 4));
//        decals.add(new AnimationDecal(assets, ImageInfo.Deer, landscape, 0, 6));
//        decals.add(new AnimationDecal(assets, ImageInfo.Plow, landscape, 4, -1));

        babe.moveToTile(3, 7);

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
        initializeAvalancheProgressBarUI();
        //initializeControlUI();
    }

    private void initializeScoreUI() {
        VisLabel scoreLabel = new VisLabel("Score: ");
        VisLabel karmaPoint = new VisLabel("Karma: ");
    }

    private void initializeControlUI() {
        VisWindow controlWindow = new VisWindow("", "default");
        controlWindow.setPosition(0f, 0f);
        controlWindow.setSize(camera.viewportWidth / 4, camera.viewportHeight / 4);
        uiStage.addActor(controlWindow);
    }

    private void initializeAvalancheProgressBarUI() {
        VisProgressBar.ProgressBarStyle horizontalProgressBarStyle = skin.get("default-horizontal", VisProgressBar.ProgressBarStyle.class);
        VisProgressBar.ProgressBarStyle avalancheProgressBarStyle = new VisProgressBar.ProgressBarStyle(horizontalProgressBarStyle);
        avalancheProgressBarStyle.knob = new TextureRegionDrawable(assets.waveIcon);
        avalancheProgressBarStyle.background = new TextureRegionDrawable(getColoredTextureRegion(Color.FOREST));
        avalancheProgressBarStyle.knobBefore = new TextureRegionDrawable(getColoredTextureRegion(Color.LIGHT_GRAY));
        progressBar = new VisProgressBar(0f, 1f, 0.01f, false, avalancheProgressBarStyle);
        progressBar.setPosition(windowCamera.viewportWidth / 4f, windowCamera.viewportHeight - 50f);
        progressBar.setValue(0f);
        progressBar.setWidth(windowCamera.viewportWidth / 2f);
        progressBar.setHeight(70f);
        uiStage.addActor(progressBar);

        VisSlider.SliderStyle horizontalSliderStyle = skin.get("default-horizontal", VisSlider.SliderStyle.class);
        VisSlider.SliderStyle cameraSliderStyle = new VisSlider.SliderStyle(horizontalSliderStyle);
        cameraSliderStyle.knob = new TextureRegionDrawable(assets.inputPrompts.get(InputPrompts.Type.button_light_tv));
        cameraSliderStyle.background = new TextureRegionDrawable(getColoredTextureRegion(new Color(0f, 0f, 0f, 0f)));
        cameraSlider = new VisSlider(0f, 1f, 0.01f, false, cameraSliderStyle);
        cameraSlider.setPosition(windowCamera.viewportWidth / 4f, windowCamera.viewportHeight - 50f);
        cameraSlider.setWidth(windowCamera.viewportWidth / 2f);
        cameraSlider.setHeight(70f);
        uiStage.addActor(cameraSlider);
    }

    private void updateProgressBarValue() {
        progressBar.setValue(getAvalancheProgress());
        cameraSlider.setValue(camera.position.z / Landscape.TILES_LONG * Landscape.TILE_WIDTH);
    }

    private TextureRegion getColoredTextureRegion(Color color) {
        Pixmap pixMap = new Pixmap(100, 20, Pixmap.Format.RGBA8888);
        pixMap.setColor(color);
        pixMap.fill();
        TextureRegion textureRegion = new TextureRegion(new Texture(pixMap));
        pixMap.dispose();
        return textureRegion;
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

    protected void getStatus (final StringBuilder stringBuilder) {
        stringBuilder.setLength(0);
        stringBuilder.append("GL calls: ");
        stringBuilder.append(profiler.getCalls());
        Gdx.app.log(TAG, stringBuilder.toString());

        stringBuilder.setLength(0);
        stringBuilder.append("Draw calls: ");
        stringBuilder.append(profiler.getDrawCalls());
        Gdx.app.log(TAG, stringBuilder.toString());

        stringBuilder.setLength(0);
        stringBuilder.append("Shader switches: ");
        stringBuilder.append(profiler.getShaderSwitches());
        Gdx.app.log(TAG, stringBuilder.toString());

        stringBuilder.setLength(0);
        stringBuilder.append("Texture bindings: ");
        stringBuilder.append(profiler.getTextureBindings());
        Gdx.app.log(TAG, stringBuilder.toString());

        stringBuilder.setLength(0);
        stringBuilder.append("Vertices: ");
        stringBuilder.append(profiler.getVertexCount().total);
        Gdx.app.log(TAG, stringBuilder.toString());

        Gdx.app.log(TAG, "-----------------------------");

        profiler.reset();

        stringBuilder.setLength(0);
    }

}
