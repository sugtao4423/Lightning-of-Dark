package com.tao.lightning_of_dark.tweetlistview;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class TweetListView extends RecyclerView{
	
	private LinearLayoutManager llm;

	public TweetListView(Context context){
		super(context);
		setVerticalScrollBarEnabled(true);
		addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
		llm = new LinearLayoutManager(context);
		setLayoutManager(llm);
	}

	public TweetListView(Context context, AttributeSet attrs){
		super(context, attrs);
		setVerticalScrollBarEnabled(true);
		addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
		llm = new LinearLayoutManager(context);
		setLayoutManager(llm);
	}

	public LinearLayoutManager getLinearLayoutManager(){
		return llm;
	}

}
