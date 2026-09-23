package dev.wechirok.betterselectivecombat.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslationsTest {
    @Test
    void existingHudAndHeldMessagesResolveInEveryLanguage() {
        var translations = new Translations();
        for (String language : Translations.SUPPORTED_LANGUAGES) {
            for (String key : new String[]{
                    "bsc.client.global.enabled", "bsc.client.global.disabled",
                    "bsc.client.item.enabled", "bsc.client.item.disabled",
                    "bsc.client.item.empty", "bsc.disable.success", "bsc.enable.success",
                    "bsc.status.enabled", "bsc.status.disabled", "bsc.error.permission"}) {
                String message = translations.text(language, key, "example:greatsword");
                assertFalse(message.isBlank(), language + ": " + key);
                assertNotEquals(key, message, language);
            }
        }
    }
}
