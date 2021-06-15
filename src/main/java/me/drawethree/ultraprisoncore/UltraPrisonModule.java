package me.drawethree.ultraprisoncore;

public interface UltraPrisonModule {

    boolean isEnabled();
    void reload();
    void enable();
    void disable();
    String getName();

}
