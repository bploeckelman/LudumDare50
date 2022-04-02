package lando.systems.ld50.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld50.Main;

public class Landscape {
    public static int TILES_WIDE = 8;
    public static int TILES_LONG = 100;
    public static float TILE_WIDTH = 50;


    public LandTile[] tiles;
    ShaderProgram landscapeShader;


    public Landscape() {
        landscapeShader = Main.game.assets.landscapeShader;
        tiles = new LandTile[TILES_WIDE * TILES_LONG];
        for (int x = 0; x < TILES_WIDE; x++){
            for (int y = 0; y < TILES_LONG; y++){
                tiles[x + TILES_WIDE * y] = new LandTile(x, y, TILE_WIDTH);
            }
        }
    }

    public void update(float dt){

        for(LandTile tile : tiles){
            tile.update(dt);
        }

    }

    public void render(SpriteBatch batch, OrthographicCamera camera) {
        batch.flush();
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        landscapeShader.bind();
        landscapeShader.setUniformMatrix("u_projTrans", camera.combined);

        for(LandTile tile : tiles){
            tile.render(landscapeShader);
        }

        batch.begin();
        batch.flush();
    }
}
