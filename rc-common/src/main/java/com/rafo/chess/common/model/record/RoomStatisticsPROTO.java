package com.rafo.chess.common.model.record;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/17.
 */
// 一个房间历史战绩统计，包含四个玩家的得分，名称等
public class RoomStatisticsPROTO {

    private int startTime ;
    private int roomID ;
    private int recordID;
    private List<PlayerPointInfoPROTO> playerInfo = new ArrayList<>(); // 四个玩家在房间内的总积分统计

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public int getRecordID() {
        return recordID;
    }

    public void setRecordID(int recordID) {
        this.recordID = recordID;
    }

    public List<PlayerPointInfoPROTO> getPlayerInfo() {
        return playerInfo;
    }

    public void setPlayerInfo(List<PlayerPointInfoPROTO> playerInfo) {
        this.playerInfo = playerInfo;
    }

    public void addPlayerInfo(PlayerPointInfoPROTO playerPointInfoPROTO) {
        this.playerInfo.add(playerPointInfoPROTO);
    }
    

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("startTime", startTime);
        obj.putInt("roomID", roomID);
        obj.putInt("recordID", recordID);
        SFSArray infos = new SFSArray();
        for(PlayerPointInfoPROTO ppi : playerInfo){
            infos.addSFSObject(ppi.toSFSObject());
        }
        obj.putSFSArray("playerInfo", infos);
        return obj;
    }
}
