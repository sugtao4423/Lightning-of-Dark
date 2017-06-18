package com.tao.lightning_of_dark.tweetlistview;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.loopj.android.image.SmartImageView;
import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.Show_Video;
import com.tao.lightning_of_dark.UiHandler;
import com.tao.lightning_of_dark.swipeImageViewer.ImageFragmentActivity;
import com.tao.lightning_of_dark.userPageFragment.UserPage;
import com.tao.lightning_of_dark.utils.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import twitter4j.MediaEntity;
import twitter4j.ResponseList;
import twitter4j.Status;

public class TweetListAdapter extends RecyclerView.Adapter<TweetListAdapter.ViewHolder>{

	private LayoutInflater inflater;
	private ArrayList<Status> data;
	private Context context;
	private ApplicationClass appClass;
	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;

	public TweetListAdapter(Context context){
		this.inflater = LayoutInflater.from(context);
		this.data = new ArrayList<Status>();
		this.context = context;
		this.appClass = (ApplicationClass)context.getApplicationContext();
	}

	@Override
	public TweetListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position){
		return new ViewHolder(inflater.inflate(R.layout.list_item_tweet, viewGroup, false));
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position){
		if(!(data != null && data.size() > position && data.get(position) != null))
			return;
		final Status item = data.get(position);
		final Status origStatus = item.isRetweet() ? item.getRetweetedStatus() : item;

		// 鍵
		if(origStatus.getUser().isProtected())
			holder.protect.setVisibility(View.VISIBLE);
		else
			holder.protect.setVisibility(View.GONE);

		// リスト背景
		if(item.isRetweetedByMe()){
			holder.v.setBackgroundResource(R.drawable.retweeted_by_me);
		}else if(item.isRetweet()){
			holder.v.setBackgroundResource(R.drawable.retweet);
		}else if(item.getUser().getScreenName().equals(appClass.getMyScreenName())){
			holder.v.setBackgroundResource(R.drawable.same_my_screenname);
		}else if(appClass.getMentionPattern().matcher(item.getText()).find()){
			holder.v.setBackgroundResource(R.drawable.mention);
		}else{
			if(position % 2 == 0)
				holder.v.setBackgroundResource(R.drawable.position0);
			else
				holder.v.setBackgroundResource(R.drawable.position1);
		}

		// アイコン、名前、スクリーンネーム、タイムスタンプ、クライアント
		if(item.isRetweet()){
			holder.rt_icon.setVisibility(View.VISIBLE);
			holder.rt_sn.setVisibility(View.VISIBLE);
			holder.date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE)
					.format(item.getRetweetedStatus().getCreatedAt()) + "  Retweeted by ");
			holder.rt_icon.setImageUrl(item.getUser().getProfileImageURL(), null, R.drawable.ic_action_refresh);
			holder.rt_sn.setText("@" + item.getUser().getScreenName());
		}else{
			holder.rt_icon.setVisibility(View.GONE);
			holder.rt_sn.setVisibility(View.GONE);
			holder.date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(item.getCreatedAt())
					+ "  via " + item.getSource().replaceAll("<.+?>", ""));
		}

		holder.name_sn.setText(origStatus.getUser().getName() + " - @" + origStatus.getUser().getScreenName());
		holder.content.setText(origStatus.getText());
		holder.icon.setImageUrl(origStatus.getUser().getBiggerProfileImageURL(), null, R.drawable.ic_action_refresh);

		holder.icon.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Intent intent = new Intent(context, UserPage.class);
				intent.putExtra("userScreenName", origStatus.getUser().getScreenName());
				context.startActivity(intent);
			}
		});

		holder.v.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				onItemClickListener.onItemClicked(context, data, position);
			}
		});
		holder.v.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v){
				return onItemLongClickListener.onItemLongClicked(context, data, position);
			}
		});

		MediaEntity[] mentitys = origStatus.getMediaEntities();
		if(mentitys != null && mentitys.length > 0){
			holder.tweetImagesScroll.setVisibility(View.VISIBLE);
			holder.tweetImagesLayout.removeAllViews();
			for(int i = 0; i < mentitys.length; i++){
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, 200);
				if(holder.tweetImagesLayout.getChildCount() != 0)
					params.setMargins(8, 0, 0, 0);
				SmartImageView child = new SmartImageView(context);
				child.setLayoutParams(params);
				child.setMaxHeight(200);
				child.setAdjustViewBounds(true);
				holder.tweetImagesLayout.addView(child);

				if(Utils.isVideoOrGif(mentitys[i])){
					final boolean isGif = Utils.isGif(mentitys[i]);
					final String[] videoUrl = Utils.getVideoURLsSortByBitrate(appClass, mentitys);
					child.setImageResource(R.drawable.video_play);
					child.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v){
							Intent intent = new Intent(context, Show_Video.class);
							intent.putExtra("URL", videoUrl[videoUrl.length - 1]);
							if(isGif)
								intent.putExtra("type", Show_Video.TYPE_GIF);
							else
								intent.putExtra("type", Show_Video.TYPE_VIDEO);
							context.startActivity(intent);
						}
					});
				}else{
					child.setImageUrl(mentitys[i].getMediaURL() + ":small", null, R.drawable.ic_action_refresh);
					final int pos = i;
					final String[] urls = new String[mentitys.length];
					for(int j = 0; j < urls.length; j++)
						urls[j] = mentitys[j].getMediaURL();
					child.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v){
							Intent intent = new Intent(context, ImageFragmentActivity.class);
							intent.putExtra("urls", urls);
							intent.putExtra("position", pos);
							context.startActivity(intent);
						}
					});
				}
				holder.tweetImagesScroll.setOnTouchListener(new OnTouchListener(){
					boolean actionMove = false;
					@SuppressLint("ClickableViewAccessibility")
					@Override
					public boolean onTouch(View v, MotionEvent event){
						switch(event.getAction()){
						case MotionEvent.ACTION_MOVE:
							actionMove = true;
							break;
						case MotionEvent.ACTION_UP:
							if(actionMove)
								break;
							holder.v.performClick();
							break;
						default:
							actionMove = false;
							break;
						}
						HorizontalScrollView sv = (HorizontalScrollView)v;
						if(sv.getChildAt(0).getWidth() > sv.getWidth())
							return v.onTouchEvent(event);
						return true;
					}
				});
			}
		}else{
			holder.tweetImagesScroll.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount(){
		if(data != null)
			return data.size();
		else
			return 0;
	}

	public Status getItem(int position){
		return data.get(position);
	}

	public void add(final Status status){
		new UiHandler(){

			@Override
			public void run(){
				data.add(status);
				notifyItemInserted(data.size() - 1);
			}
		}.post();
	}

	public void addAll(final ResponseList<Status> status){
		new UiHandler(){

			@Override
			public void run(){
				int pos = data.size();
				data.addAll(status);
				notifyItemRangeInserted(pos, status.size());
			}
		}.post();
	}

	public void insertTop(final Status item){
		new UiHandler(){

			@Override
			public void run(){
				data.add(0, item);
				notifyItemInserted(0);
				if(data.size() - 1 != 0)
					notifyItemRangeChanged(1, data.size());
			}
		}.post();
	}

	public void clear(){
		new UiHandler(){

			@Override
			public void run(){
				int size = data.size();
				data.clear();
				notifyItemRangeRemoved(0, size);
			}
		}.post();
	}

	public boolean isEmpty(){
		return data.size() == 0;
	}

	public interface OnItemClickListener{
		public void onItemClicked(Context context, ArrayList<Status> data, int position);
	}

	public interface OnItemLongClickListener{
		public boolean onItemLongClicked(Context context, ArrayList<Status> data, int position);
	}

	public void setOnItemClickListener(OnItemClickListener listener){
		this.onItemClickListener = listener;
	}

	public void setOnItemLongClickListener(OnItemLongClickListener listener){
		this.onItemLongClickListener = listener;
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
