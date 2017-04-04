package com.rafo.chess.utils;

import org.apache.commons.lang.ArrayUtils;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by fengchong on 2017/3/8.
 */
public class TestUtils {
    public static void main(String[] args){
        int[] aHandCards = new int[]{31,31,31,33,33,34,35,35,36,37};

        //去掉和的牌只判断手牌
        int[] inHandCard = ArrayUtils.remove(aHandCards,aHandCards.length-1);
        HashMap<Integer,Integer> mapInHandCards = arrayHandsCardCount(inHandCard);
        Iterator<Integer> itinc = mapInHandCards.keySet().iterator();
        while(itinc.hasNext()){
            Integer cNum = itinc.next();
            //去掉对子
            if(mapInHandCards.get(cNum)==2)
                inHandCard = arrayRemove(inHandCard,cNum,2);
            //去掉刻子
            if(mapInHandCards.get(cNum)==3 || mapInHandCards.get(cNum)==4)
                inHandCard = arrayRemove(inHandCard,cNum,3);
        }
        //去掉顺子，剩余的牌是将要和的牌
        while(inHandCard.length>=3){
            int[] temp ;
            int cardTemp = inHandCard[0];
            for(int i=0;i<3;i++){
                temp = arrayRemove(inHandCard,cardTemp+i,1);
                if(temp==null)
                    break;
                inHandCard=temp;
            }
        }

        int lastCard = aHandCards[aHandCards.length-1];
        if(inHandCard.length==1 && inHandCard[0]==lastCard)
            System.out.print("success");
            //2张牌夹算和
        else if(inHandCard.length==2 && inHandCard[0]+1==lastCard && lastCard+1==inHandCard[1])
            System.out.print("success");
            //2张牌边也算和
        else if((inHandCard.length==2 && inHandCard[0]%10==1 && inHandCard[1]%10==2 && lastCard%10==3)
                || inHandCard.length==2 && inHandCard[0]%10==8 && inHandCard[1]%10==9 && lastCard%10==7)
            System.out.print("success");


    }

    public static int[] arrayRemove(int[] cards, int ocard, int countLimit) {
        int count = 0;
        int[] reCards = new int[cards.length - countLimit];
        int index = 0;
        for (int i = 0; i < cards.length; i++) {
            //当前牌等于要移除的目标牌 且 计数count小于移除数量上线，count++
            if (cards[i] == ocard && count < countLimit) {
                count++;
                continue;
            }
            if (index < reCards.length)
                reCards[index++] = cards[i];
            else {
                return null;
            }
        }
        if (count != countLimit)
            return null;
        return reCards;
    }

    public static HashMap<Integer, Integer> arrayHandsCardCount(int[] cardsTemp) {
        // 计数
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < cardsTemp.length; i++) {
            if (!map.containsKey(cardsTemp[i])) {
                map.put(cardsTemp[i], 0);
            }
            int count = map.get(cardsTemp[i]) + 1;
            map.put(cardsTemp[i], count);
        }
        return map;
    }

}
