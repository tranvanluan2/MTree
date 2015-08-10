package mtree.tests;

import be.tarsos.lsh.Vector;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import mtree.utils.Utils;
import outlierdetection.DataLUEObject;
import outlierdetection.MESIWithHash;
import outlierdetection.MicroCluster_New;

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
        MicroCluster_New mcnew = new MicroCluster_New();
        MESI mesi = new MESI();
        MESIWithHash mesiWithHash = new MESIWithHash();
        int numberWindows = 0;
        double totalTime = 0;
        while (!stop ) {

            if (Constants.numberWindow != -1 && numberWindows > Constants.numberWindow) {
                break;
            }
            numberWindows++;
            

            ArrayList<Data> incomingData;
            if (currentTime != 0) {
                incomingData = s.getIncomingData(currentTime, Constants.slide, Constants.dataFile);
                currentTime = currentTime + Constants.slide;
            } else {
                incomingData = s.getIncomingData(currentTime, Constants.W, Constants.dataFile);
                currentTime = currentTime + Constants.W;
            }

            long start = Utils.getCPUTime(); // requires java 1.5

            /**
             * do algorithm
             */
            switch (algorithm) {
                case "exactStorm":
                    ArrayList<Data> outliers = estorm.detectOutlier(incomingData, currentTime, Constants.W, Constants.slide);
                    double elapsedTimeInSec = (Utils.getCPUTime() - start) * 1.0 / 1000000000;

                    totalTime += elapsedTimeInSec;
                    outliers.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });

                    break;
                case "approximateStorm":
                    ArrayList<Data> outliers2 = apStorm.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    elapsedTimeInSec = (Utils.getCPUTime() - start) * 1.0 / 1000000000;

                    totalTime += elapsedTimeInSec;
                    outliers2.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });
                    break;
                case "abstractC":
                    ArrayList<Data> outliers3 = abstractC.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    elapsedTimeInSec = (Utils.getCPUTime() - start) * 1.0 / 1000000000;

                    totalTime += elapsedTimeInSec;
                    outliers3.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);

                    });
                    break;
                case "lue":
                    HashSet<DataLUEObject> outliers4 = lue.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    elapsedTimeInSec = (Utils.getCPUTime() - start) * 1.0 / 1000000000;

                    totalTime += elapsedTimeInSec;
                    outliers4.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });
                    break;
                case "due":
                    HashSet<DataLUEObject> outliers5 = due.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    elapsedTimeInSec = (Utils.getCPUTime() - start) * 1.0 / 1000000000;

                    totalTime += elapsedTimeInSec;
                    outliers5.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });
                    break;
                case "microCluster":
                    ArrayList<Data> outliers6 = micro.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    elapsedTimeInSec = (Utils.getCPUTime() - start) * 1.0 / 1000000000;

                    totalTime += elapsedTimeInSec;
                    outliers6.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);

                    });
                    
                    break;
                case "microCluster_new":
                    ArrayList<Data> outliers9 = mcnew.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    elapsedTimeInSec = (Utils.getCPUTime() - start) * 1.0 / 1000000000;

                    totalTime += elapsedTimeInSec;
                    outliers9.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);

                    });
                    
//                    ArrayList<Data> outliers10 = estorm.detectOutlier(incomingData, currentTime, Constants.W,
//                            Constants.slide);
//                    
//                    System.out.println("--------------------------------------------------");
//                    System.out.println("Not in exact storm");
//                    for(Data d: outliers9){
//                        if(!outliers10.contains(d))
//                            System.out.println(d.arrivalTime);
//                    }
//                    System.out.println("---------------------------------------------------");
                    break;
                case "mesi":
                    ArrayList<Data> outliers7 = mesi.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    elapsedTimeInSec = (Utils.getCPUTime() - start) * 1.0 / 1000000000;

                    totalTime += elapsedTimeInSec;
                    outliers7.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });
                    break;
                case "mesiWithHash":
                    HashSet<Vector> outliers8 = mesiWithHash.detectOutlier(incomingData, currentTime, Constants.W,
                            Constants.slide);
                    elapsedTimeInSec = (Utils.getCPUTime() - start) * 1.0 / 1000000000;

                    totalTime += elapsedTimeInSec;
                    outliers8.stream().forEach((outlier) -> {
                        idOutliers.add(outlier.arrivalTime);
                    });
                    break;

            }
                        if (numberWindows == 1) {
                totalTime = 0;
                MesureMemoryThread.timeForIndexing = 0;
                MesureMemoryThread.timeForNewSlide =0;
                 MesureMemoryThread.timeForExpireSlide = 0;
            }
            System.out.println("#window: " + numberWindows);
            System.out.println("Total #outliers: " + idOutliers.size());
            System.out.println("Average Time: " + totalTime * 1.0 / numberWindows);
            System.out.println("Peak memory: " + MesureMemoryThread.maxMemory * 1.0 / 1024 / 1024);
            System.out.println("Time index, remove data from structure: " + MesureMemoryThread.timeForIndexing * 1.0 / 1000000000 / numberWindows);
            System.out.println("Time for querying: " + MesureMemoryThread.timeForQuerying * 1.0 / 1000000000 / numberWindows);
            System.out.println("Time for new slide: " + MesureMemoryThread.timeForNewSlide * 1.0 / 1000000000 / numberWindows);
            System.out.println("Time for expired slide: " + MesureMemoryThread.timeForExpireSlide * 1.0 / 1000000000 / numberWindows);
            System.out.println("------------------------------------");



            if (algorithm.equals("exactStorm")) {
                
                System.out.println("Avg neighbor list length = " + ExactStorm.avgAllWindowNeighbor/numberWindows);
            } else if (algorithm.equals("mesi")) {
                
                System.out.println("Avg trigger list = " + MESI.avgAllWindowTriggerList/numberWindows);
                System.out.println("Avg neighbor list = "+ MESI.avgAllWindowNeighborList/numberWindows);
            } else if (algorithm.equals("microCluster")) {
                
                System.out.println("Number clusters = " + MicroCluster.numberCluster/numberWindows);
                System.out.println("Max  Number points in event queue = " + MicroCluster.numberPointsInEventQueue);

                System.out.println("Avg number points in clusters= "+MicroCluster.numberPointsInClustersAllWindows/numberWindows);
                System.out.println("Avg Rmc size = " + MicroCluster.avgPointsInRmcAllWindows/numberWindows);
                System.out.println("Avg Length exps= " + MicroCluster.avgLengthExpsAllWindows/numberWindows);
            } else if (algorithm.equals("due")) {
//            Direct_Update_Event.numberPointsInEventQueue = Direct_Update_Event.numberPointsInEventQueue /numberWindows;
                Direct_Update_Event.avgAllWindowNumberPoints = Direct_Update_Event.numberPointsInEventQueue;
                System.out.println("max #points in event queue = " + Direct_Update_Event.avgAllWindowNumberPoints);
            }
            if (algorithm.equals("microCluster_new")) {
                System.out.println("avg points in clusters = "+MicroCluster_New.avgNumPointsInClusters *1.0/numberWindows);
                System.out.println("Avg points in event queue = "+ MicroCluster_New.avgNumPointsInEventQueue*1.0/numberWindows);
                System.out.println("avg neighbor list length = "+ MicroCluster_New.avgNeighborListLength*1.0/numberWindows);
            }
        }
        
//       
//        Constants.numberWindow--;

        
        
        ExactStorm.avgAllWindowNeighbor = ExactStorm.avgAllWindowNeighbor / numberWindows;
        MESI.avgAllWindowTriggerList = MESI.avgAllWindowTriggerList / numberWindows;
        MicroCluster.numberCluster = MicroCluster.numberCluster / numberWindows;
        MicroCluster.avgPointsInRmcAllWindows = MicroCluster.avgPointsInRmcAllWindows / numberWindows;
        MicroCluster.avgLengthExpsAllWindows = MicroCluster.avgLengthExpsAllWindows / numberWindows;
        MicroCluster.numberPointsInClustersAllWindows = MicroCluster.numberPointsInClustersAllWindows/numberWindows;
        MicroCluster_New.avgNumPointsInClusters = MicroCluster_New.avgNumPointsInClusters/numberWindows;
        mesureThread.averageTime = totalTime * 1.0 / (numberWindows-1);
        mesureThread.writeResult();
        mesureThread.stop();
        mesureThread.interrupt();

        /**
         * Write result to file
         */
        if (!"".equals(Constants.resultFile)) {
            writeResult();
        }
//      
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
                    case "--resultFile":
                        Constants.resultFile = args[i + 1];
                        break;
                    case "--samplingTime":
                        Constants.samplingPeriod = Integer.valueOf(args[i + 1]);
                        break;

                }
            }
        }
    }

    public static void writeResult() {

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Constants.resultFile, true)))) {
            for (Integer time : idOutliers) {
                out.println(time);
            }
        } catch (IOException e) {
        }

    }
}
