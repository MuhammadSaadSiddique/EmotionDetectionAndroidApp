package com.example.Emotion;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import retrofit2.Response;

public class DataManager implements DataManagerImp{
    private static DataManager mDataManager;

    private DataManager() {

    }

    public static DataManager getInstance() {
        if (mDataManager == null) {
            mDataManager = new DataManager();
        }
        return mDataManager;
    }
   /* @Override
    public Observable<Response<ImgResult>> uploadImg(MultipartBody.Part images) {
        return RetrofitClientInstance.getServiceInstance().uploadImg( images).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }*/
}
