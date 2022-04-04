package lando.systems.ld50.objects;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld50.Main;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.physics.PhysicsManager;
import lando.systems.ld50.screens.GameScreen;
import lando.systems.ld50.utils.accessors.Vector3Accessor;

import static lando.systems.ld50.objects.LandTile.*;

public class Landscape {

    public static final int TILES_WIDE = 8;
    public static final int TILES_LONG = 100;
    public static final float TILE_WIDTH = 1f;

    public LandTile[] tiles;
    public Array<Snowball> snowBalls;
    public Array<Debris> debris;

    private PhysicsManager physics;

    private final ShaderProgram landscapeShader;
    private final ShaderProgram ballShader;
    public GameScreen screen;
    public LandTile highlightedTile;
    Mesh landscapeMesh;
    private float[] vertices;
    private short[] indices;

    public Landscape(GameScreen screen) {
        this.screen = screen;
        physics = new PhysicsManager(this);
        ballShader = Main.game.assets.ballShader;
        landscapeShader = Main.game.assets.landscapeShader;
        snowBalls = new Array<>();
        debris = new Array<>();
        tiles = new LandTile[TILES_WIDE * TILES_LONG];
        vertices = new float[MAX_NUM_VERTICES * tiles.length + 8 * MAX_NUM_VERTICES];
        indices = new short[MAX_INDICES * tiles.length + 12];
        landscapeMesh = new Mesh(false, MAX_NUM_VERTICES * tiles.length + 8 * MAX_NUM_VERTICES, MAX_INDICES * tiles.length + 12,
                new VertexAttribute(VertexAttributes.Usage.Position,           NUM_COMPONENTS_POSITION, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal,        NUM_COMPONENTS_NORMAL, "a_normal"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked,        NUM_COMPONENTS_COLOR, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, NUM_COMPONENTS_TEXTURE,  "a_texCoord0")
        );
        for (int x = 0; x < TILES_WIDE; x++) {
            for (int y = 0; y < TILES_LONG; y++) {
                tiles[x + TILES_WIDE * y] = new LandTile(x, y, TILE_WIDTH, this);
            }
        }

        addWings();


//        startAvalanche();
    }

    public void update(float dt) {
        physics.update(dt);
        for(LandTile tile : tiles) {
            tile.update(dt);
        }
        for (int i = snowBalls.size-1; i >= 0; i--){
            Snowball ball = snowBalls.get(i);
            ball.update(dt);

            if (ball.position.z > TILES_LONG){
                snowBalls.removeIndex(i);
                // TODO: make these launch particles
            }
            if (ball.radius < .1f){
                snowBalls.removeIndex(i);
            }

            if (!screen.gameOver){
                if (snowBalls.size == 0){
                    screen.beginBuildPhase();
                }
            }

        }
        for (int i = debris.size-1; i >= 0; i--){
            Debris debrises = debris.get(i);
            debrises.update(dt);

            if (debrises.position.y < -3 || debrises.TTL <= 0){
                debris.removeIndex(i);
            }
        }
    }

    public void renderAvalanche(ModelBatch batch, Environment env) {
        // TODO add land tiles
        for (Snowball ball : snowBalls) {
            ball.render(batch, env);
        }
        for (Debris debrises : debris){
            debrises.render(batch, env);
        }
    }

    public void renderFBO(Camera camera){
        ShaderProgram pickingShader = Main.game.assets.pickingShader;
        pickingShader.bind();

        pickingShader.setUniformMatrix("u_projTrans", camera.combined);

        landscapeMesh.render(pickingShader, GL20.GL_TRIANGLES);

    }

    public void render(Camera camera) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        landscapeShader.bind();
        landscapeShader.setUniformf("u_ambient", screen.ambientColor);
        landscapeShader.setUniformf("u_lightColor", screen.light.color);
        landscapeShader.setUniformf("u_lightDir", screen.light.direction);
        landscapeShader.setUniformi("u_texture", 0);


        Main.game.assets.noiseTex.bind(0);
        landscapeShader.setUniformMatrix("u_projTrans", camera.combined);
        landscapeMesh.render(landscapeShader, GL20.GL_TRIANGLES, 0, indices.length);

        if (highlightedTile != null){
            highlightedTile.renderHighlight(camera);
        }
    }

    public void updateMesh(int x, int z, float[] verts, short[] indis) {
        int startVertIndex = (x + z *TILES_WIDE) * MAX_NUM_VERTICES;
        for (int i = 0; i < verts.length; i++) {
            vertices[startVertIndex+i] = verts[i];
        }
        int startIndexIndex = (x + z *TILES_WIDE) * MAX_INDICES;
        for (int i = 0; i < indis.length; i++) {
            indices[startIndexIndex+i] = indis[i];
        }
        landscapeMesh.setVertices(vertices);
        landscapeMesh.setIndices(indices);
    }

    public void addWings() {

        float wingHeight = 1f;

        int verIndex = (tiles.length) * MAX_NUM_VERTICES;
        vertices[verIndex++] = -2;
        vertices[verIndex++] = wingHeight;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 0; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = -2; // U
        vertices[verIndex++] = 0; // V

        vertices[verIndex++] = 0;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 0; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = 0; // U
        vertices[verIndex++] = 0; // V

        vertices[verIndex++] = 0;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = 100;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 0; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = 0; // U
        vertices[verIndex++] = 100; // V

        vertices[verIndex++] = -2;
        vertices[verIndex++] = wingHeight;
        vertices[verIndex++] = 100;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 0; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = -2; // U
        vertices[verIndex++] = 100; // V



        // Right Wing
        vertices[verIndex++] = TILES_WIDE;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 0; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = TILES_WIDE; // U
        vertices[verIndex++] = 0; // V

        vertices[verIndex++] = TILES_WIDE+2;
        vertices[verIndex++] = wingHeight;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 0; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = TILES_WIDE+2; // U
        vertices[verIndex++] = 0; // V


        vertices[verIndex++] = TILES_WIDE+2;
        vertices[verIndex++] = wingHeight;
        vertices[verIndex++] = 100;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 0; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = TILES_WIDE+2; // U
        vertices[verIndex++] = 100; // V

        vertices[verIndex++] = TILES_WIDE;
        vertices[verIndex++] = 0;
        vertices[verIndex++] = 100;
        vertices[verIndex++] = 0; // Normal X
        vertices[verIndex++] = 1; // Normal Y
        vertices[verIndex++] = 0; // Normal Z
        vertices[verIndex++] = 0; // r
        vertices[verIndex++] = -1; // g
        vertices[verIndex++] = -1; // b
        vertices[verIndex++] = 0; // a
        vertices[verIndex++] = TILES_WIDE; // U
        vertices[verIndex++] = 100; // V

        int indexStartIndex = (short)(tiles.length) * 9;
        int startIndexIndex = (tiles.length) * MAX_INDICES;

        indices[startIndexIndex++] = (short)(indexStartIndex + 0);
        indices[startIndexIndex++] = (short)(indexStartIndex + 1);
        indices[startIndexIndex++] = (short)(indexStartIndex + 2);
        indices[startIndexIndex++] = (short)(indexStartIndex + 2);
        indices[startIndexIndex++] = (short)(indexStartIndex + 3);
        indices[startIndexIndex++] = (short)(indexStartIndex + 0);

        indices[startIndexIndex++] = (short)(indexStartIndex + 4);
        indices[startIndexIndex++] = (short)(indexStartIndex + 5);
        indices[startIndexIndex++] = (short)(indexStartIndex + 6);
        indices[startIndexIndex++] = (short)(indexStartIndex + 6);
        indices[startIndexIndex++] = (short)(indexStartIndex + 7);
        indices[startIndexIndex++] = (short)(indexStartIndex + 4);
    }


    public void startAvalanche(){
        int numRows = 2;
        for (int j = 0; j < numRows; j++) {
            for (int i = 0; i < TILES_WIDE * 3; i++) {
                snowBalls.add(new Snowball(i / 3f + 1 / 6f + MathUtils.random(-0.2f, 0.2f), 1.5f, .5f, .3f + MathUtils.random(-0.06f, 0.12f), this));
            }
        }
    }

    public float getHeightAt(float x, float z) {
        if (x >= 0 && x < TILES_WIDE && z >= 0 && z <TILES_LONG){
            return tiles[(int)x + TILES_WIDE * (int)z].getHeightAt(x % 1f, z%1f);
        } else {
            return -1;
        }
    }

    public LandTile getTileAt(int x, int z) {
        if (x >= 0 && x < TILES_WIDE && z >= 0 && z <TILES_LONG){
            return tiles[x + TILES_WIDE * z];
        } else {
            return null;
        }
    }


    public void getTilesAround(float x, float z, Array<LandTile> neighborTiles) {
        neighborTiles.clear();
        for (int dx = ((int)x-1); dx <= x+1; dx++){
            for (int dy = ((int)z-1); dy <= z+1; dy++){
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

    public void setGameOver(){
        if (screen.gameOver) return; // only call once
        screen.gameOver = true;
        screen.creatureInstances.add(screen.yetiModel);
        screen.yetiPosition.set(4.5f, 0, 85f);
        Timeline.createParallel()
                .push(Tween.to(screen.yetiPosition, Vector3Accessor.XZ, 2f)
                        .target(4.5f, 93f))
                .push(Tween.to(screen.yetiPosition, Vector3Accessor.Y, .4f)
                        .target(.5f)
                        .repeatYoyo(40, 0))
                .start(Main.game.tween);
        int numRows = 6;
        for (int j = 0; j < numRows; j++) {
            for (int i = 0; i < TILES_WIDE * 3; i++) {
                Snowball s = new Snowball(i / 3f + 1 / 6f + MathUtils.random(-0.2f, 0.2f), 1.f, 85.5f - j, .3f + MathUtils.random(-0.06f, 0.12f), this);
                s.velocity.z = 20f;
                snowBalls.add(s);
            }
        }
    }

    public int getRandomX() {
        return MathUtils.random.nextInt(TILES_WIDE);
    }
}
