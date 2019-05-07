package sugtao4423.lod.utils;

import java.util.regex.Pattern;

public class Regex{

    public static final Pattern media_image = Pattern.compile("http(s)?://pbs.twimg.com/media/");
    public static final Pattern media_video = Pattern.compile("http(s)?://video.twimg.com/ext_tw_video/[0-9]+/(pu|pr)/vid/.+/.+(.mp4|.webm)");
    public static final Pattern media_gif = Pattern.compile("http(s)?://pbs.twimg.com/tweet_video/");
    public static final Pattern statusUrl = Pattern.compile("http(s)?://twitter.com/(i/web|[0-9a-zA-Z_]+)/status/([0-9]+)");
    public static final int statusUrlStatusIdGroup = 3;
    public static final Pattern shareUrl = Pattern.compile("http(s)?://twitter.com/intent/tweet\\?.+");
    public static final Pattern userUrl = Pattern.compile("http(s)?://twitter.com/([0-9a-zA-Z_]+)");
    public static final int userUrlScreenNameGroup = 2;
    public static final Pattern userIconUrl = Pattern.compile("^http(s)?://pbs.twimg.com/.+/+(.+)(\\..+)$");
    public static final int userIconUrlFileNameGroup = 2;
    public static final int userIconUrlDotExtGroup = 3;
    public static final Pattern userBannerUrl = Pattern.compile("^http(s)?://pbs.twimg.com/profile_banners/[0-9]+/([0-9]+)/");
    public static final int userBannerUrlFileNameGroup = 2;
    public static final Pattern userAndAnyUrl = Pattern.compile("@[0-9a-zA-Z_]+|(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.DOTALL);

}
