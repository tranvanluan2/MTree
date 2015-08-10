/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtree.tests;

import java.util.ArrayList;
import java.util.Set;
import mtree.MTree;
import static mtree.tests.MTTest.currentTime;
import mtree.utils.Constants;




/**
 *
 * @author Luan
 */

public class TestMemory {
    public static MTreeClass mtree = new MTreeClass();
    public static void main(String args[]){
        //measure memory before add points 
        Runtime.getRuntime().gc();
        long used = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        System.out.println("Before add incoming data: "+ used* 1.0 / 1024 / 1024+" MB");
        //add 10000 points (Exact Storm)
        Stream s = Stream.getInstance("TAO");
        ArrayList<Data> incomingData;
        if (currentTime != 0) {
            incomingData = s.getIncomingData(currentTime, Constants.slide);
            currentTime = currentTime + Constants.slide;
        } else {
            incomingData = s.getIncomingData(currentTime, Constants.W);
            currentTime = currentTime + Constants.W;
        }
        
        //measure memory after
        
        Runtime.getRuntime().gc();
        used = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        System.out.println("After add incoming data: "+ used* 1.0 / 1024 / 1024+" MB");
        
        //create exact storm data object
        ArrayList<DataStormObject> storms = new ArrayList<>();
        for(Data data: incomingData){
            DataStormObject d = new DataStormObject(data);
            storms.add(d);
        }
        
         //measure memory after
       
        Runtime.getRuntime().gc();
        used = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        System.out.println("After add object data: "+ used* 1.0 / 1024 / 1024+" MB");
        
        //add to mtree
         for(Data d: storms){
            mtree.add(d);
        }
        //after 
         Runtime.getRuntime().gc();
         used = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        System.out.println("After add object data to mtree: "+ used* 1.0 / 1024 / 1024+" MB");
        return;
        
    }
    
    
}
class DataStormObject extends Data {
    public int count_after;
    public ArrayList<DataStormObject> nn_before;

    public double frac_before = 0;

    public DataStormObject(Data d) {
        super();
        nn_before = new ArrayList<>();
        this.arrivalTime = d.arrivalTime;
        this.values = d.values;

    }

}
