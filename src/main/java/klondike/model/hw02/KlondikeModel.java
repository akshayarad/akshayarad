package klondike.model.hw02;

import java.util.List;

public interface KlondikeModel<C extends KlondikeCard> {

  List<C> createNewDeck();

  void startGame(List<C> deck, boolean shuffle, int numPiles, int numDraw)
      throws IllegalArgumentException, IllegalStateException;

  void movePile(int srcPile, int numCards, int destPile)
      throws IllegalArgumentException, IllegalStateException;

  void moveDraw(int destPile) throws IllegalArgumentException, IllegalStateException;

  void moveToFoundation(int srcPile, int foundationPile)
      throws IllegalArgumentException, IllegalStateException;

  void moveDrawToFoundation(int foundationPile)
      throws IllegalArgumentException, IllegalStateException;

  void discardDraw() throws IllegalStateException;

  int getNumRows() throws IllegalStateException;

  int getNumPiles() throws IllegalStateException;

  int getNumDraw() throws IllegalStateException;

  boolean isGameOver() throws IllegalStateException;

  int getScore() throws IllegalStateException;

  int getPileHeight(int pileNum) throws IllegalArgumentException, IllegalStateException;

  C getCardAt(int pileNum, int card) throws IllegalArgumentException, IllegalStateException;

  C getCardAt(int foundationPile) throws IllegalArgumentException, IllegalStateException;

  boolean isCardVisible(int pileNum, int card) throws IllegalArgumentException, IllegalStateException;

  List<C> getDrawCards() throws IllegalStateException;

  int getNumFoundations() throws IllegalStateException;
}

