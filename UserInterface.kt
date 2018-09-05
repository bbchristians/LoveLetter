package LoveLetter

abstract class UserInterface {

    abstract fun instantiate()

    abstract fun reportVictory(player: Player)

    abstract fun getTarget(player: Player, playedCard: Card) : Player?

    abstract fun getCardChoice(player: Player) : Card

    abstract fun startPlayerTurn(player: Player)

    abstract fun endPlayerTurn(player: Player)

    abstract fun displayCardPlay(player: Player, card: Card, target: Player? = null, guess: Card? = null)

    abstract fun getGuardGuess() : Card

    abstract fun displayGuardGuess(player: Player, target: Player, guess: Card, success: Boolean)

    abstract fun revealHand(revealTo: Player, revealOf: Player)

    abstract fun reportDiscard(player: Player, card: Card)

    abstract fun reportHandTrade(player: Player, target: Player)

    abstract fun reportCompareHands(player: Player, target: Player)

    abstract fun showDiscardPile(game: Game)

    abstract fun showNumberOfCardsInDeck()

    abstract fun revealAllHands()

    abstract fun reportNothingHappened()

    abstract fun reportPlayerLoss(player: Player)

    abstract fun reportScores()
}