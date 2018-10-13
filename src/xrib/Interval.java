package xrib;

import java.util.ArrayList;
import java.util.TreeSet;
//import edu.princeton.cs.algs4.Digraph;

public class Interval implements Comparable<Interval> {
	int a, b; //the x coordinates of the endpoints of the interval, multiplied by 2. 
	          //we assume a <= b  
	int level; //the line at which the interval is located, multiplied by 2
	ArrayList<Integer> diffH; //the difference in heights at the end-points of the interval.
	ArrayList<Integer> leftH; //twice the height on the left end-point of the interval
	ArrayList<Integer> tileVector; //shows how many tiles of different levels intersect this interval.
	TreeSet<Integer> tileSet; //shows which tiles intersect this interval (where tiles have the standard enumeration in 
	                          //the context of the region: in each level from left to right and in the order of increasing level.
	//Digraph Gamma; //this is a local graph that shows the order forced on tiles of different levels that intersect this interval
	//ArrayList<Integer> dotLevel; //shows the level of each dot in the digraph Gamma
	
	
	public Interval (int a, int b, int level){
		this.a = a;
		this.b = b;		
		this.level = level;
		tileSet = new TreeSet<Integer>();
	}
	
	public Interval (Interval iv) {
		this.a = iv.a;
		this.b = iv.b;
		this.level = iv.level;
	}
	//returns true if intersect the other interval. Both intervals are normalized by projecting 
	//them on the line x + y = 0 
	public boolean intersect(Interval other) {
		int a1 = 2 * this.a - this.level; 
		int b1 = 2 * this.b - this.level;
		int a2 = 2 * other.a - other.level; 
		int b2 = 2 * other.b - other.level;
		if (a2 > a1 && a2 < b1) {
			return true;
		}
		if (b2 > a1 && b2 < b1) {
			return true;
		}
		if (a2 < a1 && b2 > b1) {
			return true;
		}
		return false;
	}
	/**
	 * set the differences in heights at the end-points of the interval. 
	 * @param h
	 */
	 public void setDiffH( ArrayList<Integer> diffH) {
		 this.diffH = new ArrayList<Integer>(diffH);
	 }
	 
		/**
		 * set the height at the left end-point of the interval. 
		 * @param h
		 */
		 public void setLeftH( ArrayList<Integer> leftH) {
			 this.leftH = new ArrayList<Integer>(leftH);
		 }
	 
		/**
		 * set the vector of intersection numbers of the interval. 
		 * @param tV
		 */
		 public void setTileVector( ArrayList<Integer> tV) {
			 tileVector = new ArrayList<Integer>(tV);
		 }
	 
		 
	@Override
	public String toString() {
		return "[" + a + ", " + b + "] at l = " + level + "/2";
	}
	@Override
	public boolean equals(Object other) {
		if (other == this) return true;
		if (other == null) return false;
		if (other.getClass() != this.getClass()) return false;
		Interval that = (Interval) other;
		if (this.a != that.a) return false;
		if (this.b != that.b) return false;
		if (this.level != that.level) return false;
		
		return true;
	}
	@Override
	public int hashCode() {
		return 1299721*(1299721 * a + b) * level;
	}
	
    @Override
    public int compareTo(Interval other) {
        if (other.level < level)  {
        	return 1;
        }  else if (other.level > level) {
        	return -1;
        } else if ( other.a < a ) {
        	return 1;
        } else if (other.a > a) {
        	return -1;
        }
        return 0;
    }
}

