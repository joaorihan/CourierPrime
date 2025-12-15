package com.joaorihan.courierprime.listener;

import com.joaorihan.courierprime.CourierPrime;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener implements Listener {

    private final CourierPrime plugin;

    public UpdateListener(CourierPrime plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if (!e.getPlayer().hasPermission("courierprime.admin")){
            return;
        }

        plugin.getUpdateChecker().showUpdateMessage(e.getPlayer());
    }

}
