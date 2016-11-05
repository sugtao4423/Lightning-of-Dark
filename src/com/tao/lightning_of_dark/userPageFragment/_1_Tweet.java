package com.tao.lightning_of_dark.userPageFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;

public class _1_Tweet extends Fragment{

	private ListView foot;
	private SwipeRefreshLayout pulltoRefresh;
	private CustomAdapter adapter;
	private boolean alreadyLoad;
	private long tweetId;
	private ApplicationClass appClass;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.user_1, null);
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		alreadyLoad = false;
		// 通常のListViewSet
		ListView userTweet = (ListView)v.findViewById(R.id.UserPageList);
		userTweet.setOnItemClickListener(new ListViewListener());
		userTweet.setOnItemLongClickListener(new ListViewListener());
		// ここまで
		adapter = new CustomAdapter(getActivity());
		// フッター生成
		addFooter(userTweet);
		userTweet.setAdapter(adapter);

		// PulltoRefresh
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

	// フッター生成
	public void addFooter(ListView list){
		foot = new ListView(getActivity());
		foot.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				foot.setEnabled(false);
				loadTimeLine();
			}
		});
		list.addFooterView(foot);
	}

	// なんか色々取得 //自分でもよくわからず組んだ is 屑
	public void loadTimeLine(){
		if(alreadyLoad)
			tweetId = adapter.getItem(adapter.getCount() - 1).getId();
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
				foot.setEnabled(true);
			}
		}.execute();
	}
}