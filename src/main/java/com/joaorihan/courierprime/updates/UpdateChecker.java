package com.joaorihan.courierprime.updates;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.Message;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class UpdateChecker {

    private final CourierPrime plugin;
    private final String RESOURCE_ID = "122626";
    @Getter private String latestVersion;

    public UpdateChecker(CourierPrime plugin){
        this.plugin = plugin;
        fetchLatestVersion();
    }

    /**
     * Asynchronously fetches the latest version string from the Spigot API
     * and stores it in the 'latestVersion' field.
     */
    private void fetchLatestVersion() {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.RESOURCE_ID + "/~").openStream();
                 Scanner scanner = new Scanner(is)) {

                if (scanner.hasNext()) {
                    this.latestVersion = scanner.next();
                }
            } catch (IOException e) {
                plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
            }
        });
    }

    /**
     * Checks if the plugin's current version matches the cached latest version.
     * @return true if versions match or if the latest version isn't fetched yet.
     */
    private boolean isLatestVersion() {
        String latest = getLatestVersion();

        // Just to avoid errors in case the fetch hasn't yet finished
        if (latest == null) {
            return true;
        }

        return plugin.getDescription().getVersion().equals(latest);
    }

    /**
     * Sends an update message to the player if a new version is available.
     * @param player The player to send the message to.
     */
    public void showUpdateMessage(Player player){
        if (isLatestVersion()){
            return;
        }

        player.sendMessage(plugin.getMessageManager().getMessage(Message.NEW_VERSION_AVAILABLE, true)
                .replace("%NEW_VERSION%", getLatestVersion()));
    }

}