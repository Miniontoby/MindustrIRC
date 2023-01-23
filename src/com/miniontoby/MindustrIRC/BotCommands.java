package com.miniontoby.MindustrIRC;

import com.miniontoby.MindustrIRC.MindustrIRC;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;
import arc.struct.Seq;
import mindustry.Vars;

public class BotCommands {
	// private String[] irctolower = { ["["]="{", ["\\"]="|", ["]"]="}" };
	static private HashMap<String, AbstractCommand> bot_commands = new HashMap<String, AbstractCommand>();

	static public void start(){
		bot_commands = new HashMap<String, AbstractCommand>();
		register_bot_command("help", new HelpCommandHandler());
		register_bot_command("players", new PlayersCommandHandler());
		register_bot_command("version", new VersionCommandHandler());
		register_bot_command("source", new SourceCommandHandler());
	}

	static private String irclower(String s) {
		return s.toLowerCase();
		//return (s.toLowerCase().gsub("[%[%]\\]", irctolower));
	}
	static private boolean nickequals(String nick1, String nick2) {
		return irclower(nick1) == irclower(nick2);
	}

	static public boolean check_botcmd(String user, String from, String message) {
		String prefix = "!";
		String nick = MindustrIRC.getNickname();

		// First check for a nick prefix
		if (message.startsWith(nick)) {
			//String suffix = message.substring(nick.length() + 1, nick.length() + 2);
			String suffix = ":";

			if (suffix.equals(":") || suffix.equals(",")) {
				bot_command(user, from, message.substring(nick.length() + 1));
				return true;
			} 
		}
		// Then check for the configured prefix
		else if (!prefix.equals("") && message.substring(0, prefix.length()).toLowerCase().equals(prefix.toLowerCase())) {
			bot_command(user, from, message.substring(prefix.length()));
			return true;
		}
		return false;
	}

	static public void bot_command(String user, String from, String text) {
		Pattern pattern = Pattern.compile("^\\s*(.*)");
		Matcher matcher = pattern.matcher(text);
		if (!matcher.find()) return;
		text = matcher.group(1);

		if (text.substring(0, 1).equals("@")) {
			Pattern pattern2 = Pattern.compile("^.([^\\s]+)\\s(.+)$");
			Matcher matcher2 = pattern2.matcher(text);
			if (!matcher2.find()) return;

			String player_to = matcher2.group(1);
			String message = matcher2.group(2);
/*
		elseif not minetest.get_player_by_name(player_to) then
			irc.reply("User '"..player_to.."' is not in the game.")
			return
		elseif not irc.joined_players[player_to] then
			irc.reply("User '"..player_to.."' is not using IRC.")
			return
		end
		minetest.chat_send_player(player_to,
				minetest.colorize(irc.config.pm_color,
				"PM from "..msg.user.nick.."@IRC: "..message, false))
*/
			// MindustrIRC.IRCMessage("Message to " + player_to + ": " + message, "#mindustry", false);
			MindustrIRC.IRCMessage("Message sent!", from, false);
			return;
		}
		int pos = text.indexOf(" ", 1);
		String cmd = "";
		String args = "";
		if (pos != -1) {
			cmd = text.substring(1, pos - 1);
			args = text.substring(pos + 1);
		}
		else {
			cmd = text;
		}

		if (!bot_commands.containsKey(cmd)) {
			MindustrIRC.IRCMessage("Unknown command '" + cmd + "'. Try 'help'. Or use @playername <message> to send a private message", from, false);
			return;
		}
		String message = bot_commands.get(cmd).execute(user, args);
		if (!message.equals("")){
			MindustrIRC.IRCMessage(message, from, false);
		}
	}


	public static void register_bot_command(String name, AbstractCommand function) {
		if (name.substring(1, 1) == "@") {
			System.out.println("Erroneous bot command name. Command name begins with '@'.");
			return;
		}
		bot_commands.put(name, function);
	}

	static public void handleMessage(String[] data){
		if (data[3].startsWith(":" + MindustrIRC.getNickname())){
			String from = data[2];
			switch (data[4]) {
				case "help":
					break;
				case "players":
					Seq<String> players = Seq.with(Vars.net.getConnections()).map(con -> con.player.name).removeAll(p -> p == null);
					MindustrIRC.IRCMessage("Connected players: " + players.toString(", "), from, false);
					break;
				case "version":
					MindustrIRC.IRCMessage("Version: " + MindustrIRC.ctcp_version, from, false);
					break;
				case "source":
					MindustrIRC.IRCMessage("Source: https://edugit.org/Miniontoby/mindustrirc", from, false);
					break;
				default:
					MindustrIRC.IRCMessage("Unknown command '" + data[4] + "'. Try 'help'. Or use @playername <message> to send a private message", from, false);
					break;
			}
		}
	}
	static public HashMap getCommands(){
		return bot_commands;
	}

}
abstract class AbstractCommand {
	public abstract String execute(String user, String args);
}
class HelpCommandHandler extends AbstractCommand {
	@Override
	public String execute(String user, String args) {
		String commands = BotCommands.getCommands().keySet().toString();
		return "Available commands: " + commands;
	}
}
class PlayersCommandHandler extends AbstractCommand {
	@Override
	public String execute(String user, String args) {
		Seq<String> players = Seq.with(Vars.net.getConnections()).map(con -> con.player.name).removeAll(p -> p == null);
		return "Connected players: " + players.toString(", ");
	}
}
class VersionCommandHandler extends AbstractCommand {
	@Override
	public String execute(String user, String args) { return "Version: " + MindustrIRC.ctcp_version; }
}
class SourceCommandHandler extends AbstractCommand {
	@Override
	public String execute(String user, String args) { return "Source: https://edugit.org/Miniontoby/mindustrirc"; }
}
