package com.jammallamas.network;

import com.jammallamas.Entity;
import com.jammallamas.Player;
import com.jammallamas.Renderable;

import java.io.Serializable;
import java.util.ArrayList;

public class GameData implements Serializable {
    private static final long serialVersionUID = 1773536857132043952L;
    public ArrayList<Entity> entities;
    public ArrayList<Renderable> platforms; //for bridges and stuff
    public Player player1;
    public Player player2;
    public long grabTimeout;
    public boolean isGrabbed;
    public boolean isPaused;

    public GameData(ArrayList<Entity> entities, ArrayList<Renderable> platforms, Player player1, Player player2, long grabTimeout, boolean isGrabbed, boolean isPaused) {
        this.entities = entities;
        this.platforms = platforms;
        this.player1 = player1;
        this.player2 = player2;
        this.grabTimeout = grabTimeout;
        this.isGrabbed = isGrabbed;
        this.isPaused = isPaused;
    }
}
