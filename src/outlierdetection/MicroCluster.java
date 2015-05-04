package outlierdetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

import mtree.tests.Data;
import mtree.tests.MesureMemoryThread;
import mtree.utils.Constants;
import mtree.utils.Utils;

public class MicroCluster {

    public static HashMap<MCObject,ArrayList<MCObject>> micro_clusters = new HashMap<>();

    public static HashMap<MCObject,ArrayList<MCObject>> associate_objects = new HashMap<>();

    public static ArrayList<MCObject> PD = new ArrayList<>();

    // store list ob in increasing time arrival order
    public static ArrayList<MCObject> dataList = new ArrayList<>();

    public static MTreeClass mtree = new MTreeClass();

    public static PriorityQueue<MCObject> eventQueue = new PriorityQueue<MCObject>(new MCComparator());

    public static ArrayList<MCObject> outlierList = new ArrayList<MCObject>();

    public static ArrayList<MCObject> inCluster_objects = new ArrayList<MCObject>();

    public ArrayList<Data> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {

        
        ArrayList<Data> result = new ArrayList<Data>();
        /**
         * purge expired objects
         */

        long startTime = Utils.getCPUTime();
        ArrayList<MCObject> expiredData = new ArrayList<MCObject>();

        int index = -1;

        for (int i = 0; i < dataList.size(); i++) {

            MCObject d =  dataList.get(i);

            if (d.arrivalTime <= currentTime - W) {

                index = i;
                expiredData.add(d);

                if (d.isInCluster) {
                    ArrayList<MCObject> inCluster_objects2;
                    if (d.isCenter) {
                        inCluster_objects2 = micro_clusters.get(d);

                    }
                    // update cluster
                    else inCluster_objects2 = micro_clusters.get(d.cluster);
                    if (inCluster_objects2 != null) {
                        inCluster_objects2.remove(d);

                        /**
                         * check if size of cluster shrink below k+1
                         */
                        if (inCluster_objects2.size() < Constants.k + 1) {
                            MicroCluster.inCluster_objects.addAll(inCluster_objects2);
                            process_shrink_cluster(MicroCluster.inCluster_objects, currentTime);
                            MicroCluster.inCluster_objects.clear();
                        }
                    }

                } else {// d is in PD
                    PD.remove(d);
                    d.Rmc.stream().map((c) -> associate_objects.get(c)).filter((list_associates) -> (list_associates != null)).forEach((list_associates) -> {
                        list_associates.remove(d);
                    });
                }

            } else break;
        }
        process_event_queue(expiredData, currentTime);
        for (int i = index; i >= 0; i--) {

            dataList.remove(i);
        }
        
        MesureMemoryThread.timeForExpireSlide += Utils.getCPUTime() - startTime;
        
        /*
         * process new incoming data
         */
        // do range query with mtree of cluster centers
        startTime = Utils.getCPUTime();
        data.stream().map((d2) -> new MCObject(d2)).map((d) -> {
            process_data(d, currentTime, false);
            return d;
        }).forEach((d) -> {
            dataList.add(d);
        });


        outlierList.stream().forEach((o) -> {
            result.add(o);
        });
        MesureMemoryThread.timeForNewSlide += Utils.getCPUTime() - startTime;
        
        return result;

    }

    public void print_cluster() {
        micro_clusters.keySet().stream().map((o) -> {
            System.out.println("Center: " + o.values[0]);
            return o;
        }).map((o) -> {
            System.out.print("Member:");
            return o;
        }).map((o) -> {
            micro_clusters.get(o).stream().forEach((o2) -> {
                System.out.print(o2.values[0] + " ; ");
            });
            return o;
        }).forEach((_item) -> {
            System.out.println();
        });
        System.out.println();
    }

    public void print_outlier() {
        System.out.println("Outliers: ");
        outlierList.stream().forEach((o) -> {
            System.out.print(o.values[0] + " ; ");
        });
        System.out.println();
    }

    public void print_PD() {
        System.out.println();
        System.out.println("PD list: ");
        PD.stream().forEach((o) -> {
            System.out.print(o.values[0] + " ; ");
        });
        System.out.println();
    }

    private void process_shrink_cluster(ArrayList<MCObject> inCluster_objects, int currentTime) {
        long startTime = Utils.getCPUTime();
        mtree.remove(inCluster_objects.get(0).cluster);
        MesureMemoryThread.timeForIndexing += Utils.getCPUTime() - startTime;
        ArrayList<MCObject> list_associates = associate_objects.get(inCluster_objects.get(0).cluster);
        if (list_associates != null) list_associates.stream().forEach((o) -> {
            o.Rmc.remove(inCluster_objects.get(0).cluster);
        });
        associate_objects.remove(inCluster_objects.get(0).cluster);
        micro_clusters.remove(inCluster_objects.get(0).cluster);
        inCluster_objects.stream().map((d) -> {
            d.cluster = null;
            return d;
        }).map((d) -> {
            d.isInCluster = false;
            return d;
        }).map((d) -> {
            d.isCenter = false;
            return d;
        }).map((d) -> {
            d.numberOfSucceeding = 0;
            return d;
        }).map((d) -> {
            d.exps.clear();
            return d;
        }).map((d) -> {
            d.ev = 0;
            return d;
        }).map((d) -> {
            d.Rmc.clear();
            return d;
        }).forEach((d) -> {
            process_data(d, currentTime, true);
        });

    }

    public void addObjectToCluster(MCObject d, MCObject cluster, boolean fromCluster) {

        d.cluster = cluster;
        d.isInCluster = true;

        micro_clusters.get(cluster).add(d);
        /**
         * evaluate distance between the new object and objects in PD that associate with cluster
         */
        ArrayList<MCObject> objects = associate_objects.get(cluster);

        if (objects != null) objects.stream().forEach((o) -> {
            double distace = mtree.getDistanceFunction().calculate(d, o);
            if (distace <= Constants.R) {
                // increase number if succeeding neighbors
                // o.numberOfSucceeding++;
                if (o.arrivalTime < d.arrivalTime) {
                    if (MicroCluster.inCluster_objects.contains(o) || fromCluster == false) o.numberOfSucceeding++;
                    d.exps.add(o.arrivalTime + Constants.W);
                } else {
                    if (MicroCluster.inCluster_objects.contains(o) || fromCluster == false) o.exps
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
        });

    }

    public void process_data(MCObject d, int currentTime, boolean fromCluster) {

        
        long startTime = Utils.getCPUTime();
        MTreeClass.Query query = mtree.getNearestByRange(d, Constants.R * 3 / 2);
        MesureMemoryThread.timeForQuerying += Utils.getCPUTime() - startTime;
        // ed

        double min_distance = Double.MAX_VALUE;

        MTreeClass.ResultItem ri = null;

        boolean isFoundCluster = false;

        if (query.iterator().hasNext()) {
            ri = query.iterator().next();

            min_distance = ri.distance;

            if (micro_clusters.get((MCObject) ri.data) != null) isFoundCluster = true;
        }

        if (min_distance <= Constants.R / 2 && isFoundCluster) {
            // assign to this closet cluster
            MCObject closest_cluster = (MCObject) ri.data;
            addObjectToCluster(d, closest_cluster, fromCluster);
        }

        else {

            /**
             * do range query in PD and mtree (distance to center <= 3/2R)
             */

            ArrayList<MCObject> neighbor_in_mtree = new ArrayList<>();
            ArrayList<MCObject> neighbor_in_PD = new ArrayList<>();
            ArrayList<MCObject> neighbor_in_3_2Apart_PD = new ArrayList<>();
            ArrayList<MCObject> neighbor = new ArrayList<>();
            ArrayList<MCObject> neighbor_in_R2 = new ArrayList<>();

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

                ArrayList<MCObject> object_in_cluster = micro_clusters.get(ri2.data);
                if (object_in_cluster != null) for (MCObject o : object_in_cluster) {

                    if (mtree.getDistanceFunction().calculate(d, o) <= Constants.R) {
                        neighbor_in_mtree.add(o);
                    }
                }

            }
            PD.stream().forEach((m) -> {
                double distance = mtree.getDistanceFunction().calculate(d, m);

                if (distance <= Constants.R / 2) neighbor_in_R2.add(m);
                if (distance <= Constants.R) {
                    neighbor_in_PD.add(m);
                    neighbor_in_3_2Apart_PD.add(m);
                } else if (distance <= Constants.R * 3 / 2) neighbor_in_3_2Apart_PD.add(m);
            });
            neighbor.addAll(neighbor_in_PD);
            neighbor.addAll(neighbor_in_mtree);

            neighbor_in_PD.stream().map((o) -> {
                if (o.arrivalTime < d.arrivalTime) {
                    if (MicroCluster.inCluster_objects.contains(o) || fromCluster == false) o.numberOfSucceeding++;
                    d.exps.add(o.arrivalTime + Constants.W);
                } else {
                    if (MicroCluster.inCluster_objects.contains(o) || fromCluster == false) o.exps
                            .add(d.arrivalTime + Constants.W);
                    d.numberOfSucceeding++;
                }
                /**
                 * check for o becomes inlier
                 */
                return o;
            }).filter((o) -> (o.numberOfSucceeding + o.exps.size() >= Constants.k && outlierList.contains(o))).map((o) -> {
                outlierList.remove(o);
                return o;
            }).map((o) -> {
                if (o.exps.size() > 0) o.ev = min(o.exps);
                return o;
            }).forEach((o) -> {
                eventQueue.add(o);
            });
            neighbor_in_mtree.stream().forEach((o) -> {
                if (o.arrivalTime < d.arrivalTime) {
                    d.exps.add(o.arrivalTime + Constants.W);
                } else {
                    d.numberOfSucceeding++;
                }
            });

            if (neighbor_in_R2.size() > Constants.k * 1.1) {

                // form cluster
                d.isCenter = true;
                d.isInCluster = true;
                neighbor_in_R2.add(d);
                for (MCObject o : neighbor_in_R2) {
                    o.cluster = d;
                    o.isInCluster = true;
                    o.numberOfSucceeding = 0;
                    o.exps.clear();
                    PD.remove(o);
                    eventQueue.remove(o);
                    outlierList.remove(o);
                }
                micro_clusters.put(d, neighbor_in_R2);
                long startTime2 = Utils.getCPUTime();
                mtree.add(d);
                MesureMemoryThread.timeForIndexing += Utils.getCPUTime() - startTime2;
                // update Rmc for points in PD
                neighbor_in_3_2Apart_PD.stream().forEach((o) -> {
                    o.Rmc.add(d);
                });
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

        Utils.computeUsedMemory();
    }

    private void process_event_queue(ArrayList<MCObject> expireData, int currentTime) {
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

        expireData.stream().forEach((p) -> {
            outlierList.remove(p);
        });

        outlierList.stream().map((outlierList1) -> (MCObject) outlierList1).forEach((d) -> {
            for (int k = d.exps.size() - 1; k >= 0; k--) {
                if (d.exps.get(k) <= currentTime) d.exps.remove(k);
            }
        });

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

        exps = new ArrayList<>();
        objectInCluster = new ArrayList<>();
        Rmc = new ArrayList<>();
    }

}