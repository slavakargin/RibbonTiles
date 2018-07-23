package xrib;
import java.util.ArrayList;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Digraph;

public class XIFaces {
	ArrayList<XIface> faces;
	Digraph faceGraph;
	int n; //size of each ribbon tile;
	int M; // width of the interfaces.

	/**
	 * This class represent a collection of interfaces possible in the ribbon tiling of a horizontal strip by n-tiles.
	 * An interface is a sequence of M non-decreasing integers that starts with zero 
	 * and has some restrictions on the differences between components.
	 * 
	 * The constructor calculates all interfaces that can be obtained from the interface (0, 0, ..., 0) by 
	 * canonical removals of tiles.  
	 * 
	 * We will use the interfaces to compute the per tile entropy for ribbon tilings.
	 * of horizontal strips.   
	 *
	 * @author vladislavkargin
	 *
	 */
	public XIFaces(int n, int M) {
		this.n = n;
		this.M = M;
		faces = new ArrayList<XIface>();
		ArrayList<Integer> X = new ArrayList<Integer>();
		for (int i = 0; i < M; i++) {
			X.add(0);
		}
		XIface wall = new XIface(n, M, X);
		faces.add(wall);
		// now we need to start recursion. 
		generateIFaces(wall); //it will generate all interfaces obtainable from wall and for each of them it
		//checks if the interface is already in the collection. 
		// if yes, it will skip it, 
		// if not, it will add it and 
		//start the recursion on it.
		makeFaceGraph();
	}

	private void generateIFaces(XIface iface) {
		//TODO write this function
		ArrayList<Integer> X, Y;
		XIface  testIface; 
		X = iface.X();		

		for (int r = 0; r < M; r++) { //we go row by row, trying to build a new interface by removing a tile with a root in 
			                       //the row r. Sometimes we will run into a long horizontal line, which ensures that there
			                       //must be a removable tile with a root in row <= r, then we stop adding interfaces.
			Y = new ArrayList<Integer>(X); //copy of X;
			if (r == M - 1) { //this is a special case: top row
				Y.set(r, Y.get(r) + n);
				testIface = new XIface(n, M, Y);
				if (testIface.isValid()) { //recursion
					if (!faces.contains(testIface)) {
						faces.add(testIface);
						generateIFaces(testIface);
					}
				}
			} else { //not a top row
				int m = n;
				int tailRow = 0;
				for (int k = 0; k < M - r - 1; k++) { //going over rows on and above the root row.
					m = m - (Y.get(r + k + 1) - Y.get(r + k) + 1);     
					if (m <= 0) {
						tailRow = k;
						break; //the end of the tile is in this row.
					}			  
				}
				if (m < 0 && tailRow == 0) { // this is exactly the special situation that we have a long row so we must have a sink tile 
					                // in this row or below. 
					Y.set(r, Y.get(r + 1) + m + 1);
					testIface = new XIface(n, M, Y);
					if (testIface.isValid()) { //recursion
						if (!faces.contains(testIface)) {
							faces.add(testIface);
							generateIFaces(testIface);
						}
					}
					break; //we are done with this interface;
				}
				//Now there are 3 additional possibilities
				if (m == 0) {
					continue; //it is not possible to put tile in the right way. It will not be a sink tile.  
				} else if (m > 0) { // the end of tile is in the top row.
					for (int k = 0; k < M - r - 1; k++) {
						Y.set(r + k, Y.get(r + k + 1) + 1);
					}
					Y.set(M - 1, Y.get(M - 1) + m);
					testIface = new XIface(n, M, Y);
					if (testIface.isValid()) { //recursion
						if (!faces.contains(testIface)) {
							faces.add(testIface);
							generateIFaces(testIface);
						}
					}
				} else { // the head of the tile is in the row r and the tail of the tile is in the row r + k
					for (int j = 0; j < tailRow; j++) {
						Y.set(r + j, Y.get(r + j + 1) + 1);
					}
					Y.set(r + tailRow, Y.get(r + tailRow + 1) + m + 1);
					testIface = new XIface(n, M, Y);
					if (testIface.isValid()) { //recursion
						if (!faces.contains(testIface)) {
							faces.add(testIface);
							generateIFaces(testIface);
						}
					}
				}
			}
		}
	}

	public void makeFaceGraph(){
		faceGraph = new Digraph(faces.size());

		//Now we essentially will go over the same algorithm as when we generated the interfaces. 
		//However, since we already know all interfaces, we do not need recursion. 
		ArrayList<Integer> X, Y;
		XIface  iface, testIface; 
		for (int count = 0; count < faces.size(); count++) {
			iface = faces.get(count);
			X = iface.X();
			for (int r = 0; r < M; r++) { //we go row by row, trying to build a new interface by removing a tile with a root in 
				//the row r. Sometimes we will run into a long horizontal line, which ensures that there
				//must be a removable tile with a root in row <= r, then we stop adding interfaces.
				Y = new ArrayList<Integer>(X); //copy of X;
				if (r == M - 1) { //this is a special case: top row
					Y.set(r, Y.get(r) + n);
					testIface = new XIface(n, M, Y);
					if (faces.contains(testIface)) { //add edge
						faceGraph.addEdge(count, faces.indexOf(testIface));                     
					}
				} else { //not a top row
					int m = n;
					int tailRow = 0;
					for (int k = 0; k < M - r - 1; k++) { //going over rows on and above the root row.
						m = m - (Y.get(r + k + 1) - Y.get(r + k) + 1);     
						if (m <= 0) {
							tailRow = k;
							break; //the end of the tile is in this row.
						}			  
					}
					if (m < 0 && tailRow == 0) { // this is exactly the special situation that we have a long row so we must have a sink tile 
		                                         // in this row or below. 
		                Y.set(r, Y.get(r + 1) + m + 1);
		                testIface = new XIface(n, M, Y);
						if (faces.contains(testIface)) { //add edge
							faceGraph.addEdge(count, faces.indexOf(testIface));                     
						}
		                break; //we are done with this interface;
	                }
					//Now there are 3 additional possibilities
					if (m == 0) {
						continue; //it is not possible to put tile in the right way. It will not be a sink tile.  
					} else if (m > 0) { // the end of tile is in the top row.
						for (int k = 0; k < M - r - 1; k++) {
							Y.set(r + k, Y.get(r + k + 1) + 1);
						}
						Y.set(M - 1, Y.get(M - 1) + m);
						testIface = new XIface(n, M, Y);
						if (faces.contains(testIface)) { //add edge
							faceGraph.addEdge(count, faces.indexOf(testIface));                     
						}
					} else { // the head of the tile is in the row r and the tail of the tile is in the row r + k
						for (int j = 0; j < tailRow; j++) {
							Y.set(r + j, Y.get(r + j + 1) + 1);
						}
						Y.set(r + tailRow, Y.get(r + tailRow + 1) + m + 1);
						testIface = new XIface(n, M, Y);
						if (faces.contains(testIface)) { //add edge
							faceGraph.addEdge(count, faces.indexOf(testIface));                     
						}
					}
				}
			}
		}		
	}

	public static void main(String[] args) {
		int n = 2;
		int M = 3;
		XIface face;
		XIFaces IFaces = new XIFaces(n, M);
		StdOut.println("Generated Interfaces: ");
		for (int count = 0; count < IFaces.faces.size(); count++) {
			face = IFaces.faces.get(count);
			StdOut.println(count + ": " + face);
		}
        StdOut.println(IFaces.faceGraph);
	}

}
