package LoveLetter


data class Game(var players : Set<Player> = ArrayList<Player>().toSet()) {
    var deck = Deck(players.size)
    var ui : UserInterface = PTUI(this)
    var gameRunning = false
    var discardPile = ArrayList<Card>()

    val scores: HashMap<Player, Int> = hashMapOf()

    fun addPlayer(newPlayer: Player) {
        val newRoster = players.toMutableSet()
        newRoster.add(newPlayer)
        players = newRoster.toSet()
        deck = Deck(players.size)
    }

    fun startGame() {
        ui.instantiate()

        deck.shuffleDeck()

        if(getNumberOfPlayers() <= 2) {
            deck.burnCards(3)
        } else {
            deck.burnCards(1)
        }

        players.forEach {
            it.discardHand(false)
            it.drawCard()
            it.hasLost = false
        }

        discardPile = ArrayList()

        gameRunning = true

        while (gameRunning) {
            players.forEach {
                if( !it.hasLost && gameRunning ) {
                    it.hasProtection = false
                    it.drawCard() // ends the game if no cards in deck

                    if( gameRunning ) {
                        it.playCard() // ends the game if only 1 player is left at the end of the play
                    }

                    if( gameRunning ) {
                        ui.showNumberOfCardsInDeck()
                        ui.showDiscardPile(this)
                    }
                }
            }
        }

        var victor = getActivePlayers()[0]

        if( getActivePlayers().size > 1 ) {
            ui.revealAllHands()
            victor = getActivePlayers().sortedBy {
                -1 * it.hand[0].value
            }[0]
        }
        ui.reportVictory(victor)
        scores[victor] = 1 + (scores[victor]  ?: 0)

        ui.reportScores()
    }

    fun endGame() {
        gameRunning = false
    }

    fun updateGameState(currentTurnPlayer: Player, cardPlayed: Card) {
        players.forEach {
            if( it.hand.size == 0 ){
                // if the player has lost, but the flag hasn't been set yet (so its the first update since they lost)
                if( !it.hasLost ) {
                    ui.reportPlayerLoss(it)
                }
                it.hasLost = true
            }
            it.observePlay(currentTurnPlayer, cardPlayed)
        }
        // if only 1 player remaining
        if( players.sumBy {if (it.hasLost) 0 else 1 } <= 1 ) {
            players.forEach {
                if( !it.hasLost ) {
                    endGame()
                }
            }
        }
    }

    fun getNumberOfPlayers() : Int {
        return players.size
    }

    fun getActivePlayers() : List<Player> {
        return players.filter {
            !it.hasLost
        }.toList()
    }

    fun addToDiscardPile(card: Card) {
        discardPile.add(card)
    }

}


fun main(args: Array<String>) {

    var game = Game()

    var p1 = HumanPlayer(game, "Human")
    var p2 = HumanPlayer(game, "Human2")
    var cpu1 = CPUPlayer(game, "CPU1")
    var cpu2 = CPUPlayer(game, "CPU2")

    game.addPlayer(p1)
    game.addPlayer(cpu1)
    game.addPlayer(cpu2)

    while( true ) {
        game.startGame()
    }
}