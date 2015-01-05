/**
 * Copyright 2014 Vance Zuo
 */
package search;

import java.util.List;
import java.util.concurrent.Callable;

import eval.MnkGameEvaluator;
import game.MnkGame;

/**
 * @author Vance Zuo
 * @created Dec 31, 2014
 *
 */
public abstract class MnkGameSearcher {

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


  public final MnkGame getGame() {
    return game;
  }

  public final MnkGameEvaluator getEvaluator() {
    return eval;
  }

  public abstract Result search(int depth);
}