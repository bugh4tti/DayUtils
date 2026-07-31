package com.dayutils.plugin;

import com.dayutils.plugin.commands.DayUtilsCommand;
import com.dayutils.plugin.listeners.ChatLockListener;
import com.dayutils.plugin.listeners.NightVisionListener;
import com.dayutils.plugin.utils.ChatLockManager;
import com.dayutils.plugin.utils.NightVisionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class DayUtils extends JavaPlugin {

    private static DayUtils instance;

    private ChatLockManager chatLockManager;
    private NightVisionManager nightVisionManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.chatLockManager = new ChatLockManager();
        this.nightVisionManager = new NightVisionManager();

        DayUtilsCommand command = new DayUtilsCommand(this);
        getCommand("dayutils").setExecutor(command);
        getCommand("dayutils").setTabCompleter(command);

        Bukkit.getPluginManager().registerEvents(new ChatLockListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NightVisionListener(this), this);

        long interval = getConfig().getLong("nightvision.check-interval-ticks", 100L);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (var player : Bukkit.getOnlinePlayers()) {
                if (nightVisionManager.isEnabled(player.getUniqueId())) {
                    com.dayutils.plugin.utils.NightVisionApplier.apply(player);
                }
            }
        }, interval, interval);

        getLogger().info("DayUtils habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        getLogger().info("DayUtils deshabilitado.");
    }

    public static DayUtils getInstance() {
        return instance;
    }

    public ChatLockManager getChatLockManager() {
        return chatLockManager;
    }

    public NightVisionManager getNightVisionManager() {
        return nightVisionManager;
    }
    }
