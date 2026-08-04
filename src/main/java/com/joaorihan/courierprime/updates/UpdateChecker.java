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

    private static final String RESOURCE_ID = "122626";

    private final CourierPrime plugin;
    @Getter
    private volatile String latestVersion;
    private volatile boolean active = true;

    public UpdateChecker(CourierPrime plugin) {
        this.plugin = plugin;
        fetchLatestVersion();
    }

    /**
     * Marks this checker as obsolete. The scheduler cancellation performed by
     * the plugin lifecycle stops queued work; the active flag also protects a
     * request that is already blocked in network I/O from publishing into a
     * reloaded or disabled plugin state.
     */
    public void shutdown() {
        active = false;
    }

    /**
     * Restarts checking after a failed reload has restored this checker as the
     * active runtime manager.
     */
    public synchronized void restart() {
        if (active) {
            return;
        }
        active = true;
        fetchLatestVersion();
    }

    /**
     * Asynchronously fetches the latest version string from the Spigot API.
     */
    private void fetchLatestVersion() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!isCurrent()) {
                return;
            }

            try (InputStream inputStream = new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID + "/~"
            ).openStream();
                 Scanner scanner = new Scanner(inputStream)) {

                if (scanner.hasNext()) {
                    String fetchedVersion = scanner.next();
                    if (isCurrent()) {
                        latestVersion = fetchedVersion;
                    }
                }
            } catch (IOException | RuntimeException exception) {
                if (isCurrent()) {
                    plugin.getLogger().info("Unable to check for updates: " + exception.getMessage());
                }
            }
        });
    }

    /**
     * Checks if the plugin's current version matches the cached latest version.
     *
     * @return true if versions match or if the latest version isn't fetched yet
     */
    private boolean isLatestVersion() {
        String latest = getLatestVersion();

        // Avoid reporting an update while the asynchronous request is pending.
        if (latest == null) {
            return true;
        }

        return plugin.getDescription().getVersion().equals(latest);
    }

    /**
     * Sends an update message to the player if a new version is available.
     *
     * @param player the player to send the message to
     */
    public void showUpdateMessage(Player player) {
        if (!isCurrent() || !plugin.isEnabled() || isLatestVersion()) {
            return;
        }

        String latest = getLatestVersion();
        if (latest == null || plugin.getMessageManager() == null) {
            return;
        }

        player.sendMessage(plugin.getMessageManager().getMessage(Message.NEW_VERSION_AVAILABLE, true)
                .replace("%NEW_VERSION%", latest));
    }

    private boolean isCurrent() {
        return active && plugin == CourierPrime.getPlugin();
    }
}
