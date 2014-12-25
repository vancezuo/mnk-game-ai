/**
 * Copyright 2014 Vance Zuo
 */
package engine;

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
  private int turn;


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
  }

  public MnkGame(int m, int n, int k) {
    this(m, n, k, 1, 1);
  }

  public MnkGame() {
    this(3, 3, 3);
  }

  public boolean canDoMove(int row, int col) {
    return (0 <= row && row < n) && (0 <= col && col < m)
        && (board[row * m + col] == PLAYER_NONE);
  }

  public void doMove(int row, int col) {
    int index = row*m + col;
    board[index] = turn;
    history[ply++] = index;
    if (ply >= q && (ply - q) % p == 0)
      turn = -turn;
  }

  public boolean canUndoMove() {
    return ply > 0;
  }

  public void undoMove() {
    int index = history[ply - 1];
    if (ply >= q && (ply - q) % p == 0)
      turn = -turn;
    ply--;
    board[index] = PLAYER_NONE;
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

  public int getCurrentPlayer() {
    return turn;
  }

  public int[] getHistory() {
    int[] historyTrimmed = new int[ply];
    for (int i = 0; i < ply; i++)
      historyTrimmed[i] = history[i];
    return historyTrimmed;
  }

  public int[][] getBoard() {
    int[][] board2d = new int[n][m];
    for (int i = 0; i < n; i++)
      for (int j = 0; j < m; j++)
        board2d[i][j] = board[i*m + j];
    return board2d;
  }

  public int getElapsedTurns() {
    if (ply < q)
      return 0;
    return 1 + (ply - q) % p;
  }

}
