package lando.systems.ld50.utils;

import com.badlogic.gdx.math.MathUtils;

public class Calc {

    /**
     * Given a line segment (x0,y0)..(x1,y1) and an x value,
     * return the y value such that (x,y) is on the line
     */
    public static float linear_remap_f(float x, float x0, float y0, float x1, float y1) {
        float dx = (x1 - x0);
        float dy = (y1 - y0);
        if (dx == 0) {
            return 0;
        }
        return (x - x0) / dx * dy + y0;
    }

    /**
     * Given a line segment (x0,y0)..(x1,y1) and an x value,
     * return the y value such that (x,y) is on the line
     */
    public static int linear_remap_i(int x, int x0, int y0, int x1, int y1) {
        int dx = (x1 - x0);
        int dy = (y1 - y0);
        if (dx == 0) {
            return 0;
        }
        return (x - x0) / dx * dy + y0;
    }

    public static float mod_f(float x, float m) {
        return x - (int)(x / m) * m;
    }

    public static int clamp_i(int t, int min, int max) {
        if      (t < min) return min;
        else if (t > max) return max;
        else              return t;
    }

    public static float floor(float value) {
        return MathUtils.floor(value);
    }

    public static float ceil(float value) {
        return MathUtils.ceil(value);
    }

    public static float min(float a, float b) {
        return (a < b) ? a : b;
    }

    public static float max(float a, float b) {
        return (a > b) ? a : b;
    }

    public static int min(int a, int b) {
        return (a < b) ? a : b;
    }

    public static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    public static float approach(float t, float target, float delta) {
        return (t < target) ? min(t + delta, target) : max(t - delta, target);
    }

    public static int sign(int val) {
        return (val < 0) ? -1
             : (val > 0) ? 1
             : 0;
    }

    public static float sign(float val) {
        return (val < 0) ? -1
             : (val > 0) ? 1
             : 0;
    }

    public static int abs(int val) {
        return (val < 0) ? -val : val;
    }

}
