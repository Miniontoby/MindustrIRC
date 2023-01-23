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
	private String realname;
	private String password;
	private String defaultChannel;
	private String orignick;

	private Socket connection;
	private BufferedReader input;
	private BufferedWriter output;

	private boolean tryToReconnect = false;
	private boolean running = false;
	private Thread heartbeatThread;
	private long heartbeatDelayMillis = 5000;

	public IRCBot(String NETWORK, int PORT, String nickname, String realname, String password, String defaultChannel) {
		this.NETWORK = NETWORK;
		this.PORT = PORT;
		this.nickname = nickname;
		this.realname = realname;
		this.password = password;
		this.defaultChannel = defaultChannel;
	}

	private void connect() throws Exception {
		createconnect();
		heartbeatThread = new Thread() {
			public void run() {
				while (tryToReconnect) {
					try {
						output.write("PING " + nickname + "\r\n");
						sleep(heartbeatDelayMillis);
					} catch (InterruptedException e) {
						MindustrIRC.ConsoleLog("Interrupted!");
						shutdown();
						tryToReconnect = false;
						break;
					} catch (java.net.SocketException e) {
						disconnect();
						createconnect();
					} catch (IOException e) {
						disconnect();
						createconnect();
					} catch (Exception e) {
						MindustrIRC.ConsoleLog("Error: " + e + "\n");
					}
				}
			};
		};
		heartbeatThread.start();
	}

	private void createconnect() {
		try {
			connectAndLogin();
		} catch (Exception e) {
			MindustrIRC.ConsoleLog("[IRCBot.createconnect()] Error: " + e);
			shutdown();
		}
	}

	public void shutdown() {
		try {
			tryToReconnect = false;
			disconnect();
			if (heartbeatThread != null) heartbeatThread.interrupt();
		} catch (Exception e) {
			MindustrIRC.ConsoleLog("[IRCBot.shutdown()] Error: " + e + "\n");
		}
	}

	public void disconnect() {
		try {
			if (output != null) output.close();
			if (input != null) input.close();
			if (connection != null) connection.close();
			running = false;
		} catch (java.net.SocketException e) {
			// It is already closed.
		} catch (Exception e) {
			MindustrIRC.ConsoleLog("[IRCBot.disconnect()] Error: " + e + "\n");
		}
	}

	private void sendLoginData() throws Exception {
		output.write("NICK " + nickname + "\r\n");
		output.write("USER " + nickname + " * * :" + realname + "\r\n");
		output.flush();
	}

	private void joinChannel(String channel) throws Exception {
		output.write("JOIN " + channel + "\r\n");
		output.write("PRIVMSG NickServ :identify " + password + "\r\n");
		output.flush();
	}

	public void sendMessage(String to, String message) throws Exception {
		output.write("PRIVMSG " + to + " :" + message + "\r\n");
		output.flush();
	}
	public void sendNotice(String to, String message) throws Exception {
		output.write("NOTICE " + to + " :" + message + "\r\n");
		output.flush();
	}

	public void run() {
		try {
			tryToReconnect = true;
			connect();
		} catch (Exception ex) {
			MindustrIRC.ConsoleLog("[IRCBot.run()] Exception: " + ex.getMessage());
		}
	}

	public void connectAndLogin() throws Exception {
		connection 	= new Socket(NETWORK, PORT);
		output 		= new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
		input 		= new BufferedReader(new InputStreamReader(connection.getInputStream()));

		sendLoginData();
		String line = null;
		while ((line = input.readLine()) != null) {
			if (line.indexOf("004") >= 0) {
				break;
			}
			else if (line.indexOf("433") >= 0) {
				MindustrIRC.ConsoleLog("Nickname is already in use. Trying again in 10 seconds", true);
				if (orignick == null || orignick == nickname) {
					nickname += "_";
					orignick = nickname;
				} else {
					nickname = orignick;
				}
				setTimeout(() -> disconnect(), 10000);
				return;
			}
			else if (line.toUpperCase().startsWith("PING ")) {
				output.write("PONG " + line.substring(5) + "\r\n");
				output.flush();
			}
		}
		running = true;

		joinChannel(defaultChannel);
		if (!MindustrIRC.started) {
			sendMessage(defaultChannel, "[MindustrIRC] Server Started");
		}
		MindustrIRC.ConsoleLog("Connected to IRC!");

		Runnable backGroundRunnable = new Runnable() {
		public void run(){
			try {
				String line = null;
				while ((line = input.readLine()) != null && running) {
					if (!running) break;

					String[] dataSplitted = line.split(" ");
					if (dataSplitted[0].equals("PING")) {
						output.write("PONG " + dataSplitted[1] + "\r\n");
						output.flush();
					}
					if (dataSplitted[0].equals("QUIT")) {
						MindustrIRC.ConsoleLog("Received a quit to IRC!");
						if (dataSplitted[1].equals(nickname)) {
							MindustrIRC.ConsoleLog("It was me!");
							running = false;
							break;
						}
					}
					MindustrIRC.handleIRCMessage(dataSplitted);
				}
				disconnect();
			} catch (Exception e){}
		}};
		Thread sampleThread = new Thread(backGroundRunnable);
		sampleThread.start();
	}
	public static void setTimeout(Runnable runnable, int delay){
		new Thread(() -> {
			try {
				Thread.sleep(delay);
				runnable.run();
			}
			catch (Exception e){
				System.err.println(e);
			}
		}).start();
	}
}
