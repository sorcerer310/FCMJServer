package com.rafo.chess.engine.calculate;

import com.rafo.chess.engine.plugin.IOptPlugin;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PayDetailed implements Cloneable{
	/** 第几步 */
	private int step;
	/** 是否有效 */
	private boolean isValid = true;
	/** 支付分数的玩家 */
	private int[] fromUids;
	/** 获得分数的玩家 */
	private int toUid;
	/** 番(基础结算值) */
	private int rate;
	
	private List<Integer> cards = new ArrayList<>();

	private int type;

	private int subType;

	private PayType payType = PayType.ADD;

	/**产生支付的插件对象*/
	private IOptPlugin plugin;

	public boolean bFromMa = false;


	public IOptPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(IOptPlugin plugin) {
		this.plugin = plugin;
	}

	public int[] getFromUids() {
		return fromUids;
	}

	private boolean attached;

	private boolean canMerge = true;

	//永修结算分数
	private YXPayDetailed yxpd = new YXPayDetailed(this);
	public YXPayDetailed getYxpd() {return yxpd;}
	public void setYxpd(YXPayDetailed yxpd) {this.yxpd = yxpd;}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("step:" + step);
		sb.append(", ");
		sb.append("pluginId:" + plugin.getGen().getTempId());
		sb.append(", ");
		sb.append("name:" + plugin.getGen().getPluginName());
		sb.append(", ");
		sb.append("isValid:" + isValid);
		sb.append(", ");
		sb.append("subType:" + type);
		sb.append(", ");
		sb.append("toUid:" + toUid);
		sb.append(", ");
		ArrayList<Integer> uids = new ArrayList<>();
		for(Integer id : fromUids){
			uids.add(id);
		}
		sb.append("fromUids:" + StringUtils.join(Arrays.asList(uids),","));
		sb.append(",");
		sb.append("cards:" + StringUtils.join(cards,","));
		sb.append(",");
		sb.append("rate:" + rate);
		sb.append(",");
		sb.append("attached:" + attached);
		sb.append(",");
		sb.append("canMerge:" + canMerge);
		sb.append(",");
		return sb.toString();
	}

	public int[] getFromUid() {
		return fromUids;
	}

	public void setFromUid(int[] fromUids) {
		this.fromUids = fromUids;
	}

	public void setFromUid(List<Integer> uids) {
		if(uids != null){
			this.fromUids = new int[uids.size()];
			for(int i=0; i<uids.size(); i++){
				this.fromUids[i] = uids.get(i);
			}
		}
	}

	public int getToUid() {
		return toUid;
	}

	public void setToUid(int toUid) {
		this.toUid = toUid;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public List<Integer> getCards() {
		return cards;
	}

	public void setCards(List<Integer> cards) {
		this.cards = cards;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSubType() {
		return subType;
	}

	public void setSubType(int subType) {
		this.subType = subType;
	}

	public void addCard(int card){
		this.cards.add(card);
	}

	public void setCard(int card){
		this.cards.add(card);
	}

	public int getCard() {
		return this.cards.size() > 0? this.cards.get(0) : 0;
	}

	public PayType getPayType() {
		return payType;
	}

	public void setPayType(PayType payType) {
		this.payType = payType;
	}

	public boolean isAttached() {
		return attached;
	}

	public void setAttached(boolean attached) {
		this.attached = attached;
	}

	public boolean isCanMerge() {
		return canMerge;
	}

	public void setCanMerge(boolean canMerge) {
		this.canMerge = canMerge;
	}

	public enum PayType{
		ADD, Multiple
	}

	@Override
	public PayDetailed clone() throws CloneNotSupportedException {
		return (PayDetailed)super.clone();
	}
}