import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;

/*
 * This class will contain information about height vector function 
 * of a ribbon tiling. 
 */
public class Height {
	public int N; //The height of the rectangle (bound on y-coordinate).
	public int M; //The width of the rectangle (bound on x-coordinate).
	public int[][] heightX, heightY, heightZ; //the three components of the height
	public int[][] routing; 
	
	/*
	 * Create a N-by-M height object (on the rectangle with height N and width M). 
	 */
	public Height(int N, int M) {
		this.N = N;
		this.M = M;
		heightX = new int[N + 1][M + 1];
		heightY = new int[N + 1][M + 1];
		heightZ = new int[N + 1][M + 1];
		routing = new int[N][M + 1];
	}

	public Height(RibTiling T) {
		// Creates the class by calculating all three components of the height
		// using a provided Tiling
		this.N = T.N;
		this.M = T.M;
		heightX = new int[N + 1][M + 1];
		heightY = new int[N + 1][M + 1];
		heightZ = new int[N + 1][M + 1];
		calcHeight(T);
	}

	
/*
 * Update height: Calculate Height using data in tiling T.	
 */
	public void calcHeight(RibTiling T) {
		if (T == null) {
			StdOut.println("Height.calcHeight says: Null tiling was supplied, mylord.");
		} else {
			StdOut.println("Height.calcHeight says: I am starting to compute, mylord.");
		}
		//Initialization (height on the borders of the rectangle.
		//First on the bottom border. Note that the origin has height (0, 0 , 1)
		// in this initialization
		for (int i = 0; i < M + 1; i++) {
			switch (i % 3) {
			case 0:
				heightZ[0][i] = 1;
				break;
			case 1:
				heightX[0][i] = 1;
				break;
			case 2:
				heightY[0][i] = 1;
				break;
			}
		}
		//Then for the left border
		for (int j = 1; j < N + 1; j++) {
			switch (j % 3) {
			case 0:
				heightZ[j][0] = 1;
				break;
			case 1:
				heightX[j][0] = -1;
				heightZ[j][0] = 2;
				break;
			case 2:
				heightY[j][0] = -1;
				heightZ[j][0] = 2;
				break;
			}
		}
			//Then for the top border 
			for (int i = 0; i < M + 1; i++) {
				switch ((i + N) % 3) {
				case 0:
					heightZ[N][i] = 1;
					break;
				case 1:
					heightX[N][i] = 1;
					break;
				case 2:
					heightY[N][i] = 1;
					break;
				}
			}
			//Finally, for the right border
			for (int j = 1; j < N + 1; j++) {
				switch ((j + M) % 3) {
				case 0:
					heightZ[j][M] = 1;
					break;
				case 1:
					heightX[j][M] = -1;
					heightZ[j][M] = 2;
					break;
				case 2:
					heightY[j][M] = -1;
					heightZ[j][M] = 2;
					break;
				}
			}	
	    StdOut.println("Height.calcHeight says: Height on borders was "
	    		+ "evaluated OK, mylord.");
		//Now we are going to grow this height up the triangle.
		// we do it for all rows except the last one.
		for (int j = 1; j < N; j++) {
			//Note that we can always reach a given lattice point either from below,
			//or from the left.
            for (int i = 1; i < M; i++) {
            	RibTile tile1 = T.findTile(i - 0.5, j - 0.5);
            	RibTile tile2 = T.findTile(i + 0.5, j - 0.5);
            	if (!tile1.equals(tile2)) { //we can reach this point from below
            		switch ((i + j) % 3) {
            		case 0:
            			heightX[j][i] = heightX[j - 1][i];
            		    heightY[j][i] = heightY[j - 1][i] + 1;
            		    heightZ[j][i] = heightZ[j - 1][i] - 1;
            		    break;
            		case 1:
            		    heightX[j][i] = heightX[j - 1][i] - 1;
            			heightY[j][i] = heightY[j - 1][i];
            		    heightZ[j][i] = heightZ[j - 1][i] + 1;
            			break;
            		case 2: 
            		    heightX[j][i] = heightX[j - 1][i] + 1;
            		    heightY[j][i] = heightY[j - 1][i] - 1;
            		    heightZ[j][i] = heightZ[j - 1][i];
            			break;
            		}                       				
            	} else { //we can reach this point from the left
            		switch ((i + j) % 3) {
            		case 0:
            			heightX[j][i] = heightX[j][i - 1];
            		    heightY[j][i] = heightY[j][i - 1] - 1;
            		    heightZ[j][i] = heightZ[j][i - 1] + 1;
            		    break;
            		case 1:
            		    heightX[j][i] = heightX[j][i - 1] + 1;
            			heightY[j][i] = heightY[j][i - 1];
            		    heightZ[j][i] = heightZ[j][i - 1] - 1;
            			break;
            		case 2: 
            		    heightX[j][i] = heightX[j][i - 1] - 1;
            		    heightY[j][i] = heightY[j][i - 1] + 1;
            		    heightZ[j][i] = heightZ[j][i - 1];
            			break;
            		} 
            	}
            }
		}
	}
/*
 * modifies the height so that the height on the bottom equals 0 for all components
 */
public void modify() {
	for (int j = 0; j < N + 1; j++) {
		for (int i = 0; i < M + 1; i++) { 
			switch ((i + j) % 3) {
			case 0: 
				heightZ[j][i] = heightZ[j][i] - 1;
				break;
			case 1: 
				heightX[j][i] = heightX[j][i] - 1;
				break;
			case 2: 
				heightY[j][i] = heightY[j][i] - 1;
				break;
			}
			heightX[j][i] = (heightX[j][i] + 2 * j)/2;
			heightY[j][i] = (heightY[j][i] + 2 * j)/2;
			heightZ[j][i] = (heightZ[j][i] + 2 * j)/2;
		}
	}
}
/*
 * Calculate routings 
 */
public void calcRouting(){
	for (int t = 0; t < N; t++) { // thresholds
	for (int i = 0; i < M + 1; i++) {
		for (int j = 1; j < N + 1; j++) {
			if (heightX[j][i] > t) {
				routing[t][i] = j - 1;
				break;
			}
		}
	}
	}
}
/*
 * print out the height function
 */
	public void print() {
		for (int i = N; i > -1; i--) {
			for (int j = 0; j < M + 1; j++) {
				StdOut.print("(" + heightX[i][j] + ", " + heightY[i][j]
						+ ", " + heightZ[i][j] + ")\t");
			}
			StdOut.println();
		}
	}
/*
 * print routing
 */
	public void printRouting() {
		for (int t = 0; t < N; t++) {
		for (int i = 0; i < M + 1; i++) {
			StdOut.print(routing[t][i] + " ");
		}
		StdOut.println();
		}
	}

	/*
	 * draws routing in the specified window
	 */
public void drawRouting(Draw dr) {
	dr.setPenColor(Draw.PINK);
	for (int t = 0; t < N; t++) {
	for (int i = 0; i < M; i++) {
	    dr.line(i, routing[t][i], i + 1, routing[t][i + 1]);
	}
	}
	dr.show(40);
}
}
