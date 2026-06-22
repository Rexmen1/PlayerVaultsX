/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
 */

package com.drtshock.playervaults.vaultmanagement;

import org.bukkit.inventory.ItemStack;

public class VaultCustomization {

    private String displayName;
    private String iconData;

    public VaultCustomization() {
    }

    public VaultCustomization(String displayName, String iconData) {
        this.displayName = displayName;
        this.iconData = iconData;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIconData() {
        return this.iconData;
    }

    public void setIconData(String iconData) {
        this.iconData = iconData;
    }

    public boolean hasIcon() {
        return this.iconData != null && !this.iconData.isEmpty();
    }

    public boolean hasDisplayName() {
        return this.displayName != null && !this.displayName.isEmpty();
    }
}
