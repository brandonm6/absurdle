/*******************************************************************************
 *
 *  Draws the Absurdle board (tiles and on-screen keyboard), permitting guesses
 *  of five letters each. Contains methods to update the board (tiles and keys)
 *  and detect user-input.
 *
 ******************************************************************************/

import java.awt.Color;
import java.awt.Font;

public class Board {

    private static final int MAX_LTRS = 5;  // max number of letters allowed per word

    private static final int TILE_SIZE = 62;  // height, width of tile box
    private static final int PAD = 2; // amount of padding btw tiles/keys

    private static final Font DEF_FONT = StdDraw.getFont();  // default StdDraw font
    private static final String DEL_UNICODE = "\u232B";  // unicode for backspace key

    private int canvasHeight;  // height of canvas
    private final int canvasWidth;  // width of canvas
    private final int kbrdHeight;  // height of the keyboard region
    private final double tileInd;  // indent to first column of tiles

    private final int maxGuesses; // maximum number of guesses allowed per game

    // helper rectangle datatype
    private class Rect {
        private double x;  // x-coordinate
        private double y;  // y-coordinate
        private double width;  // width of rectangle
        private double height;  // height of rectangle

        // creates a new rectangular region described by center (x, y), width, height
        public Rect(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        // checks if coordinates x, y are in rectangle (inclusive)
        public boolean inRect(double extX, double extY) {
            double halfHeight = height / 2;
            double halfWidth = width / 2;
            if (extX >= x - halfWidth && extX <= x + halfWidth &&
                    extY >= y - halfHeight && extY <= y + halfHeight) {
                return true;
            }
            return false;
        }
    }

    // ST containing letters of keyboard and their positions on the canvas
    private final ST<String, Rect> keys;

    // creates custom board of maxGuesses # of rows (maxGuesses can be at most 8)
    public Board(int maxGuesses) {

        // limit maxGuesses to at most eight b/c board becomes larger than screen
        int GUESS_LIMIT = 8;
        if (maxGuesses > GUESS_LIMIT) {
            throw new IllegalArgumentException("Maximum number of guesses can "
                                                       + "be at most eight.");
        }
        this.maxGuesses = maxGuesses;

        int KEY_WIDTH = 44;  // width of the keys box
        int KEY_HEIGHT = 58;  // height of the keys box

        // on-screen keyboard has rows of 10, 9, and 9 (alphabet, enter and backspace)
        int[] COLUMNS = { 10, 9, 9 };
        int ROWS = 3;

        kbrdHeight = (KEY_HEIGHT + PAD) * ROWS + PAD;

        canvasHeight = kbrdHeight + (TILE_SIZE + PAD) * maxGuesses + PAD + TILE_SIZE
                / 2;
        canvasWidth = (KEY_WIDTH + PAD) * COLUMNS[0] + PAD;

        tileInd = (canvasWidth - (TILE_SIZE + PAD) * MAX_LTRS - PAD) / 2.0;

        StdDraw.setCanvasSize(canvasWidth, canvasHeight);
        StdDraw.setXscale(0, canvasWidth);
        StdDraw.setYscale(0, canvasHeight);
        StdDraw.enableDoubleBuffering();

        String[] KEYS = {
                "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                "a", "s", "d", "f", "g", "h", "j", "k", "l",
                "enter", "z", "x", "c", "v", "b", "n", "m", DEL_UNICODE
        };

        keys = new ST<String, Rect>();

        // assign coordinates to keys to be in a staggered arrangement with padding
        // btw each key and where enter and delete keys are wider than alphabet keys
        int ind = 0;  // current index in KEYS
        for (int row = 0; row < ROWS; row++) {
            double y = kbrdHeight - (KEY_HEIGHT / 2.0 + PAD) - (PAD + KEY_HEIGHT) *
                    row;
            for (int col = 0; col < COLUMNS[row]; col++) {

                double indent = 0;
                if (row == 1 || row == 2) indent = KEY_WIDTH / 2.0;

                double x = indent + (KEY_WIDTH / 2.0 + PAD) + (PAD + KEY_WIDTH) * col;

                // set enter, delete keys separately b/c different sizes than alpha
                double width = KEY_WIDTH;
                if (KEYS[ind].equals("enter")) {
                    // using 4.0 and 1.5 to translate and scale the Rect as needed
                    x -= width / 4.0;
                    width *= 1.5;
                }
                else if (KEYS[ind].equals(DEL_UNICODE)) {  // delete key
                    x += width / 4.0;
                    width *= 1.5;
                }

                keys.put(KEYS[ind], new Rect(x, y, width, KEY_HEIGHT));
                ind++;
            }
        }
    }

    // returns the maximum # of letters per word (# of tiles per row)
    public int getMaxLetters() {
        return MAX_LTRS;
    }

    // draws the on-screen keyboard and tile board
    public void drawBoard() {
        // draw keyboard
        for (String key : keys.keys()) {
            drawRect(key, keys.get(key), StdDraw.LIGHT_GRAY, StdDraw.BLACK,
                     DEF_FONT);
        }

        // draw empty tile board (for guesses)
        for (int row = 0; row < maxGuesses; row++) {
            for (int col = 0; col < MAX_LTRS; col++) {
                addTile("", row, col);
            }
        }
    }

    // draws a rectangle with text given txt, the region (Rect), background color
    // (backColor), text color (textColor), and (font)
    private void drawRect(String txt, Rect rect, Color backColor, Color textColor,
                          Font font) {
        StdDraw.setPenColor(backColor);
        StdDraw.filledRectangle(rect.x, rect.y, rect.width / 2.0,
                                rect.height / 2.0);

        StdDraw.setFont(font);
        StdDraw.setPenColor(textColor);
        StdDraw.text(rect.x, rect.y, txt.toUpperCase());
    }

    // draws a white tile with black font, txt at row (curGuess) column (curLetter)
    public void addTile(String txt, int curGuess, int curLetter) {
        addTile(txt, curGuess, curLetter, StdDraw.WHITE, StdDraw.BLACK);
    }

    // draws a tile with txt specified by the row (curGuess) and column (curLetter)
    public void addTile(String txt, int curGuess, int curLetter, Color backColor,
                        Color textColor) {
        double y = canvasHeight - (TILE_SIZE / 2.0 + PAD) - (PAD + TILE_SIZE) *
                curGuess;
        double x = tileInd + (TILE_SIZE / 2.0 + PAD) + (TILE_SIZE + PAD) * curLetter;
        int TILE_FONT_SIZE = 32;
        drawRect(txt, new Rect(x, y, TILE_SIZE, TILE_SIZE), backColor,
                 textColor, new Font("Sans Serif", Font.BOLD, TILE_FONT_SIZE));
        StdDraw.square(x, y, TILE_SIZE / 2.0);  // draw border of tile
    }

    // draws tiles of the specified row (curGuess), with letters in guess, according
    // to the color scheme specified by bucket
    public void updateBoard(String guess, String bucket, int curGuess) {
        // convert bucket type to actual colors
        String[] pat = bucket.split("");
        Color[] colors = new Color[pat.length];
        for (int i = 0; i < pat.length; i++) {
            if (pat[i].equals("g")) colors[i] = Color.GREEN.darker();
            else if (pat[i].equals("y")) colors[i] = Color.ORANGE;
            else colors[i] = Color.DARK_GRAY;  // pat[i] == "-"
        }

        String[] ltrs = guess.split("");

        for (int i = 0; i < MAX_LTRS; i++) {
            // update tiles
            addTile(ltrs[i], curGuess, i, colors[i], StdDraw.WHITE);

            // update keyboard if characters in ltrs belong to the on-screen keyboard
            if (keys.contains(ltrs[i])) {
                drawRect(ltrs[i], keys.get(ltrs[i]), colors[i], StdDraw.WHITE,
                         DEF_FONT);
            }
        }
    }

    // displays a message txt, centered at 1/4 TILE_SIZE above the on-screen keyboard
    public void throwMessage(String txt) {
        double x = canvasWidth / 2.0;
        double y = kbrdHeight + TILE_SIZE / 4.0;
        drawRect(txt, new Rect(x, y, canvasWidth, TILE_SIZE / 4.0),
                 StdDraw.WHITE, StdDraw.BLACK, DEF_FONT);
    }

    // updates the tiles on the board to display the lose message
    public void loseSeq(String sol) {
        if (sol.length() != MAX_LTRS) {
            throw new IllegalArgumentException("Invalid solution given.");
        }
        String[] MSG = {
                "     ", "     ", "     ", "the  ", "word ", "was  ",
                "     ", sol
        };
        // to accommodate for boards where maxGuesses < MOST_GUESSES
        canvasHeight += (TILE_SIZE + PAD) * (MSG.length - maxGuesses);
        rowUpdater(MSG, "-----");
    }

    // updates the tiles on the board to display the win message
    public void winSeq() {
        String[] MSG = {
                "     ", " you ", "     ", " win ", "     ", "     ",
                "     ", "     "
        };
        canvasHeight += (TILE_SIZE + PAD) * (MSG.length - maxGuesses);
        rowUpdater(MSG, "ggggg");
    }

    // updates the rows of tiles on the board according to words and makes each row
    // match the bucket color scheme
    private void rowUpdater(String[] words, String bucket) {
        for (int i = 0; i < words.length; i++) {
            updateBoard(words[i], bucket, i);
        }
    }

    // returns char of user typed key on actual keyboard
    public char nextKeyTyped() {
        if (StdDraw.hasNextKeyTyped()) {
            char typedKey = StdDraw.nextKeyTyped();
            return typedKey;
        }
        return 0;
    }

    // returns char of user clicked key on the on-screen keyboard
    public char nextKeyClicked() {
        if (!StdDraw.isMousePressed()) return 0;  // char equivalent of null

        double y = StdDraw.mouseY();
        if (y > kbrdHeight - PAD || y < PAD) return 0;

        double x = StdDraw.mouseX();

        for (String key : keys.keys()) {
            if (keys.get(key).inRect(x, y)) {
                // convert string to char so same as nextKeyTyped
                if (key.equals("enter")) return 10;  // char of enter key
                else if (key.equals(DEL_UNICODE)) return 8;  // char of backspace
                return key.charAt(0);
            }
        }
        return 0;
    }

    // tests all instance methods to make sure they're working as expected
    public static void main(String[] args) {

        // TEST NEW BOARD
        // expect boards to appear starting with 0 rows and ending with 8 rows
        int MOST_GUSSES = 8;
        for (int i = 0; i < MOST_GUSSES + 1; i++) {
            Board board = new Board(i);
            board.drawBoard();
            StdDraw.show();
            StdDraw.pause(1000);
        }


        // TEST THE DRAWING METHODS
        // expect board with 3 rows, 3rd tile of first row to have a letter H
        Board board1 = new Board(3);
        board1.drawBoard();
        board1.addTile("h", 0, 2);
        StdDraw.show();
        StdDraw.pause(1000);

        // expect the first row of tiles to spell WEARY
        // E should be yellow, A should be green, the rest should be grey
        // the E and A on the keyboard should also be yellow and green
        board1.updateBoard("weary", "-yg--", 0);
        StdDraw.show();
        StdDraw.pause(1000);

        // expect a message to pop up above the on-screen keyboard saying
        // "MAX LETTERS = 5"
        board1.throwMessage("Max Letters = " + board1.getMaxLetters());
        StdDraw.show();
        StdDraw.pause(1000);

        // expect the message above the on-screen keyboard to disappear
        board1.throwMessage("");
        StdDraw.show();
        StdDraw.pause(1000);

        // expect a short lose sequence to appear in grey tiles, saying the word
        // was weary, expect keys on the keyboard to become grey
        board1.loseSeq("wearY");
        StdDraw.show();
        StdDraw.pause(1000);

        // expect a longer lose sequence to appear in grey tiles on an 8 row board,
        // saying the word was lucid, expect keys on the keyboard to become grey
        Board board2 = new Board(8);
        board2.drawBoard();
        board2.loseSeq("lucid");
        StdDraw.show();
        StdDraw.pause(1000);

        // expect a win sequence to show up in green tiles on an 8 row board,
        // expect keys on the keyboard to become green
        board2.winSeq();
        StdDraw.show();
        StdDraw.pause(1000);


        // TEST THE INPUT METHODS
        // expect typing or clicking keys to output the char value of that key
        // enter should output "enter", backspace should output "backspace", the
        // letters should output their corresponding characters
        while (true) {
            char nextIn = board2.nextKeyTyped();
            // 0 is null char, (if no key is typed)
            if (nextIn == 0) nextIn = board2.nextKeyClicked();

            if (nextIn != 0) {
                if (nextIn == 10) {
                    StdOut.println("enter");
                }
                else if (nextIn == 8) {
                    StdOut.println("backspace");
                }
                else StdOut.println(nextIn);
            }
            StdDraw.pause(100);
        }


        // TEST THE RECT DATA TYPE
        // if clicking keys on the on-screen keyboard outputs as expected, the
        // Rect datatype and its method inRect are implemented correctly, as the
        // on-screen keys are dependent on it

    }
}
