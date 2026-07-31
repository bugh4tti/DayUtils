package com.dayutils.plugin.utils;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class NightVisionApplier {

    private NightVisionApplier() {
    }

    public static void apply(Player player) {
        PotionEffect effect = new PotionEffect(
                PotionEffectType.NIGHT_VISION,
                Integer.MAX_VALUE,
                0,
                true,
                false,
                false
        );
        player.addPotionEffect(effect);
    }

    public static void remove(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }
}
