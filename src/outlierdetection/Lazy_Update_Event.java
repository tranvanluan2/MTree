package outlierdetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

import mtree.tests.Data;
import mtree.utils.Constants;

public class Lazy_Update_Event {

    public static PriorityQueue<DataLUEObject> eventQueue = new PriorityQueue<DataLUEObject>(
            new DataLUEComparator());
    public static ArrayList<Data> outlierList = new ArrayList<Data>();
    public static MTreeClass mtree = new MTreeClass();
    // store list id in increasing time arrival order
    public static ArrayList<DataLUEObject> dataList = new ArrayList<DataLUEObject>();

    public ArrayList<Data> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {

        /**
         * remove expired data from dataList and mtree
         */
        ArrayList<DataLUEObject> expiredData = new ArrayList<DataLUEObject>();
        int index = -1;
        for (int i = 0; i < dataList.size(); i++) {
            Data d = dataList.get(i);
            if (d.arrivalTime <= currentTime - W) {
                // mark here for removing data from datalist later
                index = i;
                // remove from mtree
                mtree.remove(d);
                

                // dataList.remove(i);
                expiredData.add((DataLUEObject)d);

            } else {
                break;
            }
        }
        process_event_queue(expiredData, currentTime);
        for (int i = index; i >= 0; i--) {

            dataList.remove(i);
        }

        for (int j = 0; j < data.size(); j++) {
            Data d = data.get(j);
            DataLUEObject p = new DataLUEObject(d, currentTime);
            /**
             * do range query for ob
             */
            MTreeClass.Query query = mtree.getNearestByRange(p, Constants.R);
            for (MTreeClass.ResultItem ri : query) {
                if (ri.distance == 0) p.values[0] += (new Random()).nextDouble() / 1000000;
                DataLUEObject q = (DataLUEObject) ri.data;
                q.numberSuccedingNeighbors = q.numberSuccedingNeighbors + 1;
                p.p_neighbors.add(q.expireTime);
                if (outlierList.contains(q)) {
                    if (q.p_neighbors.size() + q.numberSuccedingNeighbors == Constants.k) {
                        outlierList.remove(q);
                        if (q.numberSuccedingNeighbors < Constants.k && q.p_neighbors.size() > 0) {
                            q.ev = min(q.p_neighbors);
                            eventQueue.add(q);
                        }
                    }
                } else {
                    q.p_neighbors.remove(minIndex(q.p_neighbors));
                    if (q.numberSuccedingNeighbors >= Constants.k) eventQueue.remove(q);
                }
            }

            Collections.sort(p.p_neighbors);
            while (p.p_neighbors.size() > Constants.k)
                p.p_neighbors.remove(0);

            if (p.p_neighbors.size() < Constants.k) outlierList.add(p);
            else {
                p.ev = min(p.p_neighbors);
                eventQueue.add(p);
            }

            mtree.add(p);
            dataList.add(p);

        }
        return outlierList;
    }

    public Integer minIndex(ArrayList<Integer> objects) {
        if (objects.size() == 0) return null;
        int result = 0;
        Integer min = objects.get(0);
        for (int i = 0; i<objects.size(); i++) {
            if (objects.get(i) < min) result = i;
        }
        return result;
    }

    public int min(ArrayList<Integer> objects) {
        int min = objects.get(0);
        for (Integer d : objects) {
            if (d< min) min = d;
        }
        return min;
    }

    private void process_event_queue(ArrayList<DataLUEObject> expireData, int currentTime) {
        DataLUEObject x = eventQueue.peek();

        while (x != null && x.ev <= currentTime) {

            x = eventQueue.poll();
            // remove p from x
            // if (x.p_neighbors.contains(p)) {
            
            for (int i = x.p_neighbors.size()-1; i >=0 ; i--)
                if(x.p_neighbors.get(i) <= currentTime)
                    x.p_neighbors.remove(i);

            if (x.p_neighbors.size() + x.numberSuccedingNeighbors < Constants.k) {
                outlierList.add(x);

            } else {
                x.ev = min(x.p_neighbors);
                eventQueue.add(x);
            }
            // }
            x = eventQueue.peek();
        }

        for (Data p : expireData) {
            outlierList.remove(p);
        }
        for (DataLUEObject p : expireData)
            for (int i = 0; i < outlierList.size(); i++) {

                DataLUEObject d = (DataLUEObject) outlierList.get(i);

                d.p_neighbors.remove(p.expireTime);
            }

        

    }

}

class DataLUEComparator implements Comparator<DataLUEObject> {
    @Override
    public int compare(DataLUEObject o1, DataLUEObject o2) {
        if (o1.ev < o2.ev) return -1;
        else if (o1.ev == o2.ev) return 0;
        else return 1;
    }
};

class DataExpireTimeLUEComparator implements Comparator<DataLUEObject> {
    @Override
    public int compare(DataLUEObject o1, DataLUEObject o2) {
        if (o1.expireTime < o2.expireTime) return -1;
        else if (o1.expireTime == o2.expireTime) return 0;
        else return 1;
    }
};

class DataLUEObject extends Data {

    public int expireTime;
    public int numberSuccedingNeighbors;
    public int ev;
    public ArrayList<Integer> p_neighbors = new ArrayList<Integer>();

    public DataLUEObject(Data d, int currentTime) {
        super();
        this.expireTime = d.arrivalTime + Constants.W;
        this.values = d.values;
        this.arrivalTime = d.arrivalTime;

    }

}