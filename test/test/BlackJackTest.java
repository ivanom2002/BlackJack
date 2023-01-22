package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static test.BlackJackTest.Card.*;
import static org.junit.Assert.*;
import org.junit.Test;
import test.BlackJackTest.Hand;

public class BlackJackTest{

    public HashMap<String, Hand> players1 = new HashMap<>();
    public HashMap<String, Hand> players2 = new HashMap<>();
    public List<Card> deck1 = new ArrayList<>();
    public List<Card> deck2 = new ArrayList<>();
    public Croupier croupier1;
    public Croupier croupier2;
    
    public BlackJackTest() {
        players1.put("Player 1", createHand(Ace, Jack));
        players1.put("Player 2", createHand(_10, _5, _6));
        players1.put("Player 3", createHand(_3, _6, Ace, _3, Ace, King));
        players2.put("Player 1", createHand(_10, King));
        players2.put("Player 2", createHand(_10, _2, _6));
        players2.put("Player 3", createHand(_8, _8, _5));
        deck1.add(_5);
        deck1.add(_4);
        deck1.add(King);
        deck1.add(_2);
        deck2.add(Ace);
        deck2.add(_3);
        deck2.add(King);
        deck2.add(_2);
        croupier1 = createCroupier(_9, _7);
        croupier2 = createCroupier(_5, _10);
        Game game = createGame(croupier1, players1, deck1);
        String[] winners = game.getWinner();
    }
    
    
    @Test
    public void test_hand_value_with_one_card() {
        assertEquals(3, createHand(_3).value());
        assertEquals(10, createHand(_10).value());
        assertEquals(10, createHand(Jack).value());
        assertEquals(10, createHand(Queen).value());
        assertEquals(10, createHand(King).value());
        assertEquals(11, createHand(Ace).value());
    }
    
    @Test
    public void test_hand_value_with_two_cards() {
        assertEquals(8, createHand(_3, _5).value());        
    }
    
    @Test
    public void test_hand_is_black_jack() {
        assertEquals(false, createHand(_3, _5).isBlackJack());        
        assertEquals(true, createHand(Ace, Jack).isBlackJack());
        assertEquals(true, createHand(Ace, King).isBlackJack());
        assertEquals(true, createHand(Ace, Queen).isBlackJack());
        assertEquals(true, createHand(_10, Ace).isBlackJack());        
    }
    
    @Test 
    public void given_three_cards_should_determine_that_is_not_black_jack() {
       assertEquals(false, createHand(_5, _6, Queen).isBlackJack());               
    }
    
    @Test 
    public void given_two_cards_should_determine_that_is_not_bust() {
       assertEquals(false, createHand(_4,_3).isBust());
       assertEquals(false, createHand(_8,_10).isBust());
    }
    
    @Test 
    public void given_three_cards_should_determine_that_is_bust_or_not() {
       assertEquals(true, createHand(_4, Jack, King).isBust());               
       assertEquals(false, createHand(_4, _2, _3).isBust());   
       assertEquals(false, createHand(_10, Ace, _10).isBust()); 
       assertEquals(false, createHand(Ace, King).isBust());
    }
    
    @Test
    public void croupier_hand_value_should_change_when_adds_card() {
        Croupier croupier = createCroupier(_2,_3);
        croupier.addCard(Ace);
        assertEquals(true, croupier.hand().value() == 16); 
        croupier.addCard(_10);
        assertEquals(true, croupier.hand().value() == 16);
        croupier.addCard(_4);
        assertEquals(true, croupier.hand().value() == 20);
    }
    
    @Test 
    public void given_a_game_the_croupier_hand_value_should_be_at_least_17(){
        Game game = createGame(croupier1, players1, deck1);
        assertEquals(true, game.getCroupier().hand().value() >= 17);
        game = createGame(croupier2, players2, deck2);
        assertEquals(true, game.getCroupier().hand().value() >= 17);
    }
    
    @Test
    public void given_a_game_case_should_determine_winners() {
        Game game = createGame(croupier1, players1, deck1);
        String[] winners = game.getWinner();
        String[] proposed_winners = {"Player 1"};
        assertArrayEquals(proposed_winners, winners);
        Game game2 = createGame(croupier2, players2, deck2);
        String[] winners2 = game2.getWinner();
        String[] proposed_winners2 = {"Player 1", "Player 3"};
        assertArrayEquals(proposed_winners2, winners2);
    }

    private Hand createHand(Card... cards) {
        List<Card> cardsList = new ArrayList<>();
        for (Card card : cards) {
            cardsList.add(card);
        }
        return new Hand() {

            @Override
            public int value() {
                return canUseAceExtendedValue() ? sum() + 10 : sum();
            }
            
            private int sum() {
                return (cardsList).stream().mapToInt(c->c.value()).sum();
            }

            private boolean canUseAceExtendedValue() {
                return sum() <= 11 && containsAce();
            }
            
            private boolean containsAce() {
                return (cardsList).stream().anyMatch(c->c==Ace);
            }

            @Override
            public boolean isBlackJack() {
                return value() == 21 && cardsList.size() == 2;
            }

            @Override
            public boolean isBust() {
                return value() > 21;
            }
            
            @Override
            public void add(Card card) {
                cardsList.add(card);
            }
        };
    }
    
    public interface Hand {
        public int value();
        public boolean isBlackJack();
        public boolean isBust();
        public void add(Card card);
    }
    
    private Croupier createCroupier(Card... cards) {
        Hand hand = createHand(cards);
        return new Croupier() {
            @Override
            public Hand hand() {
                return hand;
            }

            @Override
            public void addCard(Card card) {
                hand.add(card);
            }
        };
    }
    
    public interface Croupier {
        public Hand hand();
        public void addCard(Card card);
    }
    
    private Game createGame(Croupier croupier1, HashMap<String, Hand> players1, List<Card> deck1) {
        {
            int i = 0;
            while (croupier1.hand().value() < 17) {
                croupier1.addCard(deck1.get(i));
                i++;
            }
        }
        Croupier croupier = croupier1;
        HashMap<String, Hand> players = players1;
        List<Card> deck = deck1;
        
        return new Game() {
            @Override
            public String[] getWinner() {
                String[] arrayWinners;
                if (getCroupier().hand().isBlackJack()) {
                        return arrayWinners = new String[0];
                }
                List<String> winners = new ArrayList<>();
                for (String player: players.keySet()){
                    if (getCroupier().hand().isBust()) {
                        if (!players.get(player).isBust()) winners.add(player);
                    } else {
                        if (players.get(player).isBlackJack() || 
                                players.get(player).value() > getCroupier().hand().value()
                                && !players.get(player).isBust()) winners.add(player);
                    }
                }
                arrayWinners = new String[winners.size()];
                Collections.sort(winners);
                int i = 0;
                    for (String winner: winners) {
                        arrayWinners[i] = winner;
                        i++;
                    }
                return arrayWinners;
            }
            
            @Override
            public Croupier getCroupier(){
                return croupier;
            }
        };
    }
    
    public interface Game {
        public String[] getWinner();
        public Croupier getCroupier();
    }

    public enum Card {
        Ace, _2, _3, _4, _5, _6, _7, _8, _9, _10, Jack, Queen, King;

        private boolean isFace() {
            return this == King || this == Queen || this == Jack;
        }

        private int value() {
            return isFace() ? 10 : ordinal() + 1;
        }
    }  
}
