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
//        ArrayList<Record> listArray = new ArrayList<Record>();
        /*
        Since start point and length are both integers, you will
        use readInt here. Remember 1 Integer = 4bytes,
        which enlarge the size of file
         */

        //read and parse by Merge Info
        RandomAccessFile ifrd = new RandomAccessFile(infoToParse, "r");
        ArrayList<MergeInfo> infoList = new ArrayList<MergeInfo>();

        // read pos/len info into a Merge object, then into infoList arrayList
        for(int i=0; i<ifrd.length(); i++){
            position = ifrd.readInt();
            length = ifrd.readInt();
            MergeInfo merge = new MergeInfo(position, length);
            infoList.add(merge);
        }

        ArrayList<Record> inBuff = new ArrayList<Record>(512); // buffer to store Blocks as they go into ListArray

        ArrayList<ArrayList<Record>> listArray = new ArrayList<ArrayList<Record>>(); // to store all Block ArrayLists


        for (int j = 0; j < raf.length()/8; j++){
            byte[] inputBuff = new byte[8192]; // declare input buffer
            int currPosition = infoList.get(j).getStart();
            int currLength = infoList.get(j).getLength();

            // add a block to input buffer (using pos and len)
            raf.readFully(inputBuff, currPosition, currLength);

            // divide inputBuff into records, then put those records into inBuff arrayList
            for (int k = 0; k < 512; k++){
                byte[] recordX = java.util.Arrays.copyOfRange(inputBuff, k, k+16); // isolates one record from inputBuff
                Record myRec = new Record(recordX);
                inBuff.add(myRec);
            }

            // sort the records inside inBuff
            Collections.sort(inBuff);

            // add inBuff contents to listArray --> just add it now, multiway merge later
            listArray.add(inBuff);

            // clear inBuff in prep for next iteration
            inBuff.clear();

            //use seek to (reset file pointer position) & start getting next block from Run file
            long currFilePointer = currPosition + currLength; //would this need to be manually converted to long?
            raf.seek(currFilePointer);
        }

        // What is this doing ??
        for(int i=0; i<inBuff.size(); i++){
            System.out.println(inBuff.get(i).toString());
        }


        // 1. use pos/len to access data for each block
        // 2. each block is presented as a byte array --> then turn it into records
        // 3. create an arrayList of records (necessary for each block)
        // 4. then multiway merge arrayLists

        // use a class for multiway merge (not particularly necessary but would be helpful)
        //      each merge returns a merged arrayList


    }

    //you can process the files you read in here and call your multiway merge
    public void run(){

    }
}