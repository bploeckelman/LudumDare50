package lando.systems.ld50.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.ld50.Config;
import lando.systems.ld50.utils.screenshake.SimplexNoise;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Assets implements Disposable {

    public enum Load { ASYNC, SYNC }

    public static TextureRegion pixelTexRegion;

    public boolean initialized;

    public SpriteBatch batch;
    public ShapeDrawer shapes;
    public GlyphLayout layout;
    public AssetManager mgr;
    public Particles particles;
    public TextureAtlas atlas;

    public I18NBundle strings;
    public InputPrompts inputPrompts;

    public BitmapFont font;
    public BitmapFont smallFont;
    public BitmapFont largeFont;

    public Texture pixel;
    public Texture noiseTex;
    public TextureRegion pixelRegion;
    public ShaderProgram landscapeShader;
    public ShaderProgram ballShader;
    public ShaderProgram pickingShader;
    public ShaderProgram highlightShader;

    public TextureRegion waveIcon;

    public Animation<TextureRegion> cat;
    public Animation<TextureRegion> dog;
    public SimplexNoise noise;

    public Music mainMusic;
    public Music mainTheme;
    public Music introMusic;

    public Animation<TextureRegion>[] numberParticles = new Animation[10];

    public Sound chachingSound;
    public Sound click1Sound;
    public Sound cheer1Sound;
    public Sound cheer2Sound;
    public Sound collapse1Sound;
    public Sound explode1Sound;
    public Sound explode2Sound;
    public Sound explode3Sound;
    public Sound explode4Sound;
    public Sound guitar1Sound;
    public Sound harp1Sound;
    public Sound helicopter1Sound;
    public Sound helicopter2Sound;
    public Sound laser1Sound;
    public Sound laser2Sound;
    public Sound rumble1Sound;
    public Sound rumble2Sound;
    public Sound rumble3Sound;
    public Sound rumble4Sound;
    public Sound thud1Sound;
    public Sound thud2Sound;
    public Sound screamFemaleSound;
    public Sound screamMaleSound;

    public enum Models {
        coords, boulder_a, house_a, house_b, snowball_a, snowball_b, tree_b, tree_d, yeti, sphere;
        public Model model;
    }

    public enum Patch {
        debug, panel, metal, glass,
        glass_green, glass_yellow, glass_dim, glass_active;
        public NinePatch ninePatch;
        public NinePatchDrawable drawable;
    }

    public static class Particles {
        public TextureRegion circle;
        public TextureRegion sparkle;
        public TextureRegion smoke;
        public TextureRegion ring;
        public TextureRegion dollar;
    }

    public enum Transition {
        blinds, circle, crosshatch, cube, dissolve, doom, doorway,
        dreamy, heart, pixelize, radial, ripple, starwars, stereo
    }
    public ObjectMap<Transition, ShaderProgram> transitionShaders;

    public Assets() {
        this(Load.SYNC);
    }

    public Assets(Load load) {
        initialized = false;

        // create a single pixel texture and associated region
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        {
            pixmap.setColor(Color.WHITE);
            pixmap.drawPixel(0, 0);
            pixel = new Texture(pixmap);
        }
        pixmap.dispose();
        pixelRegion = new TextureRegion(pixel);

        // set a static for
        Assets.pixelTexRegion = new TextureRegion(pixel);

        batch = new SpriteBatch();
        shapes = new ShapeDrawer(batch, pixelRegion);
        layout = new GlyphLayout();

        mgr = new AssetManager();
        {
            mgr.load("sprites/sprites.atlas", TextureAtlas.class);
            mgr.load("gui/uiskin.json", Skin.class);

            mgr.load("i18n/strings", I18NBundle.class);

            mgr.load("fonts/outfit-medium-20px.fnt", BitmapFont.class);
            mgr.load("fonts/outfit-medium-40px.fnt", BitmapFont.class);
            mgr.load("fonts/outfit-medium-80px.fnt", BitmapFont.class);

            // audio
            mgr.load("audio/musics/main.mp3", Music.class);
            mgr.load("audio/musics/music-maintheme.ogg", Music.class);
            mgr.load("audio/musics/music-intro.ogg", Music.class);

            mgr.load("audio/sounds/chaching.ogg", Sound.class);
//            mgr.load("audio/sounds/.ogg", Sound.class);
            mgr.load("audio/sounds/cheer1.ogg", Sound.class);
            mgr.load("audio/sounds/cheer2.ogg", Sound.class);
            mgr.load("audio/sounds/click1.ogg", Sound.class);
            mgr.load("audio/sounds/collapse1.ogg", Sound.class);
            mgr.load("audio/sounds/explode1.ogg", Sound.class);
            mgr.load("audio/sounds/explode2.ogg", Sound.class);
            mgr.load("audio/sounds/explode3.ogg", Sound.class);
            mgr.load("audio/sounds/explode4.ogg", Sound.class);
            mgr.load("audio/sounds/explode5.ogg", Sound.class);
            mgr.load("audio/sounds/guitar1.ogg", Sound.class);
            mgr.load("audio/sounds/harp1.ogg", Sound.class);
            mgr.load("audio/sounds/helicopter1.ogg", Sound.class);
            mgr.load("audio/sounds/helicopter2.ogg", Sound.class);
            mgr.load("audio/sounds/laser1.ogg", Sound.class);
            mgr.load("audio/sounds/laser2.ogg", Sound.class);
            mgr.load("audio/sounds/rumble1.ogg", Sound.class);
            mgr.load("audio/sounds/rumble2.ogg", Sound.class);
            mgr.load("audio/sounds/rumble3.ogg", Sound.class);
            mgr.load("audio/sounds/rumble4.ogg", Sound.class);
            mgr.load("audio/sounds/screamfemale.ogg", Sound.class);
            mgr.load("audio/sounds/thud1.ogg", Sound.class);
            mgr.load("audio/sounds/thud2.ogg", Sound.class);
//            mgr.load("audio/sounds/.ogg", Sound.class);


            mgr.load("images/noise.png", Texture.class);

            // models
            mgr.load("models/boulder-a.g3db", Model.class);
            mgr.load("models/house-a.g3db", Model.class);
            mgr.load("models/house-b.g3db", Model.class);
            mgr.load("models/snowball-a.g3db", Model.class);
            mgr.load("models/snowball-b.g3db", Model.class);
            mgr.load("models/tree-b.g3db", Model.class);
            mgr.load("models/tree-d.g3db", Model.class);
            mgr.load("models/yeti_00.g3db", Model.class);
        }

        if (load == Load.SYNC) {
            mgr.finishLoading();
            updateLoading();
        }
    }

    public float updateLoading() {
        if (!mgr.update()) return mgr.getProgress();
        if (initialized) return 1;

        noise = new SimplexNoise(16, .8f, 12);

        noiseTex = mgr.get("images/noise.png", Texture.class);
        noiseTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        atlas = mgr.get("sprites/sprites.atlas");
        strings = mgr.get("i18n/strings", I18NBundle.class);

        inputPrompts = new InputPrompts(this);

        smallFont = mgr.get("fonts/outfit-medium-20px.fnt");
        font      = mgr.get("fonts/outfit-medium-40px.fnt");
        largeFont = mgr.get("fonts/outfit-medium-80px.fnt");

        // get audio
        mainMusic = mgr.get("audio/musics/main.mp3", Music.class);
        mainTheme = mgr.get("audio/musics/music-maintheme.ogg", Music.class);
        introMusic = mgr.get("audio/musics/music-intro.ogg", Music.class);

        chachingSound = mgr.get("audio/sounds/chaching.ogg", Sound.class);
        click1Sound = mgr.get("audio/sounds/click1.ogg", Sound.class);
        collapse1Sound = mgr.get("audio/sounds/collapse1.ogg", Sound.class);
         explode1Sound = mgr.get("audio/sounds/explode1.ogg", Sound.class);
         explode2Sound = mgr.get("audio/sounds/explode2.ogg", Sound.class);
         explode3Sound = mgr.get("audio/sounds/explode3.ogg", Sound.class);
         explode4Sound = mgr.get("audio/sounds/explode4.ogg", Sound.class);
         guitar1Sound = mgr.get("audio/sounds/guitar1.ogg", Sound.class);
         harp1Sound = mgr.get("audio/sounds/harp1.ogg", Sound.class);
         helicopter1Sound = mgr.get("audio/sounds/helicopter1.ogg", Sound.class);
         helicopter2Sound = mgr.get("audio/sounds/helicopter2.ogg", Sound.class);
         laser1Sound = mgr.get("audio/sounds/laser1.ogg", Sound.class);
         laser2Sound = mgr.get("audio/sounds/laser2.ogg", Sound.class);
         rumble1Sound = mgr.get("audio/sounds/rumble1.ogg", Sound.class);
         rumble2Sound = mgr.get("audio/sounds/rumble1.ogg", Sound.class);
         rumble3Sound = mgr.get("audio/sounds/rumble3.ogg", Sound.class);
         rumble4Sound = mgr.get("audio/sounds/rumble4.ogg", Sound.class);
          screamFemaleSound = mgr.get("audio/sounds/screamfemale.ogg", Sound.class);
          thud1Sound = mgr.get("audio/sounds/thud1.ogg", Sound.class);
          thud2Sound = mgr.get("audio/sounds/thud2.ogg", Sound.class);
//          = mgr.get("audio/sounds/.ogg", Sound.class);
//          = mgr.get("audio/sounds/.ogg", Sound.class);
//          = mgr.get("audio/sounds/.ogg", Sound.class);
//          = mgr.get("audio/sounds/.ogg", Sound.class);

        cat = new Animation<>(0.1f, atlas.findRegions("pets/cat"), Animation.PlayMode.LOOP);
        dog = new Animation<>(0.1f, atlas.findRegions("pets/dog"), Animation.PlayMode.LOOP);

        waveIcon = atlas.findRegion("icons/wave");

        // initialize particle images
        particles = new Particles();
        particles.circle  = atlas.findRegion("particles/circle");
        particles.ring    = atlas.findRegion("particles/ring");
        particles.smoke   = atlas.findRegion("particles/smoke");
        particles.sparkle = atlas.findRegion("particles/sparkle");
        particles.dollar  = atlas.findRegion("particles/dollars");

        // initialize models
        Models.boulder_a.model  = mgr.get("models/boulder-a.g3db", Model.class);
        Models.house_a.model    = mgr.get("models/house-a.g3db", Model.class);
        Models.house_b.model    = mgr.get("models/house-b.g3db", Model.class);
        Models.snowball_a.model = mgr.get("models/snowball-a.g3db", Model.class);
        Models.snowball_b.model = mgr.get("models/snowball-b.g3db", Model.class);
        Models.tree_b.model     = mgr.get("models/tree-b.g3db", Model.class);
        Models.tree_d.model     = mgr.get("models/tree-d.g3db", Model.class);
        Models.yeti.model       = mgr.get("models/yeti_00.g3db", Model.class);

        // NOTE: these are special snowflakes, not loaded via AssetsManager so must be disposed manually
        ModelBuilder builder = new ModelBuilder();
        Models.coords.model = builder.createXYZCoordinates(1f, 0.1f, 0.5f, 4, GL20.GL_TRIANGLES,
                new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal);
        Models.sphere.model = builder.createSphere(1f, 1f, 1f, 5, 5,
                new Material(ColorAttribute.createDiffuse(Color.WHITE)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal);

        // initialize patch values
        Patch.debug.ninePatch        = new NinePatch(atlas.findRegion("ninepatch/debug"), 2, 2, 2, 2);
        Patch.panel.ninePatch        = new NinePatch(atlas.findRegion("ninepatch/panel"), 15, 15, 15, 15);
        Patch.glass.ninePatch        = new NinePatch(atlas.findRegion("ninepatch/glass"), 8, 8, 8, 8);
        Patch.glass_green.ninePatch  = new NinePatch(atlas.findRegion("ninepatch/glass-green"), 8, 8, 8, 8);
        Patch.glass_yellow.ninePatch = new NinePatch(atlas.findRegion("ninepatch/glass-yellow"), 8, 8, 8, 8);
        Patch.glass_dim.ninePatch    = new NinePatch(atlas.findRegion("ninepatch/glass-dim"), 8, 8, 8, 8);
        Patch.glass_active.ninePatch = new NinePatch(atlas.findRegion("ninepatch/glass-active"), 8, 8, 8, 8);
        Patch.metal.ninePatch        = new NinePatch(atlas.findRegion("ninepatch/metal"), 12, 12, 12, 12);

        for (int i = 0; i <= 9; ++i) {
            numberParticles[i] = new Animation<>(0.1f, atlas.findRegions("particles/font-points-" + i));
        }

        Patch.debug.drawable        = new NinePatchDrawable(Patch.debug.ninePatch);
        Patch.panel.drawable        = new NinePatchDrawable(Patch.panel.ninePatch);
        Patch.glass.drawable        = new NinePatchDrawable(Patch.glass.ninePatch);
        Patch.glass_green.drawable  = new NinePatchDrawable(Patch.glass_green.ninePatch);
        Patch.glass_yellow.drawable = new NinePatchDrawable(Patch.glass_yellow.ninePatch);
        Patch.glass_dim.drawable    = new NinePatchDrawable(Patch.glass_dim.ninePatch);
        Patch.glass_active.drawable = new NinePatchDrawable(Patch.glass_active.ninePatch);
        Patch.metal.drawable        = new NinePatchDrawable(Patch.metal.ninePatch);

        // initialize shaders
        String defaultVertexPath = "shaders/default.vert";
        {
            transitionShaders = new ObjectMap<>();
            transitionShaders.put(Transition.blinds,     loadShader(defaultVertexPath, "shaders/transitions/blinds.frag"));
            transitionShaders.put(Transition.circle,     loadShader(defaultVertexPath, "shaders/transitions/circlecrop.frag"));
            transitionShaders.put(Transition.crosshatch, loadShader(defaultVertexPath, "shaders/transitions/crosshatch.frag"));
            transitionShaders.put(Transition.cube,       loadShader(defaultVertexPath, "shaders/transitions/cube.frag"));
            transitionShaders.put(Transition.dissolve,   loadShader(defaultVertexPath, "shaders/transitions/dissolve.frag"));
            transitionShaders.put(Transition.doom,       loadShader(defaultVertexPath, "shaders/transitions/doomdrip.frag"));
            transitionShaders.put(Transition.doorway,    loadShader(defaultVertexPath, "shaders/transitions/doorway.frag"));
            transitionShaders.put(Transition.dreamy,     loadShader(defaultVertexPath, "shaders/transitions/dreamy.frag"));
            transitionShaders.put(Transition.heart,      loadShader(defaultVertexPath, "shaders/transitions/heart.frag"));
            transitionShaders.put(Transition.pixelize,   loadShader(defaultVertexPath, "shaders/transitions/pixelize.frag"));
            transitionShaders.put(Transition.radial,     loadShader(defaultVertexPath, "shaders/transitions/radial.frag"));
            transitionShaders.put(Transition.ripple,     loadShader(defaultVertexPath, "shaders/transitions/ripple.frag"));
            transitionShaders.put(Transition.starwars,   loadShader(defaultVertexPath, "shaders/transitions/starwars.frag"));
            transitionShaders.put(Transition.stereo,     loadShader(defaultVertexPath, "shaders/transitions/stereo.frag"));
        }

        landscapeShader = loadShader("shaders/default3d.vert", "shaders/landscape.frag");
        ballShader = loadShader("shaders/ball.vert", "shaders/ball.frag");
        pickingShader = loadShader("shaders/default3d.vert", "shaders/picking.frag");
        highlightShader = loadShader("shaders/default3d.vert", "shaders/highlight.frag");
        initialized = true;
        return 1;
    }

    @Override
    public void dispose() {
        mgr.dispose();
        batch.dispose();
        pixel.dispose();
        font.dispose();
        smallFont.dispose();
        largeFont.dispose();
        Models.coords.model.dispose();
        Models.sphere.model.dispose();
        transitionShaders.values().forEach(ShaderProgram::dispose);
    }

    // ------------------------------------------------------------------------

    public static ShaderProgram loadShader(String vertSourcePath, String fragSourcePath) {
        ShaderProgram.pedantic = false;
        ShaderProgram shaderProgram = new ShaderProgram(
                Gdx.files.internal(vertSourcePath),
                Gdx.files.internal(fragSourcePath));
        String log = shaderProgram.getLog();

        if (!shaderProgram.isCompiled()) {
            Gdx.app.error("LoadShader", "compilation failed:\n" + log);
            throw new GdxRuntimeException("LoadShader: compilation failed:\n" + log);
        } else if (Config.shader_debug_log){
            Gdx.app.debug("LoadShader", "ShaderProgram compilation log: " + log);
        }

        return shaderProgram;
    }

}
