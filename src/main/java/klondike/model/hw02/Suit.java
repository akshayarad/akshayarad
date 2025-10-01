package klondike.model.hw02;

public enum Suit {
  CLUBS, DIAMONDS, HEARTS, SPADES;

  public boolean isRed() {
    return this == DIAMONDS || this == HEARTS;
  }
}

