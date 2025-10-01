package klondike.model.hw02;

public interface KlondikeCard {
  Suit getSuit();
  Value getValue();
  boolean isFaceUp();
  void flip();
  boolean isOneLower(KlondikeCard other);
  boolean isSameColor(KlondikeCard other);
}

