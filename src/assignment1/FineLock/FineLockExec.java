package assignment1.FineLock;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronnygeo on 9/24/16.
 */
public class FineLockExec {

    public static void main(String[] args) throws Exception {
        FineLockStationData sd = new FineLockStationData();
        ArrayList<FineLockThread> threads = new ArrayList<>();
        List lines;
        boolean delay = false;
        if (args.length > 1 && args[1].equals("--delay")) {
            delay = true;
        }

        long startTime;
        try {
            //Getting list of lines using FileLoader class
            lines = FileLoader.load(args[0]);
            int noOfLines = lines.size();
            //Getting the number of processors to determine the thread count
            int threadCount = Runtime.getRuntime().availableProcessors();
            int startThreadCount = Thread.activeCount();
            //Creating the indices array for splitting input
            int[] indices = new int[threadCount*2];
            indices[0] = 0;
            //Creating the index using the number of threads
            for (int i=1, j = 0; i < threadCount*2; i++) {
                if (i % 2 != 0) {
                    j++;
                }
                indices[i] = noOfLines/4 * j + 1;
            }
            indices[threadCount*2 - 1] = noOfLines;


            //Variable to store the program start time
            startTime = System.currentTimeMillis();

            //Creating threads for input split
            for (int tc = 0; tc < threadCount * 2; tc+= 2) {
                threads.add(new FineLockThread("Fine Lock Thread", sd, lines.subList(indices[tc], indices[tc + 1]), delay));
            }

            for (Thread t: threads) {
                t.start();
            }

            //Waiting for all the threads to complete
            while (Thread.activeCount() > startThreadCount) {
                for (Thread t: threads) {
                    t.join();
                }
            }

            System.out.println("Run Time with fine locks: " + (System.currentTimeMillis() - startTime) + "ms");
            System.out.println("Size of data: " + sd.getSize() + "\n");
            sd.printData();


        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }
}
