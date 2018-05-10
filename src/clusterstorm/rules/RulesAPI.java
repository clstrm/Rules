package clusterstorm.rules;

public class RulesAPI {

	public static boolean isConfirmed(String player) {
		if(player == null) return false;
		
		return Rules.players().hasPlayer(player);
	}
}
