package java.net;

import java.util.Locale;

import org.apache.commons.validator.routines.Punycode;
import org.apache.commons.validator.routines.PunycodeException;

public class IDN {
	public static String toASCII(String input) {
		if (isOnlyASCII(input)) {
			return input;
		}
		try {
			return Punycode.encodeDomain(input.toLowerCase(Locale.ENGLISH));
		} catch (PunycodeException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/*
	 * Check if input contains only ASCII Treats null as all ASCII
	 */
	private static boolean isOnlyASCII(String input) {
		if (input == null) {
			return true;
		}
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) > 0x7F) {
				return false;
			}
		}
		return true;
	}

	public static String toUnicode(String line) {
		return line;
	}

}
