package lando.systems.ld50.physics;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class PhysicsObject {
    public Vector3 velocity;
    public Vector3 position;
    public float radius;
    public Vector3 radialVelocity;
    public Quaternion rotation = new Quaternion();


    Quaternion angles = new Quaternion();
    public void updateRotation(float dt) {
        angles.setEulerAngles(radialVelocity.x * dt, radialVelocity.y * dt, radialVelocity.z * dt);
        rotation.set(angles.mul(rotation));
    }
}
