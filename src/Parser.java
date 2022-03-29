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
import java.util.Collections;

public class Parser {
    private RandomAccessFile mergeFile;
    private RandomAccessFile firstMergeFile;
    private byte[] outputBuffer;
    private int outBufferSize;
    private int reach;

    // the constructor of parser and you can add more here if you need to
    public Parser() throws IOException, FileNotFoundException {
        mergeFile = new RandomAccessFile("mergeFinal.bin", "rw");
        firstMergeFile = new RandomAccessFile("mergeFirst.bin", "rw");
    }

    //while there are runs in info ArrayList
    public void parseFile(String fileToParse, String infoToParse)
            throws IOException, FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(fileToParse, "r");
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
            int numOfMerges = infoList.size() / 8;
            System.out.println("num of merges: " + numOfMerges);
            int leftOverMerge = infoList.size() % 8;
            reach = numOfMerges; // reach tracks how many runs are left to read. starts at its max & will decrement

            // will merge all the runs divisible by 8
            for (int i = 0; i < numOfMerges; i++) {
                mergeRun(raf, infoList, i * 8, 8);
            }

            // will merge the leftover runs with the previously merged runs
            if (leftOverMerge != 0) {
                mergeRun(raf, infoList, numOfMerges * 8, leftOverMerge);
                numOfMerges++;
            }

            // will update infoList to reflect new merges
            ArrayList<MergeInfo> infoList2 = new ArrayList<>();
            for (int i = 0; i < numOfMerges; i++) {
                infoList2.add(infoList.get(i));
            }
            infoList = infoList2;
            //isWriteToMerge =! isWriteToMerge

            // writes to the first merge file only on the first pass through
            pass ++;
            if(pass == 1){
                firstMergeFile.write(outputBuffer);
            }
        }
        //System.out.print(mergeFile);
    }

    public void mergeRun(RandomAccessFile raf, ArrayList<MergeInfo> infoList, int start, int numOfRuns) throws IOException, FileNotFoundException {
        ArrayList<Record> minList = new ArrayList<>();

        while(reach >= 1) { // will loop until there are no more runs left to merge

            // will iterate over the number of runs (most often 8), getting the smallest record from each
            for (int i = 0; i < numOfRuns; i++) {

                // if the run is not empty, then read the next record from said run
                if (infoList.get(start + i).getLength() != 0) {
                    long currFilePointer = infoList.get(i).getStart();
                    raf.seek(currFilePointer);

                    // read in one record of the run
                    byte[] tempRec = new byte[16];
                    raf.readFully(tempRec);
                    Record myRec1 = new Record(tempRec);
                    minList.add(myRec1);
                    infoList.get(i).changeValues();
                    //System.out.println("current MinList size: " + minList.size());

                } else { // Run is empty, so subtract one from reach
                    reach--;
                    System.out.println("Reach: " + reach);
                }

            }

            // Find min key value and min index in toSort
            double min = Double.MAX_VALUE;
            int minIndex = -1;
            for (int k = 0; k < minList.size(); k++) {
                for (int j = 0; j < minList.size(); j++) {
                    if (minList.get(j).getKey() < min) {
                        min = minList.get(j).getKey();
                        minIndex = j;
                    }
                }
            }

            //Read in the smallest record to output buffer byte by byte
            for (int m = 0; m < 16; m++) {
                if (outBufferSize <= 512) {
                    outputBuffer[outBufferSize + m] = minList.get(minIndex).getWholeRecord()[m];
                    outBufferSize++;
                }
                /*
                NEED TO TRACE/MAKE SURE THAT THIS PSEUDOCODE ACTUALLY TRACKS WITH OUR STUFF
                */
            }

            // call writeToFile which does further checks
            writeToFile(numOfRuns);
        }
    }



    public void writeToFile(int numOfRuns) throws IOException, FileNotFoundException {
        if (outBufferSize%16 == numOfRuns){ // if the num of runs already merged == the number of total runs (aka all is merged)
            mergeFile.write(outputBuffer);
            outputBuffer = new byte[8192];
        } else if(outBufferSize%16 == reach){ // if the nums of runs already merged == number of runs left to merge
            mergeFile.write(outputBuffer);
            outputBuffer = new byte[8192];
        } else if(reach == 1){              // if there is only one more run to merge
            mergeFile.write(outputBuffer);
            /*
                DO WE NEED TO WRITE ALL THE MERGED FILES AND THE ONE LEFT? AREN'T WE ONLY WRITING THE MERGED FILES HERE?
             */
            outputBuffer = new byte[8192];
        }

    }

}