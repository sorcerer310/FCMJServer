package com.rafo.chess.common.model.record;

import com.smartfoxserver.v2.entities.data.SFSObject;

/**
 * Created by Administrator on 2016/9/17.
 */
public class PlayerPointInfoPROTO {

    private int playerID ; // 用户ID，非accountID
    private String head; // 用户昵称
    private int chair ; // 用户座位
    private int point ; // 得分，可正可负
    private String nickName; // 用户昵称

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public int getChair() {
        return chair;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("playerID", playerID);
        obj.putUtfString("head", head);
        obj.putUtfString("nickName", nickName);
        obj.putInt("chair", chair);
        obj.putInt("point", point);
        return  obj;
    }
}
