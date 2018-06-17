package LoveLetter

class CPUPlayer(g: Game, i: String) : Player(g, i) {

    val showHand = false
    val thinkDur = 2000L

    var playerKnowledge: HashMap<Player, Card?> = hashMapOf()

    // Value for play estimation
    private val MAX_VALUE = 100F
    private val NO_VALUE = 0F
    private val NEG_VALUE = -100F

    override fun playCard() {
        game.ui.startPlayerTurn(this)

        //Thread.sleep(thinkDur)
        readLine()

        val thisPlay = getplay()

        hand.remove(thisPlay.second)

        thisPlay.second.play(game, this, if(thisPlay.second.requiresTarget()) thisPlay.first else null)

        //Thread.sleep(thinkDur)
        readLine()

        game.ui.endPlayerTurn(this)
    }

    override fun getGuardChoice(target: Player): Card? {
        if( playerKnowledge[target] != null && playerKnowledge[target] != Card.getCardByValue(1) ) {
            return playerKnowledge[target]
        }

        val remainingCards = calculateRemainingCards()
        remainingCards[Card.getCardByValue(1)!!] = 0 // don't allow the choosing of guards

        val otherCard = getOtherCardInHand(Card.getCardByValue(1))
        if( otherCard != null ) {
            remainingCards[otherCard] = remainingCards[otherCard]!! - 1
        }

        return remainingCards.keys.sortedBy {
            -1 * remainingCards[it]!!
        }[0]
    }

    override fun registerCardKnowledge(player: Player, card: Card) {
        playerKnowledge[player] = card
    }

    override fun observePlay(player: Player, card: Card) {
        if( playerKnowledge[player] == card ) {
            playerKnowledge[player] = null
        }
    }

    private fun getTarget(playedCard: Card) : Player? {
        val validTargets = game.players.filter {
            playedCard.isValidTarget(this, it)
        }.shuffled()
        if (validTargets.size > 0) {
            return validTargets.take(1)[0]
        }
        return null
    }

    private fun getplay() : Pair<Player, Card> {
        // Make a HashMap of all possible plays
        val allPlays = HashMap<Pair<Player, Card>, Float>()
        game.players.forEach {
            val target = it
            hand.forEach {
                if( it.isValidTarget(this, target) ) {
                    allPlays[Pair(target, it)] = getPlayValue(target, it, false)
                } else if( it.isValidTarget(this, target, false) ) {
                    allPlays[Pair(target, it)] = getPlayValue(target, it, true)
                }
            }
        }

        // Sort it by play value, and return the fist one
        val sortedPlays = allPlays.keys.sortedBy {
            -1 * allPlays[it]!!
        }

        return sortedPlays[0]
    }

    private fun getPlayValue(target: Player, card: Card, targetHasProtection: Boolean) : Float {
        val targetHand = playerKnowledge[target]

        // If you have two cards that do the same thing, play the one with the lower value
        val thisCard_MAX_VALUE = MAX_VALUE - card.value/100
        val thisCard_NO_VALUE  = NO_VALUE  - card.value/100
        val thisCard_NEG_VALUE = NEG_VALUE - card.value/100

        if( targetHasProtection ) {
            when (card.value) {
                1 -> return thisCard_NO_VALUE
                2 -> return thisCard_NO_VALUE
                3 -> return thisCard_NO_VALUE
                5 -> return thisCard_NO_VALUE
                6 -> return thisCard_NO_VALUE
            }
        }
        if( targetHand != null ) { // If the AI knows this player's hand
            when (card.value) {
                1 -> { // if player's hand is not guard
                    return if( targetHand.value != 1 ) thisCard_MAX_VALUE else thisCard_NO_VALUE
                }
                2 -> return thisCard_NO_VALUE // you already know their hand
                3 -> {
                    val playersOtherCard = getOtherCardInHand(Card.getCardByValue(3))!!
                    if( playersOtherCard.value > targetHand.value ) {
                        return thisCard_MAX_VALUE-1 // Can beat another player (minus 1 because it gives off info)
                    } else if ( playersOtherCard.value == targetHand.value ) {
                        return thisCard_NO_VALUE-1 // Ties other plauer (minus 1 because it shows them your hand)
                    }
                    return thisCard_NEG_VALUE // You lose the game
                }
                5 -> return if( targetHand.value == 8 && target != this ) thisCard_MAX_VALUE else thisCard_NO_VALUE // if non-self target has princess it should be discarded
                6 -> return thisCard_NO_VALUE-1 // TODO Figure out how to determine if the AI wants the player to discard their card
            }
        } else { // If the AI does not know this player's hand
            when (card.value) {
                1 -> return thisCard_NO_VALUE+1 // Shot in the dark, better than no value
                2 -> return thisCard_NO_VALUE+2 // Figuring out info is better than a shot in the dark
                3 -> return thisCard_NO_VALUE+
                        getOtherCardInHand(Card.getCardByValue(3))!!.value -
                        calculateRemainingAverageValue() // Determine odds of winning off of blind baron play
                4 -> return thisCard_NO_VALUE+3 // Always play it safe unless you can go for a kill
                5 -> return thisCard_NO_VALUE-1 // This is just a bad play most of the time
                6 -> return thisCard_NO_VALUE-5 // This is almost always a terrible play
                7 -> return thisCard_NO_VALUE-2 // pretty bad
                8 -> return thisCard_NEG_VALUE  // you lose, idiot
            }
        }
        return thisCard_NO_VALUE
    }

    /**
     * Returns the other card in the player's hand
     *
     * If both cards are the same, returns the first card in the hand
     */
    private fun getOtherCardInHand(card: Card?) : Card? {
        if( card == null ) {
            return null
        }
        val nonBaronHand = hand.filter {
            it != card
        }
        // If hand was 2 Barons
        if( nonBaronHand.size == 0 ) {
            return hand[0]
        }
        // If hand only had 1 baron
        return nonBaronHand[0]
    }

    private fun calculateRemainingCards() : HashMap<Card, Int> {
        val remainingCards = HashMap<Card, Int>()
        Deck.getAllCardTypes().forEach {
            val eachCard = it
            remainingCards[it] = Deck.getCardCounts()[it]!! - game.discardPile.count {
                it == eachCard
            }
        }
        return remainingCards
    }

    private fun calculateRemainingAverageValue() : Float {
        val remainingCards = calculateRemainingCards()
        val numRemainingCards = remainingCards.values.sumBy { it }.toFloat()
        return remainingCards.keys.sumBy {
            it.value.times( remainingCards[it]!!)
        } / numRemainingCards
    }

    override fun toString() : String {
        val base = super.toString()
        if( showHand && hand.size > 0) {
            return base + "(Hand: ${this.hand[0].name})"
        }
        return base
    }

}