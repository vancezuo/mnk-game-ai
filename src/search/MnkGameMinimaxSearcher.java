/**
 * Copyright 2015 Vance Zuo
 */
package search;

import java.util.ArrayList;
import java.util.List;

import eval.MnkGameEvaluator;

/**
 * @author Vance Zuo
 * @created Jan 5, 2015
 *
 */
public class MnkGameMinimaxSearcher extends MnkGameSearcher {

  public MnkGameMinimaxSearcher(MnkGameEvaluator eval) {
    super(eval);
  }

  @Override
  public Result search(int depth) {
    incrementNodeCount();

    if (getGame().isGameOver())
      return new Result(getEvaluator().evaluate(), null, true);
    if (depth <= 0)
      return new Result(getEvaluator().evaluate(), null, false);

    boolean maxi = getGame().getCurrentPlayer() == MnkGameEvaluator.PLAYER_MAX;

    int score = maxi ? MnkGameEvaluator.MIN_SCORE : MnkGameEvaluator.MAX_SCORE;
    List<Integer> pv = new ArrayList<>(depth);
    boolean proof = false;
    for (int move : getGame().generatePseudolegalMoves()) {
      getGame().doMove(move, false);
      Result result = search(depth - 1);
      getGame().undoMove();
      if (Thread.currentThread().isInterrupted())
        return null;
      if (maxi ? result.getScore() > score : result.getScore() < score) {
        score = result.getScore();
        pv.clear();
        pv.add(move);
        if (result.getPrincipleVaration() != null)
          pv.addAll(result.getPrincipleVaration());
        proof = result.isProvenResult();
      }
    }

    if (score == MnkGameEvaluator.MIN_SCORE + depth - 1) {
      score++;
    } else if (score == MnkGameEvaluator.MAX_SCORE - depth + 1) {
      score--;
    }

    return new Result(score, pv, proof);
  }
}
