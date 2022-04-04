package lando.systems.ld50.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld50.Main;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.assets.ImageInfo;
import lando.systems.ld50.screens.BaseScreen;
import lando.systems.ld50.screens.GameScreen;

public class AnimationDecal {

    private float time = 0;

    public boolean dead = false;

    private Landscape landscape;
    private ImageInfo imageInfo;

    private Vector3 initPos = new Vector3();
    private Vector3 position = new Vector3();

    private Animation<TextureRegion> regionAnimation;
    private Animation<TextureRegion> waveAnimation;

    private float moveTime, moveTimeTotal;
    private Vector3 movePosition = new Vector3();

    private float waveTime = 0;

    private float directionX;
    private boolean right = true;

    private Array<Decal> decals = new Array<>();
    private Array<Decal> waveDecals = new Array<>();

    public AnimationDecal(ImageInfo imageInfo, int x, int z) {
        this(Main.game.getScreen().assets, imageInfo, ((GameScreen) Main.game.getScreen()).landscape, x, z);
    }

    public AnimationDecal(Assets assets, ImageInfo imageInfo, Landscape landscape, int x, int z) {
        regionAnimation = new Animation<>(0.1f, assets.atlas.findRegions(imageInfo.region), Animation.PlayMode.LOOP);
        waveAnimation = (imageInfo.waveRegion != null)
                ? new Animation<>(0.1f, assets.atlas.findRegions(imageInfo.waveRegion), Animation.PlayMode.LOOP_PINGPONG)
                : regionAnimation;

        this.landscape = landscape;
        this.imageInfo = imageInfo;

        TextureRegion r = regionAnimation.getKeyFrame(0);

        // 0 is center of image
        if (!setTilePosition(initPos, x, z)) {
            setTilePosition(initPos, 0, 0);
        }
        position.set(initPos);

        directionX = imageInfo.right ? 1 : -1;

        addDecals(regionAnimation, decals);
        addDecals(waveAnimation, waveDecals);
    }

    private void addDecals(Animation<TextureRegion> animation, Array<Decal> decals) {
        for (TextureRegion region : animation.getKeyFrames()) {
            Decal decal = Decal.newDecal(region, true);
            decal.setDimensions(imageInfo.width, imageInfo.height);
            // decal.setPosition(position);
            decals.add(decal);
        }
    }

    private boolean setTilePosition(Vector3 pos, int x, int z) {
        float ix = x + MathUtils.random();
        float iz = z + MathUtils.random();
        float iy = landscape.getHeightAt(ix, iz) +  imageInfo.height / 2;
        pos.set(ix, iy, iz);
        return iy >= 0;
    }

    public void moveToTile(int x, int z) {
        if (setTilePosition(movePosition, x, z)) {
            right = (movePosition.x > position.x);
            moveTimeTotal = Math.abs(movePosition.dst(initPos)) / 4;
            moveTime = 0;
        } else {
            movePosition.setZero();
        }
    }

    public void update(float dt) {
        if (!launched) {
            time += dt;
        }

        updateMovement(dt);
    }

    public void hit() {
        this.launch();
        
    }

    private void updateMovement(float dt) {

        if (isInSnow() && !launched) {
            waveTime += dt;
            return;
        }

        if (moveTimeTotal > 0) {
            moveTime += 0.4f * dt;
            float lerp = MathUtils.clamp(moveTime / moveTimeTotal, 0, 1);
            float x = MathUtils.lerp(initPos.x, movePosition.x, lerp);
            float z = MathUtils.lerp(initPos.z, movePosition.z, lerp);
            float y = landscape.getHeightAt(x, z) +  imageInfo.height / 2;

            if (launched) {
                y = position.y + (dt * 2);
                x = position.x - dt;
                z = position.z + (dt * 2);
                launchTime += dt;
            }

            position.set(x, y, z);

            if (lerp == 1) {
                moveTimeTotal = 0;
                initPos.set(position);

                if (launched) {
                    dead = true;
                    return;
                }

                moveToTile(MathUtils.random.nextInt(5), MathUtils.random.nextInt(4));
            }
        }
    }

    private boolean isInSnow() {
        float snow =  landscape.tiles[(int)position.x + (int)position.z * Landscape.TILES_WIDE].getAverageSnowHeight();
        return snow > 0.08f;
    }

    public Decal get() {
        int index = (waveTime > 0) ? waveAnimation.getKeyFrameIndex(waveTime) : regionAnimation.getKeyFrameIndex(time);
        Decal decal = (waveTime > 0) ? waveDecals.get(index) : decals.get(index);
        decal.setScaleX(right ? directionX : -directionX);
        decal.setPosition(position);

        if (launchTime > 0) {
            decal.setRotationZ(launchTime * 540);
        }

        return decal;
    }

    private boolean launched = false;
    private float launchTime = 0;

    public void launch() {
        launched = true;
        waveTime = moveTime = 0;
        moveTimeTotal = 5;
        Main.game.audio.playSound(this.imageInfo.scream, 0.35F);
    }
}
