package dev.envyware.spawn;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SpawnAdminCommand implements CommandExecutor {

    private final SpawnPlugin plugin;

    public SpawnAdminCommand(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("setspawn")) {
            setSpawn(sender);
        } else {
            deleteSpawn(sender);
        }
        return true;
    }

    private void setSpawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.send(sender, "player-only");
            return;
        }
        if (!player.hasPermission("spawn.set")) {
            plugin.send(player, "no-permission");
            return;
        }
        Location location = player.getLocation();
        plugin.getConfig().set("spawn.world", location.getWorld().getName());
        plugin.getConfig().set("spawn.x", location.getX());
        plugin.getConfig().set("spawn.y", location.getY());
        plugin.getConfig().set("spawn.z", location.getZ());
        plugin.getConfig().set("spawn.yaw", location.getYaw());
        plugin.getConfig().set("spawn.pitch", location.getPitch());
        plugin.saveConfig();
        plugin.playSound(player, "set");
        plugin.send(player, "spawn-set");
    }

    private void deleteSpawn(CommandSender sender) {
        if (!sender.hasPermission("spawn.delete")) {
            plugin.send(sender, "no-permission");
            return;
        }
        if (!plugin.getConfig().isConfigurationSection("spawn")) {
            plugin.send(sender, "no-spawn");
            return;
        }
        plugin.getConfig().set("spawn", null);
        plugin.saveConfig();
        if (sender instanceof Player player) {
            plugin.playSound(player, "delete");
        }
        plugin.send(sender, "spawn-deleted");
    }
}
