package klondike.model.hw02;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class BasicKlondikeTest {

  private BasicKlondike model;

  @Before
  public void setUp() {
    model = new BasicKlondike();
  }

  private List<KlondikeCard> newDeck() {
    return model.createNewDeck();
  }

  private List<KlondikeCard> buildDeckForDrawAceThenTwo(Suit suit) {
    List<KlondikeCard> d = new ArrayList<>();
    for (int i = 0; i < 28; i++) {
      d.add(new CardImpl(Suit.SPADES, Value.KING, false));
    }
    d.add(new CardImpl(suit, Value.ACE, false));
    d.add(new CardImpl(suit, Value.TWO, false));
    while (d.size() < 52) {
      d.add(new CardImpl(Suit.CLUBS, Value.KING, false));
    }
    return d;
  }

  private List<KlondikeCard> buildDeckForInvalidSequenceOnPile1() {
    List<KlondikeCard> d = new ArrayList<>();
    // pile 0 gets index 0
    d.add(new CardImpl(Suit.SPADES, Value.KING, false));
    // pile 1 gets index 1 and 2; make them same color and descending to violate color rule
    d.add(new CardImpl(Suit.HEARTS, Value.TEN, false));
    d.add(new CardImpl(Suit.HEARTS, Value.NINE, false));
    // fill the remaining 25 cards for initial dealing to piles 2..6
    for (int i = 0; i < 25; i++) {
      d.add(new CardImpl(Suit.SPADES, Value.QUEEN, false));
    }
    while (d.size() < 52) {
      d.add(new CardImpl(Suit.CLUBS, Value.KING, false));
    }
    return d;
  }

  @Test
  public void preStartGuards_throwUntilStarted() {
    assertThrows(IllegalStateException.class, () -> model.getNumRows());
    assertThrows(IllegalStateException.class, () -> model.getNumPiles());
    assertThrows(IllegalStateException.class, () -> model.getNumDraw());
    assertThrows(IllegalStateException.class, () -> model.getNumFoundations());
    assertThrows(IllegalStateException.class, () -> model.getDrawCards());
    assertThrows(IllegalStateException.class, () -> model.getScore());
    assertThrows(IllegalStateException.class, () -> model.isGameOver());
    assertThrows(IllegalStateException.class, () -> model.moveDraw(0));
    assertThrows(IllegalStateException.class, () -> model.moveDrawToFoundation(0));
    assertThrows(IllegalStateException.class, () -> model.movePile(0, 1, 1));
    assertThrows(IllegalStateException.class, () -> model.moveToFoundation(0, 0));
    assertThrows(IllegalStateException.class, () -> model.discardDraw());
    assertThrows(IllegalStateException.class, () -> model.getPileHeight(0));
    assertThrows(IllegalStateException.class, () -> model.getCardAt(0));
    assertThrows(IllegalStateException.class, () -> model.getCardAt(0, 0));
    assertThrows(IllegalStateException.class, () -> model.isCardVisible(0, 0));
  }

  @Test
  public void startGame_validatesArguments() {
    List<KlondikeCard> deck = newDeck();
    assertThrows(IllegalArgumentException.class, () -> model.startGame(null, false, 7, 3));
    assertThrows(IllegalArgumentException.class, () -> model.startGame(new ArrayList<>(), false, 7, 3));
    assertThrows(IllegalArgumentException.class, () -> model.startGame(deck, false, 0, 3));
    assertThrows(IllegalArgumentException.class, () -> model.startGame(deck, false, 7, 0));

    model.startGame(deck, false, 7, 3);
    assertThrows(IllegalStateException.class, () -> model.startGame(deck, false, 7, 3));
  }

  @Test
  public void startGame_initialLayoutAndVisibility() {
    List<KlondikeCard> deck = newDeck();
    model.startGame(deck, false, 7, 3);
    assertEquals(7, model.getNumPiles());
    assertEquals(4, model.getNumFoundations());
    assertEquals(3, model.getNumDraw());
    assertEquals(7, model.getNumRows());

    // Cascade tops should be face up
    for (int i = 0; i < 7; i++) {
      int height = model.getPileHeight(i);
      assertTrue(model.isCardVisible(i, height - 1));
      // all beneath top are facedown, so getCardAt should throw
      if (height > 1) {
        final int pileIdx = i;
        final int hiddenIdx = height - 2;
        assertThrows(IllegalArgumentException.class, () -> model.getCardAt(pileIdx, hiddenIdx));
      }
    }

    // Draw cards visible count
    assertThat(model.getDrawCards().size(), lessThanOrEqualTo(3));
  }

  @Test
  public void moveDraw_validAndInvalidDestinations() {
    List<KlondikeCard> deck = newDeck();
    model.startGame(deck, false, 7, 3);

    // When no draw cards available
    // cycle through whole draw pile by repeatedly discarding
    while (!model.getDrawCards().isEmpty()) {
      model.discardDraw();
    }
    assertThrows(IllegalStateException.class, () -> model.moveDraw(0));

    // restart to get draw cards back
    model = new BasicKlondike();
    deck = model.createNewDeck();
    model.startGame(deck, false, 7, 3);

    // Try moving to invalid destination index
    int finalNumPiles = model.getNumPiles();
    int badIdx = finalNumPiles; // out of bounds
    int goodIdx = 0;
    assertThrows(IllegalArgumentException.class, () -> model.moveDraw(badIdx));

    // Attempt valid move: ensure rule check is applied, either succeeds or throws
    List<KlondikeCard> before = model.getDrawCards();
    if (!before.isEmpty()) {
      KlondikeCard drawTop = before.get(0);
      // If the rule allows placement onto cascade 0 top, move succeeds
      try {
        model.moveDraw(goodIdx);
      } catch (IllegalStateException e) {
        // invalid by rule is fine
      }
    }
  }

  @Test
  public void discardDraw_behaviourAndBounds() {
    List<KlondikeCard> deck = newDeck();
    model.startGame(deck, false, 7, 3);
    List<KlondikeCard> first = model.getDrawCards();
    if (!first.isEmpty()) {
      KlondikeCard removed = first.get(0);
      model.discardDraw();
      List<KlondikeCard> next = model.getDrawCards();
      if (!next.isEmpty()) {
        assertThat(next.get(0), not(equalTo(removed)));
      }
    }
    // when empty
    while (!model.getDrawCards().isEmpty()) {
      model.discardDraw();
    }
    assertThrows(IllegalStateException.class, () -> model.discardDraw());
  }

  @Test
  public void createNewDeck_has52UniqueFaceDown() {
    List<KlondikeCard> deck = model.createNewDeck();
    assertEquals(52, deck.size());
    boolean allDown = true;
    java.util.Set<String> seen = new java.util.HashSet<>();
    for (KlondikeCard c : deck) {
      allDown &= !c.isFaceUp();
      String key = c.getSuit().name() + ":" + c.getValue().name();
      assertTrue("duplicate card in deck: " + key, seen.add(key));
    }
    assertTrue(allDown);
  }

  @Test
  public void moveDrawToFoundation_aceThenTwoSameSuit() {
    List<KlondikeCard> deck = buildDeckForDrawAceThenTwo(Suit.CLUBS);
    model.startGame(deck, false, 7, 1);
    model.moveDrawToFoundation(0);
    KlondikeCard top = model.getCardAt(0);
    assertNotNull(top);
    assertEquals(Value.ACE, top.getValue());
    assertEquals(Suit.CLUBS, top.getSuit());

    model.moveDrawToFoundation(0);
    top = model.getCardAt(0);
    assertNotNull(top);
    assertEquals(Value.TWO, top.getValue());
    assertEquals(Suit.CLUBS, top.getSuit());
  }

  @Test
  public void movePile_invalidSequenceThrows() {
    List<KlondikeCard> deck = buildDeckForInvalidSequenceOnPile1();
    model.startGame(deck, false, 7, 3);
    // Attempt to move 2 cards from pile 1; sequence violates color alternation
    assertThrows(IllegalStateException.class, () -> model.movePile(1, 2, 0));
  }

  @Test
  public void movePile_validations() {
    List<KlondikeCard> deck = newDeck();
    model.startGame(deck, false, 7, 3);
    int piles = model.getNumPiles();
    assertThrows(IllegalArgumentException.class, () -> model.movePile(-1, 1, 0));
    assertThrows(IllegalArgumentException.class, () -> model.movePile(0, 1, -1));
    assertThrows(IllegalArgumentException.class, () -> model.movePile(piles, 1, 0));
    assertThrows(IllegalArgumentException.class, () -> model.movePile(0, 1, piles));
    assertThrows(IllegalArgumentException.class, () -> model.movePile(0, 0, 1));
    assertThrows(IllegalArgumentException.class, () -> model.movePile(0, model.getPileHeight(0) + 1, 1));
    assertThrows(IllegalArgumentException.class, () -> model.movePile(0, 1, 0));
  }

  @Test
  public void moveToFoundation_and_moveDrawToFoundation_rulesAndBounds() {
    List<KlondikeCard> deck = newDeck();
    model.startGame(deck, false, 7, 3);
    int foundations = model.getNumFoundations();
    assertThrows(IllegalArgumentException.class, () -> model.moveToFoundation(-1, 0));
    assertThrows(IllegalArgumentException.class, () -> model.moveToFoundation(0, -1));
    assertThrows(IllegalArgumentException.class, () -> model.moveToFoundation(model.getNumPiles(), 0));
    assertThrows(IllegalArgumentException.class, () -> model.moveToFoundation(0, foundations));

    assertThrows(IllegalArgumentException.class, () -> model.moveDrawToFoundation(-1));
    assertThrows(IllegalArgumentException.class, () -> model.moveDrawToFoundation(foundations));

    // When no draw cards available
    while (!model.getDrawCards().isEmpty()) {
      model.discardDraw();
    }
    assertThrows(IllegalStateException.class, () -> model.moveDrawToFoundation(0));
  }

  @Test
  public void getCardAt_variantsAndVisibility() {
    List<KlondikeCard> deck = newDeck();
    model.startGame(deck, false, 7, 3);
    assertThrows(IllegalArgumentException.class, () -> model.getCardAt(-1, 0));
    assertThrows(IllegalArgumentException.class, () -> model.getCardAt(0, -1));
    assertThrows(IllegalArgumentException.class, () -> model.getCardAt(model.getNumPiles(), 0));
    assertThrows(IllegalArgumentException.class, () -> model.getCardAt(0, model.getPileHeight(0)));

    // foundation getter
    assertNull(model.getCardAt(0));
  }

  @Test
  public void scoreAndGameOver_progressesWithFoundations() {
    List<KlondikeCard> deck = newDeck();
    model.startGame(deck, false, 7, 3);

    assertFalse(model.isGameOver());
    int scoreBefore = model.getScore();
    assertThat(scoreBefore, greaterThanOrEqualTo(0));

    // Try to move from cascades to foundations when legal; loop conservatively
    boolean movedSomething = false;
    for (int f = 0; f < model.getNumFoundations(); f++) {
      for (int p = 0; p < model.getNumPiles(); p++) {
        int h = model.getPileHeight(p);
        if (h > 0) {
          try {
            model.moveToFoundation(p, f);
            movedSomething = true;
            break;
          } catch (IllegalStateException ignored) {
          }
        }
      }
    }
    int scoreAfter = model.getScore();
    if (movedSomething) {
      assertThat(scoreAfter, greaterThanOrEqualTo(scoreBefore));
    }
  }
}

