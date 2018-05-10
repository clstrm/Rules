package clusterstorm.rules;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.xephi.authme.api.API;
import fr.xephi.authme.events.LoginEvent;

public class AuthmeHook implements Listener {

	@EventHandler
	public void onLogin(LoginEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Rules.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				Rules.getInstance().performJoin(e.getPlayer());
			}
		}, 20);
	}
	
	
	public boolean isLoggedIn(Player player) {
		return API.isAuthenticated(player);
	}
}
