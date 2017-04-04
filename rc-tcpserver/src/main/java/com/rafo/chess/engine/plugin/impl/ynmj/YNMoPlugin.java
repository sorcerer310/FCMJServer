package com.rafo.chess.engine.plugin.impl.ynmj;

import com.rafo.chess.engine.majiang.action.MoAction;
import com.rafo.chess.engine.plugin.impl.MoPlugin;
import com.rafo.chess.engine.room.RoomAttributeConstants;

import java.util.HashMap;
import java.util.Map;

/***
 * 发牌
 * 
 * @author Administrator
 * 
 */
public class YNMoPlugin extends MoPlugin {

    @Override
    public void createCanExecuteAction(MoAction action) {

/*        Map<Integer, Integer> map = (HashMap<Integer, Integer>) action.getRoomInstance()
                .getAttribute(RoomAttributeConstants.YN_GAME_QUE);
        if(map.size() != action.getRoomInstance().getPlayerArr().length){
            return;
        }*/

        super.createCanExecuteAction(action);
    }
}
