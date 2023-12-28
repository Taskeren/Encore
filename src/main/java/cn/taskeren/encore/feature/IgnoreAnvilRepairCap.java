/*
 * Copyright (c) 2024 Taskeren and Contributors - All Rights Reserved.
 */

package cn.taskeren.encore.feature;

import cn.taskeren.encore.Encore;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class IgnoreAnvilRepairCap extends Feature {

	public IgnoreAnvilRepairCap(Encore encore) {
		super("ignore-anvil-repair-cap", encore);
		registerAsListener();
		syncEnablingStatus();
	}

	@Override
	public void onEncoreEnabled() {
		// listen to the window close packets, and re-sync the gamemode
		getEncore().getProtocolManager().addPacketListener(new PacketAdapter(
				getEncore(),
				ListenerPriority.NORMAL,
				PacketType.Play.Client.CLOSE_WINDOW
		) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				var player = event.getPlayer();
				getEncore().getProtocolManager().sendServerPacket(player, getGameModeChangePacket(player.getGameMode()));
			}
		});
	}

	@EventHandler
	public void onAnvil(PrepareAnvilEvent event) {
		// skip if not enabled
		if(!isEnabled()) return;

		var inv = event.getInventory();
		var player = (Player) event.getViewers().get(0);

		// remove the cap
		inv.setMaximumRepairCost(Integer.MAX_VALUE);

		// if the player cannot afford the experience cost, just skip
		if(inv.getRepairCost() > player.getLevel()) return;

		// else
		// "virtually" set the gamemode of the player to creative.
		// this is a workaround to fix the "Too expensive" problem,
		// because when the player is in creative mode, the text will not be shown.
		// the gamemode will be re-synced when the player closes the screen, listening to the events above.
		if(inv.getRepairCost() > 0) {
			getEncore().getProtocolManager().sendServerPacket(player, getGameModeChangePacket(GameMode.CREATIVE));
		}
	}

	/**
	 * Creates a Game State Change packet of Game Mode Change, and write with the given game mode.
	 *
	 * @param gameMode the given gamemode
	 * @return the ready-to-send packet
	 */
	@SuppressWarnings("deprecation")
	private static PacketContainer getGameModeChangePacket(GameMode gameMode) {
		var container = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
		container.getGameStateIDs().write(0, 3); // Magic const 3 is the ID of the gamemode change.
		container.getFloat().write(0, (float) gameMode.getValue());
		return container;
	}
}
