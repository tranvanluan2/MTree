package mtree.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import mtree.utils.Constants;

public class Stream {
    
    
    PriorityQueue<Data> streams ;
    
    public static Stream streamInstance;
    
    public static Stream getInstance(String type){
        if(streamInstance!= null)
            return streamInstance;
        else if(type == "ForestCover")
        {
            streamInstance = new Stream();
            streamInstance.getData(Constants.forestCoverFileName);
            return streamInstance;
        }
        else if(type=="TAO")
        {
            streamInstance = new Stream();
            streamInstance.getData(Constants.taoFileName);
            return streamInstance;
        }
        else if(type=="randomData")
        {
            streamInstance = new Stream();
            streamInstance.getData(Constants.randomFileName);
            return streamInstance;
        }
        else if(type=="tagData")
        {
            streamInstance = new Stream();
            streamInstance.getData(Constants.tagCALC);
            return streamInstance;
        }
        else {
            streamInstance = new Stream();
            streamInstance.getRandomInput(1000, 10);
            return streamInstance;
            
        }
    }
    
    
    public boolean hasNext(){
        return !streams.isEmpty();
    }
    public ArrayList<Data> getIncomingData(int currentTime, int length){
        ArrayList<Data> results = new ArrayList<Data>();
        Data d = streams.peek();
        while(d!=null && d.arrivalTime > currentTime
                && d.arrivalTime <= currentTime+length){
            results.add(d);
            streams.poll();
            d = streams.peek();
            
        }
        return results;
        
    }
    public void getRandomInput(int length, int range){
        
        Random r = new Random();
        for(int i=1; i<=length; i++){
            double  d = r.nextInt(range);
            Data data = new Data(d);
            data.arrivalTime = i;
            streams.add(data);
            
        }
        
    }
    
    public void getData(String filename){
        
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(new File(filename)));
        
            String line = "";
            int time =1 ;
            try {
                while((line = bfr.readLine())!=null){
                    
                    String[] atts = line.split(",");
                    double[] d  = new double[atts.length];
                    for(int i = 0; i < d.length; i++){
                        
                        d[i] = Double.valueOf(atts[i]);
                    }
                    Data data = new Data(d);
                    data.arrivalTime = time;
                    streams.add(data);
                    time ++;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    public Stream(){
        Comparator<Data> comparator = new DataComparator();
        
        streams = new PriorityQueue<Data>(comparator);
        
        
    }

}

class DataComparator implements Comparator<Data>{
    
    @Override
    public int compare(Data x, Data y)
    {
        if(x.arrivalTime < y.arrivalTime)
            return -1;
        else if(x.arrivalTime > y.arrivalTime)
            return 1;
        else return 0;
        
    
    }
    
}
