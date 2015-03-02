package outlierdetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

public class ExactStorm {

    public static MTreeClass mtree = new MTreeClass();

    // store list id in increasing time arrival order
    public static ArrayList<DataStormObject> dataList = new ArrayList<DataStormObject>();

    public ExactStorm() {

    }

    public ArrayList<Data> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {
        ArrayList<Data> outliers = new ArrayList<Data>();

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
            } else {
                break;
            }
        }
        for (int i = index; i >= 0; i--) {

            dataList.remove(i);
        }

        for (Data d : data) {

            DataStormObject ob = new DataStormObject(d);
            /**
             * do range query for ob
             */
            MTreeClass.Query query = mtree.getNearestByRange(ob, Constants.R);

            ArrayList<DataStormObject> queryResult = new ArrayList<DataStormObject>();
            for (MTreeClass.ResultItem ri : query) {
                queryResult.add((DataStormObject) ri.data);
                if (ri.distance == 0) ob.values[0] += (new Random()).nextDouble() / 1000;
            }

            Collections.sort(queryResult, new DataStormComparator());

            for (int i = 0; i < queryResult.size(); i++) {

                /**
                 * update neighbor for new ob and its neighbor's
                 */
                DataStormObject dod = queryResult.get(i);

                if (dod != null) {

                    if (ob.nn_before.size() < Constants.k) ob.nn_before.add(dod);

                    dod.count_after++;
                }

            }

            /**
             * store object into mtree
             */
            mtree.add(ob);

            dataList.add(ob);

        }

        // do outlier detection
        int count_outlier = 0;
        for (DataStormObject d : dataList) {
            /**
             * Count preceeding neighbors
             */
            // System.out.println(d.values[0]);
            int pre = 0;
            for (int i = 0; i < d.nn_before.size(); i++) {
                if (d.nn_before.get(i).arrivalTime > currentTime - W) {
                    pre++;
                }
            }
            if (pre + d.count_after < Constants.k) {
                count_outlier++;
                // System.out.println("Outlier: "+d.values[0]);
                outliers.add(d);
            }
        }
        // System.out.println("#outliers: "+count_outlier);

        return outliers;
    }
}

class MTreeClass extends MTree<Data> {

    private static final PromotionFunction<Data> nonRandomPromotion = new PromotionFunction<Data>() {
        @Override
        public Pair<Data> process(Set<Data> dataSet, DistanceFunction<? super Data> distanceFunction) {
            return Utils.minMax(dataSet);
        }
    };

    MTreeClass() {
        super(2, DistanceFunctions.EUCLIDEAN, new ComposedSplitFunction<Data>(nonRandomPromotion,
                new PartitionFunctions.BalancedPartition<Data>()));
    }

    public void add(Data data) {
        super.add(data);
        _check();
    }

    public boolean remove(Data data) {
        boolean result = super.remove(data);
        _check();
        return result;
    }

    DistanceFunction<? super Data> getDistanceFunction() {
        return distanceFunction;
    }
};

class DataStormComparator implements Comparator<DataStormObject> {
    @Override
    public int compare(DataStormObject o1, DataStormObject o2) {
        if (o1.arrivalTime < o2.arrivalTime) return 1;
        else if (o1.arrivalTime == o2.arrivalTime) return 0;
        else return -1;
    }
};

class DataStormObject extends Data {
    public int count_after;
    public ArrayList<DataStormObject> nn_before;

    public double frac_before = 0;

    public DataStormObject(Data d) {
        super();
        nn_before = new ArrayList<DataStormObject>();
        this.arrivalTime = d.arrivalTime;
        this.values = d.values;

    }

}