package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YNHuShiSanLanPlugin extends YNHuPlugin {
	private final Logger logger = LoggerFactory.getLogger("play");
	public boolean checkHu(MJPlayer player, ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {		
		if(handCards.size() < 13)
			return false;
		
		try {			
			handCards = ArraySort(handCards);

           	// 检查重复牌
           	for(int i = 0; i < handCards.size(); ++i)
           	{
           		if( checkRepeat(handCards, handCards.get(i).getCardNum() ) )
           			return false;
           	}

            // 先检查字牌数量
            int ziCount = GetziCount(handCards);
            if(ziCount < 5 ||ziCount > 7)
                return false;

            // 检查非字牌
			for(int i = 0; i < handCards.size() - ziCount; ++i)
			{
				if(checkColorRuler(handCards, handCards.get(i).getCardNum(), i) == false)
					return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.debug("十三烂胡牌:" + e.toString());
			return false;
		}
		
		logger.debug("十三烂胡牌");
		return true;
	}
	
	private boolean checkRepeat(ArrayList<MJCard> cards, int nCardNum)
	{
		int nCount =0;
		for(int i = 0; i < cards.size(); ++i)
		{			
			if(cards.get(i).getCardNum() == nCardNum )
			{
				++nCount;
				if ( nCount > 1)	//超出当前的一张牌，才算重复
					return true;
			}
		}
		
		return false;
	}

    private int GetziCount(ArrayList<MJCard> list)
    {
        int nCount = 0;
        for(int i = 0; i < list.size(); ++i)
        {
            if(list.get(i).getCardNum() > 40)
                nCount++;
        }

        return nCount;
    }

    // 检查花色规则
    boolean checkColorRuler(ArrayList<MJCard> handCards, Integer cardNum, int nIdx)
    {
    	if(nIdx == 0)
    		return true;

    	int nColor = cardNum.intValue() / 10;
    	// 看看前一张牌是否同色
    	int nPreColor = handCards.get(nIdx - 1).getCardNum() / 10;

    	if(nColor == nPreColor)
    	{
    		if(cardNum.intValue() - handCards.get(nIdx - 1).getCardNum() < 3)
    			return false;
    	}

    	return true;
    }
}
