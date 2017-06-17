package com.tao.lightning_of_dark.tweetlistview;

import java.text.NumberFormat;
import java.util.ArrayList;

import com.loopj.android.image.SmartImageView;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.userPageFragment.UserPage;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import twitter4j.PagableResponseList;
import twitter4j.User;

public class TweetListUserAdapter extends RecyclerView.Adapter<TweetListUserAdapter.ViewHolder>{

	private LayoutInflater inflater;
	private ArrayList<User> data;
	private Context context;

	public TweetListUserAdapter(Context context){
		this.inflater = LayoutInflater.from(context);
		this.data = new ArrayList<User>();
		this.context = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position){
		return new ViewHolder(inflater.inflate(R.layout.list_item_tweet, viewGroup, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position){
		if(!(data != null && data.size() > position && data.get(position) != null))
			return;

		holder.rt_icon.setVisibility(View.GONE);
		holder.rt_sn.setVisibility(View.GONE);
		holder.tweetImagesScroll.setVisibility(View.GONE);

		final User item = data.get(position);

		if(item.isProtected())
			holder.protect.setVisibility(View.VISIBLE);
		else
			holder.protect.setVisibility(View.GONE);

		if(position % 2 == 0)
			holder.v.setBackgroundResource(R.drawable.position0);
		else
			holder.v.setBackgroundResource(R.drawable.position1);

		holder.icon.setImageUrl(item.getBiggerProfileImageURL(), null, R.drawable.ic_action_refresh);
		holder.name_sn.setText(item.getName() + " - @" + item.getScreenName());
		holder.content.setText(item.getDescription());
		holder.date.setText("Tweet: " + numberFormat(item.getStatusesCount()) + "  Fav: "
				+ numberFormat(item.getFavouritesCount()) + "  Follow: " + numberFormat(item.getFriendsCount()) + "  Follower: "
				+ numberFormat(item.getFollowersCount()));

		holder.v.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v){
				Intent i = new Intent(context, UserPage.class);
				i.putExtra("userScreenName", item.getScreenName());
				context.startActivity(i);
			}
		});
	}

	public String numberFormat(int num){
		return NumberFormat.getInstance().format(num);
	}

	@Override
	public int getItemCount(){
		if(data != null)
			return data.size();
		else
			return 0;
	}

	public void addAll(PagableResponseList<User> users){
		int pos = data.size();
		data.addAll(users);
		notifyItemRangeInserted(pos, users.size());
	}

	public void clear(){
		data.clear();
		notifyDataSetChanged();
	}

	class ViewHolder extends RecyclerView.ViewHolder{

		View v;
		SmartImageView icon, rt_icon;
		ImageView protect;
		TextView name_sn, content, date, rt_sn;
		HorizontalScrollView tweetImagesScroll;
		LinearLayout tweetImagesLayout;

		public ViewHolder(View itemView){
			super(itemView);
			icon = (SmartImageView)itemView.findViewById(R.id.icon);
			rt_icon = (SmartImageView)itemView.findViewById(R.id.RetweetedUserIcon);
			name_sn = (TextView)itemView.findViewById(R.id.name_screenName);
			content = (TextView)itemView.findViewById(R.id.tweetText);
			date = (TextView)itemView.findViewById(R.id.tweet_date);
			rt_sn = (TextView)itemView.findViewById(R.id.RetweetedUserScreenName);
			protect = (ImageView)itemView.findViewById(R.id.UserProtected);
			tweetImagesScroll = (HorizontalScrollView)itemView.findViewById(R.id.tweet_images_scroll);
			tweetImagesLayout = (LinearLayout)itemView.findViewById(R.id.tweet_images_layout);
			v = itemView;
		}
	}

}
