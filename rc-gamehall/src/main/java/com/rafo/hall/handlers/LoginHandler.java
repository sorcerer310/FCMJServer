package com.rafo.hall.handlers;

import com.bbzhu.cache.Cache;
import com.bbzhu.system.HttpCenter;
import com.rafo.chess.gm.GMUtils;
import com.rafo.hall.common.GlobalConstants;
import com.rafo.hall.core.HallExtension;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.hall.model.cache.CacheKey;
import com.rafo.hall.service.ActiveService;
import com.rafo.hall.utils.CmdsUtils;
import com.rafo.hall.utils.HallRdb;
import com.rafo.hall.vo.*;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.www.model.Server;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/18.
 */
public class LoginHandler extends BaseServerEventHandler {

    private static int RESOURCE_UPDATE = 0x1110002;
    private final Logger logger = LoggerFactory.getLogger("login");
    private final Logger msgLogger = LoggerFactory.getLogger("message");
    @Override
    public void handleServerEvent(ISFSEvent isfsEvent) throws SFSException {
        long begin = System.currentTimeMillis();
        User user = (User) isfsEvent.getParameter(SFSEventParam.USER);
        String uid =(String)user.getSession().getProperty("uid");
        if (GMUtils.getGMUtils().isGMUser(user.getName())) {
			return;
		}
        user.setName(uid);
        Map<String , String> uinfo = RedisManager.getInstance().hMGetAll("uid."+uid);
        ISFSObject respObj = new SFSObject();

        if (uinfo.size() == 0 || uinfo.get("ID") == null) {
            respObj.putInt("result",GlobalConstants.LOGIN_FAILED_NOTUSER);
            send(CmdsUtils.CMD_Update , respObj , user);
            logger.debug("无效的用户ID用来登录：uid : " + uid + ", spend " + (System.currentTimeMillis()- begin) + "ms");
            throw new SFSLoginException("login error;");
        }

        int currentChannel = StringUtils.isBlank(uinfo.get("channel"))?0:Integer.parseInt(uinfo.get("channel"));
        int currentVersion = StringUtils.isBlank(uinfo.get("version"))?0:Integer.parseInt(uinfo.get("version"));
        Map<String,String> errorInfo = new HashMap<>();
        for (Map.Entry<String, String> entry : uinfo.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.equals("ID") || key.equals("card") || key.equals("points") || key.equals("total") || key.equals("room") || key.equals("haveNewEmail")){
                if (value.equals("")){
                    respObj.putInt(key, 0);
                } else {
                    try {
                        respObj.putInt(key, Integer.valueOf(value));
                    }catch (Exception e){
                        respObj.putInt(key, 0);
                        errorInfo.put(key, "0");
                    }
                }
            } else {
                respObj.putUtfString(key, value);
            }
        }

        if(errorInfo.size() > 0){
            RedisManager.getInstance().hMSet("uid." + uid, errorInfo);
        }

        respObj.putInt("result" , GlobalConstants.LOGIN_SUCCESS);
        send(CmdsUtils.CMD_Update , respObj , user);
        logger.debug("login\t" + uid + "\t"  + user.getId() + "\t" + user.getIpAddress() + "\t" + currentChannel
                + "\t"+currentVersion + "\t" + (System.currentTimeMillis()- begin));

        try {
            GameEmail mail = HallRdb.getEmail(Integer.parseInt(uid));
            if(mail != null){
                WCHaveNewEmailSynRES res = new WCHaveNewEmailSynRES();
                res.setNewNumber(mail.getEmail_new());//有新邮件,在线推送
                send(CmdsUtils.CMD_NEWEMAILSYN, res.toObject(), user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //推送公告
        HallExtension hallExt = (HallExtension) getParentExtension();
        GameNotice notice = hallExt.getNoticeService().getGameNotice();
        if (notice != null){
            send(CmdsUtils.CMD_NOTICE_SYNC, notice.toObject(), user);
        }

        //推送跑马灯
        List<Marquee> marquees = HallRdb.getMarqueeList();
        for (Marquee marquee : marquees){
            WCMarqueeSYN mar = new WCMarqueeSYN();
            mar.setContent(marquee.getContent());
            mar.setColor(marquee.getColor());
            mar.setRollTimes(marquee.getRollTimes());
            send(CmdsUtils.CMD_MARQUEESYN, mar.toObject(), user);
        }

        //从数据库读跑马灯数据
//        List<Marquee> marquees = HallRdb.getMarqueeFromDB();
//        for (Marquee marquee : marquees){
//            WCMarqueeSYN mar = new WCMarqueeSYN();
//            mar.setContent(marquee.getContent());
//            int a=Integer.valueOf(marquee.getColor(),16);
//
//            int r=a>>16&0xff;
//            int g=a>>8&0xff;
//            int b=a>>0&0xff;
//            String cr=""+r+","+g+","+b;
//            mar.setColor(cr);
//            mar.setRollTimes(marquee.getRollTimes());
////            mar.setColor(marquee.getColor());
//            send(CmdsUtils.CMD_MARQUEESYN, mar.toObject(), user);
//        }

        send(CmdsUtils.CMD_ActiveResponseLIST , ActiveService.get(user) , user);
        checkVersionUpdate(user, uinfo, currentChannel, currentVersion);
    }

    //每次登录的时候，检查一下更新，放在最后一步操作
    private void checkVersionUpdate(User user, Map<String , String> uinfo, int currentChannel, int currentVersion) {
        if(currentChannel > 0 && currentVersion > 0){

            String room = uinfo.get("room");
            if(room != null && room.length() > 1){
                return;
            }

            String loginHttp = getParentExtension().getConfigProperties().getProperty("login.httpurl");
            if(StringUtils.isBlank(loginHttp)){
                return;
            }

            String updateRequest = loginHttp + "/Update_list.do?channel="+currentChannel+"&version="+currentVersion;
            try {
                String result = HttpCenter.getInstance().httpGet(updateRequest, "utf-8");
                if (result != null && result.contains("result")) {
                    ISFSObject obj = SFSObject.newFromJsonData(result);
                    if (obj.getInt("result") == RESOURCE_UPDATE) {
                        send(CmdsUtils.SFS_EVENT_UPDATE, obj, user);
                        msgLogger.debug("versionupdate\t" + user.getName() + "\t" + currentChannel + "\t" + currentVersion + "\t" + result);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //FIXME
    private Server getServerIdByRoomId(String roomId) {
        String serverid = RedisManager.getInstance().hGet("roomid." + roomId , "serId");
        Object o = Cache.getInstance().get(CacheKey.Server + "." + serverid);
        if (o != null)
        {
            return(Server)o;
        } else {
            return null;
        }
    }


}
