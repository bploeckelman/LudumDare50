package lando.systems.ld50.objects;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.physics.PhysicsObject;

public class Debris extends PhysicsObject {

    private static final BoundingBox box = new BoundingBox();

    private final ModelInstance instance;
    public float TTL;
    private float initialRadius;

    public Debris(float x, float y, float z, float radius) {
        this.velocity = new Vector3(MathUtils.random(-5, 5f), 4f, MathUtils.random(5, 15f));
        this.position = new Vector3((int)x + .5f, y, (int)z + .5f);
        this.radialVelocity= new Vector3(0, MathUtils.random(100f, 150f), MathUtils.random(-100f, 100f));
        this.radius = radius;
        this.initialRadius = radius;
        TTL = 10;

        Model model;
        switch (MathUtils.random(2)) {
            case 0:
                model = Assets.Models.building_a_part_a.model;
                break;
            case 1:
                model = Assets.Models.building_a_part_b.model;
                break;
            case 2:
            default:
                model = Assets.Models.building_a_part_c.model;
        }

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
                .rotate(rotation)
        ;
    }

    public void update(float dt) {
        updateRotation(dt);
        TTL -= dt;
        radius = initialRadius * TTL/10f;
        instance.transform.setToTranslation(position)
                .scale(radius, radius, radius)
                .rotate(rotation);

    }

    public void render(ModelBatch batch, Environment env) {
        batch.render(instance, env);
    }

}
