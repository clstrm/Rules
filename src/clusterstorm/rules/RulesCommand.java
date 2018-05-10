package clusterstorm.rules;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RulesCommand implements CommandExecutor {

	public static final String prefix = "§2§lRules §3> §f";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0 && sender.hasPermission("rules.options")) 
		{
			if(args[0].equalsIgnoreCase("-rl")) {
				sender.sendMessage(prefix + "Starting reloading...");
				Rules.getInstance().reload();
				sender.sendMessage(prefix + "Configuration reloaded!");
				return true;
			}
			
			if(args[0].equalsIgnoreCase("-clear")) {
				Rules.players().clear();
				sender.sendMessage(prefix + "Players cleared!");
				return true;
			}
			
			if(args[0].equalsIgnoreCase("-reload")) {
				Bukkit.getScheduler().runTaskAsynchronously(Rules.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						sender.sendMessage(prefix + "Starting loading from pastebin...");
						Rules.pastebin().reload();
						Rules.getInstance().reload();
						sender.sendMessage(prefix + "Rules was loaded!");
					}
				});
				return true;
			}
		}
		
		if(!sender.hasPermission("rules.rules")) {
			sender.sendMessage("§cInsufficient permissions!");
			return true;
		}
		
		Player p = null;
		if(args.length > 0 && sender.hasPermission("rules.rules.others")) 
		{
			p = Bukkit.getPlayerExact(args[0]);
			if(p == null) {
				sender.sendMessage(prefix + "Player " + args[0] + " not found");
				return true;
			}
		} else {
			if(sender instanceof Player) p = (Player) sender;
			else {
				sender.sendMessage(prefix + "Open rules - /rules <player>");
				return true;
			}
		}
		Rules.menu().openRules(p);
		
		return true;
	}
	
}
