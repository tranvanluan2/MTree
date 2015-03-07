package outlierdetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import mtree.ComposedSplitFunction;
import mtree.DistanceFunction;
import mtree.DistanceFunctions;
import mtree.MTree;
import mtree.PartitionFunctions;
import mtree.PromotionFunction;
import mtree.tests.Data;
import mtree.tests.MTTest;
import mtree.utils.Constants;
import mtree.utils.Pair;
import mtree.utils.Utils;


public class AbstractC {
    public static MTreeClass mtree = new MTreeClass();

    //store list id in increasing time arrival order
    public static ArrayList<DataAbtractCObject> dataList = new ArrayList<DataAbtractCObject>();
    
    public AbstractC(){
        
    }
    public ArrayList<Data> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide){
        ArrayList<Data> outliers = new ArrayList<Data>();
        
        /**
         * remove expired data from dataList and mtree
         */
        int index = -1;
        for(int i = 0; i < dataList.size(); i++){
            Data d = dataList.get(i);
            if(d.arrivalTime <= currentTime-W)
            {
                //mark here for removing data from datalist later
                index = i;
                //remove from mtree
                mtree.remove(d);
            }
            else{
                break;
            }
        }
        for(int i = index; i >=0; i--)
        {
            
            dataList.remove(i);
        }
        
        
        
        for(Data d: data){
            
            DataAbtractCObject dac = new DataAbtractCObject(d, currentTime);
            
            /**
             * do range query for ob  
             */
            MTreeClass.Query query = mtree.getNearestByRange(dac, Constants.R);
            
            for(MTreeClass.ResultItem ri : query) {
                if(ri.distance == 0)
                    dac.values[0] += (new Random()).nextDouble()/1000000; 
             /** 
             * update neighbor for new ob and its neighbor's
             */
                DataAbtractCObject object = (DataAbtractCObject)ri.data;
                /**
                 * update lnt_count
                 */
                for(int n=0; n < object.lt_cnt.length; n++){
                    object.lt_cnt[n]++;
                    dac.lt_cnt[n]++;
                }
            }
            /**
             * store object into mtree
             */
            mtree.add(dac);

            dataList.add(dac);
            
        }
        
        //do outlier detection
        int count_outlier = 0;
        for(DataAbtractCObject d: dataList ){
            
//            System.out.println(d.values[0]);
            if(d.lt_cnt[0] < Constants.k){
                count_outlier ++ ;

                
                outliers.add(d);
//                System.out.println("Outlier: "+d.values[0]);
            }
            Integer[] temp = new Integer[d.lt_cnt.length-1];
            System.arraycopy(d.lt_cnt, 1, temp, 0, d.lt_cnt.length-1);
            d.lt_cnt = temp;
//            d.lt_cnt = Utils.removeFirstElement(d.lt_cnt);
        }
       
        System.out.println("Outliers: ");
        for (Data o : outliers) {
            System.out.print(o.values[0] + " ; ");
        }
        System.out.println();
        System.out.println("Data list: ");
        for (Data o : dataList) {
            System.out.print(o.values[0] + " ; ");
        }
        System.out.println();
        return outliers;
    }


};





class DataAbtractCObject extends Data{
    
    public Integer[] lt_cnt;
    
    
    
    
    public DataAbtractCObject(Data d, int currentTime){
        super();
        this.arrivalTime = d.arrivalTime;
        this.values = d.values;
        int lifespan = (int) Math.ceil((arrivalTime - currentTime+Constants.W)*1.0/Constants.slide);
        lt_cnt = new Integer[lifespan];
        Arrays.fill(lt_cnt, 0);
    }
    
    
}
