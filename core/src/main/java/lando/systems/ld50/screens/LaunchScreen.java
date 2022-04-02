package lando.systems.ld50.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class LaunchScreen extends BaseScreen {

    public void update(float dt) {
        if (!exitingScreen && Gdx.input.justTouched()){
            exitingScreen = true;
            game.setScreen(new TitleScreen());
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        ScreenUtils.clear(Color.SKY);
        OrthographicCamera camera = windowCamera;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        {
            assets.layout.setText(assets.largeFont, "Click to play", Color.WHITE, camera.viewportWidth, Align.center, false);
            assets.largeFont.draw(batch, assets.layout, 0, camera.viewportHeight / 2f + assets.layout.height);
        }
        batch.end();
    }

}
