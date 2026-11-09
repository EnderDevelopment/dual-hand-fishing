package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {
    private final Map<UUID, FishHook> offHandHooks = new HashMap<>();
    private final Random random = new Random();
    private NamespacedKey offHandKey;

    @Override
    public void onEnable() {
        offHandKey = new NamespacedKey(this, "off_hand_fishing");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        offHandHooks.values().forEach(Entity::remove);
        offHandHooks.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOffHandUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND || !event.getAction().isRightClick()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack rod = event.getItem();
        if (rod == null || rod.getType() != Material.FISHING_ROD || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        FishHook existing = offHandHooks.get(player.getUniqueId());
        if (existing != null && existing.isValid()) {
            reelIn(player, existing, rod);
            return;
        }
        launchHook(player, rod);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeHook(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        removeHook(event.getPlayer());
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        removeHook(event.getPlayer());
    }

    @EventHandler
    public void onHookDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof FishHook hook) || !isOurHook(hook)) {
            return;
        }
        event.setCancelled(true);
    }

    private void launchHook(Player player, ItemStack rod) {
        Location origin = player.getEyeLocation().clone();
        Vector direction = origin.getDirection().normalize();
        Location spawn = origin.add(direction.multiply(0.35));
        FishHook hook = player.getWorld().spawn(spawn, FishHook.class, spawned -> {
            spawned.setShooter(player);
            spawned.setVelocity(direction.multiply(1.5));
            spawned.getPersistentDataContainer().set(offHandKey, PersistentDataType.BYTE, (byte) 1);
        });
        offHandHooks.put(player.getUniqueId(), hook);
    }

    private void reelIn(Player player, FishHook hook, ItemStack rod) {
        int state = hook.getState().ordinal();
        if (state == 2) {
            catchLoot(player, hook);
        }
        hook.remove();
        offHandHooks.remove(player.getUniqueId());
        damageRod(player, rod);
    }

    private void catchLoot(Player player, FishHook hook) {
        World world = player.getWorld();
        LootTable table = Bukkit.getLootTable(NamespacedKey.minecraft("gameplay/fishing"));
        if (table == null) {
            return;
        }
        LootContext context = new LootContext.Builder(hook.getLocation())
                .killer(player)
                .lootedEntity(player)
                .build();
        List<ItemStack> loot = table.populateLoot(random, context);
        for (ItemStack item : loot) {
            Item dropped = world.dropItem(hook.getLocation(), item);
            Vector pull = player.getEyeLocation().toVector().subtract(dropped.getLocation().toVector()).normalize().multiply(0.25);
            dropped.setVelocity(pull);
        }
        player.sendActionBar(Component.text("You caught something with your off hand."));
    }

    private void damageRod(Player player, ItemStack rod) {
        ItemMeta meta = rod.getItemMeta();
        if (meta == null) {
            return;
        }
        int damage = meta.getDamage() + 1;
        if (damage >= rod.getType().getMaxDurability()) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            player.playSound(player.getLocation(), "entity.item.break", 1.0f, 1.0f);
        } else {
            meta.setDamage(damage);
            rod.setItemMeta(meta);
        }
    }

    private boolean isOurHook(FishHook hook) {
        return hook.getPersistentDataContainer().has(offHandKey, PersistentDataType.BYTE);
    }

    private void removeHook(Player player) {
        FishHook hook = offHandHooks.remove(player.getUniqueId());
        if (hook != null) {
            hook.remove();
        }
    }
}
