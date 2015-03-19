package outlierdetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import mtree.tests.Data;
import mtree.utils.Constants;

public class MESI {

    /**
     *
     */
    public static HashSet<Data> outlierList = new HashSet<>();
    public static Window window = new Window();

    public HashSet<Data> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {

        if (data.size() == Constants.W) {
            // split into slides
            int numSlide = Constants.W / Constants.slide;
            for (int i = 0; i < numSlide; i++) {

                ArrayList<Data> d = new ArrayList<>();
                for (int j = 0; j < Constants.slide; j++) {
                    d.add(data.get(i * Constants.slide + j));
                }
                Slide s = new Slide(d, currentTime);
                window.addNewSlide(s);

            }

        } else if (data.size() <= Constants.slide) {
            // add this slide to window
            Slide s = new Slide(data, currentTime);
            window.addNewSlide(s);

        }
        Thresh_LEAP(window);

        for (int i = window.startSlide; i < window.slides.size(); i++) {
            if (i >= 0) {
                for (MESIObject o : window.slides.get(i).points) {
                    if (o.isOutlier) {
                        outlierList.add(o);
                    }
                }
            }
        }
//         print_window();
//         print_outlier();

        return outlierList;

    }

    public void Thresh_LEAP(Window window) {

        if (window.slides.size() <= Constants.W / Constants.slide) {
            window.slides.stream().forEach((s) -> {
                s.points.stream().forEach((p) -> {
                    LEAP(p, window);
                });
            });
        } else {
            window.getNewestSlide().points.stream().forEach((p) -> {
                LEAP(p, window);
            });
        }

        Slide expiredSlide = window.getExpiredSlide();

        if (expiredSlide != null) {
            /**
             * clear expired slides
             */
            for (int i = 0; i < expiredSlide.id; i++) {
                if (window.slides.get(i) != null) {
                    window.slides.set(i, null);
                }
            }

            if (expiredSlide.triggered != null) {
                expiredSlide.triggered.stream().filter((p) -> (!p.isSafe)).map((p) -> {
                    p.expireEvidence(expiredSlide, window);
                    return p;
                }).forEach((p) -> {
                    // compute skipped slide for p
                    LEAP(p, p.getSkippedPoints(window, expiredSlide));
                });
            }
            expiredSlide.points.clear();
//            window.slides.subList(0, expiredSlide.id).clear();
        }

//        Utils.computeUsedMemory();
    }

    public boolean LEAP(MESIObject p, Window window) {

        boolean isOutlier = false;
        if (p.preEvidence == null) {
            p.numSucEvidence = 0;
            p.preEvidence = new HashMap<>();
        }

        int currentSlideIndex = (int) Math.floor((p.arrivalTime - 1) / Constants.slide);

        for (int i = currentSlideIndex; i <= window.getNewestSlide().id; i++) {

            Slide s = window.slides.get(i);
            HashSet<MESIObject> checkedPoints = new HashSet<>();
            for (MESIObject o : s.points) {
                if (o.arrivalTime != p.arrivalTime) {
                    checkedPoints.add(o);
//                    p.lastLEAPSlide = o.getCurrentSlideIndex();
                    p.lastLEAPSlide = i;
                    if (true == p.isNeighborhood(o)) {
                        p.updateSuccEvidence();
                        if (p.isMESIAquired()) {
                            p.isOutlier = false;

                            // check neighborhood of p with remaining points in the o's slide
                            for (MESIObject o2 : s.points) {

                                if (o2.arrivalTime > (p.lastLEAPSlide + 1) * Constants.slide) {
                                    break;
                                }
                                if (!checkedPoints.contains(o2) && o2.arrivalTime != p.arrivalTime && o2.getCurrentSlideIndex() == p.lastLEAPSlide && true == p.isNeighborhood(o2)) {
                                    p.updateSuccEvidence();
                                }
                            }
                            if (p.numSucEvidence >= Constants.k) {
                                p.isSafe = true;
                            }
                            return isOutlier;
                        }
                    }

                }
            }
        }

        List<Slide> precSlides = p.getPrecSlides(window);
        if (precSlides != null) {
            for (int i = 0; i < precSlides.size(); i++) {
                Slide slide = precSlides.get(i);
                // p.preEvidence.put(slide, 0);

                if (slide != null) {
                    for (MESIObject o : slide.points) {
                        if (true == p.isNeighborhood(o)) {
                            p.updatePrecEvidence(slide);
                            if (true == p.isMESIAquired()) {
                                p.isOutlier = false;
                                p.isSafe = false;
                                slide.updateTriggeredList(p);

                                return isOutlier;
                            }
                        }
                    }
                    /**
                     * update triggerlist if p is outlier
                     */
                    if (i == precSlides.size() - 1) {
                        slide.updateTriggeredList(p);
                    }
                }
            }
        }
//
//        isOutlier = true;
//        p.isOutlier = isOutlier;
        if (!p.isMESIAquired()) {
            p.isOutlier = true;
        }
        return isOutlier;

    }

    public void print_window() {
        System.out.println("Dat points: ");
        for (int i = window.startSlide; i < window.slides.size(); i++) {
            if (i >= 0) {
                for (MESIObject o : window.slides.get(i).points) {
                    System.out.print(o.values[0] + " ; ");
                }
            }
        }
        System.out.println();

    }

    public void print_outlier() {
        System.out.println("Outliers: ");
        outlierList.stream().forEach((o) -> {
            System.out.print(o.values[0] + " ; ");
        });
        System.out.println();
    }
}

class Window {

    public ArrayList<Slide> slides = new ArrayList<>();
    public int startSlide;

    public Slide getNewestSlide() {
        if (slides.isEmpty()) {
            return null;
        }
        return slides.get(slides.size() - 1);
    }

    public Window() {

        startSlide = -1;

    }

    public Slide getExpiredSlide() {

        if (slides.size() <= Constants.W / Constants.slide) {
            return null;
        } else {
            return slides.get(getNewestSlide().id - Constants.W / Constants.slide);
        }
    }

    public void addNewSlide(Slide s) {
        Slide newestSlide = getNewestSlide();
        if (newestSlide != null) {
            s.id = newestSlide.id + 1;
        } else {
            s.id = 0;
        }
        s.window = this;
        slides.add(s);
        if (slides.size() >= Constants.W / Constants.slide) {
            startSlide++;
        }
    }

}

class Slide {

    public List<MESIObject> points = new ArrayList<>();
    public int id;

    public Window window;
    public HashSet<MESIObject> triggered = new HashSet<>();

    public Slide(ArrayList<Data> data, int currentTime) {
        data.stream().forEach((d) -> {
            points.add(new MESIObject(d, currentTime));
        });
    }

    public void updateSkippedSlide() {
        triggered.stream().forEach((MESIObject p) -> {
            for (int i = window.startSlide; i < id; i++) {
                p.skippedSlide.add(window.slides.get(i));
            }
        });
    }

    public void updateTriggeredList(MESIObject p) {
//        if (!this.triggered.contains(p)) {
        this.triggered.add(p);
//        }
    }

}

class MESIObject extends Data {

    public boolean isOutlier;
    public boolean isSafe;
    public int lastLEAPSlide = 0;

    HashMap<Slide, Integer> preEvidence;
    public int numSucEvidence;
    public int numPreEvidence = -1;

    public ArrayList<Slide> skippedSlide = new ArrayList<>();

    public MESIObject(Data d, int currentTime) {
        super();
        this.arrivalTime = d.arrivalTime;
        this.values = d.values;

        numPreEvidence = -1;
    }

    public int getCurrentSlideIndex() {
        return (int) Math.floor((arrivalTime - 1) / Constants.slide);
    }

    public Window getSkippedPoints(Window window, Slide trigger) {
        Window result = new Window();
        result.slides = window.slides;
        // int currentSlideIndex = (int) Math.floor((arrivalTime - 1) / Constants.slide);
        // // if (currentSlideIndex >= Constants.W / Constants.slide) {
        // // if (lastLEAPSlide == 0) lastLEAPSlide = currentSlideIndex;
        // // } else lastLEAPSlide = Constants.W / Constants.slide - 1;
        result.startSlide = lastLEAPSlide + 1;
        // this.lastLEAPSlide = result.getNewestSlide().id;
        return result;
    }

    public void expireEvidence(Slide s, Window window) {
//        this.preEvidence.remove(s);
        if(preEvidence.get(s)!=null) {
            this.numPreEvidence -= preEvidence.get(s);
        } else {
        }
        if (!this.isMESIAquired()) {
            this.isOutlier = true;
            this.isSafe = false;
            for (int i = s.id + 1; i < this.getCurrentSlideIndex(); i++) {
                window.slides.get(i).triggered.add(this);
            }
        }

    }

    public boolean isNeighborhood(MESIObject p) {
        // // if (test.contains(arrivalTime + "_" + p.arrivalTime)) {
        // // System.out.println("Chet!");
        // Runtime.getRuntime().exit(1);
        // }
        // test.add(arrivalTime + "_" + p.arrivalTime);

        MTreeClass mtree = new MTreeClass();
        return mtree.getDistanceFunction().calculate(this, p) <= Constants.R;
    }
    /*
     public ArrayList<MESIObject> getSuccPoint(Window window) {
     ArrayList<MESIObject> result = new ArrayList<>();
     int currentSlideIndex = (int) Math.floor((arrivalTime - 1) / Constants.slide);
     Slide currentSlide = window.slides.get(currentSlideIndex);
     currentSlide.points.stream().filter((o) -> (o.arrivalTime != this.arrivalTime && o.getCurrentSlideIndex() >= window.startSlide)).forEach((o) -> {
     result
     .add(o);
     });
     for (int i = currentSlideIndex + 1; i <= window.getNewestSlide().id; i++) {
     if (i >= window.startSlide) {
     result.addAll(window.slides.get(i).points);
     }
     }
     return result;
     }
     */

    public void updateSuccEvidence() {
        numSucEvidence++;
    }

    public void updatePrecEvidence(Slide s) {
        if (this.preEvidence.get(s) == null) {
            this.preEvidence.put(s, 1);
            numPreEvidence ++;
        } else {
            this.preEvidence.put(s, this.preEvidence.get(s) + 1);
            numPreEvidence ++;
        }
    }

    public boolean isMESIAquired() {

        if (numPreEvidence == -1) {
            int numPreceding = 0;

            numPreceding = preEvidence.keySet().stream().filter((s) -> (s.id >= MESI.window.startSlide)).map((s) -> preEvidence.get(s)).reduce(numPreceding, Integer::sum);

            numPreEvidence = numPreceding;
        }
        
        return numSucEvidence + numPreEvidence >= Constants.k;

    }

    List<Slide> getPrecSlides(Window window) {
        int currentSlideIndex = (int) Math.floor((arrivalTime - 1) / Constants.slide);

        if (currentSlideIndex > window.startSlide) {
            return window.slides.subList(window.startSlide, currentSlideIndex);
        } else {
            return null;
        }
    }

}
