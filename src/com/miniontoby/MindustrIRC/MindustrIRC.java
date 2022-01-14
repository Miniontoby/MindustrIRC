package com.miniontoby.MindustrIRC;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.Vars.*;

public class MindustrIRC extends Mod {
	public MindustrIRC(){
		Events.on(ServerLoadEvent.class, a -> {
			Log.info("Loaded MindustrIRC constructor.");
		});
		Events.on(PlayerChatEvent.class, e -> {
			Call.sendChatMessage("Hey");
		});
	}

	@Override
	public void loadContent(){
		Log.info("Loading some example content.");
	}
	
	@Override
	public void init(){
  		if(Vars.headless){
		    Log.info("Mod loaded");
  		}
	}
}
