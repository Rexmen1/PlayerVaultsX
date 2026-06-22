/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
 */

package com.drtshock.playervaults.listeners;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.VaultMenuHolder;
import com.drtshock.playervaults.vaultmanagement.VaultMenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.OptionalInt;
import java.util.UUID;

public class VaultMenuChatListener implements Listener {

    private final PlayerVaults plugin;

    public VaultMenuChatListener(PlayerVaults plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        OptionalInt vaultNumber = this.plugin.getVaultNameEdit(player.getUniqueId());
        if (vaultNumber.isEmpty()) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage().trim();
        UUID uuid = player.getUniqueId();

        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            if (message.equalsIgnoreCase("cancel")) {
                this.plugin.cancelVaultNameEdit(uuid);
                this.plugin.getTL().vaultMenuNameCancelled().title().send(player);
                return;
            }

            int number = vaultNumber.getAsInt();
            if (message.equalsIgnoreCase("reset")) {
                VaultMenuManager.setDisplayName(uuid.toString(), number, null);
            } else {
                VaultMenuManager.setDisplayName(uuid.toString(), number, message);
            }
            this.plugin.cancelVaultNameEdit(uuid);

            if (player.getOpenInventory().getTopInventory().getHolder() instanceof VaultMenuHolder) {
                VaultMenuManager.populateMenu(player, player.getOpenInventory().getTopInventory());
            }

            this.plugin.getTL().vaultMenuNameSet().title().with("vault", String.valueOf(number)).send(player);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.plugin.cancelVaultNameEdit(event.getPlayer().getUniqueId());
        PlayerVaults.getInstance().removeInVaultMenu(event.getPlayer().getUniqueId());
    }
}
