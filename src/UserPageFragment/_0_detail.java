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
import com.tao.lightning_of_dark.UserPage;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
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
		appClass.set_0_detail_v(v);

		return v;
	}
	public void setText(Context context){
		appClass = (ApplicationClass)context.getApplicationContext();
		View v = appClass.get_0_detail_v();
		
		UserBio = (TextView)v.findViewById(R.id.UserBio);
		location = (TextView)v.findViewById(R.id.location);
		Link = (TextView)v.findViewById(R.id.link);
		User_tweet_c = (TextView)v.findViewById(R.id.User_tweet_count);
		User_favorite_c = (TextView)v.findViewById(R.id.User_favorite_count);
		User_follow_c = (TextView)v.findViewById(R.id.User_follow_count);
		User_follower_c = (TextView)v.findViewById(R.id.User_follower_count);
		sourceIcon = (SmartImageView)v.findViewById(R.id.UserPage_sourceIcon);
		targetIcon = (SmartImageView)v.findViewById(R.id.UserPage_targetIcon);
		isFollowIcon = (ImageView)v.findViewById(R.id.UserPage_isFollow);
		
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
		SpannableString ss = new SpannableString(bio);
		final Matcher m = Pattern.compile("@[0-9a-zA-Z_]+|(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.DOTALL).matcher(bio);
		while(m.find()){
		    String t = m.group();
		    if(t.startsWith("@")){
		        ss.setSpan(new URLSpan(t){
		            @Override
		            public void onClick(View widget){
		            	Context cont = widget.getContext();
		                Intent intent = new Intent(cont, UserPage.class);
		                intent.putExtra("userScreenName", this.getURL().replace("@", ""));
		                cont.startActivity(intent);
		            }
		        }, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		    }else{
		    	ss.setSpan(new URLSpan(t), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		    }
		}
		UserBio.setText(ss);
		UserBio.setMovementMethod(LinkMovementMethod.getInstance());
		
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
					new ShowToast("ユーザーアイコンの取得に失敗しました", getActivity(), 0);
			}
		};
		task.execute();
	}
}