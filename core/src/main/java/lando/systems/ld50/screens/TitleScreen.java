package lando.systems.ld50.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.audio.AudioManager;
import lando.systems.ld50.utils.SimplePath;
import lando.systems.ld50.utils.accessors.Vector2Accessor;

public class TitleScreen extends BaseScreen {
    private VisTable rootTable;
    private VisTextButton startGameButton;
    private VisTextButton creditButton;
    private VisTextButton settingsButton;

    private Texture background;
    private Array<Animation<TextureRegion>> letterAnims;
    private Array<Vector2> letterPositions;
    private SimplePath lettersPath;
    private ShapeRenderer shapes;

    private Vector2 heloPos;
    private TextureRegion heloTex;

    private final float BUTTON_WIDTH = 300f;
    private final float BUTTON_HEIGHT = 75f;
    private final float BUTTON_PADDING = 10f;

    private float[] letterStopPoints;

    private float t = 0f;

    public TitleScreen() {
        // NOTE: have to set the input processor here as well as transitionCompleted
        //   so that it's set correctly in desktop mode where there's no transition
        InputMultiplexer mux = new InputMultiplexer(uiStage, this);
        Gdx.input.setInputProcessor(mux);

        shapes = new ShapeRenderer();
        background = new Texture("images/title-screen_00.png");

        float y = 720f;
        letterAnims = assets.titleLetters;
        lettersPath = new SimplePath(false,
                -100, y - 80,
                0, y - 180,
                130, y - 330,
                250, y - 600,
                500, y - 590,
                730, y - 490,
                820, y - 360,
                950, y - 290,
                1100, y - 340,
                1280, y - 480
        );

        letterPositions = new Array<>();
        for (int i = 0; i < letterAnims.size; i++) {
            letterPositions.add(new Vector2(lettersPath.valueAt(t)));
        }

        letterStopPoints = new float[] { .9f, .78f, .7f, .63f, .55f, .48f, .305f, .145f, 0};

        game.audio.playMusic(AudioManager.Musics.introMusic);
//        game.audio.playMusic(AudioManager.Musics.outroMusic);
        heloPos = new Vector2(300, 900);
        heloTex = game.assets.heloHook;

        Timeline.createSequence()
                .push(Tween.to(heloPos, Vector2Accessor.XY, 3f)
                        .target(300, -500))
                .pushPause(.1f)
                .push(Tween.call(new TweenCallback() {
                    @Override
                    public void onEvent(int type, BaseTween<?> source) {
                        heloTex = assets.heloSign;
                    }
                }))
                .push(Tween.to(heloPos, Vector2Accessor.XY, 2f)
                        .target(300, 300))
                .push(Tween.to(heloPos,Vector2Accessor.XY, .5f)
                        .target(300,280)
                        .repeatYoyo(60, 0))
                .start(game.tween);
    }

    @Override
    public void dispose() {
        super.dispose();
        background.dispose();
        shapes.dispose();
    }

    @Override
    protected void initializeUI() {
        super.initializeUI();

//        rootTable = new VisTable();
//        rootTable.setWidth(windowCamera.viewportWidth);
//        rootTable.setHeight(windowCamera.viewportHeight / 2f);
//        rootTable.setPosition(0, 0f);
//        rootTable.align(Align.center | Align.top);
//        rootTable.pad(5f);

        VisTextButton.VisTextButtonStyle outfitMediumStyle = skin.get("outfit-medium-40px", VisTextButton.VisTextButtonStyle.class);
        VisTextButton.VisTextButtonStyle titleScreenButtonStyle = new VisTextButton.VisTextButtonStyle(outfitMediumStyle);
        titleScreenButtonStyle.up = Assets.Patch.glass.drawable;
        titleScreenButtonStyle.down = Assets.Patch.glass_dim.drawable;
        titleScreenButtonStyle.over = Assets.Patch.glass_dim.drawable;

        startGameButton = new VisTextButton("Start Game", titleScreenButtonStyle);
        Gdx.app.log("startbuttonwidth&height", "width: " + startGameButton.getWidth() + " & height: " + startGameButton.getHeight());
        startGameButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        startGameButton.setPosition(windowCamera.viewportWidth / 2f - startGameButton.getWidth() / 2f + 325, windowCamera.viewportHeight / 3f);
        startGameButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            if (!exitingScreen) {
                audio.playSound(AudioManager.Sounds.goodKarma);
                game.setScreen(new StoryScreen());
                // TODO Change this back to story screen before publishing
//                game.setScreen(new GameScreen());
                exitingScreen = true;
            }
            }
        });

        settingsButton = new VisTextButton("Settings", titleScreenButtonStyle);
        settingsButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        settingsButton.setPosition(windowCamera.viewportWidth / 2f - settingsButton.getWidth() / 2f + 325, startGameButton.getY() - startGameButton.getHeight() - BUTTON_PADDING);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSettings();
            }
        });


        creditButton = new VisTextButton("Credits", titleScreenButtonStyle);
        creditButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        creditButton.setPosition(windowCamera.viewportWidth / 2f - creditButton.getWidth() / 2f + 325, settingsButton.getY() - settingsButton.getHeight() - BUTTON_PADDING);
        creditButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            if (!exitingScreen) {
                game.setScreen(new EndScreen());
//                game.setScreen(new EpilogueScreen());
                audio.playSound(AudioManager.Sounds.goodKarma);
                audio.playMusic(AudioManager.Musics.outroMusic);
                exitingScreen = true;
            }
            }
        });

//        rootTable.add(startGameButton).padBottom(BUTTON_PADDING).width(BUTTON_WIDTH).height(BUTTON_HEIGHT);
//        rootTable.row();
//        rootTable.add(settingsButton).padBottom(BUTTON_PADDING).width(BUTTON_WIDTH).height(BUTTON_HEIGHT);
//        rootTable.row();
//        rootTable.add(creditButton).padBottom(BUTTON_PADDING).width(BUTTON_WIDTH).height(BUTTON_HEIGHT);

        uiStage.addActor(startGameButton);
        uiStage.addActor(settingsButton);
        uiStage.addActor(creditButton);

//        settingsGroup.setZIndex(settingsGroup.getZIndex()+1);
    }

    @Override
    public void transitionCompleted() {
        super.transitionCompleted();
        InputMultiplexer mux = new InputMultiplexer(uiStage, this);
        Gdx.input.setInputProcessor(mux);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        t += 0.4f * dt;
        t = MathUtils.clamp(t, 0f, 1f);
        for (int i = 0; i < letterPositions.size; i++) {
            lettersPath.valueAt(letterPositions.get(i), MathUtils.clamp(t - letterStopPoints[i], 0, 1f));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            batch.draw(background, 0, 0, windowCamera.viewportWidth, windowCamera.viewportHeight);

            for (int i = 0; i < letterPositions.size; i++){
                if (letterPositions.get(i).x == 0) continue;
                TextureRegion keyframe = letterAnims.get(i).getKeyFrames()[0];
                batch.draw(keyframe,
                        letterPositions.get(i).x - keyframe.getRegionWidth() / 2f,
                        letterPositions.get(i).y - 20f);
            }

            batch.draw(heloTex, heloPos.x, heloPos.y, 200, 430);


//            assets.layout.setText(assets.font, "Avalaunch!", Color.LIGHT_GRAY, windowCamera.viewportWidth, Align.center, false);
//            assets.font.draw(batch, assets.layout, 0f, windowCamera.viewportHeight * (3f / 4f) + assets.layout.height / 2f);
        }
        batch.end();

        // TODO - comment out
//        shapes.setProjectionMatrix(windowCamera.combined);
//        lettersPath.debugRender(shapes);
//        shapes.end();

        uiStage.draw();
    }

    // ------------------------------------------------------------------------
    // Input handling
    // ------------------------------------------------------------------------

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
        audio.playSound(AudioManager.Sounds.goodKarma);
        game.setScreen(new GameScreen());
        return true;
    }

    private void exit() {
        Gdx.app.exit();
    }

}
