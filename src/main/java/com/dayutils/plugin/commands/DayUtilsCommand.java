package com.dayutils.plugin.commands;

import com.dayutils.plugin.DayUtils;
import com.dayutils.plugin.utils.ColorUtils;
import com.dayutils.plugin.utils.NightVisionApplier;
import com.dayutils.plugin.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class DayUtilsCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of(
            "nightvision", "invsee", "lockchat", "unlockchat", "heal", "feed", "repair",
            "reload", "help", "enderchest", "ec", "clearinv", "sudo", "broadcast", "title",
            "seen", "ping"
    );

    private final DayUtils plugin;

    public DayUtilsCommand(DayUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "nightvision" -> handleNightVision(sender, args);
            case "invsee" -> handleInvsee(sender, args);
            case "lockchat" -> handleLockChat(sender, true);
            case "unlockchat" -> handleLockChat(sender, false);
            case "heal" -> handleHeal(sender, args);
            case "feed" -> handleFeed(sender, args);
            case "repair" -> handleRepair(sender, args);
            case "reload" -> handleReload(sender);
            case "help" -> sendHelp(sender);
            case "enderchest", "ec" -> handleEnderchest(sender, args);
            case "clearinv" -> handleClearInv(sender, args);
            case "sudo" -> handleSudo(sender, args);
            case "broadcast" -> handleBroadcast(sender, args);
            case "title" -> handleTitle(sender, args);
            case "seen" -> handleSeen(sender, args);
            case "ping" -> handlePing(sender, args);
            default -> sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.unknown-subcommand")));
        }

        return true;
    }

    // ================== NIGHTVISION ==================

    private void handleNightVision(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.only-players")));
            return;
        }
        if (!player.hasPermission("dayutils.command.nightvision")) {
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

    // ================== INVSEE ==================

    private void handleInvsee(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.only-players")));
            return;
        }
        if (!player.hasPermission("dayutils.command.invsee")) {
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

    // ================== LOCKCHAT / UNLOCKCHAT ==================

    private void handleLockChat(CommandSender sender, boolean lock) {
        if (!sender.hasPermission("dayutils.command.lockchat")) {
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

    // ================== HEAL ==================

    private void handleHeal(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.command.heal")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        Player target = resolveTarget(sender, args);
        if (target == null) return;

        double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        target.setHealth(maxHealth);
        target.setFireTicks(0);

        boolean self = sender instanceof Player p && p.getUniqueId().equals(target.getUniqueId());
        if (self) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.healed-self")));
        } else {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.healed-other").replace("{player}", target.getName())));
            target.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.healed-self")));
        }
    }

    // ================== FEED ==================

    private void handleFeed(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.command.feed")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        Player target = resolveTarget(sender, args);
        if (target == null) return;

        target.setFoodLevel(20);
        target.setSaturation(20f);

        boolean self = sender instanceof Player p && p.getUniqueId().equals(target.getUniqueId());
        if (self) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.fed-self")));
        } else {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.fed-other").replace("{player}", target.getName())));
            target.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.fed-self")));
        }
    }

    // ================== REPAIR ==================

    private void handleRepair(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.command.repair")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        Player target = resolveTarget(sender, args);
        if (target == null) return;

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.no-item-in-hand")));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(0);
            item.setItemMeta(meta);
        }

        boolean self = sender instanceof Player p && p.getUniqueId().equals(target.getUniqueId());
        if (self) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.repaired-self")));
        } else {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.repaired-other").replace("{player}", target.getName())));
            target.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.repaired-self")));
        }
    }

    // ================== RELOAD ==================

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("dayutils.command.reload")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        plugin.reloadConfig();
        sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("reload.success")));
    }

    // ================== HELP ==================

    private void sendHelp(CommandSender sender) {
        List<String> lines = List.of(
                "&#00DAFF&lDayUtils &7» &fComandos disponibles:",
                "&#00DAFF/du nightvision <on/off>",
                "&#00DAFF/du invsee <jugador>",
                "&#00DAFF/du lockchat &7| &#00DAFF/du unlockchat",
                "&#00DAFF/du heal [jugador]",
                "&#00DAFF/du feed [jugador]",
                "&#00DAFF/du repair [jugador]",
                "&#00DAFF/du enderchest (ec) [jugador]",
                "&#00DAFF/du clearinv [jugador]",
                "&#00DAFF/du sudo <jugador> <comando>",
                "&#00DAFF/du broadcast <mensaje>",
                "&#00DAFF/du title <mensaje>",
                "&#00DAFF/du seen <jugador>",
                "&#00DAFF/du ping <jugador>",
                "&#00DAFF/du reload"
        );
        for (String line : lines) {
            sender.sendMessage(ColorUtils.translate(line));
        }
    }

    // ================== ENDERCHEST ==================

    private void handleEnderchest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.only-players")));
            return;
        }
        if (!player.hasPermission("dayutils.command.enderchest")) {
            player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        if (args.length < 2) {
            player.openInventory(player.getEnderChest());
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.player-not-found")));
            return;
        }

        player.openInventory(target.getEnderChest());
    }

    // ================== CLEARINV ==================

    private void handleClearInv(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.command.clearinv")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        Player target = resolveTarget(sender, args);
        if (target == null) return;

        target.getInventory().clear();

        boolean self = sender instanceof Player p && p.getUniqueId().equals(target.getUniqueId());
        if (self) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.cleared-inv-self")));
        } else {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.cleared-inv-other").replace("{player}", target.getName())));
            target.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.cleared-inv-target")));
        }
    }

    // ================== SUDO ==================

    private void handleSudo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.command.sudo")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.usage-sudo")));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.player-not-found")));
            return;
        }

        String cmd = String.join(" ", List.of(args).subList(2, args.length));
        target.performCommand(cmd);

        sender.sendMessage(ColorUtils.translate(
                plugin.getConfig().getString("sudo.executed")
                        .replace("{player}", target.getName())
                        .replace("{command}", cmd)
        ));
    }

    // ================== BROADCAST ==================

    private void handleBroadcast(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.command.broadcast")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.usage-broadcast")));
            return;
        }

        String message = String.join(" ", List.of(args).subList(1, args.length));
        String formatted = ColorUtils.translate(
                plugin.getConfig().getString("broadcast.format").replace("{message}", message)
        );

        Bukkit.broadcastMessage(formatted);
    }

    // ================== TITLE ==================

    private void handleTitle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.command.title")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.usage-title")));
            return;
        }

        String raw = String.join(" ", List.of(args).subList(1, args.length));
        String message = ColorUtils.translate(raw);

        int fadeIn = plugin.getConfig().getInt("title.fade-in", 10);
        int stay = plugin.getConfig().getInt("title.stay", 70);
        int fadeOut = plugin.getConfig().getInt("title.fade-out", 10);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendTitle(message, "", fadeIn, stay, fadeOut);
        }
    }

    // ================== SEEN ==================

    private void handleSeen(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.command.seen")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.usage-seen")));
            return;
        }

        Player online = Bukkit.getPlayerExact(args[1]);
        if (online != null) {
            sender.sendMessage(ColorUtils.translate(
                    plugin.getConfig().getString("seen.online").replace("{player}", online.getName())
            ));
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);

        if (!offline.hasPlayedBefore()) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("seen.never")));
            return;
        }

        Long lastQuit = plugin.getSeenManager().getLastQuit(offline.getUniqueId());
        long lastMillis = lastQuit != null ? lastQuit : offline.getLastPlayed();
        String time = TimeUtils.formatSince(lastMillis);

        sender.sendMessage(ColorUtils.translate(
                plugin.getConfig().getString("seen.offline")
                        .replace("{player}", offline.getName())
                        .replace("{time}", time)
        ));
    }

    // ================== PING ==================

    private void handlePing(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.command.ping")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.player-not-found")));
                return;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.usage-ping")));
            return;
        }

        sender.sendMessage(ColorUtils.translate(
                plugin.getConfig().getString("ping.format")
                        .replace("{player}", target.getName())
                        .replace("{ping}", String.valueOf(target.getPing()))
        ));
    }

    // ================== UTIL ==================

    private Player resolveTarget(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.player-not-found")));
                return null;
            }
            return target;
        }
        if (sender instanceof Player player) {
            return player;
        }
        sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.only-players")));
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            for (String option : SUBCOMMANDS) {
                if (option.startsWith(args[0].toLowerCase())) {
                    result.add(option);
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("nightvision")) {
                for (String option : List.of("on", "off")) {
                    if (option.startsWith(args[1].toLowerCase())) result.add(option);
                }
            } else if (List.of("invsee", "heal", "feed", "repair", "enderchest", "ec",
                    "clearinv", "sudo", "seen", "ping").contains(sub)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) result.add(p.getName());
                }
            }
        }

        return result;
    }
                                                    }
