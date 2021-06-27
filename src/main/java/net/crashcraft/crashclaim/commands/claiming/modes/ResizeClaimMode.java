package net.crashcraft.crashclaim.commands.claiming.modes;

import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.commands.claiming.ClaimMode;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.ErrorType;
import net.crashcraft.crashclaim.data.StaticClaimLogic;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ResizeClaimMode implements ClaimMode {
    private final ClaimCommand commandManager;
    private final Player player;
    private Location firstLocation;
    private final Claim claim;
    private final VisualizationManager visualizationManager;
    private final ClaimDataManager manager;

    public ResizeClaimMode(ClaimCommand commandManager, Player player, Claim claim, Location firstLocation){
        this.commandManager = commandManager;
        this.player = player;
        this.firstLocation = firstLocation;
        this.claim = claim;

        this.visualizationManager = commandManager.getVisualizationManager();
        this.manager = commandManager.getDataManager();

        firstClick();
    }

    private void firstClick(){
        if (StaticClaimLogic.isClaimBorder(claim.getMinX(), claim.getMaxX(), claim.getMinZ(),
                claim.getMaxZ(), firstLocation.getBlockX(), firstLocation.getBlockZ())){
            if (PermissionHelper.getPermissionHelper().hasPermission(claim, player.getUniqueId(), PermissionRoute.MODIFY_CLAIM)){
                claim.setEditing(true);

                VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
                group.removeAllVisuals();

                visualizationManager.getProvider().spawnClaimVisual(VisualColor.GOLD, group, claim, player.getLocation().getBlockY() - 1).spawn();

                player.sendMessage(Localization.RESIZE__CLICK_ANOTHER_LOCATION.getMessage());
            } else {
                player.sendMessage(Localization.RESIZE__NO_PERMISSION.getMessage());
                cleanup(player.getUniqueId(), true);
            }
        } else {
            firstLocation = null;
            player.sendMessage(Localization.RESIZE__INSTRUCTIONS.getMessage());
        }
    }

    @Override
    public void click(Player player, Location click) {
        if (firstLocation == null){
            firstLocation = click;
            firstClick();
            return;
        }

        UUID uuid = player.getUniqueId();

        ErrorType error = manager.resizeClaim(claim, firstLocation.getBlockX(), firstLocation.getBlockZ(), click.getBlockX(), click.getBlockZ(), player,
                aBoolean -> {
                    VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
                    BaseVisual visual = visualizationManager.getProvider().spawnClaimVisual(VisualColor.GREEN, group, claim, player.getLocation().getBlockY() - 1);

                    group.removeAllVisuals();
                    visual.spawn();

                    visualizationManager.deSpawnAfter(visual, 5);

                    if (aBoolean) {
                        player.sendMessage(Localization.RESIZE__SUCCESS.getMessage());
                    } else {
                        cleanup(uuid, true);
                    }
                });

        switch (error){
            case OVERLAP_EXISTING:
                player.sendMessage(Localization.RESIZE__NO_OVERLAP.getMessage());
                cleanup(uuid, true);
                return;
            case TOO_SMALL:
                player.sendMessage(Localization.RESIZE__MIN_SIZE.getMessage());
                cleanup(uuid, true);
                return;
            case CANNOT_FLIP_ON_RESIZE:
                player.sendMessage(Localization.RESIZE__CANNOT_FLIP.getMessage());
                cleanup(uuid, true);
                return;
            case OVERLAP_EXISTING_SUBCLAIM:
                player.sendMessage(Localization.RESIZE__OVERLAP_EXISTING.getMessage());
                cleanup(uuid, true);
                return;
            case OVERLAP_EXISTING_OTHER:
                player.sendMessage(Localization.RESIZE__ERROR_OTHER.getMessage());
            case NONE:
                cleanup(uuid, false);
        }
    }

    @Override
    public void cleanup(UUID player, boolean visuals) {
        claim.setEditing(false);

        commandManager.forceCleanup(player, visuals);
    }
}
