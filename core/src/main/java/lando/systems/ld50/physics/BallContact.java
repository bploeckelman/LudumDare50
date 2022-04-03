package lando.systems.ld50.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import lando.systems.ld50.objects.LandTile;
import lando.systems.ld50.objects.Snowball;
import lando.systems.ld50.particles.PhysicsDecal;

// TODO: POOL THIS!!!
public class BallContact implements Comparable, Pool.Poolable
{
    public Snowball ball;
    public Vector3 contactNormal;
    public float penetrationDepth;
    public LandTile tile;

    public BallContact(){

    }

    public BallContact init(Snowball ball, Vector3 normal, float pen, LandTile tile) {
        this.tile = tile;
        this.ball = ball;
        this.contactNormal = normal;
        this.penetrationDepth = pen;
        return this;
    }

    public void resolve(float dt){
        resolveVelocity(dt);
        resolveInterpenetration(dt);
        tile.addSnow(ball);

    }

    public float calculateSeparatingVelocity() {
        return ball.velocity.dot(contactNormal);
    }

    Vector3 impulseVelocity = new Vector3();
    private void resolveVelocity(float dt) {
        float separatingVelocity = calculateSeparatingVelocity();
        if (separatingVelocity > 0){
            // no impulse required
            return;
        }
        float newSeparatingVelocity = -separatingVelocity * 0.01f;
        float deltaVelocity = newSeparatingVelocity - separatingVelocity;
        impulseVelocity.set(contactNormal).scl(deltaVelocity);
        ball.velocity.add(impulseVelocity);

    }

    Vector3 movementVector = new Vector3();
    private void resolveInterpenetration(float dt) {
        movementVector.set(contactNormal).scl(penetrationDepth);
        ball.position.add(movementVector);
    }

    @Override
    public void reset() {

    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof BallContact){
            return Float.compare(calculateSeparatingVelocity(), ((BallContact)o).calculateSeparatingVelocity());
        }
        return 0;
    }
}
