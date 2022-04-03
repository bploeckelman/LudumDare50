package lando.systems.ld50.particles;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class PhysicsDecal {
    public static Array<PhysicsDecal> instances = new Array<>();
    public static Array<Decal> freeDecals;

    public Decal decal;
    private Vector3 velocity;
    private Vector3 position;
    private float ttl;
    private phys pathing;

    public static void addDecalParticle(Vector3 pos, Vector3 vel, float life, phys type) {
        if (freeDecals.size > 0) {
            instances.add(new PhysicsDecal(freeDecals.get(0), pos, vel, life, type));
            freeDecals.removeIndex(0);
        }
    }

    public PhysicsDecal(Decal d, Vector3 pos, Vector3 vel, float life, phys type) {
        this.decal = d;
        this.position = pos;
        this.velocity = vel;
        this.ttl = life;
        this.pathing = type;
    }

    public static void updateAllDecalParticles(float dt) {
        for (int i = 0; i < instances.size;){
            instances.get(i).update(dt);
            if (instances.get(i).ttl <= 0) {
                freeDecals.add(instances.get(i).decal);
                instances.removeIndex(i);
            } else {
                i++;
            }
        }
    }

    public void update(float dt) {
        this.position.add(this.velocity.x * dt, this.velocity.y * dt, this.velocity.z * dt);
        this.ttl -= dt;
        this.decal.setPosition(this.position);
        switch ( pathing ) {
            case GravityHighDrag:
                if (this.velocity.y > 0) {
                    this.velocity.y *= 0.5;
                }
                this.velocity.y -= 1 * dt;
                this.velocity.scl(0.75f, 1, 0.75f);
                if (this.position.y < -0.1) {
                    this.ttl = -1;
                }
                break;
            case NoPhysics:
                break;
        }
        this.decal.setColor(1f, 1f, 1f, ttl/1f);

    }

    public void render() {

    }



    public enum phys { GravityHighDrag, NoPhysics }

}
