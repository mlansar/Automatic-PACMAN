package bellman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import Jama.*;

public class ResolutionBellman {
	private boolean[][] grid;
	private double[][] reward;
	private int size_x;
	private int size_y;
	private int nbStates;
	private double gamma = 0.5;
	private Random rdmnum;
	private long seed = 124;
	private int MAX_REWARD = 20;
	private HashMap<Integer, HashMap<String, Double>> action;
	private HashMap<String, HashMap<Integer, ArrayList<double[]>>> pi;
	private ArrayList<String> dir;
	private boolean matricielle = true; // True pour affichage Matricielle et False pour affichage Iterative


	ResolutionBellman(int num_g) {
		this.rdmnum = new Random(this.seed);
		this.dir = new ArrayList<String>();
		this.dir.add("left");
		this.dir.add("up");
		this.dir.add("right");
		this.dir.add("down");
		this.dir.add("stay");

		CreateGrid(num_g);
		InitRdmPol();
		WallCst();
		InitTransitionMat();
	}

	private void CreateGrid(int g) {
		switch (g) {
			case 0:
				this.size_x = 8;
				this.size_y = 5;
				this.grid = new boolean[size_x][size_y];
				this.reward = new double[size_x][size_y];
				this.nbStates = size_x * size_y;
				for (int i = 0; i < size_x; i++) {
					for (int j = 0; j < size_y; j++) {
						grid[i][j] = true;
						reward[i][j] = -1;
					}
				}
				// put some walls
				reward[2][2] = -1000;
				reward[3][2] = -1000;
				reward[4][2] = -1000;
				reward[5][2] = -1000;
				grid[2][2] = false;
				grid[3][2] = false;
				grid[4][2] = false;
				grid[5][2] = false;

				// put a strong reward somewhere
				reward[0][0] = 20;
				break;
				case 1:
					this.size_x = 6;
					this.size_y = 6;
					this.grid = new boolean[size_x][size_y];
					this.reward = new double[size_x][size_y];
					this.nbStates = size_x * size_y;
					for (int i = 0; i < size_x; i++) {
						for (int j = 0; j < size_y; j++) {
							grid[i][j] = true;
							reward[i][j] = -1;
						}
					}
			reward[0][1] = 100;
			reward[0][2] = -1000;
			reward[0][3] = -1000;
			reward[0][4] = -1000;
			reward[2][0] = -1000;
			reward[2][1] = -1000;
			reward[2][3] = -1000;
			reward[2][4] = -1000;
			reward[3][4] = -1000;
			reward[3][5] = -1000;
			reward[4][1] = -1000;
			reward[4][2] = -1000;
			reward[4][3] = -1000;
			reward[4][5] = -1000;
			reward[5][5] = -1000;

			grid[0][2] = false;
			grid[0][3] = false;
			grid[0][4] = false;
			grid[2][0] = false;
			grid[2][1] = false;
			grid[2][3] = false;
			grid[2][4] = false;
			grid[3][4] = false;
			grid[3][5] = false;
			grid[4][1] = false;
			grid[4][2] = false;
			grid[4][3] = false;
			grid[4][5] = false;
			grid[5][5] = false;
			break;
		default:
			System.out.println("Erreur choix grille!");
			System.exit(-1);
			break;
		}
	}

	// choose a random coordinate in the grid
	private void ChooseRdmState() {
		int i = rdmnum.nextInt(size_x);
		int j = rdmnum.nextInt(size_y);
		grid[i][j] = true;
	}

	// add a reward randomly on the grid
	private void PutRdmReward(int n_rew) {
		int n = 0;
		while (n < n_rew) {
			int i = rdmnum.nextInt(size_x);
			int j = rdmnum.nextInt(size_y);
			if (reward[i][j] == 0) {
				reward[i][j] = rdmnum.nextInt(MAX_REWARD);
				n++;
			}
		}
	}

	// return a state given a coordinate on the grid
	private int GridToState(int i, int j) {
		return i + size_x * j;
	}

	// return the coordinate on the gris given the state
	private int[] StateToGrid(int s) {
		int[] index = new int[2];
		index[1] = (int) s / size_x;
		index[0] = s - index[1] * size_x;
		return index;
	}

	// add the possible actions for all states
	private void InitRdmPol() {
		action = new HashMap<Integer, HashMap<String, Double>>();
		for (int i = 0; i < this.nbStates; i++) {//On parcourt chaque état
			HashMap<String, Double> direction = new HashMap<String, Double>();
			for (int j = 0; j < this.dir.size(); j++) {//On parcourt chaque action possible
				direction.put(this.dir.get(j), 0.2); // Ajoute toutes les directions avec leurs probabilités
			}
			action.put(i, direction);
		}
	}

	// return the direction (on the grid) for a given action
	private int[] getDirNeighbor(String act) {
		int[] d = new int[2];

		if (act.equals("left"))
			d[0] = -1;
		if (act.equals("right"))
			d[0] = 1;
		if (act.equals("up"))
			d[1] = 1;
		if (act.equals("down"))
			d[1] = -1;

		return d;
	}

	// To each state, give the reachable states given an action
	private HashMap<Integer, ArrayList<double[]>> computeTrans(String act) {
		HashMap<Integer, ArrayList<double[]>> trans = new HashMap<Integer, ArrayList<double[]>>();
		for (int s = 0; s < this.nbStates; s++) {
			ArrayList<double[]> etatsAcc = new ArrayList<double[]>(); // taille 1

			// On transforme l'etat s sous forme de coordonées
			int[] sGrid = new int[2];
			sGrid = StateToGrid(s);

			// On ajoute aux coordonées de s les effets de l'action a.Les nouvelles
			// coordonnées seront celles de s'
			sGrid[0] = sGrid[0] + getDirNeighbor(act)[0];
			sGrid[1] = sGrid[1] + getDirNeighbor(act)[1];

			// On transforme les coordonnées de s' sous forme d'état en prenant en compte
			// les bords
			int x = Math.max(0, Math.min(sGrid[0], this.size_x - 1));
			int y = Math.max(0, Math.min(sGrid[1], this.size_y - 1));
			double sprime = GridToState(x, y);

			// On crée le tableau ou j'associe l'état s' à sa probabilité
			double[] EtatPlusProba = new double[2];
			EtatPlusProba[0] = sprime;
			EtatPlusProba[1] = 1.0;
			etatsAcc.add(EtatPlusProba);
			trans.put(s, etatsAcc);
		}
		return trans;
	}

	// initiate values of P
	private void InitTransitionMat() {
		pi = new HashMap<String, HashMap<Integer, ArrayList<double[]>>>();
		for (String act : this.dir) {
			pi.put(act, computeTrans(act));
		}
	}

	// compute the vector r
	private double[] computeVecR() {
		double[] R = new double[nbStates];
		for (int s = 0; s < nbStates; s++) {//On parcourt les états
			double sum = 0;
			HashMap<String, Double> a = action.get(s);
			// compute the reward obtained from state s, by doing all potential action a
			for (String act : this.dir) { //On parcourt les actions
				int[] coord = StateToGrid(s);
				int x = Math.max(0, Math.min(coord[0] + getDirNeighbor(act)[0], this.size_x - 1));
				int y = Math.max(0, Math.min(coord[1] + getDirNeighbor(act)[1], this.size_y - 1));
				sum += this.reward[x][y] * a.get(act); // On utilise la formule : VecR = ∑P(s,s',a)*R(s,s',a) pour calculer le vecteurR
			}
			R[s] = sum;
		}
		return R;
	}

	@SuppressWarnings("unlikely-arg-type")
	//Calcule la matrice P
	private double[][] computeMatP() {
		double[][] P = new double[nbStates][nbStates];
		for (int s = 0; s < nbStates; s++) { //On parcourt les états possibles
			// from state s, compute P^{\pi}(s,s')
			for (String act : this.dir) { //On parcourt les actions possibles(5 dans notre cas
				int s2 = s;
				int sprime = (int) pi.get(act).get(s2).get(0)[0];
				P[s2][sprime] = action.get(s2).get(act);
			}
		}
		return P;
	}

	// converting to matrix for the inverse
	private Matrix BuildMatA() {
		double[][] f_A = new double[nbStates][nbStates];
		double[][] P = computeMatP();
		for (int s = 0; s < nbStates; s++) {
			f_A[s][s] = 1;
			for (int sp = 0; sp < nbStates; sp++) {
				f_A[s][sp] -= this.gamma * P[s][sp];
			}
		}

		Matrix matP = new Matrix(f_A);
		return new Matrix(f_A);
	}

	// converting to matrix for the inverse
	private Matrix BuildMatb() {
		double[] vec_b = computeVecR();
		double[][] b = new double[vec_b.length][1];
		for (int i = 0; i < vec_b.length; i++) {
			b[i][0] = vec_b[i];
		}
		return new Matrix(b);
	}

	// solving the linear system
	private double[][] SolvingP() {
		Matrix x = BuildMatA().solve(BuildMatb());
		return x.getArray();
	}

	private void showGrid() {
		for (int i = 0; i < size_x; i++) {
			for (int j = 0; j < size_y; j++)
				System.out.print(this.GridToState(j, i));
			// (this.grid[i][j]?1:0)
			System.out.println();
		}
	}

	private void showRewGrid() {
		for (int i = 0; i < size_x; i++) {
			for (int j = 0; j < size_y; j++)
				System.out.print(this.reward[i][j] + " ");
			System.out.println();
		}
	}

	//Fonction qui permet de calculer Q(s,a) en fonction de V(s)
	private double equationBellman(double Q, String actionString, int s, double[][] V) {
        for (int i = 0; i < pi.get(actionString).get(s).size(); i++) {
            double[] EtatsPlusProba = pi.get(actionString).get(s).get(i);
            Q = Q + EtatsPlusProba[1] * (reward[StateToGrid((int) EtatsPlusProba[0])[0]][StateToGrid((int) EtatsPlusProba[0])[1]]+ gamma * V[(int) EtatsPlusProba[0]][0]);
            //On utilise la formule : Q(s,a) = ∑P(s,s',a)[R(s,a,s')+gamma*V(s')]
        }
        return Q;
    }

	//Cette fonction permet d'améliorer notre politque afon qu'elle soit optimale et donc qu'on ait les meilleurs gains possibles
    private void ImprovePolicy(double[][] V) {
        this.action = new HashMap<Integer, HashMap<String, Double>>();
        String bestAction;
        double bestQ;
        double Q;
        boolean sameAction;
        String actionString;

        for (int s = 0; s < nbStates; s++) {
            HashMap<String, Double> probaActions = new HashMap<String, Double>();
            bestAction = "stay";
            bestQ = -500000;

            for (int action = 0; action < dir.size(); action++) {
                actionString = dir.get(action);
                Q = 0;
                Q = equationBellman(Q, actionString, s, V);

                if (Q >= bestQ) {
                    bestAction = actionString;
                    bestQ = Q;
                }
            }

            for (int action = 0; action < dir.size(); action++) {
                actionString = dir.get(action);
                sameAction = actionString.equals(bestAction);
                if (sameAction) {
                    probaActions.put(actionString, 1.0);
                } else {
                    probaActions.put(actionString, 0.0);
                }
            }
            this.action.put(s, probaActions);
        }
        WallCst();
    }


	// Utilisation de l'equation de Bellman dans le cadre du calcul de V de manière
	// itérative
	public double[][] Viteratif() {
		double[][] V = new double[nbStates][1];
		double v = 0;
		double somme =0;
		double delta = 1;
		while (delta!=0) {
			for (int s = 0; s < this.nbStates; s++) { //On parcourt chaque état s
				delta = 0;
				for (String act : this.dir) { //On parcourt chaque action act
					//Transforme l'état s sous formes de coordonnées
					int[] coord = StateToGrid(s);
					//Ajoute les effets de l'action aux coordonnées de s afin d'avoir les coordonnées de s'.Il faut prendre en compte les murs
					int x = Math.max(0, Math.min(coord[0] + getDirNeighbor(act)[0], this.size_x - 1));
					int y = Math.max(0, Math.min(coord[1] + getDirNeighbor(act)[1], this.size_y - 1));
					int sprime = GridToState(x, y);
					int[] tab = StateToGrid(sprime);
					somme += this.action.get(s).get(act)*this.pi.get(act).get(s).get(0)[1] * (this.reward[tab[0]][tab[1]] + this.gamma * V[sprime][0]);
					// On utilise la fomule :Vk+1(s) = ∑π(s,a)*∑P(s,a,s')(R(s,a,s')+gamma*V(s')) pour calculer V
				}
				v = V[s][0];
				V[s][0]= somme;
				somme = 0;
				delta += Math.max(delta,Math.abs(v-V[s][0])); //Permet de garder delta à une petite valeur
			}
		}
		return V;
	}


	private void WallCst() {
		for (int i = 0; i < size_x; i++) {
			for (int j = 0; j < size_y; j++) {
				if (!grid[i][j]) {
					HashMap<String, Double> a = new HashMap<String, Double>();
					a.put("left", 0.0);
					a.put("up", 0.0);
					a.put("right", 0.0);
					a.put("down", 0.0);
					a.put("stay", 1.0);
					action.put(GridToState(i, j), a);
				}
			}
		}
	}

	//Cette fonction nous montre comment evolue la politique dans le cadre d'un calcul matriciel de V
	public  void PolEvolution(double [][] V) {
		for(int i=0;i<12;i++) { //Les valeurs de V converge à partir de la 7-ième itération,il n'est donc pas nécessaire d'aller plus loin
			System.out.println("Valeurs de V à l'itération :" + (i+1));
			V = this.SolvingP();
			this.ImprovePolicy(V);
			for (int j = 0; j < this.nbStates; j++) {
				if (j % 5 == 0)
					System.out.println();
				System.out.print(V[j][0] + " ");
			}
			System.out.println("\n");
		}
	}

	public static void main(String[] args) {

		//Cette partie nous montre les valeurs de V calcule a l'aide des matrices
		ResolutionBellman gd = new ResolutionBellman(1);
		double[][] V1 = gd.SolvingP();
		ResolutionBellman gd1 = new ResolutionBellman(1);
		double[][] V2 = gd1.Viteratif();

		if(gd.matricielle == true) {
			//Show V
			System.out.println("Forme matricielle : Calcul du V de départ");
			for (int i = 0; i < gd.nbStates; i++) {
				if (i % 5 == 0)
					System.out.println();
				System.out.print(V1[i][0] + " ");
			}
			System.out.println("\n");

			// Improve the policy !
			for (int i = 0; i < 20; i++) {
				V1 = gd.Viteratif();
				gd.ImprovePolicy(V1);
			}

			//Show V
			System.out.println("Forme matricielle : Calcul du V après amélioration de politique");
			for (int i = 0; i < gd.nbStates; i++) {
				if (i % 5 == 0)
					System.out.println();
				System.out.print(V1[i][0] + " ");
			}
			System.out.println("\n");
			//gd.PolEvolution(V1);    //Cette fonction montre l'evolution de la politique.A decommenter si besoin
		}

		//Cette partie nous montre les valeurs de V calcule de maniere iterative
		else {
			// show V
			System.out.println("Forme itérative : Calcul du V de départ");
			for (int i = 0; i < gd1.nbStates; i++) {
			if (i % 5 == 0)
				System.out.println();
			System.out.print(V2[i][0] + " ");
			}
			System.out.println("\n");

			// Improve the policy !
			for (int i = 0; i < 20; i++) {
				V2 = gd1.Viteratif();
				gd1.ImprovePolicy(V2);
			}

			System.out.println("Forme itérative : Calcul du V après amélioration de politique");
			//Show V
			for (int i = 0; i < gd1.nbStates; i++) {
				if (i % 5 == 0)
					System.out.println();
				System.out.print(V2[i][0] + " ");
				}
				System.out.println("\n");
		}
	}
}
