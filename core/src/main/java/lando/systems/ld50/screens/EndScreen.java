package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import lando.systems.ld50.Config;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.utils.typinglabel.TypingLabel;

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
    private final String music = "{COLOR=gray}Sound by:{COLOR=white}\n {GRADIENT=white;gray}Pete Valeo{ENDGRADIENT}";
    private final String libgdx = "Made with {COLOR=red}<3{COLOR=white}\nand LibGDX";
    private final String disclaimer = "{GRADIENT=black;gray}Disclaimer:{ENDGRADIENT}  {GRADIENT=gold;yellow}{JUMP=.2}{WAVE=0.8;1.1;1.1}No bananas were harmed in the making of this game{ENDWAVE}{ENDJUMP}{ENDGRADIENT}";

    private float accum = 0f;

    Rectangle backButton;
    boolean hoverBack;

    Vector3 screenPos = new Vector3();

    public EndScreen() {
        super();

        titleLabel = new TypingLabel(game.assets.smallFont, title.toLowerCase(), 0f, Config.window_height / 2f + 290f);
        titleLabel.setWidth(Config.window_width);
        titleLabel.setFontScale(1f);

        themeLabel = new TypingLabel(game.assets.smallFont, theme.toLowerCase(), 0f, Config.window_height / 2f + 220f);
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
    }

    @Override
    public void update(float dt) {
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
    }

    @Override
    public void render(SpriteBatch batch) {

        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        {
            batch.draw(game.assets.pixel, 0, 0, Config.window_width, Config.window_height);

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

}
