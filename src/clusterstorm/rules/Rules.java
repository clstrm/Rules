package clusterstorm.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Rules extends JavaPlugin implements Listener {

	private static Rules instance;
	public static Sound sound;
	private MenuManager menu;
	private Pastebin pastebin;
	private Players players;
	private AuthmeHook hook;
	
	private boolean useConfirmation;
	private List<String> allowedCommands;
	
	@Override
	public void onEnable() {
		instance = this;
		
		if(!new File(getDataFolder() + File.separator + "config.yml").exists()) {
			getConfig().options().copyDefaults(true);
			saveDefaultConfig();
			reloadConfig();
		}
		
		useConfirmation = getConfig().getBoolean("useConfirmation", true);
		allowedCommands = getConfig().getStringList("allowedCommands");
		try {
			sound = Sound.valueOf(getConfig().getString("acceptSound"));
		} catch (Exception e) {
			sound = null;
		}
		if(allowedCommands == null) allowedCommands = new ArrayList<>();
		
		pastebin = new Pastebin();
		
		if(getConfig().getBoolean("reloadPastebinOnStart")) {
			pastebin.reload();
		}
		
		menu = new MenuManager();
		players = new Players();
		
		PluginCommand command = getCommand("rules");
		if(command != null) command.setExecutor(new RulesCommand());
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
			@Override
			public void run() {
				if(getConfig().getBoolean("authmeHook") && Bukkit.getPluginManager().isPluginEnabled("AuthMe")) {
					hook = new AuthmeHook();
					Bukkit.getPluginManager().registerEvents(hook, instance);
				} else hook = null;
			}
		});
	}
	
	public void reload() {
		useConfirmation = getConfig().getBoolean("useConfirmation", true);
		allowedCommands = getConfig().getStringList("allowedCommands");
		if(allowedCommands == null) allowedCommands = new ArrayList<>();
		try {
			sound = Sound.valueOf(getConfig().getString("acceptSound"));
		} catch (Exception e) {
			sound = null;
		}
		reloadConfig();
		menu.reload();
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory() == null) return;
		if(e.getInventory().equals(menu.i)) {
			e.setCancelled(true);
			if(e.getSlot() >= 0 && e.getSlot() < 9) {
				sendLore(e.getWhoClicked(), e.getCurrentItem());
			}
			return;
		}
		if(e.getInventory().equals(menu.c)) {
			e.setCancelled(true);
			if(!e.getInventory().equals(e.getClickedInventory())) return;
			if(e.getSlot() == menu.accept) {
				menu.accept((Player) e.getWhoClicked());
				return;
			}
			if(e.getSlot() == menu.deny) {
				menu.deny((Player) e.getWhoClicked());
				return;
			}
			
			if(e.getSlot() >= 0 && e.getSlot() < 9) {
				sendLore(e.getWhoClicked(), e.getCurrentItem());
			}
		}
	}
	
	private void sendLore(HumanEntity p, ItemStack i) {
		if(i == null) return;
		if(!i.hasItemMeta()) return;
		ItemMeta m = i.getItemMeta();
		if(m.hasDisplayName()) p.sendMessage(m.getDisplayName());
		if(!m.hasLore()) return;
		
		for (String l : m.getLore()) {
			p.sendMessage(l);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(!useConfirmation) return;
		
		if(hook == null) {
			performJoin(e.getPlayer());
			return;
		}
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
			@Override
			public void run() {
				if(hook.isLoggedIn(e.getPlayer())) performJoin(e.getPlayer());
			}
		});
	}
	
	public void performJoin(Player p) {
		if(!useConfirmation) return;
		
		if(players.hasPlayer(p.getName())) return;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
			@Override
			public void run() {
				if(p.isOnline())
					menu.openConfirm(p);
			}
		}, 5);
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(e.getInventory() == null) return;
		if(e.getInventory().equals(menu.c) && !players.hasPlayer(e.getPlayer().getName())) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				
				@Override
				public void run() {
					if(((Player) e.getPlayer()).isOnline())
						e.getPlayer().openInventory(e.getInventory());
				}
			}, 5);
		}
	}
	
	
	public static Rules getInstance() {
		return instance;
	}
	
	public static MenuManager menu() {
		return instance.menu;
	}
	
	public static Pastebin pastebin() {
		return instance.pastebin;
	}
	
	public static Players players() {
		return instance.players;
	}
	
	
	
	
	
	private boolean shouldCancel(Player p) {
		return !players.hasPlayer(p.getName());
	}
	
	
	
	
	@EventHandler
	public void a1(BlockPlaceEvent e) {
		if(shouldCancel(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void a2(BlockBreakEvent e) {
		if(shouldCancel(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void a3(PlayerInteractEvent e) {
		if(shouldCancel(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void a4(PlayerInteractEntityEvent e) {
		if(shouldCancel(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	
	@EventHandler
	public void a5(PlayerInteractAtEntityEvent e) {
		if(shouldCancel(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	
	@EventHandler
	public void a6(InventoryOpenEvent e) {
		if(e.getInventory() == null) return;
		if(e.getInventory().equals(menu.c)) return;
		
		if(shouldCancel((Player) e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void a6(PlayerCommandPreprocessEvent e) {
		String msg = e.getMessage() + " ";
		for (String a : allowedCommands) {
			if(msg.startsWith("/" + a + " ")) {
				return;
			}
		}
		
		if(shouldCancel((Player) e.getPlayer())) {
			e.setCancelled(true);
		}
	}
}
