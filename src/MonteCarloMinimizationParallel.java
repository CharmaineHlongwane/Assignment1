import java.util.concurrent.RecursiveTask;
import java.util.*;


public class MonteCarloMinimizationParallel extends RecursiveTask<Integer>{

    static final int SEQUENTIAL_CUTOFF = 1;
    int column, row;
    double xmin,xmax, ymin, ymax;
    int searches_density;
    static int num_searches;
    int min = Integer.MAX_VALUE;// OUR GLOBAL MIN
    INT local_min = Integer.MAX_VALUE;
    SearchParallel[] searches;// CLASS ARRAY

    MonteCarloMinimizationParallel(SearchParallel [] search){// takes the arguments for proccessing

    }

    //processes the searches equeal to sqc if not split and compute the other half
    protected Integer compute(){

        if( num_searches < SEQUENTIAL_CUTOFF){
            for(int i = 0; i< num_searches; i++){// attempts to find the local min
                local_min = searches[i].findValleys();// requires array in main so pass to constructor
                
            }
            return 1;
        }
        
        else{// complete arguments 
            MonteCarloMinimizationParallel right = new MonteCarloMinimizationParallel(searches);// FIX THESE TWO "SEARCHES"
            MonteCarloMinimizationParallel left = new MonteCarloMinimizationParallel(searches);
            left.fork();
            right.compute();
            left.join();
     }
    }
    


    /**
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
            num_searches = (int)(column*row*searches_density);
            SearchParallel[] search = new SearchParallel[num_searches];
            for(int i = 0; i<num_searches, i++){
                searches[i] = new SearchParallel(i+1, rand.nextInt(row), rand.nextInt(column), terrain);// need to pass this to the parallel version
            }

            MonteCarloMinimizationParallel parallelSearch = new MonteCarloMinimizationParallel();// FILL IT WITH ARGUMENTS!!
            ForkJoinPool pool = new ForkJoinPool();// creating pool of worker threads
            pool.invoke(parallelSearch);


        }

        

    }

    private static void tick(){
        static long startTime = System.currentTimeMillis();

    }
    private static void tock(){
        static long endTime = System.currentTimeMillis();
    }

    }
}
