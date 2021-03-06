package br.com.finalcraft.gppskyblock;

import br.com.finalcraft.evernifecore.config.uuids.UUIDsController;
import br.com.finalcraft.gppskyblock.integration.GPPluginBase;
import br.com.finalcraft.gppskyblock.integration.IClaim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class Island {
	private UUID ownerId;
	private IClaim claim;
	private Location spawn;
	public boolean ready = true;
	
	public Island(UUID ownerId, IClaim claim) {
		this.ownerId = ownerId;
		this.claim = claim;
		this.spawn = this.getCenter().add(0.5, 1, 0.5);
	}
	
	public Island(UUID ownerId, IClaim claim, Location spawn) {
		this.ownerId = ownerId;
		this.claim = claim;
		this.spawn = spawn;
	}
	
	public IClaim getClaim() {
		return claim;
	}
	
	public Location getSpawn() {
		return spawn;
	}
	
	public UUID getOwnerId() {
		return ownerId;
	}
	
	public Player getPlayer() {
		return Bukkit.getPlayer(ownerId);
	}
	
	public String getOwnerName() {
		return UUIDsController.getNameFromUUID(ownerId);
	}
	
	public boolean isOwnerOnline() {
		return Bukkit.getPlayer(ownerId) != null;
	}
	
	public void reset() {
		try {
			File schematicFile = new File(GPPSkyBlock.getInstance().getDataFolder(), GPPSkyBlock.getInstance().config().schematic+".schematic");
			if (!schematicFile.exists()) {
				throw new IllegalStateException("Schematic file \""+GPPSkyBlock.getInstance().config().schematic+".schematic\" doesn't exist");
			}

			this.teleportEveryoneToSpawn();

			this.ready = false;
			//new ResetIslandTask(this, schematicFile).runTaskTimer(GPPSkyBlock.getInstance(), 1L, 1L);
			GPPluginBase.getInstance().assyncRestoreIsland(this, schematicFile);
		}catch (Exception e){
			e.printStackTrace();
		}

	}
	
	public int getRadius() {
		int lx = claim.getLesserBoundaryCorner().getBlockX();
		int gx = claim.getGreaterBoundaryCorner().getBlockX();
		
		return (gx-lx)/2;
	}

	public void setRadius(int radius) {
		if (radius>254 || radius<1) {
			throw new IllegalArgumentException("Invalid radius (max 254)");
		}
		GPPluginBase.getInstance().setRadius(this, radius);
	}
	
	public Location getCenter() {
		int radius = this.getRadius();
		return new Location(claim.getWorld(), claim.getLesserBoundaryCorner().getBlockX()+radius, GPPSkyBlock.getInstance().config().yLevel, claim.getLesserBoundaryCorner().getBlockZ()+radius);
	}
	
	public void teleportEveryoneToSpawn() {
		Location spawnLocation = GPPSkyBlock.getInstance().getSpawn();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (this.getClaim().contains(player.getLocation(), false)) {
				player.teleport(spawnLocation);
			}
		}
	}
	
	public void setSpawn(Location location) throws Exception {
		this.spawn = location;
		GPPSkyBlock.getInstance().getDataStore().updateIsland(this);
	}
	
	public void setIslandBiome(Biome biome) {
		int x = this.getClaim().getLesserBoundaryCorner().getBlockX();
		int sz = this.getClaim().getLesserBoundaryCorner().getBlockZ();
		int ux = this.getClaim().getGreaterBoundaryCorner().getBlockX();
		int uz = this.getClaim().getGreaterBoundaryCorner().getBlockZ();
		
		for (; x <= ux; x++) {
			for (int z = sz; z <= uz; z++) {
				this.getClaim().getWorld().setBiome(x, z, biome);
			}
		}
	}
	
	public void setChunkBiome(Biome biome, int chunkX, int chunkZ) {
		int x = chunkX<<4;
		int sz = chunkZ<<4;
		int ux = x+16;
		int uz = sz+16;
		
		for (; x < ux; x++) {
			for (int z = sz; z < uz; z++) {
				this.getClaim().getWorld().setBiome(x, z, biome);
			}
		}
	}
	
	public void setBlockBiome(Biome biome, int blockX, int blockZ) {
		this.getClaim().getWorld().setBiome(blockX, blockZ, biome);
	}
	
	public void deleteRegionFile() {
		int x = this.getSpawn().getBlockX() >> 9, z = this.getSpawn().getBlockZ() >> 9;
		File regionFile = new File(this.getSpawn().getWorld().getWorldFolder(), "region" + File.separator + "r."+x+"."+z+".mca");
		if (!regionFile.delete()) {
			regionFile.deleteOnExit();
		}
	}

	void removeThisIslandFromServer() {
		int x = this.getSpawn().getBlockX() >> 9, z = this.getSpawn().getBlockZ() >> 9;
		File regionFile = new File(this.getSpawn().getWorld().getWorldFolder(), "region" + File.separator + "r."+x+"."+z+".mca");
		if (!regionFile.delete()) {
			regionFile.deleteOnExit();
		}
	}
}
