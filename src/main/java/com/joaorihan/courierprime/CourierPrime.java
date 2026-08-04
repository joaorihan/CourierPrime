package com.joaorihan.courierprime;

import com.joaorihan.courierprime.command.CommandManager;
import com.joaorihan.courierprime.config.*;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.courier.CourierManager;
import com.joaorihan.courierprime.courier.CourierSelectManager;
import com.joaorihan.courierprime.integration.CustomEntityManager;
import com.joaorihan.courierprime.letter.LetterManager;
import com.joaorihan.courierprime.letter.OutgoingManager;
import com.joaorihan.courierprime.listener.LetterListener;
import com.joaorihan.courierprime.listener.PlayerListener;
import com.joaorihan.courierprime.listener.UpdateListener;
import com.joaorihan.courierprime.updates.UpdateChecker;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Main class for plugin, registers and initializes all files, listeners, and commands
 *
 * @author Jeremy Noesen
 */
public class CourierPrime extends JavaPlugin {

    /**
     * plugin instance
     */
    @Getter
    public static CourierPrime plugin;

    @Getter @Setter
    private ConfigManager configManager;

    @Getter @Setter
    private MessageManager messageManager;

    @Getter @Setter
    private LetterManager letterManager;

    @Getter @Setter
    private OutgoingManager outgoingManager;

    @Getter @Setter
    private CourierSelectManager courierSelectManager;

    @Getter @Setter
    private UpdateChecker updateChecker;

    @Getter @Setter
    private CustomEntityManager customEntityManager;

    /**
     * Listener and command registrations belong to the plugin enable lifecycle,
     * not to a configuration reload.
     */
    private boolean runtimeRegistrationsCreated;

    /**
     * initialize configurations, load messages, register commands and permissions
     */
    public void onEnable() {
        plugin = this;

        reloadState();

        if (!runtimeRegistrationsCreated) {
            // Register listeners once for this plugin enable lifecycle.
            new LetterListener(this);
            new PlayerListener(this);
            new UpdateListener(this);

            // Register commands once. reloadState() never reaches this block.
            new CommandManager();
            runtimeRegistrationsCreated = true;
        }
    }

    /**
     * Rebuilds the configuration-backed runtime state without registering duplicate
     * listeners or commands.
     *
     * <p>The transition is deliberately performed before any replacement manager is
    * installed: old tasks are cancelled, active couriers are removed, and outgoing
     * mail is persisted first. New managers are then constructed from the refreshed
     * configuration and pending online mail is scheduled against the new state.</p>
     */
    public synchronized void reloadState() {
        ConfigManager previousConfigManager = getConfigManager();
        MessageManager previousMessageManager = getMessageManager();
        LetterManager previousLetterManager = getLetterManager();
        OutgoingManager previousOutgoingManager = getOutgoingManager();
        CourierSelectManager previousCourierSelectManager = getCourierSelectManager();
        UpdateChecker previousUpdateChecker = getUpdateChecker();
        CustomEntityManager previousCustomEntityManager = getCustomEntityManager();
        boolean hadRuntimeState = previousConfigManager != null
                || previousMessageManager != null
                || previousLetterManager != null
                || previousOutgoingManager != null
                || previousCourierSelectManager != null
                || previousUpdateChecker != null
                || previousCustomEntityManager != null;
        int previousReceiveDelay = hadRuntimeState ? MainConfig.getReceiveDelay() : 0;

        UpdateChecker nextUpdateChecker = null;
        try {
            stopRuntime(true);
            MainConfig.load();

            ConfigManager nextConfigManager = new ConfigManager(this);
            MessageManager nextMessageManager = new MessageManager(nextConfigManager);

            // LetterSender and OutgoingManager obtain their dependencies from the
            // plugin getters in their constructors, so make the new config/message
            // managers visible at this boundary before constructing them.
            setConfigManager(nextConfigManager);
            setMessageManager(nextMessageManager);

            LetterManager nextLetterManager = new LetterManager(this);
            OutgoingManager nextOutgoingManager = new OutgoingManager(this);
            nextOutgoingManager.loadAll();

            CourierSelectManager nextCourierSelectManager = new CourierSelectManager(nextConfigManager);

            // Initialize custom entity support (MythicMobs/ModelEngine) after
            // configuration, messages, mail, and selection state are ready, but
            // before couriers can be scheduled against the new state.
            CustomEntityManager nextCustomEntityManager = new CustomEntityManager();
            nextCustomEntityManager.initialize();

            nextUpdateChecker = new UpdateChecker(this);

            // Install the complete replacement set only after all managers have
            // been constructed and outgoing data has been loaded successfully.
            setConfigManager(nextConfigManager);
            setMessageManager(nextMessageManager);
            setLetterManager(nextLetterManager);
            setOutgoingManager(nextOutgoingManager);
            setCourierSelectManager(nextCourierSelectManager);
            setCustomEntityManager(nextCustomEntityManager);
            setUpdateChecker(nextUpdateChecker);

            reschedulePendingOnlineMail();
        } catch (RuntimeException exception) {
            // A failed replacement must not leave partially-built state or tasks
            // behind. The old manager objects remain valid because they were
            // captured before the transition and are restored as a complete set.
            if (nextUpdateChecker != null) {
                nextUpdateChecker.shutdown();
            }
            stopRuntime(false);

            setConfigManager(previousConfigManager);
            setMessageManager(previousMessageManager);
            setLetterManager(previousLetterManager);
            setOutgoingManager(previousOutgoingManager);
            setCourierSelectManager(previousCourierSelectManager);
            setCustomEntityManager(previousCustomEntityManager);
            setUpdateChecker(previousUpdateChecker);

            if (hadRuntimeState) {
                if (previousUpdateChecker != null) {
                    previousUpdateChecker.restart();
                }
                // MainConfig may now point at a partially-reloaded configuration;
                // use the delay captured from the restored runtime instead.
                reschedulePendingOnlineMail(previousReceiveDelay);
            }

            throw exception;
        }
    }

    /**
     * Stops tasks and active entities owned by this plugin, optionally persisting
     * the current outgoing mail before the state is replaced or disabled.
     */
    public void stopRuntime(boolean persistOutgoing) {
        if (getUpdateChecker() != null) {
            getUpdateChecker().shutdown();
        }

        Bukkit.getScheduler().cancelTasks(this);
        removeActiveCouriers();

        if (persistOutgoing && getOutgoingManager() != null) {
            getOutgoingManager().saveAll();
        }
    }

    private void removeActiveCouriers() {
        var activeCouriers = CourierManager.getActiveCouriers();
        for (Entity entity : new ArrayList<>(activeCouriers.keySet())) {
            if (entity == null) {
                continue;
            }

            try {
                entity.remove();
            } catch (RuntimeException exception) {
                getLogger().log(java.util.logging.Level.WARNING,
                        "Failed to remove an active courier during lifecycle transition.", exception);
            }
        }
        activeCouriers.clear();
    }

    private void reschedulePendingOnlineMail() {
        reschedulePendingOnlineMail(MainConfig.getReceiveDelay());
    }

    private void reschedulePendingOnlineMail(int receiveDelay) {
        if (getOutgoingManager() == null) {
            return;
        }

        long safeReceiveDelay = Math.max(0L, receiveDelay);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!getOutgoingManager().hasPendingLetters(player)) {
                continue;
            }

            Bukkit.getScheduler().runTaskLater(this, () -> {
                // A reload/disable cancels plugin tasks, and this identity check
                // also protects a task that was already being dispatched.
                if (CourierPrime.getPlugin() != this || !isEnabled() || !player.isOnline()) {
                    return;
                }
                new Courier(player);
            }, safeReceiveDelay);
        }
    }

    /**
     * Stop all runtime work and persist mail before nullifying the plugin instance.
     */
    public void onDisable() {
        stopRuntime(true);

        // Prevent a second disable callback from attempting to persist the same
        // manager again, while keeping the transition safe if Bukkit calls this
        // hook more than once.
        setUpdateChecker(null);
        setOutgoingManager(null);
        setCourierSelectManager(null);
        setCustomEntityManager(null);
        setLetterManager(null);
        setMessageManager(null);
        setConfigManager(null);
        runtimeRegistrationsCreated = false;

        plugin = null;
    }
}
