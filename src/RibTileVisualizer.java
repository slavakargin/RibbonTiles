import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdRandom;
import java.util.Scanner;
import java.util.ArrayList;

public class RibTileVisualizer {
	int N, M; //height and width of the tiling.
	public int nF, nH, nV, nL; //number of flips, horizontal, vertical and Gamma tiles.
	public RibTiling tiling;
	public SheffieldGraph G;
	static Scanner sc = new Scanner(System.in);
	static final int n = 3;
	public Draw window1, window2, wPlot; //windows for drawing 
	int size; //size of the windows.


	/**
	 * Construct a random tiling of an N-by-M rectangle starting with a initial tiling of a 
	 * given type and using the number ITER of random exchanges.
	 */
	public RibTileVisualizer(int N, int M, int type) {		
		tiling = new RibTiling(N,M,type); //initialize a tiling 
		this.N = N;
		this.M = M;
		/**
		 * computes Sheffield's digraph for the tiling.
		 */
		G = new SheffieldGraph(tiling);
		G.reduce();  //compute the cover graph for the Sheffield Graph 
		window1 = new Draw("Tiling 1");
		window2 = new Draw("Tiling 2");
		wPlot = new Draw("Plot");
		size = N; 
		if (M > N) size = M; 
		setWindow(window1, size, 1);
        setWindow(window2,size, 512);
        setWindow(wPlot,size,768);
		for (int i = 1; i < 10; i++) {
			wPlot.setPenRadius(0.005);
			wPlot.line(size + 1, i / 10.0 * size, size + 1.5, i / 10.0 * size);
			wPlot.textLeft(size + 2, i / 10.0 * size, i + "/10");
			wPlot.setPenRadius(0.002);
			wPlot.line(0, i / 10.0 * size, size + 2, i / 10.0 * size);
		}
	}

	/**
	 * Reads the tiling from the input stream (from a file)
	 */
	public RibTileVisualizer(In in) {
		tiling = new RibTiling(in);
		N = tiling.N;
		M = tiling.M;
		/*
		 * computes Sheffield's digraph for the tiling.
		 */
		G = new SheffieldGraph(tiling);
		G.reduce();  //compute the cover graph for the Sheffield Graph 
		window1 = new Draw("Tiling 1");
		window2 = new Draw("Tiling 2");
		size = N; 
		if (M > N) size = M; 
		setWindow(window1, size, 1);
        setWindow(window2,size, 512);
		draw(window1);	//drawing the tiling
		G.draw(window1); //draw the cover graph
		draw(window2);	
		G.draw(window2);
	}
	
	/*
	 * set default properties for windows to draw.
	 * 
	 */
	private void setWindow(Draw dr, int size, int offset) {
		dr.setLocationOnScreen(offset, 1);
		dr.setXscale(-0.5, size + 0.5);
		dr.setYscale(-0.5, size + 0.5);
		dr.clear(Draw.LIGHT_GRAY);
		dr.setPenRadius(0.005);
	}
	
	/*
	 * shows the tiling in a window.
	 */
	public void draw(Draw dr) {
		tiling.draw(dr);
		dr.show(40);
	}

	public void mix(int ITER) { //for simplicity I will write this function only for 3-tiles here (not for general n)
		int v1, v2;
		long c;
		RibTile t1;
		int nF = 0;
		for (int count = 0; count < ITER; count++) {
			v1 = StdRandom.uniform(G.V); // a random tile. 
			t1 = G.labels.get(v1);
			c = G.level(v1);					
			//Now we need to find a comparable tile, which would be a neighbor of t1 
			ArrayList<Integer> comparables = new ArrayList<Integer>(); //First, we collect all tiles that 
			                                                            // are exchangeable with t1 together.
			for (long i = c - n + 1; i < c + n; i++) {
				if ( i >= 0 && i <= N + M - n - 1 && i != c ) {
					comparables.addAll(G.levels2vertices.get(i));
				}					
			}
            ArrayList<Integer> vicini = new ArrayList<Integer>(); //All tiles that shares an edge with t1. 
			
            for (int v : comparables) {
            	if (t1.isTouch(G.labels.get(v))) {
            		vicini.add(v);
            	}
            }
            
            if (vicini.size() == 0) { //for a border tile there is a possibility that there is no exchangeable
            	                      //neighbor around. Then we skip this tile.
            	StdOut.println(G.labels.get(v1));
            	continue;
            }
            v2 = vicini.get(StdRandom.uniform(0, vicini.size()));
			int f = G.exchange(v1, v2);
			if (f == 0) {
				StdOut.println(G.labels.get(v1));
				StdOut.println(G.labels.get(v2));
				return;
			}
			nF = nF + f;


			wPlot.setPenColor(Draw.BLACK);
			wPlot.filledCircle(size * ((double) count / (double) ITER), 
					(double) size * n * nF/(double)((count + 1) * N * M), 0.1); //plot the number of
			//plots flips per iteration normalized by the total number of tiles 
			//should be a number on the scale [0, 1]
			nH = tiling.countTiles(0);
			nV = tiling.countTiles(3);
			nL = tiling.countTiles(1);
			wPlot.setPenColor(Draw.BLUE);
			wPlot.filledCircle(size * ((double) count / (double) ITER),
					(double) size * n * nV / (double)(M * N) , 0.1);
			wPlot.setPenColor(Draw.RED);
			wPlot.filledCircle(size * ((double) count / (double) ITER),
					(double) size * n * nH / (double)(M * N) , 0.1);
			wPlot.setPenColor(Draw.GREEN);
			wPlot.filledCircle(size * ((double) count / (double) ITER),
					(double) size * n * nL / (double)(M * N) , 0.1);
		}
		draw(window1);	//drawing the tiling
		G.draw(window1); //draw the cover graph
		draw(window2);	
		G.draw(window2); 
	}



	/**
	 * This function randomly chooses 2 vertices and exchanges their order in the acyclic graph.
	 * It repeats this procedure a number of times and plots the results. IT also plot the averaged number of 
	 * flips needed to take the exchange and the number of tiles of different type.
	 * [This function is not satisfactory for our purposes because this Markov chain do not converge to the 
	 * uniform distribution on tilings. 
	 * 
	 * @param ITER number of iterations
	 */

	public void mixLongRange(int ITER) {
		int v1, v2;
		long c;
		for (int k = 0; k < ITER; k++) {
			v1 = StdRandom.uniform(G.V);
			c = G.level(v1);					
			//Now we need to find a comparable tile, which would be exchangeable with t1 
			ArrayList<Integer> comparables = new ArrayList<Integer>();
			for (long i = c - n + 1; i < c + n; i++) {
				if ( i >= 0 && i <= N + M - n - 1 && i != c ) {
					comparables.addAll(G.levels2vertices.get(i));
				}					
			}
			v2 = comparables.get(StdRandom.uniform(comparables.size()));
			nF = nF + G.exchange(v1, v2);
			wPlot.setPenColor(Draw.BLACK);
			wPlot.filledCircle(size * ((double) k / (double) ITER), 
					(double) size * n * nF/(double)((k + 1) * N * M), 0.1); //plot the number of
			//flips per iteration normalized by the total number of tiles 
			//should be a number on the scale [0, 1]
			nH = tiling.countTiles(0);
			nV = tiling.countTiles(3);
			nL = tiling.countTiles(1);
			wPlot.setPenColor(Draw.BLUE);
			wPlot.filledCircle(size * ((double) k / (double) ITER),
					(double) size * n * nV / (double)(M * N) , 0.1);
			wPlot.setPenColor(Draw.RED);
			wPlot.filledCircle(size * ((double) k / (double) ITER),
					(double) size * n * nH / (double)(M * N) , 0.1);
			wPlot.setPenColor(Draw.GREEN);
			wPlot.filledCircle(size * ((double) k / (double) ITER),
					(double) size * n * nL / (double)(M * N) , 0.1);
		} 
		wPlot.show();
	}

	/*******************
	 * MAIN 
	 */

	public static void main(String[] args) {
		RibTileVisualizer vz;
		RibTile t1, t2;
		int v1, v2;
		////////////////////////////////////
		/*
		*If the tiling is not loaded from a file we will use the following parameters.
		*/
		////////////////////////////////////
		int N = 10; //height of the rectangle to tile
		int M = 12; //width
		int ITER = 100; //number of iterations for mixing
		int type = 2; //type of initial tiling (from 0 to 5)
		//0 - all vertical
		//1 - all horizontal
		//2 - szepka of Gamma and mirrored L
		//3 another type of szepka
		// 4 = formed from 5 * 3 blocks (5 in height and 3 in width
		// 5 - formed from 3 * 5 blocks


		//Generate or load a tiling
		StdOut.println("If you want to enter tiling from a file print 'File',");
		StdOut.println("otherwize press Enter.");
		String option = sc.nextLine();

		if (option.equals("File")) {
			StdOut.println("Enter file name: ");
			String fName = sc.nextLine();
			//StdOut.println(System.getProperty("user.dir"));
			In in = new In(fName);
			vz = new RibTileVisualizer(in);
			in.close();
		} else {
			vz = new RibTileVisualizer(N, M, type); 
			vz.mix(ITER);
		}

		vz.tiling.save("savedTiling.txt");
        /*
		for (int i = 0; i < vz.N + vz.M - n; i++) {
        	StdOut.print( "Level " + i + " : ");
        	StdOut.println(vz.tiling.levels2tiles.get(i));
        }
        */
        int v = 20;
        int offset = -3;
        int dir = 1;
        StdOut.println(vz.G.labels.get(v));
        //int w = vz.G.findTargetAtLevel(v, offset, dir);
        int w = vz.G.findTarget(v, dir);
        StdOut.println(vz.G.labels.get(w));
        

		//Second part of the program: Making some Manual flips.

		// Manual flips 
		double xCoord, yCoord;
		while (true) {
			StdOut.println("Choose the first tile or click outside of the board to reset: ");

			// wait for a mouse click 
			while (true) {
				if (vz.window2.mousePressed()) {
					xCoord = vz.window2.mouseX();
					yCoord = vz.window2.mouseY();
					while (vz.window2.mousePressed()) {
						continue; //wait for the end of the click
					}
					break;
				}
			}

			t1 = vz.tiling.findTile(xCoord, yCoord);
			if (t1 == null) {
				// resetting the tiling back to the state before manual flips.
				In in = new In("savedTiling.txt");
				vz = new RibTileVisualizer(in);
				in.close();
				continue; //continue to wait for the next mouse click
			} else {
			    StdOut.println("Choose the second tile: ");
			    while (true) {
				  if (vz.window2.mousePressed()) {
					xCoord = vz.window2.mouseX();
					yCoord = vz.window2.mouseY();
					while (vz.window2.mousePressed()) {
						continue; //wait for the end of the click
					}
					break;
				  }
			    }

			t2 = vz.tiling.findTile(xCoord, yCoord);
            if (t2 == null) {
            	   StdOut.println("Invalid location of the second tile.");
            	   continue;
            }


			// a check that these two tiles are flippable.
			StdOut.println("Chosen two tiles are: ");
			StdOut.println(t1);
			StdOut.println(t2);
			ArrayList<RibTile> btwnTiles = vz.tiling.findBetween(t1, t2);

			StdOut.println("The number of tiles between t1 and t2 is:  " 
			  + btwnTiles.size());

			v1 = vz.G.labels2vertices.get(t1);
			v2 = vz.G.labels2vertices.get(t2);
			vz.nF = vz.G.exchange(v1, v2);

			StdOut.println("The number of flips made in the process of exchange is: "+ vz.nF);
			vz.draw(vz.window2);	//drawing the tiling
			vz.G.draw(vz.window2); //draw the graph
		}
	}
  }
}

