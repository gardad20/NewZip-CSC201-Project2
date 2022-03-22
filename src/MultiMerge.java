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
    public static void merge(ArrayList<Record> toSort, ArrayList<MergeInfo> mergeList, int[] outputBuf){
        //check that output buff is not full and that none of the runs are empty
        int elemInOutputBuff = 0;
        for(int i=0; i<outputBuf.length;i++){
            if(outputBuf[i]!=0){
                elemInOutputBuff++;
            }
        }
        if(elemInOutputBuff!=1) {
            double min = Double.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i < toSort.size(); i++) {
                if (toSort.get(i).getKey() < min) {
                    min = toSort.get(i).getKey();
                    minIndex = i;
                }
            }
            //updates position and length in merge info list
            mergeList.get(minIndex).changeValues();
            //writes min value to output buffer
        }
    }

    public static Record findMin(ArrayList<Record>[] toSort){
        for(int i=0; i< toSort.length; i++){
            Arrays.sort(toSort[i].toArray());
        }
        return toSort.get(0);
    }

}
