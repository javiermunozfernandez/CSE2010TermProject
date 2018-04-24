
/*
  Authors (group members):Javier Munoz, Sung-Jun Baek, Thanart Pandey
  Email addresses of group members: jmunoz2014@my.fit.edu, sbaek2015@my.fit.edu, tpandey2017@my.fit.edu
  Group name: 14

  Course: CSE2010
  Section: 2

  Description of the overall algorithm and key data structures:
      The dictionary is stored as a trie.
          Each entry in the trie has a byte element, representing its index in the alphabet
          Each entry has a size 26 array of children
      To find the best Scrabble Word
          Find the word on the board, return as original word
          Find words the start with one of the letters from original word
          Find words that are the original word + a suffix
          Return the word that is worth the most points
      Finding words (availableCharacters, currentEntry, currentResult)
          if current entry is the end of a word
              check to see if its more points than the currentResult
              if it is, currentResult = entry -> scrabble word
          For each available character c, convert c to it's index in the alphabet array
              This uses hashing to make this constant time
              find the entry next at the same index in currentEntry.children
              Remove c from availableCharacters
              if next entry is the end of a word
                  check to see if its more points than the currentResult
                  if it is, currentResult = entry -> scrabble word
              recurse using nextEntry, the new availableCharacters, and the current best result
          return the best result
              
   To find a next best word selection from the dictionary for the Scrabble.
   The program mostly uses hash, binary representation (byte), and trie algorithm
*/
public class Trie {
    /*
     * Description: This stores the dictionary in a trie.
     * Constants:
     *  The following two are used along with Character.hashCode(c) to provide constant time searching
     *  OFFSET
     *  ALPHABET
     *
     *  BOARD_SIZE
     *
     * Methods:
     *      insert(Str) calls insert(Str, 0, root)
     *      insert: uses string and int to determine character to be inserted at Entry's children
     *      getIndex: takes a character and turns it into an index
     *      getBestWord: given a ScrabbleWord and the char in hand, it finds a word to play by calling the other methods
     *          getAvailableSuffixes: given an entry, it finds its descendant with the most points
     *          wordStartsWithLetterOnBoard: finds words perpendicular to the word on board. Assuming they start with a letter on board
     *          resultExtendsWordOnBoard: finds words extending the word on board (extending by adding suffixes, not prefixes)
     *          getEntryWithPrefix: returns the entry with the given prefix
     */
    private static final byte OFFSET = 13; //(Char.hash + OFFSET) % ALPHABET.length = alphabet.indexOf(Char)
    public static final char[] ALPHABET = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                                            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                                            'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final int BOARD_SIZE = 15;
    private static class Entry {
        /*
         * Description:
         * Entry class, is the 'node' of the Trie
         * 
         * Variables:
         *  index: index of the character in the Alphabet
         *  children: array of children, where each child is at the same index they'd be in the alphabet
         *  isWordEnd: is this character the end of a word
         *
         * Methods:
         *  toString: uses the index to retrieve the character from the alphabet
         */
        private final byte index; //we use byte instead of char because byte is 8 bit and char is 16 bit
        boolean isWordEnd = false;
        private final Entry[] children = new Entry[ALPHABET.length];
        Entry(final char theElement) {
            if (theElement == '!') { //Is root
                index = -1;
            } else {
                  //The following gives index the index of theElement in alphabet, assuming it is A-Z
                index = (byte) ((Character.hashCode(theElement) + OFFSET) % ALPHABET.length);
            }
        } 
        public String toString() {
            if (index == -1) { //Is root
                return "";
            } else {
                return String.valueOf(ALPHABET[index]);
            }
        }
    }
    private final Entry root;
    Trie() {
        root = new Entry('!'); //Root gets a special non-alphabet character
    }
    public final void insert(final String word) {
        insert(word, 0, root);
    }
    /**
     * Use index and word to find char element = word.charAt(index)
     * Insert the node into current's children
     * If there's already a node there, keep it
     * If index is less than the last index of word
     *  Insert with an incremented index and the new node
     * Else
     *  the node is the end of a word
     * @param word
     * @param index
     * @param current
     */
    private void insert(final String word, final int index, final Entry current) {
        final Entry[] kids = current.children;
        final char element = word.charAt(index);
        final Entry next;
        if (kids[getIndex(element)] == null) {
            next = new Entry(element);
            kids[getIndex(element)] = next;
        } else {
            next = kids[getIndex(element)];
        }
        if (index < word.length() - 1) {
            insert(word, index + 1, next);
        } else {
            next.isWordEnd = true;
        }

    }
    /**
     * Returns the best word
     * @param word
     * @param hand
     * @return
     */
    public ScrabbleWord getBestWord(final ScrabbleWord word, final char[] hand) {
        ScrabbleWord result = new ScrabbleWord();
        //Case 1: Best result is an extension of the word on board (suffix)
        result = extendsBoard(word, hand, result, "");
        //Case 2: Best result is perpendicular, starting with a letter already on the board
        result = startOnBoard(word, hand, root, "", result);
        if (result.getPoints() > 0) {
            return result; //return here to save on time
        }
        //Case 3: Best result is perpendicular, starting before a letter on the board
        result = getPrefixes(hand, word, "", root, result, 0);
        return result;
    }
    /**
     * Gets the best result extending the word on board
     * @param word
     * @param hand
     * @param result
     * @param col
     * @param row
     * @param orientation
     * @param spaceLeft
     * @return
     */
    private ScrabbleWord extendsBoard(ScrabbleWord word, char[] hand, ScrabbleWord result, final String prefix) {
        final int startCol = word.getStartColumn();
        final int startRow = word.getStartRow();
        final int col;
        final int row;
        final int spaceLeft;
        final char orientation;
        if (word.getOrientation() == 'h') {
            spaceLeft = BOARD_SIZE - (startCol + word.getScrabbleWord().length()); //space right of word
            orientation = 'h';
            col = startCol - prefix.length();
            if (col < 0) {
                return result;
            }
            row = startRow;
        } else {
            spaceLeft = BOARD_SIZE - (startRow + word.getScrabbleWord().length()); //space under of word
            orientation = 'v';
            col = startCol;
            row = startRow - prefix.length();
            if (row < 0) {
                return result;
            }
        }
        Entry letterOnBoard = getEntryWithPrefix(prefix + word.getScrabbleWord(), root); //Entry at end of word on board
        if (letterOnBoard != null) {
            result = getSuffixes(letterOnBoard, prefix + word.getScrabbleWord(), spaceLeft, hand, result, word, col, row, orientation);
        }
        return result;
    }
    /**
     * Returns the entry with the corresponding prefix
     * @param prefix
     * @param current
     * @return
     */
    private Entry getEntryWithPrefix(final String prefix, final Entry current) {
        if (prefix.length() == 0) {
            return current;
        } else {
            final char c = prefix.charAt(0);
            final int index = getIndex(c);
            final Entry newEntry = current.children[index];
            if (newEntry != null) {
                return getEntryWithPrefix(prefix.substring(1), newEntry);
            }
        }
        return null;
    }
    private ScrabbleWord getPrefixes(final char[] hand, final ScrabbleWord word, final String str,
                                                final Entry current, ScrabbleWord result, final int iteration) {
        final int spaceBefore;
        if (word.getOrientation() == 'h') {
            spaceBefore = word.getStartRow();
        } else {
            spaceBefore = word.getStartColumn();
        }
        if (spaceBefore <= iteration) {
            result = startOnBoard(word, hand, current, str, result);
            return result;
        } else {
            result = startOnBoard(word, hand, current, str, result);
            result = extendsBoard(word, hand, result, str);
            char lastChar = '!';
            for (int i = 0; i < hand.length; i++) {
                final char c = hand[i];
                if (c != '!' && c != '_' && c != lastChar) {
                    lastChar = c;
                    hand[i] = '!';
                    final Entry next = current.children[getIndex(c)];
                    if (next != null ) {
                        result = getPrefixes(hand, word, str + next, next, result, iteration + 1);
                    }
                    hand[i] = c;
                }
            }
        }
        return result;
    }
    /**
     * Finds the best word that starts with a letter already on board
     * @param word
     * @param hand
     * @param result
     * @param col
     * @param row
     * @param orientation
     * @param spaceLeft
     * @return
     */
    private ScrabbleWord startOnBoard(final ScrabbleWord word, final char[] hand, final Entry current, final String prefix,
                                      ScrabbleWord result) {
        final String wordOnBoard = word.getScrabbleWord();
        for (int i = 0; i < wordOnBoard.length(); i++) {
            final char c = wordOnBoard.charAt(i);
            final Entry firstLetter = current.children[getIndex(c)];
            if (current.children[getIndex(c)] != null) {
                if (word.getOrientation() == 'v') {
                    final int spaceLeft = BOARD_SIZE - (word.getStartColumn() + 1);
                    final int newRow = i + word.getStartRow();
                    final int newCol = word.getStartColumn() - prefix.length();
                    if (newCol < 0) {
                        continue;
                    }
                    result = getSuffixes(firstLetter, prefix + firstLetter, spaceLeft, hand, result, word, newCol, newRow, 'h');
                } else {
                    final int spaceLeft = BOARD_SIZE - (word.getStartRow() + 1);
                    final int newCol = i + word.getStartColumn();
                    final int newRow = word.getStartRow() - prefix.length();
                    if (newRow < 0) {
                        continue;
                    }
                    result = getSuffixes(firstLetter, prefix + firstLetter, spaceLeft, hand, result, word, newCol, newRow, 'v');
                }
            }
        }
        return result;
    }
    /**
     * if current entry is the end of a word
              check to see if its more points than the currentResult
              if it is, currentResult = entry -> ScrabbleWord
          For each available character c, convert c to it's index in the alphabet array
              This uses hashing to make this constant time
              find the entry next at the same index in currentEntry.children
              Remove c from availableCharacters
              if next entry is the end of a word
                  check to see if its more points than the currentResult
                  if it is, currentResult = entry -> ScrabbleWord
              recurse using nextEntry, the new availableCharacters, and the current best result
          return the best result
     * @param currentEntry
     * @param currentString
     * @param spaceLeft
     * @param hand
     * @param currentResult
     * @param startCol
     * @param startRow
     * @param orientation
     * @return
     */
    private ScrabbleWord getSuffixes(final Entry currentEntry, final String currentString, final int spaceLeft,
                                      final char[] hand, final ScrabbleWord currentResult, final ScrabbleWord word,
                                      final int startCol, final int startRow, final char orientation) {
        ScrabbleWord result = currentResult;
        if (currentEntry.isWordEnd) {
            final ScrabbleWord newWord = new ScrabbleWord(currentString, startRow, startCol, orientation);
            if (result == null) {
                result = newWord;
            } else if (!newWord.equals(word) && newWord.compareTo(result) < 0) { 
                result = newWord;
            }
        }
        if (spaceLeft > 1) {
            char lastChar = '!';
            for (int i = 0; i < hand.length; i++) {
                final char c = hand[i];
                if (c != '!' && c != '_' && c != lastChar) {
                    lastChar = c;
                    hand[i] = '!';
                    final Entry newEntry = currentEntry.children[getIndex(c)];
                    if (newEntry != null) {
                        result = getSuffixes(newEntry, currentString + newEntry, spaceLeft - 1, hand, result, word, startCol, startRow, orientation);    
                    }
                    hand[i] = c;
                }
            }
        }
        return result;
    }
    private int[] INDEXES = new int[Character.MAX_VALUE];
    /**
     * Initializes INDEXES to have each valid character's index in the alphabet
     */
    public void initializeIndexes() {
        for (char c : ALPHABET) {
            final byte b = (byte) (Character.hashCode(c));
            final int index = (b + OFFSET) % ALPHABET.length;
            INDEXES[c] = index;
        }
    }
    /**
     * Given a char, it returns it's index in alphabet. Assumes char is A-Z
     * @param c
     * @return
     */
    private int getIndex(final char c) {
        return INDEXES[c];
    }
}
