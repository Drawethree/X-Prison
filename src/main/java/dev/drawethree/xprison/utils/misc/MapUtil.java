package dev.drawethree.xprison.utils.misc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MapUtil {
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue
			(Map<K, V> map) {

		return map.entrySet()
				.stream()
				.sorted(Map.Entry.<K, V>comparingByValue().reversed())
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(e1, e2) -> e1,
						LinkedHashMap::new
				));
	}

	private MapUtil() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
