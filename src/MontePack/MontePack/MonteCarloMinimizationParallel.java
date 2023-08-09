package MontePack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.*;
// Charmaie Hlongwane HLNTHA025

public class MonteCarloMinimizationParallel extends RecursiveTask<Integer>{

    static final int SEQUENTIAL_CUTOFF = 1;
    int column, row;
    double xmin,xmax, ymin, ymax;
    int searches_density;
    
    int min = Integer.MAX_VALUE;// OUR GLOBAL MIN
    int local_min = Integer.MAX_VALUE;
    static SearchParallel[] searches;// CLASS ARRAY
    static Random rand = new Random();// random number generator
    int high, low;
    
    /**Takes the following arguments
     * @param search the array of search object, FJ divides it until size< sequential cutoff.
     * @param lw start of the array
     * @param hgh the num of total searches
     */
    MonteCarloMinimizationParallel(SearchParallel [] search, int lw, int hgh){// takes the arguments for proccessing
        low = lw;
        high = hgh;
        searches = search;
    }
    void setFinder(int boss){// delete this when you get a chance, you have it in the SP.java
        finder = boss;
    }
    static int getFinder(){
        return finder;
    }
    //processes the searches equal to sqc if not split and compute the other half
    static int finder = -1;
    int global_min = 0;
    ArrayList<Integer> found = new ArrayList<Integer>();
    protected Integer compute(){
        
        //each thread returns its own min find a way to compare the mins between the two.
        if( high - low < SEQUENTIAL_CUTOFF){// high - low
            for(int i = 0; i< searches.length; i++){// attempts to find the local min
                local_min = searches[i].find_valleys();// requires array in main so pass to constructor
                if((!searches[i].isStopped())&& local_min<min){
                    min = local_min;// setting the lowest min found to our global min
                    finder = i;
                    //found.add(min);// created finder tracker
                   // found.add(finder);
                    return min;
                }
            }
            
        }
        
        else{//fork-join 
            MonteCarloMinimizationParallel right = new MonteCarloMinimizationParallel(searches, low,(high+low)/2);// FIX THESE TWO "SEARCHES"
            MonteCarloMinimizationParallel left = new MonteCarloMinimizationParallel(searches, (low+high)/2,high);
            left.fork();
            int right_min = right.compute();// returns min for left and right
            int left_min = left.join();
            global_min = Math.min(right_min, left_min);// gives us our global min
            // how would you find the finder, think about it 
            if(left_min < right_min){
                global_min = left_min;

            }
            else{
                global_min = right_min;
            }
             
            //for(int find : found ){ // setting the finder to the min index
               // if(find == global_min){
                   // int indexFind = found.indexOf(find);// will make code slower, try to find an efficient way to do this.
                   // setFinder(found.get(indexFind + 1));

                //}
           // }
            
     }
        return global_min;
    }
    


    /**
     * The main class takes arguments from the user, creates a terain area, then creates an array of search object
     * Creates a MonteCarloMinP object that takes the array and then passes the object to a FJPool object which returns the global minimum
     * Then prints the output.
     * @param args
     */
    public static void main(String[] args){

        int row = Integer.parseInt( args[0]);
        int column = Integer.parseInt(args[1]);
        double xmin = Double.parseDouble(args[2]);
        double xmax = Double.parseDouble(args[3]);
        double ymin = Double.parseDouble(args[4]);
        double ymax = Double.parseDouble(args[5]);
        double searches_density = Double.parseDouble(args[6]);

        if(args.length!= 7){
            System.out.println("Incorrect number of arguments provided.");        
        }

        else{
            TerrainArea  terrain = new TerrainArea(row, column, xmin, xmax, ymin, ymax);
            int num_searches = (int)(column*row*searches_density);
            SearchParallel[] search = new SearchParallel[num_searches];
            for(int i = 0; i<num_searches; i++){
                
                searches[i] = new SearchParallel(i+1, rand.nextInt(row), rand.nextInt(column), terrain);// need to pass this to the parallel version
            }

            

            int lower = 0;// initializer for the searches array
            tick();// start timer
            MonteCarloMinimizationParallel parallelSearch = new MonteCarloMinimizationParallel(search, lower, num_searches);// takes array of type PS with start and end
            ForkJoinPool pool = new ForkJoinPool();// creating pool of worker threads
            int globby = pool.invoke(parallelSearch);// get our global min into main
            tock();
            // after it returns the min the print following arguments
            System.out.printf("Run parameters\n");
		    System.out.printf("\t Rows: %d, Columns: %d\n", row, column);
		    System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax );
		    System.out.printf("\t Search density: %f (%d searches)\n", searches_density,num_searches );

		/*  Total computation time */
		    System.out.printf("Time: %d ms\n",endTime - startTime );
		    int tmp=terrain.getGrid_points_visited();
		    System.out.printf("Grid points visited: %d  (%2.0f%s)\n",tmp,(tmp/(row*column*1.0))*100.0, "%");
		    tmp=terrain.getGrid_points_evaluated();
		    System.out.printf("Grid points evaluated: %d  (%2.0f%s)\n",tmp,(tmp/(row*column*1.0))*100.0, "%");
	
		    /* Results*/
		    System.out.printf("Global minimum: %d at x=%.1f y=%.1f\n\n", globby, terrain.getXcoord(searches[1].getPos_row()), terrain.getYcoord(searches[1].getPos_col()) );// replace 1 with finder
        }

        

    }

    // START AND END TIME
    static long startTime = 0;
    static long endTime = 0;
    private static void tick(){
         startTime = System.currentTimeMillis();
    }
    private static void tock(){
         endTime = System.currentTimeMillis();
    }

}

