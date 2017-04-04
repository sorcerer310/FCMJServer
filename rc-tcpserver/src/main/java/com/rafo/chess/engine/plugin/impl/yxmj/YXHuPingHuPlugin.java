package com.rafo.chess.engine.plugin.impl.yxmj;

import com.rafo.chess.engine.game.YNMJGameType;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;
import com.smartfoxserver.v2.entities.Room;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/***
 * 屁和，如果吃牌了，就不能屁和，只能和清一色
 * @author Administrator
 */
public class YXHuPingHuPlugin extends YXHuPlugin {
	@Override
	public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
		//判断是否有抢杠或者杠上开花状态，如果有闭胡模式也可以小胡
		RoomInstance room = RoomManager.getRoomInstnaceByRoomid(player.getRoomId());
		ArrayList<IPlayer> allPlayer = room.getAllPlayer();
		for(IPlayer p:allPlayer){
			if(p.getUid()==player.getUid()){
				//杠上开花判断的是自己
				if(((MJPlayer)p).isGangShangKaiHuaFlag()){
					if(huCondition(handCards,groupList)){
						addAttach(player,handCards,groupList);
						return true;
					}
				}
			}
			//抢杠胡判断的是别人
			else {
				if (((MJPlayer) p).isQiangGangPingHuFlag()) {
					if (huCondition(handCards, groupList)) {
						//增加附加得分条件
						addAttach(player, handCards, groupList);
						return true;
					}
				}
			}
		}

		//测试代码
		if( handCards.get(handCards.size()-1).getCardNum()==13)
			System.out.println("aaa");
		//测试代码

		//如果房间设置为闭和，则不能和平和
		if(RoomManager.getRoomInstnaceByRoomid(player.getRoomId()).getAttribute(RoomAttributeConstants.YB_PLAY_TYPE)==0)
			return false;

		//判断是否达成胡牌条件
		if(!huCondition(handCards,groupList))
			return false;

		//增加附加得分条件
		addAttach(player,handCards,groupList);
		return true;
	}

	/**
	 * 判断是否达成小胡条件
	 * @param handCards
	 * @param groupList
	 * @return
	 */
	private boolean huCondition(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList){

		//如果吃牌了，就不能平和，只能和清一色
		if(groupList.size()>0){
			for(CardGroup cg:groupList){
				if(cg.getCardsList().get(0).getCardNum() != cg.getCardsList().get(1).getCardNum())
					return false;
			}
		}


		int[] aHandCards = this.list2intArray(handCards);

		if(!(this.isHu(aHandCards) && this.isjia))
			return false;
		return true;
	}

	/**
	 * 增加附加得分条件
	 * @param player		玩家对象
	 * @param handCards		玩家手牌
	 * @param groupList		玩家开门牌
	 */
	private void addAttach(MJPlayer player,ArrayList<MJCard> handCards,ArrayList<CardGroup> groupList){
		//增加额外加分项
		player.getHuAttachType().clear();
		//门前清必须是自摸
//		if(this.isMenQianQing(handCards,groupList,player)) {
//			player.getHuAttachType().add(YNMJGameType.HuAttachType.MenQianQing);
//			player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);
//		}
		//开门自摸单独加
		if(this.isZiMo(handCards,player))
			player.getHuAttachType().add(YNMJGameType.HuAttachType.ZiMo);

		//增加全求人
		if(this.isQuanQiuRen(handCards,groupList,player))
			player.getHuAttachType().add(YNMJGameType.HuAttachType.QuanQiuRen);
	}
}
