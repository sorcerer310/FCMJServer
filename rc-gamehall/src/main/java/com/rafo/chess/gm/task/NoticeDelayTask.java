package com.rafo.chess.gm.task;

import com.rafo.hall.core.HallExtension;
import com.rafo.hall.utils.CmdsUtils;
import com.rafo.hall.utils.HallRdb;
import com.rafo.hall.vo.GameNotice;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NoticeDelayTask implements Runnable
{

    private SFSExtension extension;
    private final Logger logger = LoggerFactory.getLogger("NoticeDelayTask");

    public NoticeDelayTask(SFSExtension zoneExt)
    {
        this.extension = zoneExt;
    }

    @Override
    public void run()
    {
        try
        {
            logger.debug("------task GameNoticeSend, run.");
            //推送给所有在大厅内的玩家
            HallExtension hallExt = (HallExtension) extension;
            List<User> userList = (List<User>) hallExt.getParentZone().getUserList();
            GameNotice notice = HallRdb.getLastDelayNotice();
            extension.send(CmdsUtils.CMD_NOTICE_SYNC, notice.toObject(), userList);

        } catch (Exception e)
        {
            logger.debug("task error!!!!" + e.getMessage());
            System.out.println(e.getMessage());
        }

    }


}
