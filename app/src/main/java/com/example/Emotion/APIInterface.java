package com.example.Emotion;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Response;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface APIInterface {
    @Multipart
    @POST("predict_it")
    Observable<Response<ImgResult>> uploadImg(
            @Part MultipartBody.Part file);
    );
}
