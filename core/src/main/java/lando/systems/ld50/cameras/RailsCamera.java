package lando.systems.ld50.cameras;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import lando.systems.ld50.screens.GameScreen;

public class RailsCamera extends InputAdapter {

    private final Camera camera;
    private final IntIntMap keys = new IntIntMap();
    private float unitsPerPixel = 0.02f;
    private final Vector3 tmp = new Vector3();
    private final GameScreen gameScreen;

    private final Vector3 cameraDir = new Vector3(0.50008386f,-0.656027f,-0.5652821f);

    public RailsCamera(Camera camera, GameScreen gameScreen) {
        this.camera = camera;
        this.gameScreen = gameScreen;
    }

    /** Sets how many degrees to rotate per pixel the mouse moved.
     * @param unitsPerPixel scale*/
    public void setUnitsPerPixel(float unitsPerPixel) {
        this.unitsPerPixel = unitsPerPixel;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        if (gameScreen.currentCameraPhase == GameScreen.CameraPhase.avalanche) return false;
        if (gameScreen.isSettingShown || gameScreen.isControlShown) return false;
        if (Input.Buttons.LEFT != pointer) { return false; }
        float deltaX = -Gdx.input.getDeltaX() * unitsPerPixel * 0.707f;
        float deltaY = -Gdx.input.getDeltaY() * unitsPerPixel * 0.707f;
        camera.position.z = MathUtils.clamp(camera.position.z + deltaX + deltaY, 10, 110);
        return true;
    }

    public void update () {
        update(Gdx.graphics.getDeltaTime());
    }

    public void update (float deltaTime) {
        camera.update(true);
    }

}
