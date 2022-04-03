package lando.systems.ld50.objects;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
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


    public float ULHeight = 0;
    public float LLHeight = 0;
    public float URHeight = 0;
    public float LRHeight = 0;

    private Mesh mesh;
    private float[] vertices;
    private short[] indices;
    private int verticesIndex;
    private int indicesIndex;
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
        update(0);
        rebuildMesh();
    }

    public void update(float dt){
        p1.set(x, ULHeight, z);
        p2.set(x+1, URHeight, z);
        p3.set(x+1, LRHeight, z+1);
        p4.set(x, LLHeight, z+1);
        p5.set(x + .5f, (p1.y + p2.y + p3.y + p4.y)/4f, z + .5f);
    }

    public void render(ShaderProgram shader) {
        mesh.render(shader, GL20.GL_TRIANGLES);
    }


    private void rebuildMesh(){
        if (mesh != null){
            mesh.dispose();
        }
        mesh = new Mesh(false, MAX_NUM_VERTICES, MAX_INDICES,
                new VertexAttribute(VertexAttributes.Usage.Position,           NUM_COMPONENTS_POSITION, "a_position"),
//                new VertexAttribute(VertexAttributes.Usage.Normal,        NUM_COMPONENTS_NORMAL, "a_normal"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked,        NUM_COMPONENTS_COLOR, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, NUM_COMPONENTS_TEXTURE,  "a_texCoord0")
        );
        this.verticesIndex = 0;
        this.indicesIndex = 0;


        // Upper Left vert
        vertices[verticesIndex++] = p1.x;
        vertices[verticesIndex++] = p1.y;
        vertices[verticesIndex++] = p1.z;
        vertices[verticesIndex++] = ULHeight / HEIGHT_RANGE; // r
        vertices[verticesIndex++] = ULHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = ULHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Upper Right vert
        vertices[verticesIndex++] = p2.x;
        vertices[verticesIndex++] = p2.y;
        vertices[verticesIndex++] = p2.z;
        vertices[verticesIndex++] = URHeight / HEIGHT_RANGE; // r
        vertices[verticesIndex++] = URHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = URHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Lower RIGHT vert
        vertices[verticesIndex++] = p3.x;
        vertices[verticesIndex++] = p3.y;
        vertices[verticesIndex++] = p3.z;
        vertices[verticesIndex++] = LRHeight / HEIGHT_RANGE; // r
        vertices[verticesIndex++] = LRHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = LRHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Lower Left vert
        vertices[verticesIndex++] = p4.x;
        vertices[verticesIndex++] = p4.y;
        vertices[verticesIndex++] = p4.z;
        vertices[verticesIndex++] = LLHeight / HEIGHT_RANGE; // r
        vertices[verticesIndex++] = LLHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = LLHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Center Vert
        vertices[verticesIndex++] = p5.x;
        vertices[verticesIndex++] = p5.y;
        vertices[verticesIndex++] = p5.z;
        vertices[verticesIndex++] = (ULHeight + URHeight + LLHeight + LRHeight) / 4f / HEIGHT_RANGE; // r
        vertices[verticesIndex++] = (ULHeight + URHeight + LLHeight + LRHeight) / 4f / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = (ULHeight + URHeight + LLHeight + LRHeight) / 4f / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Upper Left Vert
        vertices[verticesIndex++] = x;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = z;
        vertices[verticesIndex++] = 0; // r
        vertices[verticesIndex++] = 0; // g
        vertices[verticesIndex++] = 0; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Upper Right Vert
        vertices[verticesIndex++] = x + width;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = z;
        vertices[verticesIndex++] = 0; // r
        vertices[verticesIndex++] = 0; // g
        vertices[verticesIndex++] = 0; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Lower Right Vert
        vertices[verticesIndex++] = x + width;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = z + width;
        vertices[verticesIndex++] = 0; // r
        vertices[verticesIndex++] = 0; // g
        vertices[verticesIndex++] = 0; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Lower Left Vert
        vertices[verticesIndex++] = x;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = z + width;
        vertices[verticesIndex++] = 0; // r
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

    }

    Array<Triangle> triangles = new Array<>();
    public Array<Triangle> getTriangles() {
        triangles.clear();
        triangles.add(new Triangle(p1, p2, p5));
        triangles.add(new Triangle(p2, p3, p5));
        triangles.add(new Triangle(p3, p4, p5));
        triangles.add(new Triangle(p4, p1, p5));
        return triangles;
    }

}
