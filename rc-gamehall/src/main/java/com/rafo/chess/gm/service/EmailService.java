package com.rafo.chess.gm.service;

import com.bbzhu.database.DatabaseConn;
import com.rafo.hall.common.GlobalConstants;
import com.rafo.hall.core.HallExtension;
import com.rafo.hall.utils.HallRdb;
import com.rafo.hall.utils.SerializeUtil;
import com.rafo.hall.utils.TimeUtils;
import com.rafo.hall.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/9/23.
 */
public class EmailService
{
    private static final int RESERVED_EMAIL_COUNT = 10;
    public static final int HAS_NEW_EMAIL = 1;//有新邮件
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static EmailService instance = new EmailService();
    private HallExtension hallExtension;


    public EmailService()
    {

    }

    public EmailService(HallExtension extension)
    {
        this.hallExtension = extension;
    }
    public static EmailService getInstance()
    {
        return instance;
    }

    public WCHaveNewEmailSynRES sendNewNumber(int num)
    {
        WCHaveNewEmailSynRES res = new WCHaveNewEmailSynRES();
        res.setNewNumber(num);
        return res;
    }


    //获得该玩家是否存在新邮件
    public boolean isHasNewEmail(int playerId)
    {
        return HallRdb.isHasNewEmail(playerId);
    }

    /**
     * 获取邮件
     *
     * @param playerId
     * @return
     */
    public WCVisitEmailRES getEmail(int playerId) throws Exception
    {
        GameEmail email = HallRdb.getEmail(playerId);
        WCVisitEmailRES emailRes = new WCVisitEmailRES();
        boolean isInsert = false;
        Date date = new Date();
        logger.debug("WCVisitEmailRES getID debug: playerId = {}", playerId);
        if (email == null)
        {
            email = getEmailFromMysql(playerId);
            if (email == null)
            {
                isInsert = true;
                email = new GameEmail();
                email.setPlayer_id(playerId);
                email.setLast_visit_time(date);
                email.setEmail_num(0);
                email.setEmail_new(0);
                try
                {
                    RESEmailList resEmailList = new RESEmailList();
                    email.setEmail_list(SerializeUtil.serializeObject(resEmailList));
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                logger.info("add new player to EmailService! playerId = {}", playerId);
            }
            emailRes.setResult(GlobalConstants.VISIT_NO_DATA);
        }
        if (!isInsert)
        {
            email.setEmail_new(0);//新消息提示
            email.setLast_visit_time(date);
            //在获取邮件之前先清理一下过期的邮件,保留10条
            email = clearExpiredEmail(email, RESERVED_EMAIL_COUNT);
            emailRes.setResult(GlobalConstants.VISIT_SUCCESS);
        }
        HallRdb.putEmail(email);
        saveEmail2Mysql(email);
        emailRes.setLastLoginTime(TimeUtils.parseDateToString(date));
        emailRes.setEmailList((RESEmailList) SerializeUtil.deserializeObject(email.getEmail_list()));
        return emailRes;
    }

    /**
     * 缓存中无数据，从mysql查询
     *
     * @param playerId
     * @return
     */
    private GameEmail getEmailFromMysql(int playerId)
    {
        String sql = "select * from tbl_email where player_id = " + playerId;
        Connection conn = null;
        Statement ps = null;
        GameEmail gameEmail = null;
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
                ps = conn.createStatement();
                ResultSet rs = ps.executeQuery(sql);
                while (rs.next())
                {
                    gameEmail = new GameEmail();
                    gameEmail.setId(rs.getInt("id"));
                    gameEmail.setPlayer_id(rs.getInt("player_id"));
                    gameEmail.setEmail_new(rs.getInt("email_new"));
                    gameEmail.setEmail_num(rs.getInt("email_num"));
                    gameEmail.setEmail_list(rs.getBytes("email_list"));
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            DatabaseConn.close(null, ps, conn);
        }
        return gameEmail;
    }

    /**
     * 清理过期邮件
     *
     * @param email
     * @param num
     */
    private GameEmail clearExpiredEmail(GameEmail email, int num) throws Exception
    {
        if (email.getEmail_num() > num)
        {
            if (email.getEmail_list() != null && email.getEmail_list().length != 0)
            {
                RESEmailList resEmail = (RESEmailList) SerializeUtil.deserializeObject(email.getEmail_list());
                List<RESEmailDataPROTO> proto = resEmail.getEmailDate();
                List<RESEmailDataPROTO> holdList = new ArrayList<>();
                for (int i = num; i > 0; i--)
                {
                    RESEmailDataPROTO holdData = proto.get(proto.size() - i);
                    holdList.add(holdData);
                }
                RESEmailList holdEmail = new RESEmailList();
                holdEmail.setEmailDate(holdList);
                email.setEmail_list(SerializeUtil.serializeObject(holdEmail));
                email.setEmail_num(num);
            }
        }
        return email;
    }

    /**
     * 保存email to mysql
     *
     * @param email
     * @throws Exception
     */
    private void saveEmail2Mysql(GameEmail email) throws Exception
    {

        String sql = "insert into tbl_email (player_id,email_new,email_num,last_visit_time,email_list) values (?,?,?,?,?)"
                + " ON DUPLICATE KEY UPDATE  email_new=VALUES (email_new),email_num=VALUES (email_num)," +
                "last_visit_time=VALUES (last_visit_time),email_list=VALUES (email_list)";
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = DatabaseConn.getInstance().getConnection(0);
            if (conn != null)
            {
                ps = conn.prepareStatement(sql);
                ps.setInt(1, email.getPlayer_id());
                ps.setInt(2, email.getEmail_new());
                ps.setInt(3, email.getEmail_num());
                ps.setTimestamp(4, new java.sql.Timestamp(email.getLast_visit_time().getTime()));
                ps.setObject(5, email.getEmail_list());
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
     * 添加一封邮件
     *
     * @param playerId
     * @param emailStr
     * @return
     */
    public int addEmailToPlayer(int playerId, String emailStr)
    {
        Date date = new Date();
        try
        {
            GameEmail gameEmail = HallRdb.getEmail(playerId);
            if (gameEmail == null)
            {
                //玩家还未初始化邮件系统
                gameEmail = new GameEmail();
                gameEmail.setPlayer_id(playerId);
                gameEmail.setLast_visit_time(date);
                gameEmail.setEmail_num(0);
                gameEmail.setEmail_new(0);
                RESEmailList resEmailList = new RESEmailList();
                gameEmail.setEmail_list(SerializeUtil.serializeObject(resEmailList));
            }
            RESEmailDataPROTO dataPROTO = new RESEmailDataPROTO();
            dataPROTO.setEmailDate(TimeUtils.parseDateToString(new Date()));
            dataPROTO.setEmailContent(emailStr);
            RESEmailList emailList = (RESEmailList) SerializeUtil.deserializeObject(gameEmail.getEmail_list());
            List<RESEmailDataPROTO> protoList = emailList.getEmailDate();
            protoList.add(dataPROTO);
            gameEmail.setEmail_list(SerializeUtil.serializeObject(emailList));
            gameEmail.setEmail_new(HAS_NEW_EMAIL);
            gameEmail.setEmail_num(gameEmail.getEmail_num() + 1);
            HallRdb.putEmail(gameEmail);
            saveEmail2Mysql(gameEmail);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        logger.info("add a new email ! playerId = {}", playerId);
        return 1;
    }

}