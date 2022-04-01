/*
You parse the information and read data from the bin files
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Array;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Parser {
    private RandomAccessFile mergeFile;
    private RandomAccessFile firstMergeFile;
    private ArrayList<Record> minList = new ArrayList<>();
    private byte[] outputBuffer;
    private int outBufferSize;
    private int reach;
    private File a;

    // the constructor of parser and you can add more here if you need to
    public Parser() throws IOException, FileNotFoundException {
        mergeFile = new RandomAccessFile("mergeFinal.bin", "rw");
        firstMergeFile = new RandomAccessFile("mergeFirst.bin", "rw");
         a = new File("mergeFinal.bin");
    }

    //while there are runs in info ArrayList
    public void parseFile(String fileToParse, String infoToParse)
            throws IOException, FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(fileToParse, "r");
        File b = new File(fileToParse);
        int position;
        int length;

        /*
        Since start point and length are both integers, you will
        use readInt here. Remember 1 Integer = 4bytes,
        which enlarge the size of file
         */

        //read and parse by Merge Info
        RandomAccessFile ifrd = new RandomAccessFile(infoToParse, "r");
        ArrayList<MergeInfo> infoList = new ArrayList<MergeInfo>();
        outputBuffer = new byte[8192];

        // read pos/len of each run info into a Merge object, then into infoList arrayList
        for (int i = 0; i < ifrd.length() / 8; i++) {
            position = ifrd.readInt();
            length = ifrd.readInt();
            MergeInfo merge = new MergeInfo(position, length);
            infoList.add(merge);
        }

        System.out.println("infoList size: " +infoList.size());

        int pass = 0;
        while (infoList.size() > 1) {
            raf = new RandomAccessFile(fileToParse, "r");
            int numOfMerges = infoList.size() / 8;
            System.out.println("num of merges: " + numOfMerges);
            int leftOverMerge = infoList.size() % 8;
            ArrayList<MergeInfo> infoList2 = new ArrayList<>();
            reach = infoList.size(); // reach tracks how many runs are left to read. starts at its max & will decrement ?

            // will merge all the runs divisible by 8
            for (int i = 0; i < numOfMerges; i++) {
                System.out.println("Calling mergeRun in for loop");
                minList.clear();
                //setting up for later use
                int leftStart = infoList.get(i*8).getStart();
                int totalLeng = 0;
                //getting the length of the 8 runs
                for(int j = 0; j < 8; j++){
                    totalLeng += infoList.get(i*8+j).getLength();
                }
                infoList2.add(new MergeInfo(leftStart,totalLeng));
                mergeRun(raf, infoList, i * 8, 8);
            }

            // will merge the leftover runs with the previously merged runs
            if (leftOverMerge != 0) {
                System.out.println("Calling mergeRun in leftover for loop leftover: " + leftOverMerge);
                int leftStart = infoList.get(numOfMerges*8).getStart();
                int totalLeng = 0;
                //getting the length of the 8 runs
                for(int j = 0; j < leftOverMerge; j++){
                    totalLeng += infoList.get(numOfMerges*8+j).getLength();
                }
                infoList2.add(new MergeInfo(leftStart,totalLeng));
                minList.clear();
                mergeRun(raf, infoList, numOfMerges * 8, leftOverMerge);
                numOfMerges++;
            }

            // will update infoList to reflect new merges

            infoList = infoList2;
            System.out.println("the size of list now" + infoList.size());

            // writes to the first merge file only on the first pass through
            pass ++;
            if(pass == 1){
                firstMergeFile.write(outputBuffer);
            }
        }

        fileSwitch(fileToParse,"mergeFinal.bin" );

        System.out.println("Total raf.length: " + raf.length());
    }

    public void mergeRun(RandomAccessFile raf, ArrayList<MergeInfo> infoList, int start, int numOfRuns) throws IOException, FileNotFoundException {
        System.out.println("---------new run of mergeRun--------");
        reach = numOfRuns;
        System.out.println("reach: " + reach);
        while(reach > 1) { // will loop until there are no more runs left to merge

            // will iterate over the number of runs (most often 8), getting the smallest record from each
            if(minList.isEmpty()) {
                 readMinList(-1, raf, infoList, start, numOfRuns);
            }

            // Find min key value and min index in toSort
            Record min = new Record(new byte[16]);
            for(int j = 0; j < minList.size(); j++){
                if (!minList.get(j).getLastOne()){
                    min = minList.get(j);
                    break;
                }
            }
            int minIndex = -1;
            for (int j = 0; j < minList.size(); j++) {
                if (!minList.get(j).lastOne){
                    if (minList.get(j).compareTo(min) <= 0){
                        min = minList.get(j);
                        minIndex = j;
                    }
                }
            }


            //Read in the smallest record to output buffer byte by byte
            for (int m = 0; m < 16; m++) {
                    outputBuffer[outBufferSize] = minList.get(minIndex).getWholeRecord()[m];
                    outBufferSize++;
            }

            // call writeToFile which does further checks
            readMinList(minIndex, raf, infoList, start, numOfRuns);


            writeToFile(numOfRuns, minIndex);

        }
        for (int j = 0; j < numOfRuns; j++){
            if(infoList.get(j).getLength() != 0){

                //there is the last one remaining in the minList
                int startPoint = infoList.get(j).getStart() - 16;
                int lengthLong = infoList.get(j).getLength() + 16;
                System.out.println("start point" + startPoint);
                if (minList.isEmpty()){
                    startPoint+=16;
                    lengthLong-=16;
                }
                byte[] temp = new byte[lengthLong];
                raf.seek(startPoint);
                raf.read(temp);
                mergeFile.write(temp);
            }
        }
    }

    public void readMinList(int minIndex, RandomAccessFile raf, ArrayList<MergeInfo> infoList, int start, int numOfRuns) throws IOException, FileNotFoundException{
        //If minList is empty read in min record from each run to minList
        if(reach >1) {
            if (minList.isEmpty()) {
                for (int i = 0; i < numOfRuns; i++) {
                    // if the run is not empty, then read the next record from said run
                    if (infoList.get(start + i).getLength() != 0) {
                        int currFilePointer = infoList.get(start + i).getStart();
                        raf.seek(currFilePointer);

                        // read in one record of the run
                        byte[] tempRec = new byte[16];
                        raf.read(tempRec);
                        Record myRec1 = new Record(tempRec);
                        minList.add(myRec1);
                        infoList.get(start + i).changeValues();


                    }
                }
            }
            else {

                if (infoList.get(start + minIndex).getLength() != 0) {
                    int currFilePointer = infoList.get(start + minIndex).getStart();
                    raf.seek(currFilePointer);

                    // read in one record of the run
                    byte[] tempRec = new byte[16];
                    raf.read(tempRec);
                    Record myRec1 = new Record(tempRec);

                    //adds to specified index
                    minList.remove(minIndex);
                    minList.add(minIndex, myRec1);
                    infoList.get(start + minIndex).changeValues();

                    Double check = myRec1.getKey();

                } else { // Run is empty, so subtract one from reach
                    reach--;
                    minList.get(minIndex).isLastOne();
                    System.out.println("get here");
                    System.out.println("Reach: " + reach);
                    for(int l=0; l<infoList.size(); l++){
                        System.out.println("index " + l +  " start: " + infoList.get(l).getStart() + " length: " + infoList.get(l).getLength());
                    }
                }
            }
        }
    }

    public void writeToFile(int numOfRuns, int minIndex) throws IOException, FileNotFoundException {
        // if the num of runs already merged == the number of total runs (aka all is merged)
        if (outBufferSize == 8192){
            mergeFile.write(outputBuffer);
            outputBuffer = new byte[8192];
            outBufferSize = 0;
        }

        // if there is only one more run to merge
        else if(reach == 1){
                byte []temp1;
                temp1 = Arrays.copyOfRange(outputBuffer, 0, outBufferSize);
                mergeFile.write(temp1);
        }
    }

    //call after each pass
    public void fileSwitch(String target, String origin){
        File merge = new File(origin);
        File rename = new File(target);
        rename.delete();
        merge.renameTo(rename);
    }

}