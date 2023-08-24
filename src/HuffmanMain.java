import java.io.*;
import java.util.*;

public class HuffmanMain {
    public static final boolean DEBUG = false;

    public static void main(String[] args) throws IOException {
        Scanner console = new Scanner(System.in);
        System.out.println("Welcome to the Huffman Compressor!");
        System.out.println();
        String filename;
        do {
            System.out.print("What text file would you like to load? ");
            filename = console.nextLine().trim();
        } while (!filename.endsWith(".txt"));
        System.out.println();

        HuffmanCompressor compressor = new HuffmanCompressor(filename, DEBUG);

        // Calls your HuffmanCode(int[]) constructor and save(PrintStream)
        compressor.makeCode();
        System.out.println();

        // Does not call your HuffmanCode program at all
        compressor.compress();
        System.out.println();

        // Calls your HuffmanCode(Scanner) constructor and translate(Scanner, PrintStream)
        compressor.decompress();
	}
}