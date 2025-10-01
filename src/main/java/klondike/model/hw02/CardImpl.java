package klondike.model.hw02;

import java.util.Objects;

public class CardImpl implements KlondikeCard {
  private final Suit suit;
  private final Value value;
  private boolean faceUp;

  public CardImpl(Suit suit, Value value, boolean faceUp) {
    this.suit = Objects.requireNonNull(suit);
    this.value = Objects.requireNonNull(value);
    this.faceUp = faceUp;
  }

  @Override
  public Suit getSuit() {
    return suit;
  }

  @Override
  public Value getValue() {
    return value;
  }

  @Override
  public boolean isFaceUp() {
    return faceUp;
  }

  @Override
  public void flip() {
    faceUp = !faceUp;
  }

  @Override
  public boolean isOneLower(KlondikeCard other) {
    return this.value.getNumber() + 1 == other.getValue().getNumber();
  }

  @Override
  public boolean isSameColor(KlondikeCard other) {
    return this.suit.isRed() == other.getSuit().isRed();
  }
}

