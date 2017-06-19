package sugtao4423.lod.main_fragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import sugtao4423.lod.ApplicationClass;
import sugtao4423.lod.ListViewListener;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.UiHandler;
import sugtao4423.lod.tweetlistview.EndlessScrollListener;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;

public class Fragment_home extends Fragment{

	private TweetListView list;
	private LinearLayoutManager llm;
	private TweetListAdapter adapter;
	private ApplicationClass appClass;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		View v = View.inflate(container.getContext(), R.layout.fragment_list, null);
		list = (TweetListView)v.findViewById(R.id.listLine);
		llm = list.getLinearLayoutManager();
		adapter = new TweetListAdapter(container.getContext());
		adapter.setOnItemClickListener(new ListViewListener());
		adapter.setOnItemLongClickListener(new ListViewListener());
		list.setAdapter(adapter);
		list.addOnScrollListener(getLoadMoreListener());
		return list;
	}

	public void addAll(ResponseList<Status> status){
		adapter.addAll(status);
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

	public EndlessScrollListener getLoadMoreListener(){
		return new EndlessScrollListener(llm){

			@Override
			public void onLoadMore(int current_page){
				AsyncTask<Void, Void, ResponseList<twitter4j.Status>> task = new AsyncTask<Void, Void, ResponseList<twitter4j.Status>>(){
					@Override
					protected ResponseList<twitter4j.Status> doInBackground(Void... params){
						try{
							long tweetId = adapter.getItem(adapter.getItemCount() - 1).getId();
							return appClass.getTwitter().getHomeTimeline(new Paging(1, 50).maxId(tweetId - 1));
						}catch(Exception e){
							return null;
						}
					}

					@Override
					protected void onPostExecute(ResponseList<twitter4j.Status> result){
						if(result != null)
							adapter.addAll(result);
						else
							new ShowToast("タイムライン取得エラー", getActivity(), 0);
					}
				};
				if(adapter.getItemCount() > 30)
					task.execute();
			}
		};
	}
}
