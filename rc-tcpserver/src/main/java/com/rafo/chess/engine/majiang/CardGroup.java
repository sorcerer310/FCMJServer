package com.rafo.chess.engine.majiang;

import java.util.ArrayList;

/**
 * 牌组，用来记录开门的 顺子、刻子、杠 牌
 */
public class CardGroup {
	public int gType;
	public int targetId;																								//记录了该牌组由哪个玩家打出牌触发的
	private int subType; 																								//补充的类型，用于恢复牌局，比如暗杠的杠后杠类型

	ArrayList<MJCard> list = new ArrayList<MJCard>();

	public int getGType() {
		return gType;
	}

	public ArrayList<MJCard> getCardsList() {
		return list;
	}
	public CardGroup(int gType,ArrayList<MJCard> MJCards, int targetId){
		this.gType = gType;
		this.targetId = targetId;
		list.addAll(MJCards);
	}

	public int getSubType() {
		return subType;
	}

	public void setSubType(int subType) {
		this.subType = subType;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("CardGroupList[");
		for(int i=0;i<list.size();i++)
			sb.append(list.get(i).getCardNum()).append(",");
		sb.append("]-targetId:").append(targetId).append(" ");

		return sb.toString();
	}
}