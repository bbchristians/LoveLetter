package LoveLetter


data class Game(var players : Set<Player> = ArrayList<Player>().toSet()) {
    var deck = Deck(players.size)
    var ui : UserInterface = PTUI(this)
    var gameRunning = false
    var discardPile = ArrayList<Card>()

    fun addPlayer(newPlayer: Player) {
        val newRoster = players.toMutableSet()
        newRoster.add(newPlayer)
        players = newRoster.toSet()
        deck = Deck(players.size)
    }

    fun startGame() {
        ui.instantiate()

        deck.shuffleDeck()
        discardPile = ArrayList()

        if(getNumberOfPlayers() <= 2) {
            deck.burnCards(3)
        } else {
            deck.burnCards(1)
        }

        players.forEach {
            it.drawCard()
            it.hasLost = false
        }

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

        if( getActivePlayers().size > 1 ) {
            ui.revealAllHands()
            ui.reportVictory(getActivePlayers().sortedBy {
                -1 * it.hand[0].value
            }[0])
        }
    }

    fun endGame() {
        gameRunning = false
    }

    fun updateGameState(currentTurnPlayer: Player, cardPlayed: Card) {
        players.forEach {
            if( it.hand.size == 0 ){
                it.hasLost = true
            }
            it.observePlay(currentTurnPlayer, cardPlayed)
        }
        // if only 1 player remaining
        if( players.sumBy {if (it.hasLost) 0 else 1 } <= 1 ) {
            players.forEach {
                if( !it.hasLost ) {
                    ui.reportVictory(it)
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
    var cpu1 = CPUPlayer(game, "CPU1")
    var cpu2 = CPUPlayer(game, "CPU2")

    game.addPlayer(p1)
    game.addPlayer(cpu1)
    game.addPlayer(cpu2)

    game.startGame()


}