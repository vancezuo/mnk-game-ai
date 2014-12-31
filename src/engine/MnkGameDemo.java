/**
 * Copyright 2014 Vance Zuo
 */
package engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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

  private Map<Integer, String> playerCharacter;
  private MnkGame game;

  public MnkGameDemo() {
    game = new MnkGame();
    playerCharacter = new HashMap<Integer, String>() {{
      put(MnkGame.PLAYER_NONE, ".");
      put(MnkGame.PLAYER_1, "X");
      put(MnkGame.PLAYER_2, "O");
    }};
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    Scanner in = new Scanner(System.in);
    MnkGameDemo game = new MnkGameDemo();

    Command[] cmds = new Command[] {new DisplayCommand(game),
                                    new NewGameCommand(game),
                                    new PlayCommand(game),
                                    new UndoCommand(game)};

    String token = "";
    loop: while (in.hasNext()) {
      token = in.next();
      if (token.equalsIgnoreCase("exit"))
        break;
      for (Command cmd : cmds) {
        if (cmd.hasName(token)) {
          cmd.execute(in.nextLine().trim().split("\\s+"));
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
    for (int row = 0; row < game.getRows(); row++) {
      System.out.printf("%2d ", row);
      for (int col = 0; col < game.getCols(); col++) {
        String c = playerCharacter.get(board[row][col]);
        System.out.print((c != null ? c : '?') + " ");
      }
      System.out.println();
    }
  }

  private void playMove(int row, int col) {
    if (game.getWinner() != MnkGame.PLAYER_NONE) {
      String winner = playerCharacter.get(game.getWinner());
      System.out.println("Game over, " + winner + " won.");
      return;
    }
    try {
      game.doMove(row, col);
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
      return;
    }
    if (game.getWinner() != MnkGame.PLAYER_NONE) {
      String winner = playerCharacter.get(game.getWinner());
      System.out.println("Player " + winner + " wins!");
    }
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
      System.out.println("Able to undo " + i + " moves.");
    }
  }
}