package net.crashcraft.whipclaim.menus.player;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.PermissionGroup;
import net.crashcraft.whipclaim.claimobjects.PermissionSet;
import net.crashcraft.whipclaim.claimobjects.PlayerPermissionSet;
import net.crashcraft.whipclaim.menus.ClaimMenu;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class AdminPermissionMenu extends GUI {
    private UUID target;
    private PlayerPermissionSet permissionSet;
    private PermissionGroup group;

    public AdminPermissionMenu(Player player, PermissionGroup group, UUID target) {
        super(player, "Admin Permissions", 54);
        this.target = target;
        this.group = group;
        this.permissionSet = group.getPlayerPermissionSet(target);
        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        inv.clear();

        inv.setItem(11, createGuiItem(ChatColor.GOLD + "Modify Permissions", Material.CRAFTING_TABLE));
        inv.setItem(12, createGuiItem(ChatColor.GOLD + "Modify Claim", Material.OAK_FENCE_GATE));
        inv.setItem(13, createGuiItem(ChatColor.GOLD + "View Sub Claims", Material.SEA_LANTERN));

        switch (PermissionRoute.MODIFY_PERMISSIONS.getPerm(permissionSet)){
            case 1:
                inv.setItem(20, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 2:
                inv.setItem(29, createGuiItem(ChatColor.GRAY + "Neutral", Material.GRAY_CONCRETE));
                break;
            case 0:
                inv.setItem(38, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        switch (PermissionRoute.MODIFY_CLAIM.getPerm(permissionSet)){
            case 1:
                inv.setItem(21, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 2:
                inv.setItem(30, createGuiItem(ChatColor.GRAY + "Neutral", Material.GRAY_CONCRETE));
                break;
            case 0:
                inv.setItem(39, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        switch (PermissionRoute.VIEW_SUB_CLAIMS.getPerm(permissionSet)){
            case 1:
                inv.setItem(22, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 2:
                inv.setItem(31, createGuiItem(ChatColor.GRAY + "Neutral", Material.GRAY_CONCRETE));
                break;
            case 0:
                inv.setItem(40, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        for (int start = 20; start < 23; start++){
            ItemStack itemStack = inv.getItem(start);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(start, createGuiItem(ChatColor.DARK_GREEN + "Enable", Material.GREEN_STAINED_GLASS));
            }
        }

        for (int start = 29; start < 32; start++){
            ItemStack itemStack = inv.getItem(start);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(start, createGuiItem(ChatColor.DARK_GRAY + "Neutral", Material.GRAY_STAINED_GLASS));
            }
        }

        for (int start = 38; start < 41; start++){
            ItemStack itemStack = inv.getItem(start);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(start, createGuiItem(ChatColor.DARK_RED + "Disable", Material.RED_STAINED_GLASS));
            }
        }

        inv.setItem(16, createPlayerHead(target, new ArrayList<>(Arrays.asList(ChatColor.GREEN + "You are currently editing",
                ChatColor.GREEN + "this players permissions."))));

        inv.setItem(25, createGuiItem(ChatColor.GREEN + "General Permissions", Material.CRAFTING_TABLE));
        inv.setItem(34, createGuiItem(ChatColor.GREEN + "Container Permissions", Material.CHEST));
        inv.setItem(43, createGuiItem(ChatColor.GRAY + "Admin Permissions", Material.GRAY_STAINED_GLASS_PANE));

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        int slot = event.getSlot();
        if (slot >= 19 && slot <= 23){
            clickPermOption(getRoute(slot - 19), PermState.ENABLED);
            return;
        } else if (slot >= 28 && slot <= 32){
            clickPermOption(getRoute(slot - 28), PermState.NEUTRAL);
            return;
        } else if (slot >= 37 && slot <= 41){
            clickPermOption(getRoute(slot - 37), PermState.DISABLE);
            return;
        }

        switch (rawItemName){
            case "general permissions":
                new PlayerPermissionMenu(getPlayer(), group, target).open();
                break;
            case "container permissions":
                new PlayerContainerPermissionMenu(getPlayer(), group, target).open();
                break;
            case "back":
                new PlayerPermListMenu(group.getOwner(), getPlayer(), new ClaimMenu(getPlayer(), group.getOwner()));
                break;
        }
    }

    private PermissionRoute getRoute(int slot){
        switch (slot){
            case 1:
                return PermissionRoute.MODIFY_PERMISSIONS;
            case 2:
                return PermissionRoute.MODIFY_CLAIM;
            case 3:
                return PermissionRoute.VIEW_SUB_CLAIMS;
        }
        return null;
    }

    private void clickPermOption(PermissionRoute route, int value) {
        group.setPlayerPermission(target, route, value);
        loadItems();
    }
}