/**
 * Copyright 2014 Vance Zuo
 */
package engine;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Vance Zuo
 * @created Dec 20, 2014
 *
 */
public class MnkGame {

  public static final int PLAYER_NONE = 0;
  public static final int PLAYER_1 = 1;
  public static final int PLAYER_2 = -PLAYER_1;


  private final int m, n, k, p, q;
  private final int[] board;
  private final int[] history;
  private int ply;
  private int turn, winner;


  public MnkGame(int m, int n, int k, int p, int q) {
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
    board = new int[n * m];
    history = new int[n * m];
    ply = 0;
    turn = PLAYER_1;
    winner = PLAYER_NONE;
  }

  public MnkGame(int m, int n, int k) {
    this(m, n, k, 1, 1);
  }

  public MnkGame() {
    this(3, 3, 3);
  }

  public void doMove(int row, int col, boolean checkLegal) {
    doMove(getSquare(row, col));
  }

  public void doMove(int square, boolean checkLegal) {
    if (checkLegal && !canDoMove(square))
      throw new IllegalArgumentException("Illegal move.");
    board[square] = turn;
    history[ply++] = square;
    winner = calculateWinner(square);
    if (ply >= q && (ply - q) % p == 0)
      turn = -turn;
  }

  public void doMove(int row, int col) {
    doMove(row, col, true);
  }

  public void doMove(int square) {
    doMove(square, true);
  }

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

  public void undoMove() {
    undoMove(true);
  }

  public boolean canDoMove(int square) {
    return (0 <= square && square < n * m) && (board[square] == PLAYER_NONE)
        && (winner == PLAYER_NONE);
  }

  public boolean canUndoMove() {
    return ply > 0;
  }

  public boolean isGameOver() {
    return (winner != PLAYER_NONE);
  }

  public int getCols() {
    return m;
  }

  public int getRows() {
    return n;
  }

  public int getK() {
    return k;
  }

  public int getTurnMoves() {
    return p;
  }

  public int getFirstTurnMoves() {
    return q;
  }

  public int getOccupiedSquares() {
    return ply;
  }

  public int getCurrentPlayer() {
    return turn;
  }

  public int getWinner() {
    return winner;
  }

  public int getTotalSquares() {
    return m * n;
  }

  public int getSquare(int row, int col) {
    return row*m + col;
  }

  public int getRow(int square) {
    return square / n;
  }

  public int getCol(int square) {
    return square % n;
  }

  public int getPiece(int square) {
    return board[square];
  }

  public int[] getHistory() {
    int[] historyTrimmed = new int[ply];
    for (int i = 0; i < ply; i++)
      historyTrimmed[i] = history[i];
    return historyTrimmed;
  }

  public int[][] getBoard() {
    int[][] board2d = new int[n][m];
    for (int row = 0; row < n; row++)
      for (int col = 0; col < m; col++)
        board2d[row][col] = board[getSquare(row, col)];
    return board2d;
  }

  public int getElapsedTurns() {
    if (ply < q)
      return 0;
    return 1 + (ply - q) % p;
  }

  public Iterator<Integer> getLegalMoves() {
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


  private int calculateWinner(int square) {
    int row = getRow(square);
    int col = getCol(square);
    int[][] dirs = { {-1, 1}, {-m, m}, {-m - 1, m + 1}, {-m + 1, m - 1} };
    int[][] lens = { {col, m - 1 - col}, {row, n - 1 - row},
                     {Math.min(col, row), Math.min(n - 1 - col, m - 1 - row)},
                     {Math.min(n - 1 - col, row), Math.min(col, m - 1 - row)} };
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

}
