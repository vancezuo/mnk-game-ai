/**
 * Copyright 2014 Vance Zuo
 */
package game;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import search.MnkGameMinimaxSearcher;
import search.MnkGameSearcher;
import eval.MnkGameRandomEvaluator;


/**
 * @author Vance Zuo
 * @created Dec 31, 2014
 *
 */
public class MnkGameAi {

  public static final int LOG_ALL = 0b11;
  public static final int LOG_PV = 0b10;
  public static final int LOG_MOVE = 0b01;
  public static final int LOG_NONE = 0b00;

  public static final int MIN_DEPTH = 1;
  public static final int MAX_DEPTH = Integer.MAX_VALUE;
  public static final int MIN_TIME = 10;
  public static final int MAX_TIME = Integer.MAX_VALUE;

  private MnkGameSearcher searcher;

  private int depth;
  private int time;

  private int log;


  public MnkGameAi(MnkGame game) {
    searcher = new MnkGameMinimaxSearcher(new MnkGameRandomEvaluator(game));

    depth = MAX_DEPTH;
    time = MAX_TIME;

    log = LOG_ALL;
  }


  public void setMaxDepth(int depth) {
    if (depth < MIN_DEPTH || depth > MAX_DEPTH)
      throw new IllegalArgumentException("Invalid search depth: " + depth);
    this.depth = depth;
  }

  public void setMaxTime(int time) {
    if (time < MIN_TIME || time > MAX_TIME)
      throw new IllegalArgumentException("Invalid search time: " + time);
    this.time = time;
  }

  public void setLogPv(boolean enabled) {
    log = enabled ? (log | LOG_PV) : (log & ~LOG_PV);
  }

  public void setLogMove(boolean enabled) {
    log = enabled ? (log | LOG_MOVE) : (log & ~LOG_MOVE);
  }

  public int getMaxDepth() {
    return depth;
  }

  public int getMaxTime() {
    return time;
  }

  public MnkGame getGame() {
    return searcher.getGame();
  }

  public int think() throws ExecutionException {
    if (searcher.getGame().isGameOver())
      throw new IllegalStateException("Game over. No legal moves.");

    ExecutorService executor = Executors.newSingleThreadExecutor();
    long timeStart = System.currentTimeMillis();
    long timeEnd = timeStart + time;
    long nodesStart = searcher.getNodeCount();
    MnkGameSearcher.Result result = null;
    if ((log & LOG_PV) != 0)
      printSearchResultHeader();
    for (int i = 1; i <= depth; i++) {
      MnkGameSearcher.Task task = new MnkGameSearcher.Task(searcher, i);
      Future<MnkGameSearcher.Result> future = executor.submit(task);
      long timeRemaining = timeEnd - System.currentTimeMillis();
      try {
        result = future.get(timeRemaining, TimeUnit.MILLISECONDS);
      } catch (TimeoutException | InterruptedException e) {
        break;
      } finally {
        future.cancel(true);
      }
      if ((log & LOG_PV) != 0)
        printSearchResult(result, i, System.currentTimeMillis() - timeStart,
            searcher.getNodeCount() - nodesStart);
      if (result.isProvenResult())
        break;
    }

    int move;
    if (result != null) {
      move = result.getPrincipleVariationMove();
    } else {
      move = generateRandomMove();
    }
    if ((log & LOG_MOVE) != 0) {
      int row = getGame().getRow(move);
      int col = getGame().getCol(move);
      System.out.println("AI move: (" + row + ", " + col + ")");
    }
    return move;
  }


  private void printSearchResultHeader() {
    System.out.println("Depth\tTime\tNodes\tScore\tVariation");
  }

  private void printSearchResult(MnkGameSearcher.Result r, int d, long t, long n) {
    System.out.printf("%d\t", d);
    System.out.printf("%.3f\t", t / 1000.0);
    System.out.printf("%d\t", n);
    System.out.printf("% .3f\t", r.getScoreScaled());
    for (int move : r.getPrincipleVaration()) {
      int row = getGame().getRow(move);
      int col = getGame().getCol(move);
      System.out.print(" " + row + "," + col);
    }
    System.out.println();
  }

  private int generateRandomMove() {
    Random rand = new Random();

    int i = 0;
    int totalMoves = getGame().getLegalMoves();
    for (int move : getGame().generateLegalMoves()) {
      if (rand.nextInt(totalMoves - i) == 0)
        return move;
      i++;
    }

    throw new IllegalStateException("Failed to generate move.");
  }
}
