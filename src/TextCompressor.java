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
        int extraCode = 257;
        int maxCode = 4095;
        int endOfFile = 256;
        String next_prefix;
        int currentCode;

        // Adding all single character ASCII values from 0 to 255
        for (int i = 0; i < 256; i++) {
            trie.insert("" + (char)i, i);
        }

        // Go through each letter in the String
        int i = 0;
        while (i < compressLength) {
            // Get the longest prefix with the index and set current code using the prefix
            String prefix = trie.getLongestPrefix(toCompress, i);
            currentCode = trie.lookup(prefix);

            // Write to file
            BinaryStdOut.write(currentCode, total_bits);

            int current_spot = i + prefix.length();

            // If possible, go to the next character and add it to the TST
            if (extraCode <= maxCode && current_spot < compressLength) {
                next_prefix = prefix + toCompress.charAt(current_spot);
                trie.insert(next_prefix, extraCode);
                extraCode += 1;
            }

            // Increase i based on original prefix looked at
            i = current_spot;
        }

        // Writing out the end of file
        BinaryStdOut.write(endOfFile, total_bits);
        BinaryStdOut.close();
    }

    private static void expand() {
        int extraCode = 257;
        int maxCode = 4095;
        int endOfFile = 256;
        int peekCode;
        String[] key = new String[maxCode + 1];
        String next_prefix;
        String peekString;
        String decoded;
        int currentCode = BinaryStdIn.readInt(total_bits);

        // Add all characters of ASCII values 0-255 to the map
        for (int i = 0; i < 256; i++) {
            key[i] = "" + (char)i;
        }

        // Until we've reached the end of the String
        while (currentCode != endOfFile) {
            // Search map for code
            decoded = key[currentCode];

            // Write to file and look at next code
            BinaryStdOut.write(decoded);
            peekCode = BinaryStdIn.readInt(total_bits);

            // If possible, add the first letter of the lookahead String to decoded and add to the TST
            if (extraCode <= maxCode && peekCode != endOfFile) {
                peekString = key[peekCode];
                // If it's an empty slot, we know to avoid this edge case by adding the current prefix plus the first
                // letter of the prefix
                if (peekString == null) {
                    next_prefix = decoded + decoded.charAt(0);
                }
                else {
                    next_prefix = decoded + peekString.charAt(0);
                }

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
