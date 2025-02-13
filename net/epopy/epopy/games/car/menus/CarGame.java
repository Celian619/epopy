package net.epopy.epopy.games.car.menus;

import static net.epopy.epopy.display.components.ComponentsHelper.drawLine;
import static net.epopy.epopy.display.components.ComponentsHelper.drawQuad;
import static net.epopy.epopy.display.components.ComponentsHelper.drawText;
import static net.epopy.epopy.display.components.ComponentsHelper.getResponsiveX;
import static net.epopy.epopy.display.components.ComponentsHelper.getResponsiveY;
import static net.epopy.epopy.display.components.ComponentsHelper.renderTexture;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.Timer;

import net.epopy.epopy.Main;
import net.epopy.epopy.audio.Audios;
import net.epopy.epopy.display.Textures;
import net.epopy.epopy.display.components.ComponentsHelper;
import net.epopy.epopy.display.components.ComponentsHelper.PosHeight;
import net.epopy.epopy.display.components.ComponentsHelper.PosWidth;
import net.epopy.epopy.games.gestion.AbstractGameMenu;
import net.epopy.epopy.games.gestion.GameList;
import net.epopy.epopy.utils.Input;
import net.epopy.epopy.utils.Location;

public class CarGame extends AbstractGameMenu {
	
	private static boolean pauseScreen;
	private static int timer;

	private final int grilleWidth = 20, grilleHeight = 10, bord = grilleWidth / 10, middleWidth = grilleWidth / 2, middleHeight = grilleHeight / 2;
	private final double cubeWidth = defaultWidth / (double) grilleWidth, cubeHeight = defaultHeight / (double) grilleHeight;
	
	private boolean creating, start, contreSens, addStats;
	private int direction, lastTimeLine;
	private double speed;
	private Location locCar;
	private List<Location> pointsInt, waitingPoints;
	private Textures map;
	
	@Override
	public void onEnable() {
		if (Main.getPlayer().hasSound() && !Audios.CAR.isRunning())
			Audios.CAR.start(true).setVolume(0.3f);
		song = Audios.CAR;
		
		Mouse.setGrabbed(true);
		pauseScreen = addStats = win = contreSens = start = false;
		creating = true;
		direction = timer = 0;
		speed = 0.1;
		map = null;
		locCar = new Location(middleWidth * cubeWidth - 17.5, middleHeight * cubeHeight + cubeHeight / 2);
		
		gameStats = Main.getPlayer().getCarStats();
		
		pointsInt = new LinkedList<Location>();//
		waitingPoints = new LinkedList<Location>();
		for (int x = middleWidth - 5; x < middleWidth + 5; x++) {
			pointsInt.add(new Location(x, middleHeight));
			if (x != middleWidth)
				waitingPoints.add(new Location(x, middleHeight));
		}
	}
	
	@Override
	public void update() {
		if (!creating && !pauseScreen && pause.isFinish() && !win) {
			timer++;
		}
		
		if (pause.isFinish() && !win) {
			if (Input.getKeyDown(Keyboard.KEY_ESCAPE)) {
				if (pauseScreen) {
					pauseScreen = false;
					pause.startPause(3);
					Mouse.setGrabbed(true);
					start = false;
				} else {
					pauseScreen = true;
					Mouse.setGrabbed(false);
				}
				
			}
		}
		
		// fin de la creation
		if (waitingPoints.size() == 0 && creating) {
			creating = false;
			pointsInt.clear();
			waitingPoints.clear();
			saveScreen();
			pause.startPause(5);
		}
		
		if (creating) {
			
			if (Input.isAnyKeyDown()) {
				while (waitingPoints.size() != 0)
					upgradeMap();
					
			} else
				upgradeMap();
		} else if (win || pauseScreen) {
			if (win) {
				if (Mouse.isGrabbed())
					Mouse.setGrabbed(false);
				if (rejouerButton.isClicked())
					onEnable();
			} else if (reprendreButton.isClicked()) {
				pauseScreen = false;
				pause.startPause(3);
				Mouse.setGrabbed(true);
				start = false;
			}
			return;
		} else if (pause.isFinish()) {
			if (!start) {
				start = true;
			}
			movePlayer();
		}
		Timer.tick();
		
	}
	
	@Override
	public void render() {
		drawQuad(0, 0, defaultWidth, defaultHeight);
		
		if (creating) {
			List<Location> pointsExt = new LinkedList<Location>();// exterieur
			
			for (int x = 0; x <= grilleWidth; x++) {
				for (int y = 0; y <= grilleWidth; y++) {
					Location loc = new Location(x, y);
					if (loc.getNearest(pointsInt).distance(loc) > 0) pointsExt.add(loc);
				}
			}
			
			// la ligne de départ :
			drawLine(middleWidth * cubeWidth, middleHeight *
					cubeHeight, middleWidth * cubeWidth, (middleHeight + 1) * cubeHeight, 8, new float[] { 0f, 0f, 1, 1 });
					
			paintLiaisons(pointsInt);
			paintLiaisons(pointsExt);
			
		} else {
			
			map.renderBackground();
			
			if (win) {
				
				String timeString = timer / 60 + " sec";
				boolean record = timer / 60 <= gameStats.getRecord() || gameStats.getRecord() == 0;
				renderEchap(false, timeString, record);
				if (!addStats) {
					addStats = true;
					if (record)
						gameStats.setRecord(timer / 60);
						
					if (gameStats.getRecord() <= gameStats.getObjectif()) {
						if (Main.getPlayer().getLevel() <= GameList.CAR.getID())
							Main.getPlayer().setLevel(GameList.CAR.getID() + 1);
					}
					gameStats.addPartie();
					gameStats.addTemps(timer / 60);
				}
				return;
			} else if (pauseScreen) {
				renderEchap(true);
				return;
			}
			
			renderTexture(Textures.GAME_CAR_VOITURE, locCar.getX() - 17.5, locCar.getY() - 8, 35, 16, direction);
			
			if (contreSens) {
				contreSens = false;
				drawText("Tricher, c'est mal !", defaultWidth / 2, defaultHeight - 50, PosWidth.MILIEU, PosHeight.MILIEU, 40, new float[] { 1, 0, 0, 1 });
			}
			if (!pause.isFinish()) {
				if (Input.isAnyKeyDown() && !Input.isKeyDown(Keyboard.KEY_ESCAPE) && !Input.getKeyUp(Keyboard.KEY_ESCAPE)) {
					pause.stopPause();
					return;
				}
				
				if (pause.getTimePauseTotal() == 5) {
					
					Textures.GAME_STARTING_BG.renderBackground();
					
					float[] orange = new float[] { 1, 0.5f, 0, 1 };
					drawText("CONTROLES", 1093, 370, PosWidth.MILIEU, PosHeight.MILIEU, 30, orange);
					drawText("Droite", 1093, 410, PosWidth.MILIEU, PosHeight.HAUT, 25);
					drawText("Gauche", 1093, 540, PosWidth.MILIEU, PosHeight.HAUT, 25);
					
					float[] white = new float[] { 1, 1, 1, 1 };
					drawText(Input.getKeyName(CarOptions.KEY_RIGHT), 1093, 475, PosWidth.MILIEU, PosHeight.MILIEU, 50, white);
					drawText(Input.getKeyName(CarOptions.KEY_LEFT), 1093, 600, PosWidth.MILIEU, PosHeight.MILIEU, 50, white);
					
					drawText("OBJECTIF", 660, 495, 30, orange);
					float[] grey = new float[] { 0.8f, 0.8f, 0.8f, 1 };
					
					if (gameStats.getRecord() < gameStats.getObjectif() && gameStats.getRecord() != 0) {
						drawText("Réussi !", 710, 615, PosWidth.MILIEU, PosHeight.HAUT, 25, new float[] { 0, 1, 0, 1 });
					} else {
						drawText("Finir en moins de", 710, 600, PosWidth.MILIEU, PosHeight.HAUT, 25, grey);
						drawText(gameStats.getObjectifString(), 710, 630, PosWidth.MILIEU, PosHeight.HAUT, 25, grey);
					}
					
					drawText(pause.getPauseString(), 660, 335, 100, white);
				} else
					pause.showRestartChrono();
				return;
			}
			
			if (!pauseScreen && pause.isFinish())
				drawText(timer / 60 + "", 960, 10, PosWidth.MILIEU, PosHeight.HAUT, 60);
				
		}
		
	}
	
	/*
	 *
	 * methods
	 *
	 *
	 *
	 *
	 *
	 */
	
	private void movePlayer() {
		lastTimeLine++;
		if (Input.isKeyDown(CarOptions.KEY_RIGHT) && timer > 6) {
			speed -= speed / 5 - 0.5;// freine dans les virage a grande vitesse
			direction += 2 + speed;
			if (direction > 360) direction -= 360;
		} else if (Input.isKeyDown(CarOptions.KEY_LEFT) && timer > 6) {
			speed -= speed / 5 - 0.5;
			direction -= 2 + speed;
			if (direction < 0) direction += 360;
		} else {
			speed += 0.2 / speed;
		}
		
		if (isLine() && timer > 18 && locCar.getY() >= middleHeight * cubeHeight) {// arrivee
			if (direction > 270 || direction < 90) {
				if (lastTimeLine < 1800) {
					lastTimeLine = 0;
					locCar.setPos(deplacedX(), deplacedY());// pas rester bloquer sur la line en trichant
				} else {
					win = true;
				}
			} else {
				contreSens = true;
				speed = 0;
				lastTimeLine = 0;
			}
			
		} else {
			if (!isCircuit()) // crash
				speed = 0;
				
			locCar.setPos(deplacedX(), deplacedY());
		}
		
	}
	
	private void upgradeMap() {
		for (int i = waitingPoints.size() - 1; i >= 0; i--) {
			Location loc = waitingPoints.get(i);
			if (middleHeight <= Math.abs((int) loc.getY() - middleHeight) + bord || middleWidth <= Math.abs((int) loc.getX() - middleWidth) + bord) {
				waitingPoints.remove(loc);
				continue;
			}
			
			if (new Random().nextInt(waitingPoints.size()) == 0) {
				
				List<Location> nears = loc.getNears(1);
				for (Location testLoc : nears) {
					if (testLoc.getNearestDistance(pointsInt) > 0 && goodDistanceOtherInt(testLoc)) {
						pointsInt.add(testLoc);
						waitingPoints.add(testLoc);
						return;
					}
				}
				
				waitingPoints.remove(loc);
			}
		}
	}
	
	private boolean isLine() {
		
		int x = (int) locCar.getX();
		int y = (int) locCar.getY();
		
		boolean xT = x > middleWidth * cubeWidth - 16 && x < middleWidth * cubeWidth;
		boolean yT = y > middleHeight * cubeHeight && y < (middleHeight + 1) * cubeHeight;
		
		return xT && yT;
	}
	
	private boolean isCircuit() {
		int x = (int) deplacedX();
		int y = (int) deplacedY();
		
		BufferedImage img = map.getBuffImage();
		ColorModel cm = img.getColorModel();
		boolean centre = cm.getRed(img.getRGB(x, y)) + cm.getGreen(img.getRGB(x, y)) + cm.getBlue(img.getRGB(x, y)) >= 10;
		
		x = (int) deplacedX(15);
		y = (int) deplacedY(15);
		
		boolean avant = cm.getRed(img.getRGB(x, y)) + cm.getGreen(img.getRGB(x, y)) + cm.getBlue(img.getRGB(x, y)) >= 10;
		
		x = (int) (locCar.getX() + 18 * Math.cos(Math.toRadians(direction + 25)));
		y = (int) (locCar.getY() + 18 * Math.sin(Math.toRadians(direction + 25)));
		
		boolean droite = cm.getRed(img.getRGB(x, y)) + cm.getGreen(img.getRGB(x, y)) + cm.getBlue(img.getRGB(x, y)) >= 10;
		
		x = (int) (locCar.getX() + 18 * Math.cos(Math.toRadians(direction - 25)));
		y = (int) (locCar.getY() + 18 * Math.sin(Math.toRadians(direction - 25)));
		
		boolean gauche = cm.getRed(img.getRGB(x, y)) + cm.getGreen(img.getRGB(x, y)) + cm.getBlue(img.getRGB(x, y)) >= 10;
		
		return droite && gauche && centre && avant;
		
	}
	
	private void paintLiaisons(final List<Location> points) {
		float[] color = new float[] { 0f, 0f, 0f, 1 };
		
		// draw all the lines
		for (Location loc : points) {
			List<Location> near = new LinkedList<>();
			for (Location locat : points) {
				if (distanceDiag(locat, loc) == 1) {
					ComponentsHelper.drawLine((int) (loc.getX() * cubeWidth), (int) (loc.getY() *
							cubeHeight), (int) (locat.getX() * cubeWidth), (int) (locat.getY() * cubeHeight), 8, color);
					near.add(locat);
				}
			}
			
			if (near.size() >= 2) {
				
				for (Location locat : near) {
					for (Location locat2 : near) {
						if (distanceDiag(locat, locat2) == 1) {
							glColor4f(color[0], color[1], color[2], color[3]);
							glBegin(GL_TRIANGLES);
							glVertex2f(getImageX(loc), getImageY(loc));
							glVertex2f(getImageX(locat), getImageY(locat));
							glVertex2f(getImageX(locat2), getImageY(locat2));
							glColor4f(1, 1, 1, 1);
							glEnd();
							
						}
					}
				}
			}
		}
	}
	
	private void saveScreen() {
		glReadBuffer(GL_FRONT);
		int width = Display.getWidth();
		int height = Display.getHeight();
		int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
		glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		BufferedImage circuit = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int i = (x + width * y) * bpp;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				circuit.setRGB(x, height - (y + 1), 0xFF << 24 | r << 16 | g << 8 | b);
			}
		}
		
		BufferedImage newImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
		
		Graphics g = newImage.createGraphics();
		g.drawImage(circuit, 0, 0, 1920, 1080, null);
		g.dispose();
		
		map = new Textures(newImage);
	}
	
	private int getImageX(final Location loc) {
		return (int) getResponsiveX(loc.getX() * cubeWidth);
	}
	
	private int getImageY(final Location loc) {
		return (int) getResponsiveY(loc.getY() * cubeHeight);
	}
	
	private int distanceDiag(final Location loc1, final Location loc2) {
		return (int) Math.max(Math.abs(loc1.getX() - loc2.getX()), Math.abs(loc1.getY() - loc2.getY()));
	}
	
	private boolean goodDistanceOtherInt(final Location loc) {
		Location near = loc.getNearest(pointsInt);
		int i = 100000; // a reduire
		for (Location locat : pointsInt) {
			int diff = distanceDiag(locat, loc);
			
			if (locat.distance(near) > 1 && diff < i)
				i = diff;
				
		}
		return i >= 2;
	}
	
	private double deplacedX() {
		return deplacedX(speed);
	}
	
	private double deplacedX(final double size) {
		double locX = locCar.getX() + size * Math.cos(Math.toRadians(direction));
		if (locX < 10) locX = 10;
		if (locX >= defaultWidth - 10) locX = defaultWidth - 10;
		return locX;
	}
	
	private double deplacedY() {
		return deplacedY(speed);
	}
	
	private double deplacedY(final double size) {
		double locY = locCar.getY() + size * Math.sin(Math.toRadians(direction));
		if (locY < 10) locY = 10;
		else if (locY >= defaultHeight - 10) locY = defaultHeight - 10;
		return locY;
	}
}
