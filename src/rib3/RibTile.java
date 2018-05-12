package rib3;
import edu.princeton.cs.algs4.Point2D;	
//import edu.princeton.cs.algs4.Line2D;	
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Draw; 
//import edu.princeton.cs.algs4.StdDraw; 
import java.util.HashSet;

	/******************************************************************************
	 *  Compilation:  javac RibTile.java
	 *  Execution:    none
	 *  Dependencies: Point2D.java
	 *  
	 *  Idea is Based on RectHV class from algs4 package.
	 *
	 ******************************************************************************/

	/**
	 *  The {@code RibTile} class is an immutable data type to encapsulate a
	 *  ribbon tile with 3 squares.
	 *  
	 *  @author Vladislav Kargin
	 */

	public final class RibTile implements Comparable<RibTile>{
	    protected double xmin, ymin;   // minimum x- and y-coordinates
	    protected double xmax, ymax;   // maximum x- and y-coordinates
	    protected int type; //type of tile: 0 - vertical, 1 - horizontal,
        // 2  _  (Russian G)
        //   |
        // 3   _|
	    protected RibTiling tiling; //reference to the tiling that contains this tile.
	    protected int typeCode; // a more formal code for the type. 0 if a square in the tile goes right, 1 if it goes up
	    //We write it as an integer, the lowest byte corresponds to the first step and the highest byte corresponds to the
	    //last step.
	    // So type vertical 0 -> "11" = 3, horizontal 1 -> "00" = 0, Russian G 2 -> "01" = 1, and reflected L 3 -> "10" = 2
	    protected boolean isEmpty = false;
	    protected HashSet<Line2D> border;
	    protected long level;
        protected final int n = 3;
        /**
         * Constructs an empty tile;
         */
        public RibTile() { 
        	isEmpty = true;
        }
	    /**
	     * Initializes a new tile (Somewhat DEPRECATED constructor because uses type, which cannot be generalized.)
	     * Use the second form of the constructor instead.
	     * However, it is currently still used in RibTiling;
	     */
	    public RibTile(double xmin, double ymin, int type, RibTiling tiling) {
	    	this(xmin, ymin, type2typeString(type), tiling);
	    }
	    
	    /**
	     * Initializes a new tile using typeCode represented as a string of 0 and 1.
	     */
	    public RibTile(double xmin, double ymin, String typeString, RibTiling tiling) {
	        if (Double.isNaN(xmin))
	            throw new IllegalArgumentException("x-coordinate cannot be NaN");
	        if (Double.isNaN(ymin))
	            throw new IllegalArgumentException("y-coordinates cannot be NaN");
	        this.tiling = tiling;
	        this.xmin = xmin;
	        this.ymin = ymin;
	        typeCode = Integer.parseInt(typeString, 2);
	        xmax = xmin + 1;
	        ymax = ymin + 1;
	        for (int i = 0; i < n - 1; i++) {
	        	if (((typeCode >> i) & 1) == 1){
	        		ymax++;
	        	} else {
	        		xmax++;
	        	}
	        }
	        switch (typeCode) {
	        case 3: type = 0;
	                break;
	        case 0: type = 1;
                    break;
	        case 1: type = 2;
                    break;
	        case 2: type = 3;
                    break;
	        }
	        level = (long) Math.floor(xmin + ymin);
	        //create Border
	        border = calcBorder();
	        
	    }
	    /**
	     * Creates a copy of an existing tile
	     */
	    public RibTile(RibTile tile) {
	    	this.xmin = tile.xmin;
	    	this.ymin = tile.ymin;
	    	this.xmax = tile.xmax;
	    	this.ymax = tile.ymax;
	    	this.type = tile.type;
	    	this.typeCode = tile.typeCode;
	    	this.level = tile.level;
	    	this.border = tile.border;
	    	this.tiling = tile.tiling;
	    }
	      
	    /**
	     * Creates a HashSet that contains the border edges for the tile.
	     * 
	     */
	    private HashSet<Line2D> calcBorder() {
	    	border = new HashSet<Line2D>();
	        // first square.
	        int xminP = (int) xmin;
	        int yminP = (int) ymin;
	        

	        border.add(new Line2D(xminP, yminP, xminP + 1, yminP));
	        border.add(new Line2D(xminP, yminP, xminP, yminP + 1));
	        border.add(new Line2D(xminP + 1, yminP, xminP + 1, yminP + 1));
	        border.add(new Line2D(xminP, yminP + 1, xminP + 1, yminP + 1));
	        for (int k = 0; k < n - 1; k++) {
	        	int bit = (typeCode >> k) & 1;
	        	if (bit == 0) { //moving to the right.
	        		xminP = xminP + 1;
	    	        border.add(new Line2D(xminP,yminP, xminP + 1, yminP)); //horizontal bottom
	    	        border.remove(new Line2D(xminP,yminP, xminP, yminP + 1)); //vertical left
	    	        border.add(new Line2D(xminP + 1, yminP, xminP + 1, yminP + 1)); //vertical right
	    	        border.add(new Line2D(xminP, yminP + 1, xminP + 1, yminP + 1));	//horizontal top
	        	} else {
	        		yminP = yminP + 1;
	    	        border.remove(new Line2D(xminP,yminP, xminP + 1, yminP)); //horizontal bottom
	    	        border.add(new Line2D(xminP,yminP, xminP, yminP + 1)); //vertical left
	    	        border.add(new Line2D(xminP + 1, yminP, xminP + 1, yminP + 1)); //vertical right
	    	        border.add(new Line2D(xminP, yminP + 1, xminP + 1, yminP + 1));	//horizontal top
	        	}	        	
	        } 
	        return border;
	    }

	    /**
	     * Returns the width of this tile.
	     *
	     * @return the width of this tile {@code xmax - xmin}
	     */
	    public double width() {
	        return xmax - xmin;
	    }

	    /**
	     * Returns the height of this tile.
	     *
	     * @return the height of this tile {@code ymax - ymin}
	     */
	    public double height() {
	        return ymax - ymin;
	    }
	    /**
	     * Convert type to typeString
	     * 
	     */
	    public static String type2typeString(int t) {
	    	// So type vertical 0 -> "11" = 3, horizontal 1 -> "00" = 0, Russian G 2 -> "01" = 1, and reflected L 3 -> "10" = 2
	    	String typeString = "";
	    	switch (t) {
	        case 0: 
	                typeString = "11";
	                break;
	        case 1: 
                    typeString = "00";
                    break;
	        case 2: 
                    typeString = "01";
                    break;
	        case 3: 
                    typeString = "10";
                    break;
	        }
	    	return typeString;
	    }

	    /**
	     * Returns true if this tile contain the point.
	     * @param  p the point
	     * @return {@code true} if this tile contain the point {@code p},
	               possibly at the boundary; {@code false} otherwise
	     */
	    public boolean contains(Point2D p) {
	    	if (!((p.x() >= xmin) && (p.x() <= xmax)
		               && (p.y() >= ymin) && (p.y() <= ymax))) { // the point is outside of the tile
		    		return false;
		    	}
	    	if (type == 0 || type == 1) {
	            return true; //The point is in the tile
	    	}
	    	if (type == 2) {
	    		return (!((p.x() > (xmin + xmax)/2)
			               && (p.y() < (ymin + ymax)/2))); 
	    	}
	    	if (type == 3) {
	    		return (!((p.x() < (xmin + xmax)/2) 
			               && (p.y() > (ymin + ymax)/2))); 
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
	    public int compareWeak(RibTile other) {
	    	if (this.equals(other)) {
	    		return 0;
	    	} 
	       double c0 = this.level; 
	       double c1 = other.level;
	       
	       double r0 = this.ymin - this.xmin; //y intercept of the lower left corner of this tile for the line y = x + r0
	       double r1 = other.ymin - other.xmin; //same for the other tile
	       double s0 = this.ymax - this.xmax; //y intercept of the upper right corner of this tile for the line y = x + s0
	       double s1 = other.ymax - other.xmax;

	    	if (c0 >= c1 + n + 1 || c1 >= c0 + n + 1) {
	    		return 0;
	    	}
	    	if (c1 > c0) { //level of the other light is greater
	    		//than the level of this tile and we know that these two tiles are comparable.
	    		// hence the other tile is to the left of this tile if and only if 
	    		//the y intercept of its lower left corner is strictly larger 
	    		// then y intercept of the upper left corner of this tile
	    		if (s0 < r1) {
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
	    }
	    
	    /**
	     * checks if this tile touches another given tile, that is, if they share at least one edge.
	     */
	    public boolean isTouch(RibTile other){
	    	/*for (Line2D edge : other.border) {
	    		StdOut.println(edge);
	    	}*/
	    	for (Line2D edge : border) {
	    		if (other.border.contains(edge)) return true;
	    	}
	    	return false;
	    }
	    
	    /**
	     * This is an override of the default compareTo function
	     */
	    @Override
	    public int compareTo(RibTile other) {
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
	        RibTile that = (RibTile) other;
	        if (this.xmin != that.xmin) return false;
	        if (this.ymin != that.ymin) return false;
	       /* if (this.xmax != that.xmax) return false;
	        if (this.ymax != that.ymax) return false; */
	        if (this.type != that.type) return false;
	        return true;
	    }

	    /**
	     * Returns an integer hash code for this tile. The hash code depends only on xmin and ymin
	     * @return an integer hash code for this tile
	     */
	    @Override
	    public int hashCode() {
	        int hash1 = ((Double) xmin).hashCode();
	        int hash2 = ((Double) ymin).hashCode();
	        int hash5 = ((Integer) type).hashCode(); 
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
	        return "[" + xmin + ", " + xmax + "] x [" + ymin + ", " + ymax + "]; Type = " + type;
	    }

	    
	    /**
	     * Draws this tile to a given window.
	     */
	    public void draw(Draw dr) {
	    	if (type == 0 || type == 1) {
	          dr.line(xmin, ymin, xmax, ymin);
	          dr.line(xmax, ymin, xmax, ymax);
	          dr.line(xmax, ymax, xmin, ymax);
	          dr.line(xmin, ymax, xmin, ymin);
	    	} else if (type == 2) {//bukva G
		          dr.line(xmin, ymin, xmin, ymax);
		          dr.line(xmin, ymax, xmax, ymax);
		          dr.line(xmax, ymax, xmax, (ymin + ymax)/2);
		          dr.line(xmax, (ymin + ymax)/2, (xmin + xmax)/2, (ymin + ymax)/2);
		          dr.line((xmin + xmax)/2, (ymin + ymax)/2, (xmin + xmax)/2, ymin);
		          dr.line((xmin + xmax)/2, ymin, xmin, ymin);
	    	} else { // mirrored L
		          dr.line(xmin, ymin, xmin, (ymin + ymax)/2);
		          dr.line(xmin, (ymin + ymax)/2, (xmin + xmax)/2, (ymin + ymax)/2);
		          dr.line((xmin + xmax)/2, (ymin + ymax)/2, (xmin + xmax)/2, ymax);
		          dr.line((xmin + xmax)/2, ymax, xmax, ymax);
		          dr.line(xmax, ymax, xmax, ymin);
		          dr.line(xmax, ymin, xmin, ymin);
	    	}
	    }
	    
	    /**
	     * Draws this tile to a given window, color filled and with the given marker.
	     * 
	     * @param dr - window for drawing
	     * @param marker 
	     */
	    public void drawFilled(Draw dr, int marker) {	
	    	String x;
	    	if (marker < 0) {
	    		x = ""; 
	    	} else {
 		        x = String.valueOf(marker);
	    	}
		if (type == 1) { //horizontal tile
			dr.setPenColor(Draw.BOOK_RED);
			dr.filledRectangle((xmin + xmax)/2, (ymin + ymax)/2,
					(- xmin + xmax)/2, (- ymin + ymax)/2);
			dr.setPenColor(Draw.BLACK);
			dr.text((xmin + xmax)/2, (ymin + ymax)/2 - 0.25, x);
		} else if (type == 0) { // vertical tile
			dr.setPenColor(Draw.BOOK_LIGHT_BLUE);
			dr.filledRectangle((xmin + xmax)/2, (ymin + ymax)/2,
					(- xmin + xmax)/2, (- ymin + ymax)/2);
			dr.setPenColor(Draw.BLACK);
			dr.text((xmin + xmax)/2 + 0.25, (ymin + ymax)/2, x);
		} else if (type == 2){ // Bukva g 
			dr.setPenColor(Draw.GREEN);
			dr.filledRectangle((3 * xmin + xmax)/4, (3 * ymin + ymax)/4,
					(- xmin + xmax)/4, (- ymin + ymax)/4);
			dr.filledRectangle((3 * xmin + xmax)/4, (ymin + 3 * ymax)/4,
					(- xmin + xmax)/4, (- ymin + ymax)/4);
			dr.filledRectangle((xmin + 3 * xmax)/4, (ymin + 3 * ymax)/4,
					(- xmin + xmax)/4, (- ymin + ymax)/4);
			dr.setPenColor(Draw.BLACK);
			dr.text((xmin + xmax)/2 + 0.25, (ymin + ymax)/2 + 0.25, x);
		} else { // mirrored L
			dr.setPenColor(Draw.YELLOW);
			dr.filledRectangle((3 * xmin + xmax)/4, (3 * ymin + ymax)/4,
					(- xmin + xmax)/4, (- ymin + ymax)/4);
			dr.filledRectangle((xmin + 3 * xmax)/4, (3 * ymin + ymax)/4,
					(- xmin + xmax)/4, (- ymin + ymax)/4);
			dr.filledRectangle((xmin + 3 * xmax)/4, (ymin + 3 * ymax)/4,
					(- xmin + xmax)/4, (- ymin + ymax)/4);
			dr.setPenColor(Draw.BLACK);
			dr.text((xmin + xmax)/2 + 0.25, (ymin + ymax)/2 + 0.25, x);
			
		}
		dr.setPenColor(Draw.BLACK);
		draw(dr);
    }
	    
	   public void drawMark(Draw dr, int mark) {
		   switchColor(dr, "pink");
       	int bit = (typeCode >> 0) & 1;
       	if (bit == 0) { //moving to the right.
       		dr.text(xmin + 0.5 + 1, ymin + 0.5, String.valueOf(mark));
       	} else {
       		dr.text(xmin + 0.5, ymin + 0.5 + 1, String.valueOf(mark));
       	}
       	switchColor(dr, "black");
	   }
	    
	    
	    /**
	     * Draws this tile to a given window, color filled and with a  level mark.
	     */
	    public void drawFilled(Draw dr) {
	    	drawFilled(dr, "level");
	    }
	    
	    /**
	     * Draws this tile to a given window, color filled and with a mark.
	     * The mark is level if choice = "level", and it is the label of the 
	     * corresponding vertex in the graph if choice = "vertex". If choice is "none",
	     * no mark is used.
	     */
	    public void drawFilled(Draw dr, String choice) {
	    	switch (choice) {
	    	   case "level": 
	    		   drawFilled(dr, (int) level);
	    	       break;
	    	   case "vertex":
	    		   drawFilled(dr, tiling.G.labels2vertices.get(this));
	    		   break;	 
	    	   default:
	    		   drawFilled(dr, -1);
	    	}
	    }
	    
	    //draws the border of this tile in white
	    public void drawSpecial(Draw dr) {
           drawBorder(dr);
	    }
	  //draws the border of this tile using the selected color
	    public void drawSpecial(Draw dr, String color) {
	    	double x1, y1, r;
	    	x1 = xmin + 0.5;
	    	y1 = ymin + 0.5;
	    	r = 0.2;
	    	switchColor(dr, color);
	           dr.filledCircle(x1, y1, r);
		    }
	    //draws the border of this tile in white
	    public void drawBorder(Draw dr) {
	    	dr.setPenColor(Draw.WHITE);
	    	for (Line2D line: border) {
	    		line.draw(dr);
	    	}
	    }
	    
	    //helps us to set PenColor
	    private void switchColor(Draw dr, String color) {
	    	switch (color) {
	    	case "white": 
	    		dr.setPenColor(Draw.WHITE);
	    		break;
	    	case "black":
	    		dr.setPenColor(Draw.BLACK);
	    		break;
	    	case "blue":
	    		dr.setPenColor(Draw.BLUE);
	    		break;
	    	case "cyan":
	    		dr.setPenColor(Draw.CYAN);
	    		break;
	    	case "dark_gray":
	    		dr.setPenColor(Draw.DARK_GRAY);
	    		break;
	    	case "gray": 
	    		dr.setPenColor(Draw.GRAY);
	    		break;
	    	case "green": 
	    		dr.setPenColor(Draw.GREEN);
	    		break;
	    	case "light_gray":
	    		dr.setPenColor(Draw.LIGHT_GRAY);
	    		break;
	    	case "magenta":
	    		dr.setPenColor(Draw.MAGENTA);
	    		break;
	    	case "orange":
	    		dr.setPenColor(Draw.ORANGE);
	    		break;
	    	case "pink":
	    		dr.setPenColor(Draw.PINK);
	    		break;
	    	case "red":
	    		dr.setPenColor(Draw.RED);
	    		break;
	    	case "yellow":
	    		dr.setPenColor(Draw.YELLOW);
	    		break;
	    	case "book_blue":
	    		dr.setPenColor(Draw.BOOK_BLUE);
	    	case "book_light_blue":
	    		dr.setPenColor(Draw.BOOK_LIGHT_BLUE);
	    		break;
	    	case "book_red":
	    		dr.setPenColor(Draw.BOOK_RED);
	    		break;
	    	default: 
	    		dr.setPenColor(Draw.BLACK);
	    		break;
	    	}	
	    }

	    //draws the border of this tile using the selected color
	    public void drawBorder(Draw dr, String color) {
	    	switchColor(dr, color);
	    	for (Line2D line: border) {
	    		line.draw(dr);
	    	}
	    }
	    
	    /**
	     * Draws the matching  corresponding to this tile. The input type is supposed 
	     * to tell the function how to color the matching.
	     * @param dr
	     * @param type
	     */
	    public void drawMatching(Draw dr, int type) {
	    	double x1, x2, y1, y2;
	    	x1 = xmin + 0.5;
	    	y1 = ymin + 0.5;
	    	int squareType = (int) level % n;
	        for (int i = 0; i < n - 1; i++) {
	        	if (((typeCode >> i) & 1) == 1){ //going up
	        		x2 = x1; 
	        		y2 = y1 + 1;
	        	} else {
	        		x2 = x1 + 1;
	        		y2 = y1;
	        	}
	        	if (type != 0) {
	        	switch (squareType) {
	        	case 0: switchColor(dr, "red");
	        	        break;
	        	case 1: switchColor(dr, "blue");
	        	        break;
	        	case 2: switchColor(dr, "green");
	                    break;
	            default: switchColor(dr, "black");
	                    break;
	        	}
	        	} else {
	        		switchColor(dr, "black");
	        	}
	        	dr.line(x1, y1, x2, y2);
	        	x1 = x2;
	        	y1 = y2;
	        	squareType = (squareType + 1) % n;
	        }
	    }
    	/*
    	 * prints the information about this tile
    	 * 
    	 */
	    public void print() {
	    	StdOut.print( "(" + xmin + ", " + ymin + ", " +  xmax + ", " + ymax + ": " + type + ")" );
	    }
	    /*
	     * Checks the validity of the tile
	     */
	    public Boolean isValid() {
	    	if (type == 0 && xmax - xmin == 1 && ymax - ymin == 3) return true;
	    	if (type == 1 && xmax - xmin == 3 && ymax - ymin == 1) return true;
	    	if ((type == 2 || type == 3) && xmax - xmin == 2 && ymax - ymin == 2) return true;
	    	// otherwise
	    	return false;
	    }
	    

	    /**
	     * For testing methods.
	     */
	    public static void main(String[] args) {
	    	RibTile tile = new RibTile(1, 2, "10", null);
			int size = 10; 
			Draw dr = new Draw();
			dr.setXscale(-0.5, size + 0.5);
			dr.setYscale(-0.5, size + 0.5);
			dr.clear(Draw.LIGHT_GRAY);
			dr.setPenRadius(0.005);
			tile.drawFilled(dr);
	    	//tile.drawBorder(dr);
	    	//tile.drawMatching(dr, 0);
	    	tile.drawMark(dr, (int) tile.level);
	    	dr.show();
	    }
}
