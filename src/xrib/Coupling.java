/**
 * 
 */
package xrib;

import java.util.ArrayList;

import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
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
        StdOut.println("Graph1 = " + cpl.xg1.DG);
        StdOut.println("Graph2 = " + cpl.xg2.DG);
        StdOut.println("distance = " + dist);
        StdOut.println("CountGood Graph for dir = 1 is " + cpl.countGood(1));
        StdOut.println("CountBad Graph for dir = 1 is " + cpl.countBad(1));
        StdOut.println("CountGood Graph for dir = -1 is " + cpl.countGood(-1));
        StdOut.println("CountBad Graph for dir = - 1 is " + cpl.countBad(-1));
        StdOut.println("CountGood for dir = 1 is " + cpl.countGood(1).E());
        StdOut.println("CountBad for dir = 1 is " + cpl.countBad(1).E());
        StdOut.println("CountGood for dir = -1 is " + cpl.countGood(-1).E());
        StdOut.println("CountBad for dir = - 1 is " + cpl.countBad(-1).E());
	}

}
