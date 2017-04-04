package com.rafo.hall.task;



import com.rafo.chess.common.db.RedisManager;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.match.MatchExpression;
import com.smartfoxserver.v2.entities.match.StringMatch;
import com.smartfoxserver.v2.entities.match.UserProperties;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import java.util.List;

public class GMTask implements Runnable {

    private SFSExtension extension;
    private final Logger logger = LoggerFactory.getLogger("task");

    public GMTask(SFSExtension zoneExt) {
        this.extension = zoneExt;
    }

    @Override
    public void run() {
        Jedis jedis = null;
        try {
            jedis = RedisManager.getInstance().getRedis();
            while(true) {
                String uid = jedis.rpop("hall_user_card_update");
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
                    SFSObject data = new SFSObject();
                    data.putInt("roomCard", Integer.parseInt(card));
                    extension.send("SFS_EVENT_ACCOUNT_MODIFY", data, matchingUsers.get(0));
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
