/**
 * 
 */
package xrib;

import java.util.ArrayList;

import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.Graph;

/**
 * @author vladislavkargin
 *
 */
public class Coupling {
	XGraph xg1, xg2;
	Draw dr1, dr2;
	
	public Coupling(int n, int M, int N) {
		int ITER = 1000;
		XRibTiling region = XRibTiling.rectangle(n, M, N);
		XShape shape = region.shape;
		//IntervalGraph ig1 = new IntervalGraph(n, shape);
	    xg1 = new XGraph(n, shape);
	    XUtility.Glauber(xg1.tiling, ITER);
		//IntervalGraph ig2 = new IntervalGraph(n, shape);
	    xg2 = new XGraph(n, shape);
	    XUtility.Glauber(xg2.tiling, ITER);
	}
	
	public void draw(int size) {
		if (dr1 == null) {
			dr1 = new Draw();
			dr1.setCanvasSize(size, size);
			dr2 = new Draw();
			dr2.setCanvasSize(size, size);
			dr2.setLocationOnScreen(size, 1);
		} else {
			dr1.setCanvasSize(size, size);
			dr2.setCanvasSize(size, size);
		}
		//prepareDrawFrames(pDr);
		//prepareDrawFrames(qDr);
		// now we can draw
		xg1.tiling.draw(dr1);
		xg2.tiling.draw(dr2);
	}
	
	/**
	 * calculate the distance between two graphs. Returns twice the number of edges with different orientation
	 * in xg1 and xg2
	 * [This function if realized in Utility class as distance(graph1, graph2).]
	 * 
	 * @return
	 */
	public int distance() {
		int d = 0;
		int V = xg1.V;
		for (int u = 0; u < V; u++) {
			for (int v : xg1.DG.adj(u)) {
				if (!xg2.DG.isEdge(u, v)) { //the edge (u, v) has a different orientation in xg2
					d++;
				}
			}
			for (int v : xg2.DG.adj(u)) {
				if (!xg1.DG.isEdge(u, v)) { //the edge (u, v) has a different orientation in xg2
					d++;
				}
			}
		}	
		return d;
	}
	
	//for a given direction of flip, counts how many edges are such that
	// they have opposite orientations originally and after an attempt to set 
	// both orientations to this direction, they will 
	// have the same orientation (which coincides with the orientation given by the direction). 
	// The possible values of direction is 1 or -1. 
	//For the purposes of this function, we assume that we deal with the rectangular region and that
	// this regions has the number of rows, which is divisible by n.
	// The standard tiling of this region is by vertical tiles. 
	// We assume that the positive direction (dir == 1) means
	// that the end-points of the edge are arranged in the same way as they are arranged
	// in the orientation that corresponds to the standard tiling. 
	
	public Graph countGood(int dir) {
		int V = xg1.V;
		Graph goodEdges = new Graph(V);
		// we need to go over all possible edges;
		for (int u = 0; u < V - 1; u++) {
			XRibTile tile1u = xg1.tiling.tiles().get(u); //tile that corresponds to u in the first tiling
			XRibTile tile2u = xg2.tiling.tiles().get(u); //tile that corresponds to v in the second tiling
			for (int v = u + 1; v < V; v++) {
               if (!xg1.isFreeAndComparable(u, v)) { //the orientation is either forced on (u, v)
            	                                     //or there is no such edge.        	   
            	   continue;
               }			
            XRibTile tile1v = xg1.tiling.tiles().get(v); 
   			XRibTile tile2v = xg2.tiling.tiles().get(v); 
               if (dir > 0) { // the target orientation is from v to u
            	   if (xg1.DG.isEdge(v, u) && xg2.DG.isEdge(u, v)) { //direction in the 2nd tiling is different from dir
            		   //try to flip the edge in xg2
            		   if (xg2.tiling.isFlip(tile2u, tile2v)) {
            			   goodEdges.addEdge(u, v);
            		   }
            	   }
            	   if (xg1.DG.isEdge(u, v) && xg2.DG.isEdge(v, u)) { //direction in the 1st tiling is different from dir
            		   //try to flip the edge in xg1
            		   if (xg1.tiling.isFlip(tile1u, tile1v)) {
            			   goodEdges.addEdge(u, v);
            		   }
            	   }
               } else { //the target direction is from u to v
            	   if (xg1.DG.isEdge(v, u) && xg2.DG.isEdge(u, v)) { //direction in the 1st tiling is different from dir
            		   //try to flip the edge in xg1
            		   if (xg1.tiling.isFlip(tile1u, tile1v)) {
            			   goodEdges.addEdge(u, v);
            		   }
            	   }
            	   if (xg1.DG.isEdge(u, v) && xg2.DG.isEdge(v, u)) { //direction in the 2nd tiling is different from dir
            		   //try to flip the edge in xg2
            		   if (xg2.tiling.isFlip(tile2u, tile2v)) {
            			   goodEdges.addEdge(u, v);
            		   }
            	   }
               }
			} 
		}
		return goodEdges;
	}
	
	public Graph countBad(int dir) {
		int count = 0;
		int V = xg1.V;
		Graph badEdges = new Graph(V);
		// we need to go over all possible edges;
		for (int u = 0; u < V - 1; u++) {
			XRibTile tile1u = xg1.tiling.tiles().get(u); //tile that corresponds to u in the first tiling
			XRibTile tile2u = xg2.tiling.tiles().get(u); //tile that corresponds to v in the second tiling
			for (int v = u + 1; v < V; v++) {
               if (!xg1.isFreeAndComparable(u, v)) { //the orientation is either forced on (u, v)
            	                                     //or there is no such edge.        	   
            	   continue;
               }			
            XRibTile tile1v = xg1.tiling.tiles().get(v); 
   			XRibTile tile2v = xg2.tiling.tiles().get(v); 
               if (dir > 0) { // the target orientation is from v to u
            	   if (xg1.DG.isEdge(u, v) && xg2.DG.isEdge(u, v)) { //in both tilings the edge orientation is opposite to dir
            		   if (xg1.tiling.isFlip(tile1u, tile1v) && !xg2.tiling.isFlip(tile2u, tile2v)) {
            			   badEdges.addEdge(u, v);
            		   }
            		   if (!xg1.tiling.isFlip(tile1u, tile1v) && xg2.tiling.isFlip(tile2u, tile2v)) {
            			   badEdges.addEdge(u, v);
            		   }
            	   }
               } else { // the target orientation is from u to v
            	   if (xg1.DG.isEdge(v, u) && xg2.DG.isEdge(v, u)) { //in both tilings the edge orientation is opposite to dir
            		   if (xg1.tiling.isFlip(tile1u, tile1v) && !xg2.tiling.isFlip(tile2u, tile2v)) {
            			   badEdges.addEdge(u, v);
            		   }
            		   if (!xg1.tiling.isFlip(tile1u, tile1v) && xg2.tiling.isFlip(tile2u, tile2v)) {
            			   badEdges.addEdge(u, v);
            		   }
            	   }
               }
			}
		}
		return badEdges;
	}
	
	/*
	 * Evaluates the expected change in the Spearman distance between graphs xg1 and xg2;
	 * For each pair (i, i+1), looks up tiles t_i and t_{i+1} in the sink sequence
	 * and tries to flip them in both graphs. Measures the resulting distance. 
	 * Returns the sum over all (i, i+1);
	 */

	
	public int expEffectOnSpearman(XGraph xg1, XGraph xg2) {
		XGraph xg1copy, xg2copy;
		XRibTile tile1, tile2;
		int eff = 0;
		int V = xg1.V;
		int d0 = XUtility.distSpearman(xg1, xg2);
		ArrayList<Integer> sinkseq1 = XUtility.findSinkSequence(xg1.DG);
		ArrayList<Integer> sinkseq2 = XUtility.findSinkSequence(xg2.DG);
		for (int i = 0; i < V - 1; i++) {
			xg1copy = new XGraph(xg1);
			int u = sinkseq1.get(i);
			int v = sinkseq1.get(i + 1);
			if (xg1copy.isFreeAndComparable(u, v)) {
			tile1 = xg1copy.tiling.tiles().get(u);
			tile2 = xg1copy.tiling.tiles().get(v);
			//StdOut.println("Graph1: tile1 = " + tile1 + ", tile2 = " + tile2);
			if (xg1copy.tiling.isFlip(tile1, tile2)) {
				xg1copy.tiling.flip(tile1, tile2);
			}
			}
			xg2copy = new XGraph(xg2);
			u = sinkseq2.get(i);
			v = sinkseq2.get(i + 1);
			if (xg2copy.isFreeAndComparable(u, v)) {
			tile1 = xg2copy.tiling.tiles().get(u);
			tile2 = xg2copy.tiling.tiles().get(v);
			//StdOut.println("Graph2: tile1 = " + tile1 + ", tile2 = " + tile2);
			if (xg2copy.tiling.isFlip(tile1, tile2)) {
				xg2copy.tiling.flip(tile1, tile2);
			}
			}
		int d1 = XUtility.distSpearman(xg1copy, xg2copy);
		eff = eff + d1 - d0;
		}
		return eff;
	}

	/**
	 * Performs a step of a coupling between graphs xg1 and xg2.
	 */
	
	public void coupleStep() {
		boolean flag = false;
		while (!flag) {
		int i = StdRandom.uniform(xg1.V);
		int j = StdRandom.uniform(xg2.V);
		int coin = StdRandom.uniform(2);
		if (xg1.isFreeAndComparable(i, j)) {
			flag = true;
			XRibTile a1 = xg1.tiling.tiles().get(i);
			XRibTile b1 = xg1.tiling.tiles().get(j);
			XRibTile a2 = xg2.tiling.tiles().get(i);
			XRibTile b2 = xg2.tiling.tiles().get(j);
			
			if ((a1.compareWeak(b1)==1) && (a2.compareWeak(b2)==-1) 
					|| (a1.compareWeak(b1)==-1) && (a2.compareWeak(b2)==1)) { //orientations of the edge are opposite 
				                                                               //in the graphs
				if (coin == 1) {
					if (xg1.tiling.isFlip(a1, b1)) {
						xg1.tiling.flip(a1, b1);
					}
				} else {
					if (xg2.tiling.isFlip(a2, b2)) {
						xg2.tiling.flip(a2, b2);
					}
				}
				
			} else { //the orientations are the same in both graphs
				if (coin == 1) {
					if (xg1.tiling.isFlip(a1, b1)) {
						xg1.tiling.flip(a1, b1);
					}
					if (xg2.tiling.isFlip(a2, b2)) {
						xg2.tiling.flip(a2, b2);
					}
				}
			}			
		}
		}
	}
	
	public ArrayList<Integer> couple(int T) { //performs T coupling steps.
		ArrayList<Integer> distances = new ArrayList<Integer>(T);
		for (int i = 0; i < T; i++) {
			coupleStep();
			distances.add(distance());
		}	
		return distances;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int n = 4;
		int M = 8;
		int N = 11;
		Coupling cpl = new Coupling(n, M, N);
		int size = 400;
        cpl.draw(size);
        int dist = XUtility.distance(cpl.xg1, cpl.xg2);
        int distSp = XUtility.distSpearman(cpl.xg1, cpl.xg2);
        int eff = cpl.expEffectOnSpearman(cpl.xg1, cpl.xg2);
        distSp = XUtility.distSpearman(cpl.xg1, cpl.xg2);
        //StdOut.println("Graph1 = " + cpl.xg1.DG);
        //StdOut.println("Graph2 = " + cpl.xg2.DG);
        StdOut.println("distance = " + dist);
        StdOut.println("Spearman distance = " + distSp);
        StdOut.println("Expected effect on Spearman distance = " + eff);
        //StdOut.println("Spearman distance = " + distSp);
        /*
        StdOut.println("CountGood Graph for dir = 1 is " + cpl.countGood(1));
        StdOut.println("CountBad Graph for dir = 1 is " + cpl.countBad(1));
        StdOut.println("CountGood Graph for dir = -1 is " + cpl.countGood(-1));
        StdOut.println("CountBad Graph for dir = - 1 is " + cpl.countBad(-1));
        StdOut.println("CountGood for dir = 1 is " + cpl.countGood(1).E());
        StdOut.println("CountBad for dir = 1 is " + cpl.countBad(1).E());
        StdOut.println("CountGood for dir = -1 is " + cpl.countGood(-1).E());
        StdOut.println("CountBad for dir = - 1 is " + cpl.countBad(-1).E());
        */
        int T = 50000;
        ArrayList<Integer> distances = cpl.couple(T);
        XUtility.plotIntArray(distances);
	}

}
