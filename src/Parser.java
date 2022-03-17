/*
You parse the information and read data from the bin files
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;

public class Parser {
    private RandomAccessFile mergeFile;
    private RandomAccessFile firstMergeFile;

    // the constructor of parser and you can add more here if
    // you need to
    public Parser() throws IOException, FileNotFoundException {
        mergeFile = new RandomAccessFile("mergeFinal.bin", "rw");
        firstMergeFile = new RandomAccessFile("mergeFirst.bin", "rw");
    }

    public void parseFile(String fileToParse, String infoToParse)
            throws IOException,
            FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(fileToParse, "r");
        int position;
        int length;
        //How do get the length of raf as an int?
        ArrayList<Record>[] listArray = new ArrayList<Record>[8];
        /*
        Since start point and length are both integers, you will
        use readInt here. Remember 1 Integer = 4bytes,
        which enlarge the size of file
         */

        //read and parse by Merge Info
        RandomAccessFile ifrd = new RandomAccessFile(infoToParse, "r");
        ArrayList<Mergeinfo> infoList = new ArrayList<Mergeinfo>();

        for(int i=0; i<ifrd.length(); i++){
            position = ifrd.readInt();
            length = ifrd.readInt();
            Mergeinfo merge = new Mergeinfo(position, length);
            infoList.add(merge);
        }

        // array list instead of array -- read 8 or 16 bytes at a time
        ArrayList<Record> inBuff = new ArrayList<Record>(512);

        for (int j = 0; j < raf.length()/8; j++){
            byte[] inputBuff = new byte[8192]; // declare input buffer

            int currPosition = infoList.get(j).getFirst();
            int currLength = infoList.get(j).getLength();
            //use position and length to add first block to input buffer
            raf.readFully(inputBuff, currPosition, currLength);

            //add this block to arrayList -- THIS PART IS DEFINITELY NOT DONE/RIGHT lol
            Record myRec = new Record(inputBuff);

            inBuff.add(myRec); //needs to add in the correct spot

            //use seek to (reset file pointer position) & start getting next block from Run file
            long currFilePointer = currPosition + currLength; //would this need to be manually converted to long?
            raf.seek(currFilePointer);
        }
        Collections.sort(inBuff);

        for(int i=0; i<inBuff.size(); i++){
            System.out.println(inBuff.get(i).toString());
        }


        // 1. use pos/len to access data for each block
        // 2. each block is presented as a byte array --> then turn it to record
        // 3. create an arraylist of records
        // 4. then multiway merge array list

        // use a class for multiway merge (not particularly necessary but would be helpful)
        //      each merge returns a merged arrayList





    }
    
    //you can process the files you read in here and call your multiway merge
    public void run(){

    }
}
