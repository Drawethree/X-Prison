package dev.drawethree.xprison.utils.block;

public enum ExplosionType {
    CUBE,
    SPHERE;

    public static ExplosionBlockProvider getBlockProvider(ExplosionType type) {
        if ("CUBE".equalsIgnoreCase(type.name())) {
            return CuboidExplosionBlockProvider.instance();
        } else if ("SPHERE".equalsIgnoreCase(type.name())) {
            return SpheroidExplosionBlockProvider.instance();
        } else {
            throw new IllegalArgumentException("Cannot construct ExplosionBlockProvider for " + type.name());
        }
    }
}
