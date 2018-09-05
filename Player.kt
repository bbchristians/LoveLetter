package LoveLetter

open class Player(var game: Game, val identifier: String, var hand: ArrayList<Card> = ArrayList()) {

    var hasLost = false
    var hasProtection = false // handmaid protection

    var knownCard : Card? = null // Will be an instance of Card if another player knows this card is in hand

    // TODO account for knowledge about guard plays
    // i.e. if someone guesses king as a guard guess, then they likely dont have a king in hand
    var playerKnowledge: HashMap<Player, Card?> = hashMapOf()

    var playerSuspicion: HashMap<Player, HashMap<Card, Float>> = hashMapOf()

    open fun playCard() {}

    open fun getGuardChoice(target: Player) : Card? {
        return null
    }

    fun registerCardKnowledge(player: Player, card: Card) {
        playerKnowledge[player] = card
        player.flagCardAsKnown()
    }

    open fun observePlay(player: Player, card: Card) {
        return
    }

    fun drawCard() {
        val (couldDraw, draw) = this.game.deck.takeFromTop()

        // If players knew about the player NOT having a card in hand, they no longer have that info
        game.players.forEach {
            it.clearAllNegatedSuspicions(this)
        }

        if (!couldDraw) {
            game.endGame()
        } else {
            hand.add(draw!!)
        }
    }

    override fun toString() : String {
        return identifier
    }

    fun discardHand(showUI: Boolean = true) {
        if( hand.size > 0 ) {
            val discardedCard = hand[0]
            game.addToDiscardPile(discardedCard)
            hand = ArrayList()
            if( showUI ) {
                game.ui.reportDiscard(this, discardedCard)
            }
        }
    }

    fun mustPlayCountess() : Boolean {
        val handValues = hand.map{
            it.value
        }
        return (handValues.contains(7) && (handValues.contains(5) || handValues.contains(6)))
    }

    fun flagCardAsKnown() {
        knownCard = hand[0]
    }

    fun flagCardAsUnknown() {
        knownCard = null
    }

    fun addSuspicion(player: Player, card: Card, value: Float) {
        if( playerSuspicion.containsKey(player) ) {
            if( playerSuspicion[player]!!.containsKey(card) && playerSuspicion[player]!![card]!! > 0 ) {
                playerSuspicion[player]!![card] = playerSuspicion[player]!![card]!! + value
            }
            playerSuspicion[player]!![card] = value
        } else {
            playerSuspicion.set(player, hashMapOf(card to value))
        }
    }

    fun clearSuspicion(player: Player, card: Card) {
        if( playerSuspicion.containsKey(player) && playerSuspicion[player]!!.containsKey(card) ) {
            playerSuspicion[player]!![card] = 0F
        }
    }

    fun negateAllSuspicion(player: Player, card: Card) {
        if( playerSuspicion.containsKey(player) && playerSuspicion[player]!!.containsKey(card) ) {
            playerSuspicion[player]!![card] = Float.MIN_VALUE
        }
    }

    fun clearAllNegatedSuspicions(player: Player) {
        Deck.getAllCardTypes().forEach {
            if( getSuspicion(player, it) < 0 ) {
                clearSuspicion(player, it)
            }
        }
    }

    fun getSuspicion(player: Player, card: Card) : Float {
        if( playerSuspicion[player] != null ) {
            return playerSuspicion[player]!![card] ?: 0F
        }
        return 0F
    }

}