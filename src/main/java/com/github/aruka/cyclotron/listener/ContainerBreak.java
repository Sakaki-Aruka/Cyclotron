package com.github.aruka.cyclotron.listener;

import com.github.aruka.cyclotron.Cyclotron;
import com.github.aruka.cyclotron.util.Util;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ContainerBreak implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (
                !Cyclotron.CARGO_CONTAINER.contains(block.getType()) ||
                !block.hasMetadata(Cyclotron.CyclotronKey) ||
                event.isCancelled()
        ) return;
        Util.blockDataClear(block);
    }
}
