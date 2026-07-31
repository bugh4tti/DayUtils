package com.dayutils.plugin.commands;

import com.dayutils.plugin.DayUtils;
import com.dayutils.plugin.utils.ColorUtils;
import com.dayutils.plugin.utils.NightVisionApplier;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class DayUtilsCommand implements CommandExecutor, TabCompleter {

    private final DayUtils plugin;

    public DayUtilsCommand(DayUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtils.translate("&#00DAFF&lDayUtils &7» &fUsa /du <nightvision|invsee|lockchat|unlockchat>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "nightvision" -> handleNightVision(sender, args);
            case "invsee" -> handleInvsee(sender, args);
            case "lockchat" -> handleLockChat(sender, true);
            case "unlockchat" -> handleLockChat(sender, false);
            default -> sender.sendMessage(ColorUtils.translate("&#FF5555Subcomando desconocido. Usa /du <nightvision|invsee|lockchat|unlockchat>"));
        }

        return true;
    }

    private void handleNightVision(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.only-players")));
            return;
        }
        if (!player.hasPermission("dayutils.nightvision")) {
            player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }
        if (args.length < 2 || (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {
            player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.usage-nightvision")));
            return;
        }

        boolean on = args[1].equalsIgnoreCase("on");

        if (on) {
            plugin.getNightVisionManager().enable(player.getUniqueId());
            NightVisionApplier.apply(player);
            player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("nightvision.activated")));
        } else {
            plugin.getNightVisionManager().disable(player.getUniqueId());
            NightVisionApplier.remove(player);
            player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("nightvision.deactivated")));
        }
    }

    private void handleInvsee(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.only-players")));
            return;
        }
        if (!player.hasPermission("dayutils.invsee")) {
            player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.usage-invsee")));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.player-not-found")));
            return;
        }

        player.openInventory(target.getInventory());
    }

    private void handleLockChat(CommandSender sender, boolean lock) {
        if (!sender.hasPermission("dayutils.lockchat")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        plugin.getChatLockManager().setLocked(lock);

        String senderName = sender instanceof Player p ? p.getName() : "Consola";

        String title = ColorUtils.translate(plugin.getConfig().getString("lockchat.title"));
        String subtitleKey = lock ? "lockchat.subtitle-lock" : "lockchat.subtitle-unlock";
        String subtitle = ColorUtils.translate(
                plugin.getConfig().getString(subtitleKey).replace("{player}", senderName)
        );

        int fadeIn = plugin.getConfig().getInt("lockchat.title-fade-in", 10);
        int stay = plugin.getConfig().getInt("lockchat.title-stay", 60);
        int fadeOut = plugin.getConfig().getInt("lockchat.title-fade-out", 10);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }

        sender.sendMessage(ColorUtils.translate(
                "&#00DAFFChat " + (lock ? "bloqueado" : "desbloqueado") + " correctamente."
        ));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            for (String option : List.of("nightvision", "invsee", "lockchat", "unlockchat")) {
                if (option.startsWith(args[0].toLowerCase())) {
                    result.add(option);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("nightvision")) {
                for (String option : List.of("on", "off")) {
                    if (option.startsWith(args[1].toLowerCase())) {
                        result.add(option);
                    }
                }
            } else if (args[0].equalsIgnoreCase("invsee")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        result.add(p.getName());
                    }
                }
            }
        }

        return result;
    }
                                          }
