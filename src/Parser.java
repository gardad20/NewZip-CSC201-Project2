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
    //output buff as class field

    // the constructor of parser and you can add more here if
    // you need to
    public Parser() throws IOException, FileNotFoundException {
        mergeFile = new RandomAccessFile("mergeFinal.bin", "rw");
        firstMergeFile = new RandomAccessFile("mergeFirst.bin", "rw");
    }

    //while there are runs in info ArrayList
    public void parseFile(String fileToParse, String infoToParse)
            throws IOException,
            FileNotFoundException {
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
        byte[] outputBuff = new byte[8192];

        // read pos/len info into a Merge object, then into infoList arrayList
        for (int i = 0; i < ifrd.length() / 8; i++) {
            position = ifrd.readInt();
            length = ifrd.readInt();
            MergeInfo merge = new MergeInfo(position, length);
            infoList.add(merge);
        }

        int pass = 0;
        while (infoList.size() > 1) {
            int numOfMerges = infoList.size() / 8;
            int leftOverMerge = infoList.size() % 8;

            for (int i = 0; i < numOfMerges; i++) {
                mergeRun(raf, infoList, outputBuff, i * 8, 8);
            }

            if (leftOverMerge != 0) {
                mergeRun(raf, infoList, outputBuff, numOfMerges * 8, leftOverMerge);
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

            }
        }

        //copy mergerist to mergefile

    }

    public byte[] mergeRun(RandomAccessFile raf, ArrayList<MergeInfo> infoList, byte[] output, int start, int numOfRuns) throws IOException, FileNotFoundException {
        ArrayList<Record> toSort = new ArrayList<>();

        //check if output is full, if so write to firstmergefile

        for (int i = 0; i < infoList.size(); i++) {
            byte[] tempRec = new byte[16];
            raf.readFully(tempRec, infoList.get(i).getStart(), 16);
            Record myRec1 = new Record(tempRec);
            toSort.add(myRec1);
        }

        //Find min key value and min index in toSort
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int k = 0; k < toSort.size(); k++) {
            for (int j = 0; j < toSort.size(); j++) {
                if (toSort.get(j).getKey() < min) {
                    min = toSort.get(j).getKey();
                    minIndex = j;
                }
            }
        }

        raf.readFully(output, infoList.get(minIndex).getStart(), 16);
        infoList.get(minIndex).changeValues();

        // 1) how do we keep track of output buff length?

        // 2) what to do when one run is empty?

        return output;
    }


    //you can process the files you read in here and call your multiway merge
    public void run() {

    }
}