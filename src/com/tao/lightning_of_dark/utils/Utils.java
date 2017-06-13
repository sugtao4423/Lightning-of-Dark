package com.tao.lightning_of_dark.utils;

import java.util.ArrayList;
import java.util.Collections;

import com.tao.lightning_of_dark.ApplicationClass;

import twitter4j.ExtendedMediaEntity;
import twitter4j.ExtendedMediaEntity.Variant;

public class Utils{

	public static String[] getVideoURLsSortByBitrate(ApplicationClass appClass, ExtendedMediaEntity[] exMentitys){
		String[] urls = new String[0];
		if(exMentitys != null && exMentitys.length > 0){
			for(ExtendedMediaEntity ex : exMentitys){
				if(isVideoOrGif(ex)){
					ArrayList<VideoURLs> videos = new ArrayList<VideoURLs>();
					for(Variant v : ex.getVideoVariants()){
						boolean find = false;
						if(appClass.getIsWebm() && v.getContentType().equals("video/webm"))
							find = true;
						else if(!appClass.getIsWebm() && v.getContentType().equals("video/mp4"))
							find = true;

						if(find)
							videos.add(new VideoURLs(v.getBitrate(), v.getUrl()));
					}
					if(videos.size() == 0){
						for(Variant v : ex.getVideoVariants()){
							boolean find = false;
							if(v.getContentType().equals("video/mp4"))
								find = true;
							else if(v.getContentType().equals("video/webm"))
								find = true;

							if(find)
								videos.add(new VideoURLs(v.getBitrate(), v.getUrl()));
						}
					}
					Collections.sort(videos);
					urls = new String[videos.size()];
					for(int i = 0; i < urls.length; i++)
						urls[i] = videos.get(i).url;
				}
			}
		}
		return urls;
	}

	static class VideoURLs implements Comparable<VideoURLs>{

		int bitrate;
		String url;

		public VideoURLs(int bitrate, String url){
			this.bitrate = bitrate;
			this.url = url;
		}

		@Override
		public int compareTo(VideoURLs another){
			return this.bitrate - another.bitrate;
		}
	}

	public static boolean isVideoOrGif(ExtendedMediaEntity ex){
		return (isVideo(ex) || isGif(ex));
	}

	public static boolean isVideo(ExtendedMediaEntity ex){
		return ex.getType().equals("video");
	}

	public static boolean isGif(ExtendedMediaEntity ex){
		return ex.getType().equals("animated_gif");
	}
}
