package com.miniontoby;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;

public class MindustrIRC extends Mod {
	public MindustrIRC(){
		Events.on(ClientLoadEvent.class, a -> {
			Vars.enableConsole = true;
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
}
