package com.rafo.chess.model.room;

import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.engine.room.RoomHelper;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.match.MatchExpression;
import com.smartfoxserver.v2.entities.match.StringMatch;
import com.smartfoxserver.v2.entities.match.UserProperties;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class VoteDestroyTaskRoom implements Runnable {

    private SFSExtension extension;
    private final Logger logger = LoggerFactory.getLogger("task");
    private final Logger roomLogger = LoggerFactory.getLogger("room");

    public VoteDestroyTaskRoom(SFSExtension zoneExt) {
        this.extension = zoneExt;
    }

    @Override
    public void run() {
        try {
            int userCount = extension.getParentZone().getUserManager().getUserCount();

            Properties props = extension.getConfigProperties();
            int serverId = Integer.parseInt(props.getProperty("server.id").trim());
            LoginService.updateSeverUserCount(serverId,userCount);

            List<Room> rooms = extension.getParentZone().getRoomList();
            List<Room> roomList = new ArrayList<Room>();
            for(Room r:rooms){
                if(r.isGame()){
                    roomList.add(r);
                }
            }

            for(Room r:roomList){
                SFSExtension roomExt =  (SFSExtension)r.getExtension();
                checkVoteTime(r,roomExt);
            }

            updateUserCard();

        }catch (Exception e){
            e.printStackTrace();
            logger.debug("task error!!!!"+ e.getMessage());
            System.out.println(e.getMessage());
        }

    }


    private void checkVoteTime(Room room,SFSExtension roomExt) throws PersistException, SFSVariableException {
        RoomVariable isVote = room.getVariable("isVote");
        if(isVote != null){

            if(isVote.getBoolValue()){
                int voteTime = room.getVariable("voteTime").getIntValue();
                int now = (new Long(System.currentTimeMillis()).intValue())/1000;
                if((now - voteTime)>= 60){
                    for(User u:room.getUserList()){
                        UserVariable uv = u.getVariable("voteResult");
                        if(uv == null){
                            UserVariable voteResult = new SFSUserVariable("voteResult", room.getName()+":"+0);
                            voteResult.setHidden(true);
                            roomExt.getApi().setUserVariables(u, Arrays.asList(voteResult));
                        }
                    }
                    boolean flag = RoomHelper.couldDestroy(room);
                    if(flag){
                        ISFSObject resp = new SFSObject();
                        RoomHelper.destroy(room,roomExt,resp);
                        roomLogger.debug(System.currentTimeMillis()+"\tvotetask\t"+ CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP
                                +"\t127.0.0.1\t"+ room.getName() +"\t"+ 0 +"\tsuccess\tsuccess");
                    }

                    logger.debug(System.currentTimeMillis()+"\t"+"destroyRoomTask"+"\t"+ room.getName() +"\t"+
                            "isVote:"+isVote.getBoolValue()+"\t"+ "couldDestroy:"+flag);

                }

            }
        }

    }

    private void updateUserCard(){
        Jedis jedis = null;
        try {
            jedis = RedisManager.getInstance().getRedis();
            while(true) {
                String uid = jedis.rpop("game_user_card_update");
                if (uid == null) {
                    break;
                }

                String status = RedisManager.getInstance().hGet("uid." + uid, "status");
                if (status == null || "0".equals(status)) { //未登录
                    continue;
                }

                MatchExpression userNameExp = new MatchExpression(UserProperties.NAME, StringMatch.EQUALS, uid);
                List<User> matchingUsers = extension.getApi().findUsers(extension.getParentZone().getUserList(), userNameExp, 1);

                if (matchingUsers.size() == 1) {
                    String card = RedisManager.getInstance().hGet("uid." + uid, "card");
                    RoomHelper.sendCardUpdate(extension, Integer.parseInt(card), matchingUsers.get(0));
                    logger.debug("updateUserCard\t"+uid+"\t" + card + "\tsuccess");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.debug("updateUserCard\tfail\t"+e.getMessage());
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

}
