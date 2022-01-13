package com.miniontoby;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

public class MindustrIRC extends Mod {
	public MindustrIRC(){
		Log.info("Loaded ExampleJavaMod constructor.");

		Events.on(PlayerChatEvent.class, e -> {
			
		});
	}

	@Override
	public void loadContent(){
		Log.info("Loading some example content.");
	}
}
