package com.tao.lightning_of_dark.userPageFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;
import com.tao.lightning_of_dark.tweetlistview.TweetListAdapter;
import com.tao.lightning_of_dark.tweetlistview.TweetListView;

public class _1_Tweet extends Fragment{

	private SwipeRefreshLayout pulltoRefresh;
	private TweetListAdapter adapter;
	private boolean alreadyLoad;
	private long tweetId;
	private ApplicationClass appClass;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = View.inflate(container.getContext(), R.layout.user_1, null);
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		alreadyLoad = false;
		TweetListView userTweet = (TweetListView)v.findViewById(R.id.UserPageList);
		adapter = new TweetListAdapter(container.getContext());
		adapter.setOnItemClickListener(new ListViewListener());
		adapter.setOnItemLongClickListener(new ListViewListener());
		userTweet.setAdapter(adapter);

		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserPagePull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh(){
				adapter.clear();
				alreadyLoad = false;
				loadTimeLine();
			}
		});
		return v;
	}

	// なんか色々取得 //自分でもよくわからず組んだ is 屑
	public void loadTimeLine(){
		if(alreadyLoad)
			tweetId = adapter.getItem(adapter.getItemCount() - 1).getId();
		((UserPage)_1_Tweet.this.getActivity()).resetUser();
		new AsyncTask<Void, Void, ResponseList<Status>>(){
			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... params){
				try{
					if(alreadyLoad)
						return appClass.getTwitter().getUserTimeline(appClass.getTargetScreenName(),
								new Paging(1, 50).maxId(tweetId - 1));
					else
						return appClass.getTwitter().getUserTimeline(appClass.getTargetScreenName(), new Paging(1, 50));
				}catch(Exception e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> result){
				if(result != null) {
					adapter.addAll(result);
					alreadyLoad = true;
				}else{
					new ShowToast("タイムラインを取得できませんでした", getActivity(), 0);
				}
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
			}
		}.execute();
	}
}