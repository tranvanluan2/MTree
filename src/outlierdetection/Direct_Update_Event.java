package outlierdetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import mtree.tests.Data;
import mtree.utils.Constants;
import mtree.utils.FibonacciHeap.Node;

public class Direct_Update_Event extends Lazy_Update_Event {

    @Override
    public HashSet<DataLUEObject> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {

        /**
         * remove expired data from dataList and mtree
         */
        long startTime = System.currentTimeMillis();
        int index = -1;
        for (int i = 0; i < dataList.size(); i++) {
            DataLUEObject d = dataList.get(i);
            if (d.arrivalTime <= currentTime - W) {
                // mark here for removing data from datalist later
                index = i;
                // remove from mtree
                mtree.remove(d);

            } else {
                break;
            }
        }
        outlierList.stream().map((outlierList1) -> (DataLUEObject) outlierList1).forEach((d2) -> {
            while (d2.p_neighbors.size() > 0 && d2.p_neighbors.get(0).expireTime <= currentTime) {
                d2.p_neighbors.remove(0);
            }
        });
        
        process_event_queue(null, currentTime);

        for (int i = index; i >= 0; i--) {

            dataList.remove(i);
        }

        // Runtime.getRuntime().gc();
        data.stream().map((d) -> new DataLUEObject(d, currentTime)).map((p) -> {
            /**
             * do range query for ob
             */
            MTreeClassLUE.Query query = mtree.getNearestByRange(p, Constants.R);
            for (MTreeClassLUE.ResultItem ri : query) {
                if (ri.distance == 0) {
                    p.values[0] += (new Random()).nextDouble() / 1000000;
                }

                DataLUEObject q = (DataLUEObject) ri.data;
                if (q.arrivalTime >= currentTime - Constants.W) {
                    q.numberSuccedingNeighbors = q.numberSuccedingNeighbors + 1;
                    p.p_neighbors.add(q);
                    if (q.p_neighbors.size() + q.numberSuccedingNeighbors <= Constants.k) {
                        if (q.p_neighbors.size() + q.numberSuccedingNeighbors == Constants.k) {
                            outlierList.remove(q);
                            if (q.numberSuccedingNeighbors < Constants.k) {
                                q.ev = q.p_neighbors.get(0).expireTime;

                                Node<DataLUEObject> node = eventQueue.insert(q);
                                links.put(q, node);
                            }
                        }
                    } else if (q.p_neighbors.size() + q.numberSuccedingNeighbors > Constants.k
                            && q.p_neighbors.size() > 0 && q.numberSuccedingNeighbors - 1 < Constants.k) {

                        Node<DataLUEObject> node = links.get(q);
                        q.p_neighbors.remove(0);
                        if (q.p_neighbors.size() > 0) {
                            q.ev = q.p_neighbors.get(0).expireTime;
                           
                            eventQueue.increaseKey(node, q);
                        }

                    }

                }
            }
            return p;
        }).map((p) -> {
            Collections.sort(p.p_neighbors, new DataExpireTimeLUEComparator());
            return p;
        }).map((p) -> {
            while (p.p_neighbors.size() > Constants.k) {
                p.p_neighbors.remove(0);
            }
            return p;
        }).map((p) -> {
            if (p.p_neighbors.size() < Constants.k) {
                outlierList.add(p);
            } else {
                p.ev = p.p_neighbors.get(0).expireTime;
                Node<DataLUEObject> node = eventQueue.insert(p);
                links.put(p, node);
            }
            return p;
        }).map((p) -> {
            mtree.add(p);
            return p;
        }).forEach((p) -> {
            dataList.add(p);
        });

        return outlierList;
    }

}
