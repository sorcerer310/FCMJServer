package com.rafo.hall.vo;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/22.
 */
public class RESEmailList implements Serializable
{
    private static final long serialVersionUID = -578133382510756623L;


    private List<RESEmailDataPROTO> emailDate = new ArrayList<>();

    public List<RESEmailDataPROTO> getEmailDate()
    {
        return emailDate;
    }

    public void setEmailDate(List<RESEmailDataPROTO> emailDate)
    {
        this.emailDate = emailDate;
    }

    public SFSObject toObject()
    {
        SFSObject obj = new SFSObject();

        SFSArray arr = new SFSArray();
        if (emailDate.size() > 0)
        {
            for (RESEmailDataPROTO resEmailDataPROTO : emailDate)
            {
                arr.addSFSObject(resEmailDataPROTO.toObject());
            }
        }
        obj.putSFSArray("emailDate", arr);
        return obj;
    }
}
