/*
Siqi (David) Ding
Compare two bin files and indicates the difference
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class compareFiles {
    public static void main(String[] args) throws IOException {

//        RandomAccessFile target = new RandomAccessFile("the file name you choose", "r");
//        RandomAccessFile sample = new RandomAccessFile("the file TA provided", "r");
        RandomAccessFile target = new RandomAccessFile("src/medium_output.bin", "r");
        RandomAccessFile sample = new RandomAccessFile("src/run_medium.bin", "r");
        int numOfError = 0;

        if (target.length() != sample.length())
            System.out.println("Your output size is not correct");
        else {
            //length for both file
            long temp = target.length();
            int fileSize = (int) temp;
            byte [] targetArray = new byte[fileSize];
            byte [] sampleArray = new byte[fileSize];
            target.read(targetArray);
            sample.read(sampleArray);
            boolean wrongBefore = false;
//            System.out.println("fileSize " + fileSize);
            for (int i = 0; i <fileSize/16; i++){
                //create the temp
                byte [] temp1 = Arrays.copyOfRange(targetArray, i *16, i *16 + 16);
                byte [] temp2 = Arrays.copyOfRange(sampleArray, i *16, i *16 + 16);
                Record targetRecord = new Record(temp1);
                Record sampleRecord = new Record(temp2);

                //if they are the same
                if (targetRecord.compareTo(sampleRecord) == 0){
                    if (wrongBefore){
                        wrongBefore = false;
                        System.out.println(" till num: " + (i - 1));
                    }
                }
                else{
                    if (!wrongBefore){
                        wrongBefore = true;
//                        System.out.println(targetRecord.getKey());
//                        System.out.println(sampleRecord.getKey());
                        System.out.print("The file is wrong from record num:" + i);
                    }
                    numOfError++;
                }
            }
            System.out.println("The number of errors: " + numOfError);
        }

    }
}
