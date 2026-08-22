package dev.wechirok.betterselectivecombat.lang;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Translations {
    public static final String DEFAULT_LANGUAGE = "en_us";
    public static final List<String> SUPPORTED_LANGUAGES = List.of(
            "de_de",
            "en_us",
            "es_ar",
            "es_es",
            "es_mx",
            "fr_fr",
            "it_it",
            "ja_jp",
            "ko_kr",
            "pt_br",
            "ru_ru",
            "uk_ua",
            "vi_vn",
            "zh_cn"
    );

    private static final Type DICTIONARY_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    private final Map<String, Map<String, String>> dictionaries = new LinkedHashMap<>();

    public Translations() {
        Gson gson = new Gson();
        for (String language : SUPPORTED_LANGUAGES) {
            String resource = "assets/better_selective_combat/lang/" + language + ".json";
            try (InputStream stream = Translations.class.getClassLoader().getResourceAsStream(resource)) {
                if (stream == null) {
                    throw new IllegalStateException("Missing language resource: " + resource);
                }
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    Map<String, String> dictionary = gson.fromJson(reader, DICTIONARY_TYPE);
                    dictionaries.put(language, Map.copyOf(dictionary));
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot load language resource: " + resource, exception);
            }
        }
    }

    public String text(String requestedLanguage, String key, Object... arguments) {
        String language = resolveLanguage(requestedLanguage);
        String template = dictionaries.get(language).get(key);
        if (template == null) {
            template = dictionaries.get(DEFAULT_LANGUAGE).getOrDefault(key, key);
        }
        return format(template, arguments);
    }

    public String resolveLanguage(String requestedLanguage) {
        if (requestedLanguage == null || requestedLanguage.isBlank()) {
            return DEFAULT_LANGUAGE;
        }
        String normalized = requestedLanguage.toLowerCase(Locale.ROOT).replace('-', '_');
        if (dictionaries.containsKey(normalized)) {
            return normalized;
        }
        String prefix = normalized.contains("_") ? normalized.substring(0, normalized.indexOf('_')) : normalized;
        return switch (prefix) {
            case "de" -> "de_de";
            case "en" -> "en_us";
            case "es" -> "es_es";
            case "fr" -> "fr_fr";
            case "it" -> "it_it";
            case "ja" -> "ja_jp";
            case "ko" -> "ko_kr";
            case "pt" -> "pt_br";
            case "ru" -> "ru_ru";
            case "uk" -> "uk_ua";
            case "vi" -> "vi_vn";
            case "zh" -> "zh_cn";
            default -> DEFAULT_LANGUAGE;
        };
    }

    private String format(String template, Object[] arguments) {
        StringBuilder result = new StringBuilder();
        int argumentIndex = 0;
        int cursor = 0;
        while (cursor < template.length()) {
            int placeholder = template.indexOf("%s", cursor);
            if (placeholder < 0 || argumentIndex >= arguments.length) {
                result.append(template, cursor, template.length());
                break;
            }
            result.append(template, cursor, placeholder);
            result.append(arguments[argumentIndex++]);
            cursor = placeholder + 2;
        }
        return result.toString();
    }
}
