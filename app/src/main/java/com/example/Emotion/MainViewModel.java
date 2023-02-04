package com.example.Emotion;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import okhttp3.MultipartBody;
import retrofit2.Response;

public class MainViewModel  extends BaseViewModel {
    public MutableLiveData<ApiResponse> mUpdateUserResponse = new MutableLiveData<>();

    public void uploadImg(MultipartBody.Part images) {
        mDataManager.uploadImg(images).doOnSubscribe(disposable -> {
            mUpdateUserResponse.setValue(ApiResponse.Companion.loading());
        }).subscribe(new CustomObserver<Response<ImgResult>>() {
            @Override
            public void onSuccess(@NonNull Response<ImgResult> response) {
                if (response.isSuccessful()) {
                    mUpdateUserResponse.setValue(ApiResponse.Companion.success(response.body()));
                } else if (response.code() == 401) {
                    mUpdateUserResponse.setValue(ApiResponse.Companion.error(
                            new CustomError(
                                    response.message(),response.code())));
                    Log.d("TAG", "error: " + response.code());
                } else if (response.code() == 403) {
                    mUpdateUserResponse.setValue(ApiResponse.Companion.error(
                        new CustomError(
                                response.message(),response.code())));
                    Log.d("TAG", "error: " + response.code());
                } else {
                    mUpdateUserResponse.setValue(ApiResponse.Companion.error(
                            new CustomError(
                                    response.message(),response.code())));

                }
            }

            @Override
            public void onError(@NonNull Throwable e, boolean isInternetError, @Nullable CustomError error) {
                Log.d("TAG", "onError: ");
                mUpdateUserResponse.setValue(ApiResponse.Companion.error(
                        error));
            }

            @Override
            public void onRequestComplete() {

            }
        });
    }
}
