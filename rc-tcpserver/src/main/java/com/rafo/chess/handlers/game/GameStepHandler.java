package com.rafo.chess.handlers.game;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.model.battle.WBBattleStepREQ;
import com.rafo.chess.core.GameExtension;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class GameStepHandler extends BaseClientRequestHandler
{
    final static Logger logger = LoggerFactory.getLogger("play");

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject)
    {
        GameExtension roomExt = (GameExtension) getParentExtension();
        WBBattleStepREQ message = new WBBattleStepREQ();
        try {
            assembleMessage(user, isfsObject, message);

            String tobeCards = "";
            if(message.getCards().size() > 0){
                Collections.sort(message.getCards());
                tobeCards = StringUtils.join(message.getCards(), ",");
            }
            //每收到一条客户端信息，执行一次play持续的将游戏进行下去
            roomExt.getGameService().play(Integer.parseInt(user.getName()), message.getPlayType(), message.getCard(), tobeCards);
        } catch (ActionRuntimeException e) {
            e.printStackTrace();
            logger.error("game_step_error\t"+ user.getLastJoinedRoom().getName() + "\t" + user.getName() + "\t" + user.getIpAddress() + "\t" + isfsObject.toJson());
            roomExt.getGameService().sendFailedStatus(Integer.parseInt(user.getName()));
        }
    }


    private void assembleMessage(User user, ISFSObject isfsObject, WBBattleStepREQ message)
    {
        message.setAccountId(user.getName());
        message.setRoomId(isfsObject.getInt("roomId"));
        if(isfsObject.containsKey("card")) {
            message.setCard(isfsObject.getInt("card"));
        }else{
            message.setCard(0);
        }

        if(isfsObject.containsKey("cards")){
            ISFSArray arr = isfsObject.getSFSArray("cards");
            int len = arr.size();
            for(int i=0;i<len;i++){
                message.getCards().add(arr.getInt(i));
            }
        }
        message.setPlayType(isfsObject.getInt("playType"));
    }

}
