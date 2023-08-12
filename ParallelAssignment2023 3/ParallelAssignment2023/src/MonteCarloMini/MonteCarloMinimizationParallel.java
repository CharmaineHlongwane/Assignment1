package MonteCarloMini;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.*;
// Charmaie Hlongwane HLNTHA025
public class MonteCarloMinimizationParallel{

     // START AND END TIME
    static long startTime = 0;
    static long endTime = 0;
    private static void tick(){
         startTime = System.currentTimeMillis();
    }
    private static void tock(){
         endTime = System.currentTimeMillis();
    }
     /**
     * The main class takes arguments from the user, creates a terain area, then creates an array of search object
     * Creates a MonteCarloMinP object that takes the array and then passes the object to a FJPool object which returns the global minimum
     * Then prints the output.
     * @param args
     */
    public static void main(String[] args){
        final boolean DEBUG = false;//used to debug code
         int rows, columns; //grid size
         double xmin, xmax, ymin, ymax; //x and y terrain limits
         TerrainArea terrain;  //object to store the heights and grid points visited by searches
         double searches_density;	// Density - number of Monte Carlo  searches per grid position - usually less than 1!
         Random rand = new Random();  //the random number generator
         
         if (args.length!=7) {  
             System.out.println("Incorrect number of command line arguments provided.");   	
             System.exit(0);
         }
         // Read argument values 
         rows =Integer.parseInt( args[0] );
         columns = Integer.parseInt( args[1] );
         xmin = Double.parseDouble(args[2] );
         xmax = Double.parseDouble(args[3] );
         ymin = Double.parseDouble(args[4] );
         ymax = Double.parseDouble(args[5] );
         searches_density = Double.parseDouble(args[6] );
   
         if(DEBUG) {
             // Print arguments 
             System.out.printf("Arguments, Rows: %d, Columns: %d\n", rows, columns);
             System.out.printf("Arguments, x_range: ( %f, %f ), y_range( %f, %f )\n", xmin, xmax, ymin, ymax );
             System.out.printf("Arguments, searches_density: %f\n", searches_density );
             System.out.printf("\n");
         }
         
         // Initialize 
         terrain = new TerrainArea(rows, columns, xmin,xmax,ymin,ymax);
         int num_searches = (int)( rows * columns * searches_density );
         ArrayList<SearchParallel>  searches= new ArrayList<SearchParallel>();
         
          if(DEBUG) {
             // Print initial values 
             System.out.println(searches==null);
             System.out.printf("Number searches: %d\n", num_searches);
             //terrain.print_heights();
         }// setting object to index
         for (int i=0;i<num_searches;i++) 
             searches.add(i, new SearchParallel(i+1, rand.nextInt(rows),rand.nextInt(columns),terrain));

           if(DEBUG) {
             // Print initial values 
             //System.out.println(searches==null);
             System.out.printf("Number searches: %d\n", num_searches);
             terrain.print_heights();
         }

            int lower = 0;// initializer for the searches array
            tick();// start timer
            MonteCarloParallel parallelSearch = new MonteCarloParallel(searches, lower, num_searches);// takes array of type PS with start and end
            ForkJoinPool pool = new ForkJoinPool();// creating pool of worker threads
            ourGlobal globby = pool.invoke(parallelSearch);// get our global min into main
            tock();
           
            if(DEBUG) {
                //print final state 
                terrain.print_heights();
                terrain.print_visited();
            }
             // after it returns the min the print following arguments
            System.out.printf("Run parameters\n");
		    System.out.printf("\t Rows: %d, Columns: %d\n", rows, columns);
		    System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax );
		    System.out.printf("\t Search density: %f (%d searches)\n", searches_density,num_searches );

		/*  Total computation time */
		    System.out.printf("Time: %d ms\n",endTime - startTime );
		    int tmp=terrain.getGrid_points_visited();
		    System.out.printf("Grid points visited: %d  (%2.0f%s)\n",tmp,(tmp/(rows*columns*1.0))*100.0, "%");
		    tmp=terrain.getGrid_points_evaluated();
		    System.out.printf("Grid points evaluated: %d  (%2.0f%s)\n",tmp,(tmp/(rows*columns*1.0))*100.0, "%");
	
		    /* Results*/
		   System.out.printf("Global minimum: %d at x=%.1f y=%.1f\n\n", globby.min_global, globby.x_global, globby.y_global );// replace 1 with finder
        }

        

    

   


    static class ourGlobal{// stores our min value with its x and y
        int x_global;
        int y_global;
        int min_global;

        ourGlobal(int x,int y, int  min){// constuctor
            x_global = x;
            y_global = y;
            min_global = min;

        }
    }
static class MonteCarloParallel extends RecursiveTask<ourGlobal>{

    static final int SEQUENTIAL_CUTOFF = 100;
    
    int num_search = 0;
    int min = Integer.MAX_VALUE;// OUR GLOBAL MIN
    int local_min = Integer.MAX_VALUE;
    ArrayList<SearchParallel> searcher;// CLASS ARRAY
    Random rand = new Random();// random number generator
    int high, low;// arraylist start and end

    
    /**Takes the following arguments
     * @param search the array of search object, FJ divides it until size< sequential cutoff.
     * @param lw start of the array
     * @param hgh the num of total searches
     */
    MonteCarloParallel(ArrayList<SearchParallel> search, int lw, int hgh ){// takes the arguments for proccessing
        low = lw;
        high = hgh;
        searcher = search;
        
    }
    
    //processes the searches equal to sqc if not split and compute the other half
    //static int finder = -1;
    ourGlobal boss;
    ArrayList<Integer> found = new ArrayList<Integer>();
    protected ourGlobal compute(){
        
        //each thread returns its own min find a way to compare the mins between the two.
        if( (high - low) <= SEQUENTIAL_CUTOFF){// high - low
            for(int i = 0; i< searcher.size(); i++){// attempts to find the local min
                local_min = searcher.get(i).find_valleys();// requires array in main so pass to constructor
                if((!searcher.get(i).isStopped())&& local_min<min){
                    min = local_min;// setting the lowest min found to our global min
                    int x = searcher.get(i).getPos_row();
                    int y = searcher.get(i).getPos_col();
                    //found.add(min);// created finder tracker
                   // found.add(finder);

                    ourGlobal minimum = new ourGlobal(x,y,min);
                    return minimum; 
                }
            }
            
        }
        
        else{//fork-join 
            MonteCarloParallel right = new MonteCarloParallel(searcher, low,(high+low)/2);// creation of class objects for recursion
            MonteCarloParallel left = new MonteCarloParallel(searcher, (low+high)/2,high);
            left.fork();
            ourGlobal right_min = right.compute();// returns min for left and right
            ourGlobal left_min = left.join();
            
            if (right_min.min_global< left_min.min_global ){
                boss = right_min;//boss == right minimum
            }
            else{
                boss = left_min;// our boss min to left
            }
           
     }
        return boss; 
    }
    


   
}
}

