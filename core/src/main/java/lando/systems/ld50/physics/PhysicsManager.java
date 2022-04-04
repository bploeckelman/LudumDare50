package lando.systems.ld50.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import lando.systems.ld50.audio.AudioManager;
import lando.systems.ld50.objects.LandTile;
import lando.systems.ld50.objects.Landscape;
import lando.systems.ld50.objects.Snowball;
import lando.systems.ld50.Main;

public class PhysicsManager {

    // TODO make this the real down
    Vector3 gravity = new Vector3(0, -7f, 1.3f);
    Landscape landscape;
    Pool<BallContact> contactPool = new Pool<BallContact>() {
        @Override
        protected BallContact newObject() {
            return new BallContact();
        }
    };
    Array<BallContact> ballContacts;

    Array<LandTile> neighborTiles;

    public PhysicsManager(Landscape landscape) {
        this.landscape = landscape;
        ballContacts = new Array<>();
        neighborTiles = new Array<>();
    }

    public void update(float dt){
//        if (true) return;
        solve(MathUtils.clamp(dt,.001f, .015f));
    }


    Vector3 newPos = new Vector3();
    private void solve(float dt){
        // Snowballs
        for (Snowball ball : landscape.snowBalls){
            ball.velocity.add(gravity.x * dt, gravity.y * dt, gravity.z * dt);
            ball.velocity.clamp(0, 5);
            ball.position.add(ball.velocity.x * dt, ball.velocity.y * dt, ball.velocity.z * dt);
//            newPos.set(ball.position);
//            newPos.add(ball.velocity);

            // Test if ball falls off side and correct
            float totWidth = Landscape.TILE_WIDTH * Landscape.TILES_WIDE;
            float totalLength = Landscape.TILE_WIDTH * Landscape.TILES_LONG;
            if (ball.position.x - ball.radius < 0 || ball.position.x + ball.radius > totWidth) {
                ball.velocity.scl(-1f, 1, 1);
                ball.position.x = Math.max(ball.radius, Math.min(totWidth - ball.radius, ball.position.x));
                //ball.position.add(ball.velocity.x * 0.1f, 0, 0);
            }



            // Test if ball goes through floor

            landscape.getTilesAround(ball.position.x, ball.position.z, neighborTiles);
            for (LandTile tile : neighborTiles){
                testBallTile(ball, tile);
                if (testBuilding(ball, tile)) {
                    ball.radius = 0;
                    // TODO: Remove Decoration
                    tile.decoration.transform.scl(0.7f);
                    //temp solutions
                    if (tile.decoration.transform.getScaleX() < 0.1) { tile.decoration.transform.scl(0f); tile.decoration = null; }
                    Main.game.audio.playSound(AudioManager.Sounds.houseImpact, 0.3F);
                    break;
                }

            }

//            // Keep from falling through floor
//            if (ball.position.x > 0 && ball.position.z >0 && ball.position.x < totWidth && ball.position.z < totalLength && ball.position.y - ball.radius < 0){
//                ball.position.y = ball.radius;
//            }

            float height = ball.position.y - ball.radius - landscape.getHeightAt(ball.position.x, ball.position.z);
            if (ball.position.x > 0 && ball.position.z >0 && ball.position.x < totWidth && ball.position.z < totalLength && height < 0){
                ball.position.y += -height;
            }



        }
        int iteration = 0;
        while (ballContacts.size > 0 && iteration++ < 50) {
            ballContacts.sort();
            ballContacts.get(0).resolve(dt);
            contactPool.free(ballContacts.removeIndex(0));
        }
        contactPool.freeAll(ballContacts);
        ballContacts.clear();

        //rubberbanding to keep balls together
        float meanZ = 0;
        float tol = 0.75f;
        float pow = 1f;
        for (Snowball b : landscape.snowBalls) {
            meanZ += b.position.z;
        }
        meanZ /= landscape.snowBalls.size;
        for (Snowball b : landscape.snowBalls) {
            if (b.position.z > meanZ + tol) {
                b.velocity.z -= pow * (b.position.z - meanZ - tol) * dt;
            }
            if (b.position.z < meanZ - tol * 1.5) {
                b.velocity.z += pow * 0.8 * ((meanZ - tol * 1.5) - b.position.z) * dt;
            }
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
                ballContacts.add(contactPool.obtain().init(ball, t.getNormal(), overlap + .01f, tile));
            }
        }
//        tile.freeTriangles();
    }

    private boolean testBuilding(Snowball ball, LandTile tile) {
        if (ball.position.x - ball.radius / 2 > tile.x + 0.85 ||
        ball.position.x + ball.radius / 2 < tile.x + 0.15 ||
        ball.position.z - ball.radius / 2 > tile.z + 0.85 ||
        ball.position.z + ball.radius / 2 < tile.z + 0.15) {
            return false;
        }
        //LandTile t = landscape.tiles[(int)ball.position.x + Landscape.TILES_WIDE * (int)ball.position.z];
        return tile.isDecorated() && ball.position.y < 0.5 + ball.radius/2;

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
