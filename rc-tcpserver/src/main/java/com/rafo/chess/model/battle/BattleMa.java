package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/28.
 */
public class BattleMa {
	private Integer maTypeInteger = 0;	// RoomAttributeConstants/** 加码类型		0：不加码 1:翻码 2:加码x4 3:加码x8 4:加码x16 */
    private ArrayList<ArrayList<Integer>> playerMaResult; //四个人的抽码结果
    private ArrayList<Integer> maCards; //摸出的码牌
    private ArrayList<Integer> maScores; //四家的得分

    public ArrayList<ArrayList<Integer>> getMaResult() {
        return playerMaResult;
    }

    public void setMaResult(ArrayList<ArrayList<Integer>> playerMa) {
        this.playerMaResult = playerMa;
    }
    
    public ArrayList<Integer> getMaScores() {
        return maScores;
    }
    
    public void setMaScores(ArrayList<Integer> scores) {
        this.maScores = scores;
    }
    
    public ArrayList<Integer> getMaCards() {
        return maCards;
    }

    public void setMaCards(ArrayList<Integer> cards) {
        this.maCards = cards;
    }
    
    public void setMaType(Integer type)
    {
    	maTypeInteger = type;
    }
    
    public Integer getMaType()
    {
    	return maTypeInteger;
    }

    public SFSObject toSFSObject(){
    	SFSObject data = new SFSObject();
    	try {       
            data.putInt("matype", maTypeInteger);
            
            SFSArray cards = new SFSArray();
            for(int i = 0; i < maCards.size(); ++i)
            {
            	SFSObject obj = new SFSObject();
            	obj.putInt("value", maCards.get(i));
            	cards.addSFSObject(obj);
            }
            data.putSFSArray("cards", cards);
            
            if(playerMaResult != null){
                ISFSArray result = new SFSArray();
                for(int i = 0; i < 4; ++i){
                    SFSObject maResult = new SFSObject();
                    maResult.putInt("index", i);

                    ArrayList<Integer> cardsResult = playerMaResult.get(i);
                    maResult.putInt("count", cardsResult.size());
                    
                    SFSArray reArray = new SFSArray();
                    for(int j = 0; j < cardsResult.size(); ++j)
                    {
                    	SFSObject obj = new SFSObject();
                    	obj.putInt("value", cardsResult.get(j));
                    	reArray.addSFSObject(obj);
                    }
                    maResult.putSFSArray("value", reArray);

                    result.addSFSObject(maResult);
                }
                
                data.putSFSArray("result", result);
            }

            SFSArray scores = new SFSArray();
            for(int i = 0; i < maScores.size(); ++i)
            {
            	SFSObject obj = new SFSObject();
            	obj.putInt("value", maScores.get(i));
            	//logger.debug("ma-toSFSObject:Score:"+maScores.get(i) );
            	scores.addSFSObject(obj);
            }
            data.putSFSArray("scores", scores);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}      
        
        return data;
    }

}