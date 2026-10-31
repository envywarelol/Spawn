package dev.envyware.spawn;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public final class Util {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    private static final Set<String> warnedSounds = new HashSet<>();

    private Util() {
    }

    public static Component color(String text) {
        if (text == null) {
            return Component.empty();
        }
        return SERIALIZER.deserialize(text);
    }

    public static void playSound(Player player, ConfigurationSection section, Logger logger) {
        if (section == null) {
            return;
        }
        String name = section.getString("name", "");
        Sound sound = findSound(name);
        if (sound == null) {
            synchronized (warnedSounds) {
                if (warnedSounds.add(name)) {
                    logger.warning("Invalid sound '" + name + "' at sounds." + section.getName() + ", skipping it.");
                }
            }
            return;
        }
        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private static Sound findSound(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String wanted = name.trim().toUpperCase(Locale.ROOT);
        for (Sound sound : Registry.SOUNDS) {
            NamespacedKey key = Registry.SOUNDS.getKey(sound);
            if (key == null) {
                continue;
            }
            String converted = key.getKey().toUpperCase(Locale.ROOT).replace('.', '_');
            if (converted.equals(wanted)) {
                return sound;
            }
        }
        return null;
    }
}
