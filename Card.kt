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

        // If other players knew about this card, they no longer know it's in the player's hand
        player.flagCardAsUnknown()

        // Clear suspicions of having this card for other players
        game.players.forEach {
            it.clearSuspicion(player, this)
        }

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
                        } else {
                            game.players.forEach {
                                it.negateAllSuspicion(target, guess)
                            }
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
                        val playerHandValue = player.hand[0].value
                        val targetHandValue = target.hand[0].value
                        if( playerHandValue > targetHandValue ) {
                            target.discardHand()
                            Deck.getAllCardTypes().forEach {
                                val thisCard = it
                                if( thisCard.value > targetHandValue ) {
                                    game.players.forEach {
                                        it.addSuspicion(player, thisCard, 100F)
                                    }
                                }
                            }
                        } else if( playerHandValue < targetHandValue ) {
                            player.discardHand()
                            Deck.getAllCardTypes().forEach {
                                val thisCard = it
                                if( thisCard.value > playerHandValue ) {
                                    game.players.forEach {
                                        it.addSuspicion(target, thisCard, 100F)
                                    }
                                }
                            }
                        } else {
                            game.ui.reportNothingHappened()
                            player.registerCardKnowledge(target, target.hand[0])
                            target.registerCardKnowledge(player, player.hand[0])
                            // TODO if baron was played by player, and nothing happened, and there is only 1 possible non-guard card that could cause this, then register knowledge
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
                        target.registerCardKnowledge(player, player.hand[0])
                        // TODO switch hand knowledge for other players as well
                    }
                }
                7 -> { // Countess
                    // make other players suspicious of you the player having 5, 6, or 8
                    listOf(5,6,8).forEach {
                        val curValue = it
                        game.players.forEach {
                            it.addSuspicion(player, getCardByValue(curValue)!!, 1F)
                        }
                    }
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
