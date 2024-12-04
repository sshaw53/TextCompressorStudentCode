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
    // Bits per state
    public static int letter_bits = 6;
    // public static int word_bits = 10;
    public static int reg_bits = 8;
    // Escape characters
    public static int letter_to_reg = 59;
    public static int reg_to_letter = 65;
    public static int space = 58;

    private static void compress() {
        String toCompress = BinaryStdIn.readString();
        int compressLength = toCompress.length();

        // Metadata: Write out length of String (number of words in String), we're assuming the first state is a letter
        BinaryStdOut.write(compressLength);

        int i = 0;
        while (i < compressLength) {
            int character = toCompress.charAt(i);

            // Read in each char, then map it to its 6-bit value
            while (character == 32 || (character >= 65 && character <= 122)) {
                // Convert so it can fit into a 6-bit number, add one for the space since it's used so much
                if (character == 32) {
                    character = space;
                }
                else {
                    character -= 'A';
                }
                BinaryStdOut.write(character, letter_bits);
                // Go to next character
                if (i <= compressLength - 2) {
                    i += 1;
                    character = toCompress.charAt(i);
                }
                else {
                    break;
                }
            }

            // Might be > compressLength - 1**
            if (i >= compressLength) {
                break;
            }

            // Once it's not a letter, we need to write out an escape character to go to regular
            BinaryStdOut.write(letter_to_reg, letter_bits);
            while (character != 32 && (character < 65 || character > 122)) {
                // Otherwise, just write it as is in 8-bit binary
                BinaryStdOut.write(character, reg_bits);
                // Go to next character
                if (i <= compressLength - 2) {
                    i += 1;
                    character = toCompress.charAt(i);
                }
                else {
                    break;
                }
            }
            // Next escape character
            BinaryStdOut.write(reg_to_letter, reg_bits);
        }
        BinaryStdOut.close();
    }

    private static void expand() {
        // Assume to start read in bits unless told otherwise
        int strLength = BinaryStdIn.readInt();
        int state = 0;

        int i = 0;
        while (i < strLength) {
            int character = BinaryStdIn.readInt(letter_bits);
            // In this state, keep reading until escape character
            while (character != letter_to_reg) {
                // Read in 6-bit sized data and convert to letter
                if (character == space) {
                    character = ' ';
                }
                else {
                    character += 'A';
                }
                BinaryStdOut.write(character);
                if (i <= strLength - 2) {
                    i += 1;
                    character = BinaryStdIn.readInt(letter_bits);
                }
                else {
                    break;
                }
            }

            if (i >= strLength) {
                break;
            }

            // Now we know it's looking for regular # of bits
            while (character != reg_to_letter) {
                BinaryStdOut.write(character);
                // Go to next character
                if (i <= strLength - 2) {
                    i += 1;
                    character = BinaryStdIn.readInt(reg_bits);
                }
                else {
                    break;
                }
            }
        }
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
