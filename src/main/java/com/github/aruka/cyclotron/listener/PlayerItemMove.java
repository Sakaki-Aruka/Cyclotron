package com.github.aruka.cyclotron.listener;

import com.github.aruka.cyclotron.Cyclotron;
import com.github.aruka.cyclotron.util.Util;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class PlayerItemMove implements Listener {
    private static final Set<ClickType> ALLOWED_CLICK_TYPE = Set.of(
            ClickType.RIGHT,
            ClickType.SHIFT_RIGHT,
            ClickType.LEFT,
            ClickType.SHIFT_LEFT
    );

    private static final Set<InventoryAction> ALLOWED_PLACE_ACTION = Set.of(
            InventoryAction.PLACE_ONE,
            InventoryAction.PLACE_ALL,
            InventoryAction.PLACE_SOME
    );

    @EventHandler
    public void onPlayerItemMove(InventoryClickEvent event) {
        if (!ALLOWED_CLICK_TYPE.contains(event.getClick())) return;
        if (event.isShiftClick()) withShift(event);
        else pure(event);
    }

    private void withShift(InventoryClickEvent event) {
        if (!event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || event.isCancelled()) return;
        Inventory inventory = event.getInventory();
        ItemStack item = event.getCurrentItem();
        if (Util.isNullOrAir(item) || event.getClickedInventory() == null) return;
        Block block = inventory.getLocation() != null
                ? inventory.getLocation().getBlock()
                : null;
        if (!Util.isCyclotronCargo(block)) return;
        runDelay(block, inventory);
    }

    private void pure(InventoryClickEvent event) {
        if (!ALLOWED_PLACE_ACTION.contains(event.getAction())) return;
        Inventory inventory = event.getClickedInventory();
        ItemStack item = event.getCursor();
        if (Util.isNullOrAir(item) || inventory == null || inventory.getHolder() instanceof Player) return;
        Block block = inventory.getLocation() != null
                ? inventory.getLocation().getBlock()
                : null;
        if (!Util.isCyclotronCargo(block)) return;
        runDelay(block, inventory);
    }

    private void runDelay(Block block, Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                HopperTransfer.loop(block, inventory);
            }
        }.runTaskLater(Cyclotron.INSTANCE, 1);
    }
}
