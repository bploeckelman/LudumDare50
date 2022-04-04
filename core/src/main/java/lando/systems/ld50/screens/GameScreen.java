package lando.systems.ld50.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.widget.*;
import lando.systems.ld50.Config;
import lando.systems.ld50.Main;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.assets.ImageInfo;
import lando.systems.ld50.assets.InputPrompts;
import lando.systems.ld50.audio.AudioManager;
import lando.systems.ld50.cameras.RailsCamera;
import lando.systems.ld50.cameras.SimpleCameraController;
import lando.systems.ld50.objects.AnimationDecal;
import lando.systems.ld50.objects.LandTile;
import lando.systems.ld50.objects.Landscape;
import lando.systems.ld50.objects.Snowball;
import lando.systems.ld50.particles.NoDepthCameraGroupStrategy;
import lando.systems.ld50.particles.Particles;
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

//    private GLProfiler profiler;

    private final Color background = Color.SKY.cpy();
    private final PerspectiveCamera camera;
    private final ScreenShakeCameraController shaker;
    private final SimpleCameraController cameraController;
    private final Landscape landscape;
    private final RailsCamera railController;
    private final InputMultiplexer muxP1, muxPX;

    private final Environment env;
    private final ModelBatch modelBatch;
    private final DecalBatch decalBatch;
    private final DecalBatch particlesDecalBatch;

    private ModelInstance coords;
    private Array<ModelInstance> houseInstances;
    private Array<ModelInstance> treeInstances;
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
    public MutableFloat dayTime;

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
    private boolean isControlHidden = false;
    private Button minimizeButton;
    public int roundNumber = 1;
    public int karmaScore = 0;
    public int evilScore = 0;

    private float ambienceSoundTime;

    public GameScreen() {
//        profiler = new GLProfiler(Gdx.graphics);
//        profiler.enable();

        camera = new PerspectiveCamera(70f, Config.window_width, Config.window_height);
        initializeCamera();

        cameraController = new SimpleCameraController(camera);
        shaker = new ScreenShakeCameraController(camera);
        railController = new RailsCamera(camera);

        lightDir = new Vector3(-1f, -.8f, -.2f);
        dayTime = new MutableFloat(10);

        landscape = new Landscape(this);

        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, ambientColor));
        env.add(light = new DirectionalLight().set(0.8f, 0.8f, 0.8f, lightDir.x, lightDir.y, lightDir.z));
        modelBatch = new ModelBatch();
        decalBatch = new DecalBatch(500, new CameraGroupStrategy(camera));
        particlesDecalBatch = new DecalBatch(5000, new NoDepthCameraGroupStrategy(camera, (o1, o2) -> {
            // sorting hurts the framerate significantly (especially on web)
            // and for particle effects we mostly don't care
            return 0;
        }));

        loadModels();
        loadDecals();

        touchPos = new Vector3();

        pickMapFBO =  new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth() / PICKMAP_SCALE, Gdx.graphics.getHeight() / PICKMAP_SCALE, true);
        PickMapFBOTex = pickMapFBO.getColorBufferTexture();

        muxP1 = new InputMultiplexer(uiStage, this, railController, cameraController);
        muxPX = new InputMultiplexer(uiStage, this, cameraController);
        Gdx.input.setInputProcessor(muxP1);


        game.audio.playMusic(AudioManager.Musics.mainTheme);
//        game.audio.musics.get(AudioManager.Musics.mainTheme).setVolume(0.1F);

        ambienceSoundTime = MathUtils.random(5f, 10f);

        game.audio.playSound(AudioManager.Sounds.rumble, 0.8F);
//        game.audio.playSound(AudioManager.Sounds.chaching);
        System.out.println("Fired sound on GameScreen launch");

    }

    @Override
    public void dispose() {
        super.dispose();
        modelBatch.dispose();
        decalBatch.dispose();
        particlesDecalBatch.dispose();
    }

    @Override
    public void transitionCompleted() {
        super.transitionCompleted();
        Gdx.input.setInputProcessor(muxP1);
    }

    StringBuilder str = new StringBuilder();

    @Override
    public void update(float dt) {
        super.update(dt);
        accum += dt;

//        setGameDayTime(accum);
        updateGameTime();

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q) && landscape.highlightedTile != null) {
            landscape.highlightedTile.makeDiverter(false);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
            startNewDay();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            particles.addPointsParticles((int)MathUtils.random(40, 800), Gdx.input.getX(), Gdx.input.getY(), 0.86f, 1f, 0f);
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

        for (int i = decals.size - 1; i >= 0; i--) {
            AnimationDecal decal = decals.get(i);
            if (decal.dead) {
                decals.removeIndex(i);
            } else {
                //decal.lookAt(billboardCameraPos, camera.up);
                decal.update(dt);

                decalBatch.add(decal.get());
            }
        }

        PhysicsDecal.updateAllDecalParticles(dt);
        for (PhysicsDecal decal : PhysicsDecal.instances) {
            decal.decal.lookAt(camera.position, camera.up);
            particlesDecalBatch.add(decal.decal);
        }

        if (pickPixmap != null){
            pickPixmap.dispose();
        }

        // keep the camera focused on the bulk of the avalanche wave
        if (!cameraMovementPaused && cameraPhase == 1) {
//            float prevCameraMovementT = cameraMovementT;
//            float currCameraMovementT = getAvalancheProgress();
//            cameraMovementT = MathUtils.lerp(prevCameraMovementT, currCameraMovementT, dt);
//            camera.position.set(
//                    MathUtils.lerp(startPos.x, endPos.x, cameraMovementT),
//                    MathUtils.lerp(startPos.y, endPos.y, cameraMovementT),
//                    MathUtils.lerp(startPos.z, endPos.z, cameraMovementT));
        } else if (cameraPhase % 2 == 0) {
            cameraTransitionDT += dt;
            double frac = cameraTransitionDT / 2.5;
            if (frac >= 1) {
                frac = 1;
                cameraPhase++;
                cameraTransitionDT = 0;
                if (cameraPhase == 1) {
                    Gdx.input.setInputProcessor(muxP1);
                }
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
            float target = Math.max(10f, 100 * getAvalancheProgress() + 8.5f);
            camera.position.z = MathUtils.lerp(camera.position.z, target, 2*dt);
        }

        updateDebugElements();
        updateProgressBarValue();
        minimizeButton.setZIndex(minimizeButton.getZIndex() + 100);

        // Create periodic rumbles of avalanche

        ambienceSoundTime-= dt;


        if (ambienceSoundTime <= 2){
            game.audio.playSound(AudioManager.Sounds.rumble, 0.8f);
            ambienceSoundTime = MathUtils.random(4f, 10f);
        }
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

        // draw background
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            renderBackground(batch);
        }
        batch.end();

        // draw world
        landscape.render(camera);

        modelBatch.begin(camera);
        {
//            modelBatch.render(coords, env);
            modelBatch.render(houseInstances, env);
            modelBatch.render(treeInstances, env);
            modelBatch.render(creatureInstances, env);
            landscape.render(modelBatch, env);
        }
        modelBatch.end();

        decalBatch.flush();

        particlesDecalBatch.flush();

        // NOTE: always draw so the 'hide' transition is visible
        uiStage.draw();

        //draw non Scene2D ui stuff (if there is any)
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            //batch.draw(PickMapFBOTex, 0, 100, 100, -100);
            particles.draw(batch, Particles.Layer.foreground);
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

    public void startNewDay() {
        // TODO Make sure they can't be called a lot in a row
        if (landscape.snowBalls.size > 0) return;
        Timeline.createSequence()
                .push(
                        Tween.to(dayTime, 1, 3f)
                        .target(34f)
                                .ease(Linear.INOUT))
                .push(Tween.call((type, source) -> {
                    dayTime.setValue(10);
                    landscape.startAvalanche();
                    TransitionCamera();
                }))
                .start(Main.game.tween);
    }

    public void beginBuildPhase(){
        UntransitionCamera();
    }

    Color skyColor = new Color();
    /**
     * Set the day night cycle 0-24 (12 is noon)
     * @param inTime
     */
    public void setGameDayTime(float inTime) {
        dayTime.setValue(inTime);
    }

    private float duskTime = 0;
    public void updateGameTime() {
        float time = dayTime.floatValue() % 24f;
        while (time < 0) time += 24;
        float radTime = time / 24f * MathUtils.PI2;
        light.setDirection(MathUtils.sin(radTime), MathUtils.cos(radTime), -.2f);
        light.direction.nor();
        duskTime = time > 12 ? 24 - time : time;
        if (duskTime > 6.5){
            float dayLight = Utils.smoothStep(6.5f, 7.5f, duskTime);
            skyColor.set(.5f, .2f, 0, 1).lerp(Color.WHITE, dayLight);
        } else {
            float dayLight = Utils.smoothStep(5.5f, 6.5f, duskTime);
            skyColor.set(0,0,0,1f).lerp(.5f, .2f, 0, 1, dayLight);
        }
        light.setColor(skyColor);

    }

    public void renderBackground(SpriteBatch batch) {
        batch.setShader(Main.game.assets.backgroundShader);
        Main.game.assets.backgroundShader.setUniformf("u_time", duskTime);
        batch.draw(Main.game.assets.nightTex, 0, 0, windowCamera.viewportWidth, windowCamera.viewportHeight);
        batch.setShader(null);
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
        Gdx.input.setInputProcessor(muxPX);
        cameraPhase = 2;
        camStartPos.set(camera.position);
        camMidPos.set(3f, 2.75f, camera.position.z + 5);
        camEndPos.set(4f, 2f, 10f);
        camStartDir.set(camera.direction);
        camEndDir.set(0f, -0.577f, -1f);
        camEndDir.nor();
    }

    private void UntransitionCamera() {
        cameraPhase = 0;
        camStartPos.set(camera.position);
        camMidPos.set(1f, 3.4f, camera.position.z + 5);
        camEndPos.set(1f, 4f, 10f);
        camStartDir.set(camera.direction);
        camEndDir.set(startDir);
        camEndDir.nor();
    }

    private void loadModels() {
        BoundingBox box = new BoundingBox();
        float extentX, extentY, extentZ, maxExtent;

        houseInstances = new Array<>();

        // TODO - be more clever about how these are randomly placed
        //   so they don't cluster
        int numHouses = 20;
        for (int i = 0; i < numHouses; i++) {
            // create the instance
            Model model = Assets.Models.randomHouse();
            ModelInstance instance = new ModelInstance(model);
            instance.calculateBoundingBox(box);
            extentX = (box.max.x - box.min.x);
            extentY = (box.max.y - box.min.y);
            extentZ = (box.max.z - box.min.z);
            maxExtent = Math.max(Math.max(extentX, extentY), extentZ);
            instance.transform
                    .setToTranslationAndScaling(
                            0f, 0f, 0f,
                            1f / maxExtent,
                            1f / maxExtent,
                            1f / maxExtent)
            ;
            // get an undecorated landtile
            int excludedRows = 2;
            int x = MathUtils.random(0, Landscape.TILES_WIDE - 1);
            int z = MathUtils.random(excludedRows, Landscape.TILES_LONG - 1 - excludedRows);
            LandTile tile = landscape.getTileAt(x, z);
            while (tile.isDecorated()) {
                x = MathUtils.random(0, Landscape.TILES_WIDE - 1);
                z = MathUtils.random(excludedRows, Landscape.TILES_LONG - 1 - excludedRows);
                tile = landscape.getTileAt(x, z);
            }
            // decorate it
            tile.decorate(instance);
            houseInstances.add(instance);
        }

        ModelInstance treeB = new ModelInstance(Assets.Models.tree_b.model);
        treeB.calculateBoundingBox(box);
        extentX = (box.max.x - box.min.x);
        extentY = (box.max.y - box.min.y);
        extentZ = (box.max.z - box.min.z);
        maxExtent = Math.max(Math.max(extentX, extentY), extentZ) * 2f;
        treeB.transform
                .setToTranslationAndScaling(
                        0.5f, 0f, 0.5f,
                        1f / maxExtent,
                        1f / maxExtent,
                        1f / maxExtent)
        ;
        landscape.getTileAt(2, 0).decorate(treeB);

        ModelInstance treeD = new ModelInstance(Assets.Models.tree_d.model);
        treeD.calculateBoundingBox(box);
        extentX = (box.max.x - box.min.x);
        extentY = (box.max.y - box.min.y);
        extentZ = (box.max.z - box.min.z);
        maxExtent = Math.max(Math.max(extentX, extentY), extentZ) * 2f;
        treeD.transform
                .setToTranslationAndScaling(
                        0.5f, 0f, 0.5f,
                        1f / maxExtent,
                        1f / maxExtent,
                        1f / maxExtent)
        ;
        landscape.getTileAt(3, 0).decorate(treeD);

        treeInstances = new Array<>();
        treeInstances.addAll(treeB, treeD);

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

        decals.add(new AnimationDecal(assets, ImageInfo.BabeA, landscape, 0, 7));
        decals.add(new AnimationDecal(assets, ImageInfo.BabeB, landscape, 4, 5));
//        decals.add(new AnimationDecal(assets, ImageInfo.Dude, landscape, 0, 4));
//        decals.add(new AnimationDecal(assets, ImageInfo.Deer, landscape, 0, 6));
//        decals.add(new AnimationDecal(assets, ImageInfo.Plow, landscape, 4, -1));

        // real temp
        decals.get(0).moveToTile(3, 7);
        decals.get(1).moveToTile(2, 7);

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

    private float getFirstBallProgress() {
        float firstZ = 0f;
        for (Snowball ball : landscape.snowBalls) {
            if (ball.position.z > firstZ)
                firstZ = ball.position.z;
        }
        float progress = firstZ / (Landscape.TILES_LONG * Landscape.TILE_WIDTH);
        if (Float.isNaN(progress) || progress > 1f) {
            progress = 1f;
        }
        return progress;
    }

    private int getLastRowWithSnowAccum() {
        int maxRow = landscape.TILES_LONG;
        int maxCol = landscape.TILES_WIDE;
        float rowAverageSnow = 0f;
        float rowAccumSnow = 0f;
        int lastRowWithMoreThanHalfSnow = 0;

        //ignore first 5 rows since they can have no snow (I think they should have snow by default)
        for (int x = 3; x < maxRow; x++) {
            rowAccumSnow = 0f;
            for (int y = 0; y < maxCol; y++) {
                float averageSnowHeight = landscape.tiles[x + maxCol * y].getAverageSnowHeight();
                rowAccumSnow += averageSnowHeight;
            }
            rowAverageSnow = rowAccumSnow / maxCol;
            if (rowAverageSnow >= 0.04f) {
                Gdx.app.log("row average snow", "" + rowAccumSnow);
                lastRowWithMoreThanHalfSnow = x;
                continue;
            } else {
                return lastRowWithMoreThanHalfSnow;
            }
        }
        return lastRowWithMoreThanHalfSnow;
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
        initializeUpperUI();
        initializeControlUI();
        initializeSettingsControlButton();
    }

    private void setupUpperUIWindow(VisWindow upperWindow, float x, float y, float w, float h) {
        upperWindow.setPosition(x, y);
        upperWindow.setSize(w, h);
        upperWindow.setKeepWithinStage(false);
        upperWindow.setMovable(false);
    }

    private void initializeSettingsControlButton() {
        VisImageButton.VisImageButtonStyle defaultStyle = skin.get("default", VisImageButton.VisImageButtonStyle.class);
        VisImageButton.VisImageButtonStyle settingsButtonStyle = new VisImageButton.VisImageButtonStyle(defaultStyle);
        settingsButtonStyle.up = new TextureRegionDrawable(assets.settingsIcon);
        settingsButtonStyle.down = new TextureRegionDrawable(assets.settingsIcon);
        settingsButtonStyle.over = new TextureRegionDrawable(assets.settingsIcon);


        VisImageButton settingsButton = new VisImageButton("default");
        settingsButton.setSize(100f, 100f);
        settingsButton.setPosition(50f, 7 / 8 * windowCamera.viewportHeight - settingsButton.getHeight());

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSettings();
            }
        });

        uiStage.addActor(settingsButton);
    }

    private void initializeUpperUI() {
        VisWindow.WindowStyle defaultStyle = skin.get("default", VisWindow.WindowStyle.class);
        VisWindow.WindowStyle upperUIStyle = new VisWindow.WindowStyle(defaultStyle);
        upperUIStyle.background = Assets.Patch.glass.drawable;
        
        VisWindow upperLeftWindow = new VisWindow("", upperUIStyle);
        setupUpperUIWindow(upperLeftWindow, 0f, windowCamera.viewportHeight - windowCamera.viewportHeight / 8, windowCamera.viewportWidth / 4, windowCamera.viewportHeight / 8);
        VisWindow upperCenterWindow = new VisWindow("", upperUIStyle);
        setupUpperUIWindow(upperCenterWindow, windowCamera.viewportWidth / 4, windowCamera.viewportHeight - windowCamera.viewportHeight / 8, windowCamera.viewportWidth / 2, windowCamera.viewportHeight / 8);
        VisWindow upperRightWindow = new VisWindow("", upperUIStyle);
        setupUpperUIWindow(upperRightWindow,windowCamera.viewportWidth * 3 / 4, windowCamera.viewportHeight - windowCamera.viewportHeight / 8, windowCamera.viewportWidth / 4, windowCamera.viewportHeight / 8);

        //leftWindow
        VisLabel roundLabel = new VisLabel("Round #" + roundNumber, "outfit-medium-40px");
        roundLabel.setAlignment(Align.center);
        roundLabel.setFillParent(true);
        upperLeftWindow.addActor(roundLabel);
        //centerWindow
        Group progressBarGroup = createAvalancheProgressBarUI();
        //rightWindow
        VisLabel karmaScoreLabel = new VisLabel("Karma Score: " + karmaScore, "outfit-medium-20px");
        VisLabel evilScoreLabel = new VisLabel("Evil Score: " + evilScore, "outfit-medium-20px");
        upperRightWindow.addActor(karmaScoreLabel);
        upperRightWindow.row();
        upperRightWindow.addActor(evilScoreLabel);

        uiStage.addActor(upperLeftWindow);
        uiStage.addActor(upperCenterWindow);
        uiStage.addActor(upperRightWindow);
        uiStage.addActor(progressBarGroup);

    }

    private void initializeControlUI() {
        VisWindow.WindowStyle defaultStyle = skin.get("default", VisWindow.WindowStyle.class);
        VisWindow.WindowStyle controlUIStyle = new VisWindow.WindowStyle(defaultStyle);
        controlUIStyle.background = Assets.Patch.glass.drawable;
        VisWindow controlWindow = new VisWindow("", controlUIStyle);
        controlWindow.setPosition(0f, 0f);
        controlWindow.setSize(windowCamera.viewportWidth / 4, windowCamera.viewportHeight / 4);
        controlWindow.setKeepWithinStage(false);
        controlWindow.setMovable(false);

        Button.ButtonStyle toggleButtonStyle = skin.get("toggle", Button.ButtonStyle.class);
        Button.ButtonStyle customMinimizeStyle = new Button.ButtonStyle(toggleButtonStyle);
        customMinimizeStyle.checked = new TextureRegionDrawable(assets.inputPrompts.get(InputPrompts.Type.light_big_plus));
        customMinimizeStyle.up = new TextureRegionDrawable(assets.inputPrompts.get(InputPrompts.Type.light_big_minus));
        minimizeButton = new Button(customMinimizeStyle);
        minimizeButton.setSize(35f, 35f);
        minimizeButton.setPosition(controlWindow.getWidth() - minimizeButton.getWidth(), controlWindow.getHeight() - minimizeButton.getHeight());

        VisImageButton skillButton1 = new VisImageButton("default");
        VisImageButton skillButton2 = new VisImageButton("default");
        VisImageButton skillButton3 = new VisImageButton("default");

        float buttonMargin = 10f;
        float buttonWidth = controlWindow.getWidth() / 3f - 2 * buttonMargin;
        float buttonHeight = buttonWidth;
        skillButton1.setSize(buttonWidth, buttonHeight);
        skillButton2.setSize(buttonWidth, buttonHeight);
        skillButton3.setSize(buttonWidth, buttonHeight);
        skillButton1.setPosition(controlWindow.getX() + buttonMargin * 2f, buttonMargin * 2f);
        skillButton2.setPosition(skillButton1.getX() + buttonWidth + buttonMargin , buttonMargin * 2f);
        skillButton3.setPosition(skillButton2.getX() + buttonWidth + buttonMargin, buttonMargin * 2f);


        Group controlGroup = new Group();
        controlGroup.addActor(controlWindow);
        controlGroup.addActor(minimizeButton);
        controlGroup.addActor(skillButton1);
        controlGroup.addActor(skillButton2);
        controlGroup.addActor(skillButton3);

        uiStage.addActor(controlGroup);

        Action minimizeTransitionAction = Actions.moveBy(0, -controlWindow.getHeight() + minimizeButton.getHeight(), 0.5f);
        Action maximizeTransitionAction = Actions.moveBy(0, controlWindow.getHeight() - minimizeButton.getHeight(), 0.5f);

        minimizeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (isControlHidden) {
                    maximizeTransitionAction.reset();
                    controlGroup.addAction(maximizeTransitionAction);
                    isControlHidden = false;
                } else {
                    minimizeTransitionAction.reset();
                    controlGroup.addAction(minimizeTransitionAction);
                    isControlHidden = true;
                }
            }
        });




    }

    private Group createAvalancheProgressBarUI() {
        VisProgressBar.ProgressBarStyle horizontalProgressBarStyle = skin.get("default-horizontal", VisProgressBar.ProgressBarStyle.class);
        VisProgressBar.ProgressBarStyle avalancheProgressBarStyle = new VisProgressBar.ProgressBarStyle(horizontalProgressBarStyle);
        avalancheProgressBarStyle.knob = new TextureRegionDrawable(assets.waveIcon);
        avalancheProgressBarStyle.background = new TextureRegionDrawable(getColoredTextureRegion(Color.FOREST));
        avalancheProgressBarStyle.knobBefore = new TextureRegionDrawable(getColoredTextureRegion(Color.LIGHT_GRAY));
        progressBar = new VisProgressBar(0f, 1f, .05f, false, avalancheProgressBarStyle);
        progressBar.setPosition(windowCamera.viewportWidth / 4f + 25f, windowCamera.viewportHeight * 7 / 8);
        progressBar.setValue(0f);
        progressBar.setWidth(windowCamera.viewportWidth / 2f - 50f);
        progressBar.setHeight(70f);
        uiStage.addActor(progressBar);

        VisSlider.SliderStyle horizontalSliderStyle = skin.get("default-horizontal", VisSlider.SliderStyle.class);
        VisSlider.SliderStyle cameraSliderStyle = new VisSlider.SliderStyle(horizontalSliderStyle);
        cameraSliderStyle.disabledKnob = new TextureRegionDrawable(assets.inputPrompts.get(InputPrompts.Type.button_light_tv));
        cameraSliderStyle.background = new TextureRegionDrawable(getColoredTextureRegion(new Color(0f, 0f, 0f, 0f)));
        cameraSlider = new VisSlider(0f, 1f, 0.01f, false, cameraSliderStyle);
        cameraSlider.setPosition(windowCamera.viewportWidth / 4f + 25f, windowCamera.viewportHeight * 7 / 8);
        cameraSlider.setWidth(windowCamera.viewportWidth / 2f - 50f);
        cameraSlider.setHeight(70f);
        cameraSlider.setDisabled(true);
        uiStage.addActor(cameraSlider);

        VisLabel label = new VisLabel("Avalanche Progress", "outfit-medium-40px");
        label.setAlignment(Align.center);
        label.setPosition(windowCamera.viewportWidth / 4f + 25f, windowCamera.viewportHeight - 45f);
        label.setWidth(windowCamera.viewportWidth / 2f);

        Group progressBarGroup = new Group();
        progressBarGroup.addActor(progressBar);
        progressBarGroup.addActor(cameraSlider);
        progressBarGroup.addActor(label);
        return progressBarGroup;
    }

    private void updateProgressBarValue() {
        Gdx.app.log("getLastRowWithSnowAccum", " " + getLastRowWithSnowAccum());
        progressBar.setValue(getFirstBallProgress());
        //progressBar.setValue(getLastRowWithSnowAccum());
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

//    protected void getStatus (final StringBuilder stringBuilder) {
//        stringBuilder.setLength(0);
//        stringBuilder.append("GL calls: ");
//        stringBuilder.append(profiler.getCalls());
//        Gdx.app.log(TAG, stringBuilder.toString());
//
//        stringBuilder.setLength(0);
//        stringBuilder.append("Draw calls: ");
//        stringBuilder.append(profiler.getDrawCalls());
//        Gdx.app.log(TAG, stringBuilder.toString());
//
//        stringBuilder.setLength(0);
//        stringBuilder.append("Shader switches: ");
//        stringBuilder.append(profiler.getShaderSwitches());
//        Gdx.app.log(TAG, stringBuilder.toString());
//
//        stringBuilder.setLength(0);
//        stringBuilder.append("Texture bindings: ");
//        stringBuilder.append(profiler.getTextureBindings());
//        Gdx.app.log(TAG, stringBuilder.toString());
//
//        stringBuilder.setLength(0);
//        stringBuilder.append("Vertices: ");
//        stringBuilder.append(profiler.getVertexCount().total);
//        Gdx.app.log(TAG, stringBuilder.toString());
//
//        Gdx.app.log(TAG, "-----------------------------");
//
//        profiler.reset();
//
//        stringBuilder.setLength(0);
//    }

}
