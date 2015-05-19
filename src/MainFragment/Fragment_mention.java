package MainFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class Fragment_mention extends Fragment {
	
	private ListView mention;
	private CustomAdapter adapter;
	private ApplicationClass appClass;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_mention, null);
		mention = (ListView)v.findViewById(R.id.MentionLine);
		mention.setOnItemClickListener(new ListViewListener());
		mention.setOnItemLongClickListener(new ListViewListener());
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		adapter = appClass.getMentionAdapter();
		adapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged(){
				super.onChanged();
				moreMention();
				adapter.unregisterDataSetObserver(this);
			}
		});
		mention.setAdapter(adapter);
		return v;
	}
	
	public void moreMention(){
		ListView foot = new ListView(getActivity());
		foot.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Status s = (Status)mention.getItemAtPosition(mention.getAdapter().getCount() - 2);
				final long tweetId = s.getId();
				AsyncTask<Void, Void, ResponseList<twitter4j.Status>> task = new AsyncTask<Void, Void, ResponseList<twitter4j.Status>>(){
					@Override
					protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
						try{
							return appClass.getTwitter().getMentionsTimeline(new Paging(1, 50).maxId(tweetId - 1));
						}catch(Exception e){
							return null;
						}
					}
					protected void onPostExecute(ResponseList<twitter4j.Status> result){
						if(result != null){
							for(twitter4j.Status status : result)
								adapter.add(status);
						}else
							new ShowToast("メンション取得エラー", getActivity());
					}
				};
				task.execute();
			}
		});
		mention.addFooterView(foot);
	}
}