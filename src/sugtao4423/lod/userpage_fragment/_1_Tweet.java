package sugtao4423.lod.userpage_fragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import sugtao4423.lod.ApplicationClass;
import sugtao4423.lod.ListViewListener;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.tweetlistview.EndlessScrollListener;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;

public class _1_Tweet extends Fragment{

	private SwipeRefreshLayout pulltoRefresh;
	private TweetListAdapter adapter;
	private boolean isAllLoaded;
	private ApplicationClass appClass;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = View.inflate(container.getContext(), R.layout.user_1, null);
		appClass = (ApplicationClass)container.getContext().getApplicationContext();

		TweetListView userTweet = (TweetListView)v.findViewById(R.id.UserPageList);

		adapter = new TweetListAdapter(container.getContext());
		adapter.setOnItemClickListener(new ListViewListener());
		adapter.setOnItemLongClickListener(new ListViewListener());
		userTweet.setAdapter(adapter);

		final EndlessScrollListener scrollListener = getLoadMoreListener(userTweet.getLinearLayoutManager());
		userTweet.addOnScrollListener(scrollListener);

		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserPagePull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh(){
				adapter.clear();
				isAllLoaded = false;
				loadTimeLine();
				scrollListener.resetState();
			}
		});
		return v;
	}

	public EndlessScrollListener getLoadMoreListener(LinearLayoutManager llm){
		return new EndlessScrollListener(llm){

			@Override
			public void onLoadMore(int current_page){
				if(!isAllLoaded)
					loadTimeLine();
			}
		};
	}

	public void loadTimeLine(){
		((UserPage)_1_Tweet.this.getActivity()).resetUser();
		new AsyncTask<Void, Void, ResponseList<Status>>(){
			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... params){
				try{
					if(adapter.getItemCount() > 0){
						long tweetId = adapter.getItem(adapter.getItemCount() - 1).getId();
						return appClass.getTwitter().getUserTimeline(appClass.getTargetScreenName(),
								new Paging(1, 50).maxId(tweetId - 1));
					}else{
						return appClass.getTwitter().getUserTimeline(appClass.getTargetScreenName(), new Paging(1, 50));
					}
				}catch(Exception e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> result){
				if(result != null) {
					adapter.addAll(result);
					if(appClass.getTarget() != null && appClass.getTarget().getStatusesCount() <= adapter.getItemCount())
						isAllLoaded = true;
				}else{
					new ShowToast(R.string.error_getTimeLine, getActivity(), 0);
				}
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
			}
		}.execute();
	}
}