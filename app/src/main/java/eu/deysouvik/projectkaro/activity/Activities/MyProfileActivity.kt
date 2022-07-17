package eu.deysouvik.projectkaro.activity.Activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import eu.deysouvik.projectkaro.FireBase.FireStore
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.util.jar.Manifest

class MyProfileActivity : BaseActivity() {

    companion object{
        private val READ_EXTERNAL_STORAGE_PREMISSION_CODE=1
        private val PICK_PHOTO_GALLAY_CODE=2
    }

    private var selectedImageUri: Uri? =null
    private var uploadedImageLink=""
    private var currentUserData: User? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setSupportActionBar(toolbar_my_profile_activity)
        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
            actionBar.title="My Profile"
        }
        toolbar_my_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }

        FireStore().getUser(this)

        choose_photo.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                  PickImageFromGallery()
            }
            else{
               ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_PREMISSION_CODE)
            }
        }

        btn_update.setOnClickListener {
            if(selectedImageUri!=null){
                updateUserDataToFireStoreByUploadingImageToStorage()
            }
            else{
                updateUserDataInFireStore()
            }
        }




    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== READ_EXTERNAL_STORAGE_PREMISSION_CODE){
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
        if(resultCode== Activity.RESULT_OK && requestCode== PICK_PHOTO_GALLAY_CODE && data!!.data!=null){
             selectedImageUri=data.data

            try {
                Glide
                    .with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_user_profile_image)
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
    }

    fun fillUserData(user: User){
        currentUserData=user
        Glide
            .with(this)
            .load(user.photo)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_user_profile_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        if(user.number!=0L){
            et_mobile.setText(user.number.toString())
        }

    }

    fun PickImageFromGallery(){
        val image_intent= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(image_intent, PICK_PHOTO_GALLAY_CODE)
    }


    fun getFileExtension(uri:Uri?):String?{
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(contentResolver.getType(uri!!))
    }

    private fun updateUserDataToFireStoreByUploadingImageToStorage(){
        showProgressBar("Please Wait")
        val firebaseStorage:StorageReference=FirebaseStorage.getInstance().reference

        firebaseStorage.child("User_Images").child("User_Image"+System.currentTimeMillis()+"."+getFileExtension(selectedImageUri)).putFile(selectedImageUri!!).addOnSuccessListener {
            taskSnapshot->

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                uri->
                uploadedImageLink=uri.toString()

                updateUserDataInFireStore()

            }
        }.addOnFailureListener{
            exception->
            Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
            cancel_progressBar()
        }

    }

    fun updateUserDataInFireStore(){
        val hashmap=HashMap<String,Any>()
        var areChangesMade=false
        if(uploadedImageLink!="" && uploadedImageLink!=currentUserData!!.photo ){
            hashmap["photo"]=uploadedImageLink
            areChangesMade=true
        }
        if(!et_mobile.text.isNullOrEmpty() && et_mobile.text.toString()!=currentUserData!!.number.toString()){
            hashmap["number"]=et_mobile.text.toString().toLong()
            areChangesMade=true
        }
        if(!et_name.text.isNullOrEmpty() && et_name.text.toString()!=currentUserData!!.name){
            hashmap["name"]=et_name.text.toString()
            areChangesMade=true
        }
        if(areChangesMade){
            showProgressBar("Please Wait")
            FireStore().updateUserData(this,hashmap,false)
        }


    }


    fun onSuccessUpdating(){
        cancel_progressBar()
        finish()
    }

    fun hide_progressbar(){
        cancel_progressBar()
    }

}