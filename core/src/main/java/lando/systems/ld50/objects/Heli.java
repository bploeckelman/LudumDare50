package lando.systems.ld50.objects;

import lando.systems.ld50.assets.Assets;
import lando.systems.ld50.assets.ImageInfo;

public class Heli extends AnimationDecal {

    private LandTile tile;
    private boolean flyAway = false;

    public Heli(Assets assets, Landscape landscape, LandTile tile) {
        super(assets, ImageInfo.Heli, landscape, -5, 20, tile.intZ);

        this.tile = tile;
        moveToTile(tile.intX, tile.intZ);
        moveTimeTotal = 1;
        isHeli = true;
    }

    @Override
    public void hit() {
        // no launch
    }

    @Override
    protected void completeMovement() {
        if (flyAway) {
            dead = true;
            return;
        }

        landscape.screen.savePerson(tile);

        flyAway = true;
        flyAway(30, tile.intZ);
        landscape.screen.isHeliInProgress = false;
    }

    @Override
    protected boolean isInSnow() {
        return false;
    }
}
