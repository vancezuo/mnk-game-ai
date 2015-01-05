/**
 * Copyright 2015 Vance Zuo
 */
package eval;

import game.MnkGame;

/**
 * @author Vance Zuo
 * @created Jan 5, 2015
 *
 */
public class MnkGameBasicEvaluator extends MnkGameEvaluator {

  public MnkGameBasicEvaluator(MnkGame game) {
    super(game);
  }

  @Override
  public int evaluate()  {
    int winner = getGame().getWinner();
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
