package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 得分失分的明细
 */
public class BattleScore {
    private int type; //明目，如果是得分，为胡的牌型和杠的牌型，失分为风位标识
    private int score;
    private int uid;
    private List<BattleScore> detail = new ArrayList<>(); //得分的补充番明细，数组长度为2， 第0位为类型，第1位为分数

    public BattleScore(){}

    public BattleScore(int type, int score) {
        this.type = type;
        this.score = score;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<BattleScore> getDetail() {
        return detail;
    }
    public void setDetail(List<BattleScore> detail) {
        this.detail = detail;
    }

    public void addDetail(BattleScore pay){
        detail.add(pay);
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public SFSObject toSFSObject(){
        SFSObject data = new SFSObject();
        data.putInt("t", type);
        data.putInt("s", score);
        if(detail.size() > 0){
            SFSArray pays = new SFSArray();
            for(BattleScore pay : detail){
                pays.addSFSObject(pay.toSFSObject());
            }
            data.putSFSArray("detail", pays);
        }
        return data;
    }

    public void addScore(int score) {
        this.score += score;
    }
}
