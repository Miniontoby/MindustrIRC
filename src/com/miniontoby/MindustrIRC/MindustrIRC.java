package com.miniontoby.MindustrIRC;

import com.miniontoby.MindustrIRC.IRCBot;

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
	static public String nickname = Core.settings.getString("ircNickname");
	static public String channel = Core.settings.getString("ircChannel");
	static public String version = "1.0.0";
	static public String ctcp_version = "MindustrIRC v" + version;

	static public void ConsoleLog(String message){
		Log.info("[MindustrIRC] " + message);
	}

	static public void IRCMessage(String message, String to, boolean withThing) {
		try {
			if (!withThing){
				bot.sendMessage(to, message);
			} else {
				bot.sendMessage(to, "[MindustrIRC] " + message);
			}
		} catch (Exception ex){
			ConsoleLog("IRCMessage: Exception: " + ex);
		}
	}
	static public void IRCNotice(String message, String to) {
		try {
			bot.sendNotice(to, message);
		} catch (Exception ex){
			ConsoleLog("IRCNotice: Exception: " + ex);
		}
	}
	static public void ingameMessage(String message) {
		Call.sendMessage(message);

		String ColorCoded = message.replace("[red]", "\u001B[31m").replace("[white]", "\u001B[37m").replace("[grey]", "\u001B[0m").replace("[[", "[");
		Log.info(ColorCoded);
	}

	public static int Seqsize(Iterable data) {
		if (data instanceof Collection) {
			return ((Collection<?>) data).size();
		}
		int counter = 0;
		for (Object i : data) {
			counter++;
		}
		return counter;
	}

	@Override
	public void init(){
		if(Vars.headless){
			ConsoleLog("Loaded!");
			if (Core.settings.has("ircServer") && Core.settings.has("ircPort") && Core.settings.has("ircNickname") && Core.settings.has("ircChannel")) {
				server = Core.settings.getString("ircServer");
				port = Core.settings.getInt("ircPort");
				nickname = Core.settings.getString("ircNickname");
				channel = Core.settings.getString("ircChannel");
			} else {
				Core.settings.put("ircServer", "irc.ircforever.org");
				Core.settings.put("ircPort", 6667);
				Core.settings.put("ircNickname", "MindustrIRC");
				Core.settings.put("ircChannel", "#mindustry");

				server = "irc.ircforever.org";
				port = 6667;
				nickname = "MindustrIRC";
				channel = "#mindustry";
			}
			setupGameListeners();
			connectToIRC();
		}
	}

	static private void connectToIRC() {
		Core.app.post(() -> {
			bot = new IRCBot(server, port, nickname, channel);
			bot.start();
		});
	}

	static private void setupGameListeners(){
		Events.on(GameOverEvent.class, event -> {
			if (Seqsize(Vars.net.getConnections()) != 0){
				IRCMessage("Game over!", channel, true);
			}
		});
		Events.on(WinEvent.class, event -> {
			IRCMessage("Win Event!", channel, true);
		});
		Events.on(LoseEvent.class, event -> {
			IRCMessage("Lose Event!", channel, true);
		});
		Events.on(PlayerChatEvent.class, event -> {
			String playername = event.player.name;
			String message = event.message;
			IRCMessage("<" + playername + "> " + message, channel, false);
		});
		Events.on(PlayerJoin.class, event -> {
			String playername = event.player.name;
			IRCMessage("*** " + playername + " joined the game", channel, false);
		});
		Events.on(PlayerLeave.class, event -> {
			String playername = event.player.name;
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
			String[] split = data[0].split("!");
			String user = split[0].substring(1);
			String message = "";

			switch (data[1]){
				case "PRIVMSG":
					if (data.length >= 4){
						message = data[3].split(":")[1];
						if (message.startsWith("\\001")) {
							switch (message){
								case "\\001VERSION":
									IRCNotice("\001VERSION " + version + "", user); break;
								case "\\001SOURCE":
									IRCNotice("\001SOURCE https://edugit.org/Miniontoby/mindustrirc\\001", user); break;
								default:
									break;
							}
						} else {
							for (int i = 4; i < data.length; i++){ message += " " + data[i]; }

							ingameMessage("[red][[[grey]" + user + "@IRC[red]]:[white] " + message);
							if (data.length >= 5){
								handlePrivmsg(data);
							}
						}
					}
					break;
				case "NOTICE":
					if (data.length >= 4){
						message = data[3].split(":")[1];
						for (int i = 4; i < data.length; i++){ message += " " + data[i]; }

						ingameMessage("[red]-[grey]" + user + "@IRC[red]-[white] " + message);
						if (data.length >= 5){
							handlePrivmsg(data);
						}
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

	static private void handlePrivmsg(String[] data){
		if (data[3].startsWith(":" + nickname)){
			String from = data[2];
			String command = data[4];
			switch (command) {
				case "help":
					IRCMessage("Available commands: help, players, version, source", from, false);
					break;
				case "players":
					Seq<String> players = Seq.with(Vars.net.getConnections()).map(con -> con.player.name).removeAll(p -> p == null);
					IRCMessage("Connected players: " + players.toString(", "), from, false);
					break;
				case "version":
					IRCMessage("Version: " + ctcp_version, from, false);
					break;
				case "source":
					IRCMessage("Source: https://edugit.org/Miniontoby/mindustrirc", from, false);
					break;
				default:
					IRCMessage("Unknown command '" + command + "'. Try 'help'. Or use @playername <message> to send a private message", from, false);
					break;
			}
		}

	}
}
