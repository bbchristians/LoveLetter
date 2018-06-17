package LoveLetter

data class Card(val value: Int, val name: String, val text: String, var abbrev: String) {

    fun requiresTarget() : Boolean {
        return (value != 4 && value != 7 && value != 8)
    }

    fun canTargetSelf() : Boolean {
        return (value == 5)
    }

    fun getNameShort() : String {
        return "${abbrev}($value)"
    }

    fun play(game: Game, player: Player, target: Player?) {
        game.ui.displayCardPlay(player, this, target, null)

        // Do not play the card if the target was invalid
        if( !isValidTarget(player, target) ) {
            game.ui.reportNothingHappened()
        } else {
            when (value) {
                1 -> { // Guard
                    if (target == null) {
                        game.ui.reportNothingHappened()
                    } else {
                        val guess: Card = player.getGuardChoice(target)!!
                        val success = target.hand[0] == guess
                        if (success) {
                            target.discardHand()
                        }
                        game.ui.displayGuardGuess(player, target, guess, success)
                    }
                }
                2 -> { // Priest
                    if (target == null) {
                        game.ui.reportNothingHappened()
                    } else {
                        player.registerCardKnowledge(target, target.hand[0])
                        game.ui.revealHand(player, target)
                    }
                }
                3 -> { // Baron
                    if (target == null) {
                        game.ui.reportNothingHappened()
                    } else {
                        game.ui.reportCompareHands(player, target)
                        if (player.hand[0].value > target.hand[0].value) {
                            target.discardHand()
                        } else if (player.hand[0].value < target.hand[0].value) {
                            player.discardHand()
                        }
                    }
                }
                4 -> { // Handmaid
                    player.hasProtection = true
                }
                5 -> { // Prince
                    if (target == null) {
                        game.ui.reportNothingHappened()
                    } else {
                        val handWasPrincess = target.hand[0].value == 8
                        target.discardHand()
                        if (!handWasPrincess) {
                            target.drawCard()
                        }
                    }
                }
                6 -> { // King
                    if (target == null) {
                        game.ui.reportNothingHappened()
                    } else {
                        game.ui.reportHandTrade(player, target)
                        val targetCard = target.hand[0]
                        target.hand[0] = player.hand[0]
                        player.hand[0] = targetCard
                        player.registerCardKnowledge(target, target.hand[0])
                    }
                }
                7 -> { // Countess
                }
                8 -> { // Princess
                    player.discardHand() // same as lose the game
                }
            }
        }
        game.addToDiscardPile(this)
        game.updateGameState(player, this)
    }

    fun isValidTarget(player: Player, target: Player?, accountForProtection: Boolean = true) : Boolean {
        if( target == null ) {
            return !requiresTarget()
        }
        return !target.hasLost && (this.canTargetSelf() || player != target) && (!target.hasProtection || !accountForProtection)
    }

    override fun toString() : String {
        return "${name} (${value}), ${text}"
    }

    companion object {
        fun getCardByValue(value: Int) : Card? {
            val cardInList = Deck.getAllCardTypes().filter {
                it.value == value
            }

            if( cardInList.size > 0 ) {
                return cardInList[0]
            }
            return null
        }
    }
}
