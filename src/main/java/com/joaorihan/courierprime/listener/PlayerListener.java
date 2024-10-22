package com.joaorihan.courierprime.listener;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.config.CourierOptions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;


/**
 * Listens to player related events
 *
 * @author João Rihan
 */
public class PlayerListener implements Listener {


    public PlayerListener(CourierPrime plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    /**
     * Check when a player joins, so they can retrieve unread mail
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                Courier.spawn(player);
            }
        }.runTaskLater(CourierPrime.getInstance(), CourierOptions.RECEIVE_DELAY);
    }

    /**
     * If a player is coming from a blocked world, they will retrieve their mail
     */
    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Set<World> worlds = CourierOptions.BLOCKED_WORLDS;
        World to = e.getTo().getWorld();
        World from = e.getFrom().getWorld();
        Player recipient = e.getPlayer();

        if (worlds.contains(from) && !worlds.contains(to)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Courier.spawn(recipient);
                }
            }.runTaskLater(CourierPrime.getInstance(), CourierOptions.RECEIVE_DELAY);
        }
    }

    /**
     * If a player is changing from a blocked gamemode, they will retrieve their mail
     */
    @EventHandler
    public void onGamemode(PlayerGameModeChangeEvent e) {
        Set<GameMode> modes = CourierOptions.BLOCKED_GAMEMODES;
        GameMode to = e.getNewGameMode();
        GameMode from = e.getPlayer().getGameMode();
        Player recipient = e.getPlayer();
        if (modes.contains(from) && !modes.contains(to)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Courier.spawn(recipient);
                }
            }.runTaskLater(CourierPrime.getInstance(), CourierOptions.RECEIVE_DELAY);
        }
    }


}
