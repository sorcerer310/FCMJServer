package com.rafo.chess.core;

import com.rafo.chess.engine.majiang.service.YNMJGameService;
import com.rafo.chess.handlers.HeartBeatHandler;
import com.rafo.chess.model.battle.BattleMa;
import com.rafo.chess.model.room.GBRoomCreateREQ;
import com.rafo.chess.engine.room.RafoRoomService;
import com.rafo.chess.handlers.chat.ChatHandler;
import com.rafo.chess.handlers.game.GameDingZhuangOKHandle;
import com.rafo.chess.handlers.game.GameMaFinishHandler;
import com.rafo.chess.handlers.game.GamePlayerOfflineHandler;
import com.rafo.chess.handlers.game.GameStartHandler;
import com.rafo.chess.handlers.game.GameStepHandler;
import com.rafo.chess.handlers.room.RoomDestoryHandler;
import com.rafo.chess.handlers.room.RoomJoinEventHandler;
import com.rafo.chess.handlers.room.RoomQuitHandler;
import com.rafo.chess.resources.DataContainer;
import com.rafo.chess.resources.define.IRegister;
import com.rafo.chess.service.ChatService;
import com.rafo.chess.template.TemplateGenRegister;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

/**
 * Created by Administrator on 2016/9/12.
 */
public class GameExtension extends SFSExtension {

    private YNMJGameService gameService;
    private RafoRoomService roomService;
    private ChatService chatService;

    @Override
    public void init() {
        gameService = new YNMJGameService(this);
        roomService = new RafoRoomService(this);
        chatService = new ChatService();

/*
        String resourcePath = this.getConfigProperties().getProperty("engine.resource.path");
        try {
            IRegister register = new TemplateGenRegister();
            DataContainer.getInstance().init(register,"", resourcePath);
        }catch (Exception e){
            e.printStackTrace();
        }
*/


        addEventHandler(SFSEventType.USER_JOIN_ROOM, RoomJoinEventHandler.class);
        addEventHandler(SFSEventType.USER_DISCONNECT,GamePlayerOfflineHandler.class);

        addRequestHandler(CmdsUtils.CMD_ROOM_QUIT, RoomQuitHandler.class);
        addRequestHandler(CmdsUtils.CMD_ROOM_DESTROY,RoomDestoryHandler.class);
        addRequestHandler(CmdsUtils.CMD_ROOM_DESTROY_VOTE_REQ,RoomDestoryHandler.class);
        addRequestHandler(CmdsUtils.SFS_EVENT_CHAT_SYN,ChatHandler.class);

        addRequestHandler(CmdsUtils.CMD_BATTLE_START,GameStartHandler.class);
        addRequestHandler(CmdsUtils.CMD_BATTLE_STEP,GameStepHandler.class);
        
        // 加入定庄完成准备
        addRequestHandler(CmdsUtils.CMD_BANKER_SET_READY, GameDingZhuangOKHandle.class);
        // 码关闭显示
        addRequestHandler(CmdsUtils.SFS_EVENT_MA_FINISH, GameMaFinishHandler.class);

        addRequestHandler(CmdsUtils.CMD_PING, HeartBeatHandler.class);
    }

    public boolean initService(GBRoomCreateREQ message, int playerId){
        if(gameService.getRoom() == null) {
            try {
                roomService.createRoom(message, playerId );
                gameService.setRoom(roomService.getRoom());
                chatService.init(roomService.getRoom());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public YNMJGameService getGameService() {
        return gameService;
    }

    public RafoRoomService getRoomService() {
        return roomService;
    }

    public ChatService getChatService() {
        return chatService;
    }

}
