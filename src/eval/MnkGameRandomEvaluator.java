/**
 * Copyright 2015 Vance Zuo
 */
package eval;

import game.MnkGame;

import java.util.Random;

/**
 * @author Vance Zuo
 * @created Jan 6, 2015
 *
 */
public class MnkGameRandomEvaluator extends MnkGameEvaluator {

  private static final Random rand = new Random();


  public MnkGameRandomEvaluator(MnkGame game) {
    super(game);
  }


  @Override
  public int evaluate() {
    int winner = getGame().getWinner();
    switch (winner) {
      case MnkGame.PLAYER_1:
        return MAX_SCORE;
      case MnkGame.PLAYER_2:
        return MIN_SCORE;
      default:
        return rand.nextInt(MAX_SCORE) - MAX_SCORE / 2;
    }
  }

}
