import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

//import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;

/*
 * This class describes a tiling on an N-by-M rectangle
 */
public class RibTiling {
	public HashSet<RibTile> tiling;
	public int N; //height of the rectangle
	public int M; //width of the rectangle
	//public Height height; //this is the height function on this tiling (I do not use it anymore)
	private int n = 3;
	
	public ArrayList<ArrayList<RibTile>> levels2tiles;
	public SheffieldGraph G; //Associated Sheffield's graph.
	private RibTileVisualizer vz; //a reference to the visualizer that created this tiling.
	
	/*
	 * This Constructor reads the tiling from a stream input.
	 * The input file has N and M as the first two entries followed by the triples
	 * of (xmin, ymin, type) for all tiles in the tiling.
	 * I don't do any error checking here.
	 */
	public RibTiling(In in, RibTileVisualizer vz) {
		this.vz = vz;
		tiling = new HashSet<RibTile>();
		N = in.readInt();
		M = in.readInt();
		int numberTiles = N * M / 3;
		for (int i = 0; i < numberTiles; i++) {
			int xmin = in.readInt();
			int ymin = in.readInt();
			int type = in.readInt();
			tiling.add(new RibTile(xmin,ymin,type));
		}
		G = new SheffieldGraph(this);
		G.reduce();
	}
	
	
	/*
	 * This Constructor create tiling on an N-by-M board. There are several possible initializations,
	 * which are chosen according to type variable
	 */
	public RibTiling(int N, int M, int type, RibTileVisualizer vz) {
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
		this.vz = vz;
		tiling = new HashSet<RibTile>();
		if (type >= 0) {
			switch (type) {
			case 0:
				for (int i = 0; i < M; i++) {
					for (int j = 0; j < N; j += 3) {
						tile = new RibTile(i, j, 0);
						tiling.add(tile);
					}
				}
				break;
			case 1:
				for (int i = 0; i < N; i++) {
					for (int j = 0; j < M; j += 3) {
						tile = new RibTile(j, i, 1);
						tiling.add(tile);
					}
				}
				break;
			case 2:
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
				break;
			case 3:
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
				break;
			case 4:
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
				break;
			case 5:
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
				break;
		   default: 
			    StdOut.println("Unknown type: " + type);
				break;	
			}
		} else {
			StdOut.println("The type cannot be negative");
		}
			G = new SheffieldGraph(this);
			G.reduce();
    }



	/*
	 * Copy constructor. I am not using it.
	 */
	/*
	public RibTiling(RibTiling tlng) {
		this.N = tlng.N;
		this.M = tlng.M;
		//we are not going to copy height. It can be recomputed if needed.
		RibTile tile;
		tiling = new HashSet<RibTile>();
		for (RibTile t : tlng.tiling){
			tile = new RibTile(t);
			tiling.add(tile);
		}		
	}
	*/
	
	
	public void mix(int ITER) { //for simplicity I will write this function only for 3-tiles here (not for general n)
		int v1, v2 = 0;
		long c;
		RibTile t1;
		for (int count = 0; count < ITER; count++) {
			v1 = StdRandom.uniform(G.V); // a random tile. 
			t1 = G.labels.get(v1);
			t1.drawBorder(vz.window2, "red");
			c = G.level(v1);					
			//Now we need to find a comparable tile, which would be a neighbor of t1 
			ArrayList<Integer> comparables = new ArrayList<Integer>(); //First, we collect all tiles that 
			                                                            // are exchangeable with t1 together.
			for (long i = c - n + 1; i < c + n; i++) {
				if ( i >= 0 && i <= N + M - n - 1 && i != c ) {
					comparables.addAll(G.levels2vertices.get(i));
				}					
			}
            ArrayList<Integer> vicini = new ArrayList<Integer>(); //All tiles that shares an edge with t1. 
			
            for (int v : comparables) {
            	if (t1.isTouch(G.labels.get(v))) {
            		vicini.add(v);
            	}
            }
            
            if (vicini.size() == 0) { //for a border tile there is a possibility that there is no exchangeable
            	                      //neighbor around. Then we skip this tile.
            	StdOut.println("Exchange not possible for tile: " + G.labels.get(v1));
            	continue;
            } else {
            	StdOut.println("Neighbors: ");
            	for (int i = 0; i < vicini.size(); i++) {
            		v2 = vicini.get(i);
            		RibTile t = G.labels.get(v2);
            		StdOut.println(t);
            		t.drawSpecial(vz.window2);
            		if (isFlip(t1, t)) {
            			G.flip(v1, v2);
            			break;
            		}
            	}
            }
            //Not all of vicini tiles are flippable;
            //v2 = vicini.get(StdRandom.uniform(0, vicini.size()));
            //G.flip(v1, v2);
		}
        G.update(); 
        G.reduce();
	}

	
	
	
	
	
	
	
	public void distributeTiles() {
			// I want to distribute all tiles into bins according to their level and 
			// then sort the tiles in each bin.
		    levels2tiles = new ArrayList<ArrayList<RibTile>>(N + M - n);
			for (int i = 0; i < N + M - n; i++) {
				levels2tiles.add(new ArrayList<RibTile>());			
		    }
			for (RibTile tile : tiling) {
				long l = tile.level;
				//if (l % n == 0) {
				   levels2tiles.get((int) l).add(tile);	
				//}		
			}
			//Now we will sort the bins
			for (int i = 0; i < N + M - n; i++) {
				Collections.sort(levels2tiles.get(i));			
		    }
	}
	
	public void draw(Draw dr) { //displays the tiling in a specified window
		for (RibTile tile : tiling) {
			tile.drawFilled(dr);
		}
		drawLevels(dr);
		drawVerticals(dr);
	}
	private void drawLevels(Draw dr){
		double x0, y0, x1, y1;
		//I want to draw some lines that would connect the tiles of the same level. At least for 
		// level = 0 (mod n).
		distributeTiles();
		dr.setPenRadius(0.005);
		dr.setPenColor(Draw.RED);
		for (int i = 0; i < N + M - n; i = i + n) {
			ArrayList<RibTile> levelList = levels2tiles.get(i);	
			//StdOut.println("Level = " + i + "; number of tiles: " + levelList.size());
			for (int k = 0; k < levelList.size() - 1; k++) {
                x0 = levelList.get(k).xmin + 0.5;
                x1 = levelList.get(k + 1).xmin + 0.5;
                y0 = levelList.get(k).ymin + 0.4;
                y1 = levelList.get(k + 1).ymin + 0.4;
                dr.line(x0, y0, x1, y1);
			}
	    }
	}
	
	private void drawVerticals(Draw dr){
		double x0, y0, x1, y1;
		//I want to draw some lines that would indicate relation for tiles of the  
		// level 0 modulo n
		distributeTiles();
		dr.setPenRadius(0.005);
		dr.setPenColor(Draw.WHITE);
		for (int i = 0; i < N + M - 2 * n; i = i + n) {
			ArrayList<RibTile> levelList1 = levels2tiles.get(i);
			ArrayList<RibTile> levelList2 = levels2tiles.get(i + n);
			int s1 = levelList1.size();
			int s2 = levelList2.size();
			if (s1 < s2) {
			for (int k = 0; k < s1; k++) {
                x0 = levelList1.get(k).xmin + 0.5;
                x1 = levelList2.get(k + 1).xmin + 0.5;
                y0 = levelList1.get(k).ymin + 0.4;
                y1 = levelList2.get(k + 1).ymin + 0.4;
                dr.line(x0, y0, x1, y1);
			}
			} else if (s1 == s2) {
				for (int k = 0; k < s1; k++) {
	                x0 = levelList1.get(k).xmin + 0.5;
	                x1 = levelList2.get(k).xmin + 0.5;
	                y0 = levelList1.get(k).ymin + 0.5;
	                y1 = levelList2.get(k).ymin + 0.5;
	                dr.line(x0, y0, x1, y1);
				}
			} else {
				for (int k = 0; k < s2; k++) {
	                x0 = levelList1.get(k + 1).xmin + 0.5;
	                x1 = levelList2.get(k).xmin + 0.5;
	                y0 = levelList1.get(k + 1).ymin + 0.5;
	                y1 = levelList2.get(k).ymin + 0.5;
	                dr.line(x0, y0, x1, y1);
				}
			}
	    }
	}

	/*I DON'T USE THE FUNCTION BELOW
	 * Calculates height vector function on the rectangle that corresponds to this 
	 * tiling.
	 */
	/*
	public void calcHeight() {
		height.calcHeight(this);
	}
	*/

    /**
     * Prints out all tiles in the tiling
     */
    public void print() {
    	for (RibTile tile : tiling) {
    		tile.print();
    		StdOut.println();
    	}
    }
    
    /**
     * Save the tiling to a file, so that it can be read in by one of the constructors.
     * 
     * @param f is the name of the file
     */
    public void save(String f) {
    	Out out;
        out = new Out(f);
        out.println(N + " " + M);
        for (RibTile t : tiling) {
        	out.println((int) t.xmin + " " + (int) t.ymin + " " + t.type);
        }
        out.close();
    }
    
    
    
    
    
    /**
     * Checks validity of the tiling (Some necessary but not sufficient property, so
     * the tiling may still be invalid even if this functions outputs true.)
     */
    public Boolean isValid() {
    	for (RibTile tile : tiling) {
    		if (!tile.isValid()) return false;
    	}
    	// otherwise
    	return true;
    }
    
    /*
    //Checks if a tile belongs to this tiling
     * 
     */
    public Boolean contains(RibTile tile) {
    	return tiling.contains(tile);
    }
    
    //flip a pair of ordered tiles. So it is assumed that the pair is flippable.
    //Besides, we are going to use typeCode instead of type.
    public Pair<RibTile,RibTile> flipGeneric(RibTile tile1, RibTile tile2) {
    	RibTile otile1, otile2, ntile1, ntile2;
    	Pair<RibTile, RibTile> pair;
    	//First, we order the tiles.
    	boolean flag = true;
    	if (tile1.compareWeak(tile2) == 1) { //tile2 was on the left of tile1 
    		otile1 = tile2;
    		otile2 = tile1;
    		flag = false; //we changed the order of tiles.
    	} else if (tile1.compareWeak(tile2) == -1) { //tile1 is on the left of tile2
    		otile1 = tile1;
    		otile2 = tile2;
    	} else {
    		StdOut.println("tiles are not comparable.");
    		return null; //tiles are not comparable.
    	}
    	//after the swap, otile2 will be on the left of tile otile1
    	//calculate offset. 
    	int a = (int) (otile1.xmin - otile2.xmin);
    	int b = (int) (otile1.ymin - otile2.ymin);
    	
    	if (a >= 0 && b > 0) {
    		String tc1 = Integer.toBinaryString(otile1.typeCode |= (1 << (n - a - b - 1))); // set the bit
    		String tc2 = Integer.toBinaryString(otile2.typeCode |= (1 << (a + b - 1))); // set the bit
    		//StdOut.println(tc1);
    		ntile1 = new RibTile(otile1.xmin + 1, otile1.ymin - 1, tc1);
    		ntile2 = new RibTile(otile2.xmin, otile2.ymin, tc2);
    	} else {
    		a = - a;
    		b = - b;
    		String tc1 = Integer.toBinaryString(otile1.typeCode &= ~(1 << (a + b - 1))); // unset the bit
    		String tc2 = Integer.toBinaryString(otile2.typeCode &= ~(1 << (n - 1 - a - b))); // unset the bit
    		ntile1 = new RibTile(otile1.xmin, otile1.ymin, tc1);
    		ntile2 = new RibTile(otile2.xmin - 1, otile2.ymin + 1, tc2);
    	}
        tiling.add(ntile1);
        tiling.add(ntile2);
        tiling.remove(otile1);
        tiling.remove(otile2);
        
        if (flag) {
        	pair = new Pair<RibTile, RibTile>(ntile1, ntile2);
        } else {
        	pair = new Pair<RibTile, RibTile>(ntile2, ntile1);
        }
        
        return pair;
    }
    
    
    
  
    /**
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
    		flipGeneric(tile, otherTile);
    		return 1;
    	}
    	return 0;
    }
    
    /**
     * checks if tiles t1 and t2 touch 
     */
    
    /**
     *   checks if tiles tile1 and tile2 can be flipped.
     */
    public Boolean isFlip(RibTile tile1, RibTile tile2) {
    	RibTile otile1, otile2;
    	//First, we order the tiles.
    	if (tile1.compareWeak(tile2) == 1) {
    		otile1 = tile2;
    		otile2 = tile1;
    	} else if (tile1.compareWeak(tile2) == -1) {
    		otile1 = tile1;
    		otile2 = tile2;
    	} else {
    		return false; //tiles are not comparable.
    	}
    	//calculate offset. 
    	int a = (int) (otile1.xmin - otile2.xmin);
    	int b = (int) (otile1.ymin - otile2.ymin);
    	
    	if (a >= 0 && b > 0) {
    		if (a + b > n - 1) {
    			//StdOut.println("Offset is too large.");
    			return false;
    		} else {
    			int checksum = 0;
    			for (int i = 0; i < a + b - 1; i++) {
    				checksum =+ ((otile2.typeCode >> i) & 1);
    			}
    			if (checksum != b - 1 || (((otile2.typeCode >> a + b - 1) & 1) != 0) 
    					|| (((otile1.typeCode >> (n - a - b - 1)) & 1) != 0)) {
    				return false;
    			} else if (a + b + 1 < n) {
    				for (int k = 0; k < n - (a + b + 1); k++) {
    					if (((otile2.typeCode >> a + b + k) & 1) != ((otile1.typeCode >> k) & 1)) {
    						//StdOut.println("Sequences are not aligned.");
    						return false;
    					}
    				}  				
    			}
    		}
    		return true;
    	} else if (a < 0 && b <= 0) {
    		a = - a; 
    		b = - b;
    		if (a + b > n - 1) {
    			//StdOut.println("Offset is too large.");
    			return false;
    		} else {
    			int checksum = 0;
    			for (int i = 0; i < a + b - 1; i++) {
    				checksum =+ ((otile1.typeCode >> i) & 1);
    			}
    			if (checksum != b || (((otile1.typeCode >> a + b - 1) & 1) != 1) 
    					|| (((otile2.typeCode >> (n - (a + b + 1)) & 1) != 1))) {
    				return false;
    			} else if (a + b + 1 < n) {
    				for (int k = 0; k < n - (a + b + 1); k++) {
    					if (((otile1.typeCode >> a + b + k) & 1) != ((otile2.typeCode >> k) & 1)) {
    						return false;
    					}
    				}  				
    			}
    		}
    		return true;
    	}
    	return false;	
    }
    /**
     * Finds all possible flips that involve a given tile.
     */  
    public ArrayList<RibTile> findFlips(RibTile tile) {
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
		case 1: //horizontal
			other = findTile(tile.xmin + 1.5, tile.ymin - 0.5);
			break;
		case 2: //Gamma
			other = findTile(tile.xmin + 1.5, tile.ymin + 0.5);
			//StdOut.println("Point has x = " + (tile.xmin + 1.5) + " and y = " + ( tile.ymin - 1.5) );
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
	public RibTile findTile(double x, double y) { 
		RibTile tile;
		double x0 = Math.floor(x);
		double y0 = Math.floor(y);
		//StdOut.println("x = " + x + "and y = " + y);
		for (int i = 0; i < 3; i++ ) { //checking horizontal tiles
			tile = new RibTile(x0 - i, y0, 1);
			if (tiling.contains(tile)) return tile;
		}
		for (int i = 0; i < 3; i++ ) { //checking vertical tiles
			tile = new RibTile(x0, y0 - i, 0);
			if (tiling.contains(tile)) return tile;
		}
		//checking Gammas
	    tile = new RibTile(x0, y0, 2);
			  if (tiling.contains(tile)) return tile;
	    tile = new RibTile(x0, y0 - 1, 2);
			  if (tiling.contains(tile)) return tile;	
	    tile = new RibTile(x0 - 1, y0 - 1, 2);
			  if (tiling.contains(tile)) return tile;	
			  
		//checking mirrored L's 
		tile = new RibTile(x0, y0, 3);
			  if (tiling.contains(tile)) return tile;
		tile = new RibTile(x0 - 1, y0, 3);
			  if (tiling.contains(tile)) return tile;	
		tile = new RibTile(x0 - 1, y0 - 1, 3);
			  if (tiling.contains(tile)) return tile;		  			  
		return null;
	}
	/**
	 * finds the tiles between two comparable files, that is, all tiles x such that
	 * t1 <--- x <---- t2.
	 * 
	 * 
	 * @param t1 a ribbon tile
	 * @param t2 a ribbon tile comparable to t1
	 * @return tiles which are tiles between t1 and t2
	 */
	public ArrayList<RibTile> findBetween(RibTile t1, RibTile t2) {
		ArrayList<RibTile> tiles = new ArrayList<RibTile>();
		long a, b;
		if (t1.level < t2.level) {
			a = t1.level;
			b = t2.level;
		} else {
			a = t2.level;
			b = t1.level;
		}
		for (RibTile x : tiling) {
			if (x.level >= b - n && x.level <= a + n){ //tile x is comparable with both t1 and t2;
				if (((x.compareWeak(t1) == -1) && (x.compareWeak(t2) == 1)) 
						|| ((x.compareWeak(t1) == 1) && (x.compareWeak(t2) == -1))) { 
					//x is between t1 and t2
					tiles.add(x);					
				}
			}
		}
		return tiles;
	}
	
	/**
	 * Count number of tiles of order type x 
	 */
	public int countTiles(int x) {
		int nT = 0;
		for (RibTile t : tiling) {
			if (t.typeCode == x) nT++; 
		}
		return nT;
	}
}


