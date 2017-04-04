package com.rafo.hall.vo;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

/**
 * Created by YL.
 * Date: 16-10-10
 */
public class GameNotice
{
    private int id;
    private String title;
    private String content;
    private long startTime;

    public GameNotice()
    {
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public ISFSObject toObject()
    {
        ISFSObject obj = new SFSObject();
        obj.putUtfString("title", title);
        obj.putUtfString("content", content);
        return obj;
    }

}
