package xrib;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
//import java.util.Random;
import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;


/*
 * This is the class for a ribbon tiling of a shape. 
 * 
 * Right now there is some confusion here with two variants of 
 * constructors. 
 *
 * 
 * 
 * 
 */




public class XRibTiling {
	  //conceptual variables
	    protected ArrayList<XRibTile> tiling; //An ordered set of tiles in the tiling. The order is determined by the 
	                                        //order of the lowest square of each tile. It is different from Sheffield's partial order.
	                                           //It can be empty if we have not yet 
	                                           //tiled the region
		  //Geometric variables
	    int n; //length of the ribbon
	    XShape shape; 
	    
	    StaticGraph staticDG; //I should try to remove this and rely only on xG, which
	                          //already contains info about forced edges.
	                          //This is the graph which depends only on the shape of the region and not on the tiles 
	    XGraph xG; //Graph associated to the tiling. 

		TreeMap<Square, XRibTile> square2tile; //map that shows which tile covers a given square.
		XHeight H; //height function
        // ArrayList<Integer> heightDiffs; //difference of heights at the ends of intervals at which line x + k intersect the region

      //Variables for visualization
		String label;
		
		
		public XRibTiling() {		
		}
		
		/**
		 * Creates a default tiling from a bag of squares. This constructor was build to
		 * work with regions that have only one interval in the intersection with 
		 * level lines x + y = l. The constructor will calculate a default tiling and the 
		 * graphs associated with the region and the tiling. 
		 * A new version below is available to do some of the work for the regions that have multiple 
		 * intersections, however it is not very sophisticated since its purpose is simply be called from 
		 * an XGraph object.
		 * 
		 * 
		 * @param n
		 * @param bag
		 * @param label
		 */
		
		public XRibTiling(int n, TreeSet<Square> bag, String label) {
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
			
			boolean test = shape.isTileable(n);
			if (!test) {
				StdOut.println("The region is not tileable. The numbers of squares of different colors are not the same.");
				int[] count = shape.countColoredSquares(n);
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
			//I do not really want to calculate anything even the default tiling.
			
			//creating the associated digraphs
			//staticDG = new StaticGraph(this); // not really sure if I use it for anything.
			xG = new XGraph(n, shape);
			buildTilingFromXG(); //build the default tiling. 
			//StdOut.println("Tiling size = " + tiling.size());
			//StdOut.println("Tiling = " + tiling);
			xG = new XGraph(this);
			H = new XHeight(this);
			H.calcHeightInside();
		}
		
		/**
		 * This a new version of the general constructor. It is supposed to work even for
		 * the regions that have multiple intersections with lines x+y = l;
		 * The constructor is supposed to be called from an XGraph object. 
		 * 
		 * @param n
		 * @param bag
		 */
		
		public static XRibTiling fromXGraph(int n, XGraph xG) {
			XRibTiling xrt = new XRibTiling();
			xrt.n = n;
			xrt.label = "";
			xrt.tiling = new ArrayList<XRibTile>();
			xrt.square2tile = new TreeMap<Square, XRibTile>();
			xrt.shape = xG.shape;	
			xrt.xG = xG;
			//StdOut.println(" n = " + n);
			if (xG.shape.squares.size() % n != 0) {
				StdOut.println("An error in XGraph constructor.");
				StdOut.println("The number of squares in the shape is " + xG.shape.squares.size()  
				                + ". It must be divisible by " + n);
				StdOut.println("Quitting ...");
			}
			
			boolean test = xrt.shape.isTileable(n);
			if (!test) {
				StdOut.println("The region is not tileable. The numbers of squares of different colors are not the same.");
				int[] count = xrt.shape.countColoredSquares(xrt.n);
				StdOut.println("The count is ");
				for (int i = 0; i < n; i++) {
					StdOut.print(count[i] + "; ");
				}
				StdOut.println();
			};
			//initialize tiling 
			for (int v = 0; v < xrt.shape.squares.size()/n; v++){
				xrt.tiling.add(null);
			}		
			return xrt;
		}
		
				
		/**
		 * Creates a copy of a tiling "other"
		 * @param other
		 */		
		public XRibTiling(XRibTiling other) {
           this.n = other.n;
           this.label = other.label;
           //copy shape, staticDG, tiling and square2tile structures
           this.shape = new XShape(other.shape);
           if (other.staticDG != null) {
               this.staticDG = new StaticGraph(other.staticDG);
           }
		   tiling = new ArrayList<XRibTile>(other.tiling);
		   square2tile = new TreeMap<Square, XRibTile>(other.square2tile);

           //adding graph and height at the border
           this.xG = new XGraph(this);     
			H = new XHeight(this);
		}
		
		/**
		 * Creates a default tiling of a rectangle with width M and length N.
		 * The tiling is by ribbon n-tiles.
		 * 
		 * 
		 * @param n
		 * @param M
		 * @param N
		 * @return
		 */		
		public static XRibTiling rectangle(int n, int M, int N) {
			ArrayList<Integer> shapeI = new ArrayList<Integer>();
			ArrayList<Integer> shapeF = new ArrayList<Integer>();
	        for (int i = 0; i < M; i++) {
	        	shapeI.add(0);
	        	shapeF.add(N - 1);
	        }
			TreeSet<Square> squares = XUtility.shape2bag(shapeI, shapeF);
			XRibTiling rect = new XRibTiling(n, squares, "Rectangle");
			return rect;
		}
		
		/**
		 * Creates a random tiling of a rectangle with width M and length N.
		 * The method is by a some kind of Glauber dynamics. 
		 * 
		 * 
		 * @param n
		 * @param M
		 * @param N
		 * @return
		 */		
		public static XRibTiling rectangleRandom(int n, int M, int N) {
			XRibTiling rect = rectangle(n, M, N);
			int ITER = 10000;
			XUtility.Glauber(rect, ITER);
			rect.xG = new XGraph(rect); //update xG graph
			return rect;
		}
		
		
		/**
		 * Creates a default tiling of an Aztec Diamond with size N.
		 * The tiling is by dominoes.
		 * 
		 * @param N
		 * @return
		 */		
		public static XRibTiling aztecDiamond(int N) {
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
	               bag.add(new Square(shapeI.get(i) + j, i)); 
				}
			}	
			XRibTiling aD = new XRibTiling(2, bag, "Aztec");
			return aD;
		}
		
		
		
		
		/**
		 * Creates a default tiling of a stair-shaped region with width M and length N.
		 * The tiling is by ribbon n-tiles.
		 * 
		 * In this region each row has length N but the rows are shifted relative to each other by a square,
		 * so that the resulting shape is a parallelogram.
		 * The first row starts at x = 0, the second -- at x = 1, the third -- at x = 2 and so on.  
		 * 
		 * @param n
		 * @param M
		 * @param N
		 * @return
		 */		
		public static XRibTiling stair(int n, int M, int N) {
			ArrayList<Integer> shapeI = new ArrayList<Integer>();
			ArrayList<Integer> shapeF = new ArrayList<Integer>();
	        for (int i = 0; i < M; i++) {
	        	shapeI.add(i);
	        	shapeF.add(i + N - 1);
	        }
			TreeSet<Square> squares = XUtility.shape2bag(shapeI, shapeF);
			XRibTiling rect = new XRibTiling(n, squares, "");
			return rect;
		}
		
		/**
		 * Creates a default tiling of a stair-shaped region with width M and length N.
		 * The tiling is by ribbon n-tiles. The difference from the stair regions is that 
		 * here the stair goes down. 
		 * 
		 * In this region each row has length N but the rows are shifted relative to each other by a square,
		 * so that the resulting shape is a parallelogram.
		 * The first row starts at x = M - 1, the second -- at x = M - 2, the third -- at x = M - 3 and so on.  
		 * 
		 * @param n
		 * @param M
		 * @param N
		 * @return
		 */		
		public static XRibTiling downStair(int n, int M, int N) {
			ArrayList<Integer> shapeI = new ArrayList<Integer>();
			ArrayList<Integer> shapeF = new ArrayList<Integer>();
	        for (int i = M - 1; i >= 0; i--) {
	        	shapeI.add(i);
	        	shapeF.add(i + N - 1);
	        }
			TreeSet<Square> squares = XUtility.shape2bag(shapeI, shapeF);
			XRibTiling rect = new XRibTiling(n, squares, "");
			return rect;
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
			 ArrayList<Integer> sinkSeq = new ArrayList<Integer>();
			 if (staticDG != null) {
			    sinkSeq = XUtility.findSinkSequence(staticDG.sG); //in old versions I calculated staticDG (the digraph of 
			                                                       // forced edges), then I stopped doing it.
			 } else {
				 sinkSeq = XUtility.findSinkSequence(xG.DG);
			 }
			buildTiling(sinkSeq, k);
		}
		
			//calculates a tiling consistent with the orientation in DG. 
		
		 public void buildTilingFromXG() {
				buildTilingFromXG(xG.V);
		 }
			
			
			/**
			 * calculates the first k titles of a tiling using xG graph
			 * we will assume that level2intervals in interval graph list 
			 * intervals from left to right. 
			 * @param k
			 */
			
			private void buildTilingFromXG(int k) {
				int v, l0;
				Square s0 = null;
				XRibTile tile; 
				TreeSet<Square> bag = new TreeSet<Square>(xG.ig.shape.squares); 
				//as we build the tiling we will remove the 
				//squares from the bag.
				//completeGraph();
				//StdOut.println(" n = " + n);
				ArrayList<Integer> sinkSeq = XUtility.findSinkSequence(xG.DG);
				for (int count = 0; count < k; count++) {
					v = sinkSeq.get(count);	
					l0 = xG.ig.tile2level.get(v); //level of the target tile
					// we need to find a root square for this tile. 
					//For this we look for the smallest squares at level l0 in our bag.
					//As we go by we need to check that they are close to each other. 
					//find the first ("root") square
					for (Square s : bag) {
						if (s.x + s.y == l0) {
							s0 = new Square(s); //this is the desired root of the tile. 
							break;
						}
					}
					//StdOut.println("The root square for tile " + v + " is square " + s0);
					//get the tile with the root at s0 and on the left border
					tile = XUtility.getBorderTile(s0, n, bag);
					bag.removeAll(tile.squares());
					addTile(v, tile);
				}
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
		 * @deprecated Use {@link xrib.XShape#isTileable(xrib.XRibTiling)} instead
		 */
		/*
		 * Boolean isTileable(){
			return shape.isTileable(this);
		}
		*/
		

		
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
		 * Updates the digraph xG.DG and the tiling and the square2tile
		 * but do not update the edges structures in xG
		 * (that is, the list of the edges which are flippable) 
		 * Relies on findFlips and isFlip methods.
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
			//StdOut.println("graph before flip is " + xG);
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
			//xG = new XGraph(this); //update xG graph
			
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
			otherTile = findTile(tile.xmin - 0.5, tile.ymin + 0.5);
			if (otherTile != null && isFlip(tile, otherTile)) {
				flips.add(otherTile);
			}
			otherTile = findTile(tile.xmin + 0.5, tile.ymin - 0.5);
			if (otherTile != null && isFlip(tile, otherTile)) {
				flips.add(otherTile);
			}
			otherTile = findTile(tile.xmax + 0.5, tile.ymax - 0.5);
			if (otherTile != null && isFlip(tile, otherTile)) {
				flips.add(otherTile);
			}
			otherTile = findTile(tile.xmax - 0.5, tile.ymax + 0.5);
			if (otherTile != null && isFlip(tile, otherTile)) {
				flips.add(otherTile);
			}
			return flips;
		}
	
	   public void draw() {
           draw("", 500, false);	
		}
		
		public void draw(String s, int size, boolean withLabels) {
			shape.draw(s, size);
			if (withLabels) {
				drawWithLabels(shape.myDr);		
			} else {
			    draw(shape.myDr);	
			}
		}
		
		/*
		public void drawWithLabels(String s, int size) {
			shape.draw(s, size);
			drawWithLabels(shape.myDr);			
		}
		*/
		void draw(Draw dr) {
			shape.drawShape(dr);
			for (XRibTile tile : tiling) {
				if (tile != null) {
				tile.draw(dr, - 1);
				}
			}
		}
		
		private void drawWithLabels(Draw dr) {
			shape.drawShape(dr);
			for (int v = 0; v < tiling.size(); v++) {
				XRibTile tile = tiling.get(v);
				if (tile != null) {
				tile.draw(dr, v);
				}
			}
		}

	    /**
	     * Compares this tiling to another tiling and returns true if they are the same.
	     * It is assumed that tiling are for the same region and the ribbon tiles have the same number of squares.
	     * 
	     * The equality happens only if every RibTile in this tiling equals to the corresponding
	     * RibTile in the other tiling. 
	     *
	     * @param  other the other tile
	     * @return {@code true} if this tile equals {@code other};
	     *         {@code false} otherwise
	     */
		@Override
	    public boolean equals(Object other) {
	        if (other == this) return true;
	        if (other == null) return false;
	        if (other.getClass() != this.getClass()) return false;
	        XRibTiling that = (XRibTiling) other;
			for (int i = 0; i < this.tiling.size(); i++ ) {
				if (!this.tiling.get(i).equals(that.tiling.get(i))) {
					return false;
				}
			}
			return true;
		}
		
	    /**
	     * Returns an integer hash code for this tiling. The hash code depends only on the tiling structure
	     * 
	     * @return an integer hash code for this tilint
	     */
	    @Override
	    public int hashCode() {
	    	return tiling.hashCode();
	    }
		
	/**
	 * For testing methods.
	 */
	public static void main(String[] args) {

		//ArrayList<Integer> shapeI = new ArrayList<Integer>();
		//ArrayList<Integer> shapeF = new ArrayList<Integer>();
		
		/* Test case 1
		 * 	
		 */	
		
		/*
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
		xrt.draw();		
		XRibTiling xrtCopy = new XRibTiling(xrt);	
		int ITER = 10000;
		XUtility.Glauber(xrtCopy, ITER);
		xrtCopy.draw();
		*/
		/*Test case 2
		 * 
		 * 
		 */
		int n = 3;
		int M = 6;
		int N = 6;
		//XRibTiling xrt = rectangle(n, M, N);
		//StdOut.println("Graph is " + xrt.xG);
		//StdOut.println("Number of reversible edges = " + xrt.xG.edges.size());
		//xrt.xG.draw();
		
		
		/* Conclusion from the following experiment:
		 * This method of generating a random orientation does not 
		 * lead to a uniformly distributed tiling
		 */
		/*
		xrt.xG.randomOrientation();
		xrt.buildTilingFromXG();
		xrt.xG.draw();
		StdOut.println("Number of reversible edges = " + xrt.xG.edges.size());
		*/
		//xrt.draw();
		//StdOut.println("Digraph is : " + xrt.xG);
		
		XRibTiling xrt = rectangleRandom(n, M, N);

		
		xrt.xG.draw("", 500, true);
		StdOut.println("Number of reversible edges = " + xrt.xG.edges.size());
		StdOut.println("Reversible edges = " + xrt.xG.edges);
		
		XRibTile t1 = xrt.tiling.get(xrt.xG.edges.get(0).u);
		XRibTile t2 = xrt.tiling.get(xrt.xG.edges.get(0).v);
		StdOut.println("Tiles to exchange are " + t1 + " and " + t2);
		XRibTiling xrt2 = new  XRibTiling(xrt);
		xrt2.flip(t1, t2);
		xrt2.xG.draw();
		xrt2.H.calcHeightInside();
		xrt2.H.draw();
		
	}
}
