package lando.systems.ld50.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.assets.ImageInfo;

public class AnimationDecal {

    private float time = 0;
    private Animation<TextureRegion> regionAnimation;

    private Vector3 position = new Vector3();
    private Vector3 initPos = new Vector3();
    private float directionX;
    private boolean right = true;
    private boolean animating = true;

    private Array<Decal> decals = new Array<>();

    public AnimationDecal(Assets assets, ImageInfo imageInfo, float x, float y, float z) {
        regionAnimation = new Animation<>(0.1f, assets.atlas.findRegions(imageInfo.region), Animation.PlayMode.LOOP);
        initPos.set(x, 0.25f, z);
        position.set(x, 0.25f, z);
        directionX = imageInfo.right ? 1 : -1;

        for (TextureRegion region : regionAnimation.getKeyFrames()) {
            Decal decal = Decal.newDecal(region, true);
            decal.setDimensions(imageInfo.width, imageInfo.height);
            decal.setPosition(position);
            decals.add(decal);
        }
    }

    public void update(float dt) {
        time += dt;
        position.x += right ? dt : -dt;
        if (Math.abs(position.x - initPos.x) > 1) {
            initPos.set(position);
            right = !right;
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
