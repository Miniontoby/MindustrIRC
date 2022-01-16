package com.miniontoby.MindustrIRC;

import java.io.*;
import java.net.*;
import java.util.*;
import arc.*;
import arc.util.*;
import arc.struct.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;

public class IRCBot extends Thread {
	private final String NETWORK;
	private final int PORT;
	private String nickname;
	private String defaultChannel;
	private Socket connection;
	private BufferedReader input;
	private BufferedWriter output;

	public IRCBot(String NETWORK, int PORT, String nickname, String defaultChannel) {
		this.NETWORK = NETWORK;
		this.PORT = PORT;
		this.nickname = nickname;
		this.defaultChannel = defaultChannel;
	}

	private void connect() throws Exception {
		connection = new Socket(NETWORK, PORT);
		input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		output = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
	}

	private void disconnect() throws Exception {
		connection.close();
	}

	private void sendLoginData() throws Exception {
		output.write("NICK " + nickname + "\n");
		output.write("USER " + nickname + " * * : " + nickname + "\n");
		output.flush();
	}

	private void pingPong(String[] data) throws Exception {
		if (data[0].equals("PING")) {
			output.write("PONG " + data[1] + "\n");
			output.flush();
		}
	}

	private void joinChannel(String channel) throws Exception {
		output.write("JOIN " + channel + "\n");
		output.flush();
	}

	public void sendMessage(String to, String message) throws Exception {
		output.write("PRIVMSG " + to + " :" + message + "\n");
		output.flush();
	}

	private void verifyMOTD(String[] data) throws Exception {
		if (data.length >= 2) {
			// 376 is the protocol number (end of MOTD)
			if (data[1].equals("376")) {
				joinChannel(defaultChannel);
				sendMessage(defaultChannel, "[MindustrIRC] Server Started!");
				MindustrIRC.ConsoleLog("Connected to IRC!");
			}
		}
	}

	private boolean isCommand(String[] data) {
		if (data.length >= 4) {
			if (data[1].equals("PRIVMSG")) {
				if (data[3].substring(1, 2).equals("!")) {
					return true;
				}
			}
		}
		return false;
	}

	private void verifyCommand(String[] data) throws Exception {
		if (isCommand(data)) {
			String from = data[2];
			String command = data[3].substring(2);
			switch (command) {
				case "help":
					sendMessage(from, "Available commands: help, players");
					break;
				case "players":
					Seq<String> players = Seq.with(Vars.net.getConnections()).map(con -> con.player.name).removeAll(p -> p == null);
					sendMessage(from, "Connected players: " + players.toString(", "));
					break;
				case "avapro":
					sendMessage(from, String.valueOf(Runtime.getRuntime().availableProcessors()));
					break;
				case "freememory":
					sendMessage(from, String.valueOf(Runtime.getRuntime().freeMemory()));
					break;
				case "totalmemory":
					sendMessage(from, String.valueOf(Runtime.getRuntime().totalMemory()));
					break;
				default:
					sendMessage(from, "exception -> unknown function: \"" + command + "\"");
					break;
			}
		}
	}

	public void run() {
		try {
			connect();
			sendLoginData();

			while (true) {
				String data = null;
				while ((data = input.readLine()) != null) {
					String[] dataSplitted = data.split(" ");
					pingPong(dataSplitted);
					verifyMOTD(dataSplitted);
					sendToChat(dataSplitted);
					try {
						verifyCommand(dataSplitted);
					} catch (Exception ex) {
					}
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	private void sendToChat(String[] data){
		if (data.length >= 4) {
			if (data[1].equals("PRIVMSG")) {
				String[] split = data[0].split("!");
				String user = split[0].substring(1);
				String message = data[3].split(":")[1];
				for (int i = 4; i < data.length; i++){
					message = message + " " + data[i];
				}
				Call.sendMessage("[red][[[grey]" + user + "@IRC[red]][white] " + message);
			} else if (data[1].equals("JOIN")) {
				String[] split = data[0].split("!");
				String user = split[0].substring(1);
				String chn = data[1];
				Call.sendMessage("[grey]-!- " + user + " joined " + chn);
			} else if (data[1].equals("PART")) {
				String[] split = data[0].split("!");
				String user = split[0].substring(1);
				String chn = data[2];
				String reason = data[3];
				Call.sendMessage("[grey]-!- " + user + " has left " + chn + " [" + reason + "]");
			} else if (data[1].equals("QUIT")) {
				String[] split = data[0].split("!");
				String user = split[0].substring(1);
				String reason = data[2];
				Call.sendMessage("[grey]-!- " + user + " has quit [" + reason + "]");
			} else if (data[1].equals("NICK")) {
				String[] split = data[0].split("!");
				String oldUser = split[0].substring(1);
				String newUser = data[2];
				Call.sendMessage("[grey]-!- " + oldUser + " is now known as " + newUser);
			}
		}
	}
}
