package net.epopy.network.games;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.epopy.epopy.display.Textures;
import net.epopy.network.games.modules.Ball;
import net.epopy.network.games.modules.Location3D;
import net.epopy.network.games.modules.PlayerNetwork;
import net.epopy.network.games.modules.Team;
import net.epopy.network.games.tank.Tank;
import net.epopy.network.games.tank.modules.Zone;
import net.epopy.network.handlers.packets.modules.game.PacketGameStatus.GameStatus;

public abstract class AbstractGameNetwork {

	private static Map<String, PlayerNetwork> players = new TreeMap<>();
	private static Map<String, PlayerNetwork> playersADD = new TreeMap<>();
	private static List<String> playersREMOVE = new LinkedList<>();
	
	private static Map<String, Team> teams = new TreeMap<>(); // STRING = nom de la team
	private static Map<String, Ball> balls = new TreeMap<>(); // STRING = nom de la team
	private static Map<String, Zone> zones = new TreeMap<>();//INT = id de la zone
	private static Map<String, Zone> zoneADD = new TreeMap<>();

	private static GameStatus gameStatus = GameStatus.WAITING;

	public void clear() {
		Tank.unloadTexture = true;
		players.clear();
		teams.clear();
		balls.clear();
		zones.clear();
		gameStatus = GameStatus.WAITING;
		System.out.println("[Server - Network] Datas have been clear !");
	}

	private static void updatePlayer() {
		if(!playersADD.isEmpty()) {
			for(Entry<String, PlayerNetwork> player : playersADD.entrySet()) 
				players.put(player.getKey(), player.getValue());
			playersADD.clear();
		}
		if(!playersREMOVE.isEmpty()) {
			for(String player : playersREMOVE)
				players.remove(player);
			playersREMOVE.clear();
		}
	}

	/*
	 * Variables commmumes
	 */
	//----- GAME -----
	public GameStatus getGameStatus() {
		return gameStatus;
	}
	public void setGameStatus(GameStatus g) {
		gameStatus = g;
	}
	// ----- JOUEURS -----
	public void addPlayer(final String name, final String teamName, int hp) {
		PlayerNetwork player = new PlayerNetwork(name, teams.get(teamName), hp);
		updatePlayer();
		if (!players.containsKey(name)) 
			playersADD.put(name, player);
	}

	public void removePlayer(final String name) {
		updatePlayer();
		if (players.containsKey(name))
			playersREMOVE.add(name);
		updatePlayer();
	}

	public PlayerNetwork getPlayer(final String name) {
		updatePlayer();
		return players.get(name);
	}

	public boolean containsPlayer(final String name) {
		updatePlayer();
		return players.containsKey(name);
	}

	public Collection<PlayerNetwork> getPlayers() {
		updatePlayer();
		return players.values();
	}

	// ----- TEAMS -----
	// Pour ajouter une team: name = nom de la team
	public void addTeam(final String name, final Team team) {
		if (!containsTeam(name))
			teams.put(name, team);
	}

	public void removeTeam(final String name) {
		if (teams.containsKey(name))
			teams.remove(name);
	}

	public boolean containsTeam(final String name) {
		return teams.containsKey(name);
	}

	public Team getTeam(final String name) {
		if (teams.containsKey(name))
			return teams.get(name);
		return null;
	}
	// ----- BALLS -----

	public void updateBall(final String name, final float[] color, final Location3D location3d) {
		if (balls.containsKey(name))
			balls.get(name).setLocation3d(location3d);
		else
			balls.put(name, new Ball(name, color, location3d));
	}

	public void removeBall(final String name) {
		balls.remove(name);
	}

	public List<Ball> getBalls() {
		List<Ball> clients = new ArrayList<>(balls.size());
		for (Entry<String, Ball> value : balls.entrySet())
			clients.add(value.getValue());
		return clients;
	}

	// public Collection<Ball> getBalls() {
	// return balls.values();
	// }

	// ----- ZONE -----
	public void removeZone(int id) {
		if (zones.containsKey(id))
			zones.remove(id);
	}

	private void updateZone() {
		if(!zoneADD.isEmpty()) {
			for(Entry<String, Zone> zone : zoneADD.entrySet()) 
				zones.put(zone.getKey(), zone.getValue());
			zoneADD.clear();
		}
	}
	
	public void addZone(String id) {
		updateZone();
		if (!zones.containsKey(id))
			zoneADD.put(id, new Zone(id));
	}

	public Collection<Zone> getZones() {
		updateZone();
		return zones.values();
	}

	public boolean containsZone(String id) {
		updateZone();
		return zones.containsKey(id);
	}

	public Zone getZone(String id) {
		updateZone();
		if (zones.containsKey(id))
			return zones.get(id);
		return null;
	}
	/*
	 * abstract
	 */
	public abstract void onEnable();

	public abstract void update();

	public abstract void render();

	public abstract Textures getDefaultBackGround();

	public String getName() {
		return getClass().getSimpleName();
	}
}
