package com.tao.lightning_of_dark.userPageFragment;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;
import com.tao.lightning_of_dark.tweetlistview.EndlessScrollListener;
import com.tao.lightning_of_dark.tweetlistview.TweetListUserAdapter;
import com.tao.lightning_of_dark.tweetlistview.TweetListView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class _3_follow extends Fragment{

	private LinearLayoutManager llm;
	private SwipeRefreshLayout pulltoRefresh;
	private TweetListUserAdapter adapter;
	private long cursor;
	private ApplicationClass appClass;

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
		View v = View.inflate(container.getContext(), R.layout.user_1, null);
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		cursor = -1L;

		TweetListView userFollow = (TweetListView)v.findViewById(R.id.UserPageList);
		llm = userFollow.getLinearLayoutManager();
		adapter = new TweetListUserAdapter(container.getContext());
		userFollow.setAdapter(adapter);
		userFollow.addOnScrollListener(getLoadMoreListener());

		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserPagePull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh(){
				adapter.clear();
				cursor = -1L;
				loadFollowLine();
			}
		});
		return v;
	}

	public EndlessScrollListener getLoadMoreListener(){
		return new EndlessScrollListener(llm){

			@Override
			public void onLoadMore(int current_page){
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
				}else{
					new ShowToast("フォローを取得できませんでした", getActivity(), 0);
				}
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
			}
		}.execute();
	}
}