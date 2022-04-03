package lando.systems.ld50.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.assets.ImageInfo;

public class AnimationDecal {

    private float time = 0;
    private Animation<TextureRegion> regionAnimation;
    private Landscape landscape;
    private ImageInfo imageInfo;

    private Vector3 initPos = new Vector3();
    private Vector3 position = new Vector3();

    private float moveTime, moveTimeTotal;
    private Vector3 movePosition = new Vector3();

    private float directionX;
    private boolean right = true;

    float dx = 0, dz = 0;

    private Array<Decal> decals = new Array<>();

    public AnimationDecal(Assets assets, ImageInfo imageInfo, Landscape landscape, int x, int z) {
        regionAnimation = new Animation<>(0.1f, assets.atlas.findRegions(imageInfo.region), Animation.PlayMode.LOOP);
        this.landscape = landscape;
        this.imageInfo = imageInfo;

        TextureRegion r = regionAnimation.getKeyFrame(0);

        // 0 is center of image
        if (!setTilePosition(initPos, x, z)) {
            setTilePosition(initPos, 0, 0);
        }
        position.set(initPos);

        directionX = imageInfo.right ? 1 : -1;

        for (TextureRegion region : regionAnimation.getKeyFrames()) {
            Decal decal = Decal.newDecal(region, true);
            decal.setDimensions(imageInfo.width, imageInfo.height);
            decal.setPosition(position);
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
            moveTimeTotal = Math.abs(movePosition.dst(initPos)) / 5;
            moveTime = 0;
        } else {
            movePosition.setZero();
        }
    }

    public void update(float dt) {
        time += dt;
        if (moveTimeTotal > 0) {
            moveTime += dt;
            float lerp = MathUtils.clamp(moveTime / moveTimeTotal, 0, 1);
            float x = MathUtils.lerp(initPos.x, movePosition.x, lerp);
            float z = MathUtils.lerp(initPos.z, movePosition.z, lerp);
            float y = landscape.getHeightAt(x, z) +  imageInfo.height / 2;
            position.set(x, y, z);

            if (lerp == 1) {
                moveTimeTotal = 0;
                initPos.set(position);
                moveToTile(MathUtils.random.nextInt(5), MathUtils.random.nextInt(8));
            }
        }
    }

    public Decal get() {
        int index = regionAnimation.getKeyFrameIndex(time);
        Decal decal = decals.get(index);
        decal.setScaleX(right ? directionX : -directionX);
        decal.setPosition(position);
        return decal;
    }
}
