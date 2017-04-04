package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.Map;

/**
 * Created by Administrator on 2016/10/28.
 */
public class BattleSeat {

    private Map<Integer, Integer[]> playerDice; //首局定座位
    private int bankerId;
    private int[] theDice; //每局摇骰子摇出来的数
    private int[] playerIds; //用户的顺序
    private int red; //红点

    public Map<Integer, Integer[]> getPlayerDice() {
        return playerDice;
    }

    public void setPlayerDice(Map<Integer, Integer[]> playerDice) {
        this.playerDice = playerDice;
    }

    public int getBankerId() {
        return bankerId;
    }

    public void setBankerId(int bankerId) {
        this.bankerId = bankerId;
    }

    public int[] getTheDice() {
        return theDice;
    }

    public void setTheDice(int[] theDice) {
        this.theDice = theDice;
    }

    public int[] getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(int[] playerIds) {
        this.playerIds = playerIds;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public SFSObject toSFSObject(){
        SFSObject data = new SFSObject();
        if(playerDice != null){
            ISFSArray seats = new SFSArray();
            for(int playerId :  playerIds){
                ISFSObject seat = new SFSObject();
                seat.putInt("id", playerId);

                Integer[] dices = playerDice.get(playerId);
                seat.putInt("s1", dices[0]);
                seat.putInt("s2", dices[1]);

                seats.addSFSObject(seat);
            }
            data.putSFSArray("seats", seats);
        }

        ISFSObject banker = new SFSObject();
        banker.putInt("id", bankerId);
        banker.putInt("s1", theDice[0]);
        banker.putInt("s2", theDice[1]);
        banker.putInt("red", red);
        data.putSFSObject("banker", banker);

        return data;
    }

}
