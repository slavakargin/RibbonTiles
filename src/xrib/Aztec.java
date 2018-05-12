package xrib;
import java.util.ArrayList;
import java.util.TreeSet;
import edu.princeton.cs.algs4.Stopwatch;
import edu.princeton.cs.algs4.StdOut;
//import edu.princeton.cs.algs4.Draw;

public class Aztec {
	
	public static void exampleDomino(int N) {
		int n = 2;
		int ITER = 2048001;
		XRibTile tile;
		ArrayList<Integer> shapeI = new ArrayList<Integer>(2 * N);
		ArrayList<Integer> shapeF = new ArrayList<Integer>(2 * N);
        for (int i = 0; i < N; i++) {
        	shapeI.add(N - i - 1);
        	shapeF.add(N + i);
        }
        for (int i = 0; i < N; i++) {
        	shapeI.add(i);
        	shapeF.add(2 * N - i - 1);
        }
        
		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);
		for (int i = 0; i < shapeI.size(); i++) {
			for (int j = 0; j < shapeF.get(i) - shapeI.get(i) + 1; j ++) {
			   //S++;	
               bag.add(new Square(shapeI.get(i) + j, i)); 
			}
		}	
		XRibTiling xrt = new XRibTiling(n, bag, "Aztec");
        
        /**
         * In this portion of the code we start with a standard tiling, mix it, and show the result.
         *
         */
      
        //Initial tiling
        for (int i = 0; i < N; i++) {
        	for (int j = 0; j < i + 1; j++){
        	tile = new XRibTile(N - i - 1 + 2 * j, i, "0");
        	xrt.addTile(tile);
        	}
        }
        for (int i = 0; i < N; i++) {
        	for (int j = 0; j < N - i; j++) {
        	tile = new XRibTile(i + 2 * j, N + i, "0");
        	xrt.addTile(tile);
        	}
        }

        //mix tiling
        XUtility.Glauber(xrt, ITER);
    	xrt.draw(); 		
	}
	
	public static void exampleAztecFish(int N, int ITER) {
		int n = 3;
		//int ITER = 2056000;
		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();
        for (int i = 0; i < N; i++) {
        	shapeI.add(N - i - 1);
        	shapeF.add(N + 2 * i + 1);
        }
        for (int i = 0; i < N; i++) {
        	shapeI.add(i); 
        	shapeF.add(3 * N - 2 * i - 1);
        }
		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);
		XRibTiling xrt = new XRibTiling(n, bag, "Test");
		Stopwatch st = new Stopwatch();
		XUtility.Glauber(xrt, ITER);
		StdOut.println("Elapsed time = " + st.elapsedTime());
		xrt.draw();
		xrt.H.calcHeightInside();
		xrt.H.saveHeight("Height_Aztec_" + N + "_" + ITER + ".txt");
	}
	
	public static void exampleSquare(int N, int ITER) {
		int n = 3;
		//int ITER = 2056000;
		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();
        for (int i = 0; i < N; i++) {
        	shapeI.add(0);
        	shapeF.add(N);
        }
		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);
		XRibTiling xrt = new XRibTiling(n, bag, "Test");
		Stopwatch st = new Stopwatch();
		XUtility.Glauber(xrt, ITER);
		StdOut.println("Elapsed time = " + st.elapsedTime());
		xrt.draw();
		xrt.H.calcHeightInside();
		//xrt.H.draw();
		xrt.H.saveHeight("Height_Square_" + N + "_" + ITER + ".txt");
	}

	public static void main(String[] args) {
		
		//exampleDomino(N);
		int N = 20;
		//int n = 3;
		//int ITER = 4001000; // took around 40 minutes in the old version of the program (which did not recalculate graph)
		//int ITER = 20560; //took around 20 seconds in the new version 
		int ITER = 100000; //100,000 iterations took around 88 seconds = 1.5 minutes in the new version. 
		//int ITER = 1;
		//exampleAztecFish(N, ITER);
		N = 21; //N = 20 does not work for the square if n = 3.
		exampleSquare(N, ITER);
		
		/*
		 * 
		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();
        for (int i = 0; i < N; i++) {
        	shapeI.add(N - i - 1);
        	shapeF.add(N + 2 * i + 1);
        }
        for (int i = 0; i < N; i++) {
        	shapeI.add(i); 
        	shapeF.add(3 * N - 2 * i - 1);
        }
		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);
		XGraph xG = new XGraph(n, bag, "Test");
		xG.buildTiling();
		Stopwatch st = new Stopwatch();
		xG.xrt.Glauber(ITER);
		StdOut.println("Elapsed time = " + st.elapsedTime());
		xG.xrt.draw();
		*/
	}
}
