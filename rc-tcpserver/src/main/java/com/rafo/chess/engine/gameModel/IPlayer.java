package com.rafo.chess.engine.gameModel;

public interface IPlayer {
	public int getUid();
	
	public int getIndex();
	public void setIndex(int index);
	/**取得手牌*/
	public IEHandCardsContainer getHandCards();
	/**0准备中，1 准备 2 战斗 */
	public PlayState getPlayState();
	/***/
	public void setPlayerState(PlayState state);
	/**积分*/
	public int getScore();
	/**积分*/
	public void setScore(int score);
	/***/
	public int getRoomId();
	public void setRoomId(int roomId);
    public enum PlayState
    {
        Idle, Ready, Battle
    }

	public void setUid(int uid);

	/**是否断线 false 正常 true 断线*/
	public boolean isOffline();
	public void setOffline(boolean offline);


	public boolean isSeated();
	public void setSeated(boolean seated);
	
}
