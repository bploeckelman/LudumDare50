package lando.systems.ld50.physics;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld50.objects.LandTile;
import lando.systems.ld50.objects.Landscape;
import lando.systems.ld50.objects.Snowball;

public class PhysicsManager {

    // TODO make this the real down
    Vector3 gravity = new Vector3(0, -5f, 1f);
    Landscape landscape;
    Array<BallContact> ballContacts;

    Array<LandTile> neighborTiles;

    public PhysicsManager(Landscape landscape) {
        this.landscape = landscape;
        ballContacts = new Array<>();
        neighborTiles = new Array<>();
    }

    public void update(float dt){
        solve(dt);
    }


    Vector3 newPos = new Vector3();
    private void solve(float dt){
        ballContacts.clear();
        // Snowballs
        for (Snowball ball : landscape.snowBalls){
            ball.velocity.add(gravity.x * dt, gravity.y * dt, gravity.z * dt);
            ball.position.add(ball.velocity.x * dt, ball.velocity.y * dt, ball.velocity.z * dt);
//            newPos.set(ball.position);
//            newPos.add(ball.velocity);
            // Test if ball goes through floor
            landscape.getTilesAround(ball.position.x, ball.position.z, neighborTiles);
            for (LandTile tile : neighborTiles){
                testBallTile(ball, tile);
            }
        }
        int iteration = 0;
        while (ballContacts.size > 0 && iteration++ < 100) {
            ballContacts.sort();
            ballContacts.get(0).resolve(dt);
            ballContacts.removeIndex(0);
        }
    }


    Vector3 p1 = new Vector3();
    Vector3 p2 = new Vector3();
    Vector3 p3 = new Vector3();
    private void testBallTile(Snowball ball, LandTile tile) {
        Array<Triangle> triangles = tile.getTriangles();
        for (Triangle t : triangles){
            Vector3 nearestPoint = closestPtPointTriangle(ball.position, p1.set(t.p1), p2.set(t.p2), p3.set(t.p3));
            nearestPoint.sub(ball.position);
            float overlap = ball.radius - nearestPoint.len();
            if (overlap > 0) {
                ballContacts.add(new BallContact(ball, t.getNormal(), overlap));
            }
        }
    }

    Vector3 ab = new Vector3();
    Vector3 ac = new Vector3();
    Vector3 ap = new Vector3();
    Vector3 bp = new Vector3();
    Vector3 cp = new Vector3();
    Vector3 returnVector = new Vector3();
    private Vector3 closestPtPointTriangle(Vector3 point, Vector3 a, Vector3 b, Vector3 c){
        ab.set(b).sub(a);
        ab.set(c).sub(a);
        ap.set(point).sub(a);
        float d1 = ab.dot(ap);
        float d2 = ac.dot(ap);
        if (d1 <= 0 && d2 <= 0) return a;

        bp.set(point).sub(b);
        float d3 = ab.dot(bp);
        float d4 = ac.dot(bp);
        if (d3 >= 0 && d4 <= d3) return b;

        float vc = d1*d4 - d3*d2;
        if (vc <= 0 && d1 >= 0 && d3 <= 0){
            float v = d1 / (d1 - d3);
            return returnVector.set(a).add(ab.scl(v));
        }

        cp.set(point).sub(c);
        float d5 = ab.dot(cp);
        float d6 = ac.dot(cp);
        if (d6 >= 0 && d5 <= d6) return c;

        float vb = d5*d2 - d1*d6;
        if (vb <= 0 && d2 >= 0 && d6 <= 0) {
            float w = d2 / (d2 - d6);
            return returnVector.set(a).add(ac.scl(w));
        }

        float va = d3*d6 - d5*d4;
        if (va <= 0 && (d4-d3) >= 0 && (d5-d6) >= 0) {
            float w = (d4-d3)/ ((d4-d3) + (d5-d6));
            return returnVector.set(c).sub(b).scl(w).add(b);
        }

        float denom = 1f / (va + vb + vc);
        float v = vb * denom;
        float w = vc * denom;
        return a.add(ab.scl(v)).add(ac.scl(w));
    }
}
