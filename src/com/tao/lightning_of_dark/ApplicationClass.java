package com.tao.lightning_of_dark;

import java.util.regex.Pattern;

import com.loopj.android.image.SmartImageView;

import twitter4j.Twitter;
import twitter4j.User;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

public class ApplicationClass extends Application{
	// MainActivity
	private String myScreenName;
	private Twitter twitter;
	private Pattern mentionPattern;
	private CustomAdapter[] listAdapters;
	private boolean option_regex, option_openBrowser, getBigIcon, isWebm;
	private boolean[] list_AlreadyLoad;
	// MainActivity - CustomToast
	private View customToast;
	private TextView toast_main_message, toast_tweet;
	private SmartImageView toast_icon;

	// UserPage
	private User target;
	private String targetScreenName;

	/*
	 * +-+-+-+-+-+-+-+-+-+-+-+-+
	 * |M|a|i|n|A|c|t|i|v|i|t|y|
	 * +-+-+-+-+-+-+-+-+-+-+-+-+
	 */
	// MyScreenName
	public void setMyScreenName(String myScreenName){
		this.myScreenName = myScreenName;
	}

	public String getMyScreenName(){
		return myScreenName;
	}

	// Twitter
	public void setTwitter(Twitter twitter){
		this.twitter = twitter;
	}

	public Twitter getTwitter(){
		return twitter;
	}

	// mentionPattern
	public void setMentionPattern(Pattern mentionPattern){
		this.mentionPattern = mentionPattern;
	}

	public Pattern getMentionPattern(){
		return mentionPattern;
	}

	// listAdapter
	public void setListAdapters(CustomAdapter[] listAdapters){
		this.listAdapters = listAdapters;
	}

	public CustomAdapter[] getListAdapters(){
		return listAdapters;
	}

	// option_regex
	public void setOption_regex(boolean option_regex){
		this.option_regex = option_regex;
	}

	public boolean getOption_regex(){
		return option_regex;
	}

	// option_openBrowser
	public void setOption_openBrowser(boolean option_openBrowser){
		this.option_openBrowser = option_openBrowser;
	}

	public boolean getOption_openBrowser(){
		return option_openBrowser;
	}

	// getBigIcon
	public void setGetBigIcon(boolean getBigIcon){
		this.getBigIcon = getBigIcon;
	}

	public boolean getGetBigIcon(){
		return getBigIcon;
	}

	// isWebm
	public void setIsWebm(boolean isWebm){
		this.isWebm = isWebm;
	}

	public boolean getIsWebm(){
		return isWebm;
	}

	public void loadOption(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		setOption_regex(pref.getBoolean("menu_regex", false));
		setOption_openBrowser(pref.getBoolean("menu_openBrowser", false));
		setGetBigIcon(pref.getBoolean("getBigIcon", false));
		setIsWebm(pref.getBoolean("isWebm", false));
	}

	// list_AlreadyLoad
	public void setList_AlreadyLoad(boolean[] AlreadyLoad){
		this.list_AlreadyLoad = AlreadyLoad;
	}

	public boolean[] getList_AlreadyLoad(){
		return list_AlreadyLoad;
	}

	// CustomToast
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

	/*
	 * +-+-+-+-+-+-+-+-+
	 * |U|s|e|r|P|a|g|e|
	 * +-+-+-+-+-+-+-+-+
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
}