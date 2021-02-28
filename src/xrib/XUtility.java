package xrib;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Collections;

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.Draw;


class XUtility {

	private XUtility() {}


	/**
	 * This convert a simple shape to the bag of squares. the shape consists of several raws. 
	 * ShapeI is the coordinate of the initial square in each row. 
	 * ShapeF is the coordinate of the final square in each row. 
	 * 
	 * @param shapeI
	 * @param shapeF
	 * @return
	 */
	static TreeSet<Square> shape2bag(ArrayList<Integer> shapeI, ArrayList<Integer> shapeF) {
		TreeSet<Square> bag = new TreeSet<Square>();
		for (int i = 0; i < shapeI.size(); i++) {
			for (int j = 0; j < shapeF.get(i) - shapeI.get(i) + 1; j ++) {
				//S++;	
				bag.add(new Square(shapeI.get(i) + j, i)); 
			}
		}	
		return bag;
	}

	/**
	This function finds a sequence of sinks in the acyclic Digraph DG. 
	The sinks are
	increasing in the order of increasing level from left to right
	 */

	static ArrayList<Integer> findSinkSequence(MyDigraph DG){
		int V; //Number of vertices
		Boolean[] marked; //marks the vertices that were found as sinks.
		ArrayList<Integer> sinkSeq; //sequence of the vertices as they appear as sinks 
		//as the previous sinks are removed from the graph.
		//This is just a generic placeholder for the recursive algorithm below
		V = DG.V();
		sinkSeq = new ArrayList<Integer>(V);
		marked = new Boolean[V];
		for (int i = 0; i < V; i++) {
			marked[i] = false;
			sinkSeq.add(0);
		}
		findSink(DG, sinkSeq, marked, V);
		return(sinkSeq);
	}

	/**
	This function finds a sink in the acyclic Digraph DG. 
	More specifically, if there are several sinks,  
	then it produces the smallest of them in the order of increasing
	 level from left to right
	 */

	private static void findSink(MyDigraph DG, ArrayList<Integer> sinkSeq, Boolean[] marked, int V) {
		//we will do the search recursively
		//First, we check how many vertices are yet unmarked.
		int s = 0; //number of unmarked vertices.
		for (int i = 0; i < marked.length; i++) {
			if (!marked[i]) s++;
		}
		if (s == 0) return; //all vertices are marked

		boolean sink = false;
		for (int i = 0; i < V; i++) { //looking for a sink 
			sink = true; //our initial guess is optimistic: it is a sink.
			if (marked[i]) continue; //this vertex was already marked as a sink 
			for (int v : DG.adj(i)) {
				//StdOut.println("The entry in the Marked array is: " + marked[v]);
				if (!marked[v]) { //we found a vertex, which follows i, and which was not 
					//marked as a sink before. Hence, vertex i not a sink.
					sink = false;
					break; //look at the next vertex i.
				}
			}
			if (sink) { //vertex i is a new sink, it is a sink number V + 1 - s
				sinkSeq.set(V - s, i);
				marked[i] = true;
				findSink(DG, sinkSeq, marked, V);
			}
		}
	}

	/**
	 * s0 is a square on the left border of the shape. The program calculates the tile with the
	 * root at s0 and which lies on the left border of the shape.
	 * 
	 * @param s0
	 * @return
	 */

	static XRibTile getBorderTile(Square s0, int n, TreeSet<Square> squares) {
		XRibTile tile;
		TreeSet<Square> tileSquares = new TreeSet<Square>();
		tileSquares.add(s0);
		int l0 = s0.x + s0.y;

		//find the other squares from the tile
		for (int level = l0 + 1; level < l0 + n; level++) {
			for (Square s : squares) {
				if (s.x == s0.x && s.y == s0.y + 1) {
					tileSquares.add(s);
					s0 = new Square(s.x, s.y);
					break;
				} else if (s.x == s0.x + 1 && s.y == s0.y) {
					tileSquares.add(s);
					s0 = new Square(s.x, s.y);
					break;
				}
			}
		}
		tile = new XRibTile(tileSquares);
		return tile;
	}


	/**
	 * 
	 * converts an integer n to a string of length numOfBits 
	 * with the binary representation of n.
	 * 
	 * @param n
	 * @param numOfBits
	 * @return
	 */		
	static String intToBinary (int n, int numOfBits) {
		String binary = "";
		for(int i = 0; i < numOfBits; ++i, n/=2) {
			switch (n % 2) {
			case 0:
				binary = "0" + binary;
				break;
			case 1:
				binary = "1" + binary;
				break;
			}
		}
		return binary;
	}
	/**
	 * This method realizes the Glauber dynamics on the tiling. A tile is chosen at random.
	 * Then a flip is chosen at random (if possible) and performed. This procedure is repeated
	 * for ITER iterations. Note that the original tiling is modified.
	 * 
	 * @param xrt the original tiling.
	 * @param ITER number of steps in the Glauber dynamics
	 */
	static void Glauber(XRibTiling xrt, int ITER){
		XRibTile tile, otherTile;
		ArrayList<XRibTile> flips;
		int randNum;

		for (int i = 0; i < ITER; i++) {
			if (i % 1000 == 0 & i > 0) {
				StdOut.println("Iteration " + i);
			}

			randNum = StdRandom.uniform(xrt.tiles().size());
			tile = xrt.tiles().get(randNum);

			flips = xrt.findFlips(tile);
			if (flips != null && flips.size() > 0) {
				int f = flips.size(); 
				//the maximum number of flips is 3, so we will make sure that 
				//the probability of flip is always 1/3, so that the probability to get back 
				//is also 1/3 and so the distribution on tilings is uniform
				switch (f) {
				case 1:
					if (StdRandom.uniform() < 1./3.) {
						randNum = StdRandom.uniform(f);
						otherTile = flips.get(randNum);
						boolean flag = xrt.flip(tile, otherTile);
						if (!flag) { //flag == false means that there was a problem with this flip.
							StdOut.println("there was a problem with a flip; quitting Glauber algorithm.");
							return;
						}
					}
					break;
				case 2:
					if (StdRandom.uniform() < 2./3.) {
						randNum = StdRandom.uniform(f);
						otherTile = flips.get(randNum);
						boolean flag = xrt.flip(tile, otherTile);
						if (!flag) { //flag == false means that there was a problem with this flip.
							StdOut.println("there was a problem with a flip; quitting Glauber algorithm.");
							return;
						}
					}
					break;
				case 3:
					randNum = StdRandom.uniform(f);
					otherTile = flips.get(randNum);
					boolean flag = xrt.flip(tile, otherTile);
					if (!flag) { //flag == false means that there was a problem with this flip.
						StdOut.println("there was a problem with a flip; quitting Glauber algorithm.");
						return;
					}
					break;
				default:
					StdOut.println("Number of flips is " + f + ", which contradicts my understanding of the algorithm.");
				}
			} else {
				continue;
			}

			//randNum = StdRandom.uniform(xrt.tiles().size());
			//otherTile = xrt.tiles().get(randNum);
		}
	}





	/**
	 * A version of Glauber dynamics with randomness controlled by seed.
	 * Useful for reproducibility.
	 * 
	 * @param xrt
	 * @param ITER
	 * @param seed
	 */

	static void Glauber(XRibTiling xrt, int ITER, int seed){
		StdRandom.setSeed(seed);
		Glauber(xrt, ITER);
	}




	/**
	 * calculate the distance between two xgraphs. 
	 * Returns twice the number of edges with different 
	 * orientation in xg1 and xg2
	 * 
	 * @return
	 */
	static int distance(XGraph xg1, XGraph xg2) {
		int d = 0;
		int V = xg1.V;
		for (int u = 0; u < V; u++) {
			for (int v : xg1.DG.adj(u)) {
				if (!xg2.DG.isEdge(u, v)) { //the edge (u, v) has a different orientation in xg2

					//StdOut.println("Difference detected for (" + u 
					//		+ ", " + v + ") in the pair of graphs 1, 2");					d++;
				}
			}
			for (int v : xg2.DG.adj(u)) {
				if (!xg1.DG.isEdge(u, v)) { //the edge (u, v) has a different orientation in xg2
					//StdOut.println("Difference detected for (" + u 
					//		+ ", " + v + ") in the pair of graphs 2, 1");
					d++;
				}
			}
		}	
		return d;
	}
	/**
	 * calculates the Spearman distance between 2 graphs
	 * For each graph, it calculates the sink sequence
	 * and then calculates the Spearman distance between 
	 * sequences. 
	 * 
	 * By definition, the Spearman footrule distance between two
	 * permutations X and Y is \sum_i |X_i - Y_i|, (or a half
	 * of this quantity).
	 * 
	 * @param xg1 the first xgraph
	 * @param xg2 the second xgraph
	 * @return the Spearman distance.
	 */


	static int distSpearman(XGraph xg1, XGraph xg2) {
		int d = 0;
		ArrayList<Integer> sinkseq1 = findSinkSequence(xg1.DG);
		ArrayList<Integer> sinkseq2 = findSinkSequence(xg2.DG);
		int V = sinkseq1.size();
		for (int i = 0; i < V; i++) {
			int a = sinkseq1.get(i);
			int b = sinkseq2.get(i);
			if (a > b) {
				d = d + a - b;
			} else {
				d = d - a + b;
			}
		}
		return d;
	}
	/**
	 * choose parameters of the Markov chain randomly for the graph xG
	 *
	 * @param xG
	 * @return
	 */
	
	static ArrayList<Integer> chooseVudir(XGraph xG){
		ArrayList<Integer> vudir = new ArrayList<Integer>(3);
			//choose v at random 
			int v = StdRandom.uniform(xG.V);
			vudir.add(v);
		int l = xG.ig.tile2level.get(v);
		//StdOut.println("Vertex " + v + " is at level " + l);
		int L = Collections.max(xG.ig.tile2level); // Maximal occupied level

		/* now the recipe will be (1) choose the level l(x) + k, 
		   where k = 1, 2, n - 1 or -1, - 2, -(n - 1) 
		   (2) choose a random vertex u in this level 
		   and (3) choose the direction (+1 or -1).
		 */

			//choice of level and vertex u 
			boolean flag = false;
			while (!flag) {
				int k = StdRandom.uniform(xG.n - 1) + 1;
				boolean f1 = StdRandom.bernoulli();
				int l2;
				if (f1) {
					l2 = l + k;
				} else {
					l2 = l - k;
				} 
				//StdOut.println("New level = " + l2);
				if (l2 < 0 || l2 > L) {
					continue;
				} else {
					//choice of vertex in the given level l2
					int u = xG.ig.levelStarts.get(l2)
							+ StdRandom.uniform(xG.ig.level2tiles.get(l2));
					//StdOut.println("New vertex is " + u);
					vudir.add(u);	
					flag = true;
				}
			}
			//choice of direction
			int dir = 0;
			if (StdRandom.bernoulli()) {
				dir = 1;
			} else {
				dir = -1;
			}
			vudir.add(dir);
			return vudir;
	}

	/**
	 * Applies one step of the Markov chain #1 to the tiling
	 * given by XGraph xG and returns true if the graph has changed
	 * [This specific Markov Chain has a disadvantage that if applied to a
	 * pair of tilings which differ only in a step it can sometime increase
	 * the distance between them in expectation (where difference is measured as the 
	 * number of edges on which the tilings have different orientations.]
	 * 
	 * vudir = array of [v, u, dir]
	 * 
	 * if v < 0, chooses v randomly 
	 * if u < 0, chooses u randomly
	 * if dir = 0, chooses dir randomly 
	 * 
	 * @param xG the current XGraph
	 * @param vudir array [v, u, dir] which is used if v, u, dir are reasonable
	 * @return true if there was a real change in xG, false if the xG is intact.
	 */

	static boolean stepMarkovM1(XGraph xG, ArrayList<Integer> vudir) {
		int v = vudir.get(0);
		int u = vudir.get(1);
		int dir = vudir.get(2);
		if (v < 0) {
			//choose v at random 
			v = StdRandom.uniform(xG.V);
			vudir.set(0, v);
		}
		int l = xG.ig.tile2level.get(v);
		//StdOut.println("Vertex " + v + " is at level " + l);
		int L = Collections.max(xG.ig.tile2level); // Maximal occupied level

		/* now the recipe will be (1) choose the level l(x) + k, 
		   where k = 1, 2, n - 1 or -1, - 2, -(n - 1) 
		   (2) choose a random vertex u in this level 
		   and (3) choose the direction (+1 or -1).
		   then we try to set the direction from v to u or from u to v
		   according to this choice. 
	       if not possible, stay put. 
		This should give us a symmetric Markov chain on tilings
		 */

		if (u < 0) {
			//choice of level and vertex u 
			boolean flag = false;
			while (!flag) {
				int k = StdRandom.uniform(xG.n - 1) + 1;
				boolean f1 = StdRandom.bernoulli();
				int l2;
				if (f1) {
					l2 = l + k;
				} else {
					l2 = l - k;
				} 
				//StdOut.println("New level = " + l2);
				if (l2 < 0 || l2 > L) {
					continue;
				} else {
					//choice of vertex in the given level l2
					u = xG.ig.levelStarts.get(l2)
							+ StdRandom.uniform(xG.ig.level2tiles.get(l2));
					//StdOut.println("New vertex is " + u);
					vudir.set(1, u);	
					flag = true;
				}
			}
		}
		if (dir == 0) {
			//choice of direction
			if (StdRandom.bernoulli()) {
				dir = 1;
			} else {
				dir = -1;
			}
			vudir.set(2, dir);
		}
		//Everything is chosen so we attempt to perform a Markov chain step
		// if dir = +1, then we will try to orient from v to u
		// so we will not do anything if the edge is already oriented from
		// v to u
		if (dir == 1 && !xG.DG.isEdge(v,u)) { // no edge (v,u) so (u,v) must be present
			//attempt to reverse
			XGraph.Edge e = xG.new Edge(u, v);
			if (xG.isFlip(e)) {
				xG.flip(e, false);
				//StdOut.println("Flip was successful");
				return true;
			} else { //the edge is not flippable so do not do anything
				return false;
			}
		} else if (dir == -1 && xG.DG.isEdge(v,u)) { //attempt to orient from u to v	
			XGraph.Edge e = xG.new Edge(v, u);
			if (xG.isFlip(e)) {
				xG.flip(e, false);
				//StdOut.println("Flip was successful");
				return true;
			} else { //the edge is not flippable so do not do anything
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Applies one step of the Markov chain #2 to the tiling
	 * given by XGraph xG and returns true if the graph has changed
	 * 
	 * The idea is the same as in Markov chain #1 choose u and v at 
	 * random and try to orient in a given direction. However, here 
	 * if it is not possible by a simple flip, we will try to do it 
	 * by reversing a chain of edges. 
	 * 
	 * vudir = array of [v, u, dir]
	 * 
	 * if v < 0, chooses all parameters at random and saves to vudir.
	 * 
	 * 
	 * It might still have some problems with updating of edges -- 
	 * -- need some additional testing
	 * 
	 * 
	 * @param xG the current XGraph
	 * @param vudir array [v, u, dir] which is used if v, u, dir are reasonable
	 * @return true if there was a real change in xG, false if the xG is intact.
	 */

	static boolean stepMarkovM2(XGraph xG, ArrayList<Integer> vudir) {
		int v = vudir.get(0);
		int u = vudir.get(1);
		int dir = vudir.get(2);
		if (v < 0) { //the parameters are not defined. Choose them at random.
			ArrayList<Integer> myVudir = chooseVudir(xG);
			v = myVudir.get(0);
			u = myVudir.get(1);
			dir = myVudir.get(2);
		}
		vudir.set(0, v);
		vudir.set(1, u);
		vudir.set(2, dir);
		//StdOut.println("vudir is " + vudir);
		//Now go to actual work
		// if dir = +1, then we will try to orient from v to u
				// so we will not do anything if the edge is already oriented from
				// v to u
		if (dir == 1 && !xG.DG.isEdge(v,u)) { // no oriented edge (v,u) so (u,v) must be present
					//attempt to reverse
					XGraph.Edge e = xG.new Edge(u, v);
					if (xG.isFlip(e)) {
						xG.flip(e, false);
						//StdOut.println("Flip was successful");
						return true;
					} else { //the edge is not directly flippable 
						//so we will try to build a more sophisticated flip
                    ArrayList<Integer> obstacle = xG.obstacleClever(e);
                    //StdOut.println("Clever obstacle is " + obstacle);
                    for (int i = 0; i < obstacle.size() - 1; i++) {
                    	XGraph.Edge f = xG.new Edge(obstacle.get(i), obstacle.get(i+1));
                    	if (xG.isFlip(f)) {
                    		//StdOut.println("Flipping edge " + f + "as a part of obstacle");
                    		xG.flip(f, false);
                    		return true;
                    	}
                    }
					}
		} else if (dir == -1 && xG.DG.isEdge(v,u)) { //attempt to orient from u to v	
			XGraph.Edge e = xG.new Edge(v, u);
			if (xG.isFlip(e)) {
				xG.flip(e, false);
				//StdOut.println("Flip was successful");
				return true;
			} else {
                ArrayList<Integer> obstacle = xG.obstacleClever(e);
                //StdOut.println("Clever obstacle is " + obstacle);
                for (int i = 0; i < obstacle.size() - 1; i++) {
                	XGraph.Edge f = xG.new Edge(obstacle.get(i), obstacle.get(i+1));
                	if (xG.isFlip(f)) {
                		//StdOut.println("Flipping edge " + f + " as a part of obstacle");
                		xG.flip(f, false);
                		return true;
                	}
                }
			}
		}
		//StdOut.println("stepMarkovM2: Was not able to flip edge " + vudir 
		//		+ "Perhaps already set in this direction?");
		return false;
	}
	
	/**
	 * Applies one of the Markov chains to tiling xG using parameter vudir
	 * 
	 * Tne choice of the Markov chains is determined by type.
	 * 
	 * Returns true if the step lead to an actual change in the tiling.
	 * 
	 * @param type
	 * @param xG
	 * @param vudir
	 * @return
	 */
	
	public static boolean stepMarkov(int type, XGraph xG, 
			ArrayList<Integer> vudir) {
		boolean flag = false;
		if (type == 1) {
			flag = stepMarkovM1(xG, vudir);
		} else if (type == 2) {
			flag = stepMarkovM2(xG, vudir);
		} else {
			StdOut.println("Wrong type. It should be 1 or 2 and it = " + type);
		}
		
		return flag;
	}

	/**
	 * measures the average change in distance between 2 graphs, xg1 and xg2,
	 * when a step of COUPLED Markov chains is applied to them 
	 * 
	 * @param type the type of the Markov chain. Currently type should be 1 or 2.
	 * @param xg1 an XGraph
	 * @param xg2 another XGraph
	 * @param ITER number of tries to apply the Markov chain. 
	 * @return the mean change of distance between xg1 and xg2 after the Markov
	 *   chain is applied. 
	 */


	static double meanDistChange(int type, XGraph xg1, XGraph xg2, int ITER) {
		int d0 = distance(xg1, xg2);
		ArrayList<Integer> distances = new ArrayList<Integer>(ITER);
		for (int i = 0; i < ITER; i++) {
			if (i % 100 == 0) {
			StdOut.println(i);
			}
			ArrayList<Integer> vudir = new ArrayList<Integer>(3);
			vudir.add(-1);
			vudir.add(-1);
			vudir.add(0);
			XGraph xg1copy = new XGraph(xg1);
			XGraph xg2copy = new XGraph(xg2);
			stepMarkov(type, xg1copy, vudir); //chooses a random step and applies to xg1copy
			stepMarkov(type, xg2copy, vudir); //uses the same step and applies it to xg2copy
			int d1 = distance(xg1copy, xg2copy);
			distances.add(d1);
		}
		double meand1 = mean(distances);
		return meand1 - d0;
	}

	/**
	 * TODO Might have problems. Should be fixed. 
	 * 
	 * Draws a plot of the height of a tiling of the N by N square
	 * 
	 * The square should be tileable
	 * 
	 * @param n size of ribbon tiles
	 * @param N size of the square region
	 */

	static void HeightPlot(XGraph xg, int N) {
		//create a random tiling
		/*
		XRibTiling region = XRibTiling.rectangle(n, N, N);
		XShape shape = region.shape;
		XGraph xg = new XGraph(n, shape);
		xg.randomOrientation();
		int ITER = 30000;
		XUtility.Glauber(xg.tiling, ITER);
		xg.tiling.draw();
		*/

		//calculate tiling height
		XHeight H = new XHeight(xg.tiling);  
		H.calcHeightInside();
		//Now we want to select the heights on the line 
		//approximately in the middle of the square (or on the diagonal
		ArrayList<ArrayList<Integer>> plotData = new ArrayList<ArrayList<Integer>>();
		ArrayList<Double> heightNorms = new ArrayList<Double>();
		for (int i = 0; i < N + 1; i++) {
			ArrayList<Integer> h = new ArrayList<Integer>();
			plotData.add(h);
		}
		int M = N/2;
		for (Square s: H.height.keySet()) {
			if (s.y == s.x) { //looking on the diagonal squares.
				//StdOut.println("s.x = " + s.x);
				ArrayList<Integer> h = H.height.get(s);
				double norm = 0.;
				for (int i = 0; i < xg.n; i++) {
					norm = norm + h.get(i) * h.get(i) ;
				}
				norm = Math.sqrt(norm);
				plotData.set(s.x, h);
				heightNorms.add(norm);
			}
		}
		StdOut.println("Midline Height = " + plotData);

		xg.tiling.shape.myDr.setPenColor(Draw.RED);
		xg.tiling.shape.myDr.setPenRadius(0.01);
		xg.tiling.shape.myDr.line(0, 0, N, N);



		/*
		Draw dr = new Draw();
		dr.setXscale(-0.5, N + 0.5);
		dr.setYscale(-6, +6);
		for(int i = 0; i < N; i++) {
			dr.line(i, plotData.get(i).get(0), i + 1, plotData.get(i+1).get(0));
		}
		 */
		plot(heightNorms);
		//TODO Plot the norm of the height vector, also may be plot the height along the level line.
	}

	/**
	 * plots an array of integers in a new window.
	 * 
	 * @param data, an array of integers
	 */

	static void plotIntArray(ArrayList<Integer> data) {
		Draw dr = new Draw();
		dr.show(0);
		int N = data.size();
		int max = data.get(0);
		int min = data.get(0);
		for (int i = 1; i < N; i++) {
			if (max < data.get(i)) {
				max = data.get(i);
			}
			if (min > data.get(i)) {
				min = data.get(i);
			}
		}
		dr.setXscale(-N * 0.05, N* 1.05);
		dr.setYscale(min - (max - min) * 0.05, max + (max - min) * 0.05);
		dr.setPenColor(Draw.BOOK_RED);
		dr.setPenRadius(0.01);
		for(int i = 0; i < N - 1; i++) {
			dr.line(i, data.get(i), i + 1, data.get(i+1));
		}
		dr.setPenColor(Draw.BLACK);
		dr.setPenRadius(0.01);
		dr.line(0, max, N, max);
		dr.text(N * 0.05, max + (max - min) * 0.02, "max = " + max);
		dr.line(0, min, N, min);
		dr.text(N * 0.05, min + (max - min) * 0.02, "min = " + min);
		dr.show(0);
	}


	/**
	 * plots an array of doubles in a new window.
	 * 
	 * @param data, an array of doubles
	 */

	static void plot(ArrayList<Double> data) {
		Draw dr = new Draw();
		int N = data.size();
		double max = data.get(0);
		double min = data.get(0);
		for (int i = 1; i < N; i++) {
			if (max < data.get(i)) {
				max = data.get(i);
			}
			if (min > data.get(i)) {
				min = data.get(i);
			}
		}
		dr.setXscale(-N * 0.05, N* 1.05);
		dr.setYscale(min - (max - min) * 0.05, max + (max - min) * 0.05);
		dr.setPenColor(Draw.BOOK_RED);
		dr.setPenRadius(0.01);
		for(int i = 0; i < N - 1; i++) {
			dr.line(i, data.get(i), i + 1, data.get(i+1));
		}
		dr.setPenColor(Draw.BLACK);
		dr.setPenRadius(0.01);
		dr.line(0, max, N, max);
		dr.text(N * 0.05, max + (max - min) * 0.02, "max = " + ((int) (max * 1000))/1000.);
		dr.line(0, min, N, min);
		dr.text(N * 0.05, min + (max - min) * 0.02, "min = " + ((int) (min * 1000))/1000.);
	}



	/**
	 * walks on the graph of XGraph objects, avoiding returning to the same object twice. 
	 * Records the objects that it found and returns the list of these objects. 
	 * 
	 * @param xg initial XGraph
	 * @param ITER maximum number of steps allowed
	 */
	/*
	static ArrayList<XGraph> selfAvoidWalk(XGraph xg, int ITER) {
		ArrayList<XGraph> spisok = new ArrayList<XGraph>();
		//TODO create a realization of this method. 

		return spisok;
	}
	 */
	/**
	 * Calculates the mean of the array m
	 * 
	 * @param m array of integers
	 * @return the mean of the array
	 */
	public static double mean(ArrayList<Integer> m) {
		double sum = 0;
		for (int i = 0; i < m.size(); i++) {
			sum += m.get(i);
		}
		return sum / m.size();
	}

	/*
	 * For testing methods.
	 *
	 */
	public static void main(String[] args) {

     
        //let us create a couple of graphs. 

		int k = 2;
		int n = 3;
		int M = n * k;
		int N = n * k;
       /*
		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();

		for (int i = 0; i < M; i++) {
			shapeI.add(0);
			shapeF.add(N - 1);
		}
		TreeSet<Square> squares = XUtility.shape2bag(shapeI, shapeF);
		XShape shape = new XShape(squares);
		XGraph xG = new XGraph(n, shape);
        */
		
		
        /**
         * let us create two tilings which differ only by one flip
         * The goal for the next sections is to search for an update method 
         * that will reduce 
         * the distance between these two tilings.
         * 
         * In order to make sure that there is only one flip we will apply
         * one step of the Markov chain #1 until a successful flip
         * 
         */
		
		int ITER = (int) Math.pow(n * Math.pow(k,2),2);
		StdOut.println("Number of iterations for generation = " + ITER);
		//xG = XGraph.random(n, shape, ITER);
		//A random graph on rectangle - randomness by some kind of Glauber
		//XGraph xG = XGraph.randomRectangle(n, M, N, ITER);
		double bias = 0.1;
		XGraph xG = XGraph.randomBiasedRectangle(n, M, N, ITER, bias);
		xG.draw("Old", 500, true);
       
		
		//In another graph we will flip just one edge
		XGraph xG2 = new XGraph(xG);
		int count = 0;
		boolean flag = false;
		ArrayList<Integer> vudir = new ArrayList<Integer>(3);
		vudir.add(-1);
		vudir.add(-1);
		vudir.add(0);
		while (!flag) {
			vudir.set(0, -1);
			vudir.set(1, -1);
			vudir.set(2, 0);
			flag = stepMarkovM1(xG2, vudir);
			//flag = stepMarkovM2(xG2, vudir);
			count++;
		} 
		xG2.updateTiling();
		xG2.draw("New", 500, true);
		StdOut.println("It took " + count + " attempts to do a flip");
		StdOut.println("v = " + vudir.get(0) + "; u = " + vudir.get(1)
		+ "; dir = " + vudir.get(2));
        
		
		int type = 2; // will use the second variant of Markov chain
		ITER = 1000;
		double delta = meanDistChange(type, xG, xG2, ITER);
		StdOut.println("Average change in distances for this pair is " + delta);
		
		//XGraph xG3 = new XGraph(xG);
		//stepMarkovM1(xG3, vudir);
		//xG3.updateTiling();
		//xG3.draw("SuperNew", 500);
		//StdOut.println("tile2level for xG2 = " + xG2.ig.tile2level);
		//StdOut.println("level2tiles for xG2 = " + xG2.ig.level2tiles);
		//StdOut.println("levelStarts = " + xG2.ig.levelStarts);

        
		/* 
		HeightPlot(xG, N); 
		*/
	}


}

