package com.littlesheep.tprandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RandomTeleportCommand implements CommandExecutor {

    private final Main plugin;
    private final ConcurrentHashMap<UUID, Location> playerLocations = new ConcurrentHashMap<>();

    public RandomTeleportCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("tpr.reload")) {
                sender.sendMessage(plugin.getLangMessage("commands.no-permission"));
                return true;
            }
            plugin.reloadPluginConfig();
            sender.sendMessage(plugin.getLangMessage("commands.reload-success"));
            return true;
        }

        // 随机传送命令逻辑
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("commands.only-player"));
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();
        playerLocations.put(playerId, player.getLocation());

        int range = plugin.getConfig().getInt("teleport.range");
        int countdown = plugin.getConfig().getInt("teleport.countdown");

        player.sendMessage(plugin.getLangMessage("commands.countdown-message").replace("{countdown}", String.valueOf(countdown)));

        BossBar bossBar = Bukkit.createBossBar(plugin.getLangMessage("commands.bossbar-countdown").replace("{countdown}", String.valueOf(countdown)), BarColor.BLUE, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);

        for (int i = 0; i <= countdown; i++) {
            int finalI = i;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (!player.isOnline() || !playerLocations.containsKey(playerId)) {
                    bossBar.removePlayer(player);
                    return;
                }

                if (finalI < countdown) {
                    Location initialLocation = playerLocations.get(playerId);
                    Location currentLocation = player.getLocation();
                    if (initialLocation.distanceSquared(currentLocation) > 1) {
                        player.sendMessage(plugin.getLangMessage("commands.teleport-cancelled"));
                        playerLocations.remove(playerId);
                        bossBar.removePlayer(player);
                        return;
                    }
                }

                double progress = (double) (countdown - finalI) / countdown;
                bossBar.setProgress(progress);
                bossBar.setTitle(plugin.getLangMessage("commands.bossbar-countdown").replace("{countdown}", String.valueOf(countdown - finalI)));

                if (finalI == countdown) {
                    Random random = new Random();
                    int x = random.nextInt(range * 2) - range;
                    int z = random.nextInt(range * 2) - range;
                    int y = player.getWorld().getHighestBlockYAt(x, z) + 1;

                    Location randomLocation = new Location(player.getWorld(), x, y, z);
                    player.teleport(randomLocation);
                    player.sendMessage(plugin.getLangMessage("commands.teleport-success").replace("{location}", formatLocation(randomLocation)));

                    bossBar.removePlayer(player);
                    playerLocations.remove(playerId);
                }
            }, 20L * i);
        }
        return true;
    }

    private String formatLocation(Location location) {
        return String.format("X: %d, Y: %d, Z: %d", location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
