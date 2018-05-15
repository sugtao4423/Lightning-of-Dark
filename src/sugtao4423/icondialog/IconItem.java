package sugtao4423.icondialog;

import android.graphics.Color;

public class IconItem{

	public static final char ICON_BOMB = 0xf1e2;
	public static final char ICON_SEARCH = 0xf002;
	public static final char ICON_REFRESH = 0xf021;
	public static final char ICON_USER = 0xf007;
	public static final char ICON_COG = 0xf013;

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
