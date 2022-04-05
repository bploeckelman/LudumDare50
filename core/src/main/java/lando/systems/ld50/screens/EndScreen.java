package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import lando.systems.ld50.utils.typinglabel.TypingLabel;

import static lando.systems.ld50.objects.LandTile.*;
import static lando.systems.ld50.objects.LandTile.NUM_COMPONENTS_TEXTURE;

public class EndScreen extends BaseScreen {

    private final TypingLabel titleLabel;
    private final TypingLabel themeLabel;
    private final TypingLabel leftCreditLabel;
    private final TypingLabel rightCreditLabel;
    private final TypingLabel thanksLabel;
    private final TypingLabel disclaimerLabel;

    private final Animation<TextureRegion> catAnimation;
    private final Animation<TextureRegion> dogAnimation;
    private final TextureRegion background;

    private final String title = "{GRADIENT=purple;cyan}Avalaunch{ENDGRADIENT}";
    private final String theme = "Made for Ludum Dare 50: Delay the Inevitable";

    private final String thanks = "{GRADIENT=purple;cyan}Thanks for playing our game!{ENDGRADIENT}";
    private final String developers = "{COLOR=gray}Developed by:{COLOR=white}\n {GRADIENT=white;gray}Brian Ploeckelman{ENDGRADIENT} \n {GRADIENT=white;gray}Doug Graham{ENDGRADIENT} \n {GRADIENT=white;gray}Jeffrey Hwang{ENDGRADIENT} \n {GRADIENT=white;gray}Brian Rossman{ENDGRADIENT} \n {GRADIENT=white;gray}Zander Rossman{ENDGRADIENT}";
    private final String artists = "{COLOR=gray}Art by:{COLOR=white}\n {GRADIENT=white;gray}Matt Neumann{ENDGRADIENT}";
    private final String emotionalSupport = "{COLOR=cyan}Emotional Support:{COLOR=white}\n  Asuka and Cherry";
    private final String music = "{COLOR=gray}Music and sound by:{COLOR=white}\n {GRADIENT=white;gray}Pete Valeo{ENDGRADIENT}";
    private final String libgdx = "Made with {COLOR=red}<3{COLOR=white}\nand LibGDX";
    private final String disclaimer = "{GRADIENT=black;gray}Disclaimer:{ENDGRADIENT}  {GRADIENT=gold;yellow}{JUMP=.2}{WAVE=0.8;1.1;1.1}No babes were harmed in the making of this game{ENDWAVE}{ENDJUMP}{ENDGRADIENT}";

    private float accum = 0f;

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

    Rectangle backButton;
    boolean hoverBack;

    Vector3 screenPos = new Vector3();

    public EndScreen() {
        super();

        titleLabel = new TypingLabel(game.assets.largeFont, title.toLowerCase(), 0f, Config.window_height / 2f + 290f);
        titleLabel.setWidth(Config.window_width);
        titleLabel.setFontScale(1f);

        themeLabel = new TypingLabel(game.assets.font, theme.toLowerCase(), 0f, Config.window_height / 2f + 220f);
        themeLabel.setWidth(Config.window_width);
        themeLabel.setFontScale(1f);

        leftCreditLabel = new TypingLabel(game.assets.smallFont, developers.toLowerCase() + "\n\n" + emotionalSupport.toLowerCase() + "\n\n", 75f, Config.window_height / 2f + 135f);
        leftCreditLabel.setWidth(Config.window_width / 2f - 150f);
        leftCreditLabel.setLineAlign(Align.left);
        leftCreditLabel.setFontScale(1f);


        catAnimation = game.assets.cat;
        dogAnimation = game.assets.dog;
        background = game.assets.atlas.findRegion("lando");

        rightCreditLabel = new TypingLabel(game.assets.smallFont, artists.toLowerCase() + "\n\n" + music.toLowerCase() + "\n\n" + libgdx.toLowerCase(), Config.window_width / 2 + 75f, Config.window_height / 2f + 135f);
        rightCreditLabel.setWidth(Config.window_width / 2f - 150f);
        rightCreditLabel.setLineAlign(Align.left);
        rightCreditLabel.setFontScale(1f);

        thanksLabel = new TypingLabel(game.assets.smallFont, thanks.toLowerCase(), 0f, 115f);
        thanksLabel.setWidth(Config.window_width);
        thanksLabel.setLineAlign(Align.center);
        thanksLabel.setFontScale(1f);

        disclaimerLabel = new TypingLabel(game.assets.smallFont, disclaimer, 0f, 50f);
        disclaimerLabel.setWidth(Config.window_width);
        thanksLabel.setLineAlign(Align.center);
        disclaimerLabel.setFontScale(.6f);

        backButton = new Rectangle(0, 0, 150, 50);

        buildMesh();
        backgroundCamera = new PerspectiveCamera(70, Config.window_width, Config.window_height);
        backgroundCamera.position.set(MathUtils.sin(backgroundAccum/2f) * 40, 20, MathUtils.cos(backgroundAccum/2f) * 40);
        backgroundCamera.lookAt(0,0,0);
        backgroundCamera.near = .001f;
        backgroundCamera.far = 1000;
        backgroundCamera.update();
        yetiModel = createUnitModelInstance(Assets.Models.yeti.model, 4f, 0f, 3f);

        lodgeA = createUnitModelInstance(Assets.Models.lodge_a.model, -3.03f, 0, 0);
        lodgeB = createUnitModelInstance(Assets.Models.lodge_b.model, -2.02f, 0, 0);
        lodgeC = createUnitModelInstance(Assets.Models.lodge_c.model, -1.01f, 0, 0);
        lodgeD = createUnitModelInstance(Assets.Models.lodge_d.model, 0, 0, 0);

        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, .6f, .6f, .6f, 1f));
        env.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, .5f, -1, 0));
        modelBatch = new ModelBatch();
    }

    @Override
    public void update(float dt) {
        backgroundAccum += dt;

        screenPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        windowCamera.unproject(screenPos);

        accum += dt;
        titleLabel.update(dt);
        themeLabel.update(dt);
        leftCreditLabel.update(dt);
        rightCreditLabel.update(dt);
        thanksLabel.update(dt);
        disclaimerLabel.update(dt);

        hoverBack = backButton.contains(screenPos.x, screenPos.y);

        if (Gdx.input.isTouched() && hoverBack) {
            game.setScreen(new TitleScreen());
        }

        backgroundCamera.position.set(MathUtils.sin(backgroundAccum/3f) * 5, 6, MathUtils.cos(backgroundAccum/3f) * 5);
        backgroundCamera.lookAt(0,0,0);
        backgroundCamera.up.set(0, 1, 0);
        backgroundCamera.update();
    }

    @Override
    public void render(SpriteBatch batch) {

        Gdx.gl.glClearColor(0, 0, 0, 1);

        ScreenUtils.clear(.3f, .3f, 1f, 1f, true);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);


        renderBackground(batch);

        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
//            batch.draw(game.assets.pixel, 0, 0, Config.window_width, Config.window_height);

            BitmapFont font = assets.font;
            if (hoverBack) {
                Assets.Patch.glass_active.ninePatch.draw(batch, backButton.x, backButton.y, backButton.width, backButton.height);
            } else {
                Assets.Patch.glass_dim.ninePatch.draw(batch, backButton.x, backButton.y, backButton.width, backButton.height);
            }
            assets.layout.setText(font, "Back", Color.WHITE, backButton.width, Align.center, false);
            font.draw(batch, assets.layout, backButton.x, backButton.y + (backButton.height + assets.layout.height)/2f);

            batch.setColor(0f, 0f, 0f, 0.2f);
            batch.draw(game.assets.pixelRegion, 25f, 130f, Config.window_width / 2f - 50f, 400f);
            batch.draw(game.assets.pixelRegion, Config.window_width / 2f + 25f, 130f, Config.window_width / 2f - 50f, 400f);

            batch.setColor(Color.WHITE);
            titleLabel.render(batch);
            themeLabel.render(batch);
            leftCreditLabel.render(batch);
            rightCreditLabel.render(batch);
            thanksLabel.render(batch);
            disclaimerLabel.render(batch);
            if (accum > 7.5) {
                TextureRegion catTexture = catAnimation.getKeyFrame(accum);
                TextureRegion dogTexture = dogAnimation.getKeyFrame(accum);
                batch.draw(catTexture, 285f, 180f);
                batch.draw(dogTexture, 120f, 175f);
            }
            batch.setColor(Color.WHITE);
        }
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
        yetiModel.transform.setTranslation(-1, (MathUtils.sin(backgroundAccum * 5f) + 1) * .5f, -1);
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
