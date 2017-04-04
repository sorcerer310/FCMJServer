package com.rafo.chess.model.room;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/17.
 */
public class GBRoomCreateREQ {
    private String accountID;
    private int ID;
    private int roomID;
    private int count;
    private int type;
    private int maType;
    private String ip;
    private int serverID ;

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public int getMaType() {
        return maType;
    }

    public void setMaType(int type) {
        this.maType = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }


    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putUtfString("accountID",this.accountID);
        obj.putInt("ID",this.ID);
        obj.putInt("roomID",this.roomID);
        obj.putInt("count",this.count);
        obj.putInt("type",this.type);
        obj.putInt("maType",this.maType);
        obj.putUtfString("ip",this.ip);
        obj.putInt("serverID",this.serverID);
        return obj;
    }

    public static GBRoomCreateREQ fromSFSOBject(ISFSObject obj){
        GBRoomCreateREQ result = new GBRoomCreateREQ();
        result.setCount(obj.getInt("count"));
        result.setServerID(obj.getInt("serverID"));
        result.setType(obj.getInt("type"));
        result.setMaType(obj.getInt("maType"));
        result.setAccountID(obj.getUtfString("accountID"));
        result.setID(obj.getInt("ID"));
        result.setIp(obj.getUtfString("ip"));
        result.setRoomID(obj.getInt("roomID"));
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "uid ='" + accountID + '\'' +
                ", roomID=" + roomID +
                ", count=" + count +
                ", type=" + type +
                ", maType=" + maType +
                ", ip='" + ip + '\'' +
                ", serverID=" + serverID +
                '}';
    }
}
