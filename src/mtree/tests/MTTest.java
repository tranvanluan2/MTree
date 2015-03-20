package mtree.tests;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;

import outlierdetection.AbstractC;
import outlierdetection.ApproxStorm;
import outlierdetection.Direct_Update_Event;
import outlierdetection.ExactStorm;
import outlierdetection.Lazy_Update_Event;
import outlierdetection.MESI;
import outlierdetection.MicroCluster;
import mtree.utils.Constants;
import outlierdetection.DataLUEObject;

public class MTTest {

    public static int currentTime = 0;

    public static boolean stop = false;

    public static HashSet<Integer> idOutliers = new HashSet<>();

    public static void main(String[] args) {
         Stream s = Stream.getInstance("ForestCover");
//         Stream s = Stream.getInstance("TAO");
//         Stream s = Stream.getInstance("randomData");
//        Stream s = Stream.getInstance("randomData0.1");
        // Stream s = Stream.getInstance(null);
        // Stream s = Stream.getInstance("tagData");

        ExactStorm estorm = new ExactStorm();
        ApproxStorm apStorm = new ApproxStorm(1);
        AbstractC abstractC = new AbstractC();
        Lazy_Update_Event lue = new Lazy_Update_Event();
        Direct_Update_Event due = new Direct_Update_Event();
        MicroCluster micro = new MicroCluster();
        MESI mesi = new MESI();
        int numberWindows = 0;
        double totalTime = 0;
        while (!stop && s.hasNext()) {

            numberWindows++;
            if(numberWindows == 4323)
                System.out.println();
            if (numberWindows < 10){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (numberWindows == 1000) try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            if (numberWindows > 10000) break;
            ArrayList<Data> incomingData;
            if (currentTime != 0) {
                incomingData = s.getIncomingData(currentTime, Constants.slide);
                currentTime = currentTime + Constants.slide;
            } else {
                incomingData = s.getIncomingData(currentTime, Constants.W);
                currentTime = currentTime + Constants.W;
            }

            long start = System.nanoTime(); // requires java 1.5

            /**
             * do algorithm
             */
//
//             ArrayList<Data> outliers = estorm.detectOutlier(incomingData, currentTime,Constants.W,
//             Constants.slide);
//             ArrayList<Data> outliers = apStorm.detectOutlier(incomingData, currentTime,Constants.W,
//             Constants.slide);
             ArrayList<Data> outliers = abstractC.detectOutlier(incomingData, currentTime,Constants.W,
             Constants.slide);
//            ArrayList<DataLUEObject> outliers = lue.detectOutlier(incomingData, currentTime, Constants.W,
//                Constants.slide);
//             ArrayList<Data> outliers = micro.detectOutlier(incomingData, currentTime,Constants.W,
//             Constants.slide);
//             ArrayList<DataLUEObject> outliers = due.detectOutlier(incomingData, currentTime,Constants.W,
//             Constants.slide);
//             HashSet<Data> outliers = mesi.detectOutlier(incomingData, currentTime,Constants.W,
//             Constants.slide);

            // Segment to monitor
            double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;

            totalTime += elapsedTimeInSec;
            outliers.stream().forEach((outlier) -> {
                idOutliers.add(outlier.arrivalTime);
             });//
//            outliers.stream().forEach((outlier) -> {
//                //
//                idOutliers.add(outlier.arrivalTime);
//             });

            System.out.println("#window: " + numberWindows);
            System.out.println("Total #outliers: " + idOutliers.size());
            System.out.println("------------------------------------");

        }
        // System.out.println("Peak memory: "+ Utils.peakUsedMemory);

        System.out.println("Avarage Time: " + totalTime * 1.0 / numberWindows);
        /**
         * Write result to file
         */
        Writer writer = null;
         String filename = Constants.outputStorm+Constants.W+"_"+Constants.slide+".txt";

        // String filename = Constants.outputapStorm+Constants.W+"_"+Constants.slide+"__0.1"+".txt";
        // String filename = Constants.outputabstractC+Constants.W+"_"+Constants.slide+".txt";
//         String filename = Constants.outputLUE+Constants.W+"_"+Constants.slide+".txt";
        // String filename = Constants.outputMicro+Constants.W+"_"+Constants.slide+".txt";
//        String filename = Constants.outputDUE + Constants.W + "_" + Constants.slide + ".txt";
        // String filename = Constants.outputMESI+Constants.W+"_"+Constants.slide+".txt";

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
            Integer[] outliers = idOutliers.toArray(new Integer[0]);
            for (Integer i : outliers) {

                writer.write(i + "\n");
            }
        } catch (IOException ex) {
            // report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {}
        }

        // get size of tree

    }

}
