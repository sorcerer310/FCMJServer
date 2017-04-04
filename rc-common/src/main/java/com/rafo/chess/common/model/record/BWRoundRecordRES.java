package com.rafo.chess.common.model.record;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

/**
 * Created by Administrator on 2016/9/17.
 */
public class BWRoundRecordRES {
    private int result ;
    private String accountID ;
//    private List<RoundDataPROTO> roundData = new ArrayList<>(); // 内含回放
    private SFSArray roundData;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public SFSArray getRoundData() {
        return roundData;
    }

    public void setRoundData(SFSArray roundData) {
        this.roundData = roundData;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("result", result);
        obj.putUtfString("accountID", accountID);
        if(obj != null){
            obj.putSFSArray("roundData", roundData);
        }
        return obj;
    }
}
