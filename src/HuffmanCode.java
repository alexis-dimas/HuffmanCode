/*
* The HuffmanCode class creates Huffman codes for
* characters based on their frequencies and outputs
* the ASCII values and codes. It also reads a
* compressed message and outputs the original message.
*/

import java.util.*;
import java.io.*;

public class HuffmanCode {
private HuffmanNode overallRoot;
    /*
     * Constructs a new HuffmanCode object given an array of frequencies.
     * Parameters:
     * 	int[] frequencies - contains the frequency of each character
     */
    public HuffmanCode(int[] frequencies) {
        Queue<HuffmanNode> priority = new PriorityQueue<HuffmanNode>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                priority.add(new HuffmanNode(frequencies[i], i));
            }
        }
        
        while (priority.size() != 1) {
            HuffmanNode freq1 = priority.remove();
            HuffmanNode freq2 = priority.remove();
            int totalFreq = freq1.frequency + freq2.frequency;
            priority.add(new HuffmanNode(totalFreq, 0, freq1, freq2));
        }
        overallRoot = priority.remove();
    }
    
    /*
     * Constructs a new HuffmanCode object given data of ASCII
     * values and Huffman codes. Assumes that the Scanner is 
     * not null and is always in standard format.
     * Parameters:
     * 	Scanner input - contains data in the standard format
     */
    public HuffmanCode(Scanner input) {
        while (input.hasNextLine()) {
            int asciiValue = Integer.parseInt(input.nextLine());
            String code = input.nextLine();
            overallRoot = buildTree(overallRoot, asciiValue, code, 0);
        }
    }
    
    /*
     * Assists in building a tree of characters based on their
     * respective HuffmanCode. Returns the built tree.
     * Parameters:
     * 	HuffmanNode root - the tree to be built upon
     * 	int asciiValue - the character's ASCII value
     * 	String code - the character's Huffman code
     * 	int count - number of times gone left or right
     */
    private HuffmanNode buildTree(HuffmanNode root, int asciiValue, String code, int count) {
        if (code.length() == count) {
            root = new HuffmanNode(0, asciiValue);
        } else {
            if (root == null) {
                root = new HuffmanNode(0, 0);
            } 
            if (code.charAt(count) == '0') {
                root.left = buildTree(root.left, asciiValue, code, count += 1);
            } else {
                root.right = buildTree(root.right, asciiValue, code, count += 1);
            }
        }
        return root;
    }   
    
    /*
     * Outputs pairs of ASCII values and Huffman codes in
     * standard/pre-order format.
     * Parameters:
     * 	PrintStream output - enables output to a file
     */
    public void save(PrintStream output) {
        saveHelper(output, overallRoot, "");
    }
    
    /*
     * Assists in outputting pairs of ASCII values and Huffman
     * codes in standard/pre-order format.
     * Parameters:
     * 	PrintStream output - enables output to a file
     * 	HuffmanNode root - contains the ASCII values to output
     * 	String code - Huffman code of the character
     */
    private void saveHelper(PrintStream output, HuffmanNode root, String code) {
        if (root != null) {
            if (root.left == null && root.right == null) {
                output.println(root.value);
                output.println(code);
            }
            saveHelper(output, root.left, code + '0');
            saveHelper(output, root.right, code + '1');
        }
    }

    /*
     * Reads a compressed message and outputs the original uncompressed message.
     * Parameters:
     * 	Scanner input - contains a compressed message
     * 	PrintStream output - enables output to a file
     */
    public void translate(Scanner input, PrintStream output) {
        while (input.hasNext()) {
            HuffmanNode tempRoot = overallRoot;
            while (tempRoot.left != null && tempRoot.right != null) {
                char bit = input.next().charAt(0);
                if (bit == '0') {
                tempRoot = tempRoot.left;
                } else {
                tempRoot = tempRoot.right;
                }
            }
            output.write(tempRoot.value);     
        }
    }

    // A HuffmanNode represents a single node in the tree.
    private static class HuffmanNode implements Comparable<HuffmanNode> {
        public final int frequency;
        public final int value;
        public HuffmanNode left;
        public HuffmanNode right;

        /*
         * Constructs a new HuffmanNode.
         * Parameters:
         * 	int frequency - the frequency of the character
         * 	int value - the ASCII value of the character
         */
        public HuffmanNode(int frequency, int value) {
            this(frequency, value, null, null);
        }

        /*
         * Constructs a new HuffmanNode with two children.
         * Parameters:
         * 	int frequency - the frequency of the character
         * 	int value - the ASCII value of the character
         * 	HuffmanNode left - the left node of the HuffmanNode
         * 	HuffmanNode right - the right node of the HuffmanNode
         */
        public HuffmanNode(int frequency, int value, HuffmanNode left, HuffmanNode right) {
            this.frequency = frequency;
            this.value = value;
            this.left = left;
            this.right = right;
        }

        /*
         * Returns 0 if the two frequencies are equal. Returns 
         * an integer greater than 0 if the frequency is greater
         * than the other frequency. Returns an integer less than
         * 0 if the other frequency is greater than this frequency.
         * Parameters:
         * 	HuffmanNode other - the frequency to compare with
         */
        public int compareTo(HuffmanNode other) {
            return this.frequency - other.frequency;
        }
    }
}