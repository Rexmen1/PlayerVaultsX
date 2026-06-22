/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler, Laxwashere, CmdrKittens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.drtshock.playervaults.config.file;

import com.drtshock.playervaults.config.annotation.Comment;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "InnerClassMayBeStatic", "unused"})
public class Config {
    public class Block {
        private boolean enabled = true;
        @Comment("""
                Material list for blocked items (does not support ID's), only effective if the feature is enabled.
                 If you don't know material names: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
                
                Also, if you add "BLOCK_ALL_WITH_CUSTOM_MODEL_DATA" or "BLOCK_ALL_WITHOUT_CUSTOM_MODEL_DATA"
                 then either all items with custom model data will be blocked, or all items without custom model data will be blocked.""")
        private List<String> list = new ArrayList<>() {
            {
                this.add("PUMPKIN");
            }
        };

        @Comment("Enchantments to block from entering a vault at all.")
        private List<String> enchantmentsBlocked = new ArrayList<>();

        public boolean isEnabled() {
            return this.enabled;
        }

        public List<String> getList() {
            if (this.list == null) {
                this.list = new ArrayList<>();
            }
            return Collections.unmodifiableList(this.list);
        }

        public List<String> getEnchantmentsBlocked() {
            if (this.enchantmentsBlocked == null) {
                this.enchantmentsBlocked = new ArrayList<>();
            }
            return Collections.unmodifiableList(this.enchantmentsBlocked);
        }
    }

    public class Economy {
        @Comment("Set me to true to enable economy features!")
        private boolean enabled = false;
        private double feeToCreate = 100;
        private double feeToOpen = 10;
        private double refundOnDelete = 50;

        public boolean isEnabled() {
            return this.enabled;
        }

        public double getFeeToCreate() {
            return this.feeToCreate;
        }

        public double getFeeToOpen() {
            return this.feeToOpen;
        }

        public double getRefundOnDelete() {
            return this.refundOnDelete;
        }
    }

    public class PurgePlanet {
        private boolean enabled = false;
        @Comment("Time, in days, since last edit")
        private int daysSinceLastEdit = 30;

        public boolean isEnabled() {
            return this.enabled;
        }

        public int getDaysSinceLastEdit() {
            return this.daysSinceLastEdit;
        }
    }

    public class Storage {
        public class FlatFile {
            @Comment("""
                    Backups
                     Enabling this will create backups of vaults automagically.""")
            private boolean backups = true;

            public boolean isBackups() {
                return this.backups;
            }
        }

        private FlatFile flatFile = new FlatFile();
        private String storageType = "flatfile";

        public FlatFile getFlatFile() {
            return this.flatFile;
        }

        public String getStorageType() {
            return this.storageType;
        }
    }

    @Comment("""
            PlayerVaults
            Created by: https://github.com/drtshock/PlayerVaults/graphs/contributors/
            Resource page: https://www.spigotmc.org/resources/51204/
            Discord server: https://discordapp.com/invite/JZcWDEt/
            Made with love <3""")
    private boolean aPleasantHello = true;

    @Comment("""
            Debug Mode
             This will print everything the plugin is doing to console.
             You should only enable this if you're working with a contributor to fix something.""")
    private boolean debug = false;

    @Comment("""
            Can be 1 through 6.
            Default: 6""")
    private int defaultVaultRows = 6;

    @Comment("""
            Signs
             This will determine whether vault signs are enabled.
             If you don't know what this is or if it's for you, see the resource page.""")
    private boolean signs = false;

    @Comment("""
            Economy
             These are all of the settings for the economy integration. (Requires Vault)
              Bypass permission is: playervaults.free""")
    private Economy economy = new Economy();

    @Comment("""
            Blocked Items
             This will allow you to block specific materials from vaults.
              Bypass permission is: playervaults.bypassblockeditems""")
    private Block itemBlocking = new Block();

    @Comment("""
            Cleanup
             Enabling this will purge vaults that haven't been touched in the specified time frame.
              Reminder: This is only checked during startup.
                        This will not lag your server or touch the backups folder.""")
    private PurgePlanet purge = new PurgePlanet();

    @Comment("Sets the highest vault amount this plugin will test perms for")
    private int maxVaultAmountPermTest = 99;

    @Comment("""
            Bottom Row Navigation
             Uses the bottom row of vault GUIs for quick switching between vault numbers.""")
    private BottomRowNavigation bottomRowNavigation = new BottomRowNavigation();

    @Comment("Storage option. Currently only flatfile, but soon more! :)")
    private Storage storage = new Storage();

    public static class BottomRowNavigation {
        private boolean enabled = true;

        @Comment("Material for the vault you currently have open")
        private String currentMaterial = "LIME_STAINED_GLASS_PANE";

        @Comment("Material for vaults you can switch to")
        private String vaultMaterial = "LIGHT_BLUE_STAINED_GLASS_PANE";

        @Comment("Material for vaults you do not have permission to open")
        private String lockedMaterial = "GRAY_STAINED_GLASS_PANE";

        @Comment("Material for unused navigation slots")
        private String fillerMaterial = "BLACK_STAINED_GLASS_PANE";

        @Comment("Display name for the current vault button. Use <vault> for the vault number.")
        private String currentName = "&aVault &f#<vault> &7(Current)";

        @Comment("Display name for other vault buttons. Use <vault> for the vault number.")
        private String vaultName = "&bVault &f#<vault>";

        @Comment("Display name for locked vault buttons. Use <vault> for the vault number.")
        private String lockedName = "&7Vault #<vault>";

        public boolean isEnabled() {
            return this.enabled;
        }

        public String getCurrentMaterial() {
            return this.currentMaterial;
        }

        public String getVaultMaterial() {
            return this.vaultMaterial;
        }

        public String getLockedMaterial() {
            return this.lockedMaterial;
        }

        public String getFillerMaterial() {
            return this.fillerMaterial;
        }

        public String getCurrentName() {
            return this.currentName;
        }

        public String getVaultName() {
            return this.vaultName;
        }

        public String getLockedName() {
            return this.lockedName;
        }
    }

    public static class VaultMenu {
        private boolean enabled = true;

        @Comment("How many vault slots to show in /pv menu (9-54, rounded to rows)")
        private int slots = 45;

        @Comment("Menu title")
        private String title = "&8Your Vaults";

        @Comment("Default icon material for vaults without a custom icon")
        private String defaultIcon = "CHEST";

        @Comment("Icon material for vaults the player cannot access")
        private String lockedMaterial = "GRAY_STAINED_GLASS_PANE";

        @Comment("Default display name. Use <vault> for the vault number.")
        private String defaultName = "&fVault #<vault>";

        @Comment("Lore lines for each vault icon. Placeholders: <vault>, <status>")
        private List<String> lore = new ArrayList<>() {
            {
                this.add("&7Status: <status>");
                this.add("&eLeft-click &7to open");
                this.add("&eRight-click &7with item to set icon");
                this.add("&eRight-click &7to rename in chat");
                this.add("&eShift-right-click &7to reset icon/name");
            }
        };

        private String statusHasItems = "&aHas items";
        private String statusEmpty = "&7Empty";
        private String lockedLore = "&cNo permission";
        private String editLore = "";

        public boolean isEnabled() {
            return this.enabled;
        }

        public int getSlots() {
            return this.slots;
        }

        public String getTitle() {
            return this.title;
        }

        public String getDefaultIcon() {
            return this.defaultIcon;
        }

        public String getLockedMaterial() {
            return this.lockedMaterial;
        }

        public String getDefaultName() {
            return this.defaultName;
        }

        public List<String> getLore() {
            if (this.lore == null) {
                this.lore = new ArrayList<>();
            }
            return Collections.unmodifiableList(this.lore);
        }

        public String getStatusHasItems() {
            return this.statusHasItems;
        }

        public String getStatusEmpty() {
            return this.statusEmpty;
        }

        public String getLockedLore() {
            return this.lockedLore;
        }

        public String getEditLore() {
            return this.editLore;
        }
    }

    @Comment("""
            Vault Menu
             Running /pv with no number opens a menu of your vaults.""")
    private VaultMenu vaultMenu = new VaultMenu();

    public void setFromConfig(Logger l, FileConfiguration c) {
        l.info("Importing old configuration...");
        l.info("debug = " + (this.debug = c.getBoolean("debug", false)));
        l.info("signs = " + (this.signs = c.getBoolean("signs-enabled", false)));
        l.info("economy enabled = " + (this.economy.enabled = c.getBoolean("economy.enabled", false)));
        l.info(" creation fee = " + (this.economy.feeToCreate = c.getDouble("economy.cost-to-create", 100)));
        l.info(" open fee = " + (this.economy.feeToOpen = c.getDouble("economy.cost-to-open", 10)));
        l.info(" refund = " + (this.economy.refundOnDelete = c.getDouble("economy.refund-on-delete", 50)));
        l.info("item blocking enabled = " + (this.itemBlocking.enabled = c.getBoolean("blockitems", true)));
        l.info("blocked items = " + (this.itemBlocking.list = c.getStringList("blocked-items")));
        if (this.itemBlocking.list == null) {
            this.itemBlocking.list = new ArrayList<>();
            this.itemBlocking.list.add("PUMPKIN");
            this.itemBlocking.list.add("DIAMOND_BLOCK");
            l.info(" set defaults: " + this.itemBlocking.list);
        }
        l.info("cleanup purge enabled = " + (this.purge.enabled = c.getBoolean("cleanup.enable", false)));
        l.info(" days since last edit = " + (this.purge.daysSinceLastEdit = c.getInt("cleanup.lastEdit", 30)));
        l.info("flatfile storage backups = " + (this.storage.flatFile.backups = c.getBoolean("backups.enabled", true)));
        l.info("max vault amount to test via perms = " + (this.maxVaultAmountPermTest = c.getInt("max-vault-amount-perm-to-test", 99)));
    }

    public boolean isDebug() {
        return this.debug;
    }

    public int getDefaultVaultRows() {
        return this.defaultVaultRows;
    }

    public boolean isSigns() {
        return this.signs;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public Block getItemBlocking() {
        return this.itemBlocking;
    }

    public PurgePlanet getPurge() {
        return this.purge;
    }

    public int getMaxVaultAmountPermTest() {
        return this.maxVaultAmountPermTest;
    }

    public Storage getStorage() {
        return this.storage;
    }

    public BottomRowNavigation getBottomRowNavigation() {
        return this.bottomRowNavigation;
    }

    public VaultMenu getVaultMenu() {
        return this.vaultMenu;
    }
}