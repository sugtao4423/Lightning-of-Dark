package sugtao4423.lod.dataclass;

import sugtao4423.lod.tweetlistview.TweetListAdapter;

public class TwitterList{

	private TweetListAdapter adapter;
	private boolean isAlreadyLoad;
	private String listName;
	private long listId;
	private boolean isAppStartLoad;

	public TwitterList(TweetListAdapter adapter, boolean isAlreadyLoad, String listName, long listId, boolean isAppStartLoad){
		this.adapter = adapter;
		this.isAlreadyLoad = isAlreadyLoad;
		this.listName = listName;
		this.listId = listId;
		this.isAppStartLoad = isAppStartLoad;
	}

	public TweetListAdapter getTweetListAdapter(){
		return adapter;
	}

	public void setIsAlreadyLoad(boolean isAlreadyLoad){
		this.isAlreadyLoad = isAlreadyLoad;
	}

	public boolean getIsAlreadyLoad(){
		return isAlreadyLoad;
	}

	public String getListName(){
		return listName;
	}

	public long getListId(){
		return listId;
	}

	public boolean getIsAppStartLoad(){
		return isAppStartLoad;
	}

}
