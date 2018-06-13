package sugtao4423.lod.userpage_fragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import sugtao4423.lod.App;
import sugtao4423.lod.ListViewListener;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.tweetlistview.EndlessScrollListener;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;

public class _2_favorites extends Fragment{

	private SwipeRefreshLayout pulltoRefresh;
	private TweetListAdapter adapter;
	private boolean isAllLoaded;
	private App app;
	private User targetUser;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = View.inflate(container.getContext(), R.layout.user_1, null);
		app = (App)container.getContext().getApplicationContext();

		TweetListView userFavorite = (TweetListView)v.findViewById(R.id.UserPageList);

		adapter = new TweetListAdapter(container.getContext());
		adapter.setOnItemClickListener(new ListViewListener());
		adapter.setOnItemLongClickListener(new ListViewListener());
		userFavorite.setAdapter(adapter);

		final EndlessScrollListener scrollListener = getLoadMoreListener(userFavorite.getLinearLayoutManager());
		userFavorite.addOnScrollListener(scrollListener);

		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserPagePull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh(){
				adapter.clear();
				isAllLoaded = false;
				loadMentionLine();
				scrollListener.resetState();
			}
		});
		return v;
	}

	public void setTargetUser(User targetUser){
		this.targetUser = targetUser;
	}

	public EndlessScrollListener getLoadMoreListener(LinearLayoutManager llm){
		return new EndlessScrollListener(llm){

			@Override
			public void onLoadMore(int current_page){
				if(!isAllLoaded)
					loadMentionLine();
			}
		};
	}

	public void loadMentionLine(){
		new AsyncTask<Void, Void, ResponseList<Status>>(){
			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... params){
				try{
					if(adapter.getItemCount() > 0){
						long tweetId = adapter.getItem(adapter.getItemCount() - 1).getId();
						return app.getTwitter().getFavorites(targetUser.getScreenName(),
								new Paging(1, 50).maxId(tweetId - 1));
					}else{
						return app.getTwitter().getFavorites(targetUser.getScreenName(), new Paging(1, 50));
					}
				}catch(Exception e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> result){
				if(result != null){
					adapter.addAll(result);
					if(targetUser != null && targetUser.getFavouritesCount() <= adapter.getItemCount())
						isAllLoaded = true;
				}else{
					new ShowToast(getContext().getApplicationContext(), R.string.error_getFav);
				}
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
			}
		}.execute();
	}
}