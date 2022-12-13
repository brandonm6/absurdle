/*******************************************************************************
 *  Runs the Absurdle game. List of valid guesses and solutions is specified
 *  beforehand in the words folder. Allows user to set specific maximum number
 *  of guesses (at most eight). Default number of guesses is eight.
 ******************************************************************************/

import java.util.Arrays;
import java.util.LinkedList;

public class Absurdle {

    // Runs a game of Absurdle, updating the board based on player's inputs through
    // their keyboard and the board's on-screen keyboard.
    public static void main(String[] args) {

        In guessesFile = new In("words/valid_guesses.txt");
        In solutionsFile = new In("words/valid_solutions.txt");

        // set valid guesses, solutions and max letters, guesses
        String[] VALID_GUESSES = guessesFile.readAllStrings();
        String[] VALID_SOLS = solutionsFile.readAllStrings();
        int MAX_GUESSES = 8;
        if (args.length != 0) MAX_GUESSES = Integer.parseInt(args[0]);

        // main loop (for starting new games)
        while (true) {
            // validSolutions as LinkedList b/c removing elements as user guesses
            LinkedList<String> remSols = new LinkedList<>(Arrays.asList(VALID_SOLS));

            // create new board
            Board board = new Board(MAX_GUESSES);
            int maxLetters = board.getMaxLetters();
            board.drawBoard();
            StdDraw.show();

            boolean lost = true;
            int curGuess = 0;

            // current game loop
            while (curGuess < MAX_GUESSES) {

                // linked list b/c can delete letters inputted
                LinkedList<String> guessLtrs = new LinkedList<String>();

                boolean choosing = true;
                String chosen = "";

                // accepting letters loop
                while (choosing) {

                    char nextIn = board.nextKeyTyped();
                    // 0 is null char, (if no key is typed)
                    if (nextIn == 0) nextIn = board.nextKeyClicked();

                    // 10 is enter char
                    if (nextIn == 10 && guessLtrs.size() == maxLetters) {
                        String guess = String.join("", guessLtrs);
                        if (Arrays.asList(VALID_GUESSES).contains(guess)) {
                            chosen = guess;
                            choosing = false;
                        }
                        else board.throwMessage("invalid word");
                    }
                    else if (nextIn == 8 && guessLtrs.size() > 0) {  // 8 is backspace
                        guessLtrs.removeLast();
                        board.addTile("", curGuess, guessLtrs.size());
                        board.throwMessage("");  // remove invalid word message
                    }
                    else if (Character.isLetter(nextIn) && guessLtrs.size() <
                            maxLetters) {
                        /*
                        https://docs.oracle.com/javase/7/docs/api/java/lang/Character.html
                        Using the isLetter method to ensure the tiles are only updated
                        with key presses that are letters.
                         */
                        String ltr = String.valueOf(nextIn);
                        guessLtrs.add(ltr);
                        board.addTile(ltr, curGuess, (guessLtrs.size() - 1));
                    }
                    StdDraw.show();
                    StdDraw.pause(150);  // to prevent repeat inputs when clicking
                }

                Buckets buckets = new Buckets(chosen, remSols);
                remSols = buckets.getMostRemSols();
                board.updateBoard(chosen, buckets.getLargestBucket(), curGuess);
                StdDraw.show();

                // check if won
                if (remSols.size() == 1 && remSols.peek().equals(chosen)) {
                    lost = false;
                    break;
                }

                curGuess++;
            }

            if (lost) board.loseSeq(remSols.peek());  // passes a solution
            else board.winSeq();
            board.throwMessage("press enter to play again");
            StdDraw.show();

            // loop waiting for response to play again
            while (true) {
                char nextIn = board.nextKeyTyped();
                if (nextIn == 0) nextIn = board.nextKeyClicked();

                if (nextIn == 10) break;  // 10 is enter char
            }
        }
    }
}
