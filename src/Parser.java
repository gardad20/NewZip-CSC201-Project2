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

    // The constructor of parser
    public Parser() throws IOException, FileNotFoundException {
        mergeFile = new RandomAccessFile("mergeFinal.bin", "rw");
        firstMergeFile = new RandomAccessFile("mergeFirst.bin", "rw");
        a = new File("mergeFinal.bin");
    }

    public void parseFile(String fileToParse, String infoToParse)
            throws IOException, FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(fileToParse, "r");
        File b = new File(fileToParse);
        RandomAccessFile ifrd = new RandomAccessFile(infoToParse, "r");
        ArrayList<MergeInfo> infoList = new ArrayList<MergeInfo>();
        outputBuffer = new byte[8192];
        int position;
        int length;


        // Loops through the info file reading MergeInfo objects into infoList ArrayList
        for (int i = 0; i < ifrd.length() / 8; i++) {
            position = ifrd.readInt();
            length = ifrd.readInt();
            MergeInfo merge = new MergeInfo(position, length);
            infoList.add(merge);
        }

        // Variable to count the number of passes
        int pass = 0;

        // Repeats the merge/writing process while there is more than one object in infoList
        while (infoList.size() > 1) {
            raf = new RandomAccessFile(fileToParse, "r");
            int numOfMerges = infoList.size() / 8;
            int leftOverMerge = infoList.size() % 8;
            ArrayList<MergeInfo> infoList2 = new ArrayList<>();

            // Reach tracks how many runs are left to read
            reach = infoList.size();

            // Merges all the runs divisible by 8
            for (int i = 0; i < numOfMerges; i++) {
                minList.clear();
                int leftStart = infoList.get(i*8).getStart();
                int totalLeng = 0;

                // Gets the total length of the runs
                for(int j = 0; j < 8; j++){
                    totalLeng += infoList.get(i*8+j).getLength();
                }
                infoList2.add(new MergeInfo(leftStart,totalLeng));
                mergeRun(raf, infoList, i * 8, 8);
            }

            // Merges the leftover runs that did not divide evenly into 8
            if (leftOverMerge != 0) {
                int leftStart = infoList.get(numOfMerges*8).getStart();
                int totalLeng = 0;

                // Gets the total length of the runs
                for(int j = 0; j < leftOverMerge; j++){
                    totalLeng += infoList.get(numOfMerges*8+j).getLength();
                }

                // Adds MergeInfo for new merge to infoList
                infoList2.add(new MergeInfo(leftStart,totalLeng));
                minList.clear();
                mergeRun(raf, infoList, numOfMerges * 8, leftOverMerge);
                numOfMerges++;
            }

            // Updates the MergeInfo in infoList to reflect new merges
            infoList = infoList2;

            // Writes to the first merge file on the first pass through
            pass ++;
            if(pass == 1){
                firstMergeFile.write(outputBuffer);
            }
        }

        // Switches the output file and the original input file
        fileSwitch(fileToParse,"mergeFinal.bin" );
    }

    // Method that merges records from a specified number of runs at a given start point
    public void mergeRun(RandomAccessFile raf, ArrayList<MergeInfo> infoList, int start, int numOfRuns) throws IOException, FileNotFoundException {
        reach = numOfRuns;

        // Conducts multiway merge when there is more than one run left
        while(reach > 1) {
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

            // Find smallest value and corresponding index in minList
            int minIndex = -1;
            for (int j = 0; j < minList.size(); j++) {
                if (!minList.get(j).lastOne){
                    if (minList.get(j).compareTo(min) <= 0){
                        min = minList.get(j);
                        minIndex = j;
                    }
                }
            }


            // Read in the smallest record to output buffer byte by byte
            for (int m = 0; m < 16; m++) {
                    outputBuffer[outBufferSize] = minList.get(minIndex).getWholeRecord()[m];
                    outBufferSize++;
            }

            // Reads in new values to the minList
            readMinList(minIndex, raf, infoList, start, numOfRuns);

            // Call writeToFile which does further checks
            writeToFile(numOfRuns, minIndex);
        }

        // Handles the situation where there is only one remaining run in minList
        for (int j = 0; j < numOfRuns; j++){
            if(infoList.get(j).getLength() != 0){
                int startPoint = infoList.get(j).getStart() - 16;
                int lengthLong = infoList.get(j).getLength() + 16;
                if (minList.isEmpty()){
                    startPoint+=16;
                    lengthLong-=16;
                }
                byte[] temp = new byte[lengthLong];
                raf.seek(startPoint);
                raf.read(temp);
                // Writes the last value
                mergeFile.write(temp);
            }
        }
    }


    // Method that reads in new values to the minList given the number of runs, start position, and index of minimum value
    public void readMinList(int minIndex, RandomAccessFile raf, ArrayList<MergeInfo> infoList, int start, int numOfRuns) throws IOException, FileNotFoundException{
        // If minList is empty read in min record from each run to minList
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
            // minList is not empty, so only read in the new value from the run corresponding with minIndex
            else {
                if (infoList.get(start + minIndex).getLength() != 0) {
                    int currFilePointer = infoList.get(start + minIndex).getStart();
                    raf.seek(currFilePointer);

                    // Read in one record of the run
                    byte[] tempRec = new byte[16];
                    raf.read(tempRec);
                    Record myRec1 = new Record(tempRec);

                    // Adds new value specified index
                    minList.remove(minIndex);
                    minList.add(minIndex, myRec1);
                    infoList.get(start + minIndex).changeValues();

                // Run is empty, so subtract one from reach
                } else {
                    reach--;
                    minList.get(minIndex).isLastOne();
                }
            }
        }
    }

    // Method that checks conditions and writes the outputBuffer to mergeFinal file accordingly
    public void writeToFile(int numOfRuns, int minIndex) throws IOException, FileNotFoundException {
        // If the num of runs already merged == the number of total runs, write outputBuffer to mergeFinal
        if (outBufferSize == 8192){
            mergeFile.write(outputBuffer);
            outputBuffer = new byte[8192];
            outBufferSize = 0;
        }

        // If there is only one more run to merge, write outputBuffer to mergeFinal
        else if(reach == 1){
                byte []temp1;
                temp1 = Arrays.copyOfRange(outputBuffer, 0, outBufferSize);
                mergeFile.write(temp1);
        }
    }

    // Method that switches the output file and the original input file
    public void fileSwitch(String target, String origin){
        File merge = new File(origin);
        File rename = new File(target);
        rename.delete();
        merge.renameTo(rename);
    }
}