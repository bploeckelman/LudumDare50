package lando.systems.ld50.assets;

import static lando.systems.ld50.audio.AudioManager.*;

public enum ImageInfo {
    BabeA ("characters/babe-a-walk", "characters/babe-a-wave", 0.1f, Sounds.screamFemale, 0.5f, 0.5f, true),
    BabeB ("characters/babe-b-walk", "characters/babe-b-wave", 0.1f, Sounds.screamFemale, 0.5f, 0.5f, true),
    Dude ("characters/dude-a", null, 0.1f, Sounds.screamMale, 0.5f, 0.5f, true),
    Deer ("characters/deer-a", null, 0.1f, Sounds.none, 0.5f, 0.5f, true),
    Plow ("characters/plow-a", null, 0.1f, Sounds.none,2f, 1f, false),
    Heli ("characters/chopper", null, 0.1f, Sounds.none, 2f, 1f, false);

    public String region;
    public String waveRegion;
    public Sounds scream;
    public float width;
    public float height;
    public float frameDuration;
    public boolean right;

    ImageInfo(String region, String waveRegion, float duration, Sounds scream, float width, float height, boolean right) {
        this.region = region;
        this.waveRegion = waveRegion;
        this.frameDuration = duration;
        this.scream = scream;
        this.width = width;
        this.height = height;
        this.right = right;
    }
}
