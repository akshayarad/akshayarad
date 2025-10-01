package klondike.model.hw02;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * This represents the basic implementation of the Klondike game model.
 * It manages the overall game state, including cascade piles, foundation piles,
 * the draw pile, and the draw cards that are visible to the player. The game has to be started
 * with startGame before any valid moves can be made.
 */
public class BasicKlondike implements KlondikeModel<KlondikeCard> {

  private List<List<KlondikeCard>> cascadePiles;
  private List<List<KlondikeCard>> foundationPiles;
  private Deque<KlondikeCard> drawPile;
  // you can add cards to the back and draw from front -- cycle
  private int numDraw;    // how many cards you can see from the draw pile
  private List<KlondikeCard> drawCards; // list of the cards visible from draw pile currently
  private boolean gameStarted;  // did the game start


  /**
   * Constructs a basic klondike game where the game has not started yet and everything is default.
   */
  public BasicKlondike() {
    this.cascadePiles = new ArrayList<>();
    this.foundationPiles = new ArrayList<>();
    this.drawPile = new LinkedList<>();
    this.drawCards = new ArrayList<>();
    this.gameStarted = false;
  }

  @Override
  public List<KlondikeCard> createNewDeck() {
    List<KlondikeCard> deck = new ArrayList<>();
    for (Suit suit : Suit.values()) { // .values is a method on enums to access each enum val
      for (Value value : Value.values()) {
        deck.add(new CardImpl(suit, value, false));
      }
    }
    return deck;
  }

  @Override
  public void startGame(List<KlondikeCard> deck, boolean shuffle, int numPiles, int numDraw)
      throws IllegalArgumentException, IllegalStateException {
    if (gameStarted) {
      throw new IllegalStateException("Game has already started!");
    }
    if (deck == null || deck.isEmpty()) {
      throw new IllegalArgumentException("The deck cannot be null or empty.");
    }
    if (numPiles < 1 || numDraw < 1) {
      throw new IllegalArgumentException("the number of piles and numDraw must be >= 1");
    }
    this.numDraw = numDraw;

    //copy of deck to use
    // shuffle deck if its true
    List<KlondikeCard> currDeck = new ArrayList<>(deck);
    if (shuffle) {
      Collections.shuffle(currDeck);
    }

    // dealing cards into the cascade piles
    int ind = 0; // which card in the deck is being dealed
    for (int i = 0; i < numPiles; i++) { // how many piles there should be
      List<KlondikeCard> pile = new ArrayList<>();
      for (int j = 0; j <= i; j++) { // how many in each
        KlondikeCard card = currDeck.get(ind);
        pile.add(card);
        ind++;
      }
      pile.get(pile.size() - 1).flip();
      cascadePiles.add(pile); // adds pile to list of cascade piles after all the dealing
    }

    // the rest of the cards go into the draw pile
    while (ind < currDeck.size()) {
      drawPile.addLast(currDeck.get(ind)); // adds it to the end
      ind++;
    }

    // there should be one foundation pile for each suit, and they should start empty
    for (Suit suit : Suit.values()) {
      foundationPiles.add(new ArrayList<>());
    }

    // show the player cards from draw pile based on numDraw
    refreshDrawCards();

    //start game!
    gameStarted = true;
  }

  /**
   * Helper method that clears the list of cards visible to the player then for each card in the
   * draw pile and reveals. Does not return anything, just mutates drawCards
   */
  private void refreshDrawCards() {
    drawCards.clear();
    List<KlondikeCard> drawList =
        new ArrayList<>(drawPile); // make a list w the draw pile to compare
    for (int i = 0; i < Math.min(numDraw, drawList.size()); i++) { //math.min if deck is smaller
      // it will just get whatevers there if numdraw is more than the size of the deck
      drawCards.add(drawList.get(i));
    }
  }

  @Override
  public void movePile(int srcPile, int numCards, int destPile)
      throws IllegalArgumentException, IllegalStateException {
    makeSureGameStarted();
    checkCascadeIndex(srcPile);
    checkCascadeIndex(destPile);

    if (srcPile == destPile) {
      throw new IllegalArgumentException("You can't move pile to the same pile.");
    }
    List<KlondikeCard> src = cascadePiles.get(srcPile); // makes a list of the cards in src pile
    List<KlondikeCard> dest = cascadePiles.get(destPile); // makes a list of the cards in dest pile

    if (numCards <= 0 || numCards > src.size()) {
      throw new IllegalArgumentException("Not enough cards in src pile or numCards is <1");
    }

    List<KlondikeCard> movingPile =
        new ArrayList<>(src.subList(src.size() - numCards, src.size()));
    // for sublist : from index, to index (exclusive)
    // make sure colors and values are in the right order for the source pile


    for (int i = 0; i < movingPile.size() - 1; i++) {
      KlondikeCard lower = movingPile.get(i);        // closer to the table
      KlondikeCard above = movingPile.get(i + 1);    // stacked above lower
      if (!(above.isOneLower(lower)) || above.isSameColor(lower)) {
        throw new IllegalStateException("Cards moving are not in proper order!");
      }
    }
    if (!dest.isEmpty()) {     // for if dest is not an empty cascade pile
      KlondikeCard topDest = dest.getLast();
      KlondikeCard movingBase = movingPile.getFirst();
      ;
      if (!(movingBase.isOneLower(topDest))
          || movingBase.isSameColor(topDest)) {
        throw new IllegalStateException("Move is invalid!");
      }
    } else {
      KlondikeCard movingBase = movingPile.getFirst(); // you can only put kings in empty cascade
      if (movingBase.getValue() != Value.KING) {
        throw new IllegalStateException("You can only place a king on an empty pile!");
      }
    }
    dest.addAll(movingPile); // add them all if everything is valid
    for (int i = 0; i < numCards; i++) {
      src.remove(src.size() - 1); // then remove them from the og source pile
    }
    flipLastCard(src);
  }

  @Override
  public void moveDraw(int destPile) throws IllegalArgumentException, IllegalStateException {
    makeSureGameStarted();
    checkCascadeIndex(destPile);

    if (drawCards.isEmpty()) {
      throw new IllegalStateException("There are no draw cards available to move");
    }

    // pickup card
    KlondikeCard movingCard = drawCards.getFirst();
    movingCard.flip();
    List<KlondikeCard> dest = getCards(destPile, movingCard);

    dest.add(movingCard);
    drawPile.removeFirst();
    refreshDrawCards();
  }


  @Override
  public void moveToFoundation(int srcPile, int foundationPile)
      throws IllegalArgumentException, IllegalStateException {
    makeSureGameStarted();
    checkCascadeIndex(srcPile);
    checkFoundationIndex(foundationPile);

    List<KlondikeCard> src = cascadePiles.get(srcPile);
    if (src.isEmpty()) {
      throw new IllegalStateException("The source pile is empty.");
    }

    KlondikeCard movingCard = src.getLast(); // top card
    List<KlondikeCard> foundation = foundationPiles.get(foundationPile);

    // check rules
    if (!ValidateMoves.isValidFoundationPlacement(movingCard, foundation)) {
      throw new IllegalStateException("invalid foundation move!");
    }
    foundation.add(movingCard);
    src.removeLast();
    flipLastCard(src);
  }

  @Override
  public void moveDrawToFoundation(int foundationPile)
      throws IllegalArgumentException, IllegalStateException {
    makeSureGameStarted();
    checkFoundationIndex(foundationPile);

    if (drawCards.isEmpty()) {
      throw new IllegalStateException("No draw cards available.");
    }

    KlondikeCard movingCard = drawCards.getFirst();
    movingCard.flip();
    List<KlondikeCard> foundation = foundationPiles.get(foundationPile);

    if (foundation.isEmpty()) {
      if (movingCard.getValue() != Value.ACE) {
        throw new IllegalStateException("Foundation must start with Ace.");
      }
    } else {
      if (!ValidateMoves.isValidFoundationPlacement(movingCard, foundation)) {
        throw new IllegalStateException("invalid foundation move!");
      }
    }

    foundation.add(movingCard);
    drawPile.removeFirst();
    refreshDrawCards();
  }

  @Override
  public void discardDraw() throws IllegalStateException {
    makeSureGameStarted();
    if (drawCards.isEmpty()) {
      throw new IllegalStateException("No draw cards to discard.");
    }

    drawPile.removeFirst(); // discard top draw card
    refreshDrawCards();
  }


  @Override
  public int getNumRows() throws IllegalStateException {
    makeSureGameStarted();
    int maxHeight = 0;
    for (List<KlondikeCard> pile : cascadePiles) {
      maxHeight = Math.max(maxHeight, pile.size());
    }
    return maxHeight;
  }

  @Override
  public int getNumPiles() throws IllegalStateException {
    makeSureGameStarted();
    return cascadePiles.size();
  }

  @Override
  public int getNumDraw() throws IllegalStateException {
    makeSureGameStarted();
    return numDraw;
  }

  @Override
  public boolean isGameOver() throws IllegalStateException {
    makeSureGameStarted();
    // win condition: all foundation piles full (13 cards each)
    for (List<KlondikeCard> foundation : foundationPiles) {
      if (foundation.size() != 13) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int getScore() throws IllegalStateException {
    makeSureGameStarted();
    int score = 0;
    for (List<KlondikeCard> foundation : foundationPiles) {
      if (!foundation.isEmpty()) {
        score += foundation.get(foundation.size() - 1).getValue().getNumber();
      }
    }
    return score;
  }

  @Override
  public int getPileHeight(int pileNum) throws IllegalArgumentException, IllegalStateException {
    makeSureGameStarted();
    checkCascadeIndex(pileNum);
    return cascadePiles.get(pileNum).size();
  }

  @Override
  public KlondikeCard getCardAt(int pileNum, int card)
      throws IllegalArgumentException, IllegalStateException {
    makeSureGameStarted();
    checkCascadeIndex(pileNum);

    List<KlondikeCard> pile = cascadePiles.get(pileNum);
    if (card < 0 || card >= pile.size()) {
      throw new IllegalArgumentException("Invalid card index!");
    }

    KlondikeCard c = pile.get(card);
    if (!c.isFaceUp()) {
      throw new IllegalArgumentException("The card is not face-up!");
    }
    return c;
  }

  @Override
  public KlondikeCard getCardAt(int foundationPile)
      throws IllegalArgumentException, IllegalStateException {
    makeSureGameStarted();
    checkFoundationIndex(foundationPile);

    List<KlondikeCard> foundation = foundationPiles.get(foundationPile);
    if (foundation.isEmpty()) {
      return null;
    }
    return foundation.get(foundation.size() - 1);
  }

  @Override
  public boolean isCardVisible(int pileNum, int card)
      throws IllegalArgumentException, IllegalStateException {
    makeSureGameStarted();
    checkCascadeIndex(pileNum);

    List<KlondikeCard> pile = cascadePiles.get(pileNum);
    if (card < 0 || card >= pile.size()) {
      throw new IllegalArgumentException("Invalid card index!");
    }
    return (pile.get(card)).isFaceUp();
  }

  @Override
  public List<KlondikeCard> getDrawCards() throws IllegalStateException {
    makeSureGameStarted();
    return new ArrayList<>(drawCards.subList(0, Math.min(numDraw, drawCards.size()))); // copy!
  }
  // also math min so that if the size of drawcards is less than num draw, it will just
  // return all of whats in the draw cards pile

  @Override
  public int getNumFoundations() throws IllegalStateException {
    makeSureGameStarted();
    return foundationPiles.size();
  }

  //helpers

  /**
   * Checks whether the game has started in order for other functions of the game to happen.
   *
   * @throws IllegalStateException if the game has not started yet
   */
  private void makeSureGameStarted() throws IllegalStateException {
    if (!gameStarted) {
      throw new IllegalStateException("The game has not started yet!");
    }
  }


  /**
   * makes sure that a cascade pile index is valid.
   */
  private void checkCascadeIndex(int idx) {
    if (idx < 0 || idx >= cascadePiles.size()) {
      throw new IllegalArgumentException("invalid cascade pile index!");
    }
  }

  /**
   * Makes sure that a foundation pile index is valid.
   */
  private void checkFoundationIndex(int idx) {
    if (idx < 0 || idx >= foundationPiles.size()) {
      throw new IllegalArgumentException("invalid foundation pile index!");
    }
  }

  /**
   * Flips the last card of a pile to be face up if needed by the game.
   * It will not do anything if the pile is empty or if the last card is already face up.
   */
  private void flipLastCard(List<KlondikeCard> pile) {
    if (!pile.isEmpty()) {
      KlondikeCard last = pile.getLast();
      if (!last.isFaceUp()) {
        last.flip();
      }
    }
  }

  /**
   * Gets the dest cascade pile and validates whether the given card
   * can be placed on it according to the rules.
   *
   * @param destPile   the index of the destination pile
   * @param movingCard the card to be placed
   * @return the destination pile as a List of KlondikeCards
   * @throws IllegalStateException if the move is not valid
   */
  private List<KlondikeCard> getCards(int destPile, KlondikeCard movingCard) {
    List<KlondikeCard> dest = cascadePiles.get(destPile);

    if (!dest.isEmpty()) {
      KlondikeCard destTop = dest.getLast();
      if (!ValidateMoves.isValidCascadePlacement(movingCard, destTop)) {
        throw new IllegalStateException("Your move is not valid!");
      }
    } else {
      if (movingCard.getValue() != Value.KING) {
        throw new IllegalStateException("You can only place a king on an empty pile!");
      }
    }
    return dest;
  }


}

