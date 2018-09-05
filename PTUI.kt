package LoveLetter



data class PTUI(val g: Game) : UserInterface() {

    // terminal color constants
    val red = "red"
    val green = "green"
    val yellow = "yellow"
    val blue = "blue"
    val magenta = "magenta"
    val cyan = "cyan"
    val white = "white"
    private val COLORS = mapOf(
            red to "\u001B[1;31m",
            green to "\u001B[1;32m",
            yellow to "\u001B[1;33m",
            blue to "\u001B[1;34m",
            magenta to "\u001B[1;35m",
            cyan to "\u001B[1;36m"
    )
    private val SPECIAL_COLORS = mapOf(
            white to "\u001B[0;37m"
    )

    lateinit var CARD_COLORS: HashMap<Card, String>

    private fun asColor(color: String, text: String) : String {
        return COLORS.get(color) + text + SPECIAL_COLORS.get(white)
    }

    private fun cardWithColor(card: Card, name: Boolean = false, abbrev: Boolean = false) : String {
        var cardString = card.toString()
        if( name ) {
            cardString = card.name
        }
        if( abbrev ) {
            cardString = card.getNameShort()
        }
        return asColor(CARD_COLORS[card]!!, cardString)
    }

    override fun instantiate() {
        // Signify PTUI has begun
        println(asColor(green, "The game has begun."))

        // Add color associations to card types
        CARD_COLORS = hashMapOf()
        Deck.getAllCardTypes().forEachIndexed {
            index, card ->
            CARD_COLORS[card] = COLORS.keys.toMutableList()[index % COLORS.size]
        }

    }

    override fun reportVictory(player: Player) {
        val victoryColor = if( player is HumanPlayer ) yellow else red

        println(asColor(victoryColor, "${player} has won the game!"))
    }

    override fun getTarget(player: Player, playedCard: Card) : Player? {

        val validIndexes = ArrayList<Int>()

        g.players.forEachIndexed {
            index, targetPlayer ->
            // Display players as targets, leaving out self where applicable
            if (playedCard.isValidTarget(player, targetPlayer)) {
                println("${index+1}) $targetPlayer")
                validIndexes.add(index+1)
            }
        }

        if (validIndexes.size == 0) {
            return null
        }

        var playerSelection = -1
        do {
            println("Select target for card effect:")
            try {
                playerSelection = readLine()!!.toInt()
            } catch (e: NumberFormatException) {
                // do nothing
            }
        } while (!validIndexes.contains(playerSelection))

        return g.players.elementAt(playerSelection-1)
    }

    override fun getCardChoice(player: Player): Card {
        println("It is your turn to play a card...\nYou have:")
        player.hand.forEachIndexed {
            index, card -> println("${index+1}) ${cardWithColor(card)}")
        }

        var cardSelection = -1

        do {
            println("Your Choice:")
            try {
                cardSelection = readLine()!!.toInt()
            } catch(e: NumberFormatException) {
                // do nothing
            }
        } while (0 >= cardSelection || cardSelection > player.hand.size)

        cardSelection--
        val playedCard = player.hand[cardSelection]

        return playedCard
    }

    override fun startPlayerTurn(player: Player) {
        println("\n-----Start Turn: $player-----")
    }

    override fun endPlayerTurn(player: Player) {
        if( g.gameRunning ) {
            println("-----End Turn:   $player-----\n")
        }
    }

    override fun getGuardGuess(): Card {
        var allCards = Deck.getAllCardTypes().toList().sortedBy {
            it.value
        }.drop(1)

        allCards.forEachIndexed {
            index, card ->
            println("${index+2}) ${cardWithColor(card)}")
        }

        var guardSelection = -1

        do {
            println("Your Choice:")
            try {
                guardSelection = readLine()!!.toInt()
            } catch (e: NumberFormatException) {
                // do nothing
            }
        } while(1 >= guardSelection || guardSelection > allCards.size + 1) // +1 to account for the guard being dropped

        return allCards[guardSelection-2]
    }

    override fun displayGuardGuess(player: Player, target: Player, guess: Card, success: Boolean) {
        if( success ) {
            println("$target's hand was discarded!")
        } else {
            println("${cardWithColor(guess, name=true)} was not in $target's hand.")
        }
    }

    override fun displayCardPlay(player: Player, card: Card, target: Player?, guess: Card?) {
        var base = "${cardWithColor(card, name=true)} was played by $player;"
        if( target != null ) {
            base += " Targeted $target;"
        }
        if( guess != null ) {
            base += " Guessed ${cardWithColor(guess, name=true)}"
        }
        println(base)
    }

    override fun revealHand(revealTo: Player, revealOf: Player) {
        if( revealOf.hand.size == 0 ) {
            return
        }
        when (revealTo) {
            is HumanPlayer -> {
                println("$revealOf's hand is a ${cardWithColor(revealOf.hand[0], name=true)}.")
            }
            is CPUPlayer -> {
                println("$revealTo saw $revealOf's hand.")
            }
        }
    }

    override fun reportDiscard(player: Player, card: Card) {
        println("$player discarded a ${cardWithColor(card, name=true)}.")
    }

    override fun reportHandTrade(player: Player, target: Player) {
        if( player is CPUPlayer && target is CPUPlayer ) {
            println("$player traded hands with $target.")
        } else if( player is HumanPlayer ) {
            println("You traded hands with $target, their hand was a ${cardWithColor(target.hand[0])}.")
        } else {
            println("$player traded hands with you, their hand was a ${cardWithColor(player.hand[0])}.")
        }
    }

    override fun reportCompareHands(player: Player, target: Player) {
        if( player is CPUPlayer && target is CPUPlayer ) {
            println("$player compared hands with $target.")
        } else if( player is HumanPlayer ) {
            println("You compared hands with $target, their hand was a ${cardWithColor(target.hand[0])}.")
        } else {
            println("$player compared hands with you, their hand was a ${cardWithColor(player.hand[0])}.")
        }
    }

    override fun showDiscardPile(game: Game) {
        println("Discard Pile: ")
        Deck.getAllCardTypes().forEach {
            val thisCard = it
            val countDiscarded = game.discardPile.count {
                it == thisCard
            }
            if( countDiscarded > 0 ) {
                println("${cardWithColor(thisCard, abbrev=true)} x $countDiscarded")
            }
        }
    }

    override fun showNumberOfCardsInDeck() {
        println("There are ${asColor(cyan, g.deck.cards.size.toString())} cards remaining in the deck.")
    }

    override fun revealAllHands() {

        println("\nThe game is over, the player with the largest value in hand wins:")
        g.getActivePlayers().forEach {
            println("$it's hand is a ${cardWithColor(it.hand[0])}")
        }
    }

    override fun reportNothingHappened() {
        println("...But nothing happened.")
    }

    override fun reportPlayerLoss(player: Player) {
        println(asColor(cyan, "$player has been defeated."))
    }

    override fun reportScores() {
        println("\nCurrent Scores:")
        g.scores.keys.sortedBy {
            g.scores[it]
        }.reversed().forEach {
            println("$it: ${g.scores[it]}")
        }
        println()
    }
}