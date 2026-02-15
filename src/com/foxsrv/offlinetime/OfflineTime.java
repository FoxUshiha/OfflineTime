package com.foxsrv.offlinetime;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OfflineTime extends JavaPlugin implements Listener {

    private Economy economy;
    private final ConcurrentHashMap<UUID, Long> logoutTime = new ConcurrentHashMap<>();

    private static final long DAY_1 = 86400L;
    private static final long DAY_7 = 604800L;
    private static final long DAY_30 = 2592000L;
    private static final long DAY_90 = 7776000L;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault não encontrado! Desativando plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("OfflineTime ativado com sucesso.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) return false;

        economy = rsp.getProvider();
        return economy != null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        logoutTime.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (player.hasPermission("time.bypass")) return;

        UUID uuid = player.getUniqueId();

        if (!logoutTime.containsKey(uuid)) return;

        long logoutTimestamp = logoutTime.remove(uuid);
        long offlineSeconds = (System.currentTimeMillis() - logoutTimestamp) / 1000;

        if (offlineSeconds <= 0) return;

        double balance = economy.getBalance(player);

        if (balance <= DAY_1) return;

        double multiplier;

        if (balance > DAY_90) {
            multiplier = 5.0;
        } else if (balance > DAY_30) {
            multiplier = 3.0;
        } else if (balance > DAY_7) {
            multiplier = 2.0;
        } else {
            multiplier = 1.0;
        }

        double totalToRemove = offlineSeconds * multiplier;
        double maxRemovable = balance - DAY_1;

        if (totalToRemove > maxRemovable) {
            totalToRemove = maxRemovable;
        }

        if (totalToRemove > 0) {
            economy.withdrawPlayer(player, totalToRemove);
        }
    }
}
