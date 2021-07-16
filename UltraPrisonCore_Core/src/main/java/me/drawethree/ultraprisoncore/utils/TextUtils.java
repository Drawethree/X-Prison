package me.drawethree.ultraprisoncore.utils;

import me.lucko.helper.text3.Text;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {

	public static List<String> colorize(List<String> list) {
		List<String> returnVal = new ArrayList<>(list.size());
		list.forEach(s -> returnVal.add(Text.colorize(s)));
		return returnVal;
	}
}
