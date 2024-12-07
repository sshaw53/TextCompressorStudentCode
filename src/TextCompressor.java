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
    public static int total_bits = 12;

    private static void compress() {
        TST trie = new TST();
        String toCompress = BinaryStdIn.readString();
        int compressLength = toCompress.length();
        int extraCode = 81;
        int maxCode = 4095;
        int endOfFile = 80;
        String next_prefix;
        int currentCode;

        // Go through each letter in the String
        int i = 0;
        while (i < compressLength) {
            // Get the longest prefix with the index
            String prefix = trie.getLongestPrefix(toCompress, i);

            // If the largest prefix is a character, just set currentCode to the character's ascii value, if not,
            // search the TST
            if (prefix.length() == 1) {
                currentCode = prefix.charAt(0);
            }
            // In the case that the character isn't in the TST
            else if (prefix.isEmpty()) {
                currentCode = toCompress.charAt(i);
                prefix = "" +  toCompress.charAt(i);
            }
            else {
                currentCode = trie.lookup(prefix);
            }

            // Write to file
            BinaryStdOut.write(currentCode, total_bits);

            // If possible, go to the next character and add it to the TST
            if (extraCode <= maxCode && i < compressLength - 1) {
                next_prefix = prefix + toCompress.charAt(i + 1);
                trie.insert(next_prefix, extraCode);
                extraCode += 1;
            }

            // Increase i based on original prefix looked at
            i += prefix.length();
        }

        // Writing out the end of file
        BinaryStdOut.write(endOfFile, total_bits);
        BinaryStdOut.close();
    }

    private static void expand() {
        int extraCode = 81;
        int maxCode = 4095;
        int endOfFile = 80;
        int peekCode;
        String[] key = new String[maxCode + 1];
        String next_prefix;
        String peekString;
        String decoded;
        int currentCode = BinaryStdIn.readInt(total_bits);

        // Until we've reached the end of the string
        while (currentCode != endOfFile) {
            // If the code read in is just a letter, set decoded to be the letter, if not search TST for code
            if (currentCode <= 127) {
                decoded = "" + currentCode;
            }
            else {
                decoded = key[currentCode];
            }

            // Write to file and look at next code
            BinaryStdOut.write(decoded);
            peekCode = BinaryStdIn.readInt(total_bits);

            // If possible, add the first letter of the lookahead String to decoded and add to the TST
            if (extraCode <= maxCode && peekCode != endOfFile) {
                // Find a way to get the peekString, search TST for peekCode
                peekString = "FIND";
                next_prefix = decoded + peekString.charAt(0);
                key[extraCode] = next_prefix;
                extraCode += 1;
            }

            // To make sure to go to the next code
            currentCode = peekCode;
        }
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
