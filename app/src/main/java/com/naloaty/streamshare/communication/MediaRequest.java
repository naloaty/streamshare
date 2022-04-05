package com.naloaty.streamshare.communication;

import com.naloaty.streamshare.service.Requests;
import com.naloaty.streamshare.database.entity.Album;
import com.naloaty.streamshare.media.Media;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * This class is used to send and receive all things related to media-files.
 * @see CommunicationHelper
 */
public interface MediaRequest {
    /*
     * media/albums
     */
    @GET(Requests.MEDIA + "/" + Requests.ALBUMS)
    Call<List<Album>> getAlbumsList();

    /*
     * media/medialist?albumId=***
     */
    @GET(Requests.MEDIA + "/" + Requests.MEDIA_LIST)
    Call<List<Media>> getMediaList(@Query(Requests.ALBUM_ID) String albumId);
}
