package sugtao4423.lod.dataclass;

public class Account{

	private String screenName;
	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessTokenSecret;
	private boolean showList;
	private int selectListCount;
	private String selectListIds;
	private String selectListNames;
	private String startAppLoadLists;

	public Account(String screenName, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret,
			boolean showList, int selectListCount, String selectListIds, String selectListNames, String startAppLoadLists){
		this.screenName = screenName;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
		this.showList = showList;
		this.selectListCount = selectListCount;
		this.selectListIds = selectListIds;
		this.selectListNames = selectListNames;
		this.startAppLoadLists = startAppLoadLists;
	}

	public String getScreenName(){
		return screenName;
	}

	public String getConsumerKey(){
		return consumerKey;
	}

	public String getConsumerSecret(){
		return consumerSecret;
	}

	public String getAccessToken(){
		return accessToken;
	}

	public String getAccessTokenSecret(){
		return accessTokenSecret;
	}

	public boolean getShowList(){
		return showList;
	}

	public int getSelectListCount(){
		return selectListCount;
	}

	public String getSelectListIds(){
		return selectListIds;
	}

	public String getSelectListNames(){
		return selectListNames;
	}

	public String getStartAppLoadLists(){
		return startAppLoadLists;
	}

}
