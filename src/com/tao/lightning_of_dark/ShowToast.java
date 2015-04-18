package com.tao.lightning_of_dark;

import android.content.Context;
import android.widget.Toast;

public class ShowToast {

	public ShowToast(String text,Context context) {
		super();
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
}
