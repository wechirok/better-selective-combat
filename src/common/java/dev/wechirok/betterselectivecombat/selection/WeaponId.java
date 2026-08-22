package dev.wechirok.betterselectivecombat.selection;

import java.util.Locale;
import java.util.regex.Pattern;

public final class WeaponId {
    private static final Pattern PATTERN = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    private WeaponId() {
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValid(String value) {
        return value != null && PATTERN.matcher(value).matches();
    }
}
