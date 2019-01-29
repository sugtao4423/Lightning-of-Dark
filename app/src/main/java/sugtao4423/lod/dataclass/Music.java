package sugtao4423.lod.dataclass;

public class Music{

    private String artist;
    private String album;
    private String track;

    public Music(String artist, String album, String track){
        this.artist = artist;
        this.album = album;
        this.track = track;
    }

    public String getArtist(){
        return artist;
    }

    public String getAlbum(){
        return album;
    }

    public String getTrack(){
        return track;
    }

}
