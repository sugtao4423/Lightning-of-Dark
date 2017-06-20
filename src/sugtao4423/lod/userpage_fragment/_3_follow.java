package sugtao4423.lod.userpage_fragment;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
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
import sugtao4423.lod.ApplicationClass;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.tweetlistview.EndlessScrollListener;
import sugtao4423.lod.tweetlistview.TweetListUserAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;

public class _3_follow extends Fragment{

	private SwipeRefreshLayout pulltoRefresh;
	private TweetListUserAdapter adapter;
	private long cursor;
	private boolean isAllLoaded;
	private ApplicationClass appClass;

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
		View v = View.inflate(container.getContext(), R.layout.user_1, null);
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		cursor = -1L;

		TweetListView userFollow = (TweetListView)v.findViewById(R.id.UserPageList);

		adapter = new TweetListUserAdapter(container.getContext());
		userFollow.setAdapter(adapter);

		final EndlessScrollListener scrollListener = getLoadMoreListener(userFollow.getLinearLayoutManager());
		userFollow.addOnScrollListener(scrollListener);

		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserPagePull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh(){
				adapter.clear();
				cursor = -1L;
				isAllLoaded = false;
				loadFollowLine();
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
					loadFollowLine();
			}
		};
	}

	public void loadFollowLine(){
		((UserPage)_3_follow.this.getActivity()).resetUser();
		new AsyncTask<Void, Void, PagableResponseList<User>>(){
			@Override
			protected PagableResponseList<User> doInBackground(Void... params){
				try{
					return appClass.getTwitter().getFriendsList(appClass.getTargetScreenName(), cursor);
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			public void onPostExecute(PagableResponseList<User> result){
				if(result != null) {
					adapter.addAll(result);
					cursor = result.getNextCursor();
					if(appClass.getTarget() != null && appClass.getTarget().getFriendsCount() <= adapter.getItemCount())
						isAllLoaded = true;
				}else{
					new ShowToast("フォローを取得できませんでした", getActivity(), 0);
				}
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
			}
		}.execute();
	}
}