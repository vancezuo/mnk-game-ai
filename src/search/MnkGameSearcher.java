/**
 * Copyright 2014 Vance Zuo
 */
package search;

import java.lang.reflect.InvocationTargetException;
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

    public int getPrincipleVarationLength() {
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
  private long nodes;


  public MnkGameSearcher(MnkGame game, Class<? extends MnkGameEvaluator> eval) {
    this.game = game;
    try {
      this.eval = eval.getConstructor(MnkGame.class).newInstance(game);
    } catch (NoSuchMethodException | SecurityException | InstantiationException
        | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new IllegalArgumentException(e);
    }
  }


  protected final void incrementNodeCount() {
    nodes++;
  }

  protected Iterable<Integer> generateMoves() {
    return getGame().generatePseudolegalMoves();
  }

  protected int numMoves() {
    return getGame().getPseudolegalMoves();
  }

  public final MnkGame getGame() {
    return game;
  }

  public final MnkGameEvaluator getEvaluator() {
    return eval;
  }

  public final long getNodeCount() {
    return nodes;
  }

  public abstract Result search(int depth);
}