package net.crashcraft.crashclaim.migration.adapters;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.crashcraft.crashclaim.claimobjects.PermState;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.data.ClaimResponse;
import net.crashcraft.crashclaim.migration.MigrationAdapter;
import net.crashcraft.crashclaim.migration.MigrationManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class GriefPreventionAdaptor implements MigrationAdapter {
    private final GriefPrevention griefPrevention;

    public GriefPreventionAdaptor(){
        griefPrevention = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
    }

    @Override
    public String checkRequirements(MigrationManager manager) {
        if (griefPrevention == null){
            return "GriefPrevention was not located at runtime. Is it installed?";
        }

        if (griefPrevention.dataStore.getClaims().size() == 0){
            return "GriefPrevention does not report any loaded claims to migrate";
        }

        return null;
    }

    @Override
    public String migrate(MigrationManager manager) {
        final Logger logger = manager.getLogger();
        final DataStore dataStore = griefPrevention.dataStore;

        logger.info("Found " + dataStore.getClaims().size() + " claims in memory, migrating...");
        for (Claim claim : dataStore.getClaims()){
            if (claim.isAdminClaim()){
                //TODO implement admin claims so we can support this
                continue;
            }

            if (claim.parent != null){
                logger.info("Claim had a parent, skipping and waiting for parent");
                continue;
            }

            ClaimResponse claimResponse = manager.getManager().createClaim(claim.getGreaterBoundaryCorner(), claim.getLesserBoundaryCorner(), claim.getOwnerID());

            if (!claimResponse.isStatus()){
                logger.warning("A claim has failed to be created due to [" + claimResponse.getError().name() + "] : {Owner: " + claim.getOwnerName() + "}");
                continue;
            }

            net.crashcraft.crashclaim.claimobjects.Claim cClaim = (net.crashcraft.crashclaim.claimobjects.Claim) claimResponse.getClaim();

            grantPermissions(manager, claim, cClaim);

            //Children convert to SubClaims
            for (Claim child : claim.children){
                grantPermissions(manager, child, cClaim);
            }

            logger.info("Successfully migrated claim [" + claim.getID() + "]");
        }
        return null;
    }

    private void grantPermissions(MigrationManager manager, Claim claim, net.crashcraft.crashclaim.claimobjects.Claim cClaim) {
        if (claim.areExplosivesAllowed){
            cClaim.getPerms().getGlobalPermissionSet().setExplosions(PermState.ENABLED);
        }

        ArrayList<String> builders = new ArrayList<>();
        ArrayList<String> containers = new ArrayList<>();
        ArrayList<String> access = new ArrayList<>();
        ArrayList<String> managers = new ArrayList<>();

        claim.getPermissions(builders, containers, access, managers);

        createPermsAndGrant(manager, cClaim, builders, ClaimPermission.Build);
        createPermsAndGrant(manager, cClaim, containers, ClaimPermission.Inventory);
        createPermsAndGrant(manager, cClaim, access, ClaimPermission.Access);

        for (String fakeUUID : managers){
            UUID uuid = UUID.fromString(fakeUUID);

            PlayerPermissionSet permissionSet = cClaim.getPerms().getPlayerPermissionSet(uuid);

            if (claim.managers.contains(fakeUUID)) {
                permissionSet.setModifyClaim(PermState.ENABLED);
                permissionSet.setModifyPermissions(PermState.ENABLED);
            }
        }
    }

    private void createPermsAndGrant(MigrationManager manager, net.crashcraft.crashclaim.claimobjects.Claim cClaim, ArrayList<String> uuids, ClaimPermission permission){
        for (String fakeUUID : uuids) {
            UUID uuid = UUID.fromString(fakeUUID);
            PlayerPermissionSet permissionSet = cClaim.getPerms().getPlayerPermissionSet(uuid);

            if (ClaimPermission.Build.isGrantedBy(permission)) {
                permissionSet.setBuild(PermState.ENABLED);
            }

            if (ClaimPermission.Access.isGrantedBy(permission)) {
                permissionSet.setInteractions(PermState.ENABLED);
            }

            if (ClaimPermission.Inventory.isGrantedBy(permission)) {
                permissionSet.setEntities(PermState.ENABLED);

                for (Material container : manager.getManager().getPermissionSetup().getTrackedContainers()) {
                    permissionSet.setContainer(container, PermState.ENABLED);
                }
            }

            cClaim.getPerms().setPlayerPermissionSet(
                    uuid, permissionSet
            );
        }
    }

    @Override
    public String getIdentifier() {
        return "GriefPrevention";
    }
}
