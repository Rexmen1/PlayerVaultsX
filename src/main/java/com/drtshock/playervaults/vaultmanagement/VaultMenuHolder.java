/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
 */

package com.drtshock.playervaults.vaultmanagement;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class VaultMenuHolder implements InventoryHolder {

    private Inventory inventory;
    private final UUID owner;

    public VaultMenuHolder(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return this.owner;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
