package net.epopy.network.games.tank;

import static net.epopy.epopy.display.components.ComponentsHelper.drawLine;
import static net.epopy.epopy.display.components.ComponentsHelper.drawText;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import net.epopy.epopy.display.Textures;
import net.epopy.epopy.display.components.ComponentsHelper;
import net.epopy.epopy.display.components.ComponentsHelper.PosHeight;
import net.epopy.epopy.display.components.ComponentsHelper.PosWidth;
import net.epopy.epopy.games.gestion.AbstractGameMenu;
import net.epopy.epopy.utils.Input;
import net.epopy.network.NetworkPlayer;
import net.epopy.network.games.AbstractGameNetwork;
import net.epopy.network.games.modules.Ball;
import net.epopy.network.games.modules.PlayerNetwork;
import net.epopy.network.games.tank.modules.CalculTank;
import net.epopy.network.games.tank.modules.MapLoader;
import net.epopy.network.games.tank.modules.Zone;
import net.epopy.network.games.waitingroom.WaitingRoom;
import net.epopy.network.handlers.packets.Packets;
import net.epopy.network.handlers.packets.modules.game.PacketGameStatus;
import net.epopy.network.handlers.packets.modules.game.PacketGameStatus.GameStatus;
import net.epopy.network.handlers.packets.modules.game.PacketPlayerDirection;
import net.epopy.network.handlers.packets.modules.game.PacketPlayerJoin;
import net.epopy.network.handlers.packets.modules.game.PacketPlayerShootBall;

public class Tank extends AbstractGameNetwork {

	public static boolean unloadTexture;
	public static int TANK_SIZE = 25, balls = CalculTank.getMunitions();
	public static MapLoader MAP;

	private boolean shoot, sendRequestPlayer;
	private int timeReload;
	private TankMenuEnd tankMenuEnd;

	public Tank() {
		tankMenuEnd = new TankMenuEnd();
	}
	
	@Override
	public void onEnable() {
	
	}
	
	@Override
	public void update() {
		PlayerNetwork player = getPlayer(NetworkPlayer.getNetworkPlayer().getName());
		Display.setTitle("Epopy - " + NetworkPlayer.getNetworkPlayer().getName());
		if (player != null) {
			if (getGameStatus().equals(GameStatus.IN_GAME) || getGameStatus().equals(GameStatus.WAITING)) {
				if (!sendRequestPlayer) {
					if (getGameStatus().equals(GameStatus.IN_GAME) && NetworkPlayer.getGame().getPlayers().size() < WaitingRoom.waitingRoom.getPlayers().size() * 2 + 2) {
						System.out.println("--> Manque des joueurs");
						Packets.sendPacket(NetworkPlayer.getNetworkPlayer().getNetworkPlayerHandlersGame(), new PacketPlayerJoin(NetworkPlayer.getGame().getPlayer(NetworkPlayer.getNetworkPlayer().getName()).getTeam().getName()));
						sendRequestPlayer = true;
					}
				}
				if (player != null) {
					// rotation du tank
					int rotationSpeed = 5;
					int directionMouse = CalculTank.getDirectionMouse(player.getLocation().getX(), player.getLocation().getY());
					int directionPlayer = player.getLocation().getDirection();
					
					if (!CalculTank.isMouseDistanceNear(player.getLocation())) {
						if (Math.abs(directionMouse - directionPlayer) <= rotationSpeed) {
							if (player.getLocation().getDirection() != directionMouse) {
								player.getLocation().setDirection(directionMouse);
								Packets.sendPacketUDP(NetworkPlayer.getNetworkPlayer().getNetworkPlayerHandlersGame(), new PacketPlayerDirection(directionMouse));
							}
						} else {
							boolean directionInverse = Math.abs(directionMouse - directionPlayer) > 180;
							if (directionPlayer < directionMouse) {
								directionPlayer = directionInverse ? directionPlayer - rotationSpeed : directionPlayer + rotationSpeed;
							} else {
								directionPlayer = directionInverse ? directionPlayer + rotationSpeed : directionPlayer - rotationSpeed;
							}
							if (directionPlayer >= 180) directionPlayer -= 360;
							else if (directionPlayer < -180) directionPlayer += 360;
							if (player.getLocation().getDirection() != directionPlayer) {
								player.getLocation().setDirection(directionPlayer);
								Packets.sendPacketUDP(NetworkPlayer.getNetworkPlayer().getNetworkPlayerHandlersGame(), new PacketPlayerDirection(directionPlayer));
							}
						}
					}
					
					if (!PacketGameStatus.WAITING_MESSAGE.equals("Lancement dans 00:01") &&
							!PacketGameStatus.WAITING_MESSAGE.equals("Lancement dans 00:02") &&
							!PacketGameStatus.WAITING_MESSAGE.equals("Lancement dans 00:03")) {
						// inputs
						if (Input.isKeyDown(Keyboard.KEY_DOWN))
							CalculTank.moove(true);
						else if (Input.isKeyDown(Keyboard.KEY_UP))
							CalculTank.moove(false);
					}
					
					if (timeReload <= 0) {
						if (balls > 0) {
							if (Input.isButtonDown(0)) {
								if (!shoot) {
									timeReload = getTankReload();
									Packets.sendPacket(NetworkPlayer.getNetworkPlayer().getNetworkPlayerHandlersGame(), new PacketPlayerShootBall(player.getLocation()));
									shoot = true;
									balls--;
								}
							} else
								shoot = false;
						}
					} else
						timeReload--;
						
				}
			} else if (getGameStatus().equals(GameStatus.END)) {
				if (tankMenuEnd != null) tankMenuEnd.update();
			}
		}
	}
	
	private static float[] colorReload = new float[] { 0, 0, 0, 1 };
	
	@Override
	public void render() {
		if (unloadTexture) {
			Textures.unloadTextures();
			unloadTexture = false;
		}

		getDefaultBackGround().renderBackground();

		for (Zone zone : getZones())
			zone.render();

		for (PlayerNetwork player : getPlayers())
			player.render();

		for (Ball ball : getBalls())
			ball.render();
		if (getGameStatus().equals(GameStatus.IN_GAME)) {
			drawText(String.valueOf(getTeam("BLUE").getPoints()), AbstractGameMenu.defaultWidth / 2 - 10, 20, PosWidth.DROITE, PosHeight.HAUT, 30, getTeam("BLUE").getColor());
			drawText(String.valueOf(getTeam("RED").getPoints()), AbstractGameMenu.defaultWidth / 2 + 20, 20, PosWidth.GAUCHE, PosHeight.HAUT, 30, getTeam("RED").getColor());
			if (balls <= 0) ComponentsHelper.drawText("Retourner à votre base, pour vous recharger en munitions.", 1920 / 2, 1030, PosWidth.MILIEU, PosHeight.HAUT, 30, new float[] { 1, 0.1f, 0.1f, 1 });
		} else if (getGameStatus().equals(GameStatus.WAITING)) {
			drawText(PacketGameStatus.WAITING_MESSAGE, AbstractGameMenu.defaultWidth / 2 + 10, 40, PosWidth.MILIEU, PosHeight.MILIEU, 18, new float[] { 1, 1, 1, 1 });
			if (balls <= 0) drawText("Retourner à votre base, pour vous recharger en munitions.", 1920 / 2, 1030, PosWidth.MILIEU, PosHeight.HAUT, 30, new float[] { 1, 0.1f, 0.1f, 1 });
		} else if (getGameStatus().equals(GameStatus.END)) {
			if (tankMenuEnd != null) tankMenuEnd.render();
			else tankMenuEnd = new TankMenuEnd();
		}

		if (timeReload > 0) {
			PlayerNetwork player = getPlayer(NetworkPlayer.getNetworkPlayer().getName());
			if (player != null) {
				double y = player.getLocation().getY() - 40;
				drawLine(player.getLocation().getX(), y, player.getLocation().getX() + timeReload, y, 2, colorReload);
				drawLine(player.getLocation().getX(), y, player.getLocation().getX() - timeReload, y, 2, colorReload);
			}
		}

		if (PacketGameStatus.WAITING_MESSAGE.equals("Lancement dans 00:01") || PacketGameStatus.WAITING_MESSAGE.equals("Lancement dans 00:02"))
			drawText("Capturer les zones pour gagner des points !", 1920 / 2, 1030 / 2, PosWidth.MILIEU, PosHeight.HAUT, 30, new float[] { 1, 0.1f, 0.1f, 1 });
			
	}
	
	@Override
	public Textures getDefaultBackGround() {
		return Textures.NETWORK_GAME_TANK_MAP;
	}
	
	public int getTankReload() {

		switch (TankBoutique.LEVEL_CANON) {
			case 0:
			return 50;
			case 1:
			return 45;
			case 2:
			return 40;
			case 3:
			return 35;
			case 4:
			return 30;
			
			default:
			return 50;
		}
		
	}
	
}
