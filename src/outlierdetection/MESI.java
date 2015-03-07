package outlierdetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mtree.tests.Data;
import mtree.utils.Constants;

public class MESI {

    public static Window window = new Window();

    public ArrayList<Data> detectOutlier(ArrayList<Data> data, int currentTime, int W, int slide) {
        
        if(data.size() == Constants.W){
            //split into slides
            int numSlide = Constants.W / Constants.slide;
            for(int i = 0; i < numSlide; i++){
                
                ArrayList<Data> d = new ArrayList<Data>();
                for(int j = 0; j < Constants.slide; j++){
                    d.add(data.get(i*Constants.slide + j ));
                }
                Slide s = new Slide(d, currentTime);
                window.addNewSlide(s);
            }
            
        }
        else if(data.size() == Constants.slide)
        {
            //add this slide to window
            Slide s = new Slide(data, currentTime);
            window.addNewSlide(s);
            
        }
        
        
        
        return null;

    }
}

class Window {

    public ArrayList<Slide> slides = new ArrayList<Slide>();
    public int startSlide;

    public Slide getNewestSlide() {
        if (slides.size() == 0) return null;
        return slides.get(slides.size() - 1);
    }

    public Window() {

        startSlide = 0;

    }

    public void addNewSlide(Slide s) {
        Slide newestSlide = getNewestSlide();
        if (newestSlide != null) s.id = newestSlide.id + 1;
        else s.id = 0;
        s.window = this;
        slides.add(s);
        startSlide++;
    }

}

class Slide {

    public ArrayList<MESIObject> points = new ArrayList<MESIObject>();
    public int id;

    public Window window;
    public ArrayList<MESIObject> triggered = new ArrayList<MESIObject>();

    public Slide(ArrayList<Data> data, int currentTime) {
        for (Data d : data) {
            points.add(new MESIObject(d, currentTime));
        }
    }

    public void updateSkippedSlide() {
        for (MESIObject p : triggered) {
            for (int i = window.startSlide; i < id; i++) {
                p.skippedSlide.add(window.slides.get(i));
            }
        }
    }

}

class MESIObject extends Data {
    public boolean isOutlier;
    public boolean isSafe;

    HashMap<Slide,Integer> preEvidence = new HashMap<Slide,Integer>();
    public int numSucEvidence;

    public ArrayList<Slide> skippedSlide = new ArrayList<Slide>();

    public MESIObject(Data d, int currentTime) {
        super();
        this.arrivalTime = d.arrivalTime;
        this.values = d.values;

    }

}
