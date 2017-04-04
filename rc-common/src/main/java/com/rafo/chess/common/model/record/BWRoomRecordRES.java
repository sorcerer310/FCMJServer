package com.rafo.chess.common.model.record;


import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

/**
 * Created by Administrator on 2016/9/17.
 */
public class BWRoomRecordRES {

    private int result ;
    private SFSArray roomStatistics;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public SFSArray getRoomStatistics() {
        return roomStatistics;
    }

    public void setRoomStatistics(SFSArray roomStatistics) {
        this.roomStatistics = roomStatistics;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("result", result);
        if(roomStatistics != null){
            obj.putSFSArray("roomStatistics", roomStatistics);
        }
        return obj;
    }
}
