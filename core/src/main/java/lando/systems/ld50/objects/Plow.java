package lando.systems.ld50.objects;

import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.assets.ImageInfo;

public class Plow extends AnimationDecal {

    public Plow(Assets assets, Landscape landscape, int z) {
        super(assets, ImageInfo.Plow, landscape, 0, z);

        moveToTile(Landscape.TILES_WIDE - 1, z);
        isPlow = true;
    }

    @Override
    public void hit() {
        // no launch
    }

    @Override
    protected void completeMovement() {
        dead = true;
    }


    private LandTile lastTile = null;
    @Override
    protected void updateMovement(float dt) {
        super.updateMovement(dt);

        LandTile tile = getCurrentTile();
        if (tile != lastTile) {
            lastTile = tile;
            if (tile != null) {
                tile.clearSnow();
            }
        }
    }

    private LandTile getCurrentTile() {
        return landscape.getTileAt((int)position.x, (int)position.z);
    }

    @Override
    protected boolean isInSnow() {
        return false;
    }
}
