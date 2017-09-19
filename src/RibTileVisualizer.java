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
	//public SheffieldGraph G;
	static Scanner sc = new Scanner(System.in);
	static final int n = 3;
	private int silent; //should be 0 to display a plot of some statistics
	public Draw window1, window2, wPlot; //windows for drawing 
	int size; //size of the windows.


	/**
	 * Construct a random tiling of an N-by-M rectangle starting with a initial tiling of a 
	 * given type and using the number ITER of random exchanges.
	 */
	public RibTileVisualizer(int N, int M, int type, int silent) {		
		tiling = new RibTiling(N,M,type, this); //initialize a tiling 
		this.N = N;
		this.M = M;
		this.silent = silent;
		window1 = new Draw("Tiling 1");
		window2 = new Draw("Tiling 2");
		if (silent == 0) {
		wPlot = new Draw("Plot");
		}
		size = N; 
		if (M > N) size = M; 
		setWindow(window1, size, 1);
        setWindow(window2,size, 512);
        if (silent == 0) {
        setWindow(wPlot,size,768);
		for (int i = 1; i < 10; i++) {
			wPlot.setPenRadius(0.005);
			wPlot.line(size + 1, i / 10.0 * size, size + 1.5, i / 10.0 * size);
			wPlot.textLeft(size + 2, i / 10.0 * size, i + "/10");
			wPlot.setPenRadius(0.002);
			wPlot.line(0, i / 10.0 * size, size + 2, i / 10.0 * size);
		}
        }
	}

	/**
	 * Reads the tiling from the input stream (from a file)
	 */
	public RibTileVisualizer(In in) {
		tiling = new RibTiling(in, this);
		N = tiling.N;
		M = tiling.M;
		window1 = new Draw("Tiling 1");
		window2 = new Draw("Tiling 2");
		size = N; 
		if (M > N) size = M; 
		setWindow(window1, size, 1);
        setWindow(window2,size, 512);
		tiling.draw(window1);	//drawing the tiling
		tiling.G.draw(window1); //draw the cover graph
		tiling.draw(window2);	
		tiling.G.draw(window2);
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
	

	public void drawSpecialTile(RibTile tile, Draw dr) {
		tile.drawSpecial(dr);
		dr.show(40);
	}
	public void drawSpecialTile(RibTile tile, String color, Draw dr) {
		tile.drawSpecial(dr, color);
		dr.show(40);
	}
	
	/*******************
	 * MAIN 
	 */

	public static void main(String[] args) {
		RibTileVisualizer vz;
		RibTile t1, t2;
		int v1, v2;
		int silent = 1; //set silent to zero to see a graph of some statistics.
		////////////////////////////////////
		/*
		*If the tiling is not loaded from a file we will use the following parameters.
		*/
		////////////////////////////////////
		int N = 10; //height of the rectangle to tile
		int M = 12; //width
		int ITER = 100; //number of iterations for mixing
		int type = 2; //type of initial tiling (from 0 to 5)
		                   //if negative, a random tiling will be generated
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
			In in = new In(fName);
			vz = new RibTileVisualizer(in);
			in.close();
		} else {
			vz = new RibTileVisualizer(N, M, type, silent); 
			vz.tiling.mix(ITER);
		}
		vz.tiling.save("savedTiling.txt");
		vz.tiling.draw(vz.window1);
       
		/*
        RibTile tile = vz.tiling.findTile(6.5, 3.5);
        int v = vz.tiling.G.labels2vertices.get(tile);
        int dir = 1; //direction is up
        int w = vz.tiling.G.findTarget(v, dir);
        int u = vz.tiling.G.findArrow(v, w);
        RibTile tileV = vz.tiling.G.labels.get(v);
        RibTile tileW = vz.tiling.G.labels.get(w);
        RibTile tileU = vz.tiling.G.labels.get(u);
        StdOut.println("The chosen tile is " + tileV);
        StdOut.println("The target tile is " + tileW);
        StdOut.println("The arrow tile is " + tileU);
        vz.drawSpecialTile(tileV, vz.window1);
        vz.drawSpecialTile(tileW, "pink", vz.window1);
        vz.drawSpecialTile(tileU, "orange", vz.window1);
         */
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

			v1 = vz.tiling.G.labels2vertices.get(t1);
			v2 = vz.tiling.G.labels2vertices.get(t2);
			vz.tiling.G.flip(v1, v2);
			vz.tiling.G.update();
			vz.tiling.G.reduce();

			vz.tiling.draw(vz.window2);	//drawing the tiling
			vz.tiling.G.draw(vz.window2); //draw the graph
		}
	}
  }
}

