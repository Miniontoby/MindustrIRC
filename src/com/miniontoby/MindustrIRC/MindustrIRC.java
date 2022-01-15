package com.miniontoby.MindustrIRC;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;

public class MindustrIRC extends Plugin {
	public MindustrIRC(){
//		Events.on(ServerLoadEvent.class, a -> {
//			Log.info("Loaded MindustrIRC constructor.");
//		});
		Events.on(PlayerChatEvent.class, e -> {
			Call.sendChatMessage("Hey");
		});
	}

	@Override
	public void init(){
		if(Vars.headless){
			Log.info("MindustrIRC Mod loaded!");
		}
	}
}
