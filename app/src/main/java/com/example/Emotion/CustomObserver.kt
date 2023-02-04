package com.example.Emotion

import android.util.Log
import io.reactivex.annotations.NonNull
import io.reactivex.annotations.Nullable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import retrofit2.HttpException


class CustomError
/**
 * Construstor of CustomError class
 *
 * @param message String Error message
 * @param code    int Error Code like 400,404 etc
 */
    (
    /**
     * @return String message or getter of message attribute
     */
    /**
     * Setter of Error Message
     *
     * @param message String message of error
     */
    var message: String,
    /**
     * getter of error code
     *
     * @return error code
     */
    /**
     * setter of error code
     *
     * @param code int error code
     */
    var code: Int
)

object DisposableManager {

    private val TAG = "DisposableManager"

    private var mCompositeDisposable: CompositeDisposable? = null

    private val compositeDisposable: CompositeDisposable
        get() {
            if (mCompositeDisposable == null || mCompositeDisposable!!.isDisposed) {
                mCompositeDisposable = CompositeDisposable()
            }
            return mCompositeDisposable as CompositeDisposable
        }

    fun add(disposable: Disposable) {
        try {
            compositeDisposable.add(disposable)
        } catch (e: Exception) {
            Log.d(TAG, "add: ", e)
        }

    }

    fun dispose() {
        try {
            compositeDisposable.dispose()
        } catch (e: Exception) {
            Log.d(TAG, "dispose: ", e)
        }

    }
}

abstract class CustomObserver<T> : io.reactivex.Observer<T> {

    override fun onSubscribe(d: Disposable) {
        DisposableManager.add(d)
    }

    override fun onNext(t: T) {
        onSuccess(t)
    }

    override fun onError(e: Throwable) {
        try {
            Log.d(TAG, "onError: ", e)
            if (e is HttpException) {
                onError(e, false, getErrorMessage(e))
            } else if (e is Exception) {
                onError(
                    e, false,
                    CustomError(
                        e.message!!,
                        e.hashCode()
                    )
                )
            } else {
                onError(e, true, null)
            }

        } catch (ex: Exception) {
            onError(e, false, null)
        }

    }

    override fun onComplete() {
        Log.d(TAG, "onComplete: command complete")
        onRequestComplete()
    }

    private fun getErrorMessage(exception: HttpException): CustomError {
        try {
            return CustomError(
                exception.message(),
                exception.code()
            )
        } catch (e: Exception) {
            return CustomError(
                e.message!!,
                exception.code()
            )
        }

    }

    abstract fun onSuccess(@NonNull t: T)

    abstract fun onError(
        @NonNull e: Throwable,
        isInternetError: Boolean,
        @Nullable error: CustomError?
    )

    abstract fun onRequestComplete()

    companion object {

        private val TAG = "CustomObserver"
    }

}
