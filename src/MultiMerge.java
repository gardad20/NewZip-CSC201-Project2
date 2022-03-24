/*
You should have your Multiway Merge methods here
 */

//do everything here
//1) Make min heap (each node is a record)
//2) Parse and add to min heap
//3) Sort
// merge by comparing first of each block (use min heap to get first in block)
//length is one 8-way merge (DSA 14.2)
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
public class MultiMerge {

    /*
        should take two (already internally sorted) arrayLists, merge together, return one sorted arrayList
        compare first record of each arrayList, see which is smaller, then add the smaller to the output buffer
            then, the output buffer is written to another file
     */
    public MultiMerge(){}

    //Takes an array of Arraylists and merges them (Not sure if correct)
    public static Record[] merge(ArrayList<Record> toSort, ArrayList<MergeInfo> mergeList, Record[] outputBuff){

        //check that output buff is not full and that none of the runs are empty
        int elemInOutputBuff = 0;
        for(int i=0; i<outputBuff.length;i++){
            if(outputBuff[i]!=null){
                elemInOutputBuff++;
            }
        }

        // will find & add each min value to the output buffer in order
         for (int k = 0; k < toSort.size(); k++){
             // finds Min value
             //why 1?
             if(elemInOutputBuff!=1) {
                 double min = Double.MAX_VALUE;
                 int minIndex = -1;
                 for (int j = 0; j < toSort.size(); j++) {
                     if (toSort.get(j).getKey() < min) {
                         min = toSort.get(j).getKey();
                         minIndex = j;
                     }
                 }
                 

                 //writes min value to output buffer
                 outputBuff[k] = toSort.get(minIndex); //should this be written to a diff output buffer?
                 // PROBLEM: This whole merge method WAS only adding one val (the min)
                 //          to the outputBuff, not EVERY value in sorted ascending order.
                 //          I moved this section inside a loop, which hopefully should help.


                 //updates position and length in merge info list
                 mergeList.get(minIndex).changeValues();
                 // do we need to switch the order of writing to output buff and updating vals??

                 //decreases the size of toSort so that the minVal won't be checked next iteration
                 toSort.remove(minIndex);
             }

         }
         return outputBuff;
    }

    public void mergeSort(ArrayList<MergeInfo> mergeList){
        int numOfRun = mergeList.size()/8;
        int leftOverMerge = mergeList.size()%8;

        for(int i=0; i<numOfRun; i++){

        }
    }

//    public static Record findMin(ArrayList<Record>[] toSort){
//        for(int i=0; i< toSort.length; i++){
//            Arrays.sort(toSort[i].toArray());
//        }
//        return toSort.get(0);
//    }

}
