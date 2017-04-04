package com.rafo.chess.model;


public class ScoreDetail {
    private int uid; //玩家ID
    private int rate;  //番数
    private int subType; //得分或失分名目
    private boolean canMerege = true;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public boolean isCanMerege() {
        return canMerege;
    }

    public void setCanMerege(boolean canMerege) {
        this.canMerege = canMerege;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScoreDetail that = (ScoreDetail) o;

        if (uid != that.uid) return false;
        if (rate != that.rate) return false;
        return subType == that.subType;

    }

    @Override
    public int hashCode() {
        int result = uid;
        result = 31 * result + rate;
        result = 31 * result + subType;
        return result;
    }
}
