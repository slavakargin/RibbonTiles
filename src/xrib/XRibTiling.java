package xrib;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;
//import java.util.Random;

import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

public class XRibTiling {
	  //conceptual variables
	    private ArrayList<XRibTile> tiling; //An ordered set of tiles in the tiling. The order is determined by the 
	                                        //order of the lowest square of each tile. It is different from Sheffield's partial order.
	                                           //It can be empty if we have not yet 
	                                           //tiled the region
		  //Geometric variables
	    int n; //length of the ribbon
	    XShape shape; 
	    StaticGraph staticDG; //This is the graph which depends only on the shape of the region and not on the tiles 
	    XGraph xG; //Graph associated to the tiling. 

		TreeMap<Square, XRibTile> square2tile; //map that shows which tile covers a given square.
		XHeight H; //height function
        // ArrayList<Integer> heightDiffs; //difference of heights at the ends of intervals at which line x + k intersect the region

      //Variables for visualization
		String label;
		
		/**
		 * Creates a default tiling from a bag of squares. Calculates height at the border, 
		 * crossing points for intersection of the level curves x + y = l and the region, 
		 * and difference of heights at these crossing points, which is needed to calculate
		 * how many tiles are in each level.
		 * 
		 * 
		 * @param n
		 * @param bag
		 * @param label
		 */
		
		XRibTiling(int n, TreeSet<Square> bag, String label) {
			this.n = n;
			this.label = label;
			//tiling = new TreeSet<XRibTile>(new XTiles2ndComp()); //
			tiling = new ArrayList<XRibTile>();
			square2tile = new TreeMap<Square, XRibTile>();
			shape = new XShape(bag);
			
			
			if (shape.squares.size() % n != 0) {
				StdOut.println("An error in XGraph constructor.");
				StdOut.println("The number of squares in the shape is " + shape.squares.size()  
				                + ". It must be divisible by " + n);
				StdOut.println("Quitting ...");
			}
			
			boolean test = isTileable();
			if (!test) {
				StdOut.println("The region is not tileable. The numbers of squares of different colors are not the same.");
				int[] count = countColoredTiles();
				StdOut.println("The count is ");
				for (int i = 0; i < n; i++) {
					StdOut.print(count[i] + "; ");
				}
				StdOut.println();
			};
			//initialize tiling 
			for (int v = 0; v < shape.squares.size()/n; v++){
				tiling.add(null);
			}
			//We calculate the height at the border of the shape and the structure of the associated graph
			H = new XHeight(this);
			//creating the associated digraphs
			staticDG = new StaticGraph(this);
			buildTiling(); //build the default tiling. 
			xG = new XGraph(this);
			H.calcHeightInside();
		}
		
		
		
		/**
		 * Creates a tiling from a bag of squares and a sinkSequence Calculates height at the border, 
		 * crossing points for intersection of the level curves x + y = l and the region, 
		 * and difference of heights at these crossing points, which is needed to calculate
		 * how many tiles are in each level.
		 * 
		 * 
		 * @param n
		 * @param bag
		 * @param label
		 */
		
		XRibTiling(int n, TreeSet<Square> bag, ArrayList<Integer> sinkSeq) {
			this.n = n;
			//tiling = new TreeSet<XRibTile>(new XTiles2ndComp()); //
			tiling = new ArrayList<XRibTile>();
			square2tile = new TreeMap<Square, XRibTile>();
			shape = new XShape(bag);
			
			
			if (shape.squares.size() % n != 0) {
				StdOut.println("An error in XGraph constructor.");
				StdOut.println("The number of squares in the shape is " + shape.squares.size()  
				                + ". It must be divisible by " + n);
				StdOut.println("Quitting ...");
			}
			
			boolean test = isTileable();
			if (!test) {
				StdOut.println("The region is not tileable. The numbers of squares of different colors are not the same.");
				int[] count = countColoredTiles();
				StdOut.println("The count is ");
				for (int i = 0; i < n; i++) {
					StdOut.print(count[i] + "; ");
				}
				StdOut.println();
			};
			//initialize tiling 
			for (int v = 0; v < shape.squares.size()/n; v++){
				tiling.add(null);
			}
			//We calculate the height at the border of the shape and the structure of the associated graph
			H = new XHeight(this);
			//creating the associated digraphs
			staticDG = new StaticGraph(this);
			buildTiling(sinkSeq, shape.squares.size()/n); //build the tiling from sinkSeq. 
			xG = new XGraph(this);
			H.calcHeightInside();
		}
		
		
		/**
		 * Creates a copy of a tiling "other"
		 * @param other
		 */
		
		XRibTiling(XRibTiling other) {
           this.n = other.n;
           this.label = other.label;
           //copy shape, staticDG, tiling and square2tile structures
           this.shape = new XShape(other.shape);
           this.staticDG = new StaticGraph(other.staticDG);
		   tiling = new ArrayList<XRibTile>(other.tiling);
		   square2tile = new TreeMap<Square, XRibTile>(other.square2tile);

           //adding graph and height at the border
           this.xG = new XGraph(this);     
			H = new XHeight(this);
		}
		
		
		/**
		 * calculates the first k titles of a tiling using a given sinkSequence.
		 * @param k
		 */
	    void buildTiling(ArrayList<Integer> sinkSeq, int k) {
			int v, l0;
			Square s0 = null;
			XRibTile tile;
			TreeSet<Square> bag = new TreeSet<Square>(shape.squares);	 
			
			 for (int count = 0; count < k; count++) {
			v = sinkSeq.get(count);
			l0 = staticDG.vertex2level.get(v);
			//we need to find the smallest squares at levels from l0 to l0 + n - 1 in our bag.
			//As we go by we need to check that they are close to each other. 
			//find the first ("root") square
			for (Square s : bag) {
				if (s.x + s.y == l0) {
					s0 = new Square(s);
					break;
				}
			}
			//get the tile with the root at s0 and on the left border
			tile = XUtility.getBorderTile(s0, n, bag);
			bag.removeAll(tile.squares());
		    addTile(v, tile);
			 }	
		}
		
		
	    public void buildTiling() {
			buildTiling(shape.squares.size()/n);
		}
		
		/**
		 * calculates the first k titles of a tiling
		 * @param k
		 */
		
		 void buildTiling(int k) {
			ArrayList<Integer>  sinkSeq = XUtility.findSinkSequence(staticDG.sG);
			buildTiling(sinkSeq, k);
		}
		
		
		
		/**
		 * Returns a copy of the TreeSet that contains all the tiles in the tiling. 
		 * @return
		 */
		public ArrayList<XRibTile> tiles() {
			return new ArrayList<XRibTile>(tiling);
		}
		
		
		
		public void addTile(XRibTile tile) {
			int level = tile.level;
			int v = tile.xmin - shape.crosses.get(level).get(0) + staticDG.startLevel.get(level);
            addTile(v, tile);			
		}
		
		/**
		 * a method to add a tile to the tiling. It updates square2tile structure as well.
		 * but it does not recalculate graph since the static graph of forced edges does not change
		 * and the dynamic graph is too early to calculate;
		 * 
		 *  
		 * @param v the vertex  
		 * @param tile the corresponding tile
		 */
		public void addTile(int v, XRibTile tile) {
			XRibTile t = new XRibTile(tile);
			tiling.set(v, t);
			for (Square square : t.squares()) {
				square2tile.put(square, t);
			}
		}
		
		/**
	     * Checks if a tile belongs to this tiling
		 * 
		 */
		Boolean contains(XRibTile tile) {
			return tiling.contains(tile);
		}
		
		
		/**
		 * Tests tileability of the tiling by checking if the shape has the same
		 * number of squares of each color.
		 */
		Boolean isTileable(){
			int[] count = new int[n]; // counter for the colors 
			                          //(the calculation is up to a constant, same for each color)
			int color;
			for (Square s: shape.squares) {
				color = (s.x + s.y + n) % n;
				count[color]++;
			}
			for (int i = 0; i < n - 1; i++) {
				if (count[i] != count[i + 1]) return false;
			}
			return true;
		}
		
		private int[] countColoredTiles(){
			int[] count = new int[n]; // counter for the colors 
            //(the calculation is up to a constant, same for each color)
            int color;
            for (Square s: shape.squares) {
                 color = (s.x + s.y + n) % n;
                 count[color]++;
            }
            return count;
		}
		
		
		/**
		 * Finds a tile that contain point (x, y)
		 * 
		 * @param x
		 * @param y
		 * @return XRibTile tile
		 */
		XRibTile findTile(double x, double y) { // we assume that x and y are not integer 
			//so they lie properly inside a tile, or completely outside the shape. 
			// first, we need to check if the point is inside our shape
			
			Square square = new Square((int) x, (int) y);
			XRibTile tile = square2tile.get(square);
			return tile;	
		}
		
		
		
		
		/** 
		 * flip a pair of ordered tiles. It is assumed that the pair is flippable. 
		 * 
		 */	
		
		public boolean flip(XRibTile tile1, XRibTile tile2) {
			int v1, v2;
			XRibTile ntile1, ntile2;
			//check the assumptions
			if (!isFlip(tile1, tile2)) {
				StdOut.println("failed to flip tiles  " + tile1 
						+ " and " + tile2);
				StdOut.println("They are not flippable");
				return false;
			}
			if (!tiling.contains(tile1)) {
				StdOut.println("failed to flip tiles  " + tile1 
						+ " and " + tile2);
				StdOut.println("Tile " + tile1 + " is not in the tiling");
				return false;
			}
			if (!tiling.contains(tile2)) {
				StdOut.println("failed to flip tiles  " + tile1 
						+ " and " + tile2);
				StdOut.println("Tile " + tile2 + " is not in the tiling");
				return false;
			}
			
			//then calculate tiles after the flip
			//We denote them ntile1 and ntile2
			TreeSet<Square> squares = new TreeSet<Square>(tile1.squares());
			squares.addAll(tile2.squares());
			XShape twoTiles = new XShape(squares);
			int l0 = twoTiles.Lmin;
			Square s0 = null; //find the first ("root") square
			for (Square s : squares) {
				if (s.x + s.y == l0) {
					s0 = new Square(s);
					break;
				}
			}
			ntile1 = XUtility.getBorderTile(s0, n, squares);
			squares.removeAll(ntile1.squares());
			ntile2 = new XRibTile(squares);
			if (ntile1.equals(tile1) || ntile1.equals(tile2)) { //these are the same old tiles, need a different procedure
                                     //let us find the beginning of the new tile. It is where the width of twoTiles is 2. 
				squares = new TreeSet<Square>(tile1.squares());
				squares.addAll(tile2.squares());
				for (int k = 0; k < twoTiles.crosses.size(); k++) {
					if (twoTiles.crosses.get(k).size() > 0 
							&& twoTiles.crosses.get(k).get(1) - twoTiles.crosses.get(k).get(0) > 1) {
						    l0 = k - 1;
							break;
						}
				}
				//find the first ("root") square
				for (Square s : squares) {
					if (s.x + s.y == l0) {
						s0 = new Square(s);
						break;
					}
				}
				ntile1 = XUtility.getBorderTile(s0, n, squares);
				squares.removeAll(ntile1.squares());
				ntile2 = new XRibTile(squares);
			}
			
			//Update the graph 
			v1 = tiling.indexOf(tile1);
			v2 = tiling.indexOf(tile2);
			//StdOut.println("graph before flip is " + xG.DG);
			if (tile1.compareTo(tile2) > 0) { //tile2 is to the left of tile1, and v1 -> v2
				//StdOut.println("Flipping edge (" + v1 + " -> " + v2 + ").");
				xG.DG.removeEdge(v1, v2);
				xG.DG.addEdge(v2, v1);
			} else {
				//StdOut.println("Flipping edge (" + v2 + " -> " + v1 + ").");
				xG.DG.removeEdge(v2, v1);
				xG.DG.addEdge(v1, v2);
			}
			//StdOut.println("graph after flip is " + xG.DG);
			if (!xG.DG.validate()) {
				StdOut.println(" A duplicate edge was introduced when we tried to exchange tiles "
						+ tile1 + " corresponding to v1 = " + v1);
				StdOut.println("  and " + tile2 + " corresponding to v2 = " + v2);
				StdOut.println("Offending graph is as follows:" + xG.DG);
				StdOut.println("Compare function for " + tile1 + " and " + tile2 
						+ " gives  " + tile1.compareTo(tile2)  );
				return false;
			}
			
			//update the tiling structure
			if (xG.DG.isEdge(v1,v2)) { //v1 -> v2 in graph
				 if (ntile1.compareTo(ntile2) > 0) { //ntile1 -> ntile2 in tiling
					tiling.set(v1, ntile1);
					tiling.set(v2, ntile2);
				} else { 
					tiling.set(v1, ntile2);
					tiling.set(v2, ntile1);
				}
			} 
			if (xG.DG.isEdge(v2, v1)){ //v2 -> v1 in graph
				 if (ntile2.compareTo(ntile1) > 0) { //ntile2 -> ntile1 in tiling
					tiling.set(v1, ntile1);
					tiling.set(v2, ntile2);
				} else { 
					tiling.set(v1, ntile2);
					tiling.set(v2, ntile1);
				}
			}
			
			//Remove tile1 and tile2 from square2tile map	
			for (Square square : tile1.squares()) {
				square2tile.remove(square);
			}
			for (Square square : tile2.squares()) {
				square2tile.remove(square);
			}
			//Add new tiles to square2tile map
			for (Square square : ntile1.squares()) {
				square2tile.put(square, ntile1);
			}
			for (Square square : ntile2.squares()) {
				square2tile.put(square, ntile2);
			}
			
			/*
			if (!checkGraph()) { //this assertion will slow down things and should be 
                //commented out after debugging
               StdOut.println("CheckGraph returned false. Current graph is " + xG.DG);
              return false; 
             }
			*/
			//update height -- this might slow down the program, since it recalculates all heights. 
			// a specialized update function might be helpful. 
			//H = new XHeight(this);
			return true;
		}

		/**
		 * check it two tiles are flippable
		 * 
		 * @param tile1
		 * @param tile2
		 * @return true if the pair of tiles is flippable. 
		 */
		 boolean isFlip(XRibTile tile1, XRibTile tile2) {
			TreeSet<Square> squares = new TreeSet<Square>(tile1.squares());
			squares.addAll(tile2.squares());
			XShape twoTiles = new XShape(squares);
			ArrayList<ArrayList<Integer>> weakCrosses = twoTiles.calculateWeakCrosses();
			//First of all the crosses should have only single intervals. 
			for (int k = 0; k < weakCrosses.size(); k++) {
				if (weakCrosses.get(k).size() > 2) {
					return false;
				}
			}
			//Second, the size of at least one of this intervals should be 2. 
			int width = 0;
			for (int k = 0; k < weakCrosses.size(); k++) {
				if (weakCrosses.get(k).size() > 0) {
					if  (weakCrosses.get(k).get(1) - weakCrosses.get(k).get(0) > 1) {
						width = 2;
						break;
					}
				}
			}
			if (width < 2) { 
			   return false;
			}
			return true;
		}

		/**
		 * Return all the flips that involve a given tile. 
		 * 
		 * @param tile
		 * @return
		 */
		
	   ArrayList<XRibTile> findFlips(XRibTile tile) {
			XRibTile otherTile;
			ArrayList<XRibTile> flips = new ArrayList<XRibTile>();
			/*
			for (Square s : shape.squares){
			    StdOut.println(s + " -> " + square2tile.get(s));
			}
			*/
			otherTile = findTile(tile.xmin - 0.5, tile.ymin + 0.5);
			//StdOut.println("OtherTile = " + otherTile);
			if (otherTile != null && isFlip(tile, otherTile)) {
				flips.add(otherTile);
			}
			otherTile = findTile(tile.xmin + 0.5, tile.ymin - 0.5);
			if (otherTile != null && isFlip(tile, otherTile)) {
				flips.add(otherTile);
			}
			otherTile = findTile(tile.xmax + 0.5, tile.ymax - 0.5);
			//StdOut.println("Other tile is " + otherTile);
			if (otherTile != null && isFlip(tile, otherTile)) {
				flips.add(otherTile);
			}
			otherTile = findTile(tile.xmax - 0.5, tile.ymax + 0.5);
			if (otherTile != null && isFlip(tile, otherTile)) {
				flips.add(otherTile);
			}
			return flips;
		}
	   
	   
	   /**
	    * Checks if the graph xG is consistent with the current tiling;
	    */
	   private boolean checkGraph() {
		   int V = shape.squares.size()/n;
		   for (int u = 0; u < V; u++) {
			   for (int v = 0; v < V; v++) {
				   if (v == u) continue;
				   if (tiling.get(u).compareTo(tiling.get(v)) > 0 && !xG.DG.isEdge(u, v)) {
					   StdOut.println("Tile " + tiling.get(u) + " > tile " +  tiling.get(v) 
					       + " but there is no edge from the corresponding vertex " + u + " to vertex " + v);
					   
					   return false;
				   }
				   if (xG.DG.isEdge(u, v) && tiling.get(u).compareTo(tiling.get(v)) <= 0) {
					   StdOut.println("There is an edge from vertex " + u + " to vertex " + v 
							   + " but tile " + tiling.get(u) + " <= tile " +  tiling.get(v));
					   return false;
				   }
			   }
		   }
		   return true;
	   }
	   
	   private void printTiling() {
		   for (int v = 0; v < tiling.size(); v++) {
			   StdOut.println(v + " -> " + tiling.get(v));
		   }
	   }
		
		/**
		 * This method realizes the Glauber dynamics on the tiling. A tile is chosen at random.
		 * Then a flip is chosen at random (if possible) and performed. This procedure is repeated
		 * for ITER iterations. 
		 * 
		 * @param ITER number of steps in the Glauber dynamics
		 */
	   /*
		void Glauber(int ITER){
			XRibTile tile, otherTile;
			ArrayList<XRibTile> flips;
			int randNum;
           //StdRandom.setSeed(17);
           for (int i = 0; i < ITER; i++) {
        	   if (i % 1000 == 0) {
        		   StdOut.println("Iteration " + i);
        	   }
           randNum = StdRandom.uniform(tiling.size());
           tile = tiling.get(randNum);
           flips = findFlips(tile);
           if (flips != null && flips.size() > 0) {
             randNum = StdRandom.uniform(flips.size());
             otherTile = flips.get(randNum);
             boolean flag = flip(tile, otherTile);
             if (!flag) { //flag == false means that there was a problem with this flip.
            	 return;
             }
           } else {
        	   continue;
           }
           }
		}
		
		*/

		
		public void draw() {
			shape.draw();
			draw(shape.myDr);			
		}
		
		public void draw(Draw dr) {
			for (XRibTile tile : tiling) {
				tile.draw(dr);
			}
		}

	/**
	 * For testing methods.
	 */
	public static void main(String[] args) {

		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();
		//XRibTile tile;
		
		
		/* Test case 1
		 * 	
		 */	
		int n = 4;
		int N = 6; 
		for (int i = 0; i < N; i ++){
		shapeI.add(0);
		shapeF.add(7);
		}
		for (int i = 0; i < N/2; i++){
			shapeF.set(i, 3);
		}
		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);	
		XRibTiling xrt = new XRibTiling(n, bag, "Test");
       /* tile = new XRibTile(0, 0, "010");
		xrt.addTile(tile);
		tile = new XRibTile(2, 0, "110");
		xrt.addTile(tile);
		tile = new XRibTile(3, 1 + 3, "010");
		xrt.addTile(tile);
		tile = new XRibTile(0, 1, "001");
		xrt.addTile(tile);
		tile = new XRibTile(0, 3, "001");
		xrt.addTile(tile);
		tile = new XRibTile(1, 3, "000");
		xrt.addTile(tile);
		tile = new XRibTile(5, 0 + 3, "100");
		xrt.addTile(tile);
		tile = new XRibTile(5, 1 + 3, "010");
		xrt.addTile(tile);
		tile = new XRibTile(0, 5, "000");
		xrt.addTile(tile);
		*/
		
		//Test case 2
		/*
		int N = 4;
		int n = 3;
		int ITER = 2048001;
        for (int i = 0; i < N; i++) {
        	shapeI.add(N - i - 1);
        	shapeF.add(N + 2 * i + 1);
        }
        //StdOut.println("shapeI = " + shapeI);
        //StdOut.println("shapeF = " + shapeF);
        for (int i = 0; i < N; i++) {
        	shapeI.add(i); 
        	shapeF.add(3 * N - 2 * i - 1);
        }  

		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);	
		XRibTiling xrt = new XRibTiling(n, bag, "Test");
		        */
		xrt.draw();
		ArrayList<Integer> sinkSeq0 = XUtility.findSinkSequence(xrt.xG.DG);
		StdOut.println("Original SinkSequence is " + sinkSeq0);

		/*XRibTile t1 = new XRibTile(1, 3, "000");
		XRibTile t2 = new XRibTile(3, 1 + 3, "010");
		XRibTile t3 = new XRibTile(2, 0, "111");
		XRibTile t4  = new XRibTile(3, 0, "111");
		XRibTile t5 = new XRibTile(0, 3, "011");
		XRibTile t6  = new XRibTile(0, 0, "010");
		ArrayList<XRibTile> flips = xrt.findFlips(t4);
		StdOut.println("Flips = " + flips);
		*/

		
		XRibTiling xrtCopy = new XRibTiling(xrt);
       /* flips = xrtCopy.findFlips(t3);
        StdOut.println("Flips are " + flips);
		xrtCopy.flip(t3, t4);
		xrtCopy.flip(t5, t6);*/
		
		int ITER = 10000;
		XUtility.Glauber(xrtCopy, ITER);
		xrtCopy.draw();
		ArrayList<Integer> sinkSeq1 = XUtility.findSinkSequence(xrtCopy.xG.DG);
		StdOut.println("Graph is " + xrtCopy.xG.DG);
		StdOut.println("Target SinkSequence is " + sinkSeq1);

		ArrayList<Double> ss = new ArrayList<Double>();
		for (int i = 0; i < sinkSeq1.size(); i++) {
			ss.add(sinkSeq0.get(i) * 0.5 + sinkSeq1.get(i) * (1 - 0.5));
		}
		
		ArrayIndexComparator<Double> comparator = new ArrayIndexComparator<Double>(ss);
		ArrayList<Integer> indices = comparator.createIndexArray();
		Collections.sort(indices, comparator);
		StdOut.println("ss = " + ss);
		StdOut.println("indices = " + indices);
		for (int j = 0; j < indices.size(); j++) {
			StdOut.println(ss.get(indices.get(j)));
		}
		
		//now we need also invert the permutation coded by indices.
		ArrayList<Integer> sinkSeq = new ArrayList<Integer>();
		for (int j = 0; j < indices.size(); j++) {
			sinkSeq.add(indices.indexOf(j));
		}
		StdOut.println("sinkseq = " + sinkSeq);
		
		XRibTiling xrtCopy2 = new XRibTiling(n, bag, sinkSeq);
		xrtCopy2.draw(); 
	}
}
