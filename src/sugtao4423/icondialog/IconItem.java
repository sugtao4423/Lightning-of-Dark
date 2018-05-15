package sugtao4423.icondialog;

import android.graphics.Color;

public class IconItem{

	private char icon;
	private int iconColor;
	private String title;

	public IconItem(char icon, int iconColor, String title){
		this.icon = icon;
		this.iconColor = iconColor;
		this.title = title;
	}

	public IconItem(char icon, String title){
		this.icon = icon;
		this.iconColor = Color.BLACK;
		this.title = title;
	}

	public char getIcon(){
		return icon;
	}

	public int getIconColor(){
		return iconColor;
	}

	public String getTitle(){
		return title;
	}
}
