/**
 * Copyright 2014 Vance Zuo
 */
package game;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import search.MnkGameAlphabetaSearcher;
import search.MnkGameMinimaxSearcher;
import search.MnkGameOrderedAbSearcher;
import search.MnkGameSearcher;
import eval.MnkGameBasicEvaluator;
import eval.MnkGameEvaluator;
import eval.MnkGameLineEvaluator;
import eval.MnkGameRandomEvaluator;

/**
 * @author Vance Zuo
 * @created Dec 25, 2014
 *
 */
public class MnkGameDemo {

  private abstract static class Command {
    private MnkGameDemo game;
    private String[] aliases;

    public Command(MnkGameDemo game, String... aliases) {
      this.game = game;
      this.aliases = aliases;
    }

    public final boolean hasName(String name) {
      for (String alias : aliases)
        if (alias.equalsIgnoreCase(name))
          return true;
      return false;
    }

    public final MnkGameDemo getGame() {
      return game;
    }

    public abstract void execute(String... args);
  }

  private static class DisplayCommand extends Command {
    public DisplayCommand(MnkGameDemo game) {
      super(game, "display", "d");
    }

    @Override
    public void execute(String... args) {
      getGame().display();
    }
  }

  private static class NewGameCommand extends Command {
    public NewGameCommand(MnkGameDemo game) {
      super(game, "new", "n");
    }

    @Override
    public void execute(String... args) {
      int m = 3, n = 3, k = 3, p = 1, q = 1;
      try {
        if (args.length >= 3) {
          m = Integer.parseInt(args[0]);
          n = Integer.parseInt(args[1]);
          k = Integer.parseInt(args[2]);
        }
        if (args.length >= 5) {
          p = Integer.parseInt(args[3]);
          q = Integer.parseInt(args[4]);
        }
      } catch (NumberFormatException e) {
        System.out.println("Parse error: " + e.getMessage());
        return;
      }
      getGame().newGame(m, n, k, p, q);
    }
  }

  private static class PlayCommand extends Command {
    public PlayCommand(MnkGameDemo game) {
      super(game, "play", "p");
    }

    @Override
    public void execute(String... args) {
      if (args.length < 2) {
        System.out.println("Missing row and/or column for move");
        return;
      }
      int row, col;
      try {
        row = Integer.parseInt(args[0]);
        col = Integer.parseInt(args[1]);
      } catch (NumberFormatException e) {
        System.out.println("Parse error: " + e.getMessage());
        return;
      }
      getGame().playMove(row, col);
    }
  }

  private static class AiPlayCommand extends Command {
    public AiPlayCommand(MnkGameDemo game) {
      super(game, "ai", "a");
    }

    @Override
    public void execute(String... args) {
      try {
        getGame().computerMove();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  private static class AiSetDepthCommand extends Command {
    public AiSetDepthCommand(MnkGameDemo game) {
      super(game, "set-depth", "sd");
    }

    @Override
    public void execute(String... args) {
      try {
        getGame().setComputerDepth(Integer.parseInt(args[0]));
      } catch (NumberFormatException e) {
        System.out.println("Parse error: " + e.getMessage());
      } catch (ArrayIndexOutOfBoundsException e) {
        System.out.println("No depth specified.");
      }
    }
  }

  private static class AiSetTimeCommand extends Command {
    public AiSetTimeCommand(MnkGameDemo game) {
      super(game, "set-time", "st");
    }

    @Override
    public void execute(String... args) {
      try {
        getGame().setComputerTime(Integer.parseInt(args[0]) * 1000);
      } catch (NumberFormatException e) {
        System.out.println("Parse error: " + e.getMessage());
      } catch (ArrayIndexOutOfBoundsException e) {
        System.out.println("No time limit specified.");
      }
    }
  }

  private static class AiSetEvalCommand extends Command {
    public AiSetEvalCommand(MnkGameDemo game) {
      super(game, "set-eval", "se");
    }

    @Override
    public void execute(String... args) {
      try {
        getGame().setComputerEval(args[0]);
      } catch (ArrayIndexOutOfBoundsException e) {
        System.out.println("No evaluator class specified.");
      }
    }
  }

  private static class AiSetSearchCommand extends Command {
    public AiSetSearchCommand(MnkGameDemo game) {
      super(game, "set-search", "ss");
    }

    @Override
    public void execute(String... args) {
      try {
        getGame().setComputerSearch(args[0]);
      } catch (ArrayIndexOutOfBoundsException e) {
        System.out.println("No searcher class specified.");
      }
    }
  }

  private static class UndoCommand extends Command {
    public UndoCommand(MnkGameDemo game) {
      super(game, "undo", "u");
    }

    @Override
    public void execute(String... args) {
      int times = 1;
      try {
        if (args.length >= 1)
          times = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.out.println("Parse error: " + e.getMessage());
        return;
      }
      getGame().undoMove(times);
    }
  }


  private Map<Integer, String> playerCharMap;
  private Map<String, Class<? extends MnkGameEvaluator>> evaluatorMap;
  private Map<String, Class<? extends MnkGameSearcher>> searcherMap;
  private MnkGame game;
  private MnkGameAi ai;

  public MnkGameDemo() {
    game = new MnkGame();
    playerCharMap = new HashMap<Integer, String>() {{
      put(MnkGame.PLAYER_NONE, ".");
      put(MnkGame.PLAYER_1, "X");
      put(MnkGame.PLAYER_2, "O");
    }};
    evaluatorMap = new HashMap<String, Class<? extends MnkGameEvaluator>>() {{
      put("basic", MnkGameBasicEvaluator.class);
      put("random", MnkGameRandomEvaluator.class);
      put("line", MnkGameLineEvaluator.class);
    }};
    searcherMap = new HashMap<String, Class<? extends MnkGameSearcher>>() {{
      put("minimax", MnkGameMinimaxSearcher.class);
      put("alphabeta", MnkGameAlphabetaSearcher.class);
      put("alphabeta+", MnkGameOrderedAbSearcher.class);
    }};
    ai = new MnkGameAi(game);
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    Scanner in = new Scanner(System.in);
    MnkGameDemo game = new MnkGameDemo();

    Command[] cmds =
        new Command[] {new DisplayCommand(game), new NewGameCommand(game),
                       new PlayCommand(game), new UndoCommand(game),
                       new AiPlayCommand(game), new AiSetDepthCommand(game),
                       new AiSetTimeCommand(game), new AiSetEvalCommand(game),
                       new AiSetSearchCommand(game)};

    String token = "";
    loop: while (in.hasNext()) {
      token = in.next();
      if (token.equalsIgnoreCase("exit"))
        break;
      for (Command cmd : cmds) {
        if (cmd.hasName(token)) {
          String cmdArgs = in.nextLine().trim();
          if (cmdArgs.equals("")) {
            cmd.execute();
          } else {
            cmd.execute(cmdArgs.split("\\s+"));
          }
          continue loop;
        }
      }
      System.out.println("Unrecognized command: " + token);
      in.nextLine(); // ignore rest of line
    }

    in.close();
  }


  private void newGame(int m, int n, int k, int p, int q) {
    game = new MnkGame(m, n, k, p, q);
    ai.setGame(game);
    System.out.printf("New game: %d by %d board; %d-in-a-row", n, m, k);
    if (p > 1 || q > 1)
      System.out.printf("; %d, %d, %d... moves", q, p, p);
    System.out.println(".");
  }

  private void display() {
    int[][] board = game.getBoard();
    System.out.print("  ");
    for (int col = 0; col < game.getCols(); col++) {
      System.out.printf("%2d", col);
    }
    System.out.println();
    for (int row = game.getRows() - 1; row >= 0; row--) {
      System.out.printf("%2d ", row);
      for (int col = 0; col < game.getCols(); col++) {
        String c = playerCharMap.get(board[row][col]);
        System.out.print((c != null ? c : '?') + " ");
      }
      System.out.println();
    }
  }

  private void setComputerTime(int millis) {
    try {
      ai.setMaxTime(millis);
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid time value. No changes made.");
    }
  }

  private void setComputerDepth(int depth) {
    try {
      ai.setMaxDepth(depth);
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid depth value. No changes made.");
    }
  }

  private void setComputerEval(String mode) {
    if (!evaluatorMap.containsKey(mode)) {
      System.out.print("Invalid evaluation mode. Valid:");
      for (String s : evaluatorMap.keySet())
        System.out.print(" " + s);
      System.out.println(".");
      return;
    }
    ai.setEvaluator(evaluatorMap.get(mode));
  }

  private void setComputerSearch(String mode) {
    if (!searcherMap.containsKey(mode)) {
      System.out.print("Invalid search mode. Valid:");
      for (String s : searcherMap.keySet())
        System.out.print(" " + s);
      System.out.println(".");
      return;
    }
    ai.setSearcher(searcherMap.get(mode));
  }

  private void playMove(int row, int col) {
    if (checkGameOver("Game over, player %s won.", "Game over, draw."))
      return;
    try {
      game.doMove(row, col);
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
      return;
    }
    checkGameOver("Player %s has won!", "Draw!");
  }

  private void computerMove() throws ExecutionException {
    if (checkGameOver("Game over, player %s won.", "Game over, draw."))
      return;
    int move = ai.think();
    playMove(game.getRow(move), game.getCol(move));
  }

  private boolean checkGameOver(String winFormat, String drawFormat) {
    if (game.isGameOver()) {
      String winner = playerCharMap.get(game.getWinner());
      System.out.printf(game.hasWinner() ? winFormat : drawFormat, winner);
      System.out.println();
      return true;
    }
    return false;
  }

  private void undoMove(int times) {
    int i = 0;
    try {
      while (i < times) {
        game.undoMove();
        i++;
      }
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
      if (i > 0)
        System.out.println("Was to undo " + i + " moves.");
    }
  }
}