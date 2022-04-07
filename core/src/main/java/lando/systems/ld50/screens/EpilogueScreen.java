package lando.systems.ld50.screens;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Expo;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import lando.systems.ld50.Config;
import lando.systems.ld50.Main;
import lando.systems.ld50.assets.Assets;

import static lando.systems.ld50.objects.LandTile.*;

public class EpilogueScreen extends BaseScreen {



    private static float textScale = 1.0f;
    float accum = 0;
    float backgroundAccum = 0;
    PerspectiveCamera perspectiveCamera;
    PerspectiveCamera backgroundCamera;
    Mesh landMesh;
    ModelInstance yetiModel;
    ModelInstance lodgeA;
    ModelInstance lodgeB;
    ModelInstance lodgeC;
    ModelInstance lodgeD;
    Environment env;
    ModelBatch modelBatch;

    GlyphLayout layout;

    Rectangle skipRect;
    Rectangle speedRect;
    boolean hoverSkip;
    boolean hoverSpeed;
    MutableFloat yetiPosY;


    String text = "\n\n\n\n\n\n\n\n... \n\nthe fuck was that, bro?\n\n\n" +
            "Spring Break was supposed to be about\n"+
            "shredding powder and spreading chowder\n"+
            "\n\n" +
            "NOT about some Michael-Bay-ass\n" +
            "ecological disaster type shit\n"+
            "\n\n" +
            "We should have been mad partying \n" +
            "with a bevy of righteous babes\n" +
            "\n" +
            "Instead, we were forced to \n" +
            "stare headlong into the \n" +
            "existential uncertainty of our\n" +
            "ever-encroaching mortality\n" +
            "while also confronting the bleakness \n" +
            "of trying to stave off a climate catastrophe\n" +
            "that is now almost certainly unavoidable.\n" +
            "\n" +
            "I don't think any of us were prepared \n" +
            "to process the realities of calamity\n" +
            "on a scale so massive that\n" +
            "our individual choices could \n" +
            "never even begin to avert it\n" +
            "\n" +
            "\n" +
            "The only thing left to do now is\n" +
            "try to reconcile our flawed perception \n" +
            "of existence as an ever-persistent state\n" +
            "with the knowledge that, like all things, \n" +
            "it too will eventually pass from this earth \n" +
            "into the cosmic bliss of impermanence.\n" +
            "\n" +
            "..." +
            "\n" +
            "\n" +
            "Also, the fuck was that yeti's problem?\n" +
            "Keep the cryptid-ass attitude to yourself, brah\n" +
            "\n" +
            "No room at the inn for your toxic vibes\n" +
            "\n\n"+
            "..."+
            "\n\n\n"+

            "This was straight-up NOT a good time, bro.\n" +
            "\n"+
            "Maybe next year\n" +
            "\n";


    public EpilogueScreen() {
//        super(game);
        layout = new GlyphLayout();
        game.assets.font.getData().setScale(textScale);
        layout.setText(game.assets.font, text, Color.WHITE, worldCamera.viewportWidth, Align.center, true);

        game.assets.font.getData().setScale(1.125f);
//        game.audio.playMusic(AudioManager.Musics.introMusic);

        perspectiveCamera = new PerspectiveCamera(90, 1280, 800);
        perspectiveCamera.near = .1f;
        perspectiveCamera.far=10000;
        perspectiveCamera.position.set(640, 0, 500);
        perspectiveCamera.lookAt(640, 00, 0);
        perspectiveCamera.update();
//        skipRect = new Rectangle(windowCamera.viewportWidth - 170, windowCamera.viewportHeight-70, 150, 50);
        speedRect = new Rectangle(windowCamera.viewportWidth - 370, windowCamera.viewportHeight-70, 350, 50);

        buildMesh();
        backgroundCamera = new PerspectiveCamera(70, Config.window_width, Config.window_height);
        backgroundCamera.position.set(MathUtils.sin(backgroundAccum/2f) * 40, 20, MathUtils.cos(backgroundAccum/2f) * 40);
        backgroundCamera.lookAt(0,0,0);
        backgroundCamera.near = .1f;
        backgroundCamera.far = 1000;
        backgroundCamera.update();
        yetiModel = createUnitModelInstance(Assets.Models.yeti.model, 2f, 0f, 3.5f);

        float offset = 1.5f;
        lodgeA = createUnitModelInstance(Assets.Models.lodge_a_snowed.model, offset + -3.03f, 0, 0);
        lodgeB = createUnitModelInstance(Assets.Models.lodge_b_snowed.model, offset + -2.02f, 0, 0);
        lodgeC = createUnitModelInstance(Assets.Models.lodge_c_snowed.model, offset + -1.01f, 0, 0);
        lodgeD = createUnitModelInstance(Assets.Models.lodge_d_snowed.model, offset + 0, 0, 0);

        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, .6f, .6f, .6f, 1f));
        env.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, .5f, -1, 0));
        modelBatch = new ModelBatch();

        yetiPosY = new MutableFloat(0f);
        Timeline.createSequence()
                .push(Tween.set(yetiPosY, -1).target(0f))
                .pushPause(0.2f)
                .push(Tween.to(yetiPosY, -1, 0.15f).target(0.6f).ease(Expo.OUT))
                .repeatYoyo(-1, 0f)
                .start(game.tween);
    }

    Vector3 screenPos = new Vector3();
    public void update(float dt) {
        backgroundAccum += dt;
        float speedMultiplier = 1.0f;

        screenPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        windowCamera.unproject(screenPos);

        hoverSkip = false;
        hoverSpeed = false;

//        if (skipRect.contains(screenPos.x, screenPos.y)){
//            hoverSkip = true;
//        }
        if (speedRect.contains(screenPos.x, screenPos.y)){
            hoverSpeed = true;
        }


        if (Gdx.input.isTouched() && hoverSpeed){
            speedMultiplier = 10f;
        }
        accum += 55*dt * speedMultiplier;
//        accum = MathUtils.clamp(accum, 0, layout.height);
        if (accum > layout.height && Gdx.input.justTouched()) {
            launchGame();
        }
        if (accum >= layout.height) {
            launchGame();
        }
        if (Gdx.input.isTouched() && hoverSkip) {
            launchGame();
        }

        backgroundCamera.position.set(MathUtils.sin(backgroundAccum/3f) * 2, 2, MathUtils.cos(backgroundAccum/3f) * 2);
        backgroundCamera.lookAt(0,0,0);
        backgroundCamera.up.set(0, 1, 0);
        backgroundCamera.update();
    }

    private void launchGame() {
        if (!exitingScreen){
            exitingScreen = true;
            game.setScreen(new EndScreen());

        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClearColor(0, 0, 0, 1);

        ScreenUtils.clear(.3f, .3f, 1f, 1f, true);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);


        renderBackground(batch);

        batch.setProjectionMatrix(perspectiveCamera.combined);
        batch.begin();


//        batch.draw(assets.backgrounds.titleImage, 0,0, Config.window_width, Config.window_height);

        game.assets.font.getData().setScale(textScale);
        game.assets.font.setColor(.3f, .3f, .3f, 1.0f);
        game.assets.font.draw(batch, text, 5, accum-3, worldCamera.viewportWidth, Align.center, true);
        game.assets.font.setColor(Color.WHITE);
        game.assets.font.draw(batch, text, 0, accum, worldCamera.viewportWidth, Align.center, true);
        game.assets.font.getData().setScale(1.0f);
//        batch.draw(textTexture, 0, 0, 1024, layout.height);
        batch.end();

        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();

        BitmapFont font = assets.font;
//        if (hoverSkip){
//            Assets.Patch.glass_active.ninePatch.draw(batch, skipRect.x, skipRect.y, skipRect.width, skipRect.height);
//        } else {
//            Assets.Patch.glass_dim.ninePatch.draw(batch, skipRect.x, skipRect.y, skipRect.width, skipRect.height);
//        }
//        assets.layout.setText(font, "Skip", Color.GRAY, skipRect.width, Align.center, false);
//        font.draw(batch, assets.layout, skipRect.x, skipRect.y + (skipRect.height + assets.layout.height)/2f);

        if (hoverSpeed){
            Assets.Patch.glass_active.ninePatch.draw(batch, speedRect.x, speedRect.y, speedRect.width, speedRect.height);
        } else {
            Assets.Patch.glass_dim.ninePatch.draw(batch, speedRect.x, speedRect.y, speedRect.width, speedRect.height);
        }
        assets.layout.setText(font, "Hurry up, bro", Color.WHITE, speedRect.width, Align.center, false);
        font.draw(batch, assets.layout, speedRect.x, speedRect.y + (speedRect.height + assets.layout.height)/2f);

        batch.end();


    }


    public void renderBackground(SpriteBatch batch) {

        batch.setShader(null);

        ShaderProgram landscapeShader = game.assets.landscapeShader;
        landscapeShader.bind();
        landscapeShader.setUniformf("u_ambient", .2f, .2f, .2f, 1f);
        landscapeShader.setUniformf("u_lightColor", 1f, 1f, 1f, 1f);
        landscapeShader.setUniformf("u_lightDir", 0, -1, 0);
        landscapeShader.setUniformi("u_texture", 0);


        Main.game.assets.noiseTex.bind(0);
        landscapeShader.setUniformMatrix("u_projTrans", backgroundCamera.combined);
        landMesh.render(landscapeShader, GL20.GL_TRIANGLES, 0, 6);

        modelBatch.begin(backgroundCamera);
        yetiModel.transform.setTranslation(0, yetiPosY.floatValue(), -1);
        modelBatch.render(yetiModel, env);
        modelBatch.render(lodgeA, env);
        modelBatch.render(lodgeB, env);
        modelBatch.render(lodgeC, env);
        modelBatch.render(lodgeD, env);

        modelBatch.end();

        batch.begin();
        batch.setProjectionMatrix(windowCamera.combined);
        batch.setColor(0, 0, 0, .3f);
        batch.draw(assets.pixelRegion, 0, 0, windowCamera.viewportWidth, windowCamera.viewportHeight);
        batch.end();

    }

    public void buildMesh() {
        landMesh = new Mesh(true, 4*12, 6,
                new VertexAttribute(VertexAttributes.Usage.Position,           NUM_COMPONENTS_POSITION, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal,        NUM_COMPONENTS_NORMAL, "a_normal"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked,        NUM_COMPONENTS_COLOR, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, NUM_COMPONENTS_TEXTURE,  "a_texCoord0")
        );

        float [] vertices = new float[4 * 12];
        int verIndex = 0;
        vertices[verIndex++] = -100;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = -100;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 1; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = -100; // U
        vertices[verIndex++] = -100; // V

        vertices[verIndex++] = 100;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = -100;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 1; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = 100; // U
        vertices[verIndex++] = -100; // V

        vertices[verIndex++] = 100;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = 100;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 1; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = 100; // U
        vertices[verIndex++] = 100; // V

        vertices[verIndex++] = -100;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = 100;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 1; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = -100; // U
        vertices[verIndex++] = 100; // V

        short[] indices = new short[6];
        int index = 0;
        indices[index++] = 0;
        indices[index++] = 1;
        indices[index++] = 2;

        indices[index++] = 2;
        indices[index++] = 3;
        indices[index++] = 0;

        landMesh.setVertices(vertices);
        landMesh.setIndices(indices);

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
}
