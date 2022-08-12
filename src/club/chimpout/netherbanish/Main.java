package club.chimpout.netherbanish;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Main extends JavaPlugin implements Listener {
	
	public	boolean		allowRoof		 = false;
	public	float		netherRoofHeight = 125;

	public	String		netherDimension	 = "world_nether";
	Location			netherSpawn		 = new Location(null, 0, 0, 0);

	
	
	
	
	
	
	
	
	@Override
	public void onEnable() {
		
		this.saveDefaultConfig();
		
		if(getConfig().getBoolean("enabled")) {
			
			Bukkit.getServer().getLogger().info("NetherBanish Started!");
			
		} else {
			
			Bukkit.getServer().getLogger().info("NetherBanish Disabled in Config.");
			Bukkit.getServer().getLogger().info("NetherBanish will not start.");
			
			return;
			
		}
		
		this.getServer().getPluginManager().registerEvents(this, this);
		
		World world = Bukkit.getServer().getWorld(getConfig().getString("NetherWorld"));
		
		netherSpawn.setWorld(world);
		netherSpawn.add(getConfig().getInt("spawnX"),getConfig().getInt("spawnY"),getConfig().getInt("spawnZ"));
		
		
		Bukkit.getServer().getLogger().info("NetherBanish Loaded!");
		
	}
	
	@Override
	public void onDisable() {
		
		Bukkit.getServer().getLogger().info("NetherBanish Disabled!");
		
	}
	
	
	public void reloadPlugin() {
		
		this.reloadConfig();
		
		allowRoof = getConfig().getBoolean("netherRoof");
		netherRoofHeight = getConfig().getInt("netherRoofHeight");
		
		World world = Bukkit.getServer().getWorld(getConfig().getString("NetherWorld"));
		
		netherSpawn.zero();
		netherSpawn.setWorld(world);
		netherSpawn.add(getConfig().getInt("spawnX"),getConfig().getInt("spawnY"),getConfig().getInt("spawnZ"));
		
		
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		boolean displayHelp = false;

		if (args.length >= 1) {

			String subcommand = args[0].toLowerCase();

			switch(subcommand) {
			
				case "reload":
					
					reloadPlugin();
					
					break;
					
				default:
					displayHelp = true;
				
					break;
					
			}
			
		}
					
		return displayHelp;

	}
	
	//PlayerChecks
	//PlayerChecks
	//PlayerChecks
	
	
	public boolean isBanished(Player p) {
		
		boolean banish = false;
		
		if(p.hasPermission("NetherBanish.banished")) {
			banish = true;
		}
		
		return banish;
		
	}
	
	public boolean inNether(Player p) {
		
		boolean nether = false;
		
		if (p.getWorld().getEnvironment() == Environment.NETHER) {
			nether = true;
		}
		
		return nether;
		
	}
	
	public boolean onRoof(Player p) {
		
		boolean roof = false;
		
		if(!allowRoof) {
			
			if(p.getLocation().getY() > netherRoofHeight) {
				
				roof = true;
				
			}
			
		}
		return roof;
	}
	
	public boolean worldNether(World w) {
		
		boolean isNether = false;
				
		if(w.getEnvironment() == Environment.NETHER) {
			
			isNether = true;
			
		}
		
		return isNether;
		
	}
	
	
	//Plug-in Event handling.
	//Plug-in Event handling.	
	//Plug-in Event handling.

	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		
		//Bukkit.getServer().getLogger().info(event.getCause().toString());
		
		if(event.getCause() != TeleportCause.UNKNOWN) {
		
		
			if(isBanished(event.getPlayer())) {
				
				if(worldNether(event.getTo().getWorld())) {
					
				//Player is teleporting to the nether, probably don't need this.
					
				//This would be the allow criteria for standardRespawn, below.
					
				} else if (!inNether(event.getPlayer())){
					
				//Player trying to teleport isn't in the nether, or going there
					
					event.setCancelled(true);
					
					standardRespawn(event.getPlayer());
					
				} else {
					
				//We assume the player is in the nether, and trying to leave it.
					
					event.getPlayer().sendRawMessage(ChatColor.RED + getConfig().getString("onPortal"));
					
					event.setCancelled(true);
					
				}
				
			}
		}
	}
	
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		if(isBanished(event.getPlayer())) {
			
			event.getPlayer().sendRawMessage(ChatColor.RED + getConfig().getString("onBanish"));
			
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		
		if(isBanished(event.getPlayer())) {
			
			if(!event.isAnchorSpawn()){
		
				if(worldNether(event.getRespawnLocation().getWorld())) {
					
					//respawning in the nether, no code required probably.
					
				} else {
					
					//respawning out of the nether.
					
					event.setRespawnLocation(netherSpawn);
					
				}
				
				event.getPlayer().sendRawMessage(event.getRespawnLocation().getWorld().toString());
				
			}
		
		}
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent event) {
        
		standardRespawn(event.getPlayer());

    }
	
	//State Handling functions
	
    public void standardRespawn(Player p) {
    	
    	if(isBanished(p)) {
			
			if(!inNether(p)) {
				
				p.sendRawMessage(ChatColor.RED + getConfig().getString("onTeleport"));
				
			}
			
			if(onRoof(p) && inNether(p)) {
				
				p.sendRawMessage(ChatColor.RED + getConfig().getString("onRoof"));
				
			}
			
			if(!inNether(p) || onRoof(p)) {
			
				makeBlocks(3, 3, netherSpawn);
				
				p.teleport(netherSpawn);
			
			}
			
        }
    	
    }
        
    
	public void makeBlocks(int radius, int height, Location loc) {
    	 	
    	final Location Yloc;
    	
    	int diameter = radius*2;
    	
    	loc.subtract(radius, 1, radius);

    	Yloc = loc;
    	
    	for(int ix = 0; ix < diameter; ix++) {
    		
    		for(int iz = 0; iz < diameter; iz++) {
    			
    			loc.getBlock().setType(Material.CRYING_OBSIDIAN);
    			
    			for(int iy = 0; iy < height; iy++) {
    				
    				Yloc.add(0, 1, 0).getBlock().setType(Material.AIR);
    				
    			}
    			
    			loc.add(0, -height, 1);
    			
    		}
    		
    		loc.add(1, 0, -diameter);
    		
    	}
    	
    	loc.add(-radius, 1, radius);
    	
    }
	
}