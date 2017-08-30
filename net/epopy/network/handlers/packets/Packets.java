package net.epopy.network.handlers.packets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.epopy.network.Logger;
import net.epopy.network.handlers.NetworkPlayerHandlers;
import net.epopy.network.handlers.packets.modules.PacketPlayerDisconnect;
import net.epopy.network.handlers.packets.modules.PacketPlayerFriends;
import net.epopy.network.handlers.packets.modules.PacketPlayerLogin;
import net.epopy.network.handlers.packets.modules.PacketPlayerMessage;
import net.epopy.network.handlers.packets.modules.PacketPlayerRequest;
import net.epopy.network.handlers.packets.modules.PacketPlayerStatsNetwork;
import net.epopy.network.handlers.packets.modules.PacketPlayerTChat;
import net.epopy.network.handlers.packets.modules.PacketPlayerWaitingRoom;
import net.epopy.network.handlers.packets.modules.game.PacketGameStatus;
import net.epopy.network.handlers.packets.modules.game.PacketPlayerJoin;
import net.epopy.network.handlers.packets.modules.game.PacketPlayerLeave;
import net.epopy.network.handlers.packets.modules.game.PacketPlayerLocation;
import net.epopy.network.handlers.packets.modules.game.PacketPlayerMove;
import net.epopy.network.handlers.packets.modules.game.PacketPlayerShootBall;
import net.epopy.network.handlers.packets.modules.game.PacketTeamCaptureZone;
import net.epopy.network.handlers.packets.modules.game.PacketTeamPoints;

public class Packets {
	
	public static int MAX_SIZE = 512;
	private static Map<String, Class<?>> packets;
	
	public Packets() {
		if (packets == null) {
			packets = new HashMap<>();
			packets.put(PacketPlayerLogin.class.getSimpleName(), PacketPlayerLogin.class);
			packets.put(PacketPlayerFriends.class.getSimpleName(), PacketPlayerFriends.class);
			packets.put(PacketPlayerWaitingRoom.class.getSimpleName(), PacketPlayerWaitingRoom.class);
			packets.put(PacketPlayerRequest.class.getSimpleName(), PacketPlayerRequest.class);
			packets.put(PacketPlayerMessage.class.getSimpleName(), PacketPlayerMessage.class);
			packets.put(PacketPlayerDisconnect.class.getSimpleName(), PacketPlayerDisconnect.class);
			packets.put(PacketPlayerTChat.class.getSimpleName(), PacketPlayerTChat.class);
			packets.put(PacketPlayerStatsNetwork.class.getSimpleName(), PacketPlayerStatsNetwork.class);

			// Game
			packets.put(PacketPlayerLocation.class.getSimpleName(), PacketPlayerLocation.class);
			packets.put(PacketPlayerMove.class.getSimpleName(), PacketPlayerMove.class);
			packets.put(PacketPlayerJoin.class.getSimpleName(), PacketPlayerJoin.class);
			packets.put(PacketPlayerLeave.class.getSimpleName(), PacketPlayerLeave.class);
			packets.put(PacketPlayerShootBall.class.getSimpleName(), PacketPlayerShootBall.class);
			packets.put(PacketTeamPoints.class.getSimpleName(), PacketTeamPoints.class);
			packets.put(PacketTeamCaptureZone.class.getSimpleName(), PacketTeamCaptureZone.class);
			packets.put(PacketGameStatus.class.getSimpleName(), PacketGameStatus.class);

			Logger.info("[Packets] init packet (" + packets.size() + ")");
		}
	}
	
	public static void sendPacket(final NetworkPlayerHandlers clientHandlers, final PacketAbstract packetAbstract) {
		if (packetAbstract.getPacket().getData().length > 0 && clientHandlers != null && clientHandlers.getDataOutputStream() != null) {
			try {
				clientHandlers.getDataOutputStream().write(packetAbstract.getPacket().getData());
				clientHandlers.getDataOutputStream().flush();
				// Logger.info("[Packet] Type: send || Name: " + packetAbstract.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void sendPacketUDP(final NetworkPlayerHandlers clientHandlers, final PacketAbstract packetAbstract) {
		if (packetAbstract.getPacket().getData().length > 0 && clientHandlers != null && clientHandlers.getDataOutputStream() != null)
			clientHandlers.getServerUDP().send(packetAbstract.getPacket().getData());
	}
	
	public static PacketAbstract getPacket(final String name) {
		if (packets.containsKey(name)) {
			try {
				return (PacketAbstract) packets.get(name).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
}
