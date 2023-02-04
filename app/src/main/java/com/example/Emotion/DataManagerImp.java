package com.example.Emotion;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Response;

public interface DataManagerImp {

    Observable<Response<ImgResult>> uploadImg(MultipartBody.Part images);
}
