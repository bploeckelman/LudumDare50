package lando.systems.ld50.assets;

public enum ImageInfo {
    Babe ("characters/babe-a", 0.5f, 0.5f),
    Dude ("characters/dude-a", 0.5f, 0.5f),
    Deer ("characters/deer-a", 0.5f, 0.5f),
    Plow ("characters/plow-a", 7f, 4f);

    public String region;
    public float width;
    public float height;

    ImageInfo(String region, float width, float height) {
        this.region = region;
        this.width = width;
        this.height = height;
    }
}
