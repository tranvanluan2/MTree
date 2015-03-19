package outlierdetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import mtree.tests.Data;
import mtree.utils.Constants;
import mtree.utils.FibonacciHeap.Node;

public class Direct_Update_Event extends Lazy_Update_Event {

    public ArrayList<DataLUEObject> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {

        /**
         * remove expired data from dataList and mtree
         */
        int index = -1;
        for (int i = 0; i < dataList.size(); i++) {
            DataLUEObject d = dataList.get(i);
            if (d.arrivalTime <= currentTime - W) {
                // mark here for removing data from datalist later
                index = i;
                // remove from mtree
                mtree.remove(d);

                if (d.numberSuccedingNeighbors + d.p_neighbors.size() >= Constants.k
                    && d.numberSuccedingNeighbors < Constants.k) {
                    eventQueue.delete(links.get(d));
                    links.remove(d);
                }
                // dataList.remove(i);

                // test

                else if (d.numberSuccedingNeighbors + d.p_neighbors.size() < Constants.k) outlierList
                        .remove(d);

                for (int j = 0; j < outlierList.size(); j++) {

                    DataLUEObject d2 = (DataLUEObject) outlierList.get(j);

                    while (d2.p_neighbors.size() > 0 && d2.p_neighbors.get(0).expireTime <= currentTime)
                        d2.p_neighbors.remove(0);

                }

            } else {
                break;
            }
        }
        process_event_queue(null, currentTime);

        for (int i = index; i >= 0; i--) {

            dataList.remove(i);
        }

        for (int j = 0; j < data.size(); j++) {
            Data d = data.get(j);
            DataLUEObject p = new DataLUEObject(d, currentTime);
            /**
             * do range query for ob
             */
            MTreeClassLUE.Query query = mtree.getNearestByRange(p, Constants.R);

            for (MTreeClassLUE.ResultItem ri : query) {

                // Runtime.getRuntime().gc();
                if (ri.distance == 0) p.values[0] += (new Random()).nextDouble() / 1000000;

                DataLUEObject q = (DataLUEObject) ri.data;

                if (q.arrivalTime >= currentTime - Constants.W) {
                    q.numberSuccedingNeighbors = q.numberSuccedingNeighbors + 1;

                    p.p_neighbors.add(q);

                    if (q.numberSuccedingNeighbors + q.p_neighbors.size() <= Constants.k) {
                        if (q.p_neighbors.size() + q.numberSuccedingNeighbors == Constants.k) {
                            outlierList.remove(q);
                            if (q.numberSuccedingNeighbors < Constants.k) {

                                q.ev = q.p_neighbors.get(0).expireTime;
                                Node<DataLUEObject> node = eventQueue.insert(q);
                                links.put(q, node);
                            }
                        }
                    } 
//                    else {
//                        if (q.numberSuccedingNeighbors - 1 < Constants.k
//                            && q.numberSuccedingNeighbors - 1 + q.p_neighbors.size() >= Constants.k) {
//
//                            if (q.ev == eventQueue.findMinimum().getKey().ev) q.p_neighbors.remove(0);
//                            else if (q.ev != eventQueue.findMinimum().getKey().ev && q.p_neighbors.size() > 0) {
//                                Node<DataLUEObject> node = links.get(q);
//                                q.p_neighbors.remove(0);
//                                if (q.p_neighbors.size() > 0) {
//                                    q.ev = q.p_neighbors.get(0).expireTime;
//
//                                    eventQueue.increaseKey(node, q);
//                                }
//                            }
//                        }
//
//                    }
                }
            }

            Collections.sort(p.p_neighbors, new DataExpireTimeLUEComparator());
            while (p.p_neighbors.size() > Constants.k)
                p.p_neighbors.remove(0);

            if (p.p_neighbors.size() < Constants.k) outlierList.add(p);
            else {

                p.ev = p.p_neighbors.get(0).expireTime;

                Node<DataLUEObject> node = eventQueue.insert(p);
                links.put(p, node);
            }

            mtree.add(p);
            dataList.add(p);

        }

        return outlierList;
    }

    // private void process_event_queue(ArrayList<DataLUEObject> expireData, int currentTime) {
    // DataLUEObject x = eventQueue.peek();
    //
    // while (x != null && x.ev <= currentTime) {
    //
    // x = eventQueue.poll();
    //
    // while (x.p_neighbors.get(0).expireTime <= currentTime)
    // x.p_neighbors.remove(0);
    // outlierList.add(x);
    // x = eventQueue.peek();
    // }
    //
    // }

}
