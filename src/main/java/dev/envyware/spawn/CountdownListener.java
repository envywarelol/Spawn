package dev.envyware.spawn;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CountdownListener implements Listener {

    private final SpawnPlugin plugin;
    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public CountdownListener(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(Player player) {
        UUID uuid = player.getUniqueId();
        ScheduledTask old = tasks.remove(uuid);
        if (old != null) {
            old.cancel();
            plugin.send(player, "countdown-restarted");
        }

        int seconds = Math.max(1, plugin.getConfig().getInt("countdown-seconds", 5));
        int[] remaining = {seconds};

        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, scheduled -> {
            if (remaining[0] <= 0) {
                finish(player, scheduled);
                return;
            }
            String value = String.valueOf(remaining[0]);
            player.sendActionBar(Util.color(plugin.rawMessage("countdown-actionbar", "{seconds}", value)));
            plugin.send(player, "countdown-chat", "{seconds}", value);
            plugin.playSound(player, "tick");
            remaining[0]--;
        }, () -> tasks.remove(uuid), 1L, 20L);

        if (task != null) {
            tasks.put(uuid, task);
        }
    }

    private void finish(Player player, ScheduledTask scheduled) {
        scheduled.cancel();
        tasks.remove(player.getUniqueId(), scheduled);
        Location spawn = plugin.getSpawn();
        if (spawn == null) {
            plugin.send(player, "no-spawn");
            return;
        }
        plugin.teleportToSpawn(player, spawn);
    }

    public boolean cancel(Player player) {
        ScheduledTask task = tasks.remove(player.getUniqueId());
        if (task == null) {
            return false;
        }
        task.cancel();
        return true;
    }

    public void cancelAll() {
        for (ScheduledTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        Player player = event.getPlayer();
        if (cancel(player)) {
            player.sendActionBar(Util.color(plugin.rawMessage("countdown-cancelled")));
            plugin.send(player, "countdown-cancelled");
            plugin.playSound(player, "cancel");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancel(event.getPlayer());
    }
}
