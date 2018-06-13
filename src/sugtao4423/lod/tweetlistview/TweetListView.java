package sugtao4423.lod.tweetlistview;

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

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state){
		super.getItemOffsets(outRect, view, parent, state);
		int pos = parent.getChildAdapterPosition(view);
		Adapter<?> adapter = parent.getAdapter();
		if(adapter instanceof TweetListAdapter){
			Status item = ((TweetListAdapter)adapter).getItem(pos);
			if(item == null)
				return;
			if(item.isRetweetedByMe()){
				view.setBackgroundResource(R.drawable.retweeted_by_me);
			}else if(item.isRetweet()){
				view.setBackgroundResource(R.drawable.retweet);
			}else if(item.getUser().getScreenName().equals(app.getMyScreenName())){
				view.setBackgroundResource(R.drawable.same_my_screenname);
			}else if(app.getMentionPattern().matcher(item.getText()).find()){
				view.setBackgroundResource(R.drawable.mention);
			}else{
				setAlternately(pos, view);
			}
		}else if(adapter instanceof TweetListUserAdapter){
			setAlternately(pos, view);
		}
	}

	public void setAlternately(int pos, View view){
		view.setBackgroundResource(pos % 2 == 0 ? R.drawable.position0 : R.drawable.position1);
	}

}
