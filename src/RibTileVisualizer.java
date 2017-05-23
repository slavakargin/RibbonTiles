import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

public class RibTileVisualizer {
	public RibTiling tiling;
	public SheffieldGraph G;
	
	public RibTileVisualizer(int N, int M, int type) {
		//tiling = new RibTiling(N,M,3); //initiate a``szepka'' tiling
	    //tiling = new RibTiling(N,M,2); //initiate another ``szepka'' tiling
	    //tiling = new RibTiling(N,M,0); //initiate a vertical tiling
	    //tiling = new RibTiling(N,M,1); //initiate a horizontal tiling
	    //tiling = new RibTiling(N,M,4); //initiate a type 4 tiling
		
	    tiling = new RibTiling(N,M,type); //initiate a type 5 tiling 
	}
	
	public void draw(Draw dr) {
		tiling.draw(dr);
		dr.show(40);
	}
	
	public void computeSheffieldGraph() {
		 G = new SheffieldGraph(tiling);
	}
	

	public static void main(String[] args) {
		int N = 6; //size of the square to tile
		int M = 10;
		int type = 5;
		RibTileVisualizer vz = new RibTileVisualizer(N, M, type); 

		double x, y, s; //coordinates of a random point and a random choice of direction
		             //of the flip
		int success; //equals 1 if the flip was successful
			
		
		//Setting up the window for the tiling
				Draw draw1 = new Draw("Tiling");
				int size = N; 
				if (M > N) size = M; 
				draw1.setXscale(-0.5, size + 0.5);
				draw1.setYscale(-0.5, size + 0.5);
				draw1.clear(Draw.LIGHT_GRAY);
				draw1.setPenRadius(0.005);

		
		int ITER = 30; // number of iterations
		
		for (int k = 0; k < ITER; k++) {
		x = StdRandom.uniform(0.,(double) M);
		y = StdRandom.uniform(0.,(double) N);
		s = StdRandom.uniform(0.,1.);
		
		success = vz.tiling.randomFlip(x, y, s);
				
		vz.draw(draw1);	//drawing the tiling
		}
		vz.computeSheffieldGraph();
		vz.G.reduce();
		vz.G.draw(draw1); //drawing the graph
		/*
		tiling.calcHeight();
		tiling.height.modify();
		tiling.height.print();
		tiling.height.calcRouting();
		tiling.height.printRouting();
		tiling.height.drawRouting(draw1);
		*/
		
		
		/*
		RibTiling tiling = new RibTiling(N,0); //initiate a vertical tiling
		RibTile tile1 = new RibTile(0, 0, 1, 3, 0);
		RibTile tile2 = new RibTile(1, 0, 2, 3, 0);

		
	    tiling = new RibTiling(N,1); //initiate a horizontal tiling
	    tile1 = new RibTile(0, 0, 3, 1, 1);
        tile2 = new RibTile(0, 1, 3, 2, 1);
		*/
		/*
	    RibTiling tiling = new RibTiling(N,2); //initiate a ``szepka'' tiling
	    RibTile tile1 = new RibTile(0, 0, 2, 2, 2);
        RibTile tile2 = new RibTile(1, 0, 3, 2, 3);
        tile1 = new RibTile(1, 2, 3, 4, 3);
        tile2 = new RibTile(3, 2, 5, 4, 2);
        */
		
		/*
	    RibTiling tiling = new RibTiling(N,3); //initiate another``szepka'' tiling
	    RibTile tile1 = new RibTile(2, 3, 3);
        RibTile tile2 = new RibTile(2, 4, 2);
		tiling.flip(tile1, tile2);
        tile1 = new RibTile(0, 3, 3);
        tile2 = new RibTile(2, 3, 0);
        */
		//tile1 = new RibTile(2, 2, 3, 5, 0);
		//tile2 = new RibTile(0, 2, 2, 4, 3);

       
		/*
		RibTiling tiling = new RibTiling(N,4); //initiate a type 4 tiling
		RibTile tile1 = new RibTile(0, 0, 1, 3, 0);
		RibTile tile2 = new RibTile(1, 1, 2, 4, 0);
				*/
		/*
		RibTiling tiling = new RibTiling(N,5); //initiate a type 5 tiling tiling`
		StdOut.println("Tiling is valid: " + tiling.isValid());
		RibTile tile1 = new RibTile(0, 0, 3, 1, 1);
		StdOut.println("tiling contains tile1: " + tiling.contains(tile1));
		RibTile tile2 = new RibTile(1, 1, 4, 2, 1);
		StdOut.println("tiling contains tile2: " + tiling.contains(tile2));
		*/
		//tiling.flip(tile1, tile2);
	}
}
