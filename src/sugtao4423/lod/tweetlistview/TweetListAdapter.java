package sugtao4423.lod.tweetlistview;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.loopj.android.image.SmartImageView;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import sugtao4423.lod.ApplicationClass;
import sugtao4423.lod.R;
import sugtao4423.lod.Show_Video;
import sugtao4423.lod.swipe_image_viewer.ImageFragmentActivity;
import sugtao4423.lod.userpage_fragment.UserPage;
import sugtao4423.lod.utils.Utils;
import twitter4j.MediaEntity;
import twitter4j.ResponseList;
import twitter4j.Status;

public class TweetListAdapter extends RecyclerView.Adapter<TweetListAdapter.ViewHolder>{

	private LayoutInflater inflater;
	private ArrayList<Status> data;
	private Context context;
	private ApplicationClass appClass;
	private Handler handler;
	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;

	public TweetListAdapter(Context context){
		this.inflater = LayoutInflater.from(context);
		this.data = new ArrayList<Status>();
		this.context = context;
		this.appClass = (ApplicationClass)context.getApplicationContext();
		this.handler = new Handler();
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

		// アイコン、名前、スクリーンネーム、タイムスタンプ、クライアント
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss" + (appClass.getOption_millisecond() ? ".SSS" : ""), Locale.getDefault());
		if(item.isRetweet()){
			holder.rt_icon.setVisibility(View.VISIBLE);
			holder.rt_sn.setVisibility(View.VISIBLE);
			String date = dateFormat.format(appClass.getOption_millisecond() ?
					new Date((item.getRetweetedStatus().getId() >> 22) + 1288834974657L) :
						item.getRetweetedStatus().getCreatedAt());
			holder.date.setText(date + "  Retweeted by ");
			holder.rt_icon.setImageUrl(item.getUser().getProfileImageURL(), null, R.drawable.ic_action_refresh);
			holder.rt_sn.setText("@" + item.getUser().getScreenName());
		}else{
			holder.rt_icon.setVisibility(View.GONE);
			holder.rt_sn.setVisibility(View.GONE);
			String date = dateFormat.format(appClass.getOption_millisecond() ?
					new Date((item.getId() >> 22) + 1288834974657L) :
						item.getCreatedAt());
			holder.date.setText(date + "  via " + item.getSource().replaceAll("<.+?>", ""));
		}

		holder.name_sn.setText(origStatus.getUser().getName() + " - @" + origStatus.getUser().getScreenName());
		holder.content.setText(origStatus.getText());
		holder.icon.setImageUrl(origStatus.getUser().getBiggerProfileImageURL(), null, R.drawable.ic_action_refresh);

		holder.icon.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Intent intent = new Intent(context, UserPage.class);
				intent.putExtra(UserPage.INTENT_EXTRA_KEY_USER_OBJECT, origStatus.getUser());
				context.startActivity(intent);
			}
		});

		holder.v.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				onItemClickListener.onItemClicked(context, data, holder.getLayoutPosition());
			}
		});
		holder.v.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v){
				return onItemLongClickListener.onItemLongClicked(context, data, holder.getLayoutPosition());
			}
		});

		MediaEntity[] mentitys = origStatus.getMediaEntities();
		if(mentitys != null && mentitys.length > 0){
			holder.tweetImagesScroll.setVisibility(View.VISIBLE);
			holder.tweetImagesLayout.removeAllViews();
			for(int i = 0; i < mentitys.length; i++){
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 200);
				if(holder.tweetImagesLayout.getChildCount() != 0)
					params.setMargins(8, 0, 0, 0);
				SmartImageView child = new SmartImageView(context);
				child.setLayoutParams(params);
				child.setMaxHeight(200);
				child.setAdjustViewBounds(true);

				if(Utils.isVideoOrGif(mentitys[i])){
					ImageView play = new ImageView(context);
					play.setImageResource(R.drawable.video_play);
					FrameLayout.LayoutParams playParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
							FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
					play.setLayoutParams(playParams);
					FrameLayout fl = new FrameLayout(context);
					fl.addView(child);
					fl.addView(play);
					holder.tweetImagesLayout.addView(fl);

					final boolean isGif = Utils.isGif(mentitys[i]);
					final String[] videoUrl = Utils.getVideoURLsSortByBitrate(appClass, mentitys);
					child.setImageUrl(mentitys[i].getMediaURL() + ":small", null, R.drawable.ic_action_refresh);
					child.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v){
							Intent intent = new Intent(context, Show_Video.class);
							intent.putExtra(Show_Video.INTENT_EXTRA_KEY_URL, videoUrl[videoUrl.length - 1]);
							if(isGif)
								intent.putExtra(Show_Video.INTENT_EXTRA_KEY_TYPE, Show_Video.TYPE_GIF);
							else
								intent.putExtra(Show_Video.INTENT_EXTRA_KEY_TYPE, Show_Video.TYPE_VIDEO);
							context.startActivity(intent);
						}
					});
				}else{
					holder.tweetImagesLayout.addView(child);
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
			}
			holder.tweetImagesScroll.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event){
					HorizontalScrollView sv = (HorizontalScrollView)v;
					if(sv.getChildAt(0).getWidth() > sv.getWidth())
						return v.onTouchEvent(event);
					else
						return holder.v.onTouchEvent(event);
				}
			});
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
		if(position < 0)
			return null;
		else
			return data.get(position);
	}

	public void add(final Status status){
		handler.post(new Runnable(){

			@Override
			public void run(){
				data.add(status);
				notifyItemInserted(data.size() - 1);
			}
		});
	}

	public void addAll(final ResponseList<Status> status){
		final int pos = data.size();
		data.addAll(status);
		handler.post(new Runnable(){

			@Override
			public void run(){
				notifyItemRangeInserted(pos, status.size());
			}
		});
	}

	public void insertTop(final Status item){
		handler.post(new Runnable(){

			@Override
			public void run(){
				data.add(0, item);
				notifyItemInserted(0);
			}
		});
	}

	public void clear(){
		final int size = data.size();
		data.clear();
		handler.post(new Runnable(){

			@Override
			public void run(){
				notifyItemRangeRemoved(0, size);
			}
		});
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
