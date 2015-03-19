package outlierdetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import mtree.tests.Data;
import mtree.utils.Constants;

public class AbstractC {
    public static MTreeClass mtree = new MTreeClass();

    // store list id in increasing time arrival order
    public static ArrayList<DataAbtractCObject> dataList = new ArrayList<>();

    public AbstractC() {

    }

    public ArrayList<Data> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {
        ArrayList<Data> outliers = new ArrayList<>();

        /**
         * remove expired data from dataList and mtree
         */
        int index = -1;
        for (int i = 0; i < dataList.size(); i++) {
            Data d = dataList.get(i);
            if (d.arrivalTime <= currentTime - W) {
                // mark here for removing data from datalist later
                index = i;
                // remove from mtree
                mtree.remove(d);
                // System.out.println(t);
                
               
            } else {
                break;
            }
        }
        for (int i = index; i >= 0; i--) {

            dataList.remove(i);
        }

     

        data.stream().map((d) -> new DataAbtractCObject(d, currentTime)).map((dac) -> {
            /**
             * do range query for ob
             */
            MTreeClass.Query query = mtree.getNearestByRange(dac, Constants.R);
            for (MTreeClass.ResultItem ri : query) {
                if (ri.distance == 0) dac.values[0] += (new Random()).nextDouble() / 1000000;
                /**
                 * update neighbor for new ob and its neighbor's
                 */
                DataAbtractCObject object = (DataAbtractCObject) ri.data;
                if (object.arrivalTime >= currentTime - Constants.W) {

                    /**
                     * update lnt_count
                     */
                    for (int n = 0; n < object.lt_cnt.length; n++) {
                        object.lt_cnt[n]++;
                        dac.lt_cnt[n]++;
                    }
                }
            }
            return dac;
        }).map((dac) -> {
            /**
             * store object into mtree
             */
            mtree.add(dac);
            return dac;
        }).forEach((dac) -> {
            dataList.add(dac);
            
            
//            Utils.computeUsedMemory();
        });

        // do outlier detection
        dataList.stream().map((d) -> {
            if (d.lt_cnt[0] < Constants.k) {

                outliers.add(d);

            }
            return d;
        }).forEach((d) -> {
            Integer[] temp = new Integer[d.lt_cnt.length - 1];
            System.arraycopy(d.lt_cnt, 1, temp, 0, d.lt_cnt.length - 1);
            d.lt_cnt = temp;
        }); // System.out.println("Outliers: ");
        // for (Data o : outliers) {
        // System.out.print(o.values[0] + " ; ");
        // }
        // System.out.println();
        // System.out.println("Data list: ");
        // for (Data o : dataList) {
        // System.out.print(o.values[0] + " ; ");
        // }
        // System.out.println();
        
        
//        Utils.computeUsedMemory();
        return outliers;
    }

};

class DataAbtractCObject extends Data {

    public Integer[] lt_cnt;

    public DataAbtractCObject(Data d, int currentTime) {
        super();
        this.arrivalTime = d.arrivalTime;
        this.values = d.values;
        int lifespan = (int) Math.ceil((arrivalTime - currentTime + Constants.W) * 1.0 / Constants.slide);
        lt_cnt = new Integer[lifespan];
        Arrays.fill(lt_cnt, 0);
    }

}
