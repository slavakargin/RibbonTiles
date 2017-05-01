import java.util.HashSet;
import java.util.ArrayList;
import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;

/*
 * This class describes a tiling on an N-by-M rectangle
 */
public class RibTiling {
	private HashSet<RibTile> tiling;
	public int N; //height of the rectangle
	public int M; //width of the rectangle
	private Graph G; // This the graph of the tiling. It has (N + 1)-by-(M + 1) vertices which correspond
	//to vertices of Z^2. Two vertices are connected if they belong to a border of a tile.
	//We organize the vertices as follows. The first M + 1 vertices is the vertices in the 
	//first row, the next
	//M +1 vertices is the vertices in the second row and so on. 
	
	
	/*
	 * Constructor create tiling on an N-by-M board. There are several possible initializations,
	 * which are chosen according to type variable
	 */
	public RibTiling(int N, int M, int type) {
		// N is the size of the square that we are going to tile
		// This constructor creates the default tiling:
		// If type 0, all tiles are vertical 
		// If type 1, all tiles are horizontal 
		// If type 2, the tiles are Russian bukva G and mirrored L
		// If type 3, another type of "szepka".
		// If type 4, a tiling that has two vertical tiles next to each other that differ by 1 in height.
		// If type 5, a tiling that has two horizontal tiles next to each other that differ by 1 in x coordinates
		
		RibTile tile;
		this.N = N; 
		this.M = M;
		tiling = new HashSet<RibTile>();
		if (type == 0) {
			for (int i = 0; i < M; i++) {
				for (int j = 0; j < N; j += 3) {
					tile = new RibTile(i, j, 0);
					tiling.add(tile);
				}
			}
		} else if (type == 1) {
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < M; j += 3) {
					tile = new RibTile(j, i, 1);
					tiling.add(tile);
				}
			}
		} else if (type == 2){
			for (int i = 1; i < M; i += 3) {
				for (int j = 1; j < N; j += 2) {
					tile = new RibTile(i - 1, j - 1, 2);
					tiling.add(tile);
				}				
			}
			for (int i = 2; i < M; i += 3) {
				for (int j = 1; j < N; j += 2) {
					tile = new RibTile(i - 1, j - 1, 3);
					tiling.add(tile);
				}				
			} 
		} else if (type == 3){
			for (int i = 1; i < M; i += 2) {
				for (int j = 2; j < N; j += 3) {
					tile = new RibTile(i - 1, j - 1, 2);
					tiling.add(tile);
				}				
			}
			for (int i = 1; i < M; i += 2) {
				for (int j = 1; j < N; j += 3) {
					tile = new RibTile(i - 1, j - 1, 3);
					tiling.add(tile);
				}				
			} 
		} else if (type == 4){
			// first we handle verticals
			for (int i = 1; i < M; i += 3) {
				for (int j = 1; j < N; j += 5) {
					tile = new RibTile(i - 1, j - 1, 0);
					tiling.add(tile);
					tile = new RibTile(i, j, 0);
					tiling.add(tile);
					tile = new RibTile(i + 1, j + 1, 0);
					tiling.add(tile);
				}				
			}
			// Now we add Russian G's
			for (int i = 1; i < M; i += 3) {
				for (int j = 1; j < N; j += 5) {
					tile = new RibTile(i - 1, j + 2, 2);
					tiling.add(tile);
				}				
			}
			// Now we add mirrored L's
			for (int i = 1; i < M; i += 3) {
				for (int j = 1; j < N; j += 5) {
					tile = new RibTile(i, j - 1, 3);
					tiling.add(tile);
				}				
			}
		} else {
			// first we handle horizontal tiles
						for (int i = 1; i < M; i += 5) {
							for (int j = 1; j < N; j += 3) {
								tile = new RibTile(i - 1, j - 1, 1);
								tiling.add(tile);
								tile = new RibTile(i, j, 1);
								tiling.add(tile);
								tile = new RibTile(i + 1, j + 1, 1);
								tiling.add(tile);
							}				
						}
						// Now we add Russian G's
						for (int i = 1; i < M; i += 5) {
							for (int j = 1; j < N; j += 3) {
								tile = new RibTile(i - 1, j, 2);
								tiling.add(tile);
							}				
						}
						// Now we add mirrored L's
						for (int i = 1; i < M; i += 5) {
							for (int j = 1; j < N; j += 3) {
								tile = new RibTile(i + 2, j - 1, 3);
								tiling.add(tile);
							}				
						}			
		}
	} //end of constructor.
	public void draw(Draw dr) { //displays the tiling in a specified window
		for (RibTile tile : tiling) {
			tile.drawFilled(dr);
		}
	}
	private void makeGraph() {
		G = new Graph((M + 1) * (N + 1));
		//adding border 
		for (int v = 0; v < M; v++){
			G.addEdge(v, v + 1);
			G.addEdge(v + (M + 1) * N, v + 1 + (M + 1) * N);
		}
		for (int x = 0; x < N; x++){
			G.addEdge(x * (M + 1), (x + 1) * (M + 1));
			G.addEdge((x + 1) * (M + 1) - 1,  (x + 2) * (M + 1) - 1);
		}
		//adding vertical edges.
		for (int v = 0; v < N * (M + 1); v++) {
						
		}
	}
    /*
    //Checks if a tile belongs to this tiling
     * 
     */
    public Boolean contains(RibTile tile) {
    	return tiling.contains(tile);
    }
    /*
     * Prints out all tiles in the tiling
     */
    public void print() {
    	for (RibTile tile : tiling) {
    		tile.print();
    		StdOut.println();
    	}
    }
    /*
     * Checks validity of the tiling
     */
    public Boolean isValid() {
    	for (RibTile tile : tiling) {
    		if (!tile.isValid()) return false;
    	}
    	// otherwise
    	return true;
    }
    /*
     *flips two tiles which are already known to be flippable. 
     */    
    public void flip(RibTile tile1, RibTile tile2) { 
    	if (tile1.type == 0 && tile2.type == 0) { // two vertical tiles
    		flip2verticalTiles(tile1, tile2);
    		} 
    	else if (tile1.type == 1 & tile2.type == 1) {  //two horizontal tiles
    		flip2horizontalTiles(tile1, tile2);
	        
       	} else if ((tile1.type == 2 && tile2.type == 3)
       			|| (tile1.type == 3 && tile2.type == 2)){ //We have Gamma and mirrored L. 
       		flipGammaL(tile1, tile2);       		
       	} else if ((tile1.type == 0 && tile2.type == 2)
       			|| (tile1.type == 2 && tile2.type == 0)){ // vertical and Gamma   
       		flipVerticalGamma(tile1, tile2);
       	} else if ((tile1.type == 0 && tile2.type == 3)
       			|| (tile1.type == 3 && tile2.type == 0)){ // vertical and mirrored L   
      	    flipVerticalL(tile1, tile2);	
       	} else if ((tile1.type == 1 && tile2.type == 3)
       			|| (tile1.type == 3 && tile2.type == 1)){ // horizontal and mirrored L   
       		flipHorizontalL(tile1, tile2);
       	} else if ((tile1.type == 1 && tile2.type == 2)
       			|| (tile1.type == 2 && tile2.type == 1)){ // horizontal and Gamma   
      	    flipHorizontalGamma(tile1, tile2);	
       	}
    	tiling.remove(tile1);
    	tiling.remove(tile2);
    }
    /*
     * performs a random flip, if possible. Needs a point and a random number between 0 and 1
     * returns 1 if a flip is found and 0 otherwise
     */
    public int randomFlip(double x, double y, double s) {
    	RibTile tile = findTile(x, y);
    	ArrayList<RibTile> otherTiles = findFlips(tile);
    	if (!otherTiles.isEmpty()) {
    		int K = otherTiles.size();//number of flippable tiles
    		int n = (int) (s * K); //we choose tile number n
    		RibTile otherTile = otherTiles.get(n);
    		flip(tile, otherTile);
    		return 1;
    	}
    	return 0;
    }
    /*
     *   checks if tiles tile1 and tile2 can be flipped.
     */
    private Boolean isFlip(RibTile tile1, RibTile tile2) {
    	RibTile otile1, otile2;
    	// first we order tiles by their xmin parameter.
    	if (tile1.xmin > tile2.xmin) {
    		otile1 = tile2;
    		otile2 = tile1;
    	} else {
    		otile1 = tile1;
    		otile2 = tile2;
    	}
    	if (otile1.type == 0 && otile2.type == 0) { //vertical tiles
    		if (otile1.ymin == otile2.ymin && otile1.xmin == otile2.xmin - 1) { //two vertical tiles are next to each
    			return true;
    		} else if (otile2.ymin == otile1.ymin + 1 && otile2.xmin == otile1.xmin + 1) {//two vertical tiles are next to 
    			                                                                          //each other and shifted by 1
    			return true;
    		} 
    	} else if (otile1.type == 1 && otile2.type == 1){ //horizontal tiles
    		if (otile1.xmin == otile2.xmin &&
    				(otile1.ymin == otile2.ymin - 1 || otile2.ymin == otile1.ymin - 1)) {
    			return true; 
    		} else if (otile1.xmin == otile2.xmin - 1 &&
    		          otile1.ymin == otile2.ymin - 1) {
    			return true;
    		}
    	} else  if (otile1.type == 2 && otile2.type == 3){ //Gamma and mirrored L tiles
    		if (otile1.xmin == otile2.xmin &&
    				(otile1.ymin == otile2.ymin + 1 || otile1.ymin == otile2.ymin - 2)) {
    			return true;
    		} else if (otile1.xmin == otile2.xmin - 1 && otile1.ymin == otile2.ymin) {
    			return true;
    		}    		
    	} else if (otile1.type == 3 && otile2.type == 2) { //Mirrored L and Gamma tiles
    		if (otile1.xmin == otile2.xmin && 
    				(otile1.ymin == otile2.ymin - 1 || otile1.ymin == otile2.ymin + 2)) {
    			return true;
    		} else if (otile1.xmin == otile2.xmin - 2 && otile1.ymin == otile2.ymin) {
    			return true;
    		}
    	} else if (otile1.type == 0 && otile2.type == 2) { //Vertical and Gamma 
    		if (otile1.xmin == otile2.xmin - 1 && otile1.ymin == otile2.ymin - 1) {
    			return true;
    		}
    	} else if (otile1.type == 3 && otile2.type == 0) { //Mirrored L and vertical
    		if (otile1.xmin == otile2.xmin - 2 && otile1.ymin == otile2.ymin) {
    			return true;
    		}
    	} else if (otile1.type == 1 && otile2.type == 2) { //Horizontal and Gamma
    		if (otile1.xmin == otile2.xmin && otile1.ymin == otile2.ymin + 2) {
    			return true;
    		}
    	} else if (otile1.type == 2 && otile2.type == 1) { //Gamma and Horizontal
    		if (otile1.xmin == otile2.xmin && otile1.ymin == otile2.ymin - 2) {
    			return true;
    		}
    	} else if (otile1.type == 1 && otile2.type == 3) { // Horizontal and mirrored L
    		if (otile1.xmin == otile2.xmin - 1 && otile1.ymin == otile2.ymin - 1) {
    			return true;
    		}
    	}  	
    	return false;
    }
    /*
     * Finds all possible flips that involve a given tile.
     */  
    private ArrayList<RibTile> findFlips(RibTile tile) {
    	ArrayList<RibTile> flips = new ArrayList<RibTile>();
    	RibTile other = null;
    	//
    	// Looking above the given tile:
    	//
    	switch (tile.type) { 
    		case 1: //horizontal tile
    			other = findTile(tile.xmin + 1.5, tile.ymin + 1.5);
    			break;
    		case 2: //Gamma tile
    			other = findTile(tile.xmin + 1.5, tile.ymin + 2.5);
    			break;
    		case 3: //mirrored L file
    			other = findTile(tile.xmin + 0.5, tile.ymin + 1.5);	
    	} 
    	if (other != null && isFlip(tile, other)){
    		  flips.add(other);
    	}
	    other = null;
    	//
    	// Looking below the given tile
    	//
    	switch (tile.type) { 
		case 1:
			other = findTile(tile.xmin + 1.5, tile.ymin - 0.5);
			break;
		case 2: 
			other = findTile(tile.xmin + 1.5, tile.ymin - 1.5);
			break;
		case 3: //mirrored L tile
			other = findTile(tile.xmin + 1.5, tile.ymin - 0.5);
	    } 
	    if (other != null && isFlip(tile, other)){
		  flips.add(other);
	    }
	    other = null;
	    //
	    // Looking to the left of the given tile
	    //
    	switch (tile.type) { 
		case 0: //vertical tile
			other = findTile(tile.xmin - 0.5, tile.ymin + 1.5);
			break;
		case 2: //Gamma tile
			other = findTile(tile.xmin - 0.5, tile.ymin + 0.5);
	    } 
	    if (other != null && isFlip(tile, other)){
		  flips.add(other);
	    }
	    other = null;
	    //
	    // Looking to the right of the given tile
	    //
    	switch (tile.type) { 
		case 0: //vertical tile
			other = findTile(tile.xmin + 1.5, tile.ymin + 1.5);
			break;
		case 3: //Mirrored L tile
			other = findTile(tile.xmin + 2.5, tile.ymin + 1.5);
	    } 
	    if (other != null && isFlip(tile, other)){
		  flips.add(other);
	    }    
	    // 	    
    	return flips;
    }
    
    /*
     * //finds a tile that contains point (x,y)
     */
	private RibTile findTile(double x, double y) { 
		RibTile tile;
		for (int i = 0; i < 3; i++ ) { //checking horizontal tiles
			tile = new RibTile((int) x - i, (int) y, 1);
			if (tiling.contains(tile)) return tile;
		}
		for (int i = 0; i < 3; i++ ) { //checking vertical tiles
			tile = new RibTile((int) x, (int) y - i, 0);
			if (tiling.contains(tile)) return tile;
		}
		//checking Gammas
	    tile = new RibTile((int) x, (int) y, 2);
			  if (tiling.contains(tile)) return tile;
	    tile = new RibTile((int) x, (int) y - 1, 2);
			  if (tiling.contains(tile)) return tile;	
	    tile = new RibTile((int) x - 1, (int) y - 1, 2);
			  if (tiling.contains(tile)) return tile;	
		//checking mirrored L's 
		tile = new RibTile((int) x, (int) y, 3);
			  if (tiling.contains(tile)) return tile;
		tile = new RibTile((int) x - 1, (int) y, 3);
			  if (tiling.contains(tile)) return tile;	
		tile = new RibTile((int) x - 1, (int) y - 1, 3);
			  if (tiling.contains(tile)) return tile;		  			  
		StdOut.println("have not found tile covering the point (" + x 
				+ ", " + y + ")." );
		/*for (RibTile t : tiling) {
			StdOut.println(t);
		}*/
		return null;
	}
    
  /*
   * flips two vertical tiles which are already known to be flippable.
   */
    private void flip2verticalTiles(RibTile tile1, RibTile tile2) {
    	double xmin, ymin, ymax;
	if (tile1.xmin < tile2.xmin) { // the first tile on the left
       	xmin = tile1.xmin;
        ymin = tile1.ymin;
        ymax = tile2.ymax;
	} else { //the second tile is on the left
       	xmin = tile2.xmin;
        ymin = tile2.ymin;
        ymax = tile1.ymax;
	}
    if (tile1.ymin == tile2.ymin) { 
        tiling.add(new RibTile(xmin, ymin, 3));
        tiling.add(new RibTile(xmin, ymin + 1, 2));
	} else {
        tiling.add(new RibTile(xmin, ymax - 2, 3));
        tiling.add(new RibTile(xmin, ymin, 2));
		}
    }
    /*
     * flips two horizontal tiles which are already known to be flippable.
     */
  private void flip2horizontalTiles(RibTile tile1, RibTile tile2) {
    	double xmin, ymin;
    if (tile1.ymin < tile2.ymin) { // the first tile on the bottom
       	xmin = tile1.xmin;
        ymin = tile1.ymin;
	} else { //the second tile is on the bottom
       	xmin = tile2.xmin;
        ymin = tile2.ymin;
	}
    if (tile1.xmin == tile2.xmin) { //the tiles exactly aligned
        tiling.add(new RibTile(xmin, ymin, 2));
        tiling.add(new RibTile(xmin + 1, ymin, 3));
	} else { // the tiles are shifted
        tiling.add(new RibTile(xmin, ymin, 3));
        tiling.add(new RibTile(xmin + 2, ymin, 2));
	} 
   }
  /*
   * flips two tiles which are already known to be flippable.
   * One of them is assumed to look like Gamma (or Russian G) and another as a mirrored L.
   */
  private void flipGammaL(RibTile tile1, RibTile tile2) {
  	double xmin, ymin;
  	if  (tile1.xmax == tile2.xmax && 
				(tile1.ymax - tile2.ymin == 3 || tile2.ymax - tile1.ymin == 3)) { // 3-by-2 vertical brick
		if (tile1.ymin < tile2.ymin) {
	       	ymin = tile1.ymin;
		} else {
	        ymin = tile2.ymin;
		}
        xmin = tile1.xmin;
        tiling.add(new RibTile(xmin, ymin, 0));
        tiling.add(new RibTile(xmin + 1, ymin, 0)); 
		} else if (tile1.ymax == tile2.ymax && 
				(tile1.xmax - tile2.xmin == 3 || tile2.xmax - tile1.xmin == 3)) { // 3-by-2 horizontal brick // 2-by-3 arrangement
		if (tile1.xmin < tile2.xmin) {
	       	xmin = tile1.xmin;
		} else {
	        xmin = tile2.xmin;
		}
        ymin = tile1.ymin;
        tiling.add(new RibTile(xmin, ymin, 1));
        tiling.add(new RibTile(xmin, ymin + 1, 1));
		} else if (tile1.xmax == tile2.xmax && 
				(tile1.ymax - tile2.ymin == 0 || tile2.ymax - tile1.ymin == 0)) { // convertible two 2 vertical tiles)
		if (tile1.ymin < tile2.ymin) {
	       	ymin = tile1.ymin;
		} else {
	        ymin = tile2.ymin;
		}
        xmin = tile1.xmin;
        tiling.add(new RibTile(xmin, ymin, 0));
        tiling.add(new RibTile(xmin + 1, ymin + 1, 0));
		} else if (tile1.ymax == tile2.ymax && 
				(tile1.xmax - tile2.xmin == 0 || tile2.xmax - tile1.xmin == 0)) { // convertible two 2 horizontal tiles)
			if (tile1.xmin < tile2.xmin) {
	       	xmin = tile1.xmin;
		} else {
	        xmin = tile2.xmin;
		}
        ymin = tile1.ymin;
        tiling.add(new RibTile(xmin, ymin, 1));
        tiling.add(new RibTile(xmin + 1, ymin + 1, 1));
		}
    }
  /*
   * flips two tiles which are already known to be flippable.
   * One of them is assumed to look like Gamma (or Russian G) and another is vertical.
   */
  private void flipVerticalGamma(RibTile tile1, RibTile tile2) {
	   double xmin, ymin;
		if (tile1.type == 0) {
   			xmin = tile1.xmin;
   			ymin = tile1.ymin;
   		} else {
   			xmin = tile2.xmin;
   			ymin = tile2.ymin;
   		}
        tiling.add(new RibTile(xmin, ymin, 2));
        tiling.add(new RibTile(xmin, ymin + 2, 1));
  }
  /*
   * flips two tiles which are already known to be flippable.
   * One of them is vertical and another is a mirrored L.
   */
  private void flipVerticalL(RibTile tile1, RibTile tile2) {
	   double xmin, ymin;
		if (tile1.type == 0) {
   			xmin = tile2.xmin;
   			ymin = tile2.ymin;
   		} else {
   			xmin = tile1.xmin;
   			ymin = tile1.ymin;
   		}
        tiling.add(new RibTile(xmin + 1, ymin + 1, 3));
        tiling.add(new RibTile(xmin, ymin, 1));
  } 
  /*
   * flips two tiles which are already known to be flippable.
   * One of them is horizontal and another is a mirrored L.
   */
  private void flipHorizontalL(RibTile tile1, RibTile tile2) {
	   double xmin, ymin;
		if (tile1.type == 1) {
   			xmin = tile1.xmin;
   			ymin = tile1.ymin;
   		} else {
   			xmin = tile2.xmin;
   			ymin = tile2.ymin;
   		}
        tiling.add(new RibTile(xmin, ymin, 3));
        tiling.add(new RibTile(xmin + 2, ymin, 0)); 
  }
  /*
   * flips two tiles which are already known to be flippable.
   * One of them is horizontal and another is a Gamma.
   */
  private void flipHorizontalGamma(RibTile tile1, RibTile tile2) {
	   double xmin, ymin;
		if (tile1.type == 1) {
   			xmin = tile2.xmin;
   			ymin = tile2.ymin;
   		} else {
   			xmin = tile1.xmin;
   			ymin = tile1.ymin;
   		}
        tiling.add(new RibTile(xmin, ymin, 0));
        tiling.add(new RibTile(xmin + 1, ymin + 1, 2));  
  }
}
