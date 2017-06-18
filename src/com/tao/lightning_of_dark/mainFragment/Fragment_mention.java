package com.tao.lightning_of_dark.mainFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;
import com.tao.lightning_of_dark.UiHandler;
import com.tao.lightning_of_dark.tweetlistview.EndlessScrollListener;
import com.tao.lightning_of_dark.tweetlistview.TweetListAdapter;
import com.tao.lightning_of_dark.tweetlistview.TweetListView;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Fragment_mention extends Fragment implements OnRefreshListener{

	private TweetListView list;
	private LinearLayoutManager llm;
	private SwipeRefreshLayout pulltoRefresh;
	private TweetListAdapter adapter;
	private ApplicationClass appClass;
	private Context context;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		context = container.getContext();
		View v = View.inflate(context, R.layout.fragment_list, null);
		appClass = (ApplicationClass)context.getApplicationContext();

		list = (TweetListView)v.findViewById(R.id.listLine);
		llm = list.getLinearLayoutManager();
		adapter = new TweetListAdapter(container.getContext());
		adapter.setOnItemClickListener(new ListViewListener());
		adapter.setOnItemLongClickListener(new ListViewListener());
		list.setAdapter(adapter);
		list.addOnScrollListener(getLoadMoreListener());

		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.ListPull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(this);
		onRefresh();
		return v;
	}

	public EndlessScrollListener getLoadMoreListener(){
		return new EndlessScrollListener(list.getLinearLayoutManager()){

			@Override
			public void onLoadMore(int current_page){
				AsyncTask<Void, Void, ResponseList<twitter4j.Status>> task = new AsyncTask<Void, Void, ResponseList<twitter4j.Status>>(){
					@Override
					protected ResponseList<twitter4j.Status> doInBackground(Void... params){
						try{
							long tweetId = adapter.getItem(adapter.getItemCount() - 1).getId();
							return appClass.getTwitter().getMentionsTimeline(new Paging(1, 50).maxId(tweetId - 1));
						}catch(Exception e){
							return null;
						}
					}

					@Override
					protected void onPostExecute(ResponseList<twitter4j.Status> result){
						if(result != null)
							adapter.addAll(result);
						else
							new ShowToast("メンション取得エラー", getActivity(), 0);
					}
				};
				if(adapter.getItemCount() > 30)
					task.execute();
			}
		};
	}

	@Override
	public void onRefresh(){
		adapter.clear();
		new AsyncTask<Void, Void, ResponseList<Status>>(){
			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... params){
				try{
					return appClass.getTwitter().getMentionsTimeline(new Paging(1, 50));
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> result){
				if(result != null)
					addAll(result);
				else
					new ShowToast("メンション取得エラー", context, 0);
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
			}
		}.execute();
	}

	public void insert(Status status){
		adapter.insertTop(status);
		new UiHandler(){

			@Override
			public void run(){
				if(llm.findFirstVisibleItemPosition() <= 1)
					list.smoothScrollToPosition(0);
			}
		}.post();
	}

	public void addAll(ResponseList<Status> status){
		adapter.addAll(status);
	}
}