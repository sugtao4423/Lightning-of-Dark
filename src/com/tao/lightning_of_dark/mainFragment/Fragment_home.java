package com.tao.lightning_of_dark.mainFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;

import java.util.LinkedList;
import java.util.Queue;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.ShowToast;
import com.tao.lightning_of_dark.UiHandler;
import android.annotation.SuppressLint;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Fragment_home extends Fragment implements OnScrollListener, OnTouchListener{

	private ListView list;
	private CustomAdapter adapter;
	private ApplicationClass appClass;

	private boolean stopInsertByKey;
	private Queue<Status> mStatusQueue;
	private boolean smoothScrollToTop;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		list = new ListView(container.getContext());
		list.setDivider(new ColorDrawable(Color.parseColor("#bbbbbb")));
		list.setDividerHeight(3);
		list.setOnItemClickListener(new ListViewListener(true));
		list.setOnItemLongClickListener(new ListViewListener(true));
		list.setOnScrollListener(this);
		list.setOnTouchListener(this);

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

		stopInsertByKey = false;
		mStatusQueue = new LinkedList<>();
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

	public void add(Status status){
		adapter.add(status);
	}

	public void insert(final Status status){
		new UiHandler(){

			@Override
			public void run(){
				if(stopInsertByKey) {
					// キューに貯める
					mStatusQueue.offer(status);
				}else{
					adapter.insert(status, 0);

					if(list != null && list.getChildAt(1) != null && list.getCount() > 1) {
						if(list.getFirstVisiblePosition() == 0)
							smoothScrollToTop = true;

						list.setSelectionFromTop(list.getFirstVisiblePosition() + 1, list.getChildAt(0).getTop());

						if(smoothScrollToTop)
							list.smoothScrollToPosition(0);
					}
				}
			}
		}.post();
	}

	public void releaseQueue(){
		// キューを開放する
		stopInsertByKey = false;
		if(mStatusQueue.size() > 0) {
			while(mStatusQueue.peek() != null)
				insert(mStatusQueue.poll());
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState){
		if(scrollState == SCROLL_STATE_IDLE) {
			if(view.getFirstVisiblePosition() == 0)
				releaseQueue();
			else
				stopInsertByKey = true;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event){
		switch(event.getAction()){
		case MotionEvent.ACTION_MOVE:
			smoothScrollToTop = false;
			break;
		}
		return false;
	}
}
