package outlierdetection;

import java.awt.image.ConvolveOp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

import org.hamcrest.core.IsCollectionContaining;

import mtree.DistanceFunctions;
import mtree.tests.Data;
import mtree.utils.Constants;

public class MicroCluster {

    public static HashMap<MCObject,ArrayList<MCObject>> micro_clusters = new HashMap<MCObject,ArrayList<MCObject>>();

    public static HashMap<MCObject,ArrayList<MCObject>> associate_objects = new HashMap<MCObject,ArrayList<MCObject>>();

    public static ArrayList<MCObject> PD = new ArrayList<MCObject>();

    // store list ob in increasing time arrival order
    public static ArrayList<MCObject> dataList = new ArrayList<MCObject>();

    public static MTreeClass mtree = new MTreeClass();

    public static PriorityQueue<MCObject> eventQueue = new PriorityQueue<MCObject>(new MCComparator());

    public ArrayList<Data> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {
        /**
         * purge expired objects
         */
        int index = -1;
        for (int i = 0; i < dataList.size(); i++) {
            MCObject d = (MCObject) dataList.get(i);
            if (d.arrivalTime <= currentTime - W) {
                // mark here for removing data from datalist later
                index = i;
                // remove from mtree
                if (d.isInCluster) {
                    if (d.isCenter) {
                        mtree.remove(d);

                        /**
                         * check if size of cluster shrink below k+1
                         */

                    }
                    // update cluster
                    ArrayList<MCObject> inCluster_objects = micro_clusters.get(d);
                    inCluster_objects.remove(d);

                    /**
                     * check if size of cluster shrink below k+1
                     */
                    if (inCluster_objects.size() < Constants.k + 1) {
                        process_shrink_cluster(inCluster_objects, currentTime);
                    }

                } else {// d is in PD
                    PD.remove(d);
                    process_event_queue(d, currentTime);// to be implemented
                    eventQueue.remove(d);
                    for (MCObject c : d.Rmc) {
                        associate_objects.get(c).remove(d);
                    }
                }

            }
        }
        for (int i = index; i >= 0; i--) {

            dataList.remove(i);
        }

        /*
         * process new incoming data
         */
        // do range query with mtree of cluster centers
        for (Data d2 : data) {

            MCObject d = new MCObject(d2);

            process_data(d, currentTime);

        }

        return null;

    }

    private void process_shrink_cluster(ArrayList<MCObject> inCluster_objects, int currentTime) {
        for (MCObject d : inCluster_objects) {
            d.cluster = null;
            d.isInCluster = false;
            d.isCenter = false;
            process_data(d, currentTime);
        }

    }

    public void process_data(MCObject d, int currentTime) {

        MTreeClass.Query query = mtree.getNearestByRange(d, Constants.R * 3 / 2);

        double min_distace = Double.MAX_VALUE;

        MTreeClass.ResultItem ri = query.iterator().next();

        min_distace = ri.distance;

        if (min_distace <= Constants.R / 2) {
            // assign to this closet cluster
            MCObject closest_cluster = (MCObject) ri.data;
            micro_clusters.get(closest_cluster).add(d);

            d.isInCluster = true;
            d.cluster = closest_cluster;

            /**
             * evaluate distace between the new object and objects in PD that associate with cluster
             */
            ArrayList<MCObject> objects = associate_objects.get(closest_cluster);

            for (MCObject o : objects) {
                double distace = mtree.getDistanceFunction().calculate(d, o);
                if (distace <= Constants.R) {
                    // increase number if succeeding neighbors
                    o.numberOfSucceeding++;
                    // check if o is inlier

                }
            }
        }

        else {

            /**
             * do range query in PD and mtree (distance to center <= 3/2R)
             */
            ArrayList<MCObject> neighbor_in_mtree = new ArrayList<MCObject>();
            ArrayList<MCObject> neighbor_in_PD = new ArrayList<MCObject>();
            ArrayList<MCObject> neighbor_in_3_2Apart_PD = new ArrayList<MCObject>();
            ArrayList<MCObject> neighbor = new ArrayList<MCObject>();
            ArrayList<MCObject> neighbor_in_R2 = new ArrayList<MCObject>();
            for (MTreeClass.ResultItem ri2 : query) {
                if (ri2.distance <= Constants.R) neighbor_in_mtree.add((MCObject) ri2.data);
            }
            for (MCObject m : PD) {
                double distance = mtree.getDistanceFunction().calculate(d, m) ;

                if(distance <= Constants.R/2)
                    neighbor_in_R2.add(m);
                if (distance <= Constants.R)
                    {
                        neighbor_in_PD.add(m);
                        neighbor_in_3_2Apart_PD.add(m);
                    }
                else if(distance <= Constants.R*3/2)
                    neighbor_in_3_2Apart_PD.add(m);
            }
            neighbor.addAll(neighbor_in_PD);
            neighbor.addAll(neighbor_in_mtree);

            /**
                 * 
                 */
            if (neighbor_in_R2.size() > Constants.k *1.1 ) {
                // form cluster
                d.isCenter = true;
                d.isInCluster = true;
                neighbor_in_R2.add(d);
                for (MCObject o : neighbor_in_R2) {
                    o.cluster = d;
                    o.isInCluster = true;
                    PD.remove(o);
                }
                micro_clusters.put(d, neighbor_in_R2);
                // update Rmc for points in PD
                for (MCObject o : neighbor_in_3_2Apart_PD) {
                    o.Rmc.add(d);
                }

            }

            else // add to event queue and pd
            {
                d.ev = Integer.MAX_VALUE;
                for (MCObject o : neighbor) {
                    d.exps.add(o.arrivalTime + Constants.W);
                    o.numberOfSucceeding++;
                    if (o.arrivalTime + Constants.W < d.ev) d.ev = o.arrivalTime + Constants.W;
                }
                eventQueue.add(d);
            }

        }

    }

    private void process_event_queue(MCObject d, int currentTime) {
        // TODO Auto-generated method stub

    }

}

class MCComparator implements Comparator<MCObject> {

    @Override
    public int compare(MCObject o1, MCObject o2) {
        if (o1.ev < o2.ev) return -1;
        else if (o1.ev == o2.ev) return 0;
        else return 1;

    }

}

class MCObject extends Data {

    public MCObject cluster;
    public ArrayList<Integer> exps;
    public ArrayList<MCObject> objectInCluster;
    public ArrayList<MCObject> Rmc;

    public int ev;
    public boolean isInCluster;
    public boolean isCenter;

    public int numberOfSucceeding;

    public MCObject(Data d) {
        super();
        this.arrivalTime = d.arrivalTime;
        this.values = d.values;

        exps = new ArrayList<Integer>();
        objectInCluster = new ArrayList<MCObject>();
        Rmc = new ArrayList<MCObject>();
    }

}