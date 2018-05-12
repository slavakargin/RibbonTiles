package xrib;
import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
import java.util.ArrayList;
import java.util.TreeSet;
import rib3.Line2D;

/**
	 *  The {@code RibTile} class is an immutable data type to encapsulate a
	 *  ribbon tile with n squares.
	 *  
	 *  @author Vladislav Kargin
	 */
public final class XRibTile implements Comparable<XRibTile> {
	
	    final Integer xmin, ymin;   // minimum x- and y-coordinates
	    final Integer xmax, ymax;   // maximum x- and y-coordinates
	    final int level, n;
	    final int typeCode; // a code for the type. 0 if a square in the tile goes right, 1 if it goes up
	    //We write it as an integer, the lowest byte corresponds to the first step and the highest byte corresponds to the
	    //last step.
	    // So type vertical is "11" = 3, horizontal is "00" = 0, Russian G is "01" = 1, and reflected L is "10" = 2
	    //Or in yet another words, if we start with lowest left square ("root") then we should read typeCode 
	    //in Jewish manner,
	    // from right to left, and so "0111" means up, up, up, right.
	    private final TreeSet<Square> squares; //squares in the tile. They are ordered 
	    // by using the order on squares: if level of a square1 is smaller than level of square2, then square1 < square2
	    // boolean isEmpty = false;
	    private final ArrayList<Line2D> border;
	    
        
    
        /**
         * Copy constructor;
         */
         XRibTile(XRibTile tile) { 
        	this.xmin = tile.xmin;
        	this.ymin = tile.ymin;
        	this.xmax = tile.xmax;
        	this.ymax = tile.ymax;
        	this.typeCode = tile.typeCode;
        	this.squares = tile.squares;
        	this.border = tile.border;
        	this.level = tile.level;
        	this.n = tile.n;
        }
        
        /**
         * Initializes a new tile from a bunch of squares. The functions assumes that the bunch of squares
         * satisfies the conditions for a proper ribbon tile. 
         */
        XRibTile(TreeSet<Square> squares) {
        	this.n = squares.size();
        	this.squares = squares;
        	int count = 0;
        	int x0 = 0; 
        	int myXmin = squares.first().x;
        	int myYmin = squares.first().y;
        	int myXmax = squares.first().x;
        	int myYmax = squares.first().y;
        	int myTypeCode = 0;
        	
        	for (Square s : squares) {
                if (myXmin > s.x){
        			myXmin = s.x;
        		} else if (myXmax < s.x){
        			myXmax = s.x;
        		}
                if (myYmin > s.y){
        			myYmin = s.y;
        		} else if (myYmax < s.y){
        			myYmax = s.y;
        		}
                if (count == 0) {
                	x0 = s.x;
                } else if (s.x == x0) {// vertical step
                	myTypeCode = myTypeCode | (1 << (count - 1)); // and x0 does not change
                } else { //horisontal step;typecode does not change but x0 should be updated.
                	x0 = s.x;
                }
                count++;
        	}   
            xmin = myXmin;
            ymin = myYmin;
            xmax = myXmax;
            ymax = myYmax;
        	level = xmin + ymin;
        	typeCode = myTypeCode;
        	border = calcBorder();
        }
        
	    /**
	     * Initializes a new tile using typeCode represented as a string of 0 and 1.
	     */
	    XRibTile(int xmin, int ymin, String typeString) {
	        if (Double.isNaN(xmin))
	            throw new IllegalArgumentException("x-coordinate cannot be NaN");
	        if (Double.isNaN(ymin))
	            throw new IllegalArgumentException("y-coordinates cannot be NaN");
	        //this.tiling = tiling;
	        this.xmin = xmin;
	        this.ymin = ymin;
	        n = typeString.length() + 1;
	        typeCode = Integer.parseInt(typeString, 2);
	        int myXmax = xmin + 1;
	        int myYmax = ymin + 1;
	        squares = new TreeSet<Square>();
	        squares.add(new Square((int) xmin, (int) ymin));
	        for (int i = 0; i < n - 1; i++) {
	        	if (((typeCode >> i) & 1) == 1){
	        		squares.add(new Square(myXmax - 1, myYmax));
	        		myYmax++;
	        	} else {
	        		squares.add(new Square((int) myXmax, (int) myYmax - 1));
	        		myXmax++;
	        	}
	        }
	        xmax = myXmax;
	        ymax = myYmax;
	        level = xmin + ymin;
	        //create Border
	        border = calcBorder();	
	    }
	    
	    TreeSet<Square> squares() {
	    	return new TreeSet<Square>(squares);
	    }
        
	     private ArrayList<Line2D> calcBorder() {
	    	ArrayList<Line2D> myBorder  = new ArrayList<Line2D>();
	        Line2D line;
	        for (Square sq: squares) {
	        	line = new Line2D(sq.x, sq.y, sq.x + 1, sq.y);
	        	if (myBorder.contains(line)) {
	        		myBorder.remove(line);
	        	} else {
	        		myBorder.add(line);
	        	}
	        	line = new Line2D(sq.x, sq.y, sq.x, sq.y + 1);
	        	if (myBorder.contains(line)) {
	        		myBorder.remove(line);
	        	} else {
	        		myBorder.add(line);
	        	}
	        	line = new Line2D(sq.x + 1, sq.y, sq.x + 1, sq.y + 1);
	        	if (myBorder.contains(line)) {
	        		myBorder.remove(line);
	        	} else {
	        		myBorder.add(line);
	        	}
	        	line = new Line2D(sq.x, sq.y + 1, sq.x + 1, sq.y + 1);
	        	if (myBorder.contains(line)) {
	        		myBorder.remove(line);
	        	} else {
	        		myBorder.add(line);
	        	}
	        }
	        return myBorder;
	    }
	    
	    boolean containsPoint(double x, double y) { // we assume that x and y are NOT integer.
	    	//We will check every square of the tile. 
	    	Square square = new Square((int) x, (int) y);
	    	for (Square mySq : squares) {
	    		if (mySq.equals(square)) return true;
	    	}
	    	return false;
	    }
        
        /**
	     * compares two tiles in the sense of Sheffield. 
	     * That is, if this tile sends light in the northwest direction and illuminates at least some
	     * part of the other tile, then the function returns 1. (Other tile is to the left of this tile, other < this). 
	     * If the converse situation holds than the function 
	     * returns -1. Otherwise, it returns 0 (in particular if they are the same).
	     * 
	     */
	    int compareWeak(XRibTile other) {
	    	if (this.equals(other)) {
	    		return 0;
	    	} 
	       double c0 = this.level; 
	       double c1 = other.level;
	       

	    	if (c0 >= c1 + n + 1 || c1 >= c0 + n + 1) { //tiles are incomparable
	    		return 0;
	    	}
	    	
	    	for (Square s : squares) {
	    		for (Square t: other.squares) {
	    			if (s.x + s.y == t.x + t.y) { //squares at the same level
	    				if (s.x < t.x) {
	    					return -1;
	    				} else {
	    					return 1;
	    				}
	    			}
		    		if (s.x + s.y == t.x + t.y + 1) {
		    		    if (s.x <= t.x) {
		    				return -1;
		    			} else {
		    				return 1;
		    			}	
	    			}
		    		if (s.x + s.y == t.x + t.y - 1) {
		    		    if (s.x < t.x) {
		    				return -1;
		    			} else {
		    				return 1;
		    			}	
	    			}
	    		}
	    	}
	    	return 0; 
	    }
	    	
	       
	       /*
	       double r0 = this.ymin - this.xmin; //y intercept of the lower left corner of this tile for the line y = x + r0
	       double r1 = other.ymin - other.xmin; //same for the other tile
	       double s0 = this.ymax - this.xmax; //y intercept of the upper right corner of this tile for the line y = x + s0
	       double s1 = other.ymax - other.xmax;
	       

	    	if (c1 > c0) { //level of the other tile is greater
	    		//than the level of this tile and we know that these two tiles are comparable.
	    		// hence the other tile is smaller( or to the left of this tile) if and only if 
	    		//the y intercept of its lower left corner 
	    		//is strictly larger 
	    		// than y intercept of the upper left corner of this tile
	    		if (this.ymax - this.xmax < other.ymin - other.xmin) {
	    			return 1;
	    		} else {
	    			return -1;
	    		}	    		
	    	} else if (c1 < c0){ //level of the other tile is strictly smaller than
	    		//the level of this tile the situation is opposite
	    		//
	    		if (s1 < r0) {
	    			return -1;
	    		} else {
	    			return 1;
	    		}	    		
	    	} else { //the levels are equal
	    		if (this.xmin > other.xmin) {
	    			return 1;
	    		} else if (this.xmin < other.xmin) {
	    			return -1;
	    		} else {
	    			return 0;
	    		}    		
	    	}
	    	*/
        
        
	    /**
	     * This is an override of the default compareTo function
	     */
	    @Override
	    public int compareTo(XRibTile other) {
	        return compareWeak(other);
	    }

	    /**
	     * Compares this tile to the specified tile and returns true if they are the same,
	     * which happens only if this.xmin = other.xmin, this.ymin = other.ymin, and this.type = other.type
	     *
	     * @param  other the other tile
	     * @return {@code true} if this tile equals {@code other};
	     *         {@code false} otherwise
	     */
	    @Override
	    public boolean equals(Object other) {
	        if (other == this) return true;
	        if (other == null) return false;
	        if (other.getClass() != this.getClass()) return false;
	        XRibTile that = (XRibTile) other;
	        if (this.xmin != that.xmin) return false;
	        if (this.ymin != that.ymin) return false;
	        if (this.n != that.n) return false;
	        if (this.typeCode != that.typeCode) return false;
	        return true;
	    }

	    /**
	     * Returns an integer hash code for this tile. The hash code depends only on xmin and ymin
	     * @return an integer hash code for this tile
	     */
	    @Override
	    public int hashCode() {
	        int hash1 = ((Integer) xmin).hashCode();
	        int hash2 = ((Integer) ymin).hashCode();
	        int hash5 = ((Integer) typeCode).hashCode(); 
	        return 1299721 * (1299721 * hash1 + hash2) + hash5;
	    }

	    /**
	     * Returns a string representation of this tile.
	     *
	     * @return a string representation of this tile, using the format
	     *         {@code [xmin, xmax] x [ymin, ymax]}
	     */
	    @Override
	    public String toString() {
	    	StringBuilder str = new StringBuilder("(");
	    	for (Square s: squares) {
	    		str.append(s);
	    	} 	
	    	str.append(")");
	    	str.append("Type = " + XUtility.intToBinary(typeCode, n - 1));
	    	//Integer.toString(typeCode,2));
	        return str.toString();
	    }
	    
	    
	    //helps us to set PenColor
	    private void switchColor(Draw dr, int color) {
	    	switch (color) {
	    	case 0: 
	    		dr.setPenColor(Draw.WHITE);
	    		break;
	    	case 1:
	    		dr.setPenColor(Draw.BLUE);
	    		break;
	    	case 2:
	    		dr.setPenColor(Draw.CYAN);
	    		break;
	    	case 3: 
	    		dr.setPenColor(Draw.GREEN);
	    		break;
	    	case 4:
	    		dr.setPenColor(Draw.BOOK_RED);
	    		break;
	    	case 5:
	    		dr.setPenColor(Draw.MAGENTA);
	    		break;
	    	case 6:
	    		dr.setPenColor(Draw.ORANGE);
	    		break;
	    	case 7:
	    		dr.setPenColor(Draw.YELLOW);
	    		break;
	    	case 8:
	    		dr.setPenColor(Draw.RED);
	    		break;
	    	case 9:
	    		dr.setPenColor(Draw.PINK);
	    		break;
	    	case 10:
	    		dr.setPenColor(Draw.RED);
	    		break;
	    	case 11:
	    		dr.setPenColor(Draw.YELLOW);
	    		break;
	    	case 12:
	    		dr.setPenColor(Draw.BOOK_BLUE);
	    	case 13:
	    		dr.setPenColor(Draw.BOOK_LIGHT_BLUE);
	    		break;
	    	case 14: 
	    		dr.setPenColor(Draw.DARK_GRAY);
	    		break;
	    	case 15:
	    		dr.setPenColor(Draw.BLACK);
	    		break;
	    	default: 
	    		dr.setPenColor(Draw.BLACK);
	    		break;
	    	}	
	    }

	    
	    
	    /**
	     * Draws this tile to a given window. 
	     * 
	     */
	    void draw(Draw dr) {
	    	int color;
	    	switch (n) {
	    	case 2: 
	    		if (typeCode == 0) {
	    			if (level % 2 == 0) {
	    			 dr.setPenColor(Draw.RED);
	    			} else {
	    		     dr.setPenColor(Draw.YELLOW);
	    			}
	    		} else {
	    			if (level % 2 == 0) {
		    			 dr.setPenColor(Draw.BLUE);
		    			} else {
		    		     dr.setPenColor(Draw.GREEN);
		    			}
	    		}
	    		break;
	    	case 3: 
	    		if (typeCode == 0) {
	    			if (level % 3 == 0) {
	    			 dr.setPenColor(Draw.WHITE);
	    			} else if (level % 3 == 1){
	    		     dr.setPenColor(Draw.LIGHT_GRAY);
	    			} else {
	    			 dr.setPenColor(Draw.GRAY);
	    			}
	    		} else if (typeCode == 1) {
	    			if (level % 3 == 0) {
		    			 dr.setPenColor(Draw.BOOK_RED);
		    		} else if (level % 3 == 1){
		    		     dr.setPenColor(Draw.ORANGE);
		    		} else {
		    			 dr.setPenColor(Draw.RED);
		    		} 
	    		} else if (typeCode == 2) {
	    			if (level % 3 == 0) {
		    			 dr.setPenColor(Draw.GREEN);
		    		} else if (level % 3 == 1){
		    		     dr.setPenColor(Draw.CYAN);
		    		} else {
		    			 dr.setPenColor(Draw.MAGENTA);
		    		} 
	    		} else {
	    			if (level % 3 == 0) {
		    			 dr.setPenColor(Draw.BOOK_LIGHT_BLUE);
		    		} else if (level % 3 == 1){
		    		     dr.setPenColor(Draw.BLUE);
		    		} else {
		    			 dr.setPenColor(Draw.BLACK);
		    		} 
	    		}
	    		break;
	    	default: 
	    		//StdOut.println("Swhitching color. Typecode = " + typeCode);
	    	    color = typeCode % 16;
		    	switchColor(dr, color);
	    		break;
	    	}

	    	for (Square sq : squares) {
	    		sq.draw(dr);
	    	}
            dr.setPenColor(Draw.BLACK);
            for (Line2D line : border) {
            	line.draw(dr);
            }
	    }
	    
	    
	    /**
	     * For testing methods.
	     */
	    public static void main(String[] args) {
	    	XRibTile tile = new XRibTile(1, 2, "111");
	    	TreeSet<Square> bagSquares = new TreeSet<Square>();
	    	bagSquares.add(new Square(1, 2));
	    	bagSquares.add(new Square(2, 2));
	    	bagSquares.add(new Square(2, 3));
	    	bagSquares.add(new Square(2, 4));
	    	tile = new XRibTile(bagSquares);
	    	StdOut.println("Tile's typecode = " + tile.typeCode);
			int size = 10; 
			Draw dr = new Draw();
			dr.setXscale(-0.5, size + 0.5);
			dr.setYscale(-0.5, size + 0.5);
			dr.clear(Draw.LIGHT_GRAY);
			dr.setPenRadius(0.005);
			tile.draw(dr);
			double x = 1.5;
			double y = 2.6;
			double r = 0.1;
			dr.setPenColor(Draw.RED);
			dr.filledCircle(x, y, r);
			StdOut.println("Contains the point? " + tile.containsPoint(x,y));
	    	//tile.drawBorder(dr);
	    	//tile.drawMatching(dr, 0);
	    	//tile.drawMark(dr, (int) tile.level);
	    	dr.show();
	    	StdOut.println("Border = " + tile.border);
	    }
        
}
