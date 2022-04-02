package lando.systems.ld50.objects;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;

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


    float ULHeight = 0;
    float LLHeight = 0;
    float URHeight = 0;
    float LRHeight = 0;

    private Mesh mesh;
    private float[] vertices;
    private short[] indices;
    private int verticesIndex;
    private int indicesIndex;
    float x;
    float y;
    float width;

    public LandTile(int x, int y, float width) {
        this.vertices = new float[MAX_NUM_VERTICES * NUM_COMPONENTS_PER_VERTEX];
        this.indices = new short[MAX_INDICES];
        this.width = width;
        this.x = x * width;
        this.y = y * width;

//        ULHeight = MathUtils.random(HEIGHT_RANGE);
//        URHeight = MathUtils.random(HEIGHT_RANGE);
        LLHeight = MathUtils.random(HEIGHT_RANGE);
        LRHeight = MathUtils.random(HEIGHT_RANGE);

        rebuildMesh();
    }

    public void update(float dt){

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
        vertices[verticesIndex++] = x;
        vertices[verticesIndex++] = y;
        vertices[verticesIndex++] = ULHeight;
        vertices[verticesIndex++] = ULHeight / HEIGHT_RANGE; // r
        vertices[verticesIndex++] = ULHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = ULHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Upper Right vert
        vertices[verticesIndex++] = x + width;
        vertices[verticesIndex++] = y;
        vertices[verticesIndex++] = URHeight;
        vertices[verticesIndex++] = URHeight / HEIGHT_RANGE; // r
        vertices[verticesIndex++] = URHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = URHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Lower RIGHT vert
        vertices[verticesIndex++] = x + width;
        vertices[verticesIndex++] = y + width;
        vertices[verticesIndex++] = LLHeight;
        vertices[verticesIndex++] = LLHeight / HEIGHT_RANGE; // r
        vertices[verticesIndex++] = LLHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = LLHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Lower Left vert
        vertices[verticesIndex++] = x;
        vertices[verticesIndex++] = y + width;
        vertices[verticesIndex++] = LRHeight;
        vertices[verticesIndex++] = LRHeight / HEIGHT_RANGE; // r
        vertices[verticesIndex++] = LRHeight / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = LRHeight / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Center Vert
        vertices[verticesIndex++] = x + width * .5f;
        vertices[verticesIndex++] = y + width * .5f;
        vertices[verticesIndex++] = (ULHeight + URHeight + LLHeight + LRHeight) / 4f;
        vertices[verticesIndex++] = (ULHeight + URHeight + LLHeight + LRHeight) / 4f / HEIGHT_RANGE; // r
        vertices[verticesIndex++] = (ULHeight + URHeight + LLHeight + LRHeight) / 4f / HEIGHT_RANGE; // g
        vertices[verticesIndex++] = (ULHeight + URHeight + LLHeight + LRHeight) / 4f / HEIGHT_RANGE; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Upper Left Vert
        vertices[verticesIndex++] = x;
        vertices[verticesIndex++] = y;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = 0; // r
        vertices[verticesIndex++] = 0; // g
        vertices[verticesIndex++] = 0; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Upper Right Vert
        vertices[verticesIndex++] = x + width;
        vertices[verticesIndex++] = y;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = 0; // r
        vertices[verticesIndex++] = 0; // g
        vertices[verticesIndex++] = 0; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Lower Right Vert
        vertices[verticesIndex++] = x + width;
        vertices[verticesIndex++] = y + width;
        vertices[verticesIndex++] = 0;
        vertices[verticesIndex++] = 0; // r
        vertices[verticesIndex++] = 0; // g
        vertices[verticesIndex++] = 0; // b
        vertices[verticesIndex++] = 1; // a
        vertices[verticesIndex++] = 1; // U
        vertices[verticesIndex++] = 1; // V

        // Floor Lower Left Vert
        vertices[verticesIndex++] = x;
        vertices[verticesIndex++] = y + width;
        vertices[verticesIndex++] = 0;
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

}
