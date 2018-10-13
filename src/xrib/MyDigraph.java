package xrib;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import java.lang.Iterable;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * The main point of this class is to encapsulate the Digraph class and add the capability
 * to remove some directed edges. 
 * 
 * @author vladislavkargin
 *
 */


public class MyDigraph {
	private Digraph DG;
	

	public MyDigraph(int V) {
		DG = new Digraph(V);
	}

	/**
	 * Initializes a digraph from the specified input stream. The format is the number of vertices V, 
	 * followed by the number of edges E, followed by E pairs of vertices, with each entry separated by whitespace.
	 * 	Parameters:
	* in - the input stream
	* Throws:
	* IllegalArgumentException - if the endpoints of any edge are not in prescribed range
	* IllegalArgumentException - if the number of vertices or edges is negative
	* IllegalArgumentException - if the input stream is in the wrong format
	 */
	public MyDigraph(In in) {
		DG = new Digraph(in);
	}
	
	/**
	 * Initializes a new myDigraph from a given Digraph.
	 * 	Parameters:
	 * G - the Digraph 
	 */
    public MyDigraph( Digraph G) {
    	DG = new Digraph(G);
    }

	/**
	 * Initializes a new myDigraph that is a deep copy of the specified myDigraph.
	 * 	Parameters:
	 * G - the myDigraph to copy
	 */
    public MyDigraph(MyDigraph G) {
    	DG = new Digraph( G.DG());
    }
    
    /**
     * returns a copy of the digraph DG.
     * @return
     */
    public Digraph DG() {
    	return new Digraph(DG);
    }
    
    /**
     * 	Returns the number of vertices in this digraph.
     * @return
     */
	public int V() {
		return DG.V();
	}

    /**
     * 	Returns the number of edges in this digraph.
     * @return
     */
	public int E() {
		return DG.E();
	}

	/**
	 * 	Adds the directed edge v -> w to this digraph.
	 * The behavior is different from the similar method in Digraph class.
	 * The edge is only added if it does not exist. Hence, multiple edges between the same vertices
	 * are not allowed. 
	Parameters:
	v - the tail vertex
	w - the head vertex
	Throws:
	IllegalArgumentException - unless both 0 <= v < V and 0 <= w < V
	 * @param v
	 * @param w
	 */
	public void addEdge(int v, int w) {
		if (!isEdge(v, w)) {
		    DG.addEdge(v, w);
		}
	}
	
	/**
	 * removes an edge if it is present in the graph
	 * @param v
	 * @param w
	 */
	public void removeEdge(int v, int w) {
		Digraph nDG = new Digraph(DG.V());
		for (int i = 0; i < DG.V(); i++) {
			for (int j : adj(i)) {
				if (i != v || j != w) { // we copy edges except when it is (v, w) edge.
					nDG.addEdge(i, j);
				}
			}
		}
		DG = nDG;
	}
    
	/**
	 * checks that graph does not have parallel edges. (i.e., multiple edges from v to w);
	 * 
	 * @return true if there is no multiple edges
	 */
	
	public boolean validate() {
		for (int u = 0; u < DG.V(); u++) {
			HashSet<Integer> set = new HashSet<Integer>();
			for (int v : adj(u)) {
				if(!set.add(v)) {
					StdOut.println("The edge (" + u + ", " + v + ") is a duplicate.");
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * check if the graph contains a directed edge u -> v
	 */
	public boolean isEdge(int u, int v) {
		for (int w : adj(u)) {
			if (v == w) return true;
		}
		return false;
	}

	/**
	 * 	Returns the vertices adjacent to vertex v in this digraph.
	Parameters:
	v - the vertex
	Returns:
	the vertices adjacent from vertex v in this digraph, as an iterable
	Throws:
	IllegalArgumentException - unless 0 <= v < V
	 * @param v
	 * @return
	 */
	public Iterable<Integer> adj(int v) {
		ArrayList<Integer> adjVector = new  ArrayList<Integer>();
		for (int u : DG.adj(v)) {
			adjVector.add(u);
		}
		return adjVector;
	}
	
	/**
	 * 	Returns the number of directed edges incident from vertex v. This is known as the outdegree of vertex v.
	Parameters:
	v - the vertex
	Returns:
	the outdegree of vertex v
	Throws:
	IllegalArgumentException - unless 0 <= v < V
	 * 
	 */

	public int outdegree(int v) {
		return DG.outdegree(v);
	}

	
	/** The indegree is not implemented in the version of Digraph that I have in my library.
	 * 
	 * 	Returns the number of directed edges incident to vertex v. This is known as the indegree of vertex v.
	Parameters:
	v - the vertex
	Returns:
	the indegree of vertex v
	Throws:
	IllegalArgumentException - unless 0 <= v < V
	*/
	/*
	public int indegree(int v) {
		return DG.i
	}
	*/

	/**
	 * 	Returns the reverse of the digraph.
	Returns:
	the reverse of the digraph
	 */
	
	public Digraph reverse() {
		return DG.reverse();
	}

	/**
	 * 	Returns a string representation of the graph.
	Overrides:
	toString in class Object
	Returns:
	the number of vertices V, followed by the number of edges E, followed by the V adjacency lists
	 */
	public String toString() {
		return DG.toString();
	}
}

