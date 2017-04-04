package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.DealerDingZhuangAction;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.template.impl.PluginTemplateGen;
import com.rafo.chess.utils.MathUtils;

import java.util.*;

/***
 * 下一个人接庄
 * @author Administrator
 * 
 */
public class YNDingzhuangPlugin implements IOptPlugin<DealerDingZhuangAction> {
	PluginTemplateGen gen = null;

	@Override
	public void doOperation(DealerDingZhuangAction act) {
		RoomInstance<MJCard> room = act.getRoomInstance();
		room.addRound(); //局数加一
		
		//测试代码
		//if ( room.getCurrRounds() > 1 )
		//	room.setCurrRounds(12);

		//黄庄，不换庄家
		if( room.getLastWinner().size() ==0 && room.getCurrRounds()>1 ){
			return;
		}
		
		MJPlayer banker = null;
		//运营要求抢杠胡：2.2	被抢杠（点炮）者下一把变更为庄家。
		if ( room.IsBankerQiang() && room.getBankerQiangUid() > 0 )
		{
			
			banker = (MJPlayer) room.getPlayerById(room.getBankerQiangUid());
			
			room.setBankerQiangUid( 0 );
			room.SetBankerQiang( false );
		}
		
		//如果前面抢杠胡的没有庄，才走之前的逻辑
		if ( banker == null)
		{
			banker = (MJPlayer) room.getPlayerById(room.getBankerUid());
			if (banker == null) {
				// 用骰子决定庄家
				int index = MathUtils.random(0, room.getPlayerArr().length - 1);
				
				banker = (MJPlayer) room.getPlayerArr()[index];
			} else if(room.getLastWinner().size() > 0 &&
					room.getLastWinner().get(0) != banker.getUid()) { //庄家输了，才换庄
				int nextIndex = banker.getIndex() + 1;
				nextIndex = nextIndex > 3 ? nextIndex - 4 : nextIndex;
				banker = (MJPlayer) room.getPlayerArr()[nextIndex];
			}
		}
		
		banker.setBanker(true);

		if(room.getBankerUid() > 0 && room.getBankerUid() != banker.getUid()){ //reset is banker
			MJPlayer lastBanker = (MJPlayer) room.getPlayerById(room.getBankerUid());
			if(lastBanker!= null){
				lastBanker.setBanker(false);
			}
		}
		room.setBankerUid(banker.getUid());
	}

	@Override
	public PluginTemplateGen getGen() {
		return gen;
	}

	@Override
	public void setGen(PluginTemplateGen gen) {
		this.gen = gen;
	}

	@Override
	public boolean doPayDetail(PayDetailed pd, RoomInstance room, Calculator calculator) {
		return false;
	}

}
