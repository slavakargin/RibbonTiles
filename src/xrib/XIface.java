/**
 * 
 */
package xrib;
import java.util.ArrayList;

/**
 * This class represent an interface in the ribbon tiling of a horizotal strip by n-tiles.
 * An interface is a sequence of M non-decreasing integers that starts with zero 
 * and has the following restrictions:
 *  x_k - x_l <= (n - 1) * (k - l + 1) + 1, for k > l.
 *  
 * We will use the interfaces to compute the per tile entropy for ribbon tilings.
 * of horizontal strips.   
 *
 * @author vladislavkargin
 *
 */
public class XIface {
	private int n; //the size of a ribbon tile;
	private int M; //the width of the interface; 
	private ArrayList<Integer> X; // a non-decreasing collection of M integers starting with 0;
	
	public XIface(int n, int M, ArrayList<Integer> X) {
		this.n = n;
		this.M = M;
		this.X = new ArrayList<Integer>(X);
		int x0 = X.get(0);
		if (x0 > 0) {
			for (int i = 0; i < X.size(); i++) {
				this.X.set(i, X.get(i) - x0);
			}
		}
	}
	
	public boolean isValid() {
		if (M < 0 || n < 2) { //Width is negative of the ribbon is too small 
			return false;
		}
		if (X.size() != M) { //Width is not M
			return false;
		}
		if (X.get(0) != 0) { // does not start with 0
			return false;
		}
		for (int i = 1; i < M; i++) {
			if (X.get(i) < X.get(i - 1)) { // not monotonic
				return false;
			} 
		}
		for (int i = 0; i < M - 1; i++) {
			for ( int j = i + 1; j < M; j++) {
				if (X.get(j) - X.get(i) > (n - 1) * (j - i + 1) + 1) { //restrictions on size of differences are not satisfied
					return false;				
				}
			}
		}
		return true;
	}
	
	/**
	 * Getter for X; 
	 * @return a copy of the array X.
	 */
	public ArrayList<Integer> X() {
		ArrayList<Integer> Y = new ArrayList<Integer>(X);
		return Y;
	}
	
    /**
     * Compares this interface to another interface and returns true only if they are the same,
     * which happens only if their parameters n, M, X coincide.
     *
     * @param  other the other interface
     * @return {@code true} if this interface equals {@code other};
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        XIface that = (XIface) other;
        if (this.n != that.n) return false;
        if (this.M != that.M) return false;
        for (int i = 0; i < M; i++) {
        	if (this.X.get(i) != that.X.get(i)) {
        		return false;
        	}
        }
        return true;
    }

    /**
     * Returns an integer hash code for this inteface. The hash code depends only on n, M and X.
     * @return an integer hash code for this tile
     */
    @Override
    public int hashCode() {
        int hash1 = ((Integer) n).hashCode();
        int hash2 = ((Integer) M).hashCode();
        int hash5 = X.hashCode(); 
        return 1299721 * (1299721 * hash1 + hash2) + hash5;
    }

    /**
     * Returns a string representation of this tile.
     *
     * @return a string representation of this tile, using the format
     *         {@code [x0, x1,  ..., x_{M-1}]}
     */
    @Override
    public String toString() {
    	StringBuilder str = new StringBuilder("[");
    	for (int i = 0; i < M - 1; i++) {
    		str.append(X.get(i).toString() + ", ");
    	} 	
    	str.append(X.get(M - 1).toString() + "]");
        return str.toString();
    }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
