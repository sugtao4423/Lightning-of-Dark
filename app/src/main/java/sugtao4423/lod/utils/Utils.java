package sugtao4423.lod.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sugtao4423.lod.App;
import twitter4j.MediaEntity;
import twitter4j.MediaEntity.Variant;

public class Utils{

    public static String[] getVideoURLsSortByBitrate(App app, MediaEntity[] mentitys){
        String[] urls = new String[0];
        if(mentitys != null && mentitys.length > 0){
            for(MediaEntity media : mentitys){
                if(isVideoOrGif(media)){
                    ArrayList<VideoURLs> videos = new ArrayList<VideoURLs>();
                    for(Variant v : media.getVideoVariants()){
                        boolean find = false;
                        if(app.getOptions().getIsWebm() && v.getContentType().equals("video/webm")){
                            find = true;
                        }else if(!app.getOptions().getIsWebm() && v.getContentType().equals("video/mp4")){
                            find = true;
                        }

                        if(find){
                            videos.add(new VideoURLs(v.getBitrate(), v.getUrl()));
                        }
                    }
                    if(videos.size() == 0){
                        for(Variant v : media.getVideoVariants()){
                            boolean find = false;
                            if(v.getContentType().equals("video/mp4")){
                                find = true;
                            }else if(v.getContentType().equals("video/webm")){
                                find = true;
                            }

                            if(find){
                                videos.add(new VideoURLs(v.getBitrate(), v.getUrl()));
                            }
                        }
                    }
                    Collections.sort(videos);
                    urls = new String[videos.size()];
                    for(int i = 0; i < urls.length; i++){
                        urls[i] = videos.get(i).url;
                    }
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

    public static boolean isVideoOrGif(MediaEntity ex){
        return (isVideo(ex) || isGif(ex));
    }

    public static boolean isVideo(MediaEntity ex){
        return ex.getType().equals("video");
    }

    public static boolean isGif(MediaEntity ex){
        return ex.getType().equals("animated_gif");
    }

    public static int convertDpToPx(Context context, int dp){
        float d = context.getResources().getDisplayMetrics().density;
        return (int)((dp * d) + 0.5);
    }

    public static String implode(String[] arr){
        return implode(Arrays.asList(arr));
    }

    public static String implode(long[] arr){
        ArrayList<String> strarr = new ArrayList<String>();
        for(long l : arr){
            strarr.add(String.valueOf(l));
        }
        return implode(strarr);
    }

    public static String implode(Long[] arr){
        ArrayList<String> strarr = new ArrayList<String>();
        for(Long l : arr){
            strarr.add(String.valueOf(l));
        }
        return implode(strarr);
    }

    public static String implode(List<String> list){
        return implode(list, ",");
    }

    private static <T> String implode(List<T> list, String glue){
        if(list.size() < 1){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(T e : list){
            sb.append(glue).append(e);
        }
        return sb.substring(glue.length());
    }

}
