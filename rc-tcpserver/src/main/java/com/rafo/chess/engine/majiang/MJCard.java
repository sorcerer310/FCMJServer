package com.rafo.chess.engine.majiang;

import com.rafo.chess.engine.gameModel.BaseCard;

/***
 * 棋牌对象 Wan(11), Tiao(21), Tong(31), Zi(41);
 * @author Administrator
 * 
 */
public class MJCard extends BaseCard {
    public enum MJCardType{
        WAN,TIAO,TONG,ZI
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append(this.getCardNum()).append(" ");
        return sb.toString();
    }
}
