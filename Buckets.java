/*******************************************************************************
 * Given a guess and list of solutions, Buckets determines the "bucket," or
 * color pattern response, associated with the most solutions, and stores both
 * the bucket and list of potential solutions.
 *
 * The bucket is a string of five characters composed of g's, y's, and -'s.
 *
 * "g" (green) means the letter of guess in that position is in the same
 * position in the solution.
 *
 * "y" (yellow) means the letter of guess in that position is in the solution
 * but in a different position.
 *
 * "-" (grey) means the letter of guess in that position is not in the solution.
 *
 * If there are multiple buckets with the same "most" solutions, the bucket with
 * the least greens, then least yellows is designated the largest bucket.
 ******************************************************************************/

import java.util.Arrays;
import java.util.LinkedList;

public class Buckets {

    // string of the pattern of the largest bucket
    private final String largestBucket;

    // LinkedList containing remaining solutions of the largest bucket
    private final LinkedList<String> mostRemSols;

    // finds the largest bucket given a guess and list of possible solutions
    public Buckets(String guess, LinkedList<String> remSols) {

        ST<String, Integer> BUCKET_FREQ = new ST<String, Integer>();
        ST<String, LinkedList<String>> BUCKET_SOLS = new ST<String,
                LinkedList<String>>();

        String lwrGuess = guess.toLowerCase();  // b/c solutions are lowercase Strings

        for (String sol : remSols) {

            // create ST to store total occurrences of letters in sol to ensure # of
            // yellows/greens match those that are in sol
            ST<String, Integer> ltrFreq = new ST<String, Integer>();

            String[] sLtrs = sol.split("");
            for (int i = 0; i < sLtrs.length; i++) {
                if (ltrFreq.contains(sLtrs[i])) {
                    ltrFreq.put(sLtrs[i], ltrFreq.get(sLtrs[i]) + 1);
                }
                else ltrFreq.put(sLtrs[i], 1);
            }

            // detect pattern associated w/ potential solution sol
            char[] pattern = new char[sLtrs.length];

            // check correct position matches first b/c green takes priority
            for (int i = 0; i < lwrGuess.length(); i++) {
                if (sol.charAt(i) == lwrGuess.charAt(i)) {
                    pattern[i] = 'g';

                    // decrement number of times letter has left to appear in sol
                    String gLtr = lwrGuess.substring(i, i + 1).toLowerCase();
                    ltrFreq.put(gLtr, ltrFreq.get(gLtr) - 1);
                }
            }
            // check matches in incorrect positions and non-matches
            for (int i = 0; i < lwrGuess.length(); i++) {
                if (pattern[i] == 0) {  // skip over already assigned positions (greens)
                    String gLtr = lwrGuess.substring(i, i + 1).toLowerCase();
                    // ensure letter is in sol and is not marked more times than in sol
                    if (sol.contains(gLtr) && ltrFreq.get(gLtr) > 0) {
                        pattern[i] = 'y';

                        // decrement number of times letter has left to appear in sol
                        ltrFreq.put(gLtr, ltrFreq.get(gLtr) - 1);
                    }
                    else {
                        pattern[i] = '-';
                    }
                }
            }
            String bucket = new String(pattern);

            // add pattern to ST/increment occurrence counter
            if (BUCKET_FREQ.contains(bucket)) {
                BUCKET_FREQ.put(bucket, BUCKET_FREQ.get(bucket) + 1);
            }
            else {
                BUCKET_FREQ.put(bucket, 1);
                BUCKET_SOLS.put(bucket, new LinkedList<>());
            }

            // associate sol w/ pattern in patSol ST
            BUCKET_SOLS.get(bucket).add(sol);
        }

        // find pattern with the highest frequency
        String mostBucket = BUCKET_FREQ.min();
        int highestFreq = BUCKET_FREQ.get(mostBucket);

        for (String bucket : BUCKET_FREQ.keys()) {
            if (BUCKET_FREQ.get(bucket) > highestFreq) {
                highestFreq = BUCKET_FREQ.get(bucket);
                mostBucket = bucket;
            }
            else if (BUCKET_FREQ.get(bucket) == highestFreq) {
                // need to update mostPattern if pat has fewer greens or, if greens
                // are the same, fewer yellows

                int oldG = 0;
                int oldY = 0;
                int newG = 0;
                int newY = 0;

                String[] oldPat = mostBucket.split("");
                String[] newPat = bucket.split("");

                // count number of greens and yellows in pat and current mostPattern
                for (int i = 0; i < bucket.length(); i++) {
                    if (oldPat[i].equals("g")) oldG++;
                    else if (oldPat[i].equals("y")) oldY++;

                    if (newPat[i].equals("g")) newG++;
                    else if (newPat[i].equals("y")) newY++;
                }
                if (oldG > newG) {
                    highestFreq = BUCKET_FREQ.get(bucket);
                    mostBucket = bucket;
                }
                else if (oldG == newG && oldY > newY) {
                    highestFreq = BUCKET_FREQ.get(bucket);
                    mostBucket = bucket;
                }
            }
        }
        this.largestBucket = mostBucket;
        this.mostRemSols = BUCKET_SOLS.get(mostBucket);
    }

    // returns the possible solutions associated with the largest bucket
    public LinkedList<String> getMostRemSols() {
        return mostRemSols;
    }

    // returns the pattern of the largest bucket
    public String getLargestBucket() {
        return largestBucket;
    }

    // tests all instance methods to make sure they're working as expected
    // guesses are adapted from examples at qntm.org/absurdle
    public static void main(String[] args) {
        In solutionsFile = new In("words/valid_solutions.txt");
        String[] VALID_SOLS = solutionsFile.readAllStrings();
        LinkedList<String> remSols = new LinkedList<>(Arrays.asList(VALID_SOLS));

        // terns has mix of capital and lowercase letters
        // it should result in a bucket size of 256 and pattern of "-----"
        Buckets trial1 = new Buckets("tERns", remSols);
        StdOut.println("Guess 1: terns");
        StdOut.println("Size of largest bucket (expecting 254): " +
                               trial1.getMostRemSols().size());
        StdOut.println("Pattern of largest bucket (expecting -----): " +
                               trial1.getLargestBucket());
        StdOut.println();

        // aphid should result in a bucket size of 38 and pattern of "y----"
        Buckets trial2 = new Buckets("aphid", trial1.getMostRemSols());
        StdOut.println("Guess 2: aphid");
        StdOut.println("Size of largest bucket (expecting 37): " +
                               trial2.getMostRemSols().size());
        StdOut.println("Pattern of largest bucket (expecting y----): " +
                               trial2.getLargestBucket());
        StdOut.println();

        // quack should result in a bucket size of 12 and pattern of "--y--"
        Buckets trial3 = new Buckets("quack", trial2.getMostRemSols());
        StdOut.println("Guess 3: quack");
        StdOut.println("Size of largest bucket (expecting 11): " +
                               trial3.getMostRemSols().size());
        StdOut.println("Pattern of largest bucket (expecting --y--): " +
                               trial3.getLargestBucket());
        StdOut.println();

        // mambo should have two equally sized buckets "-g---" and "-y--y" but the
        // latter is expected as output b/c it has fewer greens
        // the bucket size should be 2
        Buckets trial4 = new Buckets("mambo", trial3.getMostRemSols());
        StdOut.println("Guess 4: mambo");
        StdOut.println("Size of largest bucket (expecting 2): " +
                               trial4.getMostRemSols().size());
        StdOut.println("Pattern of largest bucket (expecting -y--y): " +
                               trial4.getLargestBucket());
        StdOut.println();

        // the only possible solution after guessing loyal should be offal
        // the expected pattern is "-y-gg" which means the first l is not marked
        // b/c there is only one l in offal, and it's in the last position
        Buckets trial5 = new Buckets("loyal", trial4.getMostRemSols());
        StdOut.println("Guess 5: loyal");
        StdOut.println("Size of largest bucket (expecting 1): " +
                               trial5.getMostRemSols().size());
        StdOut.println("Pattern of largest bucket (expecting -y-gg): " +
                               trial5.getLargestBucket());
        StdOut.println();

        // the only possible solution left should still be offal
        // the expected pattern is ""
        Buckets trial6 = new Buckets("", trial5.getMostRemSols());
        StdOut.println("Guess 6: \"\"");
        StdOut.println("Size of largest bucket (expecting 1): " +
                               trial6.getMostRemSols().size());
        StdOut.println("Pattern of largest bucket (expecting \"\"): " +
                               trial6.getLargestBucket());
    }
}
