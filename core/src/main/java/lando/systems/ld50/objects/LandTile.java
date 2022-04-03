package lando.systems.ld50.objects;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import lando.systems.ld50.Main;
import lando.systems.ld50.physics.Triangle;
import lando.systems.ld50.utils.screenshake.SimplexNoise;

public class LandTile {
    private static final int NUM_COMPONENTS_POSITION = 3;
    private static final int NUM_COMPONENTS_NORMAL = 3;
    private static final int NUM_COMPONENTS_TEXTURE = 2;
    private static final int NUM_COMPONENTS_COLOR = 4;
    private static final int NUM_COMPONENTS_PER_VERTEX = NUM_COMPONENTS_POSITION + NUM_COMPONENTS_TEXTURE + NUM_COMPONENTS_COLOR;
    private static final int MAX_TRIANGLES = 1000;
    private static final int MAX_INDICES = 200;
    private static final int MAX_NUM_VERTICES = MAX_TRIANGLES * 3;
    private static final float HEIGHT_RANGE = 1f;
    private static final float MAX_SNOW = .1f;

    private Pool<Triangle> trianglePool = new Pool<Triangle>() {
        @Override
        protected Triangle newObject() {
            return new Triangle();
        }
    };



    public float ULHeight = 0;
    public float LLHeight = 0;
    public float URHeight = 0;
    public float LRHeight = 0;


    public float ULSnowHeight = 0;
    public float LLSnowHeight = 0;
    public float URSnowHeight = 0;
    public float LRSnowHeight = 0;

    private Mesh mesh;
    private Mesh boxMesh;
    private float[] vertices;
    private short[] indices;
    private int verticesIndex;
    private int indicesIndex;
    private int boxVerticesIndex;
    private int boxIndicesIndex;
    public float x;
    public float z;
    float width;
    Vector3 p1;
    Vector3 p2;
    Vector3 p3;
    Vector3 p4;
    Vector3 p5;

    public LandTile(int x, int z, float width) {
        this.vertices = new float[MAX_NUM_VERTICES * NUM_COMPONENTS_PER_VERTEX];
        this.indices = new short[MAX_INDICES];
        this.width = width;
        this.x = x * width;
        this.z = z * width;
        p1 = new Vector3();
        p2 = new Vector3();
        p3 = new Vector3();
        p4 = new Vector3();
        p5 = new Vector3();

        SimplexNoise noise = Main.game.assets.noise;

        float terrainNoiseHeight = .4f;
        ULHeight = Math.abs((float)noise.getNoise(x, z)) * terrainNoiseHeight;
        URHeight = Math.abs((float)noise.getNoise(x+1, z)) * terrainNoiseHeight;
        LLHeight = Math.abs((float)noise.getNoise(x, z+1)) * terrainNoiseHeight;
        LRHeight = Math.abs((float)noise.getNoise(x+1, z+1)) * terrainNoiseHeight;

        mesh = new Mesh(false, MAX_NUM_VERTICES, MAX_INDICES,
                new VertexAttribute(VertexAttributes.Usage.Position,           NUM_COMPONENTS_POSITION, "a_position"),
//                new VertexAttribute(VertexAttributes.Usage.Normal,        NUM_COMPONENTS_NORMAL, "a_normal"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked,        NUM_COMPONENTS_COLOR, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, NUM_COMPONENTS_TEXTURE,  "a_texCoord0")
        );
        boxMesh = new Mesh(false, MAX_NUM_VERTICES, MAX_INDICES,
                new VertexAttribute(VertexAttributes.Usage.Position,           NUM_COMPONENTS_POSITION, "a_position"),
//                new VertexAttribute(VertexAttributes.Usage.Normal,        NUM_COMPONENTS_NORMAL, "a_normal"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked,        NUM_COMPONENTS_COLOR, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, NUM_COMPONENTS_TEXTURE,  "a_texCoord0")
        );

        update(0);
        rebuildMesh();

    }

    public void update(float dt){
        p1.set(x, ULHeight + ULSnowHeight, z);
        p2.set(x+1, URHeight + URSnowHeight, z);
        p3.set(x+1, LRHeight + LRSnowHeight, z+1);
        p4.set(x, LLHeight + LLSnowHeight, z+1);
        p5.set(x + .5f, (p1.y + p2.y + p3.y + p4.y)/4f, z + .5f);

    }

    public void render(ShaderProgram shader) {
        shader.setUniformf("x", (int)x);
        shader.setUniformf("z", (int)z);
        mesh.render(shader, GL20.GL_TRIANGLES, 0, indicesIndex);
    }

    public void renderHighlight(Camera camera){
        ShaderProgram shader = Main.game.assets.highlightShader;
        shader.bind();
        shader.setUniformMatrix("u_projTrans", camera.combined);

        boxMesh.render(shader, GL20.GL_TRIANGLES, 0, boxIndicesIndex);
    }

    public float addSnow(Snowball ball){
        float speed = ball.velocity.len();
        float dx = ball.position.x % 1f;
        float dz = ball.position.z % 1f;

        float ULSnowHeightDelta = Math.min(MAX_SNOW - ULSnowHeight, .001f * speed * (Math.max(0, 1f - dx) + Math.max(0, 1f - dz)));
        float URSnowHeightDelta = Math.min(MAX_SNOW - URSnowHeight, .001f * speed * (Math.max(0, dx) + Math.max(0, 1f - dz)));
        float LLSnowHeightDelta = Math.min(MAX_SNOW - LLSnowHeight, .001f * speed * (Math.max(0, 1f - dx) + Math.max(0, 1f)));
        float LRSnowHeightDelta = Math.min(MAX_SNOW - LRSnowHeight, .001f * speed * (Math.max(0, dx) + Math.max(0, 1f)));


        float maxLost = Math.max(ULSnowHeightDelta, URSnowHeightDelta);
        maxLost = Math.max(maxLost, Math.max(LLSnowHeightDelta, LRSnowHeightDelta));
        ULSnowHeight += ULSnowHeightDelta;
        URSnowHeight += URSnowHeightDelta;
        LLSnowHeight += LLSnowHeightDelta;
        LRSnowHeight += LRSnowHeightDelta;
        ball.radius -= maxLost * .2f;
        update(0);
        rebuildMesh();
        return maxLost;
    }

    public void flattenTo(float newHeight) {
        ULHeight = URHeight = LLHeight = LRHeight = newHeight;
        update(0);
        rebuildMesh();
    }

    public float getAverageHeight() {
        return (ULHeight + URHeight + LLHeight + LRHeight) / 4f;
    }

    public float getHeightAt(float x, float z){
        float leftHeight = p1.y * z + p4.y *(1f - z);
        float rightHeight = p2.y * z + p3.y *(1f - z);
        return leftHeight * x + rightHeight * (1f - x);
    }


    private void buildHighlightMesh() {

        float[] boxverts = new float[9*4];

        boxVerticesIndex = 0;

        boxverts[boxVerticesIndex++] = p1.x;
        boxverts[boxVerticesIndex++] = p1.y+.05f;
        boxverts[boxVerticesIndex++] = p1.z;
        boxverts[boxVerticesIndex++] = .2f; // R
        boxverts[boxVerticesIndex++] = .8f; // G
        boxverts[boxVerticesIndex++] = 1f; // B
        boxverts[boxVerticesIndex++] = 1; // A
        boxverts[boxVerticesIndex++] = 0f; // U
        boxverts[boxVerticesIndex++] = 0f; // V

        boxverts[boxVerticesIndex++] = p2.x;
        boxverts[boxVerticesIndex++] = p2.y + .05f;
        boxverts[boxVerticesIndex++] = p2.z;
        boxverts[boxVerticesIndex++] = .2f; // R
        boxverts[boxVerticesIndex++] = .8f; // G
        boxverts[boxVerticesIndex++] = 1f; // B
        boxverts[boxVerticesIndex++] = 1; // A
        boxverts[boxVerticesIndex++] = 1; // U
        boxverts[boxVerticesIndex++] = 0; // V

        boxverts[boxVerticesIndex++] = p3.x;
        boxverts[boxVerticesIndex++] = p3.y + .05f;
        boxverts[boxVerticesIndex++] = p3.z;
        boxverts[boxVerticesIndex++] = .2f; // R
        boxverts[boxVerticesIndex++] = .8f; // G
        boxverts[boxVerticesIndex++] = 1f; // B
        boxverts[boxVerticesIndex++] = 1; // A
        boxverts[boxVerticesIndex++] = 1f; // U
        boxverts[boxVerticesIndex++] = 1f; // V

        boxverts[boxVerticesIndex++] = p4.x;
        boxverts[boxVerticesIndex++] = p4.y + .05f;
        boxverts[boxVerticesIndex++] = p4.z;
        boxverts[boxVerticesIndex++] = .2f; // R
        boxverts[boxVerticesIndex++] = .8f; // G
        boxverts[boxVerticesIndex++] = 1f; // B
        boxverts[boxVerticesIndex++] = 1; // A
        boxverts[boxVerticesIndex++] = 0; // U
        boxverts[boxVerticesIndex++] = 1f; // V

        short[] boxIndices = new short[6];
        boxIndicesIndex = 0;
        boxIndices[boxIndicesIndex++] = 0;
        boxIndices[boxIndicesIndex++] = 1;
        boxIndices[boxIndicesIndex++] = 2;

        boxIndices[boxIndicesIndex++] = 2;
        boxIndices[boxIndicesIndex++] = 3;
        boxIndices[boxIndicesIndex++] = 0;

        boxMesh.setVertices(boxverts);
        boxMesh.setIndices(boxIndices);

    }


    private void rebuildMesh(){

        this.verticesIndex = 0;
        this.indicesIndex = 0;


        // Upper Left vert
        vertices[verticesIndex++] = p1.x;
        vertices[verticesIndex++] = p1.y;
        vertices[verticesIndex++] = p1.z;
        vertices[verticesIndex++] = ULSnowHeight / MAX_SNOW; // r
        vertices[verticesIndex++] = ULHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = ULHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = p1.x; // U
        vertices[verticesIndex++] = p1.z; // V

        // Upper Right vert
        vertices[verticesIndex++] = p2.x;
        vertices[verticesIndex++] = p2.y;
        vertices[verticesIndex++] = p2.z;
        vertices[verticesIndex++] = URSnowHeight / MAX_SNOW; // r
        vertices[verticesIndex++] = URHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = URHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = p2.x; // U
        vertices[verticesIndex++] = p2.z; // V

        // Lower RIGHT vert
        vertices[verticesIndex++] = p3.x;
        vertices[verticesIndex++] = p3.y;
        vertices[verticesIndex++] = p3.z;
        vertices[verticesIndex++] = LRSnowHeight / MAX_SNOW; // r
        vertices[verticesIndex++] = LRHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = LRHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = p3.x; // U
        vertices[verticesIndex++] = p3.z; // V

        // Lower Left vert
        vertices[verticesIndex++] = p4.x;
        vertices[verticesIndex++] = p4.y;
        vertices[verticesIndex++] = p4.z;
        vertices[verticesIndex++] = LLSnowHeight / MAX_SNOW; // r
        vertices[verticesIndex++] = LLHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = LLHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = p4.x; // U
        vertices[verticesIndex++] = p4.z; // V

        // Center Vert
        vertices[verticesIndex++] = p5.x;
        vertices[verticesIndex++] = p5.y;
        vertices[verticesIndex++] = p5.z;
        vertices[verticesIndex++] = (ULSnowHeight + URSnowHeight + LLSnowHeight + LRSnowHeight) / 4f / MAX_SNOW; // r
        vertices[verticesIndex++] = (ULHeight + URHeight + LLHeight + LRHeight) / 4f / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = (ULHeight + URHeight + LLHeight + LRHeight) / 4f / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = p5.x; // U
        vertices[verticesIndex++] = p5.z; // V

        // Floor Upper Left Vert
        vertices[verticesIndex++] = x;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = z;
        vertices[verticesIndex++] = -2; // r
        vertices[verticesIndex++] = 0; // g
        vertices[verticesIndex++] = 0; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Upper Right Vert
        vertices[verticesIndex++] = x + width;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = z;
        vertices[verticesIndex++] = -2; // r
        vertices[verticesIndex++] = 0; // g
        vertices[verticesIndex++] = 0; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Lower Right Vert
        vertices[verticesIndex++] = x + width;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = z + width;
        vertices[verticesIndex++] = -2; // r
        vertices[verticesIndex++] = 0; // g
        vertices[verticesIndex++] = 0; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Lower Left Vert
        vertices[verticesIndex++] = x;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = z + width;
        vertices[verticesIndex++] = -2; // r
        vertices[verticesIndex++] = 0; // g
        vertices[verticesIndex++] = 0; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V


        indices[indicesIndex++] = 0;
        indices[indicesIndex++] = 1;
        indices[indicesIndex++] = 4;

        indices[indicesIndex++] = 1;
        indices[indicesIndex++] = 2;
        indices[indicesIndex++] = 4;

        indices[indicesIndex++] = 2;
        indices[indicesIndex++] = 3;
        indices[indicesIndex++] = 4;

        indices[indicesIndex++] = 3;
        indices[indicesIndex++] = 0;
        indices[indicesIndex++] = 4;

        indices[indicesIndex++] = 5;
        indices[indicesIndex++] = 3;
        indices[indicesIndex++] = 8;

        indices[indicesIndex++] = 0;
        indices[indicesIndex++] = 3;
        indices[indicesIndex++] = 5;

        indices[indicesIndex++] = 0;
        indices[indicesIndex++] = 1;
        indices[indicesIndex++] = 6;

        indices[indicesIndex++] = 0;
        indices[indicesIndex++] = 6;
        indices[indicesIndex++] = 5;

        indices[indicesIndex++] = 1;
        indices[indicesIndex++] = 2;
        indices[indicesIndex++] = 6;

        indices[indicesIndex++] = 2;
        indices[indicesIndex++] = 6;
        indices[indicesIndex++] = 7;

        indices[indicesIndex++] = 2;
        indices[indicesIndex++] = 3;
        indices[indicesIndex++] = 7;

        indices[indicesIndex++] = 3;
        indices[indicesIndex++] = 7;
        indices[indicesIndex++] = 8;


        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        buildHighlightMesh();
    }

    Array<Triangle> triangles = new Array<>();
    public Array<Triangle> getTriangles() {
        triangles.clear();
        triangles.add(trianglePool.obtain().init(p1, p2, p5));
        triangles.add(trianglePool.obtain().init(p2, p3, p5));
        triangles.add(trianglePool.obtain().init(p3, p4, p5));
        triangles.add(trianglePool.obtain().init(p4, p1, p5));
        return triangles;
    }

    public void freeTriangles() {
        trianglePool.freeAll(triangles);
        triangles.clear();
    }

}
