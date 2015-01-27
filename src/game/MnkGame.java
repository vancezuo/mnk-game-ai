/**
 * Copyright 2014 Vance Zuo
 */
package game;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The MnkGame class represents a family of grid-based connection games.
 * <p>
 * Each instance can have a different number of column and rows and
 * win-in-a-row lengths. The number of pieces each player can place per turn,
 * as well as on the first player's first turn specifically, is also
 * customizable. A "drop" rule forces pieces to the lowest unoccupied row
 * in their column.
 * <p>
 * Common games that can be represented by the class include Tic-tac-toe,
 * Gomoku, and Connect 4.
 * 
 * @author Vance Zuo
 * @created Dec 20, 2014
 *
 */
public class MnkGame {

  // Player constants
  /** Constant representing empty spaces, i.e. "no" player. */
  public static final int PLAYER_NONE = 0;
  /** Constant representing the first player. */
  public static final int PLAYER_1 = 1;
  /** Constant representing the second player. */
  public static final int PLAYER_2 = -PLAYER_1;


  // Instance variables
  private final int m, n, k; // m = cols, n = rows, k = win-in a row
  private final int p, q; // p = pieces per turn, q = pieces for first turn
  private final boolean drop; // whether pieces "drop" to lowest row
  private final int[] board; // m x n grid as 1D array
  private final int[] history; // past piece placements

  private int ply; // number of past piece placements
  private int turn, winner; // current player, winning player (if any)


  // Constructors
  /**
   * Constructs a new game with specified board size, and win/turn/drop rules.
   * 
   * @param m       Number of columns
   * @param n       Number of rows
   * @param k       Number of pieces in a line needed to win
   * @param p       Number of pieces placed per turn (except player 1's first)
   * @param q       Number of pieces placed on player 1's first turn
   * @param drop    Iff true, pieces placed in the lowest row of a column
   */
  public MnkGame(int m, int n, int k, int p, int q, boolean drop) {
    if (m <= 0)
      throw new IllegalArgumentException("Non-positive m: " + m);
    if (n <= 0)
      throw new IllegalArgumentException("Non-positive n: " + n);
    if (k <= 0 || (k > m && k > n))
      throw new IllegalArgumentException("Invalid k: " + k);
    if (p <= 0)
      throw new IllegalArgumentException("Non-positive p: " + p);
    if (q <= 0)
      throw new IllegalArgumentException("Non-positive q: " + q);

    this.m = m;
    this.n = n;
    this.k = k;
    this.p = p;
    this.q = q;
    this.drop = drop;
    board = new int[n * m];
    history = new int[n * m]; // allocate enough for full game
    ply = 0;
    turn = PLAYER_1;
    winner = PLAYER_NONE;
  }

  /**
   * Constructs a new game with specified board size and win/turn rules.
   * The drop rule is disabled, allowing pieces to be played anywhere.
   * 
   * @param m       Number of columns
   * @param n       Number of rows
   * @param k       Number of pieces in a line needed to win
   * @param p       Number of pieces placed per turn (except player 1's first)
   * @param q       Number of pieces placed on player 1's first turn
   */
  public MnkGame(int m, int n, int k, int p, int q) {
    this(m, n, k, p, q, false);
  }

  /**
   * Constructs a new game with specified board size and win rule.
   * Turn rules are set to one piece per turn.
   * The drop rule is disabled, allowing pieces to be played anywhere.
   * 
   * @param m       Number of columns
   * @param n       Number of rows
   * @param k       Number of pieces in a line needed to win
   */
  public MnkGame(int m, int n, int k) {
    this(m, n, k, 1, 1);
  }

  /**
   * Constructs a new game with the rules of tic-tac-toe.
   */
  public MnkGame() {
    this(3, 3, 3);
  }


  // Public methods
  /**
   * Plays a move at a specified square, optionally verifying its legality.
   * <p>
   * Except for performance-related reasons, it is recommended to check the
   * move's legality. An IllegalArgumentException is thrown if checkLegal
   * is true and the move is illegal. The game state will not change
   * in this case.
   * 
   * @param row         Row of the move
   * @param col         Column of the move
   * @param checkLegal  Iff true, verify the legality of the move
   */
  public void doMove(int row, int col, boolean checkLegal) {
    doMove(getSquare(row, col));
  }

  /**
   * Plays a move at the specified square, optionally verifying its legality.
   * <p>
   * Except for performance-related reasons, it is recommended to check to
   * move's legality. An IllegalArgumentException is thrown if checkLegal
   * is true and the move is illegal. The game state will not change
   * in this case.
   * <p>
   * The square parameter is related to row and column via the following
   * formula: <pre>square = row * #columns + column</pre>.
   * 
   * @param square      Square index of the move
   * @param checkLegal  Iff true, verify the legality of the move
   */
  public void doMove(int square, boolean checkLegal) {
    if (checkLegal && !canDoMove(square))
      throw new IllegalArgumentException("Illegal move.");
    board[square] = turn;
    history[ply++] = square;
    winner = calculateWinner(square);
    if (ply >= q && (ply - q) % p == 0)
      turn = -turn;
  }

  /**
   * Plays a move at a specified square.
   * <p>
   * The move must legal, else an IllegalArgumentException is thrown.
   * The game state will not change in this case.
   * 
   * @param row         Row of the move
   * @param col         Column of the move
   */
  public void doMove(int row, int col) {
    doMove(row, col, true);
  }

  /**
   * Plays a move at a specified square.
   * <p>
   * The move must legal, else an IllegalArgumentException is thrown.
   * The game state will not change in this case.
   * <p>
   * The square parameter is related to row and column via the following
   * formula: <code>square = row * #columns + column</code>.
   * 
   * @param square      Square index of the move
   */
  public void doMove(int square) {
    doMove(square, true);
  }

  /**
   * Undoes the last move played, optionally verifying its legality.
   * <p>
   * Except for performance-related reasons, it is recommended to check the
   * move's legality. An IllegalArgumentException is thrown if checking
   * legality is on and the move is illegal. The game state will not change
   * in this case.
   * 
   * @param checkLegal  Iff true, verify an undo is possible.
   */
  public void undoMove(boolean checkLegal) {
    if (checkLegal && !canUndoMove())
      throw new IllegalArgumentException("Cannot undo any moves.");
    int index = history[ply - 1];
    if (ply >= q && (ply - q) % p == 0)
      turn = -turn;
    winner = PLAYER_NONE;
    ply--;
    board[index] = PLAYER_NONE;
  }

  /**
   * Undoes the last move played.
   * <p>
   * The undo must be possible, else an IllegalArgumentException is thrown.
   * The game state will not change in this case.
   */
  public void undoMove() {
    undoMove(true);
  }

  /**
   * Checks if playing a move at specified square is legal.
   * <p>
   * See {@link #getSquare(int, int)} for details on how the square
   * parameter is related to row and column.
   * 
   * @param square  Square index of the move
   * @return        True if move is legal; false otherwise
   */
  public boolean canDoMove(int square) {
    return (0 <= square && square < n * m) && (board[square] == PLAYER_NONE)
        && (winner == PLAYER_NONE)
        && (!drop || getRow(square) == 0 || board[square - m] != PLAYER_NONE);
  }

  /**
   * Checks if is possible to undo a move.
   * 
   * @return    True if can undo a move; false otherwise
   */
  public boolean canUndoMove() {
    return ply > 0;
  }

  /**
   * Checks if the game has a winner.
   * 
   * @return    True if a player has won; false otherwise
   */
  public boolean hasWinner() {
    return winner != PLAYER_NONE;
  }

  /**
   * Checks if the game is over, i.e. a win for a player or a draw.
   * 
   * @return    True if the game is over; false otherwise
   */
  public boolean isGameOver() {
    return hasWinner() || (getOccupiedSquares() == getSquares());
  }

  /**
   * Gets the number of columns.
   * 
   * @return    Number of columns in the game's board.
   */
  public int getCols() {
    return m;
  }

  /**
   * Gets the number of rows.
   * 
   * @return    Number of rows in the game's board.
   */
  public int getRows() {
    return n;
  }

  /**
   * Gets the number of diagonals.
   * <p>
   * See {@link #getDiagonal(int)} for the meaning of "diagonal".
   * 
   * @return    Number of normal diagonals, i.e. lines with directly
   *            correlated row and column values
   */
  public int getDiagonals() {
    return m + n - 1;
  }

  /**
   * Gets the number of anti-diagonals.
   * <p>
   * See {@link #getAntiDiagonal(int)} for the meaning of "anti-diagonal".
   * 
   * @return    Number of anti-diagonals, i.e. lines with inversely
   *            correlated row and column values
   */
  public int getAntiDiagonals() {
    return getDiagonals();
  }

  /**
   * Gets the number of pieces in a line needed to win.
   * 
   * @return    Number of pieces in a line needed to win
   */
  public int getK() {
    return k;
  }

  /**
   * Gets the number of pieces each player gets to place per turn.
   * This is not including player 1's first move, which is retrievable
   * via {@link #getFirstTurnMoves()}.
   * 
   * @return    Number of pieces placed per turn (except player 1's first)
   */
  public int getTurnMoves() {
    return p;
  }

  /**
   * Gets the number of pieces the player 1 plays on their first move.
   * 
   * @return    Number of pieces placed on player 1's turn
   */
  public int getFirstTurnMoves() {
    return q;
  }

  /**
   * Gets whether the drop rule is on.
   * 
   * @return    True if pieces must be placed in the lowest row of a column;
   *            false otherwise
   */
  public boolean hasDropMoves() {
    return drop;
  }

  /**
   * Gets the number of occupied, i.e. non-empty, squares.
   * 
   * @return    Number pieces on the board.
   */
  public int getOccupiedSquares() {
    return ply;
  }

  /**
   * Gets the current player to move.
   * 
   * @return    Constant representing whose turn it is to move
   */
  public int getCurrentPlayer() {
    return turn;
  }

  /**
   * Gets the winning player if any, else returns {@link #PLAYER_NONE}.
   * 
   * @return    Constant representing the winner
   */
  public int getWinner() {
    return winner;
  }

  /**
   * Gets the number of squares in the game's board.
   * 
   * @return    Number of squares, i.e. number of columns * number of rows
   */
  public int getSquares() {
    return m * n;
  }

  /**
   * Gets the square corresponding to the specified row and column.
   * <p>
   * Square indexing is based on a row-major order representation of the
   * grid, i.e. the square is related to row and column via the following
   * formula: <code>square = row * #columns + column</code>.
   * 
   * @param row     Row of the square
   * @param col     Column of the square
   * @return        Square index with the specified row and column
   */
  public int getSquare(int row, int col) {
    return row*m + col;
  }

  /**
   * Gets the row corresponding to the specified square.
   * <p>
   * See {@link #getSquare(int, int)} for details on how the square
   * parameter is related to row.
   * 
   * @param square  Square index
   * @return        Row of the square index
   */
  public int getRow(int square) {
    return square / m;
  }

  /**
   * Gets the squares corresponding to the specified row.
   * <p>
   * Each square is related to the row such that
   * <code>getRow(square) = row</code>.
   * 
   * @param row     Row of the squares
   * @return        Array of squares with specified row
   */
  public int[] getRowSquares(int row) {
    int[] list = new int[m];
    for (int col = 0; col < m; col++)
      list[col] = board[getSquare(row, col)];
    return list;
  }

  /**
   * Gets the column corresponding to the specified square.
   * <p>
   * See {@link #getSquare(int, int)} for details on how the square
   * parameter is related to column.
   * 
   * @param square  Square index
   * @return        Column of the square index
   */
  public int getCol(int square) {
    return square % m;
  }

  /**
   * Gets the squares corresponding to the specified column.
   * <p>
   * Each square is related to the column such that
   * <code>getCol(square) = column</code>.
   * 
   * @param col     Column of the squares
   * @return        Array of squares with specified column
   */
  public int[] getColSquares(int col) {
    int[] list = new int[n];
    for (int row = 0; row < n; row++)
      list[row] = board[getSquare(row, col)];
    return list;
  }

  /**
   * Gets the diagonal corresponding to the specified square.
   * <p>
   * See {@link #getSquare(int, int)} for obtaining the square parameter
   * from a row and column.
   * <p>
   * Diagonals have directly correlated row and column values. Specifically,
   * the column minus the row of squares on a given diagonal is constant.
   * <p>
   * Diagonal indexing assigns the lowest index to the diagonal at the
   * corner with maximum row and minimum column; the highest at the
   * corner with minimum row and maximum column. For example, on the 3x3
   * board, the diagonal indexes are as follows:
   * <pre>
   *     0 1 2 column
   * 0   2 3 4
   * 1   1 2 3
   * 2   0 1 2
   * row</pre>
   * For a given diagonal <i>i</i>, either increasing the column or
   * decreasing row by 1 goes to the diagonal <i>i + 1</i>; either
   * decreasing the column or increasing the row by 1 goes to the
   * diagonal <i>i - 1</i>.
   * 
   * @param square  Square index
   * @return        Diagonal the square occupies
   */
  public int getDiagonal(int square) {
    return getCol(square) - getRow(square) + getRows() - 1;
  }

  /**
   * Gets the squares corresponding to the specified diagonal.
   * <p>
   * See {@link #getDiagonal(int)} for details about diagonal indexing,
   * which is what the <code>diag</code> parameter is based on.
   * 
   * @param diag    Diagonal index
   * @return        Array of squares in the specified diagonal
   */
  public int[] getDiagonalSquares(int diag) {
    int[] list = new int[getDiagonalSize(diag)];
    int startRow = Math.max(n - 1 - diag, 0);
    int startCol = Math.max(diag - n, 0);
    int square = getSquare(startRow, startCol);
    for (int i = 0; i < list.length; i++) {
      list[i] = square;
      square += m + 1;
    }
    return list;
  }

  /**
   * Gets the number of squares in the specified diagonal.
   * <p>
   * This is more efficient than calling <code>getDiagonal(int).length</code>
   * if the length is the only attribute desired.
   * <p>
   * See {@link #getDiagonal(int)} for details about diagonal indexing,
   * which is what the <code>diag</code> parameter is based on.
   * 
   * @param diag    Diagonal index
   * @return        Number of squares in the diagonal.
   */
  public int getDiagonalSize(int diag) {
    return getAntiDiagonalSize(diag);
  }

  /**
   * Gets the anti-diagonal corresponding to the specified square.
   * <p>
   * See {@link #getSquare(int, int)} for obtaining the square parameter
   * from a row and column.
   * <p>
   * Anti-diagonals have inversely correlated row and column values.
   * Specifically, the sum of the column and row of squares on a given
   * diagonal is constant.
   * <p>
   * Anti-diagonal indexing assigns the lowest index to the diagonal at the
   * corner with minimum row and minimum column; the highest at the
   * corner with maximum row and maximum column. For example, on the 3x3
   * board, the diagonal indexes are as follows:
   * <pre>
   *     0 1 2 column
   * 0   0 1 2
   * 1   1 2 3
   * 2   2 3 4
   * row</pre>
   * For a given anti-diagonal <i>i</i>, either increasing the column
   * or row by 1 goes to the anti-diagonal <i>i + 1</i>; either decreasing the
   * column or row by 1 goes to the anti-diagonal <i>i - 1</i>.
   * 
   * @param square  Square index
   * @return        Anti-diagonal the square occupies
   */
  public int getAntiDiagonal(int square) {
    return getCol(square) + getRow(square);
  }

  /**
   * Gets the squares corresponding to the specified anti-diagonal.
   * <p>
   * See {@link #getAntiDiagonal(int)} for details about anti-diagonal
   * indexing, which is what the <code>diag</code> parameter is based on.
   * 
   * @param diag    Anti-diagonal index
   * @return        Array of squares in the specified anti-diagonal
   */
  public int[] getAntiDiagonalSquares(int diag) {
    int[] list = new int[getAntiDiagonalSize(diag)];
    int startRow = Math.max(diag - m, 0);
    int startCol = Math.min(diag, m - 1);
    int square = getSquare(startRow, startCol);
    for (int i = 0; i < list.length; i++) {
      list[i] = square;
      square += m - 1;
    }
    return list;
  }

  /**
   * Gets the number of squares in the specified anti-diagonal.
   * <p>
   * This is more efficient than calling
   * <code>getAntiDiagonal(int).length</code>
   * if the length is the only attribute desired.
   * <p>
   * See {@link #getAntiDiagonal(int)} for details about anti-diagonal
   * indexing, which is what the <code>diag</code> parameter is based on.
   * 
   * @param diag    Diagonal index
   * @return        Number of squares in the diagonal.
   */
  public int getAntiDiagonalSize(int diag) {
    return min(n + m - 1 - diag, diag + 1, n, m);
  }

  /**
   * Gets the piece at the specified square.
   * <p>
   * See {@link #getSquare(int, int)} for details on how the square
   * parameter is related to row and column.
   * 
   * @param square  Square index
   * @return        Constant representing the the player whose piece is on the
   *                square, or {@link #PLAYER_NONE} otherwise.
   */
  public int getPiece(int square) {
    return board[square];
  }

  /**
   * Gets the moves played in the game.
   * <p>
   * Note the returned array is a copy. Modifying it does not changes the
   * game state.
   * 
   * @return    An array containing the squares of all the moves played, in
   *            order from first to latest.
   */
  public int[] getHistory() {
    int[] historyTrimmed = new int[ply];
    for (int i = 0; i < ply; i++)
      historyTrimmed[i] = history[i];
    return historyTrimmed;
  }

  /**
   * Gets the move played in the game on after <code>ply</code> plies.
   * <p>
   * See {@link #getElapsedPly()} for a description of a ply.
   * 
   * @param ply     Number of plies before the desired move
   * @return        Square index of the move played after <code>ply</code>
   *                moves into the game
   */
  public int getHistory(int ply) {
    if (ply < 0 || ply >= history.length)
      throw new IllegalArgumentException("Illegal history move access.");
    return history[ply];
  }

  /**
   * Gets a two-dimensional array representation of the board.
   * 
   * @return    two-dimension array in row major order representing the board
   */
  public int[][] getBoard() {
    int[][] board2d = new int[n][m];
    for (int row = 0; row < n; row++)
      for (int col = 0; col < m; col++)
        board2d[row][col] = board[getSquare(row, col)];
    return board2d;
  }

  /**
   * Gets the number of ply that have elapsed since the start of the game.
   * <p>
   * A ply is an atomic "move" by a player involving the placement of
   * exactly one piece. This is the same as a player's entire move if
   * they place only one piece per turn.
   * <p>
   * In other words, the number of ply elapsed is the same as the number
   * of pieces placed on the board.
   * 
   * @return    Number of ply since start of game
   */
  public int getElapsedPly() {
    return ply;
  }

  /**
   * Gets the number of turns that elapse after <code>totalPly</code> ply.
   * <p>
   * A turn consists of all the pieces a player plays in a row before their
   * opponent gets the move. This is different the a ply if player get
   * to place multiple pieces on their turns. See {@link #getElapsedPly()}
   * for a description of ply.
   * 
   * @param totalPly    Total number of elapsed ply
   * @return            Number of elapsed turns after <code>totalPly</code>
   *                    elapsed ply from beginning of a game
   */
  public int getElapsedTurns(int totalPly) {
    if (totalPly < q)
      return 0;
    return 1 + (totalPly - q) % p;
  }

  /**
   * Gets the number of turns that have elapsed in the game.
   * 
   * @see       #getElapsedTurns(int)
   * @return    Number of elapsed turns since the beginning of the game
   */
  public int getElapsedTurns() {
    return getElapsedTurns(ply);
  }

  /**
   * Gets the number of pieces left to place for the turn of the current
   * player, after <code>totalPly</code> ply.
   * <p>
   * See {@link #getElapsedTurns(int)} for a description of turns, which
   * are not necessarily the same as ply. See {@link #getElapsedPly()}
   * for a description of ply.
   * 
   * @param totalPly    Total number of elapsed ply
   * @return            Number of "moves" (pieces to place) left for the
   *                    current player (after <code>totalPly</code> ply has
   *                    elapsed) to play
   */
  public int getTurnRemainingMoves(int totalPly) {
    if (totalPly < q)
      return q - totalPly;
    return p - (totalPly - q) % p;
  }

  /**
   * Gets the number of pieces left to place for the current player's turn.
   * 
   * @see               #getTurnRemainingMoves(int)
   * @param totalPly    Total number of elapsed ply
   * @return            Number of "moves" (pieces to place) left for the
   *                    current player to play
   */
  public int getTurnRemainingMoves() {
    return getTurnRemainingMoves(ply);
  }

  /**
   * Gets the number of psuedo-legal moves the current player can play.
   * <p>
   * A pseudolegal move is a location where the player <i>could</i> play.
   * Whether it's legal depends on whether the game is over already or not.
   * 
   * @return    Number of moves where the current player could play, assuming
   *            the game is not over.
   */
  public int getPseudolegalMoves() {
    if (drop)
      return getCols() - getOccupiedCols();
    return getSquares() - getOccupiedSquares();
  }

  /**
   * Gets the number of legal moves the current player can play.
   * <p>
   * This is essentially the same as {@link #getPseudolegalMoves()}, except
   * if the game is over, in which case there are no legal moves.
   * <p>
   * It is recommended to use this method over {@link #getPseudolegalMoves()}
   * except for performance reasons.
   * 
   * @return    Number of legal moves the current player has
   */
  public int getLegalMoves() {
    if (winner != PLAYER_NONE)
      return 0;
    return getPseudolegalMoves();
  }

  /**
   * Gets a generator of all pseudo-legal moves of the current player.
   * <p>
   * See {@link #getPseudolegalMoves()} for a description of pseudo-legal.
   * 
   * @return    An Iterable for iterating through the pseudo-legal moves
   *            of the current player
   */
  public Iterable<Integer> generatePseudolegalMoves() {
    return drop ? genDropPseudolegalMoves() : genFullPseudolegalMoves();
  }

  /**
   * Gets a generator of all pseudo-legal moves in "inside-out" order.
   * <p>
   * The order can be visualized by dividing the board into "layers" of squares
   * that are equidistant from the edge. The generator first starts with
   * the innermost layer, iterating in row-major order from least column/row
   * to greatest, and then successively generates the outer layers.
   * <p>
   * It is considerably slower that {@link #generatePseudolegalMoves()},
   * but the order that the moves are produced may be preferable in some
   * cases.
   * 
   * @return    An Iterable for iterating through the pseudo-legal moves
   *            of the current player, in an order that roughly starts
   *            from the innermost squares and proceeds out
   */
  public Iterable<Integer> generateInOutPseudolegalMoves() {
    return drop ? genInOutDropPseudolegalMvs() : genInOutFullPseudolegalMvs();
  }

  /**
   * Gets a generator of all legal moves of the current player.
   * <p>
   * It is recommended to use this method over
   * {@link #generatePseudolegalMoves()} except for performance reasons.
   * 
   * @return    An Iterable for iterating through the legal moves of the
   *            current player.
   */
  public Iterable<Integer> generateLegalMoves() {
    if (winner != PLAYER_NONE)
      return Collections.emptyList();
    return generatePseudolegalMoves();
  }

  /**
   * Gets a generator of all legal moves in "inside-out" order.
   * <p>
   * See {@link #generateInOutPseudolegalMoves()} for the meaning of
   * "inside-out" in the context of this method.
   * <p>
   * It is considerably slower that {@link #generateLegalMoves()},
   * but the order that the moves are produced may be preferable in some
   * cases.
   * 
   * @return    An Iterable for iterating through the legal moves
   *            of the current player, in an order that roughly starts
   *            from the innermost squares and proceeds out
   */
  public Iterable<Integer> generateInOutLegalMoves() {
    if (winner != PLAYER_NONE)
      return Collections.emptyList();
    return generateInOutPseudolegalMoves();
  }


  // Private methods
  /* Checks if there is k-in-a-row through the square. */
  private int calculateWinner(int square) {
    int row = getRow(square);
    int col = getCol(square);
    int[][] dirs = { {-1, 1}, {-m, m}, {-m - 1, m + 1}, {-m + 1, m - 1} };
    int[][] lens = { {col, m - 1 - col}, {row, n - 1 - row},
                     {Math.min(col, row), Math.min(m - 1 - col, n - 1 - row)},
                     {Math.min(m - 1 - col, row), Math.min(col, n - 1 - row)} };
    for (int i0 = 0; i0 < 4; i0++) {
      int consecutive = 1;
      for (int i1 = 0; i1 < 2; i1++) {
        for (int index = square, j = 0; j < lens[i0][i1]; j++) {
          if (board[(index += dirs[i0][i1])] != turn)
            break;
          if (++consecutive >= k)
            return turn;
        }
      }
    }
    return PLAYER_NONE;
  }

  /* Gets the minimum of 4 ints. */
  private int min(int i0, int i1, int i2, int i3) {
    return Math.min(Math.min(i0, i1), Math.min(i2, i3));
  }

  /* Gets the number of columns that are "full" -- no empty squares. */
  private int getOccupiedCols() {
    int total = 0;
    for (int i = (n - 1) * m; i < n * m; i++) {
      if (board[i] != PLAYER_NONE)
        total++;
    }
    return total;
  }

  /* Gets a generator for all empty squares. */
  private Iterable<Integer> genFullPseudolegalMoves() {
    return new Iterable<Integer>() {
      @Override
      public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
          int index = 0;

          @Override
          public boolean hasNext() {
            while (index < n * m) {
              if (board[index] == PLAYER_NONE)
                return true;
              index++;
            }
            return false;
          }

          @Override
          public Integer next() {
            if (!hasNext())
              throw new NoSuchElementException();
            return index++;
          }
        };
      }
    };
  }

  /* Gets a generator for all empty squares at the bottom of their columns. */
  private Iterable<Integer> genDropPseudolegalMoves() {
    return new Iterable<Integer>() {
      @Override
      public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
          int index = 0;
          int col = 0;

          @Override
          public boolean hasNext() {
            while (col < m) {
              if (board[index] == PLAYER_NONE)
                return true;
              index += m;
              if (index >= m * n)
                index = ++col;
            }
            return false;
          }

          @Override
          public Integer next() {
            if (!hasNext())
              throw new NoSuchElementException();
            int ret = index;
            index = ++col;
            return ret;
          }
        };
      }
    };
  }

  /* Gets a generator for all empty squares (but in "inside-out" order). */
  private Iterable<Integer> genInOutFullPseudolegalMvs() {
    return new Iterable<Integer>() {
      @Override
      public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
          int index = (m + 1) * Math.min((n - 1) / 2, (m - 1) / 2);
          int start = index;

          boolean shortSideEven = (m > n ? n & 1 : m & 1) == 0;
          int col = 0, ncol = Math.max(0, m - n) + (shortSideEven ? 1 : 0);
          int row = 0, nrow = Math.max(0, n - m) + (shortSideEven ? 1 : 0);

          @Override
          public boolean hasNext() {
            while (true) {
              if (row > nrow) {
                if (start <= 0)
                  return false;
                index = (start -= m + 1);
                col = 0;
                row = 0;
                ncol += 2;
                nrow += 2;
                continue;
              }
              if (col > ncol) {
                index += m - 1 - ncol;
                col = 0;
                row++;
                continue;
              }
              if (board[index] == PLAYER_NONE)
                return true;
              goNext();
            }
          }

          @Override
          public Integer next() {
            if (!hasNext())
              throw new NoSuchElementException();
            int ret = index;
            goNext();
            return ret;
          }

          private void goNext() {
            if (col == 0 && row != 0 && row != nrow && ncol != 0) {
              index += ncol;
              col += ncol;
            } else {
              index++;
              col++;
            }
          }
        };
      }
    };
  }

  /* Gets a generator for all empty squares at the bottom of their columns.
   * (But in "inside-out" order.) */
  private Iterable<Integer> genInOutDropPseudolegalMvs() {
    return new Iterable<Integer>() {
      @Override
      public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
          int index = m / 2;
          int col = m / 2;
          int change = ((m & 1) == 0) ? 1 : -1;

          @Override
          public boolean hasNext() {
            while (col >= 0) {
              if (board[index] == PLAYER_NONE)
                return true;
              index += m;
              if (index >= m * n) {
                goNext();
              }
            }
            return false;
          }

          @Override
          public Integer next() {
            if (!hasNext())
              throw new NoSuchElementException();
            int ret = index;
            goNext();
            return ret;
          }

          private void goNext() {
            index = (col += change);
            change *= -1;
            change += (change > 0) ? 1 : -1;
          }
        };
      }
    };
  }
}
