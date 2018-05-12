package rib3;
import java.util.ArrayList;
public class Utility {

	private Utility() {}

	public static ArrayList<Integer> findSinkSequence(Digraph DG){
		int V; //Number of vertices
		Boolean[] marked; //marks the vertices that were found as sinks.
		ArrayList<Integer> sinkSeq; //sequence of the vertices as they appear as sinks 
		//as the previous sinks are removed from the graph.
		//This is just a generic placeholder for the recursive algorithm below
		V = DG.V();
		sinkSeq = new ArrayList<Integer>(V);
		marked = new Boolean[V];
		for (int i = 0; i < V; i++) {
			marked[i] = false;
			sinkSeq.add(0);
		}
		findSink(DG, sinkSeq, marked, V);
		return(sinkSeq);
	}

	private static void findSink(Digraph DG, ArrayList<Integer> sinkSeq, Boolean[] marked, int V) {
		//we will do the search recursively
		//First, we check how many vertices are yet unmarked.
		int s = 0; //number of unmarked vertices.
		for (int i = 0; i < marked.length; i++) {
			if (!marked[i]) s++;
		}
		if (s == 0) return; //all vertices are marked

		boolean sink = false;
		for (int i = 0; i < V; i++) { //looking for a sink 
			sink = true; //our initial guess is optimistic: it is a sink.
			if (marked[i]) continue; //this vertex was already marked as a sink 
			for (int v : DG.adj(i)) {
				//StdOut.println("The entry in the Marked array is: " + marked[v]);
				if (!marked[v]) { //we found a vertex, which follows i, and which was not 
					//marked as a sink before. Hence, vertex i not a sink.
					sink = false;
					break; //look at the next vertex i.
				}
			}
			if (sink) { //vertex i is a new sink, it is a sink number V + 1 - s
				sinkSeq.set(V - s, i);
				marked[i] = true;
				findSink(DG, sinkSeq, marked, V);
			}
		}
	}

	public static ArrayList<Integer> calculateHeight(Digraph DG){
		int V = DG.V(); //Number of vertices
		ArrayList<Integer> sinkSeq = findSinkSequence(DG); //sequence of the vertices as they appear as sinks 
		//when the previous sinks are removed from the graph.
		ArrayList<Integer> height = new ArrayList<Integer>(V);
		//initialization
		for (int i = 0; i < V; i++) {
			height.add(0);
		} 
		//calculation
		for (int i = 0; i < V; i++) {
			height.set(sinkSeq.get(i), i);
		}
		return height;
	}
	
	//We need a convenient method to calculate a vertex' level in the corresponding tiling.
	public static ArrayList<Integer> buildVertex2Level(int N,  int M, int n) {
		    int V = M * N / n;
		    int L = N + M - n;
			int size = N;
				if (M < size) size = M;
				ArrayList<Integer> nTiles = new ArrayList<Integer>(L); //array of the numbers of tiles in each level;
				for (int i = 0; i < L; i++) {
					if (i < size) {
						nTiles.add(i, i/n + 1);
					}
					if (size <= i && i < N + M - size) {
						nTiles.add(i, (int) (((double) i) / n - (i - size)/n));
					}
					if (i >= N + M - size) {
						nTiles.add(i, (N + M - 1 - i)/n);
					}
				}
				//Now we build the array vertex2level.
				ArrayList<Integer> vertex2level = new ArrayList<Integer>(V);
				int v = 0;
				for (int level = 0; level < L; level++) { 
					for (int i = 0; i < nTiles.get(level); i++) { 
						vertex2level.add(v, level);
						v++;
					}
				}
				return vertex2level;
	}
}
	

