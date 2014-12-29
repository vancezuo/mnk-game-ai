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
    private String[] aliases;

    public Command(String... aliases) {
      this.aliases = aliases;
    }

    public final boolean hasName(String name) {
      for (String alias : aliases)
        if (alias.equalsIgnoreCase(name))
          return true;
      return false;
    }

    public abstract void execute(Object... args);
  }

  private abstract static class GameCommand extends Command {
    private static final Map<Integer, String> PLAYER_CHAR = new HashMap<Integer, String>() {{
      put(MnkGame.PLAYER_NONE, ".");
      put(MnkGame.PLAYER_1, "X");
      put(MnkGame.PLAYER_2, "O");
    }};

    private MnkGame game;

    public GameCommand(MnkGame game, String... aliases) {
      super(aliases);
      this.game = game;
    }

    public MnkGame getGame() {
      return game;
    }

    public String getPlayerString(int player) {
      return PLAYER_CHAR.get(player);
    }
  }

  private static class DisplayCommand extends GameCommand {
    public DisplayCommand(MnkGame game) {
      super(game, "display", "d");
    }

    @Override
    public void execute(Object... args) {
      int[][] board = getGame().getBoard();
      System.out.print("  ");
      for (int col = 0; col < getGame().getCols(); col++) {
        System.out.printf("%2d", col);
      }
      System.out.println();
      for (int row = 0; row < getGame().getRows(); row++) {
        System.out.printf("%2d ", row);
        for (int col = 0; col < getGame().getCols(); col++) {
          String c = getPlayerString(board[row][col]);
          System.out.print((c != null ? c : '?') + " ");
        }
        System.out.println();
      }
    }
  }

  private static class NewGameCommand extends GameCommand {
    public NewGameCommand(MnkGame game) {
      super(game, "new", "n");
    }

    @Override
    public void execute(Object... args) {
      getGame().reset();
    }
  }

  private static class PlayCommand extends GameCommand {
    public PlayCommand(MnkGame game) {
      super(game, "play", "p");
    }

    @Override
    public void execute(Object... args) {
      if (args.length < 2) {
        System.out.println("Missing row and/or column for move");
        return;
      }
      if (getGame().getWinner() != MnkGame.PLAYER_NONE) {
        String winner = getPlayerString(getGame().getWinner());
        System.out.println("Game over, " + winner + " won");
        return;
      }
      try {
        int row = Integer.parseInt((String) args[0]);
        int col = Integer.parseInt((String) args[1]);
        getGame().doMove(row, col);
        if (getGame().getWinner() != MnkGame.PLAYER_NONE) {
          String winner = getPlayerString(getGame().getWinner());
          System.out.println("Player " + winner + " wins!");
        }
      } catch (NumberFormatException e) {
        System.out.println("Parse error: " + e.getMessage());
      } catch (IllegalArgumentException e) {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }

  private static class UndoCommand extends GameCommand {
    public UndoCommand(MnkGame game) {
      super(game, "undo", "u");
    }

    @Override
    public void execute(Object... args) {
      try {
        getGame().undoMove();
      } catch (IllegalArgumentException e) {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    int m = 3, n = 3, k = 3, p = 1, q = 1;
    if (args.length >= 3) {
      m = Integer.parseInt(args[0]);
      n = Integer.parseInt(args[1]);
      k = Integer.parseInt(args[2]);
    }
    if (args.length >= 5) {
      p = Integer.parseInt(args[3]);
      q = Integer.parseInt(args[4]);
    }

    Scanner in = new Scanner(System.in);
    MnkGame game = new MnkGame(m, n, k, p, q);

    Command[] commands =
        new Command[] {new DisplayCommand(game), new NewGameCommand(game),
                       new PlayCommand(game), new UndoCommand(game)};

    String token = "";
    loop: while (in.hasNext()) {
      token = in.next();
      if (token.equalsIgnoreCase("exit"))
        break;
      for (Command cmd : commands) {
        if (cmd.hasName(token)) {
          cmd.execute((Object[]) in.nextLine().trim().split("\\s+"));
          continue loop;
        }
      }
      System.out.println("Unrecognized command: " + token);
      in.nextLine(); // ignore rest of line
    }

    in.close();
  }
}