package sugtao4423.lod.dataclass;

public class Account{

	private String screen_name;
	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessTokenSecret;
	private boolean showList;
	private int selectListCount;
	private String selectListIds;
	private String selectListNames;
	private String startAppLoadLists;

	public Account(String screen_name, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret,
			boolean showList, int selectListCount, String selectListIds, String selectListNames, String startAppLoadLists){
		this.screen_name = screen_name;
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
		return screen_name;
	}

	public String getCK(){
		return consumerKey;
	}

	public String getCS(){
		return consumerSecret;
	}

	public String getAT(){
		return accessToken;
	}

	public String getATS(){
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
