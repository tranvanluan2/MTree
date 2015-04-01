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

    public static String algorithm;

    public static void main(String[] args) {

        readArguments(args);

        MesureMemoryThread mesureThread = new MesureMemoryThread();
        mesureThread.start();
//         Stream s = Stream.getInstance("ForestCover");
        Stream s = Stream.getInstance("TAO");
//         Stream s = Stream.getInstance("randomData");
//        Stream s = Stream.getInstance("randomData1");
        // Stream s = Stream.getInstance(null);
        // Stream s = Stream.getInstance("tagData");
//        Stream s = Stream.getInstance("Trade");

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
            if (Constants.numberWindow != -1 && numberWindows > Constants.numberWindow) {
                break;
            }

            ArrayList<Data> incomingData;
            if (currentTime != 0) {
                incomingData = s.getIncomingData(currentTime, Constants.slide);
                currentTime = currentTime + Constants.slide;
            } else {
                incomingData = s.getIncomingData(currentTime, Constants.W);
                currentTime = currentTime + Constants.W;
            }

            long start = System.currentTimeMillis(); // requires java 1.5

            /**
             * do algorithm
             */
            switch (algorithm) {
                case "exactStorm":
                    ArrayList<Data> outliers = estorm.detectOutlier(incomingData, currentTime, Constants.W, Constants.slide);
                    outliers.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });
                    break;
                case "approximateStorm":
                    ArrayList<Data> outliers2 = apStorm.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    outliers2.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });
                    break;
                case "abstractC":
                    ArrayList<Data> outliers3 = abstractC.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    outliers3.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);

                    });
                    break;
                case "lue":
                    HashSet<DataLUEObject> outliers4 = lue.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    outliers4.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });
                    break;
                case "due":
                    HashSet<DataLUEObject> outliers5 = due.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    outliers5.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });
                    break;
                case "microCluster":
                    ArrayList<Data> outliers6 = micro.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    outliers6.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);

                    });
                    break;
                case "mesi":
                    HashSet<Data> outliers7 = mesi.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    outliers7.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });
                    break;
            }

            double elapsedTimeInSec = (System.currentTimeMillis() - start) * 1.0 / 1000;

            totalTime += elapsedTimeInSec;

            System.out.println("#window: " + numberWindows);
            System.out.println("Total #outliers: " + idOutliers.size());
            System.out.println("------------------------------------");

        }

        mesureThread.averageTime = totalTime * 1.0 / numberWindows;

        mesureThread.writeResult();
        mesureThread.stop();
        mesureThread.interrupt();
        /**
         * Write result to file
         */
//        Writer writer = null;
//         String filename = Constants.outputStorm+Constants.W+"_"+Constants.slide+".txt";

        // String filename = Constants.outputapStorm+Constants.W+"_"+Constants.slide+"__0.1"+".txt";
        // String filename = Constants.outputabstractC+Constants.W+"_"+Constants.slide+".txt";
//         String filename = Constants.outputLUE+Constants.W+"_"+Constants.slide+".txt";
        // String filename = Constants.outputMicro+Constants.W+"_"+Constants.slide+".txt";
//        String filename = Constants.outputDUE + Constants.W + "_" + Constants.slide + ".txt";
        // String filename = Constants.outputMESI+Constants.W+"_"+Constants.slide+".txt";
//        try {
//            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
//            Integer[] outliers = idOutliers.toArray(new Integer[0]);
//            for (Integer i : outliers) {
//
//                writer.write(i + "\n");
//            }
//        } catch (IOException ex) {
//            // report
//        } finally {
//            try {
//                writer.close();
//            } catch (Exception ex) {}
//        }
        // get size of tree
    }

    public static void readArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {

            //check if arg starts with --
            String arg = args[i];
            if (arg.indexOf("--") == 0) {
                switch (arg) {
                    case "--algorithm":
                        algorithm = args[i + 1];
                        break;
                    case "--R":
                        Constants.R = Double.valueOf(args[i + 1]);
                        break;
                    case "--W":
                        Constants.W = Integer.valueOf(args[i + 1]);
                        break;
                    case "--k":
                        Constants.k = Integer.valueOf(args[i + 1]);
                        break;
                    case "--datafile":
                        Constants.dataFile = args[i + 1];
                        break;
                    case "--output":
                        Constants.outputFile = args[i + 1];
                        break;
                    case "--numberWindow":
                        Constants.numberWindow = Integer.valueOf(args[i + 1]);
                        break;
                    case "--slide":
                        Constants.slide = Integer.valueOf(args[i + 1]);
                        break;

                }
            }
        }
    }

    public static void writeOutput(Double avergeTime, Double peakMemory) {

    }
}
