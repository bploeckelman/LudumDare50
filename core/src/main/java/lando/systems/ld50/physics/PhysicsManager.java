package lando.systems.ld50.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import lando.systems.ld50.Config;
import lando.systems.ld50.Main;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.audio.AudioManager;
import lando.systems.ld50.objects.*;
import lando.systems.ld50.particles.PhysicsDecal;

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
    Vector3 position = new Vector3();
    BoundingBox box = new BoundingBox();

    public PhysicsManager(Landscape landscape) {
        this.landscape = landscape;
        ballContacts = new Array<>();
        neighborTiles = new Array<>();
    }

    float scale = 1f;
    float internalTime = 0;
    float timeStep = .005f;
    float slowdownScale = 0.4f;
    public void update(float dt){
        if (dt == 0) return;
        internalTime += slowdownScale * dt;
        while (internalTime > timeStep) {
            internalTime -= timeStep;
            solve(MathUtils.clamp(timeStep * scale, .0001f, .05f));
        }
    }


    Vector3 newPos = new Vector3();
    private void solve(float dt){
        // Snowballs
        for (Snowball ball : landscape.snowBalls){
            ball.velocity.add(gravity.x * dt, gravity.y * dt, gravity.z * dt);
            ball.velocity.clamp(0, 5);
            ball.position.add(ball.velocity.x * dt, ball.velocity.y * dt, ball.velocity.z * dt);

            // Test if ball falls off side and correct
            float totWidth = Landscape.TILE_WIDTH * Landscape.TILES_WIDE;
            float totalLength = Landscape.TILE_WIDTH * Landscape.TILES_LONG;
            if (ball.position.x - ball.radius < 0 || ball.position.x + ball.radius > totWidth) {
                ball.velocity.scl(-1f, 1, 1);
                ball.position.x = Math.max(ball.radius, Math.min(totWidth - ball.radius, ball.position.x));
                //ball.position.add(ball.velocity.x * 0.1f, 0, 0);
            }

            for (AnimationDecal decal : landscape.screen.decals) {
                if (decal.get().getPosition().dst(ball.position) < ball.radius / 1.25) {
                    decal.hit();
                }
            }

            // Test if ball goes through floor
            landscape.getTilesAround(ball.position.x, ball.position.z, neighborTiles);
            for (LandTile tile : neighborTiles){
                testBallTile(ball, tile);

                if (testBuilding(ball, tile)) {


                    // decoration got hit, fuck it up somehow
                    // TODO - maybe tweak how much damage it takes each collision
                    tile.decorationHealth -= ball.radius;

                    ball.radius = 0;

                    // make more red based on health
                    ColorAttribute attrib = (ColorAttribute) tile.decoration.materials.get(0).get(ColorAttribute.Diffuse);
                    attrib.color.set(1f, tile.decorationHealth, tile.decorationHealth, 1f);


                    // decoration got killed, do stuff
                    if (tile.decorationHealth <= 0f) {
                        // spawn a particle effect
                        float radius = 0.1f;
                        position.set(tile.x + 0.5f, 0f, tile.z + 0.5f);
                        int numParticles = 40;
                        for (int i = 0; i < numParticles; i++) {
                            PhysicsDecal.addDecalParticle(
                                    Main.game.assets.particles.smoke,
                                    position.x + MathUtils.random(-radius, radius),
                                    position.y + MathUtils.random(0, 0.5f),
                                    position.z + MathUtils.random(-radius, radius),
                                    MathUtils.random(-0.5f, 0.5f),
                                    1f,
                                    MathUtils.random(-0.5f, 0.5f),
                                    0f, 0f, 0f, 0.75f,
                                    1.25f + MathUtils.random(.5f),
                                    PhysicsDecal.Phys.NoPhysics);
                        }

                        // TODO this isn't what we want for trees and shit
                        for (int i = 0; i < 4; i++){
                            landscape.debris.add(new Debris(tile.x, .5f, tile.z, .1f));
                        }

                        landscape.screen.removeModelInstance(tile.decoration);

                        // This is a lodge Tile
                        if (tile.z == Landscape.TILES_LONG -1){
                            landscape.setGameOver();
                        }

                        if (tile.decoration.model == Assets.Models.building_a.model) {
                            ModelInstance instance = new ModelInstance(Assets.Models.building_a_snowed.model);
                            instance.calculateBoundingBox(box);
                            float extentX = (box.max.x - box.min.x);
                            float extentY = (box.max.y - box.min.y);
                            float extentZ = (box.max.z - box.min.z);
                            float maxExtent = Math.max(Math.max(extentX, extentY), extentZ);
                            instance.transform
                                    .setToTranslationAndScaling(
                                            0f, 0f, 0f,
                                            1f / maxExtent,
                                            1f / maxExtent,
                                            1f / maxExtent);
                            tile.decorate(instance);
                            tile.isDecorationDestructable = false;
                            landscape.screen.addHouseModelInstance(instance);
                        } else {
                            tile.decoration = null;
                        }
                    } else { // building is safe, spawn good karma particles

                    }

                    Main.game.audio.playSound(AudioManager.Sounds.houseImpact, 0.6F);
                    break;
                }
            }

            float height = ball.position.y - ball.radius - landscape.getHeightAt(ball.position.x, ball.position.z);
            if (ball.position.x > 0 && ball.position.z >0 && ball.position.x < totWidth && ball.position.z < totalLength && height < 0){
                ball.position.y += -height;
            }
        }

        // Debris
        for (Debris debris : landscape.debris) {
            debris.velocity.add(gravity.x * dt, gravity.y * dt, gravity.z * dt);
            debris.velocity.clamp(0, 5.5f);
            debris.position.add(debris.velocity.x * dt, debris.velocity.y * dt, debris.velocity.z * dt);

            // Test if debris falls off side and correct
            float totWidth = Landscape.TILE_WIDTH * Landscape.TILES_WIDE;
            float totalLength = Landscape.TILE_WIDTH * Landscape.TILES_LONG;
            if (debris.position.x - debris.radius < 0 || debris.position.x + debris.radius > totWidth) {
                debris.velocity.scl(-1f, 1, 1);
                debris.position.x = Math.max(debris.radius, Math.min(totWidth - debris.radius, debris.position.x));
            }
            // Test if debris goes through floor
            landscape.getTilesAround(debris.position.x, debris.position.z, neighborTiles);
            for (LandTile tile : neighborTiles) {
                testBallTile(debris, tile);
            }
            float height = debris.position.y - debris.radius - landscape.getHeightAt(debris.position.x, debris.position.z);
            if (debris.position.x > 0 && debris.position.z >0 && debris.position.x < totWidth && debris.position.z < totalLength && height < 0){
                debris.position.y += -height;
            }
        }


        int iteration = 0;
        while (ballContacts.size > 0 && iteration++ < 100) {
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
    private void testBallTile(PhysicsObject ball, LandTile tile) {
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
        if (!tile.isDecorated()) {
            return false;
        }
        if (!tile.isDecorationDestructable){
            return false;
        }
        if (ball.interactedDecorations.contains(tile, true)) {
            return false;
        }
//<<<<<<< HEAD
//        boolean result = tile.isDecorated() && ball.position.y < 0.5 + ball.radius/2;
//
//=======

//        boolean ballNotNearTile =
//                   ball.position.x - ball.radius / 2 > tile.x + 0.85
//                || ball.position.x + ball.radius / 2 < tile.x + 0.15
//                || ball.position.z - ball.radius / 2 > tile.z + 0.85
//                || ball.position.z + ball.radius / 2 < tile.z + 0.15;
//        if (ballNotNearTile) {
//            return false;
//        }

        boolean isBallInBoundsX = ball.position.x > tile.x - ball.radius/2 && ball.position.x < tile.x + 1f + ball.radius/2;
        boolean isBallInBoundsZ = ball.position.z > tile.z - ball.radius/2 && ball.position.z < tile.z + 1f + ball.radius/2;
        boolean isBallLowEnoughToHit = ball.position.y < 0.8 + ball.radius / 2;
        boolean hitBuilding = tile.isDecorationDestructable && isBallLowEnoughToHit && isBallInBoundsX && isBallInBoundsZ;

        if (isBallInBoundsX && isBallInBoundsZ){


            if (isBallLowEnoughToHit){
                // spawn bad karma particles
                landscape.screen.addBadKarmaPoints(93, tile);
            } else {
                // Spawn good particles
                landscape.screen.addGoodKarmaPoints(137, tile);
            }
            ball.interactedDecorations.add(tile);
        }

        return hitBuilding;
//>>>>>>> e88e15f... Destroy buildings, add particle effect and replace with broken building model
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
