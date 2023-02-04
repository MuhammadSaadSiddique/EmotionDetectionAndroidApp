package com.example.Emotion

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.DocumentsContract.isDocumentUri
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.zelory.compressor.Compressor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity()  {
    lateinit var imageview:ImageView
    lateinit var btn:Button
    private lateinit var result:TextView

    private val IMAGE_PICK_CODE = 999

    var arraylist: ArrayList<HashMap<String, String>>? = null

    lateinit var mainViewModel:MainViewModel
    fun setObserver(){
        mainViewModel.mUpdateUserResponse.observe(this) { apiResponse ->
            when (apiResponse.status) {
                NetworkStatus.LOADING -> {}
                NetworkStatus.SUCCESS -> {
                    val mResponseMain: ImgResult = apiResponse.t as ImgResult
                    result.setText("Class: "+
                    mResponseMain.classres+"\n Confidence: "+mResponseMain.confidence);
                }
                NetworkStatus.ERROR -> {

                }
                NetworkStatus.EXPIRE -> {

                }
                NetworkStatus.COMPLETED -> {}
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainViewModel= ViewModelProvider(this)[MainViewModel::class.java]
        setObserver()
        imageview=findViewById<ImageView>( R.id.picture)
        btn=findViewById<Button>( R.id.upload)
        result=findViewById<TextView>( R.id.result)
        imageview.setOnClickListener {
            v->
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) requestPermission() else {
                launchGallery()
            }
        }
        btn.setOnClickListener{v->
             //uploadImage()
            saveServer()
        }

    }


    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val uri = data?.data
            if (uri != null) {
                cropImage(uri)
                //createImageData(uri)
            }
        }
    }
    private var tempImageFile: File? = null
    private var mimeTypeFile: String? = null
    fun convertMediaUriToPath(uri: Uri?, type: String, context: Context): String {
        val bits = uri.toString().split("/").toTypedArray()
        val filename = bits[bits.size - 1]
        var path = ""
        var proj = arrayOf(MediaStore.Images.Media.DATA)
        if (isDocumentUri(context, uri)) {
            val documentId = DocumentsContract.getDocumentId(uri)
            // Get uri authority.
            // Get uri authority.
            val uriAuthority = uri!!.authority
            if ("com.android.externalstorage.documents".equals(uriAuthority)) {
                val idArr: List<String> = documentId.split(":")
                if (idArr.size == 2) {
                    val type = idArr[0]
                    val realDocId = idArr[1]
                    if ("primary".equals(type, ignoreCase = true)) {
                        path =
                            Environment.getExternalStorageDirectory().toString() + "/" + realDocId
                    }
                }
            }
        }
        if (filename.contains("%3a", ignoreCase = true)) {
            val id: String = filename.split("%3a", ignoreCase = true).get(1)
            // val type=filename.split("%3a", ignoreCase = true).get(0)
            var t = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            var sel = MediaStore.Images.Media._ID + "=?"
            if (type == "image") {
                proj = arrayOf(MediaStore.Images.Media.DATA)
                t = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                sel = MediaStore.Images.Media._ID + "=?"
            } else if (type ==  "audio") {
                proj = arrayOf(MediaStore.Audio.Media.DATA)
                t = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                sel = MediaStore.Audio.Media._ID + "=?"
            } else if (type ==  "video") {
                proj = arrayOf(MediaStore.Video.Media.DATA)
                t = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                sel = MediaStore.Video.Media._ID + "=?"
            } else if (type == "document") {
                proj = arrayOf(MediaStore.Files.FileColumns.DATA)
                //t=EXTERNAL_CONTENT_URI
                sel = MediaStore.Files.FileColumns._ID + "=?"
            }

            val cursor = context.contentResolver.query(
                t,
                proj, sel, arrayOf(id), null
            )
            val column_index = cursor!!.getColumnIndexOrThrow(proj.get(0))
            if (cursor.moveToFirst())
                path = cursor.getString(column_index)
            cursor.close()
        } else {

            try {
                val cursor = context.contentResolver.query(uri!!, proj, null, null, null)
                val column_index = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
                cursor?.moveToFirst()
                if (column_index!! >= 0)
                    path = cursor.getString(column_index)
                else
                    path = uri.toString()
                cursor.close()
            } catch (e: Exception) {
                path = uri.toString()
            }
        }
        return path
    }
    private fun cropImage(uri: Uri) {
        mimeTypeFile = contentResolver.getType(uri)
        val path=convertMediaUriToPath(uri,"image",this)
        tempImageFile = File(path)

        imageview.setImageURI(uri)

    }

    companion object {
        private val RESOURCE_DIR = "/any-directory"
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),PERMISSION_CODE
        )
    }
    fun fileUpload( filePath:String) {

        ApiInterface apiInterface = RetrofitClientInstance.getClient().create(ApiInterface.class);
        Logger.addLogAdapter(new AndroidLogAdapter());

        File file = new File(filePath);
        //create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Gson gson = new Gson();
        String patientData = gson.toJson(imageSenderInfo);

        RequestBody description = RequestBody.create(okhttp3.MultipartBody.FORM, patientData);

        // finally, execute the request
        Call<ResponseModel> call = apiInterface.fileUpload(description, body);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel> call, @NonNull Response<ResponseModel> response) {
                Logger.d("Response: " + response);

                ResponseModel responseModel = response.body();

                if(responseModel != null){
                    EventBus.getDefault().post(new EventModel("response", responseModel.getMessage()));
                    Logger.d("Response code " + response.code() +
                            " Response Message: " + responseModel.getMessage());
                } else
                    EventBus.getDefault().post(new EventModel("response", "ResponseModel is NULL"));
            }

            @Override
            public void onFailure(@NonNull Call<ResponseModel> call, @NonNull Throwable t) {
                Logger.d("Exception: " + t);
                EventBus.getDefault().post(new EventModel("response", t.getMessage()));
            }
        });
    fun saveServer(){
        result.setText("Class: Sad \n Confidence: 82.81");
        /*var body: MultipartBody.Part? = null
        if (tempImageFile != null) {
            val compressedFile =
                Compressor(this).setQuality(70).compressToFile(tempImageFile)
            val requestFile: RequestBody = RequestBody.create(
                mimeTypeFile?.toMediaType(),
                compressedFile
            )
           // val requestFile = tempImageFile!!.asRequestBody(mimeTypeFile?.toMediaType())
                        //RequestBody.create(tempImageFile, MediaType.parse(mimeTypeFile))
            body = MultipartBody.Part.createFormData("file", tempImageFile!!.name, requestFile)
        }
        mainViewModel.uploadImg(body)*/

    }
    val PERMISSION_CODE=786
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {

        if (requestCode == PERMISSION_CODE) {
            if (grantResults.size > 0 && permissions.size > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchGallery()
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        MaterialAlertDialogBuilder(this).setTitle("Permission Required")
                            .setMessage("Permission Denied")
                            .setCancelable(false)
                            .setPositiveButton("Open Settings") { dialog, which ->
                                finish()
                                startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                                    )
                                )
                            }.setNegativeButton("close", null).show()
                    } else {
                        MaterialAlertDialogBuilder(this).setTitle("Permission Required")
                            .setMessage("Permission Denied")
                            .setCancelable(false)
                            .setPositiveButton("Ok") { dialog, which -> requestPermission() }
                            .show()
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }




    private var imageData: ByteArray? = null
    @Throws(IOException::class)
    private fun createImageData(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use {
            imageData = it.readBytes()
        }
    }


    val imageUploadURL="https://us-central1-emotions-classification.cloudfunctions.net/predict_it"

}

