package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 结算时玩家的结算结果，结算时每个玩家都需要有个BattleBalance对象进行结算
 * Created by Administrator on 2016/9/17.
 */
public class BattleBalance {
    private int playerId;
    //通信时，isHu ting winType合成一个数组,
    //第0位代表是否胡牌 第1位代表是否听牌过 第2位代表胡牌的牌型
    private boolean isHu;
    private int winType = -1;   // 胡牌类型
    private int ting;

    private List<Integer> cards = new ArrayList<>();     // 保存所有的牌
    private int winScore;                               // 番数
    private int winPoint;                               // 积分
    private int huPoint=0;                                //结算界面显示的和分
    private int gangPoint=0;                              //结算界面显示的杠分
    private List<CardBalance> balances = new ArrayList<>();
    private List<BattleScore> scores = new ArrayList<>();

    private List<Integer> hutype = new ArrayList<>();
    public List<Integer> getHutype() {return hutype;}

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getWinType() {
        return winType;
    }

    public void setWinType(int winType) {
        this.winType = winType;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }

    public int getWinScore() {
        return winScore;
    }

    public void setWinScore(int winScore) {
        this.winScore = winScore;
    }

    public int getWinPoint() {
        return winPoint;
    }
    public void setWinPoint(int winPoint) {
        this.winPoint = winPoint;
    }

    public int getHuPoint() {return huPoint;}

    public void setHuPoint(int huPoint) {this.huPoint = huPoint;}
    public void addHuPoint(int huPoint) {this.huPoint += huPoint;}

    public int getGangPoint() {return gangPoint;}

    public void setGangPoint(int gangPoint) {this.gangPoint = gangPoint;}

    public List<CardBalance> getBalances() {
        return balances;
    }

    public void setBalances(List<CardBalance> balances) {
        this.balances = balances;
    }

    public void addCards(int card) {
        this.cards.add(card);
    }

    public int getTing() {
        return ting;
    }

    public void setTing(int ting) {
        this.ting = ting;
    }

    public void addBalances(CardBalance cardBlance) {
        this.balances.add(cardBlance);
    }

    public void addBattleScore(BattleScore battleScore) {
        this.scores.add(battleScore);
    }

    public void addPoint(int point){
        this.winPoint += point;
    }

    public boolean isHu() {
        return isHu;
    }

    public void setHu(boolean hu) {
        isHu = hu;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("playerId", playerId );

        List<Integer> winTypes = new ArrayList<>();
        winTypes.add(isHu?2:1);
        winTypes.add(ting);
        winTypes.add(winType);

        obj.putIntArray("winType", winTypes );
        obj.putIntArray("cards", cards);
        obj.putInt("ting", ting );
        obj.putInt("winPoint", winPoint );
        obj.putInt("huPoint",huPoint);
        obj.putInt("gangPoint",gangPoint);
//        hutype.add(156);
//        hutype.add(153);
        for(Integer i:hutype)
            obj.putIntArray("hutype",hutype);

        SFSArray bs = new SFSArray();
        for(CardBalance cardBalance : balances){
            bs.addSFSObject(cardBalance.toSFSObject());
        }
        if(scores.size() > 0){
            SFSArray battleScoreArr = new SFSArray();
            for(BattleScore battleScore : scores){
                battleScoreArr.addSFSObject(battleScore.toSFSObject());
            }
            obj.putSFSArray("scores", battleScoreArr);
        }

        obj.putSFSArray("balances", bs);
        return obj;
    }
}
