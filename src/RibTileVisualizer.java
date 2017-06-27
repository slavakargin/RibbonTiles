import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdRandom;
import java.util.Scanner;

public class RibTileVisualizer {
	int N, M; //height and width of the tiling.
	public RibTiling tiling;
	public RibTiling tilingCopy;
	public SheffieldGraph G;
	static Scanner sc = new Scanner(System.in);

	/**
	 * Construct a random tiling of an N-by-M rectangle
	 */
	public RibTileVisualizer(int N, int M, int type) {
		//tiling = new RibTiling(N,M,3); //initiate a``szepka'' tiling
		//tiling = new RibTiling(N,M,2); //initiate another ``szepka'' tiling
		//tiling = new RibTiling(N,M,0); //initiate a vertical tiling
		//tiling = new RibTiling(N,M,1); //initiate a horizontal tiling
		//tiling = new RibTiling(N,M,4); //initiate a type 4 tiling		
		tiling = new RibTiling(N,M,type); //initiate a type 5 tiling 
	}

	/**
	 * Reads the tiling from the input stream (from a file)
	 */
	public RibTileVisualizer(In in) {
		tiling = new RibTiling(in);
	}
	/*
	 * shows the tiling in a window.
	 */
	public void draw(Draw dr) {
		tiling.draw(dr);
		dr.show(40);
	}

	/**
	 * computes Sheffield's digraph for a tiling.
	 */
	public void computeSheffieldGraph(RibTiling tlng) {
		G = new SheffieldGraph(tlng);
	}
	
    /*******************
     * MAIN 
     */

	public static void main(String[] args) {
		RibTileVisualizer vz;
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
			int N = 10; //height of the rectangle to tile
			int M = 12; //width
			int type = 4; //type of initial tiling
			vz = new RibTileVisualizer(N, M, type); 
			vz.N = N;
			vz.M = M;
		}

		//Setting up the window for the tiling
		Draw draw1 = new Draw("Tiling before Flip");
		int size = vz.N; 
		if (vz.M > vz.N) size = vz.M; 
		draw1.setXscale(-0.5, size + 0.5);
		draw1.setYscale(-0.5, size + 0.5);
		draw1.clear(Draw.LIGHT_GRAY);
		draw1.setPenRadius(0.005);


        //if the tiling is generate, we are going to mix it with a few random flips.
		double x, y, s; //coordinates of a random point and a random choice of direction
		//of the flip
		int success; //equals 1 if the flip was successful
		if (!option.equals("File")) {
			int ITER = 10; // number of random flips
			for (int k = 0; k < ITER; k++) {
				x = StdRandom.uniform(0.,(double) vz.M);
				y = StdRandom.uniform(0.,(double) vz.N);
				s = StdRandom.uniform(0.,1.);
				success = vz.tiling.randomFlip(x, y, s);
			}
		}
		vz.tilingCopy = new RibTiling(vz.tiling); //make a copy in case we want to reset.
        
		vz.draw(draw1);	//drawing the tiling
		vz.computeSheffieldGraph(vz.tiling);
		vz.G.reduce();  //compute the cover graph for the Sheffield Graph 
		vz.G.draw(draw1); //draw the cover graph

        //Second part of the program: Making some Manual flips.
		Draw draw2 = new Draw("Tiling for choosing tiles for flip and displaying results");
		draw2.setXscale(- 0.5, size + 0.5);
		draw2.setYscale(-0.5, size + 0.5);
		draw2.setLocationOnScreen(512, 1);
		draw2.clear(Draw.LIGHT_GRAY);
		draw2.setPenRadius(0.005);
		vz.draw(draw2);	//drawing the tiling
		vz.G.draw(draw2); //draw the graph

		// Manual flips 
		double xCoord, yCoord;
		while (true) {
			StdOut.println("Choose coordinates of the first tile or click outside of the board to reset: ");

			// wait for a mouse click 
			while (true) {
				if (draw2.mousePressed()) {
					xCoord = draw2.mouseX();
					yCoord = draw2.mouseY();
					break;
				}
			}

			RibTile t1 = vz.tiling.findTile(xCoord, yCoord);
			if (t1 != null) {
				t1.print();
				StdOut.println();
			} else {
				// resetting the tiling back to the state before manual flips.
				vz.tiling = new RibTiling(vz.tilingCopy);
				vz.tiling.draw(draw2);
				vz.computeSheffieldGraph(vz.tiling);
				vz.G.reduce();  //compute the cover graph
				vz.G.draw(draw2); //draw the graph
				while (draw2.mousePressed()) {
					continue; //waiting for the end of the click
				}
				continue;
			}

			while (draw2.mousePressed()) {
				continue; //waiting for the end of the click
			}


			StdOut.println("Enter coordinates of the second tile: ");
			while (true) {
				if (draw2.mousePressed()) {
					xCoord = draw2.mouseX();
					yCoord = draw2.mouseY();
					break;
				}
			}

			RibTile t2 = vz.tiling.findTile(xCoord, yCoord);
			t2.print();
			StdOut.println();
			while (draw2.mousePressed()) {
				continue; //waiting for the end of the click
			}


			// a check that these two tiles are flippable.
			if (vz.tiling.isFlip(t1,t2)) {
				vz.tiling.flip(t1, t2);
				vz.draw(draw2);	//drawing the tiling
				vz.computeSheffieldGraph(vz.tiling);
				vz.G.reduce();  //compute the cover graph
				vz.G.draw(draw2); //draw the graph
			} else {
				StdOut.println("These tiles can't be flipped.");
			}
		}
	}
	
}

