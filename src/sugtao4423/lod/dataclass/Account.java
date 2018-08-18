package sugtao4423.lod.dataclass;

public class Account{

	private String screenName;
	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessTokenSecret;
	private long listAsTL;
	private long[] selectListIds;
	private String[] selectListNames;
	private String[] startAppLoadLists;

	public Account(String screenName, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret,
			long listAsTL, long[] selectListIds, String[] selectListNames, String[] startAppLoadLists){
		this.screenName = screenName;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
		this.listAsTL = listAsTL;
		this.selectListIds = selectListIds;
		this.selectListNames = selectListNames;
		this.startAppLoadLists = startAppLoadLists;
	}

	public Account(String screenName, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret){
		this(screenName, consumerKey, consumerSecret, accessToken, accessTokenSecret, -1L, new long[0], new String[0], new String[0]);
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

	public long getListAsTL(){
		return listAsTL;
	}

	public long[] getSelectListIds(){
		return selectListIds;
	}

	public String[] getSelectListNames(){
		return selectListNames;
	}

	public String[] getStartAppLoadLists(){
		return startAppLoadLists;
	}

}
