package mtree.tests;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import mtree.utils.Constants;

public class Test {
    public static void main(String[] args){
        
        String filename = "C:\\Users\\Luan\\MTree\\errorList.txt";
        Writer writer = null;
      try {
          writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "utf-8"));
          int[] changePoints = new int[]{101, 401, 701, 1001};
          for(int i: changePoints){
              for(int j=0; j<= 49; j++){
                  writer.write((i+j)+"\n");
                  
              }
          }
      } catch (IOException ex) {
        // report
          System.out.println(ex.getMessage());
      } finally {
         try {writer.close();} catch (Exception ex) {}
      }
    }

}
