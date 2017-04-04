package com.rafo.chess.common.model.record;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/17.
 */
public class RoundDataPROTO {

    private int id;
    private int startTime ;
    private List<PlayerPointInfoPROTO> playerInfo = new ArrayList<>(); // 四名玩家在这一局的得分信息
    private SFSObject battleData ; // 战斗回放

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public List<PlayerPointInfoPROTO> getPlayerInfo() {
        return playerInfo;
    }

    public void setPlayerInfo(List<PlayerPointInfoPROTO> playerInfo) {
        this.playerInfo = playerInfo;
    }

    public SFSObject getBattleData() {
        return battleData;
    }

    public void setBattleData(SFSObject battleData) {
        this.battleData = battleData;
    }

    public void addPlayerInfo(PlayerPointInfoPROTO playerInfoPROTO) {
        this.playerInfo.add(playerInfoPROTO);
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("id", id);
        obj.putInt("startTime", startTime);

        SFSArray infos = new SFSArray();
        for(PlayerPointInfoPROTO ppi : playerInfo){
            infos.addSFSObject(ppi.toSFSObject());
        }

        obj.putSFSArray("playerInfo", infos);
        if(battleData != null) {
            obj.putSFSObject("battleData", battleData);
        }

        return obj;
    }
}
