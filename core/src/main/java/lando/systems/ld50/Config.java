package lando.systems.ld50;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import text.formic.Stringf;

public class Config {
    public static final String title = "Ludum Dare 50";
    public static final int window_width = 1280;
    public static final int window_height = 720;
    public static final boolean shader_debug_log = false;

    public static boolean debug_general = false;

    public static String getFpsString() {
        return Stringf.format("[FPS] %d", Gdx.graphics.getFramesPerSecond());
    }

    public static String getJavaHeapString() {
        return Stringf.format("[Heap (java)] %,d kb", Gdx.app.getJavaHeap() / 1024);
    }

    public static String getNativeHeapString() {
        return Stringf.format("[Heap (native)] %,d kb", Gdx.app.getNativeHeap() / 1024);
    }

    public static String getDrawCallString(SpriteBatch batch) {
        return Stringf.format("[Render Calls] %d", batch.renderCalls);
    }

    public static String getSimTime(SpriteBatch batch, long timing) {
        return Stringf.format("[Physics Sim Time] %.3f ms", timing/1000000f);
    }
}
