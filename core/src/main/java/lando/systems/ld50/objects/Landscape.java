package lando.systems.ld50.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld50.Main;
import lando.systems.ld50.physics.PhysicsManager;
import lando.systems.ld50.screens.GameScreen;
import com.badlogic.gdx.math.MathUtils;

public class Landscape {

    public static final int TILES_WIDE = 8;
    public static final int TILES_LONG = 100;
    public static final float TILE_WIDTH = 1f;

    public LandTile[] tiles;
    public Array<Snowball> snowBalls;

    private PhysicsManager physics;

    private final ShaderProgram landscapeShader;
    private final ShaderProgram ballShader;
    GameScreen screen;
    LandTile highlightedTile;

    public Landscape(GameScreen screen) {
        this.screen = screen;
        physics = new PhysicsManager(this);
        ballShader = Main.game.assets.ballShader;
        landscapeShader = Main.game.assets.landscapeShader;
        snowBalls = new Array<>();
        tiles = new LandTile[TILES_WIDE * TILES_LONG];
        for (int x = 0; x < TILES_WIDE; x++) {
            for (int y = 0; y < TILES_LONG; y++) {
                tiles[x + TILES_WIDE * y] = new LandTile(x, y, TILE_WIDTH);
            }
        }

        startAvalanche();
    }

    public void update(float dt) {
        physics.update(dt);
        for(LandTile tile : tiles) {
            tile.update(dt);
        }
        for (int i = snowBalls.size-1; i >= 0; i--){
            Snowball ball = snowBalls.get(i);
            ball.update(dt);

            if (ball.position.y < -3 || ball.radius < .1f){
                snowBalls.removeIndex(i);
            }
        }
    }

    public void render(ModelBatch batch, Environment env) {
        // TODO add land tiles
        for (Snowball ball : snowBalls) {
            ball.render(batch, env);
        }
    }

    public void renderFBO(Camera camera){
        ShaderProgram pickingShader = Main.game.assets.pickingShader;
        pickingShader.bind();

        pickingShader.setUniformMatrix("u_projTrans", camera.combined);

        for(LandTile tile : tiles) {
            tile.render(pickingShader);
        }
    }

    public void render(SpriteBatch batch, Camera camera) {
        batch.flush();
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        landscapeShader.bind();
        landscapeShader.setUniformi("u_texture", 0);

        Main.game.assets.noiseTex.bind(0);
        landscapeShader.setUniformMatrix("u_projTrans", camera.combined);

        for(LandTile tile : tiles) {
            tile.render(landscapeShader);
        }

        if (highlightedTile != null){
            highlightedTile.renderHighlight(camera);
        }

//        ballShader.bind();
//        ballShader.setUniformMatrix("u_projTrans", camera.combined);
//        for (Snowball ball : snowBalls){
//            ball.render(ballShader);
//        }
//
//        batch.setShader(null);

        batch.begin();
        batch.flush();
    }


    public void startAvalanche(){
//        snowBalls.clear();
        for (int i = 0; i < TILES_WIDE*3; i++){
            snowBalls.add(new Snowball(i/3f + 1/6f + MathUtils.random(-0.2f, 0.2f) , 1.5f, .5f, .3f + MathUtils.random(-0.06f, 0.12f)));
        }
    }


    public void getTilesAround(float x, float z, Array<LandTile> neighborTiles) {
        neighborTiles.clear();
        for (int dx = ((int)x)-1; dx <= x+1; dx++){
            for (int dy = ((int)z)-1; dy <= z+1; dy++){
                if (dx >= 0 && dx < TILES_WIDE && dy >= 0 && dy < TILES_LONG){
                    neighborTiles.add(tiles[dx + TILES_WIDE * dy]);
                }
            }
        }
    }

    public void setSelectedTile(int x, int z){
        if (x < 0 || z < 0) {
            highlightedTile = null;
        } else {
            highlightedTile = tiles[x + TILES_WIDE * z];
        }
    }
}
