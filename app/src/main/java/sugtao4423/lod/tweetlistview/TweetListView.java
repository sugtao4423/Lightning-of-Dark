package sugtao4423.lod.tweetlistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.util.AttributeSet;
import android.view.View;

import sugtao4423.lod.App;
import sugtao4423.lod.R;
import twitter4j.Status;

public class TweetListView extends RecyclerView{

    private LinearLayoutManager llm;

    public TweetListView(Context context){
        super(context);
        setVerticalScrollBarEnabled(true);
        addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        addItemDecoration(new AlternatelyColor(context));
        llm = new LinearLayoutManager(context);
        setLayoutManager(llm);
    }

    public TweetListView(Context context, AttributeSet attrs){
        super(context, attrs);
        setVerticalScrollBarEnabled(true);
        addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        addItemDecoration(new AlternatelyColor(context));
        llm = new LinearLayoutManager(context);
        setLayoutManager(llm);
    }

    public LinearLayoutManager getLinearLayoutManager(){
        return llm;
    }

}

class AlternatelyColor extends ItemDecoration{

    private App app;

    public AlternatelyColor(Context context){
        this.app = (App)context.getApplicationContext();
    }

    @SuppressLint("ResourceType")
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state){
        super.getItemOffsets(outRect, view, parent, state);
        int pos = parent.getChildAdapterPosition(view);
        Adapter<?> adapter = parent.getAdapter();
        if(adapter instanceof TweetListAdapter){
            Status item = ((TweetListAdapter)adapter).getItem(pos);
            if(item == null){
                return;
            }
            if(item.isRetweetedByMe()){
                view.setBackgroundResource(R.xml.retweeted_by_me);
            }else if(item.isRetweet()){
                view.setBackgroundResource(R.xml.retweet);
            }else if(item.getUser().getScreenName().equals(app.getCurrentAccount().getScreenName())){
                view.setBackgroundResource(R.xml.same_my_screenname);
            }else if(app.getMentionPattern().matcher(item.getText()).find()){
                view.setBackgroundResource(R.xml.mention);
            }else{
                setAlternately(pos, view);
            }
        }else if(adapter instanceof TweetListUserAdapter){
            setAlternately(pos, view);
        }
    }

    @SuppressLint("ResourceType")
    public void setAlternately(int pos, View view){
        view.setBackgroundResource(pos % 2 == 0 ? R.xml.position0 : R.xml.position1);
    }

}
