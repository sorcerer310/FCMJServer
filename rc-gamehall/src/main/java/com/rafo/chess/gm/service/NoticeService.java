package com.rafo.chess.gm.service;

import com.bbzhu.database.DatabaseConn;
import com.rafo.hall.core.HallExtension;
import com.rafo.hall.utils.HallRdb;
import com.rafo.hall.vo.GameNotice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YL.
 * Date: 16-10-10
 */
public class NoticeService
{
    private final Logger logger = LoggerFactory.getLogger(NoticeService.class);
    private static NoticeService instance = new NoticeService();
    public HallExtension hallExt;
    public static final int NOTICE_CURRENT = 0;
    public static final int NOTICE_DELAY = 1;

    public NoticeService()
    {

    }

    public NoticeService(HallExtension extension)
    {
        this.hallExt = extension;
    }

    public static NoticeService getInstance()
    {
        return instance;
    }

    /**
     * 保存新公告
     *
     * @return
     */
    public void saveNotice(GameNotice notice, int type)
    {
        if(type == 0)//实时公告
        {
            HallRdb.putCurrentNotice(notice);
        }else if(type == 1)
        {
            List<GameNotice> list = new ArrayList<>();
            list.add(notice);
            HallRdb.putDelayNotice(list);
        }
        saveNotice2Mysql(notice);
    }

    /**
     * 获取一条最新公告
     *
     * @return
     */
    public GameNotice getGameNotice()
    {
        GameNotice notice = HallRdb.getCurrentNotice();
        if (notice == null)
        {
            List<GameNotice> list = getNoticeFromMysql(NOTICE_CURRENT);
            if (list != null && list.size() > 0) {
                notice = list.get(0);
                HallRdb.putCurrentNotice(notice);
                HallRdb.putDelayNotice(getNoticeFromMysql(NOTICE_DELAY));
            }

        }
        return notice;
    }


    /**
     * 持久化到mysql
     *
     * @param notice
     */
    private void saveNotice2Mysql(GameNotice notice)
    {
        String sql = "insert into tbl_notice (title,content,startTime) values (?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = DatabaseConn.getInstance().getConnection(0);
            if (conn != null)
            {
                ps = conn.prepareStatement(sql);
                ps.setString(1, notice.getTitle());
                ps.setString(2, notice.getContent());
                ps.setLong(3, notice.getStartTime());
                ps.execute();
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            DatabaseConn.close(null, ps, conn);
        }
    }


    /**
     * 从数据库中获取notice
     *
     * @return
     */
    private List<GameNotice> getNoticeFromMysql(int type)
    {
        List<GameNotice> list = new ArrayList<>();
        long time = System.currentTimeMillis();
        StringBuilder sql = new StringBuilder();
        sql.append("select * from tbl_notice where ");
        sql.append(time);
        if (type == NOTICE_CURRENT)
        {
            sql.append(" < startTime order by startTime desc limit 1");
        } else
        {
            sql.append(" > startTime");
        }
        Connection conn = null;
        Statement ps = null;
        GameNotice notice;
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
                ps = conn.createStatement();
                ResultSet rs = ps.executeQuery(sql.toString());
                while (rs.next())
                {
                    notice = new GameNotice();
                    notice.setId(rs.getInt("id"));
                    notice.setTitle(rs.getString("title"));
                    notice.setContent(rs.getString("content"));
                    notice.setStartTime(rs.getLong("startTime"));
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            DatabaseConn.close(null, ps, conn);
        }
        return list;
    }

}