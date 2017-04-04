package com.rafo.hall.service;

import com.bbzhu.database.DatabaseConn;
import com.rafo.chess.common.db.MySQLManager;
import com.rafo.chess.exception.PersistException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Administrator on 2016/9/19.
 */
public class LoginService
{

    public static void updateForbidTime(int uid, String forbidTime) throws PersistException
    {
        String sql = "UPDATE tbl_player SET forbidTime='" + forbidTime+ "' WHERE id=" + uid;

        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = DatabaseConn.getInstance().getConnection(0);
            if (conn != null)
            {
                ps = conn.prepareStatement(sql);
                ps.execute();
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            throw new PersistException("mysql error" + sql);
        } finally
        {
            MySQLManager.close(null, ps, conn);
        }

    }

}
