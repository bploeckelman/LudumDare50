package lando.systems.ld50.assets;

public enum ImageInfo {
    BabeA ("characters/babe-a", "characters/babe-a-wave", 0.1f),
    BabeB ("characters/babe-b", "characters/babe-b-wave", 0.1f),
    Dude ("characters/dude-a", null, 0.1f),
    Deer ("characters/deer-a", null, 0.1f),
    Plow ("characters/plow-a", null, 0.1f, 7f, 4f, false);

    public String region;
    public String waveRegion;
    public float width;
    public float height;
    public float frameDuration;
    public boolean right;

    ImageInfo(String region, String waveRegion, float duration) {
        this(region, waveRegion, duration, 0.5f, 0.5f, true);
    }

    ImageInfo(String region, String waveRegion, float duration, float width, float height, boolean right) {
        this.region = region;
        this.waveRegion = waveRegion;
        this.frameDuration = duration;
        this.width = width;
        this.height = height;
        this.right = right;
    }
}
