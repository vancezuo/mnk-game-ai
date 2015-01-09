/**
 * Copyright 2015 Vance Zuo
 */
package search;

import eval.MnkGameEvaluator;
import game.MnkGame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vance Zuo
 * @created Jan 8, 2015
 *
 */
public class MnkGameAlphabetaSearcher extends MnkGameSearcher {

  public MnkGameAlphabetaSearcher(MnkGame game,
      Class<? extends MnkGameEvaluator> eval) {
    super(game, eval);
  }


  @Override
  public Result search(int depth) {
    return search(depth, MnkGameEvaluator.MIN_SCORE, MnkGameEvaluator.MAX_SCORE);
  }

  private Result search(int depth, int alpha, int beta) {
    incrementNodeCount();

    if (getGame().isGameOver())
      return new Result(getEvaluator().evaluate(), null, true);
    if (depth <= 0)
      return new Result(getEvaluator().evaluate(), null, false);

    boolean maxi = getGame().getCurrentPlayer() == MnkGameEvaluator.PLAYER_MAX;

    List<Integer> pv = new ArrayList<>(depth);
    boolean proof = false;
    for (int move : getGame().generatePseudolegalMoves()) {
      getGame().doMove(move, false);
      Result result = search(depth - 1, alpha, beta);
      getGame().undoMove();
      if (Thread.currentThread().isInterrupted())
        return null;
      int score = result.getScore();
      if (maxi ? score > alpha : score < beta) {
        if (maxi) alpha = score; else beta = score;
        if (alpha >= beta)
          break;
        pv.clear();
        pv.add(move);
        if (result.getPrincipleVaration() != null)
          pv.addAll(result.getPrincipleVaration());
        proof = result.isProvenResult();
      }
    }

    return new Result(maxi ? alpha : beta, pv, proof);
  }

}
