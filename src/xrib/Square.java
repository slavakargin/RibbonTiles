package xrib;
import edu.princeton.cs.algs4.Draw;

final class Square implements Comparable<Square> {
	//the square is defined by the coordinates of its south-west corner.
	final int x; 
	final int y;
	
	//Square() {}

	Square(int x, int y) {
	   this.x = x;
	   this.y = y;
	}
	
	Square(Square s) {
		   this.x = s.x;
		   this.y = s.y;
		}
	
	/**
	 * draws the square to the specified window
	 */
	void draw(Draw dr){
    	double centerX = x + 0.5;
    	double centerY = y + 0.5;
    	dr.filledSquare(centerX, centerY, 0.5);
	}
	
	
    /**
     * Compares this square to another squqre and returns true if they are the same,
     * which happens only if this.x = other.x, this.y = other.y
     *
     * @param  other the other square
     * @return {@code true} if this square equals {@code other};
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        Square that = (Square) other;
        if (this.x != that.x) return false;
        if (this.y != that.y) return false;
        return true;
    }

    /**
     * Returns an integer hash code for this square. The hash code depends only on x and y
     * @return an integer hash code for this square
     */
    @Override
    public int hashCode() {
        return 1299721 * x + y;
    }

    /**
     * Returns a string representation of this square.
     *
     * @return a string representation of this square, using the format
     *         {@code [x, y]}
     */
    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
    
    @Override
    public int compareTo(Square other) {
        if (other.x + other.y < x + y)  {
        	return 1;
        }  else if (other.x + other.y > x + y) {
        	return -1;
        } else if ( other.x < x ) {
        	return 1;
        } else if (other.x > x) {
        	return -1;
        }
        return 0;
    }

}
