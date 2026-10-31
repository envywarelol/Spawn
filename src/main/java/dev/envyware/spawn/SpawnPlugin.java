package dev.envyware.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SpawnPlugin extends JavaPlugin {

    private FileConfiguration messages;
    private CountdownListener countdowns;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        countdowns = new CountdownListener(this);
        getServer().getPluginManager().registerEvents(countdowns, this);

        SpawnAdminCommand adminCommand = new SpawnAdminCommand(this);
        getCommand("setspawn").setExecutor(adminCommand);
        getCommand("delspawn").setExecutor(adminCommand);

        SpawnCommand spawnCommand = new SpawnCommand(this);
        getCommand("spawn").setExecutor(spawnCommand);
        getCommand("spawn").setTabCompleter(spawnCommand);
    }

    @Override
    public void onDisable() {
        if (countdowns != null) {
            countdowns.cancelAll();
        }
    }

    public CountdownListener getCountdowns() {
        return countdowns;
    }

    public String rawMessage(String key, String... replacements) {
        String text = messages.getString(key, key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace(replacements[i], replacements[i + 1]);
        }
        return text;
    }

    public void send(CommandSender sender, String key, String... replacements) {
        String prefix = messages.getString("prefix", "");
        sender.sendMessage(Util.color(prefix + rawMessage(key, replacements)));
    }

    public void playSound(Player player, String name) {
        ConfigurationSection section = getConfig().getConfigurationSection("sounds." + name);
        Util.playSound(player, section, getLogger());
    }

    public Location getSpawn() {
        ConfigurationSection section = getConfig().getConfigurationSection("spawn");
        if (section == null) {
            return null;
        }
        World world = Bukkit.getWorld(section.getString("world", ""));
        if (world == null) {
            return null;
        }
        return new Location(world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch"));
    }

    public void teleportToSpawn(Player player, Location spawn) {
        player.getScheduler().run(this, task -> player.teleportAsync(spawn).thenAccept(success -> {
            if (success) {
                playSound(player, "teleport");
                send(player, "teleported");
            }
        }), null);
    }
}
