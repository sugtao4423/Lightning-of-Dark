package sugtao4423.lod.userpage_fragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;

import twitter4j.Relationship;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;

import com.loopj.android.image.SmartImageView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import sugtao4423.lod.App;
import sugtao4423.lod.ChromeIntent;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.swipe_image_viewer.ImageFragmentActivity;
import sugtao4423.lod.utils.Regex;

public class _0_detail extends Fragment{

	private TextView isFollowIcon, userBio, userLocation, userLink, userTweetC, userFavoriteC, userFollowC, userFollowerC, userCreate;
	private SmartImageView sourceIcon, targetIcon;
	private User target;
	private App app;

	private SmartImageView userBanner, userIcon;
	private TextView userName, userScreenName, protect;

	private boolean isTextSet;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.user_0, container, false);

		app = (App)getActivity().getApplicationContext();

		userBanner = (SmartImageView)v.findViewById(R.id.banner);
		userIcon = (SmartImageView)v.findViewById(R.id.UserIcon);
		userName = (TextView)v.findViewById(R.id.UserName);
		userScreenName = (TextView)v.findViewById(R.id.UserScreenName);
		protect = (TextView)v.findViewById(R.id.UserPage_protected);

		userBio = (TextView)v.findViewById(R.id.UserBio);
		userLocation = (TextView)v.findViewById(R.id.location);
		userLink = (TextView)v.findViewById(R.id.link);
		userTweetC = (TextView)v.findViewById(R.id.User_tweet_count);
		userFavoriteC = (TextView)v.findViewById(R.id.User_favorite_count);
		userFollowC = (TextView)v.findViewById(R.id.User_follow_count);
		userFollowerC = (TextView)v.findViewById(R.id.User_follower_count);
		userCreate = (TextView)v.findViewById(R.id.User_create_date);
		sourceIcon = (SmartImageView)v.findViewById(R.id.UserPage_sourceIcon);
		targetIcon = (SmartImageView)v.findViewById(R.id.UserPage_targetIcon);
		isFollowIcon = (TextView)v.findViewById(R.id.UserPage_isFollow);

		protect.setVisibility(View.GONE);

		Typeface tf = app.getFontAwesomeTypeface();
		protect.setTypeface(tf);
		((TextView)v.findViewById(R.id.icon_tweet_count)).setTypeface(tf);
		((TextView)v.findViewById(R.id.icon_favorite_count)).setTypeface(tf);
		((TextView)v.findViewById(R.id.icon_follow_count)).setTypeface(tf);
		((TextView)v.findViewById(R.id.icon_follower_count)).setTypeface(tf);
		((TextView)v.findViewById(R.id.icon_create_date)).setTypeface(tf);

		setClick(getActivity());
		return v;
	}

	public void setTargetUser(User targetUser){
		this.target = targetUser;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		setText();
	}

	public void setText(){
		if(target == null || isTextSet)
			return;
		isTextSet = true;
		if(target.isProtected())
			protect.setVisibility(View.VISIBLE);
		userIcon.setImageUrl(target.getOriginalProfileImageURL(), null, R.drawable.ic_action_refresh);
		userBanner.setImageUrl(target.getProfileBannerRetinaURL());
		userName.setText(target.getName());
		userScreenName.setText("@" + target.getScreenName());

		if(app.getCurrentAccount().getScreenName().equals(target.getScreenName())){
			sourceIcon.setVisibility(View.GONE);
			targetIcon.setVisibility(View.GONE);
			isFollowIcon.setVisibility(View.GONE);
		}else{
			sourceIcon.setVisibility(View.VISIBLE);
			targetIcon.setVisibility(View.VISIBLE);
			isFollowIcon.setVisibility(View.VISIBLE);
			followCheck();
			set_source_and_targetIcon();
		}

		setLinkTouch(getContext(), userBio, replaceUrlEntity2ExUrl(target.getDescription(), target.getDescriptionURLEntities()));
		setLinkTouch(getContext(), userLocation, target.getLocation());
		setLinkTouch(getContext(), userLink, replaceUrlEntity2ExUrl(target.getURL(), target.getURLEntity()));
		userTweetC.setText(numberFormat(target.getStatusesCount()));
		userFavoriteC.setText(numberFormat(target.getFavouritesCount()));
		userFollowC.setText(numberFormat(target.getFriendsCount()));
		userFollowerC.setText(numberFormat(target.getFollowersCount()));
		userCreate.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(target.getCreatedAt()));
	}

	public String replaceUrlEntity2ExUrl(String target, URLEntity entity){
		if(target == null || target.isEmpty())
			return "";
		return target.replace(entity.getURL(), entity.getExpandedURL());
	}

	public String replaceUrlEntity2ExUrl(String target, URLEntity[] entity){
		if(target == null || target.isEmpty())
			return "";
		for(URLEntity e : entity)
			target = replaceUrlEntity2ExUrl(target, e);
		return target;
	}

	public void setLinkTouch(final Context context, TextView view, String setStr){
		if(setStr == null || setStr.isEmpty()){
			view.setText("");
			return;
		}
		SpannableString ss = new SpannableString(setStr);
		Matcher m = Regex.userAndAnyUrl.matcher(setStr);
		while(m.find()){
			final String t = m.group();
			if(t.startsWith("@") || t.startsWith("http")){
				ss.setSpan(new URLSpan(t){
					@Override
					public void onClick(View v){
						if(t.startsWith("@")){
							Intent intent = new Intent(context, UserPage.class);
							intent.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, this.getURL().replace("@", ""));
							context.startActivity(intent);
						}else if(t.startsWith("http")){
							new ChromeIntent(context, Uri.parse(t));
						}
					}
				}, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}else{
				ss.setSpan(new URLSpan(t), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		view.setText(ss);
		view.setMovementMethod(LinkMovementMethod.getInstance());
	}

	public String numberFormat(int num){
		return NumberFormat.getInstance().format(num);
	}

	public void followCheck(){
		new AsyncTask<Void, Void, Relationship>(){
			@Override
			protected Relationship doInBackground(Void... params){
				try{
					return app.getTwitter().showFriendship(app.getCurrentAccount().getScreenName(), target.getScreenName());
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(Relationship ship){
				if(ship != null){
					isFollowIcon.setTypeface(app.getFontAwesomeTypeface());
					if(ship.isSourceFollowingTarget() && ship.isSourceFollowedByTarget())
						isFollowIcon.setText(getContext().getString(R.string.icon_followEach));
					else if(ship.isSourceFollowingTarget())
						isFollowIcon.setText(getContext().getString(R.string.icon_followFollow));
					else if(ship.isSourceFollowedByTarget())
						isFollowIcon.setText(getContext().getString(R.string.icon_followFollower));
					else if(ship.isSourceBlockingTarget())
						isFollowIcon.setText(getContext().getString(R.string.icon_followBlock));
				}
			}
		}.execute();
	}

	public void set_source_and_targetIcon(){
		new AsyncTask<Void, Void, String[]>(){
			@Override
			protected String[] doInBackground(Void... params){
				try{
					return new String[]{app.getTwitter().verifyCredentials().getBiggerProfileImageURL(),
							target.getBiggerProfileImageURL()};
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(String[] result){
				if(result != null){
					sourceIcon.setImageUrl(result[0], null, R.drawable.ic_action_refresh);
					targetIcon.setImageUrl(result[1], null, R.drawable.ic_action_refresh);
				}else
					new ShowToast(getContext().getApplicationContext(), R.string.error_getUserIcon);
			}
		}.execute();
	}

	public void setClick(final Context context){
		userIcon.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Intent image = new Intent(context, ImageFragmentActivity.class);
				image.putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, new String[]{target.getOriginalProfileImageURL()});
				image.putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_TYPE, ImageFragmentActivity.TYPE_ICON);
				context.startActivity(image);
			}
		});
		userIcon.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v){
				new ChromeIntent(context, Uri.parse(target.getOriginalProfileImageURL()));
				return true;
			}
		});
		userBanner.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(target.getProfileBannerURL() != null){
					Intent image = new Intent(context, ImageFragmentActivity.class);
					image.putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, new String[]{target.getProfileBanner1500x500URL()});
					image.putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_TYPE, ImageFragmentActivity.TYPE_BANNER);
					context.startActivity(image);
				}
			}
		});
		userBanner.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v){
				if(target.getProfileBannerURL() != null)
					new ChromeIntent(context, Uri.parse(target.getProfileBanner1500x500URL()));
				return true;
			}
		});
	}
}