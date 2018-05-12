package rib3;
//import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.In;
//import edu.princeton.cs.algs4.StdOut;

//import java.util.ArrayList;

/*
 * This class has not yet been fully implemented. The code below is just a cut-paste of a code that was working in
 * the context of a visualizer class.
 */

public class ManualFlipHandler {
	private RibTiling tiling;



	public ManualFlipHandler(String fName) {
		int size = 400;
		int offset = 400;
		In in = new In(fName);
		tiling = new RibTiling(in, fName, size, size, offset, offset);
		in.close();
		tiling.draw(tiling.myDr, "vertex");	//drawing the tiling
		tiling.G.draw(tiling.myDr); //draw the cover graph
	} 

	public void doFlips() {
		// Manual flips 
		RibTile t1, t2;
		double xCoord, yCoord;
		while (true) {
			//StdOut.println("Choose the first tile or click outside of the board to exit.");

			// wait for a mouse click 
			while (true) {
				if (tiling.myDr.mousePressed()) {
					xCoord = tiling.myDr.mouseX();
					yCoord = tiling.myDr.mouseY();
					while (tiling.myDr.mousePressed()) {
						continue; //wait for the end of the click
					}
					break;
				}
			}

			t1 = tiling.findTile(xCoord, yCoord);
			if (t1 == null) {
				//StdOut.println("Exiting the manual flip mode.");
				break; // exiting the main loop
			} else {
				//StdOut.println("Choose the second tile: ");
				while (true) {
					if (tiling.myDr.mousePressed()) {
						xCoord = tiling.myDr.mouseX();
						yCoord = tiling.myDr.mouseY();
						while (tiling.myDr.mousePressed()) {
							continue; //wait for the end of the click
						}
						break;
					}
				}

				t2 = tiling.findTile(xCoord, yCoord);
				if (t2 == null) {
					//StdOut.println("Invalid location of the second tile.");
					continue;
				}


				// a check that these two tiles are flippable.
				//StdOut.println("Chosen two tiles are: ");
				//StdOut.println(t1);
				//StdOut.println(t2);
				boolean f = tiling.isFlip(t1, t2);
				if (!f) {
					//StdOut.println("These tiles are not flippable");
				} else {
					int v1 = tiling.G.labels2vertices.get(t1);
					int v2 = tiling.G.labels2vertices.get(t2);
					tiling.G.flip(v1, v2);
					tiling.G.update();
					tiling.G.reduce();

					tiling.draw(tiling.myDr, "vertex");	//drawing the tiling
					tiling.G.draw(tiling.myDr); //draw the graph
				}
			} 
		}

	}
	
    /**
     * 
     */
    public static void main(String[] args) {
    	ManualFlipHandler mfh = new ManualFlipHandler("savedTiling.txt");
    	mfh.doFlips();
    	/*
    	StdOut.print(mfh.tiling.G.labels.get(3));
    	StdOut.println();
    	StdOut.print(mfh.tiling.G.labels.get(11));
    	StdOut.println();
    	StdOut.println(mfh.tiling.G.labels.get(3).compareWeak(mfh.tiling.G.labels.get(11))); 
    	StdOut.println();
    	StdOut.print(mfh.tiling.G.DG);
    	*/
    }

}
