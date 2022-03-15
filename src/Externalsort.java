/*
The main file to read two files from the command line
 */

//Do not need to change
import java.io.IOException;

public class Externalsort {
    public static void main(String[] args) throws IOException {
        Parser parser = new Parser();
        if (args.length != 2){
            System.out.println("Invalid Command");
            System.exit(0);
        }
        else{
            parser.parseFile(args[0], args[1]);
        }

    }
}
