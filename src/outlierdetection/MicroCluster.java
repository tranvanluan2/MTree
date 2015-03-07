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

    public static ArrayList<Data> outlierList = new ArrayList<Data>();

    public static ArrayList<MCObject> inCluster_objects = new ArrayList<MCObject>();

    public ArrayList<Data> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {
        /**
         * purge expired objects
         */

        ArrayList<Data> expiredData = new ArrayList<Data>();

        int index = -1;

        for (int i = 0; i < dataList.size(); i++) {

            MCObject d = (MCObject) dataList.get(i);

            if (d.arrivalTime <= currentTime - W) {

                index = i;
                expiredData.add(d);

                if (d.isInCluster) {
                    ArrayList<MCObject> inCluster_objects;
                    if (d.isCenter) {
                        inCluster_objects = micro_clusters.get(d);

                    }
                    // update cluster
                    else inCluster_objects = micro_clusters.get(d.cluster);
                    inCluster_objects.remove(d);

                    /**
                     * check if size of cluster shrink below k+1
                     */
                    if (inCluster_objects.size() < Constants.k + 1) {
                        MicroCluster.inCluster_objects.addAll(inCluster_objects);
                        process_shrink_cluster(MicroCluster.inCluster_objects, currentTime);
                        MicroCluster.inCluster_objects.clear();
                    }

                } else {// d is in PD
                    PD.remove(d);
                    for (MCObject c : d.Rmc) {
                        ArrayList<MCObject> list_associates = associate_objects.get(c);
                        if (list_associates != null) list_associates.remove(d);

                    }
                }

            } else break;
        }
        process_event_queue(expiredData, currentTime);
        for (int i = index; i >= 0; i--) {

            dataList.remove(i);
        }

        /*
         * process new incoming data
         */
        // do range query with mtree of cluster centers
        for (Data d2 : data) {

            MCObject d = new MCObject(d2);

            process_data(d, currentTime, false);

            dataList.add(d);

        }

//        print_cluster();
//        print_outlier();
//        print_PD();
        return outlierList;

    }

    public void print_cluster() {
        for (MCObject o : micro_clusters.keySet()) {

            System.out.println("Center: " + o.values[0]);
            System.out.print("Member:");
            for (MCObject o2 : micro_clusters.get(o))
                System.out.print(o2.values[0] + " ; ");
            System.out.println();
        }
        System.out.println();
    }

    public void print_outlier() {
        System.out.println("Outliers: ");
        for (Data o : outlierList) {
            System.out.print(o.values[0] + " ; ");
        }
        System.out.println();
    }

    public void print_PD() {
        System.out.println();
        System.out.println("PD list: ");
        for (Data o : PD) {
            System.out.print(o.values[0] + " ; ");
        }
        System.out.println();
    }

    private void process_shrink_cluster(ArrayList<MCObject> inCluster_objects, int currentTime) {
        mtree.remove(inCluster_objects.get(0).cluster);
        ArrayList<MCObject> list_associates = associate_objects.get(inCluster_objects.get(0).cluster);
        if (list_associates != null) for (MCObject o : list_associates)
            o.Rmc.remove(inCluster_objects.get(0).cluster);
        associate_objects.remove(inCluster_objects.get(0).cluster);
        micro_clusters.remove(inCluster_objects.get(0).cluster);
        for (MCObject d : inCluster_objects) {
            d.cluster = null;
            d.isInCluster = false;
            d.isCenter = false;
            d.numberOfSucceeding = 0;
            d.exps.clear();
            d.ev = 0;
            d.Rmc.clear();

            process_data(d, currentTime, true);
        }

    }

    public void addObjectToCluster(MCObject d, MCObject cluster, boolean fromCluster) {

        d.cluster = cluster;
        d.isInCluster = true;

        micro_clusters.get(cluster).add(d);
        /**
         * evaluate distance between the new object and objects in PD that associate with cluster
         */
        ArrayList<MCObject> objects = associate_objects.get(cluster);

        if (objects != null) for (MCObject o : objects) {

            double distace = mtree.getDistanceFunction().calculate(d, o);
            if (distace <= Constants.R) {
                // increase number if succeeding neighbors
                // o.numberOfSucceeding++;
                if (o.arrivalTime < d.arrivalTime) {
                    if (MicroCluster.inCluster_objects.contains(o)|| fromCluster == false) o.numberOfSucceeding++;
                    d.exps.add(o.arrivalTime + Constants.W);
                } else {
                    if (MicroCluster.inCluster_objects.contains(o)|| fromCluster == false) o.exps
                            .add(d.arrivalTime + Constants.W);
                    d.numberOfSucceeding++;
                }
                // check if o is inlier
                if (o.exps.size() + o.numberOfSucceeding >= Constants.k && outlierList.contains(o)) {
                    outlierList.remove(o);
                    // add o to event queue
                    if (!o.exps.isEmpty()) {
                        o.ev = min(o.exps);
                        eventQueue.add(o);
                    }
                }

            }
        }

    }

    public void process_data(MCObject d, int currentTime, boolean fromCluster) {

        MTreeClass.Query query = mtree.getNearestByRange(d, Constants.R * 3 / 2);

        
        //ed

        double min_distance = Double.MAX_VALUE;

        MTreeClass.ResultItem ri = null;
        
        
        if (query.iterator().hasNext()) {
            ri = query.iterator().next();

            min_distance = ri.distance;
        }

        if (min_distance <= Constants.R / 2) {
            // assign to this closet cluster
            MCObject closest_cluster = (MCObject) ri.data;
            addObjectToCluster(d, closest_cluster, fromCluster);
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
                if (ri2.distance == 0) d.values[0] += (new Random()).nextDouble() / 1000000;

                /**
                 * scan in cluster to find neighbors
                 */
                d.Rmc.add((MCObject) ri2.data);

                if (associate_objects.get(ri2.data) == null) {
                    ArrayList<MCObject> l = new ArrayList<MCObject>();
                    l.add(d);
                    associate_objects.put((MCObject) ri2.data, l);
                } else {
                    associate_objects.get(ri2.data).add(d);
                }
                for (MCObject o : micro_clusters.get(ri2.data)) {

                    if (mtree.getDistanceFunction().calculate(d, o) <= Constants.R) {
                        neighbor_in_mtree.add(o);
                    }
                }

            }
            for (MCObject m : PD) {
                double distance = mtree.getDistanceFunction().calculate(d, m);

                if (distance <= Constants.R / 2) neighbor_in_R2.add(m);
                if (distance <= Constants.R) {
                    if ((int) m.values[0] == 16) System.out.println();
                    neighbor_in_PD.add(m);
                    neighbor_in_3_2Apart_PD.add(m);
                } else if (distance <= Constants.R * 3 / 2) neighbor_in_3_2Apart_PD.add(m);
            }
            neighbor.addAll(neighbor_in_PD);
            neighbor.addAll(neighbor_in_mtree);
            
            for (MCObject o : neighbor_in_PD) {

                if (o.arrivalTime < d.arrivalTime) {
                    if (MicroCluster.inCluster_objects.contains(o)|| fromCluster == false) o.numberOfSucceeding++;
                    d.exps.add(o.arrivalTime + Constants.W);
                } else {
                    if ( MicroCluster.inCluster_objects.contains(o)|| fromCluster == false) o.exps
                            .add(d.arrivalTime + Constants.W);
                    d.numberOfSucceeding++;
                }
                /**
                 * check for o becomes inlier
                 */
                if (o.numberOfSucceeding + o.exps.size() >= Constants.k && outlierList.contains(o)) {
                    outlierList.remove(o);
                    if (o.exps.size() > 0) o.ev = min(o.exps);
                    eventQueue.add(o);
                }

            }
            for (MCObject o : neighbor_in_mtree) {

                if (o.arrivalTime < d.arrivalTime) {
                    d.exps.add(o.arrivalTime + Constants.W);
                } else {
                    d.numberOfSucceeding++;
                }
                
            }

            if (neighbor_in_R2.size() > Constants.k * 1.1) {

                // form cluster
                d.isCenter = true;
                d.isInCluster = true;
                neighbor_in_R2.add(d);
                for (MCObject o : neighbor_in_R2) {
                    o.cluster = d;
                    o.isInCluster = true;
                    o.numberOfSucceeding =0;
                    o.exps.clear();
                    PD.remove(o);
                    eventQueue.remove(o);
                    outlierList.remove(o);
                }
                micro_clusters.put(d, neighbor_in_R2);
                mtree.add(d);
                // update Rmc for points in PD
                for (MCObject o : neighbor_in_3_2Apart_PD) {

                    o.Rmc.add(d);
                }
                associate_objects.put(d, neighbor_in_3_2Apart_PD);

            }

            else // add to event queue and pd
            {
                
                

                PD.add(d);

                 Collections.sort(d.exps, Collections.reverseOrder());
                 for (int i = d.exps.size() - 1; i >= Constants.k; i--)
                 d.exps.remove(i);

                if (d.numberOfSucceeding + d.exps.size() < Constants.k) {

                    outlierList.add(d);

                } else if (d.numberOfSucceeding + d.exps.size() >= Constants.k && d.exps.size() > 0) {

                    /**
                     * keep k most recent preceeding neighbors
                     */

                    d.ev = min(d.exps);
                    eventQueue.add(d);
                }
            }

        }

    }

    private void process_event_queue(ArrayList<Data> expireData, int currentTime) {
        MCObject x = eventQueue.peek();

        while (x != null && x.ev <= currentTime) {

            x = eventQueue.poll();

            for (int i = x.exps.size() - 1; i >= 0; i--) {
                if (x.exps.get(i) <= currentTime) x.exps.remove(i);
            }

            if (x.exps.size() + x.numberOfSucceeding < Constants.k) {

                outlierList.add(x);

            } else if (x.exps.size() > 0) {
                x.ev = min(x.exps);
                eventQueue.add(x);
            }

            x = eventQueue.peek();
        }

        for (Data p : expireData) {
            outlierList.remove(p);
        }

        for (int i = 0; i < outlierList.size(); i++) {

            MCObject d = (MCObject) outlierList.get(i);

            for (int k = d.exps.size() - 1; k >= 0; k--) {
                if (d.exps.get(k) <= currentTime) d.exps.remove(k);
            }
        }

    }

    private int min(ArrayList<Integer> exps) {
        int min = exps.get(0);
        for (Integer i : exps)
            if (i < min) min = i;
        return min;
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