package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/17.
 */
public class CardBalance {

    private int type ;
    private int card ;
    private int score ;
    private int targetId; // target uid
    private List<Integer> cards = new ArrayList<>();
    private List<Integer> subtype = new ArrayList<>();

    public CardBalance(){
    }

    public CardBalance(int type, int card, int score) {
        this.type = type;
        this.card = card;
        this.score = score;
    }

    public CardBalance(int type, List<Integer> cards, int score) {
        this.type = type;
        this.cards = cards;
        this.score = score;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCard() {
        return card;
    }

    public void setCard(int card) {
        this.card = card;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }

    public List<Integer> getSubtype() {
        return subtype;
    }

    public void setSubtype(List<Integer> subtype) {
        this.subtype = subtype;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();

        obj.putInt("type",type);
        obj.putInt("card",card);
        obj.putInt("score",score);
        if(cards.size() > 0){
            obj.putIntArray("cards", cards);
        }

        if(subtype!=null && subtype.size() > 0){
            obj.putIntArray("subtype", subtype);
        }

        obj.putInt("tuid", targetId);

        return obj;
    }

}
