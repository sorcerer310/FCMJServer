package com.rafo.chess.engine.majiang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import com.rafo.chess.engine.gameModel.IEHandCardsContainer;
import com.rafo.chess.engine.gameModel.IPlayer;

public class MJHandCardsContainer implements IEHandCardsContainer<MJCard> {
	MJPlayer player;
	/** 活牌 */
	ArrayList<MJCard> handcards = new ArrayList<MJCard>();
	/** 亮牌 */
	ArrayList<CardGroup> opencards = new ArrayList<CardGroup>();

	public ArrayList<CardGroup> getOpencards() {
		return opencards;
	}

	public void addOpenCards(ArrayList<MJCard> cards) {

	}

	@Override
	public IPlayer getPlayer() {
		return player;
	}

	@Override
	public ArrayList<MJCard> getHandCards() {
		return handcards;
	}

	@Override
	public void setHandCards(ArrayList<MJCard> list) {
		handcards = list;
	}

	@Override
	public void addHandCards(ArrayList<MJCard> list) {
		handcards.addAll(list);
	}

	@Override
	public void sortCards() {
		Collections.sort(handcards, new Comparator<MJCard>() {
			@Override
			public int compare(MJCard arg0, MJCard arg1) {
				return arg0.getCardNum() - arg1.getCardNum();
			}
		});
	}

	@Override
	public void cleanHands() {
		handcards.clear();
		opencards.clear();
	}

	@Override
	public HashMap<Integer, Integer> getCardCountFromHands() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (MJCard card : handcards) {
			if (!map.containsKey(card.getCardNum())) {
				map.put(card.getCardNum(), 0);
			}
			int count = map.get(card.getCardNum());
			count++;
			map.put(card.getCardNum(), count);
		}
		return map;
	}

}
