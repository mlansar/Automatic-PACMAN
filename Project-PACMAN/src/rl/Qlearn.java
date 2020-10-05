package rl;

import java.util.ArrayList;
import java.util.Hashtable;

public class Qlearn {
	public double epsilon = 0.1; // parametre epsilon pour \epsilon-greedy
	public double alpha = 0.2; // taux d'apprentissage
	public double gamma = 0.9; // parametre gamma des eq. de Bellman/

	// Suggestions
	public int actions[];
	public Hashtable<Tuple<Integer, Integer>, Double> q;

	// Constructeurs
	public Qlearn(int[] actions) {
		this.actions = actions;
		q = new Hashtable<Tuple<Integer, Integer>, Double>();
	}

	public Qlearn(int[] actions, double epsilon, double alpha, double gamma) {
		this.actions = actions;
		this.epsilon = epsilon;
		this.alpha = alpha;
		this.gamma = gamma;
		q = new Hashtable<Tuple<Integer, Integer>, Double>();
	}

	/*
	 * Retourne le gain moyen le plus élevé
	 */
	public double maxQ(int state) {
		double gain = 0;
		double courantGain = 0;
		for (int action = 0; action < 8; action++) { // On parcourt toutes les actions
			Tuple<Integer, Integer> courantQ = new Tuple<>(state, action); // On définit un tuple (pour un état et toutes les
																		// actions possibles)
			if (q.get(courantQ) == null) { // On prend en compte le cas où le Q(s,a) n'a jamais été changé
				courantGain = 0;
			} else {
				courantGain = q.get(courantQ); // On stock le gain moyen associé à ce Tuple
			}
			if (courantGain >= gain) { // On regarde si il s'agit de la plus grande récompense
				gain = courantGain;
			}
		}
		return gain; // On retourne le plus grand gain moyen
	}

	/*
	 * Retourne une des actions qui a le plus grand gain pour un état donnée
	 */
	public int maxAction(int state) {
		double maxGain = maxQ(state); // On stock le plus gros gain d'un état
		ArrayList<Integer> bestAction = new ArrayList<>(); // On stock les meilleurs actions
		for (int action = 0; action < 8; action++) { // On parcourt toutes les actions possibles
			Tuple<Integer, Integer> courantQ = new Tuple<>(state, action);
			if (q.get(courantQ) == null && maxGain == 0) {
				bestAction.add(action);
			} else if (q.get(courantQ) != null && q.get(courantQ) == maxGain) {
				bestAction.add(action);
			}

		}
		if (bestAction.size() != 0) {
			return bestAction.get((int) (Math.random() * bestAction.size())); // On retourne l'une des actions qui a le
																				// plus gros gain
		} else {
			return (int) (Math.random() * 8);
		}

	}

	/*
	 * Met à jour la Hashtable Q (les gains moyens en partant d'un état state et en
	 * effectuant une action action en Q Learning
	 */
	public void qLearning(int state, int action, int nextState, double reward) {
		Tuple<Integer, Integer> courantQ = new Tuple<>(state, action); // On définit un tuple
		double res = 0;
		if (this.q.get(courantQ) == null) { // On initialise Q(s,a) à 0 si le pointer est null
			this.q.put(courantQ,0.0);
		}
		//System.out.println(maxQ(nextState));		
		res = this.q.get(courantQ) + alpha * (reward + gamma * (maxQ(nextState)) - this.q.get(courantQ)); // On
																										   			// applique
																													// la
																													// régle
																													// de
																													// mise
																													// à
																													// jour
																													// des
																													// gains
																													// moyens
		this.q.put(courantQ, res);
	}
	
	/*
	 * Met à jour la Hashtable Q (les gains moyens en partant d'un état state et en
	 * effectuant une action action en SARSA)
	 */
	public void SARSA(int state, int action, int nextState, double reward) {
		int nextAction = chooseAction(nextState);
		Tuple<Integer, Integer> courantQ = new Tuple<>(state, action); // On définit un tuple
		Tuple<Integer, Integer> courantNextQ = new Tuple<>(nextState, nextAction);
		double res = 0;
		if (this.q.get(courantQ) == null) { // On initialise Q(s,a) à 0 si le pointer est null
			this.q.put(courantQ, 0.0);
		}
		if (this.q.get(courantNextQ) == null) { // On initialise Q'(s,a) à 0 si le pointer est null
			this.q.put(courantNextQ, 0.0);
		}
		res = this.q.get(courantQ) + alpha * (reward + gamma * this.q.get(courantNextQ) - this.q.get(courantQ));// On
																												// applique
																												// la
																												// régle
																												// de
																												// mise
																												// à
																												// jour
																												// des
																												// gains
																												// moyens
		this.q.put(courantQ, res);
	}

	/*
	 * Choisit l'action selon le principe de l'Epsilon Greedy
	 */
	public int chooseAction(int state) {
		if (Math.random() < epsilon) { // On retourne une action au hasard si la valeur est en dessous d'epsilon
			return (int) (Math.random() * 8);
		} else {
			return maxAction(state); // Sinon on retourne la meilleur action
		}
	}
}
