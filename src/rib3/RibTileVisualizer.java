
package rib3;
import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;
//import edu.princeton.cs.algs4.StdRandom;

import java.util.Scanner;
import java.util.ArrayList;

public class RibTileVisualizer {
	int N, M; //height and width of the tiling.
	public int nF, nH, nV, nL; //number of flips, horizontal, vertical and Gamma tiles.
	public RibTiling tiling, tiling2, tilingCopy;
	public TilingConnector tc;
	//public SheffieldGraph G;
	static Scanner sc = new Scanner(System.in);
	static final int n = 3;
	public Draw window1, window2, window3, window4, wPlot; //windows for drawing 
	int size; //size of the window inside the canvas.
	int canvasSizeW = 480; 
	int canvasSizeH = 400; 
	int offsetX = 480;
	int offsetY = 460;

	/**
	 * Construct a random tiling of an N-by-M rectangle starting with a initial tiling of a 
	 * given type and using the number ITER of random exchanges.
	 */
	public RibTileVisualizer(int N, int M, int type, int type2) {		
		tiling = new RibTiling(N,M,type, this); //initialize a tiling 
		tiling2 = new RibTiling(N,M,type2, this); //initialize a second tiling
		this.N = N;
		this.M = M;
		window1 = new Draw("Tiling 1");
		window2 = new Draw("Tiling 2");
		window3 = new Draw("Tiling 3");
		//window4 = new Draw("Last Tile");
		size = N; 
		if (M > N) size = M; 
		setWindow(window1, size, 0, 0);
		setWindow(window2,size, offsetX, 0);
		setWindow(window3, size, 0, offsetY);
		//setWindow(window4, size, offsetX, offsetY);
		/*
        if (silent == 0) { //this is remainder from the time when I draw some statistics in a separate window.
        setWindow(wPlot,size,768, 0);
		for (int i = 1; i < 10; i++) {
			wPlot.setPenRadius(0.005);
			wPlot.line(size + 1, i / 10.0 * size, size + 1.5, i / 10.0 * size);
			wPlot.textLeft(size + 2, i / 10.0 * size, i + "/10");
			wPlot.setPenRadius(0.002);
			wPlot.line(0, i / 10.0 * size, size + 2, i / 10.0 * size);
		}
        }
		 */
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
		setWindow(window1, size, 0, 0);
		setWindow(window2,size, offsetX, 0);
		setWindow(window3, size, 0, offsetY);
		tiling.draw(window1);	//drawing the tiling
		tiling.G.draw(window1); //draw the cover graph
		tiling.draw(window2);	
		tiling.G.draw(window2);
	}

	/*
	 * set default properties for windows to draw.
	 * 
	 */
	private void setWindow(Draw dr, int size, int offsetX, int offsetY) {
		dr.setCanvasSize(canvasSizeW, canvasSizeH);
		dr.setLocationOnScreen(1 + offsetX, 1 + offsetY);
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

	public void saveTiling(String fn) {
		StdOut.println("Saving tiling1 " + tiling.G.DG);
		tiling.save(fn);
	}

	public void getCopy(String fn) {
		In in = new In(fn);
		tilingCopy = new RibTiling(in, this);
		in.close();
		StdOut.println("Loading original tiling1 " + tilingCopy.G.DG);
	}

	/*******************
	 * MAIN 
	 */

	public static void main(String[] args) {
		RibTileVisualizer vz;
		////////////////////////////////////
		/*
		 *If the tiling is not loaded from a file we will use the following parameters.
		 */
		////////////////////////////////////
		int N = 3; //height of the rectangle to tile
		int M = 6; //width
		int ITER = 1000; //number of iterations for mixing
		int type = 0; //type of initial tiling (from 0 to 5)
		int type2 = 0;
		                  //0 - all vertical
		                  //1 - all horizontal
		                  //2 - szepka of Gamma and mirrored L
		                   //3 another type of szepka
		                  // 4 = formed from 5 * 3 blocks (5 in height and 3 in width
		                    // 5 - formed from 3 * 5 blocks
		String mark = "vertex"; //This determines how each tile is marked. Possible choices:
		                         //"vertex", "level", "none" 
		int stage = 3; //this is the number of vertices processed
        
		////////////////////////////////
		//
		//Generate or load  tilings
		//When generating, two tilings are created
		//
		////////////////////////////////
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
			vz = new RibTileVisualizer(N, M, type, type2); 
			vz.tiling.mix(ITER);
			vz.tiling2.mix(ITER);
		}
		vz.saveTiling("savedTiling.txt");
		vz.getCopy("SavedTiling.txt");

		
		/////////////////////////////////////
		//
		//Draw tilings
		//
		////////////////////////////////////
		vz.tiling.draw(vz.window1, mark);
		vz.tiling.G.draw(vz.window1);
		vz.tiling2.draw(vz.window2, mark);
		vz.tiling2.G.draw(vz.window2);
		
		
		/////////////////////////////////////
		//
		//Connecting tilings with flips
		//
		////////////////////////////////////////

		vz.tc = new TilingConnector(vz.tiling, vz.tiling2, vz);
		int V = vz.tc.V;
		
		//Show sink sequences:
		StdOut.println("Sink Sequences:");
		StdOut.println(vz.tc.sinkSeq1);
		StdOut.println(vz.tc.sinkSeq2);
	
		
		//Show heights:
		StdOut.println("Heights:");
		StdOut.println(vz.tc.height1);
		StdOut.println(vz.tc.height2);
		
		StdOut.println("Levels:");
		for (int v = 0; v < vz.tiling.V; v++){
		StdOut.print(vz.tiling.G.level(v) + ", ");
		}
		StdOut.println();
		vz.tc.connectByHeight(stage);
    	//ManualFlipHandler mfh = new ManualFlipHandler("savedTiling.txt");
    	//mfh.doFlips();
		
		ArrayList<Integer> codeHeight = vz.tc.computeCode(stage);
		StdOut.println("CodeHeight at stage " + stage + " is ");
		StdOut.println(codeHeight);
		
		Digraph codeGraph = vz.tc.computeCodeGraph(stage);
		StdOut.println("CodeGraph at stage " + stage + " is ");
		StdOut.println(codeGraph);
		
		
		DirectedCycle dc = new DirectedCycle(codeGraph);
		Boolean flag = dc.hasCycle();
		StdOut.println("CodeGraph has cycle?");
		StdOut.println(flag);
		if (flag) {
			for (int v : dc.cycle()){
				StdOut.println(v);
			}
		}
		/*
		RibTile lastTile = vz.tiling.G.labels.get(vz.tc.sinkSeq1.get(vz.tc.V - stage)); //This is the last tile processed.ribTile
		StdOut.println(lastTile);
	    lastTile.drawSpecial(vz.window3, "white");
	    vz.window3.show();
	    */
		StdOut.println("Original Height1 presented in the order given by the second tiling: ");
		for (int i = 0; i < V; i++) {
			int v = vz.tc.height2.indexOf(i);
			StdOut.print(vz.tc.originalHeight1.get(v) + ", ");
		}
		StdOut.println();
		StdOut.println("Reconstruction codeHeight presented in the order given by the second tiling: ");
		ArrayList<Integer> code = vz.tc.computeCode(stage);
		for (int i = 0; i < V; i++) {
			int v = vz.tc.height2.indexOf(i);
			StdOut.print(code.get(v) + ", ");
		}
		StdOut.println();
		TilingReconstructor tr = new TilingReconstructor(N, M, vz.tiling.G.DG, codeGraph, vz.canvasSizeW, vz.canvasSizeH, vz.offsetX, vz.offsetY); 	
		tr.draw(tr.rt.myDr);	    
	}
}

