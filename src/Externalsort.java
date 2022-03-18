/*
The main file to read two files from the command line
 */
/*
 On my honor:
 - I have not used source code obtained from another student,
 or any other unauthorized source, either modified or
 unmodified.

- All source code and documentation used in my program
 is either my original work, or was derived by me from the
 source code published in the textbook for this course.

- I have not discussed coding details about this project with
 anyone other than my partner (in the case of a joint
 submission), instructor, tutors or the TAs assigned
 to this course. I understand that I may discuss the concepts
 of this program with other students, and that another student
 may help me debug my program so long as neither of us writes
 anything during the discussion or modifies any computer file
 during the discussion. I have violated neither the spirit nor
letter of this restriction.

Signed, Sophie Lee and Abby Gardner
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
