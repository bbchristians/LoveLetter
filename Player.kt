package LoveLetter

open class Player(var game: Game, val identifier: String, var hand: ArrayList<Card> = ArrayList()) {

    var hasLost = false
    var hasProtection = false // handmaid protection

    open fun playCard() {}

    open fun getGuardChoice(target: Player) : Card? {
        return null
    }

    open fun registerCardKnowledge(player: Player, card: Card) {
        return
    }

    open fun observePlay(player: Player, card: Card) {
        return
    }

    fun drawCard() {
        val (couldDraw, draw) = this.game.deck.takeFromTop()

        if (!couldDraw) {
            game.endGame()
        } else {
            hand.add(draw!!)
        }
    }

    override fun toString() : String {
        return identifier
    }

    fun discardHand() {
        if( hand.size > 0 ) {
            val discardedCard = hand[0]
            game.addToDiscardPile(discardedCard)
            hand = ArrayList()
            game.ui.reportDiscard(this, discardedCard)
        }
    }

    fun mustPlayCountess() : Boolean {
        val handValues = hand.map{
            it.value
        }
        return (handValues.contains(7) && (handValues.contains(5) || handValues.contains(6)))
    }


}