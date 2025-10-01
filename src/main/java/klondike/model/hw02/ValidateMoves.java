package klondike.model.hw02;

import java.util.List;

final class ValidateMoves {
  private ValidateMoves() {}

  static boolean isValidCascadePlacement(KlondikeCard moving, KlondikeCard destTop) {
    // must be opposite color and exactly one lower than dest top
    return !moving.isSameColor(destTop) && moving.isOneLower(destTop);
  }

  static boolean isValidFoundationPlacement(KlondikeCard moving, List<KlondikeCard> foundation) {
    if (foundation.isEmpty()) {
      return moving.getValue() == Value.ACE;
    }
    KlondikeCard top = foundation.get(foundation.size() - 1);
    // Same suit, and exactly one higher than top
    return moving.getSuit() == top.getSuit()
        && top.isOneLower(moving);
  }
}

