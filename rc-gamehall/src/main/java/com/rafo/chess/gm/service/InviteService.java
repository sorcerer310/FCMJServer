package com.rafo.chess.gm.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.bbzhu.database.DatabaseConn;
import com.rafo.hall.vo.GameEmail;

public class InviteService {
	
	public static final String sqlget="select * from tbl_player where id=";
	public static final String sqlupdate="select * from tbl_player where id=";
	
	public static int getInviteId(int playerId)
	{
		int inviteId=-1;
		
		String sql="select * from tbl_player where id="+playerId;
		
		Connection conn = null;
        Statement ps = null;
        
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
                ps = conn.createStatement();
                ResultSet rs = ps.executeQuery(sql);
                
                while (rs.next())
                {
                    inviteId=rs.getInt("inviteId");
                    if(!isCanUseInvited(inviteId))
                    	inviteId=-1;
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            DatabaseConn.close(null, ps, conn);
        }
		
		return inviteId;
	}
	
	public static int setInviteId(int playerId,int inviteId)
	{
		int status=0;
		
		if(playerId==inviteId)
			return -1;
		
		String sql="update tbl_player set inviteId="+inviteId+" where id="+playerId;
		String sqlp1="select * from tbl_player where id="+playerId;
		String sqlp2="select * from tbl_player where id="+inviteId;
		String sqlp3="select * from tbl_player where inviteId="+inviteId;
//		String sql="select * from tbl_player where inviteId="+inviteId;
		
		Connection conn = null;
		PreparedStatement ps = null;
        
        
        if(!isCanUseInvited(inviteId))
        	return -2;
        if(isPlayerInvited(playerId))
        	return -3;
        if(getInviteId(inviteId)==playerId)
        	return -4;
        
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
            	ps = conn.prepareStatement(sql);
                
            	ps.execute();
            	status=1;
//            	while (rs.next())
//                {
//                    int niid=rs.getInt("inviteId");
//                    if(niid==inviteId)
//                    	status=true;
//                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            DatabaseConn.close(null, ps, conn);
        }
        
        if(status==1)
        	UpdatePlayerStatus(playerId);
		
		return status;
	}
	
	public static boolean isCanUseInvited(int inviteId)
	{
		boolean flag=false;;
		
		String sql="select * from tbl_agentinfo where agent_id="+inviteId;
		
		Connection conn = null;
        Statement ps = null;
        
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
                ps = conn.createStatement();
                ResultSet rs = ps.executeQuery(sql);
                
                while (rs.next())
                {
                	flag=true;
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            DatabaseConn.close(null, ps, conn);
        }
        
		return flag;
	}
	
	public static boolean isPlayerInvited(int playerId)
	{
		boolean flag=false;;
		
		String sql="select * from tbl_player where id="+playerId;
		
		Connection conn = null;
        Statement ps = null;
        
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
                ps = conn.createStatement();
                ResultSet rs = ps.executeQuery(sql);
                
                while (rs.next())
                {
                	int inid=rs.getInt("inviteId");
                	if(isPlayerExsits(inid))
                		flag=true;
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            DatabaseConn.close(null, ps, conn);
        }
        
		return flag;
	}
	
	public static boolean isPlayerExsits(int playerId)
	{
		boolean flag=false;;
		
		String sql="select * from tbl_player where id="+playerId;
		
		Connection conn = null;
        Statement ps = null;
        
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
                ps = conn.createStatement();
                ResultSet rs = ps.executeQuery(sql);
                
                while (rs.next())
                {
                	flag=true;
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            DatabaseConn.close(null, ps, conn);
        }
        
		return flag;
	}
	
	public static int getCardNum(int playerId)
	{
		int cardnum=0;
		
		String sql="select * from tbl_player where id="+playerId;
		
		Connection conn = null;
        Statement ps = null;
        
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
                ps = conn.createStatement();
                ResultSet rs = ps.executeQuery(sql);
                
                while (rs.next())
                {
                	cardnum=rs.getInt("card");
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            DatabaseConn.close(null, ps, conn);
        }
        
		return cardnum;
	}
	
	public static void UpdatePlayerStatus(int playerId)
	{
		String sql="update tbl_player set trans_status=0 where id="+playerId;
        
		Connection conn = null;
		PreparedStatement ps = null;
		
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
            	ps = conn.prepareStatement(sql);
                
            	ps.execute();
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }finally
        {
            DatabaseConn.close(null, ps, conn);
        }
	}
	
	public static void addCardNum(int playerId,int num)
	{
		int cardnum=getCardNum(playerId)+num;
		String sql="update tbl_player set card="+cardnum+" where id="+playerId;
        
		Connection conn = null;
		PreparedStatement ps = null;
		
        try
        {
            conn = DatabaseConn.getInstance().getConnection();
            if (conn != null)
            {
            	ps = conn.prepareStatement(sql);
                
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
}
