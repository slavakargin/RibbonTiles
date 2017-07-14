import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdRandom;
import java.util.Scanner;
import java.util.ArrayList;

public class RibTileVisualizer {
	int N, M; //height and width of the tiling.
	//int ITER; //number of iteration in the mixing process
	public int nF, nH, nV, nL; //number of flips, horizontal, vertical and Gamma tiles.
	public RibTiling tiling;
	public RibTiling tilingCopy;
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
		//tiling = new RibTiling(N,M,3); //initiate a``szepka'' tiling
		//tiling = new RibTiling(N,M,2); //initiate another ``szepka'' tiling
		//tiling = new RibTiling(N,M,0); //initiate a vertical tiling
		//tiling = new RibTiling(N,M,1); //initiate a horizontal tiling
		//tiling = new RibTiling(N,M,4); //initiate a type 4 tiling		
		tiling = new RibTiling(N,M,type); //initiate a type 5 tiling 
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
		window1.setXscale(-0.5, size + 0.5);
		window1.setYscale(-0.5, size + 0.5);
		window1.clear(Draw.LIGHT_GRAY);
		window1.setPenRadius(0.005);
		window2.setXscale(- 0.5, size + 0.5);
		window2.setYscale(-0.5, size + 0.5);
		window2.setLocationOnScreen(512, 1);
		window2.clear(Draw.LIGHT_GRAY);
		window2.setPenRadius(0.005);
		wPlot.setXscale(- 0.5, size + 4);
		wPlot.setYscale(-0.5, size + 0.5);
		wPlot.setLocationOnScreen(768, 1);
		wPlot.clear(Draw.LIGHT_GRAY);
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
		/**
		 * computes Sheffield's digraph for the tiling.
		 */
		G = new SheffieldGraph(tiling);
		G.reduce();  //compute the cover graph for the Sheffield Graph 
	}
	/*
	 * shows the tiling in a window.
	 */
	public void draw(Draw dr) {
		tiling.draw(dr);
		dr.show(40);
	}

	
	public void mix(int ITER) {
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
		//If the tiling is not loaded from a file we will use the following parameters.
		int N = 20; //height of the rectangle to tile
		int M = 21; //width
		int type = 1; //type of initial tiling (from 0 to 5)
		//0 - all vertical
		//1 - all horizontal
		//2 - szepka of Gamma and mirrored L
		//3 another type of szepka
		// 4 = formed from 5 * 3 blocks (5 in height and 3 in width
		// 5 - formed from 3 * 5 blocks
		int ITER = 1000; //number of iterations for mixing
		
		//Generate or load a tiling
		StdOut.println("If you want to enter tiling from a file print 'File',");
		StdOut.println("otherwize press Enter.");
		String option = sc.nextLine();
		
		if (option.equals("File")) {
			StdOut.println("Enter file name: ");
			String fName = sc.nextLine();
			StdOut.println(System.getProperty("user.dir"));
			In in = new In(fName);
			vz = new RibTileVisualizer(in);
			vz.N = vz.tiling.N;
			vz.M = vz.tiling.M;
		} else {
            vz = new RibTileVisualizer(N, M, type); 
            vz.mix(ITER);
		}
		
		vz.tiling.save("savedTiling.txt");

		vz.draw(vz.window1);	//drawing the tiling
		vz.G.draw(vz.window1); //draw the cover graph
		
        //Setting up the window to draw the statistics
		/*Draw drStat = new Draw("Graph of Statistics");
		drStat.setXscale(- 0.5, size + 0.5);
		drStat.setYscale(-0.5, size + 0.5);
		drStat.setLocationOnScreen(758, 1);
		drStat.clear(Draw.LIGHT_GRAY);
		drStat.setPenRadius(0.005);*/

			
		vz.tilingCopy = new RibTiling(vz.tiling); //make a copy in case we want to reset.
        

		

        //Second part of the program: Making some Manual flips.

		vz.draw(vz.window2);	//drawing the tiling
		vz.G.draw(vz.window2); //draw the graph

		// Manual flips 
		double xCoord, yCoord;
		while (true) {
			StdOut.println("Choose coordinates of the first tile or click outside of the board to reset: ");

			// wait for a mouse click 
			while (true) {
				if (vz.window2.mousePressed()) {
					xCoord = vz.window2.mouseX();
					yCoord = vz.window2.mouseY();
					break;
				}
			}

			t1 = vz.tiling.findTile(xCoord, yCoord);
			if (t1 != null) {
			} else {
				// resetting the tiling back to the state before manual flips.
				vz.tiling = new RibTiling(vz.tilingCopy);
				vz.tiling.draw(vz.window2);
				vz.G.draw(vz.window2); //draw the graph
				while (vz.window2.mousePressed()) {
					continue; //waiting for the end of the click
				}
				continue;
			}

			while (vz.window2.mousePressed()) {
				continue; //waiting for the end of the click
			}


			StdOut.println("Enter coordinates of the second tile: ");
			while (true) {
				if (vz.window2.mousePressed()) {
					xCoord = vz.window2.mouseX();
					yCoord = vz.window2.mouseY();
					break;
				}
			}

			t2 = vz.tiling.findTile(xCoord, yCoord);
			while (vz.window2.mousePressed()) {
				continue; //waiting for the end of the click
			}


			// a check that these two tiles are flippable.
			StdOut.println("Chosen two tiles are: ");
			StdOut.println(t1);
			StdOut.println(t2);
			ArrayList<RibTile> btwnTiles = vz.tiling.findBetween(t1, t2);
			
			StdOut.println("The number of tiles between t1 and t2 is: " );
			StdOut.println(btwnTiles.size());
			
			v1 = vz.G.labels2vertices.get(t1);
			v2 = vz.G.labels2vertices.get(t2);
			vz.nF = vz.G.exchange(v1, v2);
			
			StdOut.println("The number of flips made in the process of exchange is: "+ vz.nF);
			vz.draw(vz.window2);	//drawing the tiling
			vz.G.draw(vz.window2); //draw the graph
		}
	}
	
}

