import java.io.*;
import java.util.*;

public class HuffmanCompressor {
    public static final int CHAR_MAX = 256;

    private String filename;
    private String basename;
    private String codeFilename;
    private String shortFilename;
    private boolean debugShortFile;
    private String outputFilename;

    public HuffmanCompressor(String filename, boolean debugShortFile) {
        this.filename = filename;
        this.basename = filename.split(".txt")[0];
        this.codeFilename = null;
        this.shortFilename = null;
        this.outputFilename = null;
        this.debugShortFile = debugShortFile;
    }

    public void makeCode() throws IOException {
        System.out.println("Making the Huffman code for " + filename + "...");

        int[] frequencies = new int[CHAR_MAX];
        FileInputStream input = new FileInputStream(filename);
        int curr = input.read();
        while (curr != -1) {
            int next = input.read();
            // Ignore terminating line feed (0x0a followed by -1)
            if (!(curr == 0x0a && next == -1)) {
                frequencies[curr] += 1;
            }
            curr = next;
        }
        input.close();
        System.out.println("   Constructed a frequency array of the characters in your file.");

        HuffmanCode t = new HuffmanCode(frequencies);
        System.out.println("   Constructed a new HuffmanCode(int[] frequencies)!");

        codeFilename = basename + ".code";
        t.save(new PrintStream(new File(codeFilename)));
        System.out.println("   Saved code to " + codeFilename + "!");

        System.out.println("...Done making the Huffman Code!");
    }

    public void compress() throws IOException {
        System.out.println("Compressing " + basename + ".txt...");

        String[] codes = new String[CHAR_MAX];
        Scanner codeInput = new Scanner(new File(codeFilename));
        while (codeInput.hasNextLine()) {
            int character = Integer.parseInt(codeInput.nextLine());
            codes[character] = codeInput.nextLine();
        }
        codeInput.close();

        if (debugShortFile) {
            shortFilename = basename + ".debug";
        } else {
            shortFilename = basename + ".short";
        }

        FileInputStream input = new FileInputStream(filename);
        OutputStream output = new FileOutputStream(shortFilename);
        if (!debugShortFile) {
            output = new CharToBitOutputStream(output);
        }

        int curr = input.read();
        while (curr != -1) {
            String code = codes[curr];
            if (code != null) {
                for (int i = 0; i < code.length(); i += 1) {
                    output.write(code.charAt(i));
                }
                curr = input.read();
            } else {
                // Error if code is missing and not a terminating line feed (0x0a followed by -1)
                if (!(curr == 0x0a && input.read() == -1)) {
                    System.out.println("   Missing code for " + curr + " ('" + (char) curr + "')!");
                    System.exit(1);
                }
                curr = -1;
            }
        }
        input.close();
        output.close();
        System.out.println("   Compressed contents to " + shortFilename + "!");

        System.out.println("...Done compressing the text file!");
    }

    public void decompress() throws IOException {
        System.out.println("Decompressing " + shortFilename + " using the Huffman Code...");

        Scanner codeInput = new Scanner(new File(codeFilename));
        HuffmanCode t = new HuffmanCode(codeInput);
        codeInput.close();
        System.out.println("   Constructed a new HuffmanCode(Scanner input)!");

        InputStream input = new FileInputStream(shortFilename);
        if (!debugShortFile) {
            input = new BitToCharInputStream(input);
        }
        Scanner scanner = new Scanner(input).useDelimiter("");
        outputFilename = basename + ".new";
        PrintStream output = new PrintStream(new File(outputFilename));
        t.translate(scanner, output);
        scanner.close();
        output.close();
        System.out.println("   Decompressed contents to output.txt!");
        System.out.println("...Done decompressing the short file!");
    }

    // The CharToBitOutputStream and BitToCharInputStream classes provide the ability to write and
    // read individual bits to a file in a compact form. One major limitation of this approach is
    // that the resulting file will always have a number of bits that is a multiple of 8. Whatever
    // bits are output to the file are padded at the end with 0's so the total is a multiple of 8.

    private static class CharToBitOutputStream extends OutputStream {
        private OutputStream output;
        private List<Integer> buffer;
        private int currentByte;  // a buffer used to build up next set of bits
        private int numBits;      // how many bits are currently in the buffer

        private static final int BYTE_SIZE = 8; // bits per byte

        // post: constructs a CharToBitOutputStream sending output to the given stream
        public CharToBitOutputStream(OutputStream output) {
            this.buffer = new ArrayList<Integer>();
            this.output = output;
        }

        // post: writes given char bit '0' or '1' to output as a true bit
        public void write(int bit) {
            if (bit != '0' && bit != '1') {
                throw new IllegalArgumentException("Illegal bit: " + bit);
            }
            bit = bit - '0';
            currentByte += bit << numBits;
            numBits += 1;
            if (numBits == BYTE_SIZE) {
                buffer.add(currentByte);
                numBits = 0;
                currentByte = 0;
            }
        }

        // post: output is closed
        public void close() throws IOException {
            int remaining = BYTE_SIZE - numBits;
            if (remaining == 8) {
                remaining = 0;
            } 
            // Flush the last byte (if there is one)
            if (remaining > 0) {
                buffer.add(currentByte);
            }
            // Prepend the output with the number of missing bits from the end
            output.write(remaining);
            for (int b : buffer) {
                output.write(b);
            }
            output.close();
        }
    }

    private static class BitToCharInputStream extends InputStream {
        private InputStream input;
        private int currentByte;     // current set of bits (buffer)
        private int nextByte;        // next set of bits (buffer)
        private int numBits;         // how many bits from buffer have been used
        private int remainingAtEnd;  // how many bits will be remaining at the end after we're done

        private static final int BYTE_SIZE = 8;  // bits per byte

        // pre : given file name is legal
        // post: constructs a BitToCharInputStream reading input from the file
        public BitToCharInputStream(InputStream input) throws IOException {
            this.input = input;
            // Read in the number of remaining bits at the end
            remainingAtEnd = input.read();
            // Set up the nextByte field.
            nextByte = input.read();
            nextByte();
        }

        // post: reads the next bit and return a char '0' or '1', or -1 if there are no more bits
        public int read() throws IOException {
            if (!hasNextBit()) {
                return -1;
            }
            return nextBit() + '0';
        }

        // post: returns true if there are remaining bits
        private boolean hasNextBit() {
            boolean atEnd = currentByte == -1;
            boolean onlyRemaining = (nextByte == -1) && (BYTE_SIZE - numBits == remainingAtEnd);
            return !atEnd && !onlyRemaining;
        }

        // post: reads next bit from input (-1 if at end of file)
        //       throws IOException if there is no bit to return
        private int nextBit() throws IOException {
            if (!hasNextBit()) {
                throw new IOException();
            }
            int result = currentByte % 2;
            currentByte /= 2;
            numBits += 1;
            if (numBits == BYTE_SIZE) {
                nextByte();
            }
            return result;
        }

        // post: refreshes the internal buffer with the next BYTE_SIZE bits
        private void nextByte() throws IOException {
            currentByte = nextByte;
            if (currentByte != -1) {
                nextByte = input.read();
            }
            numBits = 0;
        }

        // post: input is closed
        public void close() throws IOException {
            input.close();
        }
    }
}