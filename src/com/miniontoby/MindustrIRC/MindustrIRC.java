package com.miniontoby.MindustrIRC;

import com.miniontoby.MindustrIRC.IRCBot;
import com.miniontoby.MindustrIRC.BotCommands;

import java.net.*;
import java.io.*;
import java.util.*;
import arc.*;
import arc.util.*;
import arc.struct.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.*;

public class MindustrIRC extends Plugin {
	static private IRCBot bot;
	static public String server = Core.settings.getString("ircServer");
	static public int port = Core.settings.getInt("ircPort");
	static private String nickname = Core.settings.getString("ircNickname");
	static private String realname = Core.settings.getString("ircRealname");
	static private String password = Core.settings.getString("ircPassword");
	static private String channel = Core.settings.getString("ircChannel");
	static public String version = "1.1.0";
	static public String ctcp_version = "MindustrIRC v" + version;

	static public boolean started = false;

	static public void ConsoleLog(String message, boolean withPrefix){
		if (withPrefix) Log.info("[MindustrIRC] " + message);
		else Log.info(message);

//		try(FileWriter fw = new FileWriter("logs.txt", true);
//		BufferedWriter bw = new BufferedWriter(fw);
//		PrintWriter out = new PrintWriter(bw)) {
//			if (withPrefix) out.println("[MindustrIRC] " + message);
//			else out.println(message);
//		} catch (IOException e) {}
	}
	static public void ConsoleLog(String message) {
		ConsoleLog(message, false);
	}

	static public String getNickname(){
		return nickname;
	}

	static public void IRCMessage(String message, String to, boolean withThing) {
		try {
			if (!withThing){
				bot.sendMessage(to, message);
			} else {
				bot.sendMessage(to, "[MindustrIRC] " + message);
			}
		} catch (Exception ex){
			ConsoleLog("IRCMessage: Exception: " + ex, true);
		}
	}
	static public void IRCNotice(String message, String to) {
		try {
			bot.sendNotice(to, message);
		} catch (Exception ex){
			ConsoleLog("IRCNotice: Exception: " + ex, true);
		}
	}
	static public void ingameMessage(String message) {
		Call.sendMessage(message);

		String ColorCoded = message.replace("[red]", "\u001B[31m").replace("[white]", "\u001B[37m").replace("[grey]", "\u001B[0m").replace("[[", "[");
		ConsoleLog(ColorCoded);
	}

	public static int Seqsize(Iterable data) {
		if (data instanceof Collection) return ((Collection<?>) data).size();
		int counter = 0;
		for (Object i : data) counter++;
		return counter;
	}

	@Override
	public void init(){
		if(Vars.headless){
			ConsoleLog("Loaded!", true);
			Core.settings.defaults("ircServer", "irc.libera.chat", "ircPort", 6667, "ircNickname", "MindustrIRC", "ircRealname", "MindustrIRC Bot by Miniontoby", "ircPassword", "examplepassword", "ircChannel", "#mindustry");
			loadSettings();
			setupGameListeners();
			connectToIRC();
			BotCommands.start();
		}
	}

	public void loadSettings() {
		if (Core.settings.has("ircServer") && Core.settings.has("ircPort") && Core.settings.has("ircNickname") && Core.settings.has("ircRealname") && Core.settings.has("ircPassword") && Core.settings.has("ircChannel")) {
			server = Core.settings.getString("ircServer");
			port = Core.settings.getInt("ircPort");
			nickname = Core.settings.getString("ircNickname");
			realname = Core.settings.getString("ircRealname");
			password = Core.settings.getString("ircPassword");
			channel = Core.settings.getString("ircChannel");
		}
	}

	static private void connectToIRC() {
		Core.app.post(() -> {
			bot = new IRCBot(server, port, nickname, realname, password, channel);
			bot.start();
		});
	}

	static private void setupGameListeners(){
		Events.on(PlayEvent.class, event -> {
			if (!started) {
				IRCMessage("The server just started hosting a game! Join now!", channel, true);
				started = true;
			} else if (Seqsize(Vars.net.getConnections()) != 0){
				IRCMessage("A new game has started!", channel, true);
			}
		});
		Events.on(DisposeEvent.class, event -> { 
			try {
				bot.shutdown();
			} catch (Exception ex) {}
		});

		Events.on(GameOverEvent.class, event -> {
			if (Seqsize(Vars.net.getConnections()) != 0) IRCMessage("Game over!", channel, true);
		});
		Events.on(WinEvent.class, event -> {
			IRCMessage("Win Event!", channel, true);
		});
		Events.on(LoseEvent.class, event -> {
			IRCMessage("Lose Event!", channel, true);
		});
		Events.on(PlayerChatEvent.class, event -> {
			String playername = event.player.plainName();
			String message = Strings.stripColors(event.message);
			if (message.startsWith("/")) return;
			IRCMessage("<" + playername + "> " + message, channel, false);
		});
		Events.on(PlayerJoin.class, event -> {
			String playername = event.player.plainName();
			IRCMessage("*** " + playername + " joined the game", channel, false);
		});
		Events.on(PlayerLeave.class, event -> {
			String playername = event.player.plainName();
			IRCMessage("*** " + playername + " left the game", channel, false);
		});
	}

	static public void handleIRCMessage(String[] data) {
		// [---------0---------] [--1--] [---2--] [---3--]
		// :NICK!IDENT@BIND.HOST PRIVMSG #CHN/USR :MESSAGE
		// :NICK!IDENT@BIND.HOST NOTICE  #CHN/USR :MESSAGE
		// :NICK!IDENT@BIND.HOST JOIN    :#channel
		// :NICK!IDENT@BIND.HOST PART    #channel :REASON
		// :NICK!IDENT@BIND.HOST QUIT    :REASON
		// :NICK!IDENT@BIND.HOST NICK    NEWNICK
		if (data.length >= 3) {
			//Log.info(String.join(" ", data));
			String[] split = data[0].split("!");
			String user = split[0].substring(1);
			String message = "";

			switch (data[1]){
				case "PRIVMSG":
					if (data.length >= 4){
						message = data[3].split(":",2)[1];
						if (message.startsWith("\\001")) {
							switch (message){
								case "\\001VERSION":
									IRCNotice("\001VERSION " + version + "", user); break;
								case "\\001SOURCE":
									IRCNotice("\001SOURCE https://edugit.org/Miniontoby/mindustrirc\\001", user); break;
								default:
									break;
							}
						} 
						else {
							for (int i = 4; i < data.length; i++){ message += " " + data[i]; }
							ingameMessage("[red][[[grey]" + user + "@IRC[red]]:[white] " + message);

							BotCommands.check_botcmd(user, data[2], message);
							//if (data.length >= 5){
							//	BotCommands.handleMessage(data);
							//}
						}
					}
					break;
				case "NOTICE":
					if (data.length >= 4) {
						message = data[3].split(":",2)[1];
						for (int i = 4; i < data.length; i++){ message += " " + data[i]; }
						ingameMessage("[red]-[grey]" + user + "@IRC[red]-[white] " + message);
						BotCommands.check_botcmd(user, data[2], message);
						//if (data.length >= 5) BotCommands.handleMessage(data);
					}
					break;
				case "JOIN":
					String chan = data[2].split(":")[1];
					ingameMessage("[grey]-!- " + user + " joined " + chan);
					break;
				case "PART":
					if (data.length >= 4){
						message = data[3].split(":")[1];
						for (int i = 4; i < data.length; i++){ message += " " + data[i]; }
					}

					ingameMessage("[grey]-!- " + user + " has left " + data[2] + " [" + message + "]");
					break;
				case "QUIT":
					message = data[2].split(":")[1];
					for (int i = 3; i < data.length; i++){ message += " " + data[i]; }

					ingameMessage("[grey]-!- " + user + " has quit [" + message + "]");
					break;
				case "NICK":
					ingameMessage("[grey]-!- " + user + " is now known as " + data[2]);
					break;
				default:
			}
		}
	}

	//register commands that run on the server
	@Override
	public void registerServerCommands(CommandHandler handler){
		handler.register("irc_reconnect", "Reconnect IRC.", args -> {
			if (bot != null) {
				try {
					bot.shutdown();
				} catch (Exception ex) {
					ConsoleLog("Didn't shutdown bot because of an error. Please try again.", true);
				}
			}
			connectToIRC();
		});
		handler.register("irc_set", "[option] [value]", "Change IRC Config.", args -> {
			String[] options = {"Server", "Port", "Nickname", "Realname", "Password", "Channel"};
			if (args.length >= 1) {
				String optionName = Arrays.stream(options).filter(x -> args[0].equalsIgnoreCase(x)).findFirst().orElse(null);
				if(optionName != null) {
					if (args.length == 2){
						if (optionName == "Port") Core.settings.put("irc"+optionName, Integer.parseInt(args[1]));
						else Core.settings.put("irc"+optionName, args[1]);
						ConsoleLog(optionName + " is now set to " + args[1], true);
						loadSettings();
					} else {
						String value = "";
						if (optionName == "Port") value = Integer.toString(Core.settings.getInt("irc"+optionName));
						else value = Core.settings.getString("irc"+optionName);
						ConsoleLog(optionName + " is set to " + value, true);
					}
				} else {
					ConsoleLog(args[0] + " is not a valid option! Please choose one of: "  + String.join(", ", options), true);
				}
			} else {
				ConsoleLog("Available settings: " + String.join(", ", options), true);
			}
		});
	}
}
