/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, SIERRA SHAW
 */
public class TextCompressor {

    private static void compress() {

        // TODO: Complete the compress() method
        // Assume the first bit is a letter
        int currentBit = 0;
        int letter_bits = 6;
        int letter_to_reg = 58;
        int reg_to_letter = 65;
        int word_bits = 10;
        int reg_bits = 8;
        int state = 0;
        String toCompress = BinaryStdIn.readString();
        int compressLength = toCompress.length();

        // Metadata: Write out length of String (number of words in String)
        BinaryStdOut.write(compressLength);

        int i = 0;
        while (i < compressLength) {
            int character = toCompress.charAt(i);

            // Read in each char, then map it to its 6-bit value
            while (character >= 65 && character <= 122) {
                // Convert so it can fit into a 6-bit number
                character -= 'A';
                BinaryStdOut.write(character, letter_bits);
                i += 1;
                character = toCompress.charAt(i);
            }
            // Once it's not a letter, we need to write out an escape character to go to regular
            BinaryStdOut.write(letter_to_reg, letter_bits);
            while (character < 65 || character > 122) {
                // Write out the escape character
                BinaryStdOut.write(character, reg_bits);
                i += 1;
                character = toCompress.charAt(i);
            }
            i += 1;

            // Switch to next bit
            currentBit = (currentBit + 1) % 2;
        }
        BinaryStdOut.close();
    }

    private static void expand() {

        // TODO: Complete the expand() method

        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
