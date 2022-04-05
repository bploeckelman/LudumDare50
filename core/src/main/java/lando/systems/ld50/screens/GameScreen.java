package lando.systems.ld50.screens;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quad;
import aurelienribon.tweenengine.equations.Sine;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
import lando.systems.ld50.objects.*;
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
//    private StringBuilder str = new StringBuilder();

    private final Color background = Color.SKY.cpy();
    public final PerspectiveCamera camera;
    private final ScreenShakeCameraController shaker;
    private final SimpleCameraController cameraController;
    public final Landscape landscape;
    private final RailsCamera railController;
    private final InputMultiplexer inputMuxPlanPhase, inputMuxAvalanchePhase;

    private final Environment env;
    private final ModelBatch modelBatch;
    private final DecalBatch decalBatch;
    private final DecalBatch particlesDecalBatch;

    private ModelInstance coords;
    public ModelInstance yetiModel;
    public Vector3 yetiPosition = new Vector3();
    private Array<ModelInstance> houseInstances;
    private Array<ModelInstance> treeInstances;
    public Array<ModelInstance> creatureInstances;
    private Array<ModelInstance> lodgeInstances;
    public Array<AnimationDecal> decals;
    public Array<Decal> decalsStatic = new Array<>();

    private Vector3 touchPos;
    private Vector3 startPos, endPos;
    private Vector3 startUp, endUp;
    private Vector3 startDir, endDir;
    private float timeInTransition = 0;
    private float transitionLength;
    private float transitionPostDelay = 0;

    private Vector3 lightDir;
    public DirectionalLight light;
    public Color ambientColor = new Color(.2f,.2f, .2f, 1f);
    public MutableFloat dayTime;
    public float buildHour = 9f;

    public FrameBuffer pickMapFBO;
    public Texture PickMapFBOTex;
    public Pixmap pickPixmap;

    private float cameraMovementT = 0;
    private boolean cameraMovementPaused = false;
    private final Vector3 billboardCameraPos = new Vector3();
    private VisWindow debugWindow;
    private VisProgressBar progressBar;
    private VisProgressBar karmaProgressBar;
    private VisSlider cameraSlider;
    private VisWindow nextDayWindow;
    private VisTextButton nextDayButton;
    private Group skillButtonGroup;
    private float accum = 0;
    public boolean isControlShown = false;
    private Button minimizeButton;
    public int roundNumber = 1;
    public int goodKarmaPoints = 0;
    public int badKarmaPoints = 0;

    public VisLabel roundLabel;
    public VisLabel goodKarmaLabel;
    public VisLabel badKarmaLabel;
    public VisLabel goodKarmaPointLabel;
    public VisLabel badKarmaPointLabel;

    private float ambienceSoundTime;
    private Vector3 position = new Vector3();

    public boolean gameOver;
    public float gameOverDelay = 0;

    public enum Karma {GOOD, EVIL}
    public Karma currentKarmaPicked = Karma.GOOD;
    public enum Skill {NONE, PLOW, RAMP, DIVERTER, BOULDER, LASER, HELI}
    public Skill activeSkill = Skill.NONE;

    public enum CameraPhase {
        transitionToRails, plan, transitionToAvalanche, avalanche, transitionViewLodge;
        static CameraPhase next(CameraPhase phase) {
            switch (phase) {
                case transitionToRails: return plan;
                case plan: return transitionToAvalanche;
                case transitionToAvalanche: return avalanche;
                case avalanche: return transitionViewLodge;
                default: return transitionToRails;
            }
        }
    }
    public CameraPhase currentCameraPhase = CameraPhase.plan;

    public GameScreen() {
//        profiler = new GLProfiler(Gdx.graphics);
//        profiler.enable();
        gameOver = false;
        camera = new PerspectiveCamera(70f, Config.window_width, Config.window_height);
        initializeCamera();

        shaker = new ScreenShakeCameraController(camera);
        cameraController = new SimpleCameraController(camera, this);
        railController = new RailsCamera(camera, this);

        lightDir = new Vector3(-1f, -.8f, -.2f);
        dayTime = new MutableFloat(buildHour);

        landscape = new Landscape(this);

        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, ambientColor));
        env.add(light = new DirectionalLight().set(0.8f, 0.8f, 0.8f, lightDir.x, lightDir.y, lightDir.z));
        modelBatch = new ModelBatch();
        decalBatch = new DecalBatch(500, new CameraGroupStrategy(camera));
        if (Gdx.app.getType() == Application.ApplicationType.WebGL) {
            particlesDecalBatch = new DecalBatch(5000, new NoDepthCameraGroupStrategy(camera, (o1, o2) -> {
                // sorting hurts the framerate significantly (especially on web)
                // and for particle effects we mostly don't care

                return (int)Math.signum(o1.getPosition().z - o2.getPosition().z);
            }));
        } else { // desktop
            particlesDecalBatch = new DecalBatch(5000, new CameraGroupStrategy(camera));
        }

        loadModels();
        loadDecals();

        touchPos = new Vector3();

        pickMapFBO =  new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth() / PICKMAP_SCALE, Gdx.graphics.getHeight() / PICKMAP_SCALE, true);
        PickMapFBOTex = pickMapFBO.getColorBufferTexture();

        inputMuxPlanPhase = new InputMultiplexer(uiStage, this, railController, cameraController);
        inputMuxAvalanchePhase = new InputMultiplexer(uiStage, this, cameraController);
        Gdx.input.setInputProcessor(inputMuxPlanPhase);

        game.audio.playMusic(AudioManager.Musics.mainTheme);
//        game.audio.musics.get(AudioManager.Musics.mainTheme).setVolume(0.1F);

        ambienceSoundTime = MathUtils.random(5f, 10f);

        game.audio.playSound(AudioManager.Sounds.rumble, 0.8F);
//        game.audio.playSound(AudioManager.Sounds.chaching);
    }

    public void addHouseModelInstance(ModelInstance instance) {
        houseInstances.add(instance);
    }

    public void removeModelInstance(ModelInstance instance) {
        // don't know which bucket it's in, so we'll just try to remove from all and cross our fingers
        houseInstances.removeValue(instance, true);
        treeInstances.removeValue(instance, true);
        creatureInstances.removeValue(instance, true);
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
        Gdx.input.setInputProcessor(inputMuxPlanPhase);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        accum += dt;
        yetiModel.transform.setTranslation(yetiPosition);

        if (gameOver && landscape.snowBalls.size == 0){
            gameOverDelay += dt;
            if (gameOverDelay > 2.5f && !exitingScreen){
                exitingScreen = true;
                Main.Transition.set(assets, Assets.Transition.heart);
                game.setScreen(new EpilogueScreen());
                game.audio.playMusic(AudioManager.Musics.outroMusic);
            }
        }
//        setGameDayTime(accum);
        updateGameTime();
        if (roundLabel != null){
            roundLabel.setText("Day " + roundNumber);
        }
        if (goodKarmaLabel != null) {
            goodKarmaPointLabel.setText("" + goodKarmaPoints);
        }
        if (badKarmaLabel != null) {
            badKarmaPointLabel.setText("" + badKarmaPoints);
        }

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

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            landscape.setGameOver();
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

//        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && currentCameraPhase == CameraPhase.transitionToRails) {
//            TransitionCamera();
//        }
//
//        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && currentCameraPhase == CameraPhase.avalanche) {
//            UntransitionCamera();
//        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && landscape.highlightedTile != null) {
            landscape.highlightedTile.makeRamp();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q) && landscape.highlightedTile != null) {
            landscape.highlightedTile.makeDiverter(false);
        }

        if (gameOver) {
            landscape.setSelectedTile(-1, -1);
        } else {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            worldCamera.unproject(touchPos);
            Vector2 selectedTile = getSelectedTile((int) touchPos.x, (int) touchPos.y);
            landscape.setSelectedTile((int) selectedTile.x, (int) selectedTile.y);
        }

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

        for (Decal decal : decalsStatic) {
            decalBatch.add(decal);
        }
        PhysicsDecal.updateAllDecalParticles(dt);
        for (PhysicsDecal decal : PhysicsDecal.instances) {
            decal.decal.lookAt(camera.position, camera.up);
            particlesDecalBatch.add(decal.decal);
        }

        if (pickPixmap != null){
            pickPixmap.dispose();
        }
/*
        // keep the camera focused on the bulk of the avalanche wave
        if (!cameraMovementPaused && currentCameraPhase == CameraPhase.transitionToRails) {
//            float prevCameraMovementT = cameraMovementT;
//            float currCameraMovementT = getAvalancheProgress();
//            cameraMovementT = MathUtils.lerp(prevCameraMovementT, currCameraMovementT, dt);
//            camera.position.set(
//                    MathUtils.lerp(startPos.x, endPos.x, cameraMovementT),
//                    MathUtils.lerp(startPos.y, endPos.y, cameraMovementT),
//                    MathUtils.lerp(startPos.z, endPos.z, cameraMovementT));
        } else if (currentCameraPhase == CameraPhase.plan) {
            cameraTransitionDT += dt;
            double frac = cameraTransitionDT / 2.5;
            if (frac >= 1) {
                frac = 1;
                currentCameraPhase = CameraPhase.next(currentCameraPhase);
                cameraTransitionDT = 0;
                if (currentCameraPhase == CameraPhase.transitionToRails) {
                    Gdx.input.setInputProcessor(inputMuxPlanPhase);
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

        } else if (currentCameraPhase == CameraPhase.avalanche) {
            float target = Math.max(10f, 100 * getAvalancheProgress() + 8.5f);
            camera.position.z = MathUtils.lerp(camera.position.z, target, 2*dt);
        }
*/

        switch (currentCameraPhase) {
            case transitionViewLodge:
                if (landscape.snowBalls.size > 0 || gameOver) {
                    timeInTransition = MathUtils.clamp(timeInTransition, 0, transitionLength);
                }

            case transitionToAvalanche:
            case transitionToRails:
                timeInTransition += dt;
                double frac = Math.min(1, timeInTransition / transitionLength);

                //perform easeInOutQuad with a quadratic bezier
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

                if (timeInTransition > transitionLength + transitionPostDelay) {
                    goToNextCameraPhase();
                }
                break;

            case avalanche:
                float target = Math.max(10f, Landscape.TILES_LONG * Landscape.TILE_WIDTH * getAvalancheProgress() + avalancheOffset);
                camera.position.z = MathUtils.lerp(camera.position.z, target, 2*dt);
                if (landscape.snowBalls.size == 0 || camera.position.z > avalancheOffset + 0.9 * Landscape.TILES_LONG * Landscape.TILE_WIDTH) {//TODO set avalanche percent to transition
                    goToNextCameraPhase();
                }
                break;

            case plan:
                //TODO time of day stuff(??)
                break;


        }

        camera.update();
        cameraController.update();
        shaker.update(dt);



        updateDebugElements();
        updateProgressBarValue();
        skillButtonGroup.setZIndex(skillButtonGroup.getZIndex() + 100);
        minimizeButton.setZIndex(minimizeButton.getZIndex() + 100);

        // Create periodic rumbles of avalanche

        ambienceSoundTime-= dt;


        if (ambienceSoundTime <= 2){
            game.audio.playSound(AudioManager.Sounds.rumble, 0.8f);
            ambienceSoundTime = MathUtils.random(4f, 10f);
        }
    }

    private void goToNextCameraPhase() {
        currentCameraPhase = CameraPhase.next(currentCameraPhase);
        switch (currentCameraPhase) {
            case transitionViewLodge:
                if (landscape.snowBalls.size > 0) {
                    camStartPos.set(camera.position);
                    camMidPos.set(1f, 3.4f, camera.position.z + 10);
                    camEndPos.set(1f, 4f, 105f);
                    camStartDir.set(camera.direction);
                    camEndDir.set(0.50008386f, -0.656027f, -0.5652821f);
                    camEndDir.nor();

                    transitionLength = 1.5f;
                    transitionPostDelay = 3.5f;
                    timeInTransition = 0;
                    break;
                } else { currentCameraPhase = CameraPhase.next(currentCameraPhase); }
            case transitionToRails:
                camStartPos.set(camera.position);
                camMidPos.set(4f, 3.4f, camera.position.z - 5);
                camEndPos.set(1f, 4f, 10f);
                camStartDir.set(camera.direction);
                camEndDir.set(0.50008386f,-0.656027f,-0.5652821f);
                camEndDir.nor();

                transitionLength = 2.5f;
                transitionPostDelay = 0f;
                timeInTransition = 0;
                break;
            case transitionToAvalanche:
                camStartPos.set(camera.position);
                camMidPos.set(3f, avalancheViewHeight + 0.8f, camera.position.z + 5);
                camEndPos.set(4f, avalancheViewHeight, 10f);
                camStartDir.set(camera.direction);
                camEndDir.set(0f, -(MathUtils.sinDeg(avalancheViewAngleDeg) / MathUtils.cosDeg(avalancheViewAngleDeg)), -1f);
                camEndDir.nor();

                transitionLength = 2.5f; // PETE
                transitionPostDelay = 0f;
                timeInTransition = 0;
                break;

            case avalanche:
                break;
            case plan:
                break;

        }
    }

    private float avalancheOffset = 12.5f;
    private float avalancheViewAngleDeg = 40f;
    private float avalancheViewHeight = 4.5f;

    Vector3 t1 = new Vector3();
    Vector3 t2 = new Vector3();
    Vector3 t3 = new Vector3();
    Vector3 t4 = new Vector3();
    Vector3 t5 = new Vector3();

    Vector3 camStartPos = new Vector3();
    Vector3 camMidPos = new Vector3();
    Vector3 camEndPos = new Vector3();
    Vector3 camStartDir = new Vector3();
    Vector3 camEndDir = new Vector3();

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

        // draw models
        modelBatch.begin(camera);
        {
//            modelBatch.render(coords, env);
            modelBatch.render(houseInstances, env);
            modelBatch.render(treeInstances, env);
            modelBatch.render(creatureInstances, env);
            modelBatch.render(lodgeInstances, env);

            landscape.renderAvalanche(modelBatch, env);
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
            particles.draw(batch, Particles.Layer.middle);
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
        hideNextDayWindow();
        if (landscape.snowBalls.size > 0) return;
        Timeline.createSequence()
                .push(
                        Tween.to(dayTime, 1, 2.125f) // PETE
                        .target(24f + buildHour)
                                .ease(Sine.IN))
                .push(Tween.call((type, source) -> {
                    dayTime.setValue(buildHour);
                    landscape.startAvalanche();
                    goToNextCameraPhase();
                }))
                .start(Main.game.tween);
    }

    public void beginBuildPhase(){
        roundNumber++;
        showNextDayWindow();
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
//            skyColor.set(.5f, .2f, 0, 1).lerp(Color.GOLDENROD, dayLight);
        } else {
            float dayLight = Utils.smoothStep(5.5f, 6.5f, duskTime);
            skyColor.set(0,0,0,1f).lerp(.5f, .2f, 0, 1, dayLight);
//            skyColor.set(0,0,0,1f).lerp(Color.GOLDENROD, dayLight);
        }
        light.setColor(skyColor);

    }

    public void renderBackground(SpriteBatch batch) {
        batch.setShader(Main.game.assets.backgroundShader);
        Main.game.assets.backgroundShader.setUniformf("u_time", duskTime);
        batch.draw(Main.game.assets.nightTex, 0, 0, windowCamera.viewportWidth, windowCamera.viewportHeight);
        batch.setShader(null);
    }

    public void addBadKarmaPoints(int points, LandTile tile) {
        Vector3 coords = new Vector3(tile.x+0.5f, 0.5f, tile.z+0.5f);
        camera.project(coords);

        badKarmaPoints += points;
        particles.addPointsParticles(points, coords.x, coords.y, 0.9f, 0.1f, 0.1f);
        particles.addParticleBurstCollect(16,
                new float[]{1f, 0.1f, 0.1f},
                new float[]{coords.x, coords.y},
                new float[]{Config.window_width - MathUtils.random(40f, 100f), Config.window_height - MathUtils.random(20f, 50f)});
    }

    public void addGoodKarmaPoints(int points, LandTile tile) {
        Vector3 coords = new Vector3(tile.x+0.5f, 0.5f, tile.z+0.5f);
        camera.project(coords);

        goodKarmaPoints += points;
        particles.addPointsParticles(points, coords.x, coords.y, 0.1f, 0.8f, 0.1f);
        particles.addParticleBurstCollect(6,
                new float[]{0.2f, 0.9f, 0.1f},
                new float[]{coords.x, coords.y},
                new float[]{Config.window_width - MathUtils.random(40f, 100f), Config.window_height - MathUtils.random(20f, 50f)});
    }

    public boolean isGameOver() {
        return gameOver;
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

    private void loadModels() {
        lodgeInstances = new Array<>();

        LandTile tile;
        ModelInstance instance;

        tile = landscape.getTileAt(0, Landscape.TILES_LONG - 1);
        instance = createUnitModelInstance(Assets.Models.lodge_a.model, 0f, 0f, 0f);
        tile.decorate(instance, 0f);
        lodgeInstances.add(instance);

        tile = landscape.getTileAt(1, Landscape.TILES_LONG - 1);
        instance = createUnitModelInstance(Assets.Models.lodge_b.model, 0f, 0f, 0f);
        tile.decorate(instance, 0f);
        lodgeInstances.add(instance);

        tile = landscape.getTileAt(2, Landscape.TILES_LONG - 1);
        instance = createUnitModelInstance(Assets.Models.lodge_c.model, 0f, 0f, 0f);
        tile.decorate(instance, 0f);
        lodgeInstances.add(instance);

        tile = landscape.getTileAt(3, Landscape.TILES_LONG - 1);
        instance = createUnitModelInstance(Assets.Models.lodge_d.model, 0f, 0f, 0f);
        tile.decorate(instance, 0f);
        lodgeInstances.add(instance);

        tile = landscape.getTileAt(4, Landscape.TILES_LONG - 1);
        instance = createUnitModelInstance(Assets.Models.lodge_c.model, 0f, 0f, 0f);
        tile.decorate(instance, 0f);
        lodgeInstances.add(instance);

        tile = landscape.getTileAt(5, Landscape.TILES_LONG - 1);
        instance = createUnitModelInstance(Assets.Models.lodge_b.model, 0f, 0f, 0f);
        tile.decorate(instance, 0f);
        lodgeInstances.add(instance);

        tile = landscape.getTileAt(6, Landscape.TILES_LONG - 1);
        instance = createUnitModelInstance(Assets.Models.lodge_c.model, 0f, 0f, 0f);
        tile.decorate(instance, 0f);
        lodgeInstances.add(instance);

        tile = landscape.getTileAt(7, Landscape.TILES_LONG - 1);
        instance = createUnitModelInstance(Assets.Models.lodge_a.model, 0f, 0f, 0f);
        tile.decorate(instance, 0f);
        // fuck it, it's ludum dare (screws up normals and makes it dark relative to others)
        instance.transform.scale(-1f, 1f, 1f);
        lodgeInstances.add(instance);

        // TODO - be more clever about how these are randomly placed so they don't cluster
        houseInstances = new Array<>();
        int numHouses = 20;
        for (int i = 0; i < numHouses; i++) {
            // create the instance
            instance = createUnitModelInstance(Assets.Models.randomHouse(), 0f, 0f, 0f);

            // find an undecorated landtile
            int excludedRows = 2;
            tile = getUndecoratedTile(excludedRows);

            // decorate it with the instance
            tile.decorate(instance);
            houseInstances.add(instance);
        }

        // TODO - /r/trees
        treeInstances = new Array<>();

        // TODO - place down by lodge (or maybe make it rise up from the ground or come running down with the final wave that destroys the lodge)
        // yeti statue
        creatureInstances = new Array<>();
        yetiModel = createUnitModelInstance(Assets.Models.yeti.model, 4f, 0f, 3f);


        coords = new ModelInstance(Assets.Models.coords.model);
        coords.transform.setToTranslation(0f, 0f, 0f);
    }

    private LandTile getUndecoratedTile(int excludedRows) {
        int x = MathUtils.random(0, Landscape.TILES_WIDE - 1);
        int z = MathUtils.random(excludedRows, Landscape.TILES_LONG - 1 - excludedRows);
        LandTile tile = landscape.getTileAt(x, z);
        while (tile.isDecorated()) {
            x = MathUtils.random(0, Landscape.TILES_WIDE - 1);
            z = MathUtils.random(excludedRows, Landscape.TILES_LONG - 1 - excludedRows);
            tile = landscape.getTileAt(x, z);
        }

        return tile;
    }

    private final BoundingBox box = new BoundingBox();
    private ModelInstance createUnitModelInstance(Model model, float posX, float posY, float posZ) {
        ModelInstance instance = new ModelInstance(model);
        instance.calculateBoundingBox(box);
        float extentX = (box.max.x - box.min.x);
        float extentY = (box.max.y - box.min.y);
        float extentZ = (box.max.z - box.min.z);
        float maxExtent = Math.max(Math.max(extentX, extentY), extentZ);
        instance.transform
                .setToTranslationAndScaling(
                        posX, posY, posZ,
                        1f / maxExtent,
                        1f / maxExtent,
                        1f / maxExtent);
        return instance;
    }

    private void loadDecals() {
        decals = new Array<>();

        int numPeople = 24;
        for (int i = 0; i < numPeople; i++) {
            LandTile tile = getUndecoratedTile(12);

            decals.add(getRandomPerson(tile.intX, tile.intZ));
        }
    }

    private AnimationDecal getRandomPerson(int x, int z) {
        ImageInfo personInfo;
        switch (MathUtils.random.nextInt(2)) {
            case 0:
                personInfo = ImageInfo.BabeA;
                break;
            default:
                personInfo = ImageInfo.BabeB;
                break;
        }

        AnimationDecal person = new AnimationDecal(assets, personInfo, landscape, x, z);
        person.autoMove = true;
        person.isPerson = true;
        person.moveToTile(landscape.getRandomX(), z);
        return person;
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
        float progress = firstZ / (Landscape.TILES_LONG * Landscape.TILE_WIDTH) * 100f;
//        if (Float.isNaN(progress) || progress > 1f) {
//            progress = 1f;
//        }
        return progress;
    }

    private int getLastRowWithSnowAccum() {
        int maxRow = Landscape.TILES_LONG;
        int maxCol = Landscape.TILES_WIDE;
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
        touchStart.set(screenX, screenY, 0);
        // ...
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (isGameOver()) return false;
        if (isSettingShown) return false;
        touchDrag.set(screenX, screenY, 0);
        if (touchStart.dst(touchDrag) > 15) { return false; }
        if (landscape.highlightedTile != null && !landscape.highlightedTile.isDecorated()) {
            switch (activeSkill) {
                case RAMP:
                    landscape.highlightedTile.makeRamp();
                    game.audio.playSound(AudioManager.Sounds.earth, .8F);
                    break;
                case PLOW:
                    addPlow(landscape);
                    game.audio.playSound(AudioManager.Sounds.goodKarma, 1.0F);
                    break;
                case HELI:
                    game.audio.playSound(AudioManager.Sounds.helicopter, 1.0F);
                    break;
                case DIVERTER:
                    landscape.highlightedTile.makeDiverter(true);
                    break;
                case BOULDER:
                    break;
                case LASER:
                    killPerson(landscape.highlightedTile);
                    game.audio.playSound(AudioManager.Sounds.laser);
                    break;
                case NONE:
                default:
                    break;
            }
            return true;
        }
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
        if (pickColor.a == 0 || pickColor.b == 0) return hoverPos.set(-1, -1);
        int col = (int) (pickColor.r * (255f));
        int row = (int) (pickColor.g * (255f));
        if (row < 4 || row > 96) row = -1;
        return hoverPos.set(col, row);
    }

    private boolean isPlowing = false;
    private void addPlow(Landscape landscape) {
        if (isPlowing) { return; }

        // add plow
    }

    private void killPerson(LandTile selectedTile) {
        for (AnimationDecal animationDecal : decals) {
            if (animationDecal.isPerson && animationDecal.isOn(selectedTile)) {
                // TODO: LAZER MOTHER FUCKER
                animationDecal.hit();
            }
        }
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
        initializeNextDayButtonUI();
    }

    private void setupUpperUIWindow(VisWindow upperWindow, float x, float y, float w, float h) {
        upperWindow.setPosition(x, y);
        upperWindow.setSize(w, h);
        upperWindow.setKeepWithinStage(false);
        upperWindow.setMovable(false);
    }

    private void initializeSettingsControlButton() {
//        VisImageButton.VisImageButtonStyle defaultStyle = skin.get("default", VisImageButton.VisImageButtonStyle.class);
//        VisImageButton.VisImageButtonStyle settingsButtonStyle = new VisImageButton.VisImageButtonStyle(defaultStyle);
//        settingsButtonStyle.up = new TextureRegionDrawable(assets.settingsIcon);
//        settingsButtonStyle.down = new TextureRegionDrawable(assets.settingsIcon);
//        settingsButtonStyle.over = new TextureRegionDrawable(assets.settingsIcon);
//
//
//        VisImageButton settingsButton = new VisImageButton(settingsButtonStyle);

//        VisWindow.WindowStyle defaultStyle = skin.get("default", VisWindow.WindowStyle.class);
//        VisWindow.WindowStyle upperUIStyle = new VisWindow.WindowStyle(defaultStyle);
//        upperUIStyle.background = Assets.Patch.glass.drawable;
//
//        VisWindow settingsWindow = new VisWindow("", upperUIStyle);
//        setupUpperUIWindow(settingsWindow, 0f, 0f, windowCamera.viewportWidth / 8f, windowCamera.viewportHeight / 8f);
//        settingsWindow.setWidth(windowCamera.viewportWidth / 8f);
//        settingsWindow.setPosition(0f, 0f);
        VisTextButton.VisTextButtonStyle outfitMediumStyle = skin.get("outfit-medium-20px", VisTextButton.VisTextButtonStyle.class);
        VisTextButton.VisTextButtonStyle settingsButtonStyle = new VisTextButton.VisTextButtonStyle(outfitMediumStyle);
        settingsButtonStyle.up = Assets.Patch.glass.drawable;
        settingsButtonStyle.down = Assets.Patch.glass_dim.drawable;
        settingsButtonStyle.over = Assets.Patch.glass_dim.drawable;

        VisTextButton settingsButton = new VisTextButton("Settings", settingsButtonStyle);
        settingsButton.setSize(windowCamera.viewportWidth / 8f, windowCamera.viewportHeight / 8f);
        settingsButton.setPosition(0f, 0f);

        //settingsButton.setSize(50f, 50f);
        //settingsButton.setPosition(25f, 7f / 8f * windowCamera.viewportHeight - settingsButton.getHeight() - 25f);

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSettings();
            }
        });
        uiStage.addActor(settingsButton);
//        settingsWindow.addActor(settingsButton);
//        uiStage.addActor(settingsWindow);
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
        upperRightWindow.align(Align.center | Align.top);

        //leftWindow
        roundLabel = new VisLabel("Round #" + roundNumber, "outfit-medium-40px");
        roundLabel.setAlignment(Align.center);
        roundLabel.setFillParent(true);
        upperLeftWindow.addActor(roundLabel);
        //centerWindow
        Group progressBarGroup = createAvalancheProgressBarUI();
        //rightWindow
        goodKarmaLabel = new VisLabel("Good Karma:", "outfit-medium-20px");
        badKarmaLabel = new VisLabel("Evil Karma:", "outfit-medium-20px");
        goodKarmaPointLabel = new VisLabel("" + goodKarmaPoints, "outfit-medium-20px-blue");
        badKarmaPointLabel = new VisLabel("" + badKarmaPoints, "outfit-medium-20px-red");

//        upperRightWindow.addActor(karmaScoreLabel);
//        upperRightWindow.row();
//        upperRightWindow.addActor(evilScoreLabel);
        //fuck this, it's ludumdare
        goodKarmaLabel.setPosition(upperRightWindow.getX() + 40f, upperRightWindow.getY() + 60f);
        badKarmaLabel.setPosition(upperRightWindow.getX() + 40f, upperRightWindow.getY() + 35f);
        goodKarmaPointLabel.setPosition(windowCamera.viewportWidth - 80f, upperRightWindow.getY() + 60f);
        badKarmaPointLabel.setPosition(windowCamera.viewportWidth - 80f, upperRightWindow.getY() + 35f);

        VisProgressBar.ProgressBarStyle verticalProgressBarStyle = skin.get("default-horizontal", VisProgressBar.ProgressBarStyle.class);
        VisProgressBar.ProgressBarStyle karmaProgressBarStyle = new VisProgressBar.ProgressBarStyle(verticalProgressBarStyle);
        karmaProgressBarStyle.knobBefore = new TextureRegionDrawable(getColoredTextureRegion(Color.BLUE));
        karmaProgressBarStyle.knobAfter = new TextureRegionDrawable(getColoredTextureRegion(Color.RED));

        karmaProgressBar = new VisProgressBar(0f, 100f, .1f, false, karmaProgressBarStyle);
        karmaProgressBar.setSize(upperRightWindow.getWidth() - 50f, 20f);
        karmaProgressBar.setPosition(upperRightWindow.getX() + 25f, upperRightWindow.getY() + 10f);
        karmaProgressBar.setValue(50f);

        VisImage yeti = new VisImage(new TextureRegionDrawable(assets.yetiIcon));
        yeti.setPosition(karmaProgressBar.getX() + karmaProgressBar.getWidth() - 15f, karmaProgressBar.getY());
        yeti.setSize(35f, 35f);

        Scene2DAnimationActor babe = new Scene2DAnimationActor(assets.babeA);
        babe.setPosition(karmaProgressBar.getX() - 20f, karmaProgressBar.getY());
        babe.setSize(50f, 50f);

        uiStage.addActor(upperLeftWindow);
        uiStage.addActor(upperCenterWindow);
        uiStage.addActor(upperRightWindow);
        uiStage.addActor(progressBarGroup);
        uiStage.addActor(karmaProgressBar);
        uiStage.addActor(yeti);
        uiStage.addActor(babe);
        uiStage.addActor(goodKarmaLabel);
        uiStage.addActor(badKarmaLabel);
        uiStage.addActor(goodKarmaPointLabel);
        uiStage.addActor(badKarmaPointLabel);

    }

    private void initializeNextDayButtonUI() {
        VisWindow.WindowStyle defaultStyle = skin.get("default", VisWindow.WindowStyle.class);
        VisWindow.WindowStyle nextDayWindowStyle = new VisWindow.WindowStyle(defaultStyle);
        nextDayWindowStyle.background = Assets.Patch.glass.drawable;
        nextDayWindow = new VisWindow("", nextDayWindowStyle);
        nextDayWindow.setSize(windowCamera.viewportWidth / 4, windowCamera.viewportHeight / 4);
        nextDayWindow.setPosition(windowCamera.viewportWidth * 3 / 4, 0f);
        nextDayWindow.setKeepWithinStage(false);
        nextDayWindow.setMovable(false);

        nextDayButton = new VisTextButton("End Build", "outfit-medium-40px");
        nextDayButton.setFillParent(true);
        nextDayButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startNewDay();
                nextDayButton.setDisabled(true);
            }
        });


        nextDayWindow.addActor(nextDayButton);

        uiStage.addActor(nextDayWindow);
    }

    private void showNextDayWindow() {
        Action maximizeTransitionAction = Actions.moveBy(0, nextDayWindow.getHeight(), 0.5f);
        nextDayWindow.addAction(maximizeTransitionAction);
        nextDayButton.setDisabled(false);
    }

    private void hideNextDayWindow() {
        Action minimizeTransitionAction = Actions.moveBy(0, -nextDayWindow.getHeight(), 0.5f);
        nextDayWindow.addAction(minimizeTransitionAction);
        nextDayButton.setDisabled(true);
    }

    private void initializeControlUI() {
        float buttonSize = 35f;

//        VisWindow.WindowStyle defaultStyle = skin.get("default", VisWindow.WindowStyle.class);
//        VisWindow.WindowStyle controlUIStyle = new VisWindow.WindowStyle(defaultStyle);
//        controlUIStyle.background = Assets.Patch.glass.drawable;
//        VisWindow controlWindow = new VisWindow("", controlUIStyle);
//        controlWindow.setSize(windowCamera.viewportWidth / 4, windowCamera.viewportHeight / 4);
//        controlWindow.setPosition(0f, -controlWindow.getHeight() + buttonSize);
//        controlWindow.setKeepWithinStage(false);
//        controlWindow.setMovable(false);
        VisWindow.WindowStyle defaultStyle = skin.get("default", VisWindow.WindowStyle.class);
        VisWindow.WindowStyle controlUIStyle = new VisWindow.WindowStyle(defaultStyle);
        controlUIStyle.background = Assets.Patch.glass.drawable;
        VisWindow controlWindow = new VisWindow("", controlUIStyle);
        controlWindow.setSize(windowCamera.viewportWidth / 8, windowCamera.viewportHeight * 5 / 8);
        controlWindow.setPosition(0f, windowCamera.viewportHeight / 8f);
        controlWindow.setKeepWithinStage(false);
        controlWindow.setMovable(false);

        Button.ButtonStyle toggleButtonStyle = skin.get("toggle", Button.ButtonStyle.class);
        Button.ButtonStyle customMinimizeStyle = new Button.ButtonStyle(toggleButtonStyle);
        customMinimizeStyle.up = new TextureRegionDrawable(assets.inputPrompts.get(InputPrompts.Type.light_right_circle));
        customMinimizeStyle.checked = new TextureRegionDrawable(assets.inputPrompts.get(InputPrompts.Type.light_left_circle));
        minimizeButton = new Button(customMinimizeStyle);
        minimizeButton.setSize(buttonSize, buttonSize);
        minimizeButton.setPosition(controlWindow.getWidth() - minimizeButton.getWidth() / 2f, controlWindow.getY() + controlWindow.getHeight() / 2f);

        skillButtonGroup = new Group();

        VisTextButton.VisTextButtonStyle blueTextButtonStyle = skin.get("toggle", VisTextButton.VisTextButtonStyle.class);
        VisTextButton.VisTextButtonStyle redTextButtonStyle = new VisTextButton.VisTextButtonStyle(blueTextButtonStyle);
        //blue: {focusBorder: border-dark-blue, down: button-blue-down, up: button-blue, over: button-blue-over, disabled: button, font: default-font, fontColor: white, disabledFontColor: grey },
        blueTextButtonStyle.disabled = new TextureRegionDrawable(getColoredTextureRegion(Color.BLUE));
        blueTextButtonStyle.disabledFontColor = Color.WHITE;
        redTextButtonStyle.checked = new TextureRegionDrawable(getColoredTextureRegion(Color.RED));
        redTextButtonStyle.disabled = new TextureRegionDrawable(getColoredTextureRegion(Color.RED));
        redTextButtonStyle.disabledFontColor = Color.WHITE;

        VisTextButton karmaTabGood = new VisTextButton("Good", blueTextButtonStyle);
        VisTextButton karmaTabEvil = new VisTextButton("Evil", redTextButtonStyle);

        VisImageButton.VisImageButtonStyle toggleImageButtonStyle = skin.get("toggle", VisImageButton.VisImageButtonStyle.class);
        VisImageButton.VisImageButtonStyle rampButtonStyle = new VisImageButton.VisImageButtonStyle(toggleImageButtonStyle);
        rampButtonStyle.up = new TextureRegionDrawable(assets.rampIcon);
        rampButtonStyle.checked = new TextureRegionDrawable(assets.rampIcon);
        rampButtonStyle.over = new TextureRegionDrawable(assets.rampIcon);
        rampButtonStyle.down = new TextureRegionDrawable(assets.rampIcon);
        rampButtonStyle.focusBorder = Assets.Patch.glass_yellow.drawable;

        VisImageButton.VisImageButtonStyle diverterButtonStyle = new VisImageButton.VisImageButtonStyle(toggleImageButtonStyle);
        diverterButtonStyle.up = new TextureRegionDrawable(assets.diverterIcon);
        diverterButtonStyle.checked = new TextureRegionDrawable(assets.diverterIcon);
        diverterButtonStyle.over = new TextureRegionDrawable(assets.diverterIcon);
        diverterButtonStyle.down = new TextureRegionDrawable(assets.diverterIcon);
        diverterButtonStyle.focusBorder = Assets.Patch.glass_yellow.drawable;

        VisImageButton.VisImageButtonStyle boulderButtonStyle = new VisImageButton.VisImageButtonStyle(toggleImageButtonStyle);
        boulderButtonStyle.up = new TextureRegionDrawable(assets.boulderIcon);
        boulderButtonStyle.checked = new TextureRegionDrawable(assets.boulderIcon);
        boulderButtonStyle.over = new TextureRegionDrawable(assets.boulderIcon);
        boulderButtonStyle.down = new TextureRegionDrawable(assets.boulderIcon);
        boulderButtonStyle.focusBorder = Assets.Patch.glass_yellow.drawable;

        VisImageButton.VisImageButtonStyle laserButtonStyle = new VisImageButton.VisImageButtonStyle(toggleImageButtonStyle);
        laserButtonStyle.up = new TextureRegionDrawable(assets.laserIcon);
        laserButtonStyle.checked = new TextureRegionDrawable(assets.laserIcon);
        laserButtonStyle.over = new TextureRegionDrawable(assets.laserIcon);
        laserButtonStyle.down = new TextureRegionDrawable(assets.laserIcon);
        laserButtonStyle.focusBorder = Assets.Patch.glass_yellow.drawable;

        VisImageButton.VisImageButtonStyle plowButtonStyle = new VisImageButton.VisImageButtonStyle(toggleImageButtonStyle);
        plowButtonStyle.up = new TextureRegionDrawable(assets.plowIcon);
        plowButtonStyle.checked = new TextureRegionDrawable(assets.plowIcon);
        plowButtonStyle.over = new TextureRegionDrawable(assets.plowIcon);
        plowButtonStyle.down = new TextureRegionDrawable(assets.plowIcon);
        plowButtonStyle.focusBorder = Assets.Patch.glass_yellow.drawable;

        VisImageButton.VisImageButtonStyle heliButtonStyle = new VisImageButton.VisImageButtonStyle(toggleImageButtonStyle);
        heliButtonStyle.up = new TextureRegionDrawable(assets.heliIcon);
        heliButtonStyle.checked = new TextureRegionDrawable(assets.heliIcon);
        heliButtonStyle.over = new TextureRegionDrawable(assets.heliIcon);
        heliButtonStyle.down = new TextureRegionDrawable(assets.heliIcon);
        heliButtonStyle.focusBorder = Assets.Patch.glass_yellow.drawable;

        VisImageButton skillButton1 = new VisImageButton(rampButtonStyle);
        VisImageButton skillButton2 = new VisImageButton(plowButtonStyle);
        VisImageButton skillButton3 = new VisImageButton(heliButtonStyle);

        float margin = 10f;
        karmaTabGood.setSize(controlWindow.getWidth() / 2 - 10f, 30f);
        karmaTabGood.setPosition(controlWindow.getX() + 10f, controlWindow.getY() + controlWindow.getHeight() - karmaTabGood.getHeight() - margin);
        karmaTabGood.setBackground(new TextureRegionDrawable(getColoredTextureRegion(Color.BLUE)));
        karmaTabGood.setChecked(true);
        karmaTabGood.setDisabled(true);
        karmaTabEvil.setSize(controlWindow.getWidth() / 2 - 10f, 30f);
        karmaTabEvil.setPosition(controlWindow.getWidth() / 2 + controlWindow.getX(), controlWindow.getY() + controlWindow.getHeight() - karmaTabEvil.getHeight() - margin);
        karmaTabEvil.setBackground(new TextureRegionDrawable(getColoredTextureRegion(Color.RED)));

        float buttonHeight = controlWindow.getHeight() / 4f;
        float buttonWidth = buttonHeight;
        skillButton1.setSize(buttonWidth, buttonHeight);
        skillButton2.setSize(buttonWidth, buttonHeight);
        skillButton3.setSize(buttonWidth, buttonHeight);
        skillButton1.setPosition(controlWindow.getWidth() / 2f - buttonWidth / 2f, karmaTabGood.getY() - buttonHeight - margin * 2f);
        skillButton2.setPosition(controlWindow.getWidth() / 2f - buttonWidth / 2f, skillButton1.getY() - buttonHeight - margin);
        skillButton3.setPosition(controlWindow.getWidth() / 2f - buttonWidth / 2f, skillButton2.getY() - buttonHeight - margin);
        skillButtonGroup.addActor(skillButton1);
        skillButtonGroup.addActor(skillButton2);
        skillButtonGroup.addActor(skillButton3);
        skillButton1.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (skillButton1.isChecked()) {
                    switch (currentKarmaPicked) {
                        case GOOD:
                            activeSkill = Skill.RAMP;
                            break;
                        case EVIL:
                            activeSkill = Skill.DIVERTER;
                            break;
                    }
                    skillButton2.setChecked(false);
                    skillButton3.setChecked(false);
                    Gdx.app.log("Skill Click", activeSkill.name());
                }
            }
        });
        skillButton2.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (skillButton2.isChecked()) {
                    switch (currentKarmaPicked) {
                        case GOOD:
                            activeSkill = Skill.PLOW;
                            break;
                        case EVIL:
                            activeSkill = Skill.BOULDER;
                            break;
                    }
                    skillButton1.setChecked(false);
                    skillButton3.setChecked(false);
                    Gdx.app.log("Skill Click", activeSkill.name());
                }
            }
        });
        skillButton3.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (skillButton3.isChecked()) {
                    switch (currentKarmaPicked) {
                        case GOOD:
                            activeSkill = Skill.HELI;
                            break;
                        case EVIL:
                            activeSkill = Skill.LASER;
                            break;
                    }
                    skillButton1.setChecked(false);
                    skillButton2.setChecked(false);
                    Gdx.app.log("Skill Click", activeSkill.name());
                }
            }
        });

        karmaTabGood.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!karmaTabGood.isDisabled()) {
                    karmaTabEvil.setDisabled(false);
                    karmaTabEvil.setChecked(false);
                    karmaTabGood.setDisabled(true);
                    //karmaTabGood.setChecked(true);
                    currentKarmaPicked = Karma.GOOD;
                    activeSkill = Skill.NONE;
                    skillButton1.setChecked(false);
                    skillButton2.setChecked(false);
                    skillButton3.setChecked(false);
                    skillButton1.setStyle(rampButtonStyle);
                    skillButton2.setStyle(plowButtonStyle);
                    skillButton3.setStyle(heliButtonStyle);
                }
            }
        });
        karmaTabEvil.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!karmaTabEvil.isDisabled()) {
                    karmaTabGood.setDisabled(false);
                    karmaTabGood.setChecked(false);
                    karmaTabEvil.setDisabled(true);
                    //karmaTabGood.setChecked(true);
                    currentKarmaPicked = Karma.EVIL;
                    activeSkill = Skill.NONE;
                    skillButton1.setChecked(false);
                    skillButton2.setChecked(false);
                    skillButton3.setChecked(false);
                    skillButton1.setStyle(diverterButtonStyle);
                    skillButton2.setStyle(boulderButtonStyle);
                    skillButton3.setStyle(laserButtonStyle);
                }
            }
        });

        skillButtonGroup.addActor(karmaTabGood);
        skillButtonGroup.addActor(karmaTabEvil);

        Group controlGroup = new Group();
        controlGroup.addActor(controlWindow);
        controlGroup.addActor(minimizeButton);
        controlGroup.addActor(skillButtonGroup);

        uiStage.addActor(controlGroup);

        Action minimizeTransitionAction = Actions.moveBy(controlWindow.getWidth() - minimizeButton.getWidth() + 25f, 0f, 0.5f);
        Action maximizeTransitionAction = Actions.moveBy(-controlWindow.getWidth() + minimizeButton.getWidth() - 25f, 0f, 0.5f);

        minimizeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!isControlShown) {
                    maximizeTransitionAction.reset();
                    controlGroup.addAction(maximizeTransitionAction);
                    isControlShown = true;
                } else {
                    minimizeTransitionAction.reset();
                    controlGroup.addAction(minimizeTransitionAction);
                    isControlShown = false;
                }
            }
        });
        isControlShown = true;

    }

    private Group createAvalancheProgressBarUI() {
        VisProgressBar.ProgressBarStyle horizontalProgressBarStyle = skin.get("default-horizontal", VisProgressBar.ProgressBarStyle.class);
        VisProgressBar.ProgressBarStyle avalancheProgressBarStyle = new VisProgressBar.ProgressBarStyle(horizontalProgressBarStyle);
        avalancheProgressBarStyle.knob = new TextureRegionDrawable(assets.waveIcon);
        avalancheProgressBarStyle.background = new TextureRegionDrawable(getColoredTextureRegion(Color.FOREST));
        avalancheProgressBarStyle.knobBefore = new TextureRegionDrawable(getColoredTextureRegion(Color.LIGHT_GRAY));
        progressBar = new VisProgressBar(0f, 100f, .1f, false, avalancheProgressBarStyle);
        progressBar.setPosition(windowCamera.viewportWidth / 4f + 25f, windowCamera.viewportHeight * 7 / 8);
        progressBar.setValue(0f);
        progressBar.setWidth(windowCamera.viewportWidth / 2f - 50f);
        progressBar.setHeight(70f);
        uiStage.addActor(progressBar);

        VisSlider.SliderStyle horizontalSliderStyle = skin.get("default-horizontal", VisSlider.SliderStyle.class);
        VisSlider.SliderStyle cameraSliderStyle = new VisSlider.SliderStyle(horizontalSliderStyle);
        cameraSliderStyle.disabledKnob = new TextureRegionDrawable(assets.videoCameraIcon);
        cameraSliderStyle.background = new TextureRegionDrawable(getColoredTextureRegion(new Color(0f, 0f, 0f, 0f)));
        cameraSlider = new VisSlider(0f, 100f, 0.1f, false, cameraSliderStyle);
        cameraSlider.setPosition(windowCamera.viewportWidth / 4f + 50f, windowCamera.viewportHeight * 7 / 8);
        cameraSlider.setWidth(windowCamera.viewportWidth / 2f - 100f);
        cameraSlider.setHeight(70f);
        cameraSlider.setDisabled(true);
        uiStage.addActor(cameraSlider);

        VisLabel label = new VisLabel("Avalanche Progress", "outfit-medium-40px");
        label.setAlignment(Align.center);
        label.setPosition(windowCamera.viewportWidth / 4f + 25f, windowCamera.viewportHeight - 45f);
        label.setWidth(windowCamera.viewportWidth / 2f);

        VisImage skiLodge = new VisImage(new TextureRegionDrawable(assets.skiLodge));
        skiLodge.setPosition(cameraSlider.getX() + cameraSlider.getWidth() - 20f, cameraSlider.getY() + 15f);
        skiLodge.setSize(50f, 50f);

        Group progressBarGroup = new Group();
        progressBarGroup.addActor(progressBar);
        progressBarGroup.addActor(cameraSlider);
        progressBarGroup.addActor(skiLodge);
        progressBarGroup.addActor(label);
        return progressBarGroup;
    }

    private void updateProgressBarValue() {
        //Gdx.app.log("getLastRowWithSnowAccum", " " + getLastRowWithSnowAccum());
        progressBar.setValue(getFirstBallProgress());
        //progressBar.setValue(getLastRowWithSnowAccum());
        cameraSlider.setValue(camera.position.z / Landscape.TILES_LONG * Landscape.TILE_WIDTH * 100f);
        float goodPercentage;
        if (goodKarmaPoints == 0 && badKarmaPoints == 0) {
            goodPercentage = 50f;
        } else if (goodKarmaPoints == 0) {
            goodPercentage = 0f;
        } else {
            goodPercentage = (float) goodKarmaPoints / (goodKarmaPoints + badKarmaPoints) * 100f;
        }
        karmaProgressBar.setValue(goodPercentage);
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
