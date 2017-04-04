package com.hall.test;

import com.bbzhu.database.DatabaseConn;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.gm.service.EmailService;

import java.io.IOException;

/**
 * Created by Administrator on 2016/10/11.
 */
public class MailTest {
    public static void main(String[] args) throws IOException {
        DatabaseConn.getInstance().addDataSource(0,"com.mysql.jdbc.Driver","jdbc:mysql://localhost:3306/login?characterEncoding=utf-8&amp;autoReconnect=true","root","root");
        RedisManager.getInstance().init("");
        EmailService.getInstance().addEmailToPlayer(10,"welcome");
    }
}
