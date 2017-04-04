package com.rafo.chess.gm;

import com.rafo.chess.gm.task.NoticeDelayTask;
import com.rafo.hall.core.HallExtension;
import com.rafo.hall.utils.CmdsUtils;
import com.rafo.hall.vo.GameNotice;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by YL.
 * Date: 16-10-13
 */
public class GM_GameNotice extends GMCommand
{
    @Override
    public boolean exec(SFSObject params, SFSExtension sFSExtension)
    {
        HallExtension hallExt = (HallExtension) sFSExtension;
        GameNotice notice = new GameNotice();
        notice.setTitle(params.getUtfString("title"));
        notice.setContent(params.getUtfString("content"));
        int type = params.getInt("type");
        if (type == 0)//实时公告
        {
            //获取大厅内的玩家
            notice.setStartTime(System.currentTimeMillis());
            hallExt.getNoticeService().saveNotice(notice, type);
            List<User> userList = (List<User>) hallExt.getParentZone().getUserList();
            hallExt.send(CmdsUtils.CMD_NOTICE_SYNC, notice.toObject(), userList);
        } else if (type == 1)//定时公告
        {
            long sendTime = params.getLong("sendtime");
            notice.setStartTime(sendTime);
            hallExt.getNoticeService().saveNotice(notice, type);
            //保存公告
            SmartFoxServer sfs = SmartFoxServer.getInstance();
            NoticeDelayTask voteDestroy = new NoticeDelayTask(sFSExtension);
            long delayTime = sendTime - System.currentTimeMillis();
            int delaySecond = (int) (delayTime / 1000);
            sfs.getTaskScheduler().schedule(voteDestroy, delaySecond, TimeUnit.SECONDS);
        }
        return true;
    }
}
