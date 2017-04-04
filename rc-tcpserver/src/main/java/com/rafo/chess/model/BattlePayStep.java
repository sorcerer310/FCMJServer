package com.rafo.chess.model;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetailed;
import com.rafo.chess.engine.calculate.YXPayDetailed;
import com.rafo.chess.engine.gameModel.IPlayer;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.RoomInstance;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.model.battle.BattleScore;
import com.rafo.chess.utils.Constants;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 汇总每一步的得分数据
 * 因为每一步产生的得分可能会有多种明目
 * 如：暗杠 一色 飘 扣听
 */
public class BattlePayStep {

    private int step;
    private int toUid;
    private int type;
    private int baseRate = 1;

    private Map<Integer, List<ScoreDetail>> mutlipleScoreDetail = new HashMap<>();                                      //用户支付的乘法的番数明细
    private Map<Integer, List<ScoreDetail>> addScoreDetail = new HashMap<>();                                           //用户支付的加法的番数明细

    private Map<Integer, Integer> multipleRateTotal = new HashMap<>();                                                  //用户支付的乘法的番数汇总
    private Map<Integer, Integer> addRateTotal = new HashMap<>();                                                       //用户支付的加法的番数汇总

    private int gainTotal;                                                                                              //得分汇总
    private Map<Integer, Integer> lostTotal = new HashMap<>();                                                          //给分汇总

    private BattleScore battleScore = new BattleScore();

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getToUid() {
        return toUid;
    }

    public void setToUid(int toUid) {
        this.toUid = toUid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(int baseRate) {
        this.baseRate = baseRate;
    }

    public Map<Integer, List<ScoreDetail>> getMutlipleScoreDetail() {
        return mutlipleScoreDetail;
    }

    public Map<Integer, List<ScoreDetail>> getAddScoreDetail() {
        return addScoreDetail;
    }

    public int getGainTotal() {
        return gainTotal;
    }

    public Map<Integer, Integer> getLostTotal() {
        return lostTotal;
    }

    public BattleScore getBattleScore() {
        return battleScore;
    }

    /**以下为永修麻将要结算的分数**/
    private YXPayDetailed yxpd ;
    /**以上为永修麻将要结算的分数**/

    /**
     * 增加乘法成绩详情
     * @param fromIds
     * @param subType
     * @param rate
     * @param canMerge
     */
    public void addMultipleScoreDetail(int[] fromIds, int subType, int rate, boolean canMerge){
        if(rate == 0){
            return;
        }
        for(int uid : fromIds) {
            ScoreDetail detail = new ScoreDetail();
            detail.setUid(uid);
            detail.setSubType(subType);
            detail.setRate(rate);
            detail.setCanMerege(canMerge);

            List<ScoreDetail> details = mutlipleScoreDetail.get(uid);
            if(details == null){
                details = new ArrayList<>();
                mutlipleScoreDetail.put(uid, details);
            }

            if( !detail.isCanMerege() || !details.contains(detail)){
                details.add(detail);
                addMultipleRate(uid, rate);
            }
        }
    }

    /**
     * 汇总乘法的分数
     * @param uid
     * @param rate
     */
    private void addMultipleRate(int uid, int rate){
        Integer multiRate = multipleRateTotal.get(uid);
        if(multiRate == null){
            multipleRateTotal.put(uid, rate);
        }else{
        	// 加码的这么处理
        	Integer huRate = multipleRateTotal.get(uid);
            multipleRateTotal.put(uid, huRate + rate/**multiRate*/);
        }
    }

    /**
     * 对成绩进行加法计算，为了计算所得的番数
     * @param fromIds
     * @param subType
     * @param rate
     */
    public void addAddScoreDetail(int[] fromIds, int subType, int rate){
        for(int uid : fromIds) {
            ScoreDetail detail = new ScoreDetail();
            detail.setUid(uid);
            detail.setSubType(subType);
            detail.setRate(rate);

            List<ScoreDetail> details = addScoreDetail.get(uid);
            if(details == null){
                details = new ArrayList<>();
                addScoreDetail.put(uid, details);
            }

            details.add(detail);

            //汇总加法的番数
            Integer addRate = addRateTotal.get(uid);
            if(addRate == null){
                addRateTotal.put(uid, rate);
            }else{
                addRateTotal.put(uid, rate+addRate);
            }
        }
    }

    /**
     * 每一步产生的结算,永修已废弃，不使用
     * @param bankerId
     * @param redRate
     */
    public void calculate(int bankerId, int redRate){
        //uid gain //uid lost
        int rate = 1;

        rate *= redRate;

        if(bankerId == toUid){
            //rate *= 2;
        }

        int gainScore = 0;

        for(Map.Entry<Integer,Integer> multiRate : multipleRateTotal.entrySet()){
            int fromUid = multiRate.getKey();
            int lostScore = multiRate.getValue();
            lostScore *= rate;

            if(fromUid == bankerId){
                //lostScore *= 2;
            }
            gainScore += lostScore;

            Integer score = lostTotal.get(fromUid);
            if(score == null){
                lostTotal.put(fromUid, lostScore);
            }else{
                lostTotal.put(fromUid, score + lostScore);
            }
        }

        this.gainTotal = gainScore;
    }

    /**
     * 永修结算成绩录入
     * @param fuid                      输家id数组，可能有多个输家
     * @param tp                        赢家id
     * @param subTypeScore              牌型分
     * @param attachScore               附加分
     * @param jiamaScore                房间加码分
     * @param jiesuanBenfengScore       结算*本风分赢家加分
     * @param jiesuanBenfengDelScore    结算*本风分输家减分
     * @param gangScore                 杠加分
     * @param gangDelScore              杠减分
     */
    public void addYXScoreDetail(int[] fuid, int tp,int subTypeScore, int attachScore, int jiamaScore
            , int jiesuanBenfengScore, Map<Integer,Integer> jiesuanBenfengDelScore
            , Map<Integer,Integer> gangScore, Map<Integer,Integer> gangDelScore, PayDetailed pd){
        yxpd = pd.getYxpd();
        yxpd.toPlayer = tp;
        yxpd.fromPlayers = fuid;
        yxpd.subTypeScore = subTypeScore;
        yxpd.attachScore = attachScore;
        yxpd.jiamaScore = jiamaScore;
        yxpd.jiesuanBenfengScore = jiesuanBenfengScore;
        yxpd.jiesuanBenfengDelScore = jiesuanBenfengDelScore;
        yxpd.gangScore = gangScore;
        yxpd.gangDelScore = gangDelScore;
    }

    /**
     * 永修使用计算最后的总结果
     * @param room  房间对象
     */
    public void calculate(RoomInstance room,Calculator calculator ){
        ArrayList<IPlayer> allPlayer = room.getAllPlayer();

        //1:赢家加的分
        int addscore = (yxpd.subTypeScore + yxpd.attachScore + yxpd.jiamaScore) * yxpd.fromPlayers.length + yxpd.jiesuanBenfengScore
                + gangScore(calculator,yxpd.gangScore.get(toUid)) - gangScore(calculator,yxpd.gangDelScore.get(toUid));
        //此处设置huPoint分
        calculator.getUserBattleBalances().get(yxpd.pd.getToUid()).addHuPoint(
                (yxpd.subTypeScore + yxpd.attachScore + yxpd.jiamaScore) * yxpd.fromPlayers.length
                + yxpd.jiesuanBenfengScore);

        //2:输家减的分
        Map<Integer, Integer> lostScore = new HashMap<>();
        //循环所有玩家，如果有输家按输家分减分，如果为其他家，只减杠分与本风分
        for (IPlayer allp : allPlayer) {
            //输家 点炮的人，如果自摸为三家
            if(ArrayUtils.contains(yxpd.fromPlayers,allp.getUid())){
                //输家合计分:-(牌型分 + 附加得分项 + 结算本风减分 +加码分 + 他人杠减分 - 自己杠加分)
                int ls = yxpd.subTypeScore + yxpd.attachScore + yxpd.jiesuanBenfengDelScore.get(allp.getUid()) + yxpd.jiamaScore
                        + gangScore(calculator,yxpd.gangDelScore.get(allp.getUid())) - gangScore(calculator,yxpd.gangScore.get(allp.getUid()));
                calculator.addCardBalance(allp.getUid(), toUid, 0, -ls, yxpd.pd);
                //此处设置huPoint分。为输的人 增加胡分 包括：-(牌型分 + 附加得分项 + 结算本风减分 + 加码分)
                calculator.getUserBattleBalances().get(allp.getUid()).addHuPoint(
                        -(yxpd.subTypeScore + yxpd.attachScore + yxpd.jiesuanBenfengDelScore.get(allp.getUid()) + yxpd.jiamaScore));

                lostScore.put(allp.getUid(), ls);
            }
            //未点炮的人
            else if (allp.getUid() != yxpd.toPlayer) {
                //未点炮人合计分:只计算杠分
                int ls = gangScore(calculator,yxpd.gangDelScore.get(allp.getUid())) - gangScore(calculator,yxpd.gangScore.get(allp.getUid()));
                calculator.addCardBalance(allp.getUid(), yxpd.toPlayer, 0, -ls, yxpd.pd);
                //此处设置huPoint分 未点炮的人胡分为0分
                calculator.getUserBattleBalances().get(allp.getUid()).addHuPoint(0);
                lostScore.put(allp.getUid(), ls);
            }
        }

        //最后设置计算过杠分，一炮多响时计算下一家胡牌再不计算杠分
        calculator.setComputedGang(true);

        //增加附加加分项目名称
//        for (Integer hat : ((MJPlayer)room.getPlayerById(yxpd.toPlayer)).getHuAttachType()) {
//            calculator.getUserBattleBalances().get(yxpd.toPlayer).getHutype().add(hat);
//        }


        //得分汇总
        this.gainTotal = addscore;

        //直接填充失分汇总
        for(Map.Entry<Integer,Integer> lst:lostScore.entrySet()){
            int fromUid = lst.getKey();
            lostTotal.put(fromUid,lostScore.get(fromUid));
        }
    }

    public void toBattleScore(RoomInstance room){
        /**
         * 结算界面
         * 得分明目 得分
         *   --得分方位 得分
         */
        //getType()保存的是和的类型
        battleScore.setType(this.getType());
        //getType()保存的是和的类型
        battleScore.setUid(this.getToUid());
        //gainTotal保存的既为最后要发送的数据
        battleScore.setScore(this.gainTotal);

        //这里赋值的应该是和牌类型和分数！！！从getLostTotal中取的。
        for(Map.Entry<Integer,Integer> scores : this.getLostTotal().entrySet()){
            int index = room.getPlayerById(scores.getKey()).getIndex();
            int lostScoreType = Constants.lostScoreType[index];
            BattleScore subScore = new BattleScore();
            subScore.setType(lostScoreType);
            subScore.setScore(scores.getValue());
            battleScore.addDetail(subScore);
        }
    }

    /**
     * 判断当前用户是否应该计算杠分
     * @param c         获取calculator中的标记
     * @param gscore    杠分
     * @return      返回判断后的分数
     */
    private int gangScore(Calculator c,Integer gscore){
        return c.isComputedGang()?0:gscore;
    }

    public String log(){
        StringBuilder sb = new StringBuilder();
        sb.append("step:").append(this.step).append(",");
        sb.append("uid:").append(toUid).append(",");
        sb.append("gainScore:").append(gainTotal).append(",");
        String lost = "[";
        for(Map.Entry<Integer,Integer> lostScores : lostTotal.entrySet()){
            lost += lostScores.getKey()+":"+lostScores.getValue()+";";
        }
        lost += "]";
        sb.append(lost);
        return sb.toString();
    }
}
