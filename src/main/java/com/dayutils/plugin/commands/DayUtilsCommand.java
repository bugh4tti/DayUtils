package com.dayutils.plugin.commands;

import com.dayutils.plugin.DayUtils;
import com.dayutils.plugin.utils.ColorUtils;
import com.dayutils.plugin.utils.NightVisionApplier;
import org.bukkit.Bukkit;
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

    private final DayUtils plugin;

    public DayUtilsCommand(DayUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtils.translate("&#00DAFF&lDayUtils &7» &fUsa /du <nightvision|invsee|lockchat|unlockchat|heal|feed|repair>"));
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
            default -> sender.sendMessage(ColorUtils.translate("&#FF5555Subcomando desconocido. Usa /du <nightvision|invsee|lockchat|unlockchat|heal|feed|repair>"));
        }

        return true;
    }

    // ================== NIGHTVISION ==================

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

    // ================== INVSEE ==================

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

    // ================== LOCKCHAT / UNLOCKCHAT ==================

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

    // ================== HEAL ==================

    private void handleHeal(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.heal")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        Player target = resolveTarget(sender, args);
        if (target == null) {
            return;
        }

        double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        target.setHealth(maxHealth);
        target.setFireTicks(0);

        boolean self = sender instanceof Player p && p.getUniqueId().equals(target.getUniqueId());

        if (self) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.healed-self")));
        } else {
            sender.sendMessage(ColorUtils.translate(
                    plugin.getConfig().getString("actions.healed-other").replace("{player}", target.getName())
            ));
            target.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.healed-self")));
        }
    }

    // ================== FEED ==================

    private void handleFeed(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.feed")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        Player target = resolveTarget(sender, args);
        if (target == null) {
            return;
        }

        target.setFoodLevel(20);
        target.setSaturation(20f);

        boolean self = sender instanceof Player p && p.getUniqueId().equals(target.getUniqueId());

        if (self) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.fed-self")));
        } else {
            sender.sendMessage(ColorUtils.translate(
                    plugin.getConfig().getString("actions.fed-other").replace("{player}", target.getName())
            ));
            target.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.fed-self")));
        }
    }

    // ================== REPAIR ==================

    private void handleRepair(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dayutils.repair")) {
            sender.sendMessage(ColorUtils.translate(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        Player target = resolveTarget(sender, args);
        if (target == null) {
            return;
        }

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
            sender.sendMessage(ColorUtils.translate(
                    plugin.getConfig().getString("actions.repaired-other").replace("{player}", target.getName())
            ));
            target.sendMessage(ColorUtils.translate(plugin.getConfig().getString("actions.repaired-self")));
        }
    }

    // ================== UTIL ==================

    /**
     * Si se pasa un segundo argumento, busca ese jugador como objetivo.
     * Si no, y el sender es un jugador, se usa a sí mismo.
     */
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
            for (String option : List.of("nightvision", "invsee", "lockchat", "unlockchat", "heal", "feed", "repair")) {
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
            } else if (List.of("invsee", "heal", "feed", "repair").contains(args[0].toLowerCase())) {
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
