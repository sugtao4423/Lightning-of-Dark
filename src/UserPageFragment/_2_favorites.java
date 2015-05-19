package UserPageFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class _2_favorites extends Fragment {
	
	private ListView UserFav, foot;
	private SwipeRefreshLayout PulltoRefresh;
	private CustomAdapter adapter;
	private ResponseList<twitter4j.Status> FavLine;
	private boolean AlreadyLoad;
	private long tweetId;
	private ApplicationClass appClass;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.user_2, null);
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		AlreadyLoad = false;
		
		UserFav = (ListView)v.findViewById(R.id.Userfav);
		UserFav.setOnItemClickListener(new ListViewListener());
		UserFav.setOnItemLongClickListener(new ListViewListener());
		
		adapter = new CustomAdapter(getActivity());
		//フッター生成
		addFooter();
		UserFav.setAdapter(adapter);
		
		PulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserFavPull);
		PulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
		PulltoRefresh.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				adapter.clear();
				AlreadyLoad = false;
				MentionLine();
			}
		});
		
		return v;
	}
	
	//フッター生成
	public void addFooter(){
		foot = new ListView(getActivity());
		foot.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				foot.setEnabled(false);
				MentionLine();
			}
		});
		UserFav.addFooterView(foot);
	}
	
	public void MentionLine(){
		if(AlreadyLoad){
			Status s = (Status)UserFav.getItemAtPosition(UserFav.getAdapter().getCount() - 2);
			tweetId = s.getId();
		}
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try{
					if(AlreadyLoad)
						FavLine = appClass.getTwitter().getFavorites(appClass.getTarget().getId(), new Paging(1, 50).maxId(tweetId - 1));
					else
						FavLine = appClass.getTwitter().getFavorites(appClass.getTarget().getId(), new Paging(1, 50));
					return true;
				}catch(Exception e){
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result){
					for(twitter4j.Status status : FavLine)
						adapter.add(status);
					if(!AlreadyLoad)
						AlreadyLoad = true;
				}else
					new ShowToast("ふぁぼ取得エラー", getActivity());
				PulltoRefresh.setRefreshing(false);
				PulltoRefresh.setEnabled(true);
				foot.setEnabled(true);
			}
		};
		task.execute();
	}
}