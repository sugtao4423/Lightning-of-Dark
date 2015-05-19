package com.tao.lightning_of_dark;

import java.util.regex.Pattern;

import com.loopj.android.image.SmartImageView;

import twitter4j.Twitter;
import twitter4j.User;
import android.app.AlertDialog;
import android.app.Application;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationClass extends Application {
	//MainActivity
	private String MyScreenName;
	private Twitter twitter;
	private Pattern mentionPattern;
	private CustomAdapter homeAdapter, mentionAdapter, listAdapter;
	private boolean option_regex, option_openBrowser, getBigIcon, list_AlreadyLoad;
	
	//UserPage
	private User target;
	
	//_0_detail
	private TextView UserBio, location, Link, User_tweet_c, User_favorite_c, User_follow_c, User_follower_c;
	private SmartImageView sourceIcon, targetIcon;
	private ImageView isFollowIcon;
	
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
	
/*	+-+-+-+-+-+-+-+-+-+
	|_|0|_|d|e|t|a|i|l|
	+-+-+-+-+-+-+-+-+-+
*/
	public void setUserBio(TextView UserBio){
		this.UserBio = UserBio;
	}
	public TextView getUserBio(){
		return UserBio;
	}
	public void setLocation(TextView location){
		this.location = location;
	}
	public TextView getLocation(){
		return location;
	}
	public void setLink(TextView Link){
		this.Link = Link;
	}
	public TextView getLink(){
		return Link;
	}
	public void setUser_tweet_c(TextView User_tweet_c){
		this.User_tweet_c = User_tweet_c;
	}
	public TextView getUser_tweet_c(){
		return User_tweet_c;
	}
	public void setUser_favorite_c(TextView User_favorite_c){
		this.User_favorite_c = User_favorite_c;
	}
	public TextView getUser_favorite_c(){
		return User_favorite_c;
	}
	public void setUser_follow_c(TextView User_follow_c){
		this.User_follow_c = User_follow_c;
	}
	public TextView getUser_follow_c(){
		return User_follow_c;
	}
	public void setUser_follower_c(TextView User_follower_c){
		this.User_follower_c = User_follower_c;
	}
	public TextView getUser_follower_c(){
		return User_follower_c;
	}
	public void setSourceIcon(SmartImageView sourceIcon){
		this.sourceIcon = sourceIcon;
	}
	public SmartImageView getSourceIcon(){
		return sourceIcon;
	}
	public void setTargetIcon(SmartImageView targetIcon){
		this.targetIcon = targetIcon;
	}
	public SmartImageView getTargetIcon(){
		return targetIcon;
	}
	public void setIsFollowIcon(ImageView isFollowIcon){
		this.isFollowIcon = isFollowIcon;
	}
	public ImageView getIsFollowIcon(){
		return isFollowIcon;
	}
	
/*	+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	|L|i|s|t|V|i|e|w|L|i|s|t|e|n|e|r|
	+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
*/
	public void setDialog(AlertDialog dialog){
		this.dialog = dialog;
	}
	public AlertDialog getDialog(){
		return dialog;
	}
}