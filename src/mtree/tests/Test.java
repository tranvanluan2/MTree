package mtree.tests;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import mtree.utils.Constants;
import mtree.utils.FibonacciHeap;

public class Test {
    public static void main(String[] args){
        
        
        FibonacciHeap<Integer> heaps = new FibonacciHeap<Integer>();

        
        
        FibonacciHeap.Node<Integer> node1 = heaps.insert(1);
        FibonacciHeap.Node<Integer> node3 = heaps.insert(3);
        FibonacciHeap.Node<Integer> node5 = heaps.insert(5);
        FibonacciHeap.Node<Integer> node7 = heaps.insert(7);
        FibonacciHeap.Node<Integer> node9 = heaps.insert(9);
        FibonacciHeap.Node<Integer> node10 = heaps.insert(10);
        FibonacciHeap.Node<Integer> node11 = heaps.insert(19);
        FibonacciHeap.Node<Integer> node12 = heaps.insert(29);
        FibonacciHeap.Node<Integer> node13 = heaps.insert(39);
        Integer min = heaps.extractMin().getKey();
        heaps.increaseKey(node3, 101);
        heaps.increaseKey(node5, 102);
        heaps.increaseKey(node7, 103);
        heaps.increaseKey(node9, 104);
//        heaps.increaseKey(node10, 104);
//        heaps.increaseKey(node11, 204);
         min = heaps.extractMin().getKey();
        System.out.println("Min = "+min);
        
        
        
}
}