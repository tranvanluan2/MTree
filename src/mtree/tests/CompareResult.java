package mtree.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.print.attribute.HashAttributeSet;

public class CompareResult {
    
    public static void main(String[] args) throws IOException{
        
        double[] result = compare("C:\\Users\\Luan\\MTree\\abstractC30_5.txt", "C:\\Users\\Luan\\MTree\\errorList.txt");
        System.out.println("Precison: "+result[0]);
        System.out.println("Recall: "+result[1]);
    }
    
    public static double[] compare(String filename1, String filename2) throws IOException{
        
        BufferedReader approx = new BufferedReader(new FileReader(new File(filename1)));

        BufferedReader exact = new BufferedReader(new FileReader(new File(filename2)));
        
        HashSet<Integer> approxValues = new HashSet<Integer>();

        HashSet<Integer> exactValues = new HashSet<Integer>();
        
        String line = "";
        while((line = approx.readLine())!=null){
            approxValues.add(Integer.valueOf(line.trim()));
            
        }
         line = "";
         while((line = exact.readLine())!=null){
             exactValues.add(Integer.valueOf(line.trim()));
             
         }
         
         double precision = 0;
         double recall = 0;
         
         for(Integer i:approxValues){
             
             if(exactValues.contains(i))
                 precision++;
         }
         for(Integer i: exactValues){
             if(approxValues.contains(i))
                 recall++;
         }
         precision = precision *1.0/ approxValues.size();
         recall = recall *1.0/ exactValues.size();
         return new double[]{precision, recall};
    }
}
