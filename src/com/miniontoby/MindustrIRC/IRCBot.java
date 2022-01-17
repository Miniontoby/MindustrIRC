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

	private void joinChannel(String channel) throws Exception {
		output.write("JOIN " + channel + "\n");
		output.flush();
	}

	public void sendMessage(String to, String message) throws Exception {
		output.write("PRIVMSG " + to + " :" + message + "\n");
		output.flush();
	}
	public void sendNotice(String to, String message) throws Exception {
		output.write("NOTICE " + to + " :" + message + "\n");
		output.flush();
	}

/*
	private boolean isCommand(String[] data) {
		if (data.length >= 4) {
			if (data[1].equals("PRIVMSG")) {
				if (data[3].substring(1, 2).equals("!")) {
					String command = data[3].substring(2);
					return true;
				}
			}
		}
		return false;
	}
*/

	public void run() {
		try {
			connect();
			sendLoginData();

			while (true) {
				String data = null;
				while ((data = input.readLine()) != null) {
					String[] dataSplitted = data.split(" ");
					if (dataSplitted[0].equals("PING")) {
						output.write("PONG " + dataSplitted[1] + "\n");
						output.flush();
					}
					if (dataSplitted.length >= 2) {
						if (dataSplitted[1].equals("376")) {
							joinChannel(defaultChannel);
							sendMessage(defaultChannel, "[MindustrIRC] Server Started!");
							MindustrIRC.ConsoleLog("Connected to IRC!");
						}
					}
					MindustrIRC.handleIRCMessage(dataSplitted);
				}
			}
		} catch (Exception ex) {
			System.out.println("IRCBot.run(): Exception: " + ex.getMessage());
		}
	}
}
