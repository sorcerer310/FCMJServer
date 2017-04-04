package com.rafo.hall.service;

import com.rafo.chess.common.db.RedisManager;
import com.rafo.hall.vo.ActiveListVO;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2016/11/21.
 */
public class ActiveService {
    public static ISFSObject get(User user){
        ISFSObject respObj = new SFSObject();
        Set<String> keys = RedisManager.getInstance().keys("active.id.*");
        List<String> urls= new ArrayList<String>();
        SFSArray sfsArray = new SFSArray();
        for (String key : keys) {
            java.text.DateFormat format1 = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String time = RedisManager.getInstance().hGet(key,"sendtime");
            Date sDate = null;
            try {
                sDate = format1.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (System.currentTimeMillis() > sDate.getTime()){
                continue;
            }

            String channel = RedisManager.getInstance().hGet(key,"channel");
            if (!RedisManager.getInstance().hGet("uid."+user.getName(),"channel").equals(channel)) {
                continue;
            }

            ActiveListVO vo = new ActiveListVO();
            vo.setId(RedisManager.getInstance().hGet(key,"id"));
            vo.setUrl(RedisManager.getInstance().hGet(key,"content"));

            sfsArray.addSFSObject(vo.toObject());

        }
        respObj.putSFSArray("actives" , sfsArray);
        return respObj;
    }
}
