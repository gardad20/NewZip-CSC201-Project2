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

    //while there are runs in info arraylsit
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
        for(int i=0; i<ifrd.length()/8; i++){
            position = ifrd.readInt();
            length = ifrd.readInt();
            MergeInfo merge = new MergeInfo(position, length);
            infoList.add(merge);
        }

        //array of objects?
        ArrayList<byte[]> listArray = new ArrayList<byte[]>(); // to store all Block ArrayLists

        //to hold positions for each run
        ArrayList<Integer> positionArray = new ArrayList<>();

        //while loop while (infoList.size()!=
        //create temporary array list for mergeinfo
        for (int j = 0; j < infoList.size(); j++){
            int currPosition = infoList.get(j).getStart();
            int currLength = infoList.get(j).getLength();
            byte[] inputBuff = new byte[8192]; // declare input buffer

            // add a block to input buffer (using pos and len)
            raf.readFully(inputBuff, currPosition, 8192);

            // add inBuff contents to listArray --> just add it now, multiway merge later
            listArray.add(inputBuff);

            // clear inBuff in prep for next iteration
            //inBuff.clear();

            //use seek to (reset file pointer position) & start getting next block from Run file
            long currFilePointer = currPosition + currLength; //would this need to be manually converted to long?
            raf.seek(currFilePointer);
        }


        // 1. use pos/len to access data for each block
        // 2. each block is presented as a byte array --> then turn it into records
        // 3. create an arrayList of records (necessary for each block)
        // 4. then multiway merge arrayLists

        // use a class for multiway merge (not particularly necessary but would be helpful)
        //      each merge returns a merged arrayList

//
//        //multi-way merge
//       int[] flagHolder = new int[8];
//        //output buff hold records?
//
//       boolean flag = true;
//       for(int i=0; i< flagHolder.length; i++){
//           if(flagHolder[i]!=7){
//               flag = false;
//           }
//           // Updates array once empty
//           // If not full block left leave remaining bytes empty
//           if(flagHolder[i]==7){
//               byte[] newArray = new byte[8192];
//               raf.readFully(newArray, positionArray.get(i), 8192);
//               //will work once listArray contains arrays
//               listArray.set(i, newArray);
//           }
//       }
//       while(!flag){
//           //where we compare and update flag in flagHolder array
//           // get the key value for each array at its flag
//           double[] keys = new double[8];
//           for (int j=0; j<8; j++){
//               byte[] recordX = java.util.Arrays.copyOfRange(listArray.get(j), flagHolder[j], j+8); // isolates one record from inputBuff
//               Record myRec = new Record(recordX);
//               keys[j] = myRec.getKey();
//               //firstMergeFile.write(recordX); // PROBLEM! should be sorted (but isn't rn) when going into the File
//           }
//           //find min key value in keys array --> add that value to outputBuff and change corresponding flag in flagHolders
////           for(){
////
////           }
       //
    }

    //you can process the files you read in here and call your multiway merge
    public void run(){

    }
}