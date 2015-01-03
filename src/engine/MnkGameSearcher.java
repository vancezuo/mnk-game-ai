/**
 * Copyright 2014 Vance Zuo
 */
package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Vance Zuo
 * @created Dec 31, 2014
 *
 */
public class MnkGameSearcher {

  public static class Task implements Callable<Result> {
    private final MnkGameSearcher searcher;
    private final int depth;

    public Task(MnkGameSearcher searcher, int depth) {
      this.searcher = searcher;
      this.depth = depth;
    }

    @Override
    public Result call() throws Exception {
      return searcher.search(depth);
    }
  }

  public static class Result {
    private final int score;
    private final List<Integer> pv;
    private final boolean proof;

    public Result(int score, List<Integer> pv, boolean proof) {
      this.score = score;
      this.pv = pv;
      this.proof = proof;
    }

    public int getScore() {
      return score;
    }

    public double getScoreScaled() {
      return score / (double) MnkGameEvaluator.MAX_SCORE;
    }

    public List<Integer> getPrincipleVaration() {
      return pv;
    }

    public int getPrincipleVarationDepth() {
      return pv.size();
    }

    public int getPrincipleVariationMove() {
      return pv.get(0);
    }

    public boolean isProvenResult() {
      return proof;
    }
  }


  private MnkGame game;
  private MnkGameEvaluator eval;


  public MnkGameSearcher(MnkGameEvaluator eval) {
    this.game = eval.getGame();
    this.eval = eval;
  }


  public MnkGame getGame() {
    return game;
  }

  public Result search(int depth) {
    return minimax(depth);
  }

  public Result minimax(int depth) {
    if (game.isGameOver() || depth <= 0)
      return new Result(eval.evaluate(), null, game.isGameOver());
    boolean maxi = (game.getCurrentPlayer() == MnkGameEvaluator.PLAYER_MAX);
    int score = maxi ? MnkGameEvaluator.MIN_SCORE : MnkGameEvaluator.MAX_SCORE;
    List<Integer> pv = new ArrayList<>(depth);
    boolean proof = false;
    for (int move : game.generatePseudolegalMoves()) {
      game.doMove(move, false);
      Result result = minimax(depth - 1);
      game.undoMove();
      if ((maxi && result.score > score) || (!maxi && result.score < score)) {
        score = result.score;
        pv.clear();
        pv.add(move);
        if (result.pv != null)
          pv.addAll(result.pv);
        proof = result.proof;
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
