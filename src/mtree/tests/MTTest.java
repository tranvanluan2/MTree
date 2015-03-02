package mtree.tests;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import outlierdetection.AbstractC;
import outlierdetection.ApproxStorm;
import outlierdetection.ExactStorm;
import outlierdetection.Lazy_Update_Event;
import mtree.ComposedSplitFunction;
import mtree.DistanceFunction;
import mtree.DistanceFunctions;
import mtree.MTree;
import mtree.PartitionFunctions;
import mtree.PromotionFunction;
import mtree.tests.Data;
import mtree.utils.Constants;
import mtree.utils.Pair;
import mtree.utils.Utils;





public class MTTest {

    

    public static int currentTime = 0;
    
    public static boolean stop = false;
    
    public static HashSet<Integer> idOutliers = new HashSet<>();
    public static void main(String[] args){
//        Stream s = Stream.getInstance("ForestCover");
//        Stream s = Stream.getInstance("TAO");
//        Stream s = Stream.getInstance("randomData");
//        Stream s = Stream.getInstance(null);
        Stream s = Stream.getInstance("tagData");

        ExactStorm estorm = new ExactStorm();
        ApproxStorm apStorm = new ApproxStorm(0.1);
        AbstractC abstractC = new AbstractC();
        Lazy_Update_Event lue = new Lazy_Update_Event();
        int numberWindows = 0;
        double totalTime = 0;
        while(!stop && s.hasNext()){
            
            numberWindows++;
            ArrayList<Data> incomingData ;
            if(currentTime != 0)
            {
                incomingData = s.getIncomingData(currentTime, Constants.slide);
                currentTime = currentTime + Constants.slide;
            }
            else 
            {
                incomingData = s.getIncomingData(currentTime, Constants.W);
                currentTime = currentTime + Constants.W;
            }
            
            
           
            
           
            long start = System.nanoTime(); // requires java 1.5
          
            
            /**
             * do algorithm
             */
            
//            ArrayList<Data> outliers = estorm.detectOutlier(incomingData, currentTime,Constants.W, Constants.slide);
//            ArrayList<Data> outliers = apStorm.detectOutlier(incomingData, currentTime,Constants.W, Constants.slide);
            ArrayList<Data> outliers = abstractC.detectOutlier(incomingData, currentTime,Constants.W, Constants.slide);
//            ArrayList<Data> outliers = lue.detectOutlier(incomingData, currentTime,Constants.W, Constants.slide);
            
         // Segment to monitor
            double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;   
            
            totalTime +=elapsedTimeInSec;
            for(Data outlier: outliers){
                
                idOutliers.add(outlier.arrivalTime);
            }
            
            System.out.println("Total #outliers: "+idOutliers.size());
            System.out.println("------------------------------------");
            
            
        }
        
        System.out.println("Avarage Time: "+ totalTime*1.0/numberWindows);
        /**
         * Write result to file
         */
        Writer writer = null;
//        String filename = Constants.outputStorm+Constants.W+"_"+Constants.slide+".txt";

//        String filename = Constants.outputapStorm+Constants.W+"_"+Constants.slide+"__0.1"+".txt";
        String filename = Constants.outputabstractC+Constants.W+"_"+Constants.slide+".txt";
//        String filename = Constants.outputLUE+Constants.W+"_"+Constants.slide+".txt";
        
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream(filename), "utf-8"));
            for(Integer i: idOutliers){

                writer.write(i+"\n");
            }
        } catch (IOException ex) {
          // report
        } finally {
           try {writer.close();} catch (Exception ex) {}
        }
        
        //get size of tree 
        
    }
    
    
    
    
}
