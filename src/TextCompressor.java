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

        // Go through each letter in the String
        int i = 0;
        while (i < compressLength) {
            // Get the longest prefix with my index & its code
            String prefix = trie.getLongestPrefix(toCompress, i);
            int currentCode = trie.lookup(prefix);

            // If the code doesn't exist (if it's a character), add to trie if possible and just set currentCode to the
            // character's ascii value
            if (currentCode == -1) {
                if (extraCode <= maxCode) {
                    trie.insert(prefix, prefix.charAt(0));
                }
                currentCode = prefix.charAt(0);
            }

            BinaryStdOut.write(currentCode, total_bits);
            // If possible, go to the next character and add it to the trie
            if (extraCode <= maxCode && i < compressLength - 1) {
                next_prefix = prefix + toCompress.charAt(i + 1);
                trie.insert(next_prefix, extraCode);
                extraCode += 1;
            }
            i += prefix.length();
        }

        // Writing out the end of file
        BinaryStdOut.write(endOfFile, total_bits);
        BinaryStdOut.close();
    }

    private static void expand() {
        TST trie = new TST();
        int extraCode = 81;
        int maxCode = 4095;
        int currentCode = 0;
        int endOfFile = 80;
        int peekCode;
        String next_prefix;
        String peekString;

        while (currentCode != endOfFile) {
            currentCode = BinaryStdIn.readInt(total_bits);
            String decoded = "";
            peekCode = BinaryStdIn.readInt(total_bits);
            peekString = "";

            if (extraCode <= maxCode && peekCode != endOfFile) {
                next_prefix = decoded + peekString.charAt(0);
                trie.insert(next_prefix, extraCode);
                extraCode += 1;
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
