/**
 * Copyright 2014 Vance Zuo
 */
package engine;

/**
 * @author Vance Zuo
 * @created Dec 30, 2014
 *
 */
public class MnkGameEvaluator {

  public static final int PLAYER_MAX = MnkGame.PLAYER_1;
  public static final int PLAYER_MIN = MnkGame.PLAYER_2;

  public static final int MAX_SCORE = 1 << 30;
  public static final int MIN_SCORE = -MAX_SCORE;


  private MnkGame game;


  public MnkGameEvaluator(MnkGame game) {
    this.game = game;
  }


  public MnkGame getGame() {
    return game;
  }

  public int evaluate() {
    int winner = game.getWinner();
    switch (winner) {
      case MnkGame.PLAYER_1:
        return MAX_SCORE;
      case MnkGame.PLAYER_2:
        return MIN_SCORE;
      default:
        return 0;
    }
  }

}
