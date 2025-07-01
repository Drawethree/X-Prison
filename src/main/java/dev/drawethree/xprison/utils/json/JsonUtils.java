package dev.drawethree.xprison.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class JsonUtils {

    private JsonUtils() {
        // Utility class, no instances
    }

    public static int getInt(JsonObject obj, String key, int defaultValue) {
        JsonElement element = obj.get(key);
        return (element != null && !element.isJsonNull()) ? element.getAsInt() : defaultValue;
    }

    public static long getLong(JsonObject obj, String key, long defaultValue) {
        JsonElement element = obj.get(key);
        return (element != null && !element.isJsonNull()) ? element.getAsLong() : defaultValue;
    }

    public static boolean getBoolean(JsonObject obj, String key, boolean defaultValue) {
        JsonElement element = obj.get(key);
        return (element != null && !element.isJsonNull()) ? element.getAsBoolean() : defaultValue;
    }

    public static double getDouble(JsonObject obj, String key, double defaultValue) {
        JsonElement element = obj.get(key);
        return (element != null && !element.isJsonNull()) ? element.getAsDouble() : defaultValue;
    }

    public static String getString(JsonObject obj, String key, String defaultValue) {
        JsonElement element = obj.get(key);
        return (element != null && !element.isJsonNull()) ? element.getAsString() : defaultValue;
    }

    public static JsonObject getObject(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        return (element != null && element.isJsonObject()) ? element.getAsJsonObject() : new JsonObject();
    }
}
