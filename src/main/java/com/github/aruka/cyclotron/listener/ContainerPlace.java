package com.github.aruka.cyclotron.listener;

import com.github.aruka.cyclotron.Cyclotron;
import com.github.aruka.cyclotron.util.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ContainerPlace implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onContainerPlace(BlockPlaceEvent event) {
        if (event.isCancelled() || !event.canBuild()) return;
        Block block = event.getBlockPlaced();
        if (!block.getType().equals(Material.CHEST)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                Util.doubleChest(block, false);
            }
        }.runTaskLater(Cyclotron.INSTANCE, 1);
    }
}
