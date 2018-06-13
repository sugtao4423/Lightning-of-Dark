package sugtao4423.lod.main_fragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import sugtao4423.lod.App;
import sugtao4423.lod.Keys;
import sugtao4423.lod.ListViewListener;
import sugtao4423.lod.R;
import sugtao4423.lod.Settings;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.tweetlistview.EndlessScrollListener;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;

public class Fragment_List extends Fragment{

	private TweetListView list;
	private TweetListAdapter adapter;
	private SwipeRefreshLayout pulltoRefresh;
	private int listIndex;

	public Fragment_List(int index){
		listIndex = index;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
		View v = View.inflate(container.getContext(), R.layout.fragment_list, null);
		final App app = (App)container.getContext().getApplicationContext();

		list = (TweetListView)v.findViewById(R.id.listLine);
		adapter = app.getListAdapters()[listIndex];
		adapter.setOnItemClickListener(new ListViewListener());
		adapter.setOnItemLongClickListener(new ListViewListener());
		list.setAdapter(adapter);

		final EndlessScrollListener scrollListener = getLoadMoreListener(container.getContext(), list.getLinearLayoutManager());
		list.addOnScrollListener(scrollListener);

		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.ListPull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh(){
				adapter.clear();
				boolean[] tmp = app.getListAlreadyLoad();
				tmp[listIndex] = false;
				app.setListAlreadyLoad(tmp);
				getList(container.getContext());
				scrollListener.resetState();
			}
		});
		return v;
	}

	public EndlessScrollListener getLoadMoreListener(final Context context, LinearLayoutManager llm){
		return new EndlessScrollListener(llm){

			@Override
			public void onLoadMore(int current_page){
				if(adapter.getItemCount() > 30)
					getList(context);
			}
		};
	}

	public void getList(Context context){
		final App app = (App)context.getApplicationContext();
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if(pref.getBoolean(Keys.SHOW_LIST, false)){
			String[] listId_str = pref.getString(Keys.SELECT_LIST_IDS, null).split(",", 0);
			final long[] listId = new long[listId_str.length];
			for(int i = 0; i < listId_str.length; i++)
				listId[i] = Long.parseLong(listId_str[i]);
			if(listId[listIndex] != -1){
				new AsyncTask<Void, Void, ResponseList<Status>>(){
					@Override
					protected ResponseList<twitter4j.Status> doInBackground(Void... params){
						try{
							if(app.getListAlreadyLoad()[listIndex]){
								long tweetId = adapter.getItem(adapter.getItemCount() - 1).getId();
								return app.getTwitter().getUserListStatuses(listId[listIndex],
										new Paging(1, 50).maxId(tweetId - 1));
							}else{
								return app.getTwitter().getUserListStatuses(listId[listIndex], new Paging(1, 50));
							}
						}catch(TwitterException e){
							return null;
						}
					}

					@Override
					protected void onPostExecute(ResponseList<twitter4j.Status> result){
						if(result != null){
							adapter.addAll(result);
							if(!app.getListAlreadyLoad()[listIndex]){
								boolean[] tmp = app.getListAlreadyLoad();
								tmp[listIndex] = true;
								app.setListAlreadyLoad(tmp);
							}
						}else{
							new ShowToast(getContext().getApplicationContext(), R.string.error_getList);
						}
						pulltoRefresh.setRefreshing(false);
						pulltoRefresh.setEnabled(true);
					}
				}.execute();
			}else{
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
				new AlertDialog.Builder(getActivity())
				.setTitle("リストを選択してください")
				.setMessage("リストが未選択です。\n設定ページを開きますか？")
				.setPositiveButton("開く", new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						startActivity(new Intent(getActivity(), Settings.class));
					}
				}).setNegativeButton("キャンセル", null).show();
			}
		}
	}
}