package com.tao.lightning_of_dark.tweetlistview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener{

	int visibleThreshold = 5;
	int firstVisibleItem, visibleItemCount, totalItemCount;
	private int previousTotal = 0;
	private boolean loading = true;
	private int current_page = 1;

	private LinearLayoutManager mLinearLayoutManager;

	public EndlessScrollListener(LinearLayoutManager linearLayoutManager){
		this.mLinearLayoutManager = linearLayoutManager;
	}

	public void setVisibleThreshold(int visibleThreshold){
		this.visibleThreshold = visibleThreshold;
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy){
		super.onScrolled(recyclerView, dx, dy);

		visibleItemCount = recyclerView.getChildCount();
		totalItemCount = mLinearLayoutManager.getItemCount();
		firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

		if(loading){
			if(totalItemCount > previousTotal){
				loading = false;
				previousTotal = totalItemCount;
			}
		}

		if(!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)){
			current_page++;
			onLoadMore(current_page);
			loading = true;
		}
	}

	public abstract void onLoadMore(int current_page);
}