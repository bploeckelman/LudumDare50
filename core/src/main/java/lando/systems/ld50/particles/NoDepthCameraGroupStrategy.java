package lando.systems.ld50.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;

import java.util.Comparator;

public class NoDepthCameraGroupStrategy extends CameraGroupStrategy {
    public NoDepthCameraGroupStrategy(Camera camera) {
        super(camera);
    }

    public NoDepthCameraGroupStrategy(Camera camera, Comparator<Decal> sorter) {
        super(camera, sorter);
    }

    @Override
    public void beforeGroups () {
        super.beforeGroups();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }
}
