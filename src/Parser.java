/*
You parse the information and read data from the bin files
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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

        /*
        Since start point and length are both integers, you will
        use readInt here. Remember 1 Integer = 4bytes,
        which enlarge the size of file
         */
        RandomAccessFile ifrd = new RandomAccessFile(infoToParse, "r");

        //need to wrap this in some sort of loop
        int position = ifrd.readInt();
        int length = ifrd.readInt();

        byte[] inputBuff = new byte[8192]; // "a block is 8,192 bytes" according to project description

        raf.readFully(inputBuff, position, length); //use position and length to add first block to input buffer

        //now: add this block to RAM minheap

        //then: use seek to start getting next block from Run file
        long currFilePointer = position + length; //would this need to be manually converted to long?
        raf.seek(currFilePointer);



    }
    
    //you can process the files you read in here and call your mutiway-
    //merge
    public void run(){

    }
}
