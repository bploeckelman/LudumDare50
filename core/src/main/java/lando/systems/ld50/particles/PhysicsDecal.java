package lando.systems.ld50.particles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class PhysicsDecal implements Pool.Poolable {

    public static Array<PhysicsDecal> instances = new Array<>();

    public static Pool<PhysicsDecal> pool = Pools.get(PhysicsDecal.class, 2000);

    public static TextureRegion decalTempRegion = new TextureRegion();
    public static Pool<Decal> decalPool = new Pool<Decal>(2000) {
        @Override
        protected Decal newObject() {
            return Decal.newDecal(decalTempRegion, true);
        }
    };

    public enum Phys { GravityHighDrag, NoPhysics }

    public Decal decal = null;

    private final Vector3 velocity = new Vector3();
    private final Vector3 position = new Vector3();
    private Phys pathing = Phys.NoPhysics;
    private float ttl = 0f;

    public static void addDecalParticle(TextureRegion texture,
                                        float posX, float posY, float posZ,
                                        float velX, float velY, float velZ,
                                        float r, float g, float b, float a,
                                        float life, Phys type) {
        Decal decal = decalPool.obtain();
        decal.setTextureRegion(texture);
        decal.setColor(r, g, b, a);
        decal.setWidth(texture.getRegionWidth());
        decal.setHeight(texture.getRegionHeight());
        decal.setScale(0.01f);

        PhysicsDecal instance = pool.obtain();
        instance.init(decal, posX, posY, posZ, velX, velY, velZ, life, type);
        instances.add(instance);
    }

    public static void updateAllDecalParticles(float dt) {
        for (int i = instances.size - 1; i >= 0; i--) {
            PhysicsDecal particle = instances.get(i);
            particle.update(dt);

            if (particle.ttl <= 0) {
                decalPool.free(particle.decal);
                pool.free(particle);
                instances.removeIndex(i);
            }
        }
    }

    public void init(Decal decal,
                     float posX, float posY, float posZ,
                     float velX, float velY, float velZ,
                     float life,
                     Phys type) {
        this.decal = decal;
        this.position.set(posX, posY, posZ);
        this.velocity.set(velX, velY, velZ);
        this.ttl = life;
        this.pathing = type;
    }

    @Override
    public void reset() {
        position.setZero();
        velocity.setZero();
        pathing = Phys.NoPhysics;
        ttl = 0;
    }

    public void update(float dt) {
        ttl -= dt;
        position.add(velocity.x * dt, velocity.y * dt, velocity.z * dt);
        decal.setColor(decal.getColor().r, decal.getColor().g, decal.getColor().b, ttl);
        decal.setPosition(position);
        switch (pathing) {
            case GravityHighDrag:
                if (velocity.y > 0) {
                    velocity.y *= 0.5;
                }
                velocity.y -= 1 * dt;
                velocity.scl(0.75f, 1, 0.75f);
                if (position.y < -0.1) {
                    ttl = -1;
                }
                break;
            case NoPhysics:
                break;
        }
    }

    public void render() {}

}
