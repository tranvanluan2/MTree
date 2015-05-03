/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtree.tests;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtree.utils.Constants;
import mtree.utils.Utils;

/**
 *
 * @author Luan
 */
public class MesureMemoryThread extends Thread {

    public boolean stop = false;

    public static long maxMemory = 0;

    public  double averageTime = 0;

    public static double timeForIndexing = 0;
    public static double timeForDetecting = 0; 
    
    public static double timeForReporting = 0;
    public static double timeForQuerying = 0; 
    public void computeMemory() {

//        List<MemoryPoolMXBean> pools= ManagementFactory.getMemoryPoolMXBeans();
//        for(MemoryPoolMXBean p: pools){
//            
//            if(p.getName().equals("Tenured Generation"))
//                if(maxMemory < p.getUsage().getUsed())
//                    maxMemory = p.getUsage().getUsed();
//        }
        Runtime.getRuntime().gc();
        long used = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        if(maxMemory < used)
            maxMemory = used;
        
        
        
       
    }

    @Override
    public void run() {

        while (true) {
            computeMemory();
            try {
                Thread.sleep(Constants.samplingPeriod);
            } catch (InterruptedException ex) {
                Logger.getLogger(MesureMemoryThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void writeResult() {

        System.out.println("Peak memory: " + maxMemory * 1.0 / 1024 / 1024);
        System.out.println("Average CPU time: " + averageTime);

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Constants.outputFile, true)))) {
            out.println("----------------------------------------");
            out.println("Algorithm=" + MTTest.algorithm);
            out.println("Data File = " + Constants.dataFile);
            out.println("R=" + Constants.R + ";" + "k=" + Constants.k + ";W=" + Constants.W + ";slide=" + Constants.slide);
            out.println("Number Window = " + Constants.numberWindow);
            out.println("Peak Memory = " + maxMemory * 1.0 / 1024 / 1024);
            out.println("CPU time = " + averageTime);

        } catch (IOException e) {
        }
    }
}
