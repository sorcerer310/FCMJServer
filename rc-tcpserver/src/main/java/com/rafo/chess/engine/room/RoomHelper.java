package com.rafo.chess.engine.room;

import com.rafo.chess.common.GlobalConstants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.exception.PersistException;

import com.rafo.chess.model.account.LoginRoom;
import com.rafo.chess.model.account.LoginUser;
import com.rafo.chess.model.room.BGRoomEnterRES;
import com.rafo.chess.model.room.GBRoomEnterREQ;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.match.BoolMatch;
import com.smartfoxserver.v2.entities.match.MatchExpression;
import com.smartfoxserver.v2.entities.match.RoomProperties;
import com.smartfoxserver.v2.entities.match.StringMatch;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2016/9/20.
 */
public class RoomHelper {

    public static void storeRoom2Redis(LoginRoom loginRoom) throws PersistException {
        RedisManager.getInstance().hMSetWithException("roomid."+Integer.toString(loginRoom.getRoomID()),loginRoom.toStrMap());
    }

    public static boolean couldDestroy(Room room){
        GameExtension extension = (GameExtension)room.getExtension();
        int uc = extension.getRoomService().getRoom().getAllPlayer().size();
        int agreeUC = 0;
        for(User u:room.getUserList()){
            UserVariable userVariable = u.getVariable("voteResult");
            if(userVariable!=null){
                String vr = userVariable.getStringValue();
                if(vr != null){
                    String[] kv = vr.split(":");
                    if(room.getName().equals(kv[0])){
                        if(!"1".equals(kv[1])){
                            agreeUC = agreeUC +1;
                        }
                    }
                }
            }

        }
        return agreeUC == uc;
    }

    public static boolean checkRoomId(int roomId, User user, SFSExtension extension){
        MatchExpression exp = new MatchExpression(RoomProperties.IS_GAME, BoolMatch.EQUALS, true).and (RoomProperties.NAME, StringMatch.EQUALS,roomId+"");
        List<Room> rooms = extension.getApi().findRooms(user.getZone().getRoomList(), exp, 1);
        boolean theServer = rooms.size() < 1;
        boolean global = RedisManager.getInstance().exists("roomid."+Integer.toString(roomId));
        return theServer&!global;

    }

    /**
     * 完成一局后消耗房卡，扣卡操作
     * @param room
     * @param currentBattleTime
     * @param extension
     * @return
     * @throws SFSVariableException
     * @throws PersistException
     */
    public static boolean subCard(Room room,int currentBattleTime,SFSExtension extension) throws SFSVariableException, PersistException {
        Logger logger = LoggerFactory.getLogger("room");
        boolean result = false;
        RoomVariable rv = room.getVariable("isSubcard");
        SFSObject createRoomREQ = (SFSObject)room.getProperty("message");
        int count = createRoomREQ.getInt("count");
        int willSubCard = 0;

        if(count == 0){
            willSubCard = 1;
        }else if(count == 1){
            willSubCard = 2;
        }else if(count ==2){
            willSubCard = 4;
        }

        //推广期不消耗钻石,如果正常消耗注释此处
        willSubCard = 0;


        if(!rv.getBoolValue() && currentBattleTime>=1){
            try {
                RoomVariable isSubCard = new SFSRoomVariable("isSubcard", true);
                isSubCard.setHidden(true);
                room.setVariable(isSubCard);
                extension.getApi().setRoomVariables(room.getOwner(), room, Arrays.asList(isSubCard));

                //只有房主减房卡
//                LoginUser loginUser = new LoginUser(room.getOwner());
//                int now_card = loginUser.getCard() - willSubCard;
//
//                room.getOwner().setProperty("sfsobj", loginUser.toSFSObject());
//
//                loginUser.setCard(now_card);
//                LoginService.updateUserCard(Integer.parseInt(room.getOwner().getName()), willSubCard, room.getName());
//                LoginService.updateUserAttribute(Integer.parseInt(room.getOwner().getName()), "card", String.valueOf(now_card));
//
//                sendCardUpdate(extension, now_card, room.getOwner());

                //所有人减房卡
                for(User u:room.getPlayersList())
                {
                    LoginUser loginUser = LoginService.getUserFromRedis(u.getName());
                    int now_card = loginUser.getCard() - willSubCard;

                    loginUser.setCard(now_card);
                    LoginService.updateUserCard(Integer.parseInt(u.getName()), willSubCard,now_card, room.getName());
                    LoginService.updateUserAttribute(Integer.parseInt(u.getName()), "card", String.valueOf(now_card));

                    sendCardUpdate(extension, now_card, u);
                }

                logger.debug(System.currentTimeMillis()+"\t"+room.getOwner().getName()+"\tsubcard\t"+room.getOwner().getIpAddress()+"\t"+
                        room.getName()+"\t"+count+"\t"+willSubCard);
            }catch (Exception e){
                e.printStackTrace();
                logger.debug(System.currentTimeMillis()+"\t"+room.getOwner().getName()+"\tsubcardfail\t"+room.getOwner().getIpAddress()+"\t"+
                        room.getName()+"\t"+count+"\t"+willSubCard+"\t"+e.getMessage());
            }
        }
        return result;
    }

    public static void destroyRoom(SFSExtension extension) throws SFSVariableException, PersistException {
        Room room = extension.getParentRoom();
        room.destroy();
        RoomHelper.clearUserRoomInfo(room);
        RoomHelper.destroyRedisRoom(room);
        RoomManager.destroyRoom(Integer.parseInt(room.getName()));
        extension.getApi().removeRoom(room);
    }

    public static void clearUserRoomInfo(Room room) throws PersistException {
        RoomInstance innerRoom = RoomManager.getRoomInstnaceByRoomid(Integer.parseInt(room.getName()));
        if(innerRoom !=null) {
            List<IPlayer> players = innerRoom.getAllPlayer();
            for (IPlayer player : players) {
                RedisManager.getInstance().hMSet("uid." + player.getUid(), "room", "0");
            }
        }
    }

    public static void destroy(Room room,SFSExtension extension,ISFSObject resp) throws PersistException, SFSVariableException {
        RoomHelper.destroyRoom(extension);
        resp.putInt("result", GlobalConstants.WC_VOTE_DESTROY_SUCCESS);
        extension.send(CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP,resp,room.getUserList());
    }

    public static boolean checkCard(int card,int count){
    	//0:4局 钻石x2  1:8局 钻石x3  2:16局 钻石x5
        if(count == 0){
            return card >= RoomAttributeConstants.ROOM_ZUANSHI_4;
        }else if(count == 1) {
            return card >= RoomAttributeConstants.ROOM_ZUANSHI_8;
        }else if(count == 2) {
            return card >= RoomAttributeConstants.ROOM_ZUANSHI_16;
        }
        return false;
    }

    public static BGRoomEnterRES enterFailed(GBRoomEnterREQ message, int errorCode)
    {
        BGRoomEnterRES res = new BGRoomEnterRES();
        res.setResult(errorCode);
        res.setRoomID(message.getRoomID());
        res.setApplierAccountID(message.getAccountID());
        res.setApplierID(message.getID());
        return res;
    }

    public static void destroyRedisRoom(Room room) {
        RedisManager.getInstance().del("roomid."+Integer.parseInt(room.getName()));
    }

    public static void sendCardUpdate(SFSExtension extension, int card, User user){
        SFSObject data = new SFSObject();
        data.putInt("roomCard", card);
        CmdsUtils.sendMessage(extension, CmdsUtils.SFS_EVENT_ACCOUNT_MODIFY, data, user);
    }

}
