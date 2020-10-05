package rl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class Pacman extends Agent {

	// Variables chooseAction
	private int lastState = -1; // etat précédent
	private int lastAction = -1; // action précédente
	private boolean use_ia = true; // utiliser le qlearning ?
	private boolean use_SARSA = false; // Utiliser SARSA si true ou le QLearning si false
	private final int base = 5;

	// IA
	private Qlearn ia;

	// Scores
	public int good = 0;
	public int idle = 0;
	public int stuck = 0;
	public int eaten = 0;
	public boolean restart = false;

	// possibles récompenses
	private double r_ghost = -40; // je tombe sur un fantome //-250 // -250
	private double r_food = 80; // je tombe sur de la nourriture //450 // 400
	private double r_stuck = -25; // je vais vers un mur//-75 // -100
	private double r_nothing = 0; // je ne fais rien ??/-10 // 0

	// voisinage
	private ArrayList<Tuple<Integer, Integer>> lookcells;

	// variables anim
	private Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
	private Image pacman3up, pacman3down, pacman3left, pacman3right;
	private Image pacman4up, pacman4down, pacman4left, pacman4right;
	private final int Ag_animcount = 4;
	private int animdelay = 2;
	private int animcount = animdelay;
	private int animdir = 1;
	private int animpos = 0;

	Pacman(Board BD) {
		super(BD);
		loadimages();

		// Define the possible actions
		int[] actions = new int[8];
		for (int a = 0; a < actions.length; a++)
			actions[a] = a;
		ia = new Qlearn(actions); // Initialise le tableau des gains moyens

		lastState = -1;
		lastAction = -1;
		good = 0;
		eaten = 0;
		idle = 0;
		stuck = 0;

		// InitState
		InitLookCells(2);
	}

	public void ai_move() {

		int pos;

		// I'm on a cell
		if (x % b.blocksize == 0 && y % b.blocksize == 0) {
			pos = posx + b.nrofblocks * posy;

			int[] diag = new int[4]; // SW - NW - NE - SE
			diag[0] = ((posx > 0) && (posy < (b.nrofblocks - 1)))
					? (x - b.blocksize) / b.blocksize + b.nrofblocks * (int) ((y + b.blocksize) / b.blocksize)
					: -1;
			diag[1] = ((posx > 0) && (posy > 0))
					? (x - b.blocksize) / b.blocksize + b.nrofblocks * (int) ((y - b.nrofblocks) / b.blocksize)
					: -1;
			diag[2] = ((posx < (b.nrofblocks - 1)) && (posy > 0))
					? (x + b.blocksize) / b.blocksize + b.nrofblocks * (int) ((y - b.blocksize) / b.blocksize)
					: -1;
			diag[3] = ((posx < (b.nrofblocks - 1)) && (posy < (b.nrofblocks - 1)))
					? (x + b.blocksize) / b.blocksize + b.nrofblocks * (int) ((y + b.blocksize) / b.blocksize)
					: -1;

			dx = 0;
			dy = 0;
			if (!update())
				restart = true;

			// against a wall
			if (b.screendata[pos] != 0) {
				if ((b.screendata[pos] & 1) != 0 && dx == -1) {
					dx = 0;
				}
				if ((b.screendata[pos] & 2) != 0 && dy == -1) {
					dy = 0;
				}
				if ((b.screendata[pos] & 4) != 0 && dx == 1) {
					dx = 0;
				}
				if ((b.screendata[pos] & 8) != 0 && dy == 1) {
					dy = 0;
				}
			}

			if ((dx == -1) && (dy == 1)) {
				if (diag[0] == -1) {
					dx = 0;
					dy = 0;
				} else if (b.levelstate[diag[0]] == b.STATE_WALL) {
					dx = 0;
					dy = 0;
				}
			}
			if ((dx == -1) && (dy == -1)) {
				if (diag[1] == -1) {
					dx = 0;
					dy = 0;
				} else if (b.levelstate[diag[1]] == b.STATE_WALL) {
					dx = 0;
					dy = 0;
				}
			}
			if ((dx == 1) && (dy == -1)) {
				if (diag[2] == -1) {
					dx = 0;
					dy = 0;
				} else if (b.levelstate[diag[2]] == b.STATE_WALL) {
					dx = 0;
					dy = 0;
				}
			}
			if ((dx == 1) && (dy == 1)) {
				if (diag[3] == -1) {
					dx = 0;
					dy = 0;
				} else if (b.levelstate[diag[3]] == b.STATE_WALL) {
					dx = 0;
					dy = 0;
				}
			}

			if ((dx == 0) && (dy == 0))
				idle = 1;
			viewdx = dx;
			viewdy = dy;

		}
		setx(x + (dx * speed));
		sety(y + (dy * speed));
	}

	public void user_move() {
		int pos;
		short ch;

		if (b.reqdx == -dx && b.reqdy == -dy) {
			dx = b.reqdx;
			dy = b.reqdy;
			if ((b.reqdx != 0) && (b.reqdy != 0)) {
				viewdx = dx;
				viewdy = dy;
			}
		}

		// I'm on a cell
		if (x % b.blocksize == 0 && y % b.blocksize == 0) {
			pos = x / b.blocksize + b.nrofblocks * (int) (y / b.blocksize);
			ch = b.screendata[pos];

			// I find food !
			if ((ch & 16) != 0) { // 16 is food
				b.screendata[pos] = (short) (ch & 15);
				b.score++;
			}

			// I'm moving
			if (b.reqdx != 0 || b.reqdy != 0) {
				if (!((b.reqdx == -1 && b.reqdy == 0 && (ch & 1) != 0)
						|| (b.reqdx == 1 && b.reqdy == 0 && (ch & 4) != 0)
						|| (b.reqdx == 0 && b.reqdy == -1 && (ch & 2) != 0)
						|| (b.reqdx == 0 && b.reqdy == 1 && (ch & 8) != 0))) {
					dx = b.reqdx;
					dy = b.reqdy;
					viewdx = dx;
					viewdy = dy;
				}
			}

			// Modification : if reqdx and reqdy are 0 : standstill
			if ((b.reqdx == 0) && (b.reqdy == 0)) {
				dx = 0;
				dy = 0;
			}

			// Check for standstill (against a wall)
			if ((dx == -1 && dy == 0 && (ch & 1) != 0) || (dx == 1 && dy == 0 && (ch & 4) != 0)
					|| (dx == 0 && dy == -1 && (ch & 2) != 0) || (dx == 0 && dy == 1 && (ch & 8) != 0)) {
				dx = 0;
				dy = 0;
			}
		}
		setx(x + (dx * speed));
		sety(y + (dy * speed));
	}

	private void random_move() {
		dx = (int) Math.round(Math.random()) * 2 - 1;
		dy = (int) Math.round(Math.random()) * 2 - 1;
	}

	/*
	 * following scheme :
	 * 
	 * 4 158 02 9B 36A 7
	 * 
	 */
	private int getStateNb(int[] state) {
		int id_s = 0;
		for (int i = 0; i < state.length; i++)
			id_s += state[i] * Math.pow(base, i);

		return id_s;
	}

	private int calcState() {
		int[] state = new int[12];
		int id = 0;
		for (Tuple<Integer, Integer> coord : lookcells) {
			int level_st = b.getIndexFromCoord(posx + coord.x, posy + coord.y);

			if ((level_st < 0) || (level_st >= b.levelstate.length))
				state[id] = b.STATE_WALL; // put walls if we get out of the world
			else
				state[id] = b.levelstate[level_st];
			id++;
		}
		return getStateNb(state);
	}

	private boolean update() {
		int id_state = calcState();
		int id_action = 0;
		double reward = r_nothing;

		int cell_state = b.levelstate[b.getIndexFromCoord(x / b.blocksize, y / b.blocksize)];

		if ((cell_state & b.STATE_BADGUY) != 0) { // trouver un fantome
			eaten++;
			reward = r_ghost;
			// MAJ IA
			if (use_SARSA) {
				ia.SARSA(lastState, lastAction, id_state, reward); // On met a jour les gains moyen avec SARSA
			} else {
				ia.qLearning(lastState, lastAction, id_state, reward); // On met a jour les gains moyen avec QLearning
			}
			return false;

		} else if ((cell_state & b.STATE_GOODSTUFF) != 0) { // trouver de la nourriture
			good++;
			reward = r_food;
			b.random_cheese();
		}

		if (idle == 1) { // to not move is bad !
			idle = 0;
			stuck++;
			reward = r_stuck;
		}

		if (lastState != -1) {
			// MAJ IA
			if (use_SARSA) {
				ia.SARSA(lastState, lastAction, id_state, reward); // On met a jour les gains moyen avec SARSA
			} else {
				ia.qLearning(lastState, lastAction, id_state, reward); // On met a jour les gains moyen avec QLearning
			}
		}

		if (use_ia) {
			// choose an action here using qlearn or SARSA
			id_action = ia.chooseAction(id_state);
			goInDirection(id_action);
		} else { // random direction
			random_move();
		}

		lastState = id_state;
		lastAction = id_action;
		return true;
	}

	public void set_epsilon(double epsilon) {
		ia.epsilon = epsilon;
	}

	public void reset_state() {
		eaten = 0;
		stuck = 0;
		good = 0;
	}

	private void printState(int[] state) {
		System.out.println("    " + state[4]);
		System.out.println("  " + state[1] + " " + state[5] + " " + state[8]);
		System.out.println(state[0] + " " + state[2] + " * " + state[9] + " " + state[11]);
		System.out.println("  " + state[3] + " " + state[6] + " " + state[10]);
		System.out.println("    " + state[7]);
	}

	private void printState(int state) {
		int id_state = state;
		int[] get_state = new int[12];
		for (int i = 11; i >= 0; i--) {
			get_state[i] = (int) Math.floor(id_state / Math.pow(base, i));
			id_state = (int) (id_state % Math.pow(base, i));
		}
		printState(get_state);
	}

	private void showAction(int action) {
		switch (action) {
		case 0: // N
			System.out.println("nord");
			break;
		case 1: // NE
			System.out.println("nord-est");
			break;
		case 2: // E
			System.out.println("est");
			break;
		case 3: // SE
			System.out.println("sud-est");
			break;
		case 4: // S
			System.out.println("sud");
			break;
		case 5: // SW
			System.out.println("sud-ouest");
			break;
		case 6: // W
			System.out.println("ouest");
			break;
		case 7: // NW
			System.out.println("nord-ouest");
			break;
		default:
			break;

		}
	}

	private void goInDirection(int action) {
		switch (action) {
		case 0: // N
			dx = 0;
			dy = -1;
			break;
		case 1: // NE
			dx = 1;
			dy = -1;
			break;
		case 2: // E
			dx = 1;
			dy = 0;
			break;
		case 3: // SE
			dx = 1;
			dy = 1;
			break;
		case 4: // S
			dx = 0;
			dy = 1;
			break;
		case 5: // SW
			dx = -1;
			dy = 1;
			break;
		case 6: // W
			dx = -1;
			dy = 0;
			break;
		case 7: // NW
			dx = -1;
			dy = -1;
			break;
		default:
			break;

		}
	}

	@Override
	void move() {
		// ai_move : use the dx,dy to make the move
		// user_move : you can control the pacman
		ai_move();
		// user_move();
	}

	@Override
	void draw(Graphics2D g2d) {
		if (viewdx == -1) {
			drawPacnamLeft(g2d);
		} else if (viewdx == 1) {
			drawPacmanRight(g2d);
		} else if (viewdy == -1) {
			drawPacmanUp(g2d);
		} else {
			drawPacmanDown(g2d);
		}

	}

	private void drawPacmanUp(Graphics2D g2d) {

		switch (animpos) {
		case 1:
			g2d.drawImage(pacman2up, x + 1, y + 1, b);
			break;
		case 2:
			g2d.drawImage(pacman3up, x + 1, y + 1, b);
			break;
		case 3:
			g2d.drawImage(pacman4up, x + 1, y + 1, b);
			break;
		default:
			g2d.drawImage(pacman1, x + 1, y + 1, b);
			break;
		}
	}

	private void drawPacmanDown(Graphics2D g2d) {

		switch (animpos) {
		case 1:
			g2d.drawImage(pacman2down, x + 1, y + 1, b);
			break;
		case 2:
			g2d.drawImage(pacman3down, x + 1, y + 1, b);
			break;
		case 3:
			g2d.drawImage(pacman4down, x + 1, y + 1, b);
			break;
		default:
			g2d.drawImage(pacman1, x + 1, y + 1, b);
			break;
		}
	}

	private void drawPacnamLeft(Graphics2D g2d) {

		switch (animpos) {
		case 1:
			g2d.drawImage(pacman2left, x + 1, y + 1, b);
			break;
		case 2:
			g2d.drawImage(pacman3left, x + 1, y + 1, b);
			break;
		case 3:
			g2d.drawImage(pacman4left, x + 1, y + 1, b);
			break;
		default:
			g2d.drawImage(pacman1, x + 1, y + 1, b);
			break;
		}
	}

	private void drawPacmanRight(Graphics2D g2d) {

		switch (animpos) {
		case 1:
			g2d.drawImage(pacman2right, x + 1, y + 1, b);
			break;
		case 2:
			g2d.drawImage(pacman3right, x + 1, y + 1, b);
			break;
		case 3:
			g2d.drawImage(pacman4right, x + 1, y + 1, b);
			break;
		default:
			g2d.drawImage(pacman1, x + 1, y + 1, b);
			break;
		}
	}

	@Override
	void loadimages() {

		pacman1 = new ImageIcon("pacpix/PacMan1.png").getImage();
		pacman2up = new ImageIcon("pacpix/PacMan2up.png").getImage();
		pacman3up = new ImageIcon("pacpix/PacMan3up.png").getImage();
		pacman4up = new ImageIcon("pacpix/PacMan4up.png").getImage();
		pacman2down = new ImageIcon("pacpix/PacMan2down.png").getImage();
		pacman3down = new ImageIcon("pacpix/PacMan3down.png").getImage();
		pacman4down = new ImageIcon("pacpix/PacMan4down.png").getImage();
		pacman2left = new ImageIcon("pacpix/PacMan2left.png").getImage();
		pacman3left = new ImageIcon("pacpix/PacMan3left.png").getImage();
		pacman4left = new ImageIcon("pacpix/PacMan4left.png").getImage();
		pacman2right = new ImageIcon("pacpix/PacMan2right.png").getImage();
		pacman3right = new ImageIcon("pacpix/PacMan3right.png").getImage();
		pacman4right = new ImageIcon("pacpix/PacMan4right.png").getImage();

	}

	@Override
	void doAnim() {
		animcount--;

		if (animcount <= 0) {
			animcount = animdelay;
			animpos = animpos + animdir;

			// When I arrive at beginning/end of anim : change direction of anim
			if (animpos == (Ag_animcount - 1) || animpos == 0) {
				animdir = -animdir;
			}
		}
	}

	void InitLookCells(int lookdist) {
		lookcells = new ArrayList<Tuple<Integer, Integer>>();
		for (int i = -lookdist; i < lookdist + 1; i++) {
			for (int j = -lookdist; j < lookdist + 1; j++) {
				if ((Math.abs(i) + Math.abs(j) <= 2) && (i != 0 || j != 0)) {
					lookcells.add(new Tuple<Integer, Integer>(i, j, b.blocksize, b.blocksize));
				}
			}
		}
	}
}
