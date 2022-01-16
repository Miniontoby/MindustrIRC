package com.miniontoby.MindustrIRC;
import com.miniontoby.MindustrIRC.IRCBot;

import java.net.*;
import java.io.*;
import java.util.*;
import arc.*;
import arc.util.*;
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

	static public void ConsoleLog(String message){
		Log.info("[MindustrIRC] " + message);
	}

	static public void IRCMessage(String message) {
		try {
			bot.sendMessage(channel, message);
//			bot.sendMessage("#mindustry", "[MindustrIRC] " + message);
		} catch (Exception ex){
			ConsoleLog("Exception: " + ex);
		}
	}

	@Override
	public void init(){
		if(Vars.headless){
			ConsoleLog("Loaded!");
//			if (empty(server) || empty(port) || empty(nickname) || empty(channel)) {
				Core.settings.put("ircServer", "irc.ircforever.org");
				Core.settings.put("ircPort", 6667);
				Core.settings.put("ircNickname", "MindustrIRC");
				Core.settings.put("ircChannel", "#mindustry");

				String server = Core.settings.getString("ircServer");
				port = Core.settings.getInt("ircPort");
				nickname = Core.settings.getString("ircNickname");
				channel = Core.settings.getString("ircChannel");
//			}
			setupListeners();
			connectToIRC();
		}
	}

	static void connectToIRC() {
		Core.app.post(() -> {
			bot = new IRCBot(server, port, nickname, channel);
			bot.start();
		});
	}
	static private void setupListeners(){
		Events.on(GameOverEvent.class, event -> {
			IRCMessage("Game over!");
		});
		Events.on(WinEvent.class, event -> {
			IRCMessage("Win Event!");
		});
		Events.on(LoseEvent.class, event -> {
			IRCMessage("Lose Event!");
		});
		Events.on(PlayerChatEvent.class, event -> {
			String playername = event.player.name;
			String message = event.message;
			IRCMessage("<" + playername + "> " + message);
		});
		Events.on(PlayerJoin.class, event -> {
			String playername = event.player.name;
			IRCMessage("*** " + playername + " joined the game");
		});
		Events.on(PlayerLeave.class, event -> {
			String playername = event.player.name;
			IRCMessage("*** " + playername + " left the game");
		});
	}
}
