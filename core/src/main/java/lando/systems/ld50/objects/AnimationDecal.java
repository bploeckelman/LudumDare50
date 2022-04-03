package lando.systems.ld50.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.assets.ImageInfo;

public class AnimationDecal {

    private float time = 0;
    private Animation<TextureRegion> regionAnimation;

    private Array<Decal> decals = new Array<>();

    public AnimationDecal(Assets assets, ImageInfo imageInfo, float x, float y, float z) {
       regionAnimation = new Animation<>(0.1f, assets.atlas.findRegions(imageInfo.region), Animation.PlayMode.LOOP);
       for (TextureRegion region : regionAnimation.getKeyFrames()) {
           Decal decal = Decal.newDecal(region, true);
           decal.setDimensions(imageInfo.width, imageInfo.height);
           decal.setPosition(x, y, z);
           decals.add(decal);
       }
    }

    public void update(float dt) {
        time += dt;
    }

    public Decal get() {
        int index = regionAnimation.getKeyFrameIndex(time);
        return decals.get(index);
    }
}
