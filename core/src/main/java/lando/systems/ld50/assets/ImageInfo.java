package lando.systems.ld50.assets;

public enum ImageInfo {
    Babe ("characters/babe-a", 0.1f),
    Dude ("characters/dude-a", 0.1f),
    Deer ("characters/deer-a", 0.1f),
    Plow ("characters/plow-a", 0.1f, 7f, 4f, false);

    public String region;
    public float width;
    public float height;
    public float frameDuration;
    public boolean right;

    ImageInfo(String region, float duration) {
        this(region, duration, 0.5f, 0.5f, true);
    }

    ImageInfo(String region, float duration, float width, float height, boolean right) {
        this.region = region;
        this.frameDuration = duration;
        this.width = width;
        this.height = height;
        this.right = right;
    }
}
