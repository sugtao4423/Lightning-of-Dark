package UserPageFragment;

import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Relationship;
import twitter4j.TwitterException;
import twitter4j.User;

import com.loopj.android.image.SmartImageView;
import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class _0_detail extends Fragment {
	
	private TextView UserBio, location, Link, User_tweet_c, User_favorite_c, User_follow_c, User_follower_c;
	private SmartImageView sourceIcon, targetIcon;
	private ImageView isFollowIcon;
	private User target;
	private ApplicationClass appClass;
	
	@Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.user_0, null);
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		appClass.setUserBio((TextView)v.findViewById(R.id.UserBio));
		appClass.setLocation((TextView)v.findViewById(R.id.location));
		appClass.setLink((TextView)v.findViewById(R.id.link));
		appClass.setUser_tweet_c((TextView)v.findViewById(R.id.User_tweet_count));
		appClass.setUser_favorite_c((TextView)v.findViewById(R.id.User_favorite_count));
		appClass.setUser_follow_c((TextView)v.findViewById(R.id.User_follow_count));
		appClass.setUser_follower_c((TextView)v.findViewById(R.id.User_follower_count));
		
		appClass.setSourceIcon((SmartImageView)v.findViewById(R.id.UserPage_sourceIcon));
		appClass.setTargetIcon((SmartImageView)v.findViewById(R.id.UserPage_targetIcon));
		appClass.setIsFollowIcon((ImageView)v.findViewById(R.id.UserPage_isFollow));

		return v;
	}
	public void setText(Context context){
		appClass = (ApplicationClass)context.getApplicationContext();
		UserBio = appClass.getUserBio();
		location = appClass.getLocation();
		Link = appClass.getLink();
		User_tweet_c = appClass.getUser_tweet_c();
		User_favorite_c = appClass.getUser_favorite_c();
		User_follow_c = appClass.getUser_follow_c();
		User_follower_c = appClass.getUser_follower_c();
		sourceIcon = appClass.getSourceIcon();
		targetIcon = appClass.getTargetIcon();
		isFollowIcon = appClass.getIsFollowIcon();
		target = appClass.getTarget();
		
		if(appClass.getMyScreenName().equals(target.getScreenName())){
			sourceIcon.setVisibility(View.GONE);
			targetIcon.setVisibility(View.GONE);
			isFollowIcon.setVisibility(View.GONE);
		}else{
			sourceIcon.setVisibility(View.VISIBLE);
			targetIcon.setVisibility(View.VISIBLE);
			isFollowIcon.setVisibility(View.VISIBLE);
			followCheck();
			set_souce_and_targetIcon();
		}
		
		String bio = target.getDescription();
		Matcher at_user = Pattern.compile("@\\w*", Pattern.DOTALL).matcher(bio);
		Matcher url = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.DOTALL).matcher(bio);
		if(at_user.find() || url.find()){
			at_user.reset();
			url.reset();
			if(at_user.find()){
				at_user.reset();
				while(at_user.find())
					bio = bio.replace(at_user.group(), "<a href=\"https://twitter.com/" + at_user.group().substring(1) + "\">" + at_user.group() + "</a>");
			}
			if(url.find()){
				url.reset();
				while(url.find())
					bio = bio.replace(url.group(), "<a href=\"" + url.group() + "\">" + url.group() + "</a>");
			}
			UserBio.setMovementMethod(LinkMovementMethod.getInstance());
			UserBio.setText(Html.fromHtml(bio));
		}else
			UserBio.setText(bio);
		location.setText(target.getLocation());
		Link.setText(target.getURL());
		User_tweet_c.setText(numberFormat(target.getStatusesCount()));
		User_favorite_c.setText(numberFormat(target.getFavouritesCount()));
		User_follow_c.setText(numberFormat(target.getFriendsCount()));
		User_follower_c.setText(numberFormat(target.getFollowersCount()));
	}
	public String numberFormat(int num){
		return NumberFormat.getInstance().format(num);
	}

	public void followCheck(){
		AsyncTask<Void, Void, Relationship> task = new AsyncTask<Void, Void, Relationship>(){
			@Override
			protected Relationship doInBackground(Void... params) {
				try {
					return appClass.getTwitter().showFriendship(appClass.getMyScreenName(), target.getScreenName());
				} catch (TwitterException e) {
					return null;
				}
			}
			@Override
			protected void onPostExecute(Relationship ship){
				if(ship != null){
					if(ship.isSourceFollowingTarget() && ship.isSourceFollowedByTarget())
						isFollowIcon.setImageResource(R.drawable.follow_each);
					else if(ship.isSourceFollowingTarget())
						isFollowIcon.setImageResource(R.drawable.follow_follow);
					else if(ship.isSourceFollowedByTarget())
						isFollowIcon.setImageResource(R.drawable.follow_follower);
					else if(ship.isSourceBlockingTarget())
						isFollowIcon.setImageResource(R.drawable.follow_block);
				}
			}
		};
		task.execute();
	}
	public void set_souce_and_targetIcon(){
		AsyncTask<Void, Void, String[]> task = new AsyncTask<Void, Void, String[]>(){
			@Override
			protected String[] doInBackground(Void... params) {
				try {
					if(appClass.getGetBigIcon()){
						return new String[]{
								appClass.getTwitter().verifyCredentials().getBiggerProfileImageURL() ,
								target.getBiggerProfileImageURL()
						};
					}else{
						return new String[]{
						appClass.getTwitter().verifyCredentials().getProfileImageURL() ,
						target.getProfileImageURL()
						};
					}
				} catch (TwitterException e) {
					return null;
				}
			}
			@Override
			protected void onPostExecute(String[] result){
				if(result[0] != null){
					sourceIcon.setImageUrl(result[0]);
					targetIcon.setImageUrl(result[1]);
				}else
					new ShowToast("ユーザーアイコンの取得に失敗しました", getActivity());
			}
		};
		task.execute();
	}
}
