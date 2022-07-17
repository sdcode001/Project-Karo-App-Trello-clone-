package eu.deysouvik.projectkaro.activity.Activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import eu.deysouvik.projectkaro.FireBase.FireStore
import eu.deysouvik.projectkaro.Models.Board
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*

class CreateBoardActivity : BaseActivity() {

    var createdby=""
    private var selectedBoardPhotoUri: Uri? =null
    private var uploadedBoardInamgeUri=""
    companion object{
        private val READ_EXTERNAL_STORAGE_PREMISSION_CODE=1
        private val PICK_PHOTO_GALLAY_CODE=2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setSupportActionBar(toolbar_create_board_activity)
        val actionbar=supportActionBar
        if (actionbar != null) {
            actionbar.title="Create Board"
            actionbar!!.setDisplayHomeAsUpEnabled(true)
            actionbar!!.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }
        toolbar_create_board_activity.setNavigationOnClickListener {
            onBackPressed()
        }

        createdby= intent.getStringExtra("USERNAME").toString()



        choose_board_photo.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                PickImageFromGallery()
            }
            else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_PREMISSION_CODE)
            }
        }


        btn_create.setOnClickListener {
            if(selectedBoardPhotoUri!=null){
              uploadUserDataToFireStoreByUploadingImageToStorage()
            }
            else{
                uploadBoardDataToFireStore()
            }
        }





    }


    fun PickImageFromGallery(){
        val image_intent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(image_intent, PICK_PHOTO_GALLAY_CODE)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==READ_EXTERNAL_STORAGE_PREMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                PickImageFromGallery()
            }
            else{
                Toast.makeText(this, "You Denied to give storage permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode==PICK_PHOTO_GALLAY_CODE && data!!.data!=null){
            selectedBoardPhotoUri=data.data

            try {
                Glide
                    .with(this)
                    .load(selectedBoardPhotoUri.toString())
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(iv_board_image)
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
    }


    private fun uploadUserDataToFireStoreByUploadingImageToStorage(){
        showProgressBar("Please Wait")
        val firebaseStorage: StorageReference = FirebaseStorage.getInstance().reference

        firebaseStorage.child("Board_Images").child("Board_Image"+System.currentTimeMillis()+"."+getFileExtension(selectedBoardPhotoUri)).putFile(selectedBoardPhotoUri!!).addOnSuccessListener {
                taskSnapshot->
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                uploadedBoardInamgeUri=uri.toString()
                uploadBoardDataToFireStore()

            }
        }.addOnFailureListener{
                exception->
            Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
            cancel_progressBar()
        }

    }





    private fun uploadBoardDataToFireStore(){
        if(!et_board_name.text.isNullOrEmpty()){
            val assignUserList:ArrayList<String> = ArrayList()
            assignUserList.add(getUserId()!!)
            val board= Board(et_board_name.text.toString(),uploadedBoardInamgeUri,createdby,assignUserList)
            showProgressBar("Please Wait")
            FireStore().addBoard(this,board)
        }
        else{
            Toast.makeText(this, "Please enter Board name!", Toast.LENGTH_SHORT).show()
        }
    }


    fun getFileExtension(uri:Uri?):String?{
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(contentResolver.getType(uri!!))
    }


 fun onSuccessBoardCreate(){
     cancel_progressBar()
     setResult(Activity.RESULT_OK)
     finish()
 }

    fun hide_progressBar(){
        cancel_progressBar()
    }





}