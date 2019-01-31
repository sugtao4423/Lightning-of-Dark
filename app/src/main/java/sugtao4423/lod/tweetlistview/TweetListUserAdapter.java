package sugtao4423.lod.tweetlistview;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;

import java.text.NumberFormat;
import java.util.ArrayList;

import sugtao4423.lod.App;
import sugtao4423.lod.R;
import sugtao4423.lod.userpage_fragment.UserPage;
import twitter4j.PagableResponseList;
import twitter4j.User;

public class TweetListUserAdapter extends RecyclerView.Adapter<TweetListUserAdapter.ViewHolder>{

    private LayoutInflater inflater;
    private ArrayList<User> data;
    private Context context;
    private App app;
    private Handler handler;

    public TweetListUserAdapter(Context context){
        this.inflater = LayoutInflater.from(context);
        this.data = new ArrayList<User>();
        this.context = context;
        this.app = (App)context.getApplicationContext();
        this.handler = new Handler();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position){
        return new ViewHolder(inflater.inflate(R.layout.list_item_tweet, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        if(!(data != null && data.size() > position && data.get(position) != null)){
            return;
        }

        holder.rt_icon.setVisibility(View.GONE);
        holder.rt_sn.setVisibility(View.GONE);
        holder.tweetImagesScroll.setVisibility(View.GONE);

        final User item = data.get(position);

        if(item.isProtected()){
            holder.protect.setTypeface(app.getFontAwesomeTypeface());
            holder.protect.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getOptions().getUserNameFontSize() - 3);
            holder.protect.setVisibility(View.VISIBLE);
        }else{
            holder.protect.setVisibility(View.GONE);
        }

        holder.icon.setImageUrl(item.getBiggerProfileImageURLHttps(), null, R.drawable.icon_loading);
        holder.name_sn.setText(item.getName() + " - @" + item.getScreenName());
        holder.content.setText(item.getDescription());
        String userCountsText = context.getString(R.string.param_user_count_detail,
                numberFormat(item.getStatusesCount()),
                numberFormat(item.getFavouritesCount()),
                numberFormat(item.getFriendsCount()),
                numberFormat(item.getFollowersCount())
        );
        holder.date.setText(userCountsText);

        holder.name_sn.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getOptions().getUserNameFontSize());
        holder.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getOptions().getContentFontSize());
        holder.date.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getOptions().getDateFontSize());

        holder.v.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v){
                Intent i = new Intent(context, UserPage.class);
                i.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, item.getScreenName());
                context.startActivity(i);
            }
        });
    }

    public String numberFormat(int num){
        return NumberFormat.getInstance().format(num);
    }

    @Override
    public int getItemCount(){
        if(data != null){
            return data.size();
        }else{
            return 0;
        }
    }

    public void addAll(final PagableResponseList<User> users){
        final int pos = data.size();
        data.addAll(users);
        handler.post(new Runnable(){

            @Override
            public void run(){
                notifyItemRangeInserted(pos, users.size());
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

    class ViewHolder extends RecyclerView.ViewHolder{

        View v;
        SmartImageView icon, rt_icon;
        TextView name_sn, content, date, rt_sn, protect;
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
            protect = (TextView)itemView.findViewById(R.id.UserProtected);
            tweetImagesScroll = (HorizontalScrollView)itemView.findViewById(R.id.tweet_images_scroll);
            tweetImagesLayout = (LinearLayout)itemView.findViewById(R.id.tweet_images_layout);
            v = itemView;
        }
    }

}
