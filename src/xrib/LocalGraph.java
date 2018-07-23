/**
 * 
 */
package xrib;

import java.util.ArrayList;

import edu.princeton.cs.algs4.Digraph;

/**
 * @author vladislavkargin
 *
 */
public class LocalGraph {
	Digraph Gamma; //this is a local graph that shows the order forced on tiles of different levels that intersect a given interval
	ArrayList<Integer> dotLevels; //shows the level of each dot in the digraph Gamma
	
	/**
	 * default constructor. Creates a local graph with no dots.
	 */
	public LocalGraph() {
		Gamma = new Digraph(0);
		dotLevels = new ArrayList<Integer>();
	}
	
	/**
	 * A constructor that joins two local graphs. The direction can be either up or down. 
	 * 
	 */
	public LocalGraph(LocalGraph G1, LocalGraph G2, String direction){
		//the main trouble is that some of the dots should disappear. For example, if we join in the direction up
		// then the dots with the level l - n should disappear. 
		Gamma = new Digraph(G1.Gamma.V() + G2.Gamma.V());
		dotLevels = new ArrayList<Integer>();
		for (int i = 0; i < G1.Gamma.V(); i++) {
			dotLevels.add(G1.dotLevels.get(i));
		}
		for (int j = 0; j < G2.Gamma.V(); j++) {
			dotLevels.add(G2.dotLevels.get(j));
		}
		for (int u = 0; u < G1.Gamma.V(); u++) {
			for (int w : G1.Gamma.adj(u)) {
				Gamma.addEdge(u, w);
			}
		}		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
