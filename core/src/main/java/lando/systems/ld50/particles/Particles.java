package lando.systems.ld50.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import lando.systems.ld50.assets.Assets;

public class Particles implements Disposable {

    public enum Layer { background, middle, foreground }

    private static final int MAX_PARTICLES = 4000;

    private final Assets assets;
    private final ObjectMap<Layer, Array<Particle>> activeParticles;
    private final Pool<Particle> particlePool = Pools.get(Particle.class, MAX_PARTICLES);

    public Particles(Assets assets) {
        this.assets = assets;
        this.activeParticles = new ObjectMap<>();
        int particlesPerLayer = MAX_PARTICLES / Layer.values().length;
        this.activeParticles.put(Layer.background, new Array<>(false, particlesPerLayer));
        this.activeParticles.put(Layer.middle,     new Array<>(false, particlesPerLayer));
        this.activeParticles.put(Layer.foreground, new Array<>(false, particlesPerLayer));
    }

    public void clear() {
        for (Layer layer : Layer.values()) {
            particlePool.freeAll(activeParticles.get(layer));
            activeParticles.get(layer).clear();
        }
    }

    public void update(float dt) {
        for (Layer layer : Layer.values()) {
            for (int i = activeParticles.get(layer).size - 1; i >= 0; --i) {
                Particle particle = activeParticles.get(layer).get(i);
                particle.update(dt);
                if (particle.isDead()) {
                    activeParticles.get(layer).removeIndex(i);
                    particlePool.free(particle);
                }
            }
        }
    }

    public void draw(SpriteBatch batch, Layer layer) {
        activeParticles.get(layer).forEach(particle -> particle.render(batch));
    }

    @Override
    public void dispose() {
        clear();
    }

    // ------------------------------------------------------------------------
    // Helper fields for particle spawner methods
    // ------------------------------------------------------------------------
    private final Color tempColor = new Color();
    private final Vector2 tempVec2 = new Vector2();
    // ------------------------------------------------------------------------
    // Spawners for different particle effects
    // ------------------------------------------------------------------------

    public void sparkle(float x, float y) {
        TextureRegion keyframe = assets.particles.sparkle;
        tempColor.set(Color.WHITE);
        int numParticles = 3;
        for (int i = 0; i < numParticles; ++i) {
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y)
                    .velocityDirection(MathUtils.random(-5, 5) + 90, MathUtils.random(-200, -150))
                    .startSize(MathUtils.random(10, 16))
                    .endSize(MathUtils.random(2, 8))
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .timeToLive(2f)
                    .startColor(tempColor)
                    .init());
        }
    }

    public void addSmoke(float x, float y){
        TextureRegion keyframe = assets.particles.smoke;
        float grayValue = MathUtils.random(.7f) + .3f;
        tempColor.set(grayValue, grayValue, grayValue, 1f);
        int numParticles = 10;
        for (int i = 0; i < numParticles; ++i) {
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y)
                    .targetPos(x + MathUtils.random(-20f, 50f), y + MathUtils.random(-20f, 50f))
                    .velocityDirection(MathUtils.random(-20, 20), MathUtils.random(-20, 20f))
                    .startSize(MathUtils.random(10, 16))
                    .endSize(MathUtils.random(2, 8))
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .timeToLive(MathUtils.random(.5f, 1.5f))
                    .startColor(tempColor)
                    .init());
        }
    }

    public void addLargeSmoke(float x, float y){
        TextureRegion keyframe = assets.particles.smoke;
        float grayValue = MathUtils.random(.7f) + .3f;
        tempColor.set(grayValue, grayValue, grayValue, 1f);
        int numParticles = 150;
        for (int i = 0; i < numParticles; ++i) {
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x + MathUtils.random(-70f, 70f), y + MathUtils.random(-70f, 70f))
//                    .targetPos(x + MathUtils.random(0f, 250f), y + MathUtils.random(0f, 250f))
                    .velocityDirection(MathUtils.random(-200f, 200f), MathUtils.random(-200f, 200f))
                    .startSize(MathUtils.random(100f, 200f))
                    .endSize(MathUtils.random(50f, 80f))
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .timeToLive(MathUtils.random(1.5f, 3.5f))
                    .startColor(tempColor)
                    .init());
        }
    }

    public void addCash(float x, float y, int amount) {

        Color color = amount > 0 ? Color.GREEN : Color.RED;
        TextureRegion keyframe = assets.particles.dollar;
        int numParticles = Math.abs(amount) / 10;
        for (int i = 0; i < numParticles; ++i) {
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y)
                    .velocityDirection(MathUtils.random(20f, 160f), MathUtils.random(100f) + 40)
                    .startSize(20)
                    .endSize(30)
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .timeToLive(MathUtils.random(1f, 1.5f))
                    .startColor(color)
                    .init());
        }
    }

    public void addSmokeStackSmoke(float x, float y){
        TextureRegion keyframe = assets.particles.smoke;

        int numParticles = 1;
        for (int i = 0; i < numParticles; ++i) {
            float grayValue = MathUtils.random(.95f, 1f);
            tempColor.set(grayValue, grayValue, grayValue, 1f);
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x+MathUtils.random(-20f, 20f), y + MathUtils.random(-20f, 20f)) // + MathUtils.random(-70f, 70f), y)
                    .velocityDirection(MathUtils.random(-90f, -30f), MathUtils.random(-50f, -200f))
                    .startSize(MathUtils.random(100f, 120f))
                    .endSize(MathUtils.random(10f, 80f))
                    .startColor(tempColor)
                    .startAlpha(MathUtils.random(.3f, .6f))
                    .endAlpha(0f)
                    .timeToLive(MathUtils.random(1.5f, 2.5f))
                    .init());
        }
    }

    private final Color[] projectilePistonColors = new Color[] {
            Color.ORANGE, Color.CORAL, Color.FIREBRICK, Color.YELLOW, Color.PURPLE
    };

    public void projectileBreak(float x, float y, boolean failure){
        TextureRegion keyframe = assets.particles.ring;
        int numParticles = 50;
        for (int i = 0; i < numParticles; ++i) {
            if (failure) {
                tempColor.set(Color.GRAY);
                numParticles = 30;
            } else {
                tempColor.set(projectilePistonColors[MathUtils.random(0, projectilePistonColors.length - 1)]);
            }
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x + MathUtils.random(-5f, 5f), y + MathUtils.random(-5f, 5f))
                    .velocityDirection(MathUtils.random(-300f, 300f), MathUtils.random(-300f, 300f))
                    .startSize(MathUtils.random(4f, 8f))
                    .endSize(MathUtils.random(10f, 20f))
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .timeToLive(MathUtils.random(0.25f, 0.5f))
                    .startColor(tempColor)
                    .init());
        }
    }

    public void reactorSteam(Rectangle rect) {
        TextureRegion keyframe = assets.particles.smoke;
        int numParticles = 5;

        for (int i = 0; i < numParticles; ++i) {
            float x = rect.x + MathUtils.random(rect.width);
            float y = rect.y + MathUtils.random(rect.height);
            tempColor.set(1, 1, 1, 1f);
            activeParticles.get(Layer.background).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y) // + MathUtils.random(-70f, 70f), y)
                    .velocityDirection(MathUtils.random(0, 180), MathUtils.random(50f, 100f))
                    .startSize(MathUtils.random(30f, 80f))
                    .endSize(MathUtils.random(1f, 10f))
                    .startColor(tempColor)
                    .startAlpha(MathUtils.random(.3f, .6f))
                    .endAlpha(0f)
                    .timeToLive(MathUtils.random(.5f, 2.5f))
                    .init());
        }    }

    public void projectileTrail(float x, float y) {
        TextureRegion keyframe = assets.particles.sparkle;
        tempColor.set(Color.WHITE);
        int numParticles = 1;
        for (int i = 0; i < numParticles; ++i) {
            activeParticles.get(Layer.middle).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y)
                    .startSize(MathUtils.random(32, 40))
                    .endSize(MathUtils.random(3, 8))
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .timeToLive(MathUtils.random(0.5f, 1f))
                    .startColor(tempColor)
                    .init());
        }
    }

    public void addPointsParticles(long points, float x, float y, float r, float g, float b) {
        // create a particle for each number in 'points'
        String pointsStr = Long.toString(points, 10);
        int size = MathUtils.clamp((int) Math.sqrt(points), 12, 30);
        for (int i = 0; i < pointsStr.length(); ++i) {

            TextureRegion texture = assets.numberParticles[Character.digit(pointsStr.charAt(i), 10)].getKeyFrames()[0];
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(texture)
                    .endAlpha(0f)
                    .startColor(r, g, b, 1.5f)
                    .startSize(size * 0.55f, size)
                    .velocity(0, 40)
                    .startPos(x - ((pointsStr.length()-1) * size) * 0.3f + (i * size) * 0.6f, y)
                    .timeToLive(2.5f)
                    .init());
        }
    }

    public void addParticleBurstCollect(int quantity, float[] color, float[] position, float[] target) {
        for (int i = 0; i < quantity; i++) {
            activeParticles.get(Layer.middle).add(Particle.initializer(particlePool.obtain())
                    .keyframe(assets.particles.sparkle)
                    .startColor(color[0], color[1], color[2], 1)
                    .targetPos(target[0], target[1])
                    .startPos(position[0] + MathUtils.random(35, 70) * MathUtils.sinDeg(i*360f/quantity), position[1] + MathUtils.random(35, 70) * MathUtils.cosDeg(i*360f/quantity))
                    .timeToLive(MathUtils.random(0.8f, 1.4f))
                    .startSize(25, 25)
                    .endSize(10, 10)
                    .init());
        }

    }

}
