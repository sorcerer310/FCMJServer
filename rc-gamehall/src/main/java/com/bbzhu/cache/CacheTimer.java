package com.bbzhu.cache;

import com.bbzhu.utils.Dao;
import com.bbzhu.utils.Pojo;
import com.rafo.hall.handlers.LoginHandler;
import com.rafo.hall.model.cache.*;
import com.www.model.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2016/9/28.
 */
public class CacheTimer extends TimerTask{

   // private final Logger logger = LoggerFactory.getLogger("Extensions");
    @Override
    public void run() {
      //  synchronized (LoginHandler.createLock)
      //  {
            ConcurrentHashMap<Object,Object> map = new ConcurrentHashMap<Object,Object>();

            Object cur =  Cache.getInstance().get(CacheKey.curSerIndex);
            if (cur == null) {
                cur = 0;
            }
//            Cache.getInstance().clean();
            List<Pojo> pojos = Dao.getInstance().findAll(new Server());
            int i= 0;
            for (Pojo pojo:pojos) {

                Server server = (Server)pojo;
             //   logger.info("id:" + server.getId());
                map.put(CacheKey.ServerConfig +"."+ server.getId() , server);
                map.put(CacheKey.ServerConfigIndex +"."+ i,server);

                i++;
            }

            map.put(CacheKey.ServerCount  , pojos.size());

            map.put(CacheKey.curSerIndex  , cur);

            Cache.getInstance().SetAll(map);
       // }
    }
}
