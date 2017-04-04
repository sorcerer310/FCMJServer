package com.rafo.chess.model.battle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/17.
 */
public class WBBattleStepREQ {

    private String accountId ;
    private int roomId ;
    private int playType ;  //打牌类型
    private int card ;
    private List<Integer> cards = new ArrayList<>(); //具体牌型，eg:吃的具体的牌

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public int getCard() {
        return card;
    }

    public void setCard(int card) {
        this.card = card;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }
}
