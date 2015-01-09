/**
 * Copyright 2014 Vance Zuo
 */
package game;

import java.util.Collections;
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

  public boolean hasWinner() {
    return winner != PLAYER_NONE;
  }

  public boolean isGameOver() {
    return hasWinner() || (getOccupiedSquares() == getSquares());
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

  public int getSquares() {
    return m * n;
  }

  public int getSquare(int row, int col) {
    return row*m + col;
  }

  public int getRow(int square) {
    return square / m;
  }

  public int getCol(int square) {
    return square % m;
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

  public int getElapsedPly() {
    return ply;
  }

  public int getElapsedTurns(int totalPly) {
    if (totalPly < q)
      return 0;
    return 1 + (totalPly - q) % p;
  }

  public int getElapsedTurns() {
    return getElapsedTurns(ply);
  }

  public int getTurnRemainingMoves(int totalPly) {
    if (totalPly < q)
      return q - totalPly;
    return p - (totalPly - q) % p;
  }

  public int getTurnRemainingMoves() {
    return getTurnRemainingMoves(ply);
  }

  public int getPseudolegalMoves() {
    return getSquares() - getOccupiedSquares();
  }

  public int getLegalMoves() {
    if (winner != PLAYER_NONE)
      return 0;
    return getPseudolegalMoves();
  }

  public Iterable<Integer> generatePseudolegalMoves() {
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

  public Iterable<Integer> generateInOutPseudolegalMoves() {
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

  public Iterable<Integer> generateLegalMoves() {
    if (winner != PLAYER_NONE)
      return Collections.emptyList();
    return generatePseudolegalMoves();
  }

  public Iterable<Integer> generateInOutLegalMoves() {
    if (winner != PLAYER_NONE)
      return Collections.emptyList();
    return generateInOutPseudolegalMoves();
  }


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

}
