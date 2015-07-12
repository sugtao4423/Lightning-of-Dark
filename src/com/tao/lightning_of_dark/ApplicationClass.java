package com.tao.lightning_of_dark;

import java.util.regex.Pattern;

import com.loopj.android.image.SmartImageView;

import twitter4j.Twitter;
import twitter4j.User;
import android.app.AlertDialog;
import android.app.Application;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ApplicationClass extends Application {
	//MainActivity
	private String MyScreenName;
	private Twitter twitter;
	private Pattern mentionPattern;
	private CustomAdapter homeAdapter, mentionAdapter, listAdapter;
	private boolean option_regex, option_openBrowser, getBigIcon, list_AlreadyLoad;
	//MainActivity - CustomToast
	private View customToast;
	private TextView toast_main_message, toast_tweet;
	private SmartImageView toast_icon;
	
	//Fragment_home
	private ListView homeList;
	
	//UserPage
	private User target;
	private String targetScreenName;
	
	//_0_detail
	private View _0_detail_v;
	
	//ListViewListener
	private AlertDialog dialog;
	
/*	+-+-+-+-+-+-+-+-+-+-+-+-+
	|M|a|i|n|A|c|t|i|v|i|t|y|
	+-+-+-+-+-+-+-+-+-+-+-+-+
*/
	//MyScreenName
	public void setMyScreenName(String MyScreenName){
		this.MyScreenName = MyScreenName;
	}
	public String getMyScreenName(){
		return MyScreenName;
	}
	//Twitter
	public void setTwitter(Twitter twitter){
		this.twitter = twitter;
	}
	public Twitter getTwitter(){
		return twitter;
	}
	//mentionPattern
	public void setMentionPattern(Pattern mentionPattern){
		this.mentionPattern = mentionPattern;
	}
	public Pattern getMentionPattern(){
		return mentionPattern;
	}
	//homeAdapter
	public void setHomeAdapter(CustomAdapter homeAdapter){
		this.homeAdapter = homeAdapter;
	}
	public CustomAdapter getHomeAdapter(){
		return homeAdapter;
	}
	//mentionAdapter
	public void setMentionAdapter(CustomAdapter mentionAdapter){
		this.mentionAdapter = mentionAdapter;
	}
	public CustomAdapter getMentionAdapter(){
		return mentionAdapter;
	}
	//listAdapter
	public void setListAdapter(CustomAdapter listAdapter){
		this.listAdapter = listAdapter;
	}
	public CustomAdapter getListAdapter(){
		return listAdapter;
	}
	//option_regex
	public void setOption_regex(boolean option_regex){
		this.option_regex = option_regex;
	}
	public boolean getOption_regex(){
		return option_regex;
	}
	//option_openBrowser
	public void setOption_openBrowser(boolean option_openBrowser){
		this.option_openBrowser = option_openBrowser;
	}
	public boolean getOption_openBrowser(){
		return option_openBrowser;
	}
	//getBigIcon
	public void setGetBigIcon(boolean getBigIcon){
		this.getBigIcon = getBigIcon;
	}
	public boolean getGetBigIcon(){
		return getBigIcon;
	}
	//list_AlreadyLoad
	public void setList_AlreadyLoad(boolean AlreadyLoad){
		this.list_AlreadyLoad = AlreadyLoad;
	}
	public boolean getList_AlreadyLoad(){
		return list_AlreadyLoad;
	}
		//CustomToast
	public void setToastView(View custom_toast){
		this.customToast = custom_toast;
	}
	public View getToastView(){
		return customToast;
	}
	public void setToast_Main_Message(TextView toast_main_message){
		this.toast_main_message = toast_main_message;
	}
	public TextView getToast_Main_Message(){
		return toast_main_message;
	}
	public void setToast_Tweet(TextView toast_tweet){
		this.toast_tweet = toast_tweet;
	}
	public TextView getToast_Tweet(){
		return toast_tweet;
	}
	public void setToast_Icon(SmartImageView toast_icon){
		this.toast_icon = toast_icon;
	}
	public SmartImageView getToast_Icon(){
		return toast_icon;
	}
	
/*	+-+-+-+-+-+-+-+-+-+-+-+-+-+
	|F|r|a|g|m|e|n|t|_|h|o|m|e|
	+-+-+-+-+-+-+-+-+-+-+-+-+-+
*/
	public void setHomeList(ListView homeList){
		this.homeList = homeList;
	}
	public ListView getHomeList(){
		return homeList;
	}
	
/*	+-+-+-+-+-+-+-+-+
	|U|s|e|r|P|a|g|e|
	+-+-+-+-+-+-+-+-+
*/
	public void setTarget(User target){
		this.target = target;
	}
	public User getTarget(){
		return target;
	}
	public void setTargetScreenName(String targetScreenName){
		this.targetScreenName = targetScreenName;
	}
	public String getTargetScreenName(){
		return targetScreenName;
	}
	
/*	+-+-+-+-+-+-+-+-+-+
	|_|0|_|d|e|t|a|i|l|
	+-+-+-+-+-+-+-+-+-+
*/
	public void set_0_detail_v(View v){
		this._0_detail_v = v;
	}
	public View get_0_detail_v(){
		return _0_detail_v;
	}
	
/*	+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	|L|i|s|t|V|i|e|w|L|i|s|t|e|n|e|r|
	+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
*/
	public void setListViewDialog(AlertDialog dialog){
		this.dialog = dialog;
	}
	public AlertDialog getListViewDialog(){
		return dialog;
	}
}