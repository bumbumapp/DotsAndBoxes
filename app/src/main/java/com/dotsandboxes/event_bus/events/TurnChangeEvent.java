package com.dotsandboxes.event_bus.events;


import com.dotsandboxes.game.controllers.Game;

public class TurnChangeEvent {
        public final Game.Player nextPlayer;

    public TurnChangeEvent(Game.Player nextPlayer) {
        this.nextPlayer = nextPlayer;
    }

    @Override
    public String toString() {
        return "TurnChangeEvent{" +
                "nextPlayer=" + nextPlayer +
                '}';
    }
}
