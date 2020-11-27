package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.menus.ClaimMenu;
import net.crashcraft.crashclaim.menus.list.ClaimListMenu;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class MenuCommand extends BaseCommand {
    private ClaimDataManager manager;
    private VisualizationManager visualizationManager;

    public MenuCommand(ClaimDataManager manager, VisualizationManager visualizationManager){
        this.manager = manager;
        this.visualizationManager = visualizationManager;
    }

    @CommandAlias("claims")
    @CommandPermission("crashclaim.user.claims")
    public void onClaimMenu(Player player){
        new ClaimListMenu(player, null).open();
    }

    @CommandAlias("claimsettings")
    @CommandPermission("crashclaim.user.claimsettings")
    public void onClaimMenuSingle(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            new ClaimMenu(player, claim, null).open();
        } else {
            player.sendMessage(ChatColor.RED + "There is no claim where you are standing.");
        }
    }

    @CommandAlias("unclaim|removeclaim")
    @CommandPermission("crashclaim.user.unclaim")
    public void unClaim(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            new ConfirmationMenu(player,"Confirm Delete Claim",
                    ChatColor.DARK_RED + "Permanently Delete this claim?",
                    new ArrayList<>(Arrays.asList(ChatColor.RED + "Claim Blocks will be restored to ",
                            ChatColor.RED + "the contributing parties")),
                    GlobalConfig.visual_menu_items.get(claim.getWorld()),
                    (p, aBoolean) -> {
                        if (aBoolean) {
                            if (PermissionHelper.getPermissionHelper().hasPermission(claim, p.getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                                CrashClaim.getPlugin().getDataManager().deleteClaim(claim);
                                VisualGroup group = visualizationManager.fetchVisualGroup(player, false);
                                if (group != null){
                                    group.removeAllVisuals();
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to modify this claim.");
                            }
                        }
                        return "";
                    }, p -> "").open();
        } else {
            player.sendMessage(ChatColor.RED + "There is no claim where you are standing.");
        }
    }

    @CommandAlias("unclaimsubclaim|removesubclaim")
    @CommandPermission("crashclaim.user.unclaimsubclaim")
    public void unSubClaim(Player player){
        Location location = player.getLocation();
        SubClaim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID()).getSubClaim(location.getBlockX(), location.getBlockZ());
        if (claim != null){
            new ConfirmationMenu(player,"Confirm Delete Claim",
                    ChatColor.DARK_RED + "Permanently Delete this claim?",
                    new ArrayList<>(Arrays.asList(ChatColor.RED + "Claim Blocks will be restored to ",
                            ChatColor.RED + "the contributing parties")),
                    GlobalConfig.visual_menu_items.get(claim.getWorld()),
                    (p, aBoolean) -> {
                        if (aBoolean) {
                            if (PermissionHelper.getPermissionHelper().hasPermission(claim, p.getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                                CrashClaim.getPlugin().getDataManager().deleteSubClaim(claim);

                                VisualGroup group = visualizationManager.fetchVisualGroup(player, false);
                                if (group != null){
                                    group.removeAllVisuals();

                                    BaseVisual visual = visualizationManager.getProvider().spawnClaimVisual(VisualColor.GREEN, group, claim.getParent(), player.getLocation().getBlockY() - 1);
                                    visual.spawn();

                                    visualizationManager.despawnAfter(visual, 10);
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to modify this sub claim.");
                            }
                        }
                        return "";
                    }, p -> "").open();
        } else {
            player.sendMessage(ChatColor.RED + "There is no sub claim where you are standing.");
        }
    }
}