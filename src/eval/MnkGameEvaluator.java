/**
 * Copyright 2014 Vance Zuo
 */
package eval;

import game.MnkGame;

/**
 * @author Vance Zuo
 * @created Dec 30, 2014
 *
 */
public abstract class MnkGameEvaluator {

  public static final int PLAYER_MAX = MnkGame.PLAYER_1;
  public static final int PLAYER_MIN = MnkGame.PLAYER_2;

  public static final int MAX_SCORE = 1 << 30;
  public static final int MIN_SCORE = -MAX_SCORE;


  private MnkGame game;


  public MnkGameEvaluator(MnkGame game) {
    this.game = game;
  }


  public final MnkGame getGame() {
    return game;
  }

  public abstract int evaluate();

}
