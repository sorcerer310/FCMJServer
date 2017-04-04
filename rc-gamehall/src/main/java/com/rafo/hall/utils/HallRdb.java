package com.rafo.hall.utils;

import com.bbzhu.database.DatabaseConn;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.gm.service.EmailService;
import com.rafo.hall.vo.GameEmail;
import com.rafo.hall.vo.GameNotice;
import com.rafo.hall.vo.Marquee;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by YL.
 * Date: 16-9-28
 */
public class HallRdb
{
    private static final String REDIS_EMAIL_KEY = "email.";
    public static final String REDIS_PLAYER_EMAIL_KEY = "player_email.";
    public static final String REDIS_NOTICE_CURRENT_KEY = "notice.current";
    public static final String REDIS_NOTICE_DELAY_KEY = "notice.delay.";
    private static final String REDIS_MARQUEE_KEY = "marquee.";

    public static void putEmail(GameEmail email) throws UnsupportedEncodingException
    {
        String key = REDIS_EMAIL_KEY + email.getPlayer_id();
        Map<String, String> emailMap = new HashMap<>();
        emailMap.put("id", String.valueOf(email.getId()));
        emailMap.put("player_id", String.valueOf(email.getPlayer_id()));
        emailMap.put("email_new", String.valueOf(email.getEmail_new()));
        emailMap.put("email_num", String.valueOf(email.getEmail_num()));
        emailMap.put("last_visit_time", TimeUtils.parseDateToString(email.getLast_visit_time()));
        String temp = Base64.encodeBase64String(email.getEmail_list());
        emailMap.put(new String("email_list".getBytes()), temp);
        RedisManager.getInstance().hMSet(key, emailMap);

    }


    public static GameEmail getEmail(int playerUid) throws IOException
    {
        String key = REDIS_EMAIL_KEY + playerUid;
        Map<String, String> emailMap = RedisManager.getInstance().hMGetAll(key);
        GameEmail email = null;
        if (emailMap.size() != 0)
        {
            email = new GameEmail();
            email.setId(Integer.parseInt(emailMap.get("id")));
            email.setPlayer_id(Integer.parseInt(emailMap.get("player_id")));
            email.setEmail_new(Integer.parseInt(emailMap.get("email_new")));
            email.setEmail_num(Integer.parseInt(emailMap.get("email_num")));
            email.setLast_visit_time(TimeUtils.parseStringToTime(emailMap.get("last_visit_time")));
            byte[] bt = Base64.decodeBase64(emailMap.get("email_list"));
            email.setEmail_list(bt);
        }
        return email;
    }

    /**
     * 查询是否存在新邮件
     *
     * @param playerId
     * @return
     */
    public static boolean isHasNewEmail(int playerId)
    {
        String key = REDIS_EMAIL_KEY + playerId;
        List<String> new_email = RedisManager.getInstance().hMGet(key, "email_new");
        String s = new_email.get(0);
        return s.equals(String.valueOf(EmailService.HAS_NEW_EMAIL));
    }

    /**
     * 获取系统公告
     *
     * @return
     */
    public static GameNotice getCurrentNotice()
    {
        Map<String, String> noticeMap = RedisManager.getInstance().hMGetAll(REDIS_NOTICE_CURRENT_KEY);
        GameNotice notice = null;
        if (noticeMap.size() != 0)
        {
            notice = new GameNotice();
            notice.setTitle(noticeMap.get("title"));
            notice.setContent(noticeMap.get("content"));
        }
        return notice;
    }

    /**
     * 缓存当前公告
     *
     * @param notice
     */
    public static void putCurrentNotice(GameNotice notice)
    {
        Map<String, String> noticeMap = new HashMap<>();
        noticeMap.put("title", notice.getTitle());
        noticeMap.put("content", notice.getContent());
        RedisManager.getInstance().hMSet(REDIS_NOTICE_CURRENT_KEY, noticeMap);
    }

    /**
     * 缓存定时公告
     *
     * @param list
     */
    public static void putDelayNotice(List<GameNotice> list)
    {
        for (GameNotice notice : list)
        {
            Map<String, String> noticeMap = new HashMap<>();
            noticeMap.put("title", notice.getTitle());
            noticeMap.put("content", notice.getContent());
            RedisManager.getInstance().hMSet(REDIS_NOTICE_DELAY_KEY + notice.getStartTime(), noticeMap);
        }
    }

    /**
     * 获取将要执行的最新公告
     *
     * @return
     */
    public static GameNotice getLastDelayNotice()
    {
        Set<String> keys = RedisManager.getInstance().keys(REDIS_NOTICE_DELAY_KEY + "*");
        List<Long> delays = new ArrayList<>();
        for (String key : keys)
        {
            String time = key.split("\\.")[2];
            delays.add(Long.parseLong(time));
        }
        Collections.sort(delays);
        String noticeKey = REDIS_NOTICE_DELAY_KEY + delays.get(0);
        Map<String, String> map = RedisManager.getInstance().hMGetAll(noticeKey);
        GameNotice notice = null;
        if (map != null && map.size() != 0)
        {
            notice = new GameNotice();
            notice.setTitle(map.get("title"));
            notice.setContent(map.get("content"));
            //更新当前公告
            putCurrentNotice(notice);
            //删除redis已取出的定时公告
            RedisManager.getInstance().del(noticeKey);
        }
        return notice;
    }

    /**
     * redis中获取marquee
     *
     * @return
     */
    public static List<Marquee> getMarqueeList()
    {
        List<Marquee> list = new ArrayList<>();
        Set<String> keys = RedisManager.getInstance().keys(REDIS_MARQUEE_KEY + "*");
        for (String key : keys)
        {
            String endTimeStr = key.split("\\.")[1];
            long endTime = Long.parseLong(endTimeStr);
            if (endTime > System.currentTimeMillis())
            {
                Map<String, String> map = RedisManager.getInstance().hMGetAll(REDIS_MARQUEE_KEY + endTime);
                Marquee marquee = new Marquee();
                marquee.setContent(map.get("content"));
                marquee.setColor(map.get("color"));
                marquee.setRollTimes(Integer.parseInt(map.get("rollTimes")));
                marquee.setEndTime(Long.parseLong(map.get("endTime")));
                marquee.setStartTime(Long.parseLong(map.get("startTime")));
                list.add(marquee);
            }
        }
        return list;
    }

    public static List<Marquee> getMarqueeFromDB()
    {
        List<Marquee> list = new ArrayList<>();
        long time = System.currentTimeMillis();
        StringBuilder sql = new StringBuilder();
        sql.append("select * from lightsend");
//        sql.append(time);
//        sql.append(" >= sendtime and ");
//        sql.append(time);
//        sql.append(" < endtime");
        Connection conn = null;
        Statement ps = null;
        Marquee marquee;
        try
        {
            conn = DatabaseConn.getInstance().getConnection(1);
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
                    marquee.setRollTimes(rs.getInt("count"));
                    marquee.setStartTime(rs.getLong("sendtime"));
                    marquee.setEndTime(rs.getLong("endtime"));
                    marquee.setCreateTime(rs.getLong("addtime"));


//                    marquee.setIsSend(rs.getInt("isSend"));
//                    marquee.setIsSendNow(rs.getInt("isSendNow"));
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

    /**
     * put redis
     *
     * @param list
     */
    public static void putMarquee(List<Marquee> list)
    {
        for (Marquee marquee : list)
        {
            Map<String, String> map = new HashMap<>();
            map.put("content", marquee.getContent());
            map.put("color", marquee.getColor());
            map.put("startTime", String.valueOf(marquee.getStartTime()));
            map.put("endTime", String.valueOf(marquee.getEndTime()));
            map.put("rollTimes", String.valueOf(marquee.getRollTimes()));
            long endTime = marquee.getEndTime();
            String marqueeKey = REDIS_MARQUEE_KEY + endTime;
            RedisManager.getInstance().hMSet(marqueeKey, map);
        }
    }
}


