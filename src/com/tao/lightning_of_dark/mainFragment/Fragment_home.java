package com.tao.lightning_of_dark.mainFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.ShowToast;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Fragment_home extends Fragment{

	private ListView list;
	private CustomAdapter adapter;
	private ApplicationClass appClass;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		list = new ListView(container.getContext());
		list.setDivider(new ColorDrawable(Color.parseColor("#bbbbbb")));
		list.setDividerHeight(3);
		list.setOnItemClickListener(new ListViewListener(true));
		list.setOnItemLongClickListener(new ListViewListener(true));

		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		adapter = new CustomAdapter(container.getContext());
		adapter.registerDataSetObserver(new DataSetObserver(){
			@Override
			public void onChanged(){
				super.onChanged();
				moreHome();
				adapter.unregisterDataSetObserver(this);
			}
		});
		list.setAdapter(adapter);
		return list;
	}

	public void moreHome(){
		ListView foot = new ListView(getActivity());
		foot.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				Status s = (Status)list.getItemAtPosition(list.getAdapter().getCount() - 2);
				final long tweetId = s.getId();
				new AsyncTask<Void, Void, ResponseList<twitter4j.Status>>(){
					@Override
					protected ResponseList<twitter4j.Status> doInBackground(Void... params){
						try{
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
				}.execute();
			}
		});
		list.addFooterView(foot);
	}

	public void insert(final Status status){
		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params){
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result){
				if(list.getChildCount() != 0) {
					int pos = list.getFirstVisiblePosition();
					int top = list.getChildAt(0).getTop();
					adapter.insert(status, 0);
					if(pos == 0 && top == 0)
						list.setSelectionFromTop(pos, 0);
					else
						list.setSelectionFromTop(pos + 1, top);
				}
			}
		}.execute();
	}

	public void add(Status status){
		adapter.add(status);
	}
}
