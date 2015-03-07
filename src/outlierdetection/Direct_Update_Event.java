package outlierdetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import mtree.tests.Data;
import mtree.utils.Constants;

public class Direct_Update_Event extends Lazy_Update_Event {

    public ArrayList<Data> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {

        /**
         * remove expired data from dataList and mtree
         */
        ArrayList<DataLUEObject> expiredData = new ArrayList<DataLUEObject>();
        int index = -1;
        for (int i = 0; i < dataList.size(); i++) {
            DataLUEObject d = dataList.get(i);
            if (d.arrivalTime <= currentTime - W) {
                // mark here for removing data from datalist later
                index = i;
                // remove from mtree
                mtree.remove(d);

                // dataList.remove(i);
                expiredData.add(d);

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

                    if (q.numberSuccedingNeighbors >= Constants.k) eventQueue.remove(q);

                    else {

                        q.p_neighbors.remove(minIndex(q.p_neighbors));
                        Collections.sort(q.p_neighbors);
                        while (q.p_neighbors.size() > Constants.k - q.numberSuccedingNeighbors)
                            q.p_neighbors.remove(0);
                        q.ev = min(q.p_neighbors);
                        eventQueue.remove(q);
                        // if(q.p_neighbors.size() + q.numberSuccedingNeighbors != Constants.k)
                        // System.out.println("Cho nay dfdf Sai me roi!: "+ (q.p_neighbors.size() +
                        // q.numberSuccedingNeighbors));
                        eventQueue.add(q);
                    }

                }
            }

            Collections.sort(p.p_neighbors);
            while (p.p_neighbors.size() > Constants.k)
                p.p_neighbors.remove(0);

            if (p.p_neighbors.size() < Constants.k) outlierList.add(p);
            else {

                if (p.p_neighbors.size() + p.numberSuccedingNeighbors != Constants.k) System.out
                        .println("Cho nay Sai me roi!");
                p.ev = min(p.p_neighbors);

                eventQueue.add(p);
            }

            mtree.add(p);
            dataList.add(p);

        }
        return outlierList;
    }

    private void process_event_queue(ArrayList<DataLUEObject> expireData, int currentTime) {
        DataLUEObject x = eventQueue.peek();

        while (x != null && x.ev <= currentTime) {

            x = eventQueue.poll();

            outlierList.add(x);
            x = eventQueue.peek();
        }

        for (Data p : expireData) {
            outlierList.remove(p);
        }
        for (DataLUEObject p : expireData)
            for (int i = 0; i < outlierList.size(); i++) {

                DataLUEObject d = (DataLUEObject) outlierList.get(i);

                for (int j = d.p_neighbors.size() - 1; j >= 0; j--)
                    if (d.p_neighbors.get(j) <= p.expireTime) d.p_neighbors.remove(j);
            }

    }

}
