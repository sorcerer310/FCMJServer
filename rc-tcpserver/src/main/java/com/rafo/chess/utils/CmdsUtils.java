package com.rafo.chess.utils;


import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class CmdsUtils {

    public static final  String  CMD_MOVE = "move";
    public static final  String  CMD_RESTART= "restart";
    public static final  String  CMD_READY = "ready";
    public static final  String  CMD_MSG = "msg";
    public static final  String  CMD_CREATROOM = "createroom";
    public static final  String  CMD_JOINROOM = "joinroom";
    public static final  String  CMD_DISSOLVEROOM = "dissolveroom";
    public static final  String  CMD_ROUND_RECORD = "round_record";
    public static final  String  CMD_BATTLE_STEP = "battle_step";
    public static final  String  CMD_BATTLE_START = "battle_start";
    public static final  String  CMD_BATTLE_READY = "battle_ready";
    public static final  String  CMD_BATTLE_OFFLINE = "battle_offline";
    public static final  String  CMD_USER_INFO_UPDATE = "userupdate";
    public static final  String  CMD_ROOM_DESTROY = "destoryroom";
    public static final  String  CMD_ROOM_QUIT = "roomquit";
    public static final  String  CMD_ROOM_DESTROY_VOTE_REQ = "vote_req";
    public static final  String  CMD_ROOM_DESTROY_VOTE_RESP = "vote_res";
    public static final  String  SFS_EVENT_ACCOUNT_MODIFY = "SFS_EVENT_ACCOUNT_MODIFY";
    public static final  String SFS_EVENT_CHAT_SYN = "SFS_EVENT_CHAT_SYN";
    public static final  String SFS_EVENT_ACCOUNT_LOGOUT = "SFS_EVENT_ACCOUNT_LOGOUT";
    public static final String SFS_EVENT_UPDATE = "SFS_EVENT_UPDATE";
    public static final String SFS_EVENT_FORCE_DESTORY_ROOM = "SFS_EVENT_FORCE_DESTORY_ROOM";

    public static final String CMD_BANKER_SET_START = "banker_set_start";
    public static final String CMD_BANKER_SET_READY = "banker_set_ready";
    public static final String CMD_PING = "ping";
    
    public static final String SFS_EVENT_MA_START = "SFS_EVENT_MA_START";
    public static final String SFS_EVENT_MA_FINISH = "SFS_EVENT_MA_FINISH";

    public static boolean sendMessage(SFSExtension extension, String cmd, SFSObject sfsObject, String userName){
        try {
            User user = extension.getApi().getUserByName(userName);
            if (user != null) {
                extension.send(cmd, sfsObject, user);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean sendMessage(SFSExtension extension, String cmd, SFSObject sfsObject, User user){
        try {
            if (user != null) {
                extension.send(cmd, sfsObject, user);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}