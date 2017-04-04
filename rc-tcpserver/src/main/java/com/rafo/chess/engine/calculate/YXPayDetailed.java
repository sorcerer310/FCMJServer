package com.rafo.chess.engine.calculate;

import java.util.Map;

/**
 * 永修使用的计分对象
 * 每个YXPayDetailed对象表示一个赢家胡牌的所有分数。
 * 如果有一炮多响的情况下，会生成多个YXPayDetailed数据对象
 * Created by fengchong on 2017/3/20.
 */
public class YXPayDetailed {
    public int subTypeScore = 0;                                                                                        //牌型分
    public int attachScore = 0;                                                                                         //附加得分项
    public int jiamaScore = 0;																							//房间加码分
    public int jiesuanBenfengScore = 0;																					//结算*本风 赢家加的分
    public Map<Integer,Integer> jiesuanBenfengDelScore = null;															//结算*本风 输家减的分
    public Map<Integer, Integer> gangScore = null;                                                              		//杠+的分
    public Map<Integer, Integer> gangDelScore = null;                                                           		//杠-的分，除了杠的人其他三家减
    public int toPlayer = 0;                                                                                            //赢得玩家id
    public int[] fromPlayers = null;                                                                                    //输的玩家id
    public int addScoreTotal = 0;																						//赢家分数汇总
    public Map<Integer,Integer> lostScoreTotal = null;																	//输家分数汇总
    public PayDetailed pd = null;
    public YXPayDetailed(PayDetailed p){this.pd = p;this.pd.setYxpd(this);}
}
