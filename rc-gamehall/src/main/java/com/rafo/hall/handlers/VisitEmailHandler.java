package com.rafo.hall.handlers;

import com.rafo.chess.gm.service.EmailService;
import com.rafo.hall.utils.CmdsUtils;
import com.rafo.hall.vo.WCVisitEmailRES;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;

/**
 * Created by Administrator on 2016/9/22.
 */
public class VisitEmailHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject)
    {
        String uid = user.getName();
        WCVisitEmailRES res;
        try
        {
            res = EmailService.getInstance().getEmail(Integer.parseInt(uid));
            SFSExtension sfs = getParentExtension();
            sfs.send(CmdsUtils.CMD_WCVISITEMAIL, res.toSFSObject(), user);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
