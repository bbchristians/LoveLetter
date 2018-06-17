package LoveLetter

class HumanPlayer(g: Game, i: String) : Player(g, i) {

    override fun playCard() {
        game.ui.startPlayerTurn(this)

        // Get card the player wants to play
        val playedCard : Card?

        if( mustPlayCountess() ) {
            playedCard = hand.find {
                it.value == 7
            }
        } else {
            playedCard = game.ui.getCardChoice(this)
        }

        hand.remove(playedCard)

        // Choose targets for that card if needed
        var targetPlayer : Player? = null

        if(playedCard!!.requiresTarget()) {
            targetPlayer = game.ui.getTarget(this, playedCard)
        }

        playedCard.play(game, this, targetPlayer)

        game.ui.endPlayerTurn(this)
    }

    override fun getGuardChoice(target: Player): Card? {
        return game.ui.getGuardGuess()
    }
}