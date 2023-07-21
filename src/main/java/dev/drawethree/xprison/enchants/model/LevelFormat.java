package dev.drawethree.xprison.enchants.model;

import java.util.Map;
import java.util.TreeMap;

public enum LevelFormat {

    NUMBER, ROMAN, FIXED;

    private static final Map<Integer, String> FIXED_NUMBERS = Map.of(
            1, "I",
            2, "II",
            3, "III",
            4, "IV",
            5, "V",
            6, "VI",
            7, "VII",
            8, "VIII",
            9, "IX",
            10, "X"
    );

    private static final TreeMap<Integer, String> ROMAN_NUMBERS = new TreeMap<>();

    static {
        ROMAN_NUMBERS.put(1000, "M");
        ROMAN_NUMBERS.put(900, "CM");
        ROMAN_NUMBERS.put(500, "D");
        ROMAN_NUMBERS.put(400, "CD");
        ROMAN_NUMBERS.put(100, "C");
        ROMAN_NUMBERS.put(90, "XC");
        ROMAN_NUMBERS.put(50, "L");
        ROMAN_NUMBERS.put(40, "XL");
        ROMAN_NUMBERS.put(10, "X");
        ROMAN_NUMBERS.put(9, "IX");
        ROMAN_NUMBERS.put(5, "V");
        ROMAN_NUMBERS.put(4, "IV");
        ROMAN_NUMBERS.put(1, "I");
    }

    public static LevelFormat of(String name) {
        if (name == null) {
            return LevelFormat.NUMBER;
        }
        switch (name.trim().toUpperCase()) {
            case "ROMAN":
                return LevelFormat.ROMAN;
            case "FIXED":
                return LevelFormat.FIXED;
            case "NUMBER":
            default:
                return LevelFormat.NUMBER;
        }
    }

    public String format(int level) {
        if (this == NUMBER || level < 1) {
            return String.valueOf(level);
        }
        if (level <= 10) {
            return FIXED_NUMBERS.get(level);
        }
        if (this == ROMAN) {
            return toRoman(level);
        }
        return String.valueOf(level);
    }

    private String toRoman(int level) {
        final int mapLevel = ROMAN_NUMBERS.floorKey(level);
        if (mapLevel == level) {
            return ROMAN_NUMBERS.get(level);
        }
        return ROMAN_NUMBERS.get(mapLevel) + toRoman(level - mapLevel);
    }
}
