# MindustrIRC

## What?

MindustrIRC is an IRC Plugin for the game [Mindustry](https://mindustrygame.github.io)


## What does it do?

It relays all messages which are send in the game to the IRC channel and the other way round


It also relays JOIN, PART, QUIT, NICK and NOTICE


It also comes with some IRC sided commands, like `players`. To use them send at IRC: `<YOURBOTNICK>: <command>`


## How to install?

To install this mod, go to your `config/mods/` folder and run the following command in shell:

$ wget "https://edugit.org/Miniontoby/mindustrirc/-/raw/main/build/libs/mindustrircDesktop.jar?inline=false"


## How to use?

At default it will connect to irc.ircforever.org:6667 #mindustry 

Using the Core.settings I have done that, but I don't know how to change it from commandline atm... Will change it soon

You can change the code to change the server address and then build the .jar with the `./update.sh`


## How to stop your server again?

At the moment I dont have it auto stop on the `exit` command, but I am still working on it!

So just execute the `exit` command and then CTRL-C to stop the server



