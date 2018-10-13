package xrib;

/**
 * Represent the variable "intersection number of an interval with tiles of a specific level".
 * Essentially a pair of an interval and a level 
 * 
 */
/*
 * Currently I do not use this class. It was meant to be a part of a program for 
 * calculating a tiling of a  non-simply connected region. However, this program has to 
 * be based on an integer programming algorithm and seems to be pretty difficult 
 * (integration with Matlab Optimization Toolbox etc). 
 */

public class InterVariable  implements Comparable<InterVariable>{
	private Interval iv;
	private int level; //the level of the tiles 
	
	public InterVariable(Interval iv, int level) {
		 this.iv = new Interval(iv);
		 this.level = level;	
	}
	 
@Override
public String toString() {
	return  iv.a + ", " + iv.b + ", " + iv.level + ", " + level;
}
@Override
public boolean equals(Object other) {
	if (other == this) return true;
	if (other == null) return false;
	if (other.getClass() != this.getClass()) return false;
	InterVariable that = (InterVariable) other;
	if (!this.iv.equals(that.iv)) return false;
	if (this.level != that.level) return false;
	
	return true;
}
@Override
public int hashCode() {
	return 1299721 * iv.hashCode() + level;
}

@Override
public int compareTo(InterVariable other) {
   if (iv.compareTo(other.iv) == 1)  {
   	return 1;
   }  else if (iv.compareTo(other.iv) == -1) {
   	return -1;
   } else if ( iv.level > other.level ) {
   	return 1;
   } else if (iv.level < other.level) {
   	return -1;
   }
   return 0;
}

}
