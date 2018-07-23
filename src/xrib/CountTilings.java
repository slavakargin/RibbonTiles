package xrib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.Stopwatch;

public class CountTilings {
	
	/**
	 * this counts the number of n-ribbon tilings of a given shape
	 * 
	 * [Currently, there should be at least 2 tilings of the given shape. If there is just one
	 * default tiling, then the program will never stop. 
	 * 
	 * @param shape
	 * @param n 
	 * @param maxSearchTime maximum time allowed for the search of a new tiling 
	 * @param timeLimit maximum amount of time for the counting;
	 * 
	 */
	static HashSet<XRibTiling> countTilings(int n, XRibTiling tiling, double maxSearchTime, double timeLimit) {
		XRibTile tile, otherTile;
		ArrayList<XRibTile> flips;
		int randNum;
		double startTimer;
		
		HashSet<XRibTiling> allTilings = new HashSet<XRibTiling>();
		XRibTiling xrt = new XRibTiling(tiling);
		xrt.buildTiling();
		xrt.draw();
		allTilings.add(xrt);
		Stopwatch st = new Stopwatch();
		startTimer = st.elapsedTime(); //start time for the timer which limits the search of a new tiling
		//it will be updated after a new tiling is found.
		while (st.elapsedTime() < timeLimit) {
			boolean foundFlip = false;
			while (!foundFlip) { 
				randNum = StdRandom.uniform(xrt.tiles().size());
				tile = xrt.tiles().get(randNum);
				flips = xrt.findFlips(tile);
				if (flips != null && flips.size() > 0) { //found a valid flip
					foundFlip = true;
					randNum = StdRandom.uniform(flips.size());
					otherTile = flips.get(randNum);
					boolean flag = xrt.flip(tile, otherTile);
					if (!flag) { //flag == false means that there was a problem with this flip.
						StdOut.println("there was a problem with a flip; quitting counting algorithm.");
						return allTilings;
					}
					if (st.elapsedTime() - startTimer > maxSearchTime) {
						StdOut.println("The search for a new tiling exceeded the allowed  time limit; quitting counting algorithm.");
						StdOut.println("The number of found tilings is " + allTilings.size());
						//xrt.draw(); //draw the last found;
						return allTilings;
					}
				}
			} // now we should check if this new flip leads us to a new tiling
			if (!allTilings.contains(xrt)) {
				allTilings.add(new XRibTiling(xrt)); // we add a copy of the tiling, so that later
				                                    //changes will not affect it
				startTimer = st.elapsedTime(); 
				StdOut.println("tiling counter = " + allTilings.size());
			} else {
				continue;
			}
		}
		StdOut.println("Exiting because maximum time limit is exeeded.");
		StdOut.println("The number of found tilings is " + allTilings.size());
		return allTilings;
	}
	
	public static void main(String[] args) {
		XRibTiling region;
		int n = 3; //number of squares in a ribbon
		int M = 7; //number of rows
		int N = 3; //number of columns
		double maxSearchTime = 40.; //the limit (in seconds) on the search of a new tiling;
		double timeLimit = 600.; // the limit (in seconds) on the total time spent in search; 

		//region = XRibTiling.rectangle(n, M, N);
		region = XRibTiling.stair(n, M, N);
		//region = XRibTiling.downStair(n, M, N);
	
		HashSet<XRibTiling> allTilings = countTilings(n, region, maxSearchTime, timeLimit);
		int randNum = StdRandom.uniform(allTilings.size());
		ArrayList<XRibTiling> listOfTilings = new ArrayList<XRibTiling>(allTilings);
		XRibTiling xrt = listOfTilings.get(randNum);
		xrt.draw();
		/*for (XRibTiling xrt: allTilings) {
			StdOut.println(xrt);
			xrt.draw();
		}*/
	}
} 
