import edu.princeton.cs.algs4.Draw;

public class Line2D {
	private int x1, y1, x2, y2;
	
	public Line2D(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;		
	}	
	

    /**
     * Compares this line to a specified line and returns true if they are the same
     *
     * @param  other the other line
     * @return {@code true} if this tile equals {@code other};
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        Line2D that = (Line2D) other;
        if (this.x1 != that.x1) return false;
        if (this.y1 != that.y1) return false;
        if (this.x2 != that.x2) return false;
        if (this.y2 != that.y2) return false; 
        //if (this.type != that.type) return false;
        return true;
    }

    /**
     * Returns an integer hash code for this line. 
     * @return an integer hash code for this line
     */
    @Override
    public int hashCode() {
        return 1299721*(1299721 * (1299721 * x1 + y1) + x2) + y2;
    }

    /**
     * Returns a string representation of this tile.
     *
     * @return a string representation of this tile, using the format
     *         {@code [xmin, xmax] x [ymin, ymax]}
     */
    @Override
    public String toString() {
        return "[" + x1 + ", " + y1 + "] -> [" + x2 + ", " + y2 + "]; ";
    }

    
    /**
     * Draws this tile to a given window.
     */
    public void draw(Draw dr) {
          dr.line(x1, y1, x2, y2);
    }
}