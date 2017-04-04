package com.rafo.chess.gm.service;

import com.bbzhu.database.DatabaseConn;
import com.rafo.hall.core.HallExtension;
import com.rafo.hall.utils.HallRdb;
import com.rafo.hall.vo.Marquee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YL.
 * Date: 16-10-17
 */
public class MarqueeService
{
    private final Logger logger = LoggerFactory.getLogger(MarqueeService.class);
    private static MarqueeService instance = new MarqueeService();
    private HallExtension hallExt;
    public static final int MARQUEE_CURRENT = 0;
    public static final int MARQUEE_DELAY = 1;

    public MarqueeService()
    {

    }

    public MarqueeService(HallExtension extension)
    {
        this.hallExt = extension;
        this.loadDBToRedis();
    }

    /**
     * 从mysql获取数据
     */
    private void loadDBToRedis()
    {
        List<Marquee> list = getMarqueeFromMysql();
        HallRdb.putMarquee(list);
    }

    public static MarqueeService getInstance()
    {
        return instance;
    }

    /**
     * 及时同步新公告
     *
     * @return
     */
    public void saveMarquee(Marquee marquee)
    {
        List<Marquee> list = new ArrayList<>();
        list.add(marquee);
        HallRdb.putMarquee(list);
        saveMarquee2Mysql(marquee);
    }

    /**
     * 获取要播放的跑马灯
     *
     * @return
     */
    public List<Marquee> getMarqueeList()
    {
        return HallRdb.getMarqueeList();
    }


    /**
     * 持久化到mysql
     *
     * @param marquee
     */
    private void saveMarquee2Mysql(Marquee marquee)
    {
        String sql = "insert into tbl_marquee (content, color, roll_times, start_time, end_time, create_time, isSendNow, isSend) " +
                "values (?,?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = DatabaseConn.getInstance().getConnection(0);
            if (conn != null)
            {
                ps = conn.prepareStatement(sql);
                ps.setString(1, marquee.getContent());
                ps.setString(2, marquee.getColor());
                ps.setInt(3, marquee.getRollTimes());
                ps.setLong(4, marquee.getStartTime());
                ps.setLong(5, marquee.getEndTime());
                ps.setLong(6, marquee.getCreateTime());
                ps.setInt(7, marquee.getIsSendNow());
                ps.setInt(8, marquee.getIsSend());
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
     * 从数据库中获取marquee
     *
     * @return
     */
    private List<Marquee> getMarqueeFromMysql()
    {
        List<Marquee> list = new ArrayList<>();
        long time = System.currentTimeMillis();
        StringBuilder sql = new StringBuilder();
        sql.append("select * from tbl_marquee where ");
        sql.append(time);
        sql.append(" >= startTime and");
        sql.append(time);
        sql.append(" < endTime");
        Connection conn = null;
        Statement ps = null;
        Marquee marquee;
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
                ps = conn.createStatement();
                ResultSet rs = ps.executeQuery(sql.toString());
                while (rs.next())
                {
                    marquee = new Marquee();
                    marquee.setId(rs.getInt("id"));
                    marquee.setContent(rs.getString("content"));
                    marquee.setColor(rs.getString("color"));
                    marquee.setRollTimes(rs.getInt("roll_times"));
                    marquee.setStartTime(rs.getLong("start_time"));
                    marquee.setEndTime(rs.getLong("end_time"));
                    marquee.setCreateTime(rs.getLong("create_time"));
                    marquee.setIsSend(rs.getInt("isSend"));
                    marquee.setIsSendNow(rs.getInt("isSendNow"));
                    list.add(marquee);
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