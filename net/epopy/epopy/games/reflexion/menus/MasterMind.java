package net.epopy.epopy.games.reflexion.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import net.epopy.epopy.display.Textures;
import net.epopy.epopy.display.components.ButtonGui;
import net.epopy.epopy.display.components.ComponentsHelper;
import net.epopy.epopy.display.components.ComponentsHelper.PositionHeight;
import net.epopy.epopy.display.components.ComponentsHelper.PositionWidth;
import net.epopy.epopy.games.gestion.AbstractGameMenu;
import net.epopy.epopy.utils.Input;

public class MasterMind extends AbstractGameMenu {
	
	private final int caseSize = 20;
	private final int debutX = 70;
	private final int debutY = 70;
	private final int ecartX = 70;
	private final int ecartY = 70;
	
	private List<float[]> colors;
	private List<line> lines;
	
	private List<ButtonGui> rightArrow;
	private List<ButtonGui> leftArrow;

	private Boolean[] selected;
	
	@Override
	public void onEnable() {
		selected = new Boolean[] { false, false, false, false };
		
		lines = new ArrayList<line>();
		colors = new ArrayList<float[]>();
		colors.add(new float[] { 1, 0, 0, 1 });// rouge
		colors.add(new float[] { 0, 1, 0, 1 });// vert
		colors.add(new float[] { 0, 0, 1, 1 });// bleu
		colors.add(new float[] { 1, 1, 1, 1 });// blanc
		colors.add(new float[] { 1, 1, 0, 1 });// jaune
		colors.add(new float[] { 1, 0.5f, 0, 1 });// orange

		colors.add(new float[] { 0.2f, 0.2f, 0.2f, 1 });// gris : vide
		Random r = new Random();
		List<Integer> colors = new ArrayList<Integer>();

		for (int i = 0; i < 4; i++)
			colors.add(r.nextInt(6));

		lines.add(new line(colors, 0));

		lines.add(new line(null, 1));

		rightArrow = new ArrayList<ButtonGui>();
		rightArrow.add(new ButtonGui(Textures.REFLEXION_RARROW, Textures.REFLEXION_RARROWFOCUS));
		rightArrow.add(new ButtonGui(Textures.REFLEXION_RARROW, Textures.REFLEXION_RARROWFOCUS));
		rightArrow.add(new ButtonGui(Textures.REFLEXION_RARROW, Textures.REFLEXION_RARROWFOCUS));
		rightArrow.add(new ButtonGui(Textures.REFLEXION_RARROW, Textures.REFLEXION_RARROWFOCUS));

		leftArrow = new ArrayList<ButtonGui>();
		leftArrow.add(new ButtonGui(Textures.REFLEXION_LARROW, Textures.REFLEXION_LARROWFOCUS));
		leftArrow.add(new ButtonGui(Textures.REFLEXION_LARROW, Textures.REFLEXION_LARROWFOCUS));
		leftArrow.add(new ButtonGui(Textures.REFLEXION_LARROW, Textures.REFLEXION_LARROWFOCUS));
		leftArrow.add(new ButtonGui(Textures.REFLEXION_LARROW, Textures.REFLEXION_LARROWFOCUS));
	}

	@Override
	public void update() {
		int yB = debutY + ecartY * (lines.size() - 1) - caseSize / 2;
		int x = 0;
		if (Input.getButtonDown(0)) {
			int mx = Mouse.getX();
			int my = Display.getHeight() - Mouse.getY();
			mx = (int) (mx / (double) Display.getWidth() * defaultWidth);
			my = (int) (my / (double) Display.getHeight() * defaultHeight);
			for (int i = 0; i < 4; i++) {
				int xB = debutX + ecartX * x - 14;// 28 de large : -14 + 14
				if (mx > xB && mx < xB + 28 && my > yB && my < yB + 28) {
					if (lines.get(lines.size() - 1).couleurs.get(i) != 6) {
						selected[i] = !selected[i];
						break;
					}
				}
				x++;
			}
		}
		yB = debutY + ecartY * (lines.size() - 1) + 1;

		x = -1;
		for (ButtonGui button : rightArrow) {
			x++;
			if (selected[x]) continue;
			
			if (button.isClicked()) {
				line lastLine = lines.get(lines.size() - 1);
				List<Integer> couleurs = lastLine.couleurs;
				int nb = couleurs.get(x);
				nb++;
				
				if (nb > colors.size() - 2) {
					nb = 0;
				}
				couleurs.set(x, nb);
				lastLine.setCouleurs(couleurs);
			}
			
			int xB = debutX + ecartX * x + 15;
			button.update(xB, yB, PositionWidth.GAUCHE, PositionHeight.MILIEU, 16, 26);

		}
		
		x = -1;
		for (ButtonGui button : leftArrow) {
			x++;
			if (selected[x]) continue;
			
			if (button.isClicked()) {
				line lastLine = lines.get(lines.size() - 1);
				List<Integer> couleurs = lastLine.couleurs;
				int nb = couleurs.get(x);
				nb--;
				
				if (nb < 0) {
					nb = colors.size() - 2;
				}
				couleurs.set(x, nb);
				lastLine.setCouleurs(couleurs);
			}

			int xB = debutX + ecartX * x - 15;
			button.update(xB, yB, PositionWidth.DROITE, PositionHeight.MILIEU, 16, 26);

		}
		if (selected[0] && selected[1] && selected[2] && selected[3]) {
			lines.get(lines.size() - 1).calcResult();
			if (win) return;
			else if (lines.size() == 10) {
				onEnable();// gameOver
			}
			lines.add(new line(null, lines.size()));
			for (int i = 0; i < 4; i++) {
				selected[i] = false;
			}

			update();
		}
	}
	
	@Override
	public void render() {
		int i = 0;
		for (ButtonGui button : rightArrow) {
			if (!selected[i]) {
				button.render();
			}
			i++;
		}
		i = 0;
		for (ButtonGui button : leftArrow) {
			if (!selected[i]) {
				button.render();
			}
			i++;
		}
		
		for (line l : lines)
			l.draw();
			
	}

	private class line {

		private List<Integer> couleurs;
		private List<Boolean> result;
		private final int nbr;

		private line(List<Integer> couleurs, final int num) {
			if (couleurs == null) {
				couleurs = new ArrayList<Integer>();
				for (int i = 0; i < 4; i++)
					couleurs.add(6);// vide
			}
			this.couleurs = couleurs;
			nbr = num;
		}

		private void setCouleurs(final List<Integer> couleurs) {
			this.couleurs = couleurs;
		}

		private void calcResult() {
			result = new ArrayList<Boolean>();

			List<Integer> restant = new ArrayList<Integer>(couleurs);
			List<Integer> notUsed = new ArrayList<Integer>(lines.get(0).couleurs);
			for (int num = 3; num >= 0; num--) {
				Integer couleur = couleurs.get(num);
				if (lines.get(0).couleurs.get(num) == couleur) {
					result.add(true);
					restant.remove(num);
					notUsed.remove(num);
				}

			}

			for (Integer couleur : notUsed) {
				if (restant.contains(couleur)) {
					restant.remove(couleur);
					result.add(false);
				}
			}

			if (result.size() == 4) {
				boolean gagne = true;
				for (boolean b : result)
					gagne = gagne && b;// tout doit être true
				win = gagne;
			}
		}

		private void draw() {
			if (nbr == 0 && win || nbr != 0) {
				for (int i = 0; i < 4; i++) {
					int x = debutX + ecartX * i;
					int y = debutY + ecartY * nbr;
					ComponentsHelper.drawCircle(x, y, caseSize, 4, colors.get(couleurs.get(i)));
					
				}
				if (result != null) {
					int place = 2;
					for (boolean b : result) {
						int x = debutX + ecartX * 3 + place * 30;
						int y = debutY + ecartY * nbr;
						ComponentsHelper.drawCircle(x, y, 10, 10, b ? new float[] { 0, 1, 0, 1 } : new float[] { 0, 0, 1, 1 });
						
						place++;
					}
				}
			}
		}
	}

}
