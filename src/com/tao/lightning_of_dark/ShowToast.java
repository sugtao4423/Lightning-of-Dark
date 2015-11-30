package com.tao.lightning_of_dark;

import com.loopj.android.image.SmartImageView;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ShowToast{

	private View toastView;
	private TextView main_message, tweet;
	private SmartImageView icon;

	public ShowToast(String text, Context context, int toastType){
		ApplicationClass appClass = (ApplicationClass)context.getApplicationContext();

		toastView = appClass.getToastView();
		main_message = appClass.getToast_Main_Message();
		tweet = appClass.getToast_Tweet();
		icon = appClass.getToast_Icon();

		// 0:normal
		switch(toastType){
		case 0:
			normalToast(text, context);
			break;
		}
	}

	public void normalToast(String text, Context context){
		Toast toast = new Toast(context);

		main_message.setText(text);
		main_message.setTextColor(Color.parseColor("#ffffff"));
		main_message.setPadding(3, 2, 3, 2);

		tweet.setVisibility(View.GONE);
		icon.setVisibility(View.GONE);

		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(toastView);
		toast.show();
	}
}
