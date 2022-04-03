package lando.systems.ld50.objects;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import lando.systems.ld50.Main;

public class Snowball {
    public Vector3 position;
    public Vector3 velocity;
    public float radius;

//    private static Mesh mesh;

    // TODO - move Model out to Assets so it's easier to manage (for disposal)
    private static Model model;

    private final ModelInstance instance;


    public Snowball(float x, float y, float z, float radius) {
        this.velocity = new Vector3();
        this.position = new Vector3(x, y, z);
        this.radius = radius;

        if (model == null) {
            ModelBuilder builder = new ModelBuilder();
            int attribs = VertexAttributes.Usage.Position
                        | VertexAttributes.Usage.ColorPacked
                        | VertexAttributes.Usage.Normal;
            model = builder.createSphere(1f, 1f, 1f, 10, 10,
                    new Material(ColorAttribute.createDiffuse(Color.FIREBRICK)), attribs);
        }
        instance = new ModelInstance(model);
        instance.transform.setToTranslation(position)
                .scale(radius, radius, radius);
    }

    public void update(float dt){
        instance.transform.setToTranslation(position)
                .scale(radius, radius, radius);
    }

    public void render(ModelBatch batch, Environment env) {
        batch.render(instance, env);
    }

    // TODO - can probably remove the manual mesh stuff
//    Matrix4 rotationMatrix = new Matrix4();
//    Matrix4 worldTransform = new Matrix4();
//    Color color = new Color(Color.WHITE);
//    public void render(ShaderProgram shader) {
//
//        worldTransform.idt();
//        worldTransform.translate(position.x, position.y, position.z);
//        worldTransform.scl(radius);
//
//        Main.game.assets.pixel.bind(0);
//
//        shader.setUniformMatrix("u_worldTrans", worldTransform);
//        shader.setUniformf("u_color", color);
//        shader.setUniformi("u_texture", 0);
//
//        getMesh().render(shader, GL30.GL_TRIANGLES);
//
//    }
//
//    protected Mesh getMesh() {
//        if (mesh == null){
//            buildMesh();
//        }
//        return mesh;
//    }
//
//    private void buildMesh() {
//        MeshBuilder builder = new MeshBuilder();
//        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
//        float width = 1f;
//        float height = 1f;
//        float depth = 1f;
//        int divU = 20;
//        int divV = 20;
//        SphereShapeBuilder.build(builder, width, height, depth, divU, divV);
//        mesh = builder.end();
//
//    }
}
