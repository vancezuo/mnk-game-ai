/**
 * Copyright 2015 Vance Zuo
 */
package search;

import eval.MnkGameEvaluator;
import game.MnkGame;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * @author Vance Zuo
 * @created Jan 19, 2015
 *
 */
public class MnkGameOrderedAbSearcher extends MnkGameAlphabetaSearcher {

  private static class WeightedMove {
    Integer move;
    int score;

    public WeightedMove(Integer move, int score) {
      this.move = move;
      this.score = score;
    }
  }


  protected int[] weights;
  private int lastPly;


  public MnkGameOrderedAbSearcher(MnkGame game,
      Class<? extends MnkGameEvaluator> eval) {
    super(game, eval);
    weights = new int[game.getSquares()];
    for (int i = 0; i < weights.length; i++) {
      int top = game.getRow(i);
      int bottom = game.getRows() - 1 - top;
      int left = game.getCol(i);
      int right = game.getCols() - 1 - left;
      weights[i] = Math.min(Math.min(top, bottom), Math.min(left, right));
    }
    lastPly = 0;
  }


  @Override
  protected Iterable<Integer> generateMoves() {
    updateWeights();

    Comparator<WeightedMove> comp = new Comparator<WeightedMove>() {
      @Override
      public int compare(WeightedMove m1, WeightedMove m2) {
        return m2.score - m1.score; // reverse
      }
    };
    PriorityQueue<WeightedMove> moves = new PriorityQueue<>(numMoves(), comp);
    for (Integer move : super.generateMoves())
      moves.add(new WeightedMove(move, weights[move]));

    return new Iterable<Integer>() {
      @Override
      public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
          @Override
          public boolean hasNext() {
            return !moves.isEmpty();
          }

          @Override
          public Integer next() {
            return moves.remove().move;
          }
        };
      }
    };
  }

  protected void updateWeights() {
    int currentPly = getGame().getElapsedPly();
    while (lastPly > currentPly)
      updateMove(getGame().getHistory(--lastPly), true);
    while (lastPly < currentPly)
      updateMove(getGame().getHistory(lastPly++), false);
    /* for (int i = 0; i < weights.length; i++) {
      if (i % getGame().getCols() == 0)
        System.out.println();
      System.out.printf("%3d", weights[i]);
    } // */
  }

  protected void updateMove(int square, boolean undo) {
    int m = getGame().getCols();
    int n = getGame().getRows();
    int k = getGame().getK();
    int row = getGame().getRow(square);
    int col = getGame().getCol(square);
    int[] dirs = {-1, 1, -m, m, -m - 1, m + 1, -m + 1, m - 1};
    int[] lens = {col, m - 1 - col, row, n - 1 - row, Math.min(col, row),
                  Math.min(m - 1 - col, n - 1 - row),
                  Math.min(m - 1 - col, row), Math.min(col, n - 1 - row)};
    for (int i = 0; i < dirs.length; i++) {
      for (int j = 1; j < Math.min(k, lens[i]); j++) {
        weights[square + dirs[i] * j] += (undo ? -1 : 1) * (k - j);
      }
    }
  }

}
