package com.rafo.hall.vo;

/**
 * Created by YL.
 * Date: 16-10-17
 */
public class Marquee
{
    private int id;
    private String content;
    private int rollTimes;
    private String color;
    private long startTime;
    private long endTime;
    private long createTime;
    private int isSendNow;
    private int isSend;

    public Marquee()
    {

    }
    public Marquee(String content, String color, long endTime, int rollTimes)
    {
        this.content = content;
        this.color = color;
        this.endTime = endTime;
        this.rollTimes = rollTimes;
        this.startTime = System.currentTimeMillis();//定时处理
        this.createTime = System.currentTimeMillis();
        this.isSend = 0;
        this.isSendNow = 0;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public int getRollTimes()
    {
        return rollTimes;
    }

    public void setRollTimes(int rollTimes)
    {
        this.rollTimes = rollTimes;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getIsSendNow()
    {
        return isSendNow;
    }

    public void setIsSendNow(int isSendNow)
    {
        this.isSendNow = isSendNow;
    }

    public int getIsSend()
    {
        return isSend;
    }

    public void setIsSend(int isSend)
    {
        this.isSend = isSend;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public long getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(long createTime)
    {
        this.createTime = createTime;
    }
}
