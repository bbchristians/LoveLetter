package LoveLetter

import kotlin.coroutines.experimental.buildIterator

data class Deck(var playerCount: Int, var cards: List<Card> = listOf()) {

    val CARD_COUNTS : HashMap<Card, Int> = hashMapOf(
                                                Card(1, "Guard", "Guess a player's hand", "Grd") to 5,
                                                Card(2, "Priest", "Look at a hand", "Pst") to 2,
                                                Card(3, "Baron", "Compare hands; lower hand is out", "Brn") to 2,
                                                Card(4, "Handmaid", "Protection until your next turn", "Hnd") to 2,
                                                Card(5, "Prince", "One player discards their hand", "Pce") to 2,
                                                Card(6, "King", "Trade hands", "Kng") to 1,
                                                Card(7, "Countess", "Discard if caught with King or Prince", "Cou") to 1,
                                                Card(8, "Princess", "Lose if discarded", "Pcs") to 1
                                            )

    fun shuffleDeck() {
        this.cards = CARD_COUNTS.flatMap {
            spreadCards(it.key, it.value).asSequence().asIterable()
        }.shuffled()
    }

    private fun spreadCards(card: Card, count: Int) : Iterator<Card> = buildIterator {
        (1..count).forEach{
            yield(card)
        }
    }

    fun takeFromTop() : Pair<Boolean, Card?> {
        if (this.cards.size <= 0) {
            return Pair(false, null)
        }

        var topCard = this.cards[0]
        this.cards = this.cards.drop(1)
        return Pair(true, topCard)
    }

    fun burnCards(numCards: Int) {
        (1..numCards).forEach {
            takeFromTop()
        }
    }

    companion object {

        fun getAllCardTypes() : List<Card> = Deck(0).CARD_COUNTS.keys.sortedBy {
            it.value
        }

        fun getCardCounts() : HashMap<Card, Int> = Deck(0).CARD_COUNTS
    }
}