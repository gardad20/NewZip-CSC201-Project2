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

    // the constructor of parser and you can add more here if
    // you need to
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
        outBufferSize = 0;
        outputBuffer = new byte[8192];

        // read pos/len info into a Merge object, then into infoList arrayList
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
            reach = numOfMerges; //starts the reach count at its max, since it will decrement

            for (int i = 0; i < numOfMerges; i++) {
                mergeRun(raf, infoList, i * 8, 8);
            }

            if (leftOverMerge != 0) {
                mergeRun(raf, infoList, numOfMerges * 8, leftOverMerge);
                numOfMerges++;
            }

            ArrayList<MergeInfo> infoList2 = new ArrayList<>();

            for (int i = 0; i < numOfMerges; i++) {
                infoList2.add(infoList.get(i));
            }

            infoList = infoList2;
            //isWriteToMerge =! isWriteToMerge

            pass ++;
            if(pass == 1){
                firstMergeFile.write(outputBuffer);
            }
        }
        System.out.print(mergeFile);
    }

    public void mergeRun(RandomAccessFile raf, ArrayList<MergeInfo> infoList, int start, int numOfRuns) throws IOException, FileNotFoundException {
        ArrayList<Record> minList = new ArrayList<>();

        for (int i = 0; i < numOfRuns; i++) {
            // check condition of this loop -- how to use start/numOfRuns parameters
            // used to be infoList.size() which loops by how many total runs exist

            if (infoList.get(start+i).getLength() != 0) { //if the run is not empty, then read the next record from said run
                long currFilePointer = infoList.get(i).getStart();
                raf.seek(currFilePointer);

                // read in one record of the run
                byte[] tempRec = new byte[16];
                raf.readFully(tempRec);
                Record myRec1 = new Record(tempRec);
                minList.add(myRec1);
                System.out.println("current MinList size: " + minList.size());

            } else { // Run is empty, so subtract one from reach
                reach--;
                // reach = how many runs are left to read
                System.out.println("Reach: "+reach);
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

        //System.out.println("pls");
        raf.seek(infoList.get(minIndex).getStart()); //added in new
        //raf.readFully(outputBuffer, infoList.get(minIndex).getStart(), 16); // read the smallest Record to the output buffer

        for (int m = 0; m < 16; m++){
            outputBuffer[outBufferSize] = infoList.get(minIndex).getWholeRecord();
            outBufferSize++;
            //need to trace/make sure that this pseudocode actually tracks with our stuff
        }

        infoList.get(minIndex).changeValues();
        writeToFile(numOfRuns);

        // create minlist<Record> = 8 elements for 8way merge
        //use minList to merge outside of this loop
        while (numOfRuns > 1){ //or (Reach !=numOfRuns-1)
            //findMin method to check for min inside MinList
            // should return the index of the smallest (tell us which run has the smallest one)
            // if (runInfo.get(index + start).getLength !=0) {
            // write() }
            // else { make that element as a null or does not let it compare with other el.s
            // //reach ++
            // write()
            // }
        }




         // DOES THIS CHUNK NEED TO ITERATE MORE OFTEN? weren't we only reading in ONE min value per call to mergeRun?
//        for (int i = 0; i < numOfRuns; i++) {
//            if (infoList.get(start+i).getLength() != 0) { //if the run is not empty, then read in the next min
//                System.out.println("pls");
//
//                raf.seek(infoList.get(minIndex).getStart()); //added in new
//                raf.readFully(outputBuffer, infoList.get(minIndex).getStart(), 16); // read the smallest Record to the output buffer
//
//
//                // infoList.get(minIndex) --> do the infoList and toSort indices match up? esp if
//                outBufferSize++;
//                infoList.get(minIndex).changeValues();
//                writeToFile(numOfRuns);
//            }
//        }




            // 2) what to do when one run is empty? --> merge all 8 completely together, then next 8

            // 3) how to deal with two different cases (how many merges) (aka. start and numOfRuns parameters)?

            // 4) should we be writing to raf (as the original input file) --> when mergeFile is the same size as raf?


//        return output;
    }


    public void writeToFile(int numOfRuns) throws IOException, FileNotFoundException {
        if (outBufferSize == numOfRuns){ // if the num of runs already merged == the number of total runs (aka all is merged)
            mergeFile.write(outputBuffer);
            outputBuffer = new byte[8192];
        } else if(outBufferSize == reach){ // if the nums of runs already merged == number of runs left to merge
            mergeFile.write(outputBuffer);
            outputBuffer = new byte[8192];
        } else if(reach == 1){              // if there is only one more run to merge
            mergeFile.write(outputBuffer);
            outputBuffer = new byte[8192];
        }

    }

    //you can process the files you read in here and call your multiway merge
    public void run() {

    }
}