package lando.systems.ld50.objects;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import lando.systems.ld50.Main;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.particles.PhysicsDecal;

public class Snowball {

    private static final BoundingBox box = new BoundingBox();

    public Vector3 position;
    public Vector3 velocity;
    public float radius;

    private final ModelInstance instance;

    public Snowball(float x, float y, float z, float radius) {
        this.velocity = new Vector3();
        this.position = new Vector3(x, y, z);
        this.radius = radius;

        Model model = Assets.Models.sphere.model;
        // TODO: boulder just for testing
//        int whichSnowball = MathUtils.random(0, 2);
//        switch (whichSnowball) {
//            default:
//            case 0: model = Assets.Models.snowball_a.model; break;
//            case 1: model = Assets.Models.snowball_b.model; break;
//            case 2: model = Assets.Models.boulder_a.model; break;
//        }
        instance = new ModelInstance(model);
        instance.calculateBoundingBox(box);
        float extentX = (box.max.x - box.min.x);
        float extentY = (box.max.y - box.min.y);
        float extentZ = (box.max.z - box.min.z);
        float maxExtent = Math.max(Math.max(extentX, extentY), extentZ);
        instance.transform
                .setToTranslationAndScaling(
                        position.x, position.y, position.z,
                        1f / maxExtent,
                        1f / maxExtent,
                        1f / maxExtent)
                .scale(radius, radius, radius)
        ;
    }

    public void update(float dt) {
        instance.transform.setToTranslation(position)
                .scale(radius, radius, radius);

        int numParticles = 1;
        for (int i = 0; i < numParticles; i++) {
            float scale = radius * MathUtils.random(2f, 4f);
            PhysicsDecal.addDecalParticle(
                    Main.game.assets.particles.smoke,
                    position.x + MathUtils.random(-radius, radius),
                    position.y + MathUtils.random(-radius, radius),
                    position.z + .5f,
                    scale * 2.5f * MathUtils.sin(MathUtils.random(360f)),
                    scale * 0.2f,
                    scale * 2.5f * MathUtils.cos(MathUtils.random(360f)),
                    1.25f,
                    PhysicsDecal.Phys.GravityHighDrag);
        }
    }

    public void render(ModelBatch batch, Environment env) {
        batch.render(instance, env);
    }

}
