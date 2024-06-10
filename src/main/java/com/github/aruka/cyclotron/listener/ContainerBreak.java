package com.github.aruka.cyclotron.listener;

import com.github.aruka.cyclotron.Cyclotron;
import com.github.aruka.cyclotron.util.Util;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ContainerBreak implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!Cyclotron.CARGO_CONTAINER.contains(block.getType())) return;
        if (!block.hasMetadata(Cyclotron.CyclotronKey)) return;
        Util.blockDataClear(block);
    }
}
