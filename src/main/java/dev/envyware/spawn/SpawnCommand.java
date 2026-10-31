package dev.envyware.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SpawnCommand implements TabExecutor {

    private final SpawnPlugin plugin;

    public SpawnCommand(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            spawnOther(sender, args[0]);
            return true;
        }
        if (!(sender instanceof Player player)) {
            plugin.send(sender, "player-only");
            return true;
        }
        if (!player.hasPermission("spawn.use")) {
            plugin.send(player, "no-permission");
            return true;
        }
        Location spawn = plugin.getSpawn();
        if (spawn == null) {
            plugin.send(player, "no-spawn");
            return true;
        }
        if (player.hasPermission("spawn.bypass")) {
            plugin.teleportToSpawn(player, spawn);
            return true;
        }
        plugin.getCountdowns().start(player);
        return true;
    }

    private void spawnOther(CommandSender sender, String targetName) {
        if (!sender.hasPermission("spawn.others")) {
            plugin.send(sender, "no-permission");
            return;
        }
        Location spawn = plugin.getSpawn();
        if (spawn == null) {
            plugin.send(sender, "no-spawn");
            return;
        }
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            plugin.send(sender, "player-not-found", "{player}", targetName);
            return;
        }
        plugin.getCountdowns().cancel(target);
        plugin.teleportToSpawn(target, spawn);
        if (target != sender) {
            plugin.send(sender, "teleported-other", "{player}", target.getName());
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> names = new ArrayList<>();
        if (args.length != 1 || !sender.hasPermission("spawn.others")) {
            return names;
        }
        String typed = args[0].toLowerCase(Locale.ROOT);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase(Locale.ROOT).startsWith(typed)) {
                names.add(online.getName());
            }
        }
        return names;
    }
}
