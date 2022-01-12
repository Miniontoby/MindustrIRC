const myDialog = new BaseDialog("Dialog Title");
// Add "go back" button
myDialog.addCloseButton();
// Add text to the main content
myDialog.cont.add("Goodbye.");
// Show dialog
myDialog.show();

let ws = null;
var options = {
	server: "wss://miniontoby.coconut.ircnow.org:8188/webirc/websocket/",
	nick: "MindustrIRC",
	channel: "#ircforever"
};
function connectToIRC(){
	print "Starting up!\n";
	ws = new WebSocket(options.server, "base64");
	ws.onopen = function() {
		retries = 0;
		send( 'NICK '+options.nick+' \n');
		send( 'USER '+options.nick+' * * :MindustrIRC Mod\n');
		inlogcheck = false;
	};
	ws.onmessage = function(content) {
		var messageOutput6 = strToUTF8Arr(content.data);
		var messageOutput7 = base64EncArr(messageOutput6);
		var messageoutput = content.data;

		var array = messageoutput.split(" ");
		var server = array[0];
		var nickname = array[0].split("!")[0].replace(":", "");
		var action = array[1];
		var userchannel = array[2];
		if(array[3] != undefined) {
			var param = array[3].substr(1,array[3].length) + " ";
			for(var i = 4;i<array.length;i++) {
				param += array[i] + " ";
			}
		}
		var e = window.atob(messageOutput7);
		if ( e.match( /^PING (\S*)/i ) ) {
			send( 'PONG ' + RegExp.$1 + '\n' );
		}
		if(e.indexOf("Found your hostname") != -1 || e.indexOf("No Ident response") != -1) {
			if(inlogcheck) {
				send( 'NICK '+options.nick+' \n');
				send( 'USER '+options.nick+' * * :MindustrIRC Mod\n');
				inlogcheck = false;
			}
		}
		if(param != undefined && param.indexOf("MODE "+options.nick+" :+i") != -1) {
			send( 'JOIN ' + options.channel +'\n' );
		}
		if(action == "JOIN" && nickname == options.nick) {
			mejoin(e);
		} else if(action == "JOIN" && nickname != options.nick) {
			status( nickname + " joined " + options.channel,false);
		}
		if(action == "PART" && nickname != options.nick) {
			if(param != undefined) {
				status(nickname + " lefted " +options.channel+": "+param, false);
			} else {
				status(nickname + " lefted " + options.channel, false);
			}
		}
		if(action == "PRIVMSG") {
			privmsg(nickname, param);
		}
		if (e.indexOf("is your displayed hostname now") != -1){
			send( 'JOIN ' + options.channel + '\n' );
		}

	};
	ws.onclose = function() { retryOpeningWebSocket(); };

	if (timer) clearTimeout(timer);
	timer = setInterval( ping, 100000 );
}
function send(data) {
	ws.send(data);
}
connectToIRC();


Events.on(GameOverEvent, event => {
	console.log(event);
})
Events.on(LoseEvent, event => {
	console.log(event);
})
Events.on(PlayerChatEvent, event => {
	console.log(event);
})
Events.on(PlayerLeaveEvent, event => {
	console.log(event);
})
Events.on(PlayerJoinEvent, event => {
	console.log(event);
})
Events.on(WinEvent, event => {
	console.log(event);
})

