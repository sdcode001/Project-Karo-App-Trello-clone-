package eu.deysouvik.projectkaro.activity.Activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.custom_progress_bar.*

open class BaseActivity :AppCompatActivity() {

    lateinit var progressBar_dialog:Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)



    }

    fun showProgressBar(txt:String){
        progressBar_dialog= Dialog(this)
        progressBar_dialog.setContentView(R.layout.custom_progress_bar)
        progressBar_dialog.tv_progress_text.text=txt
        progressBar_dialog.show()

    }
    fun cancel_progressBar(){
        progressBar_dialog.cancel()
    }


    fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun showError(txt:String){
        val snackbar=Snackbar.make(findViewById(android.R.id.content),txt,Snackbar.LENGTH_LONG)
        val snackbarView=snackbar.view
        snackbarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snackbar_color))
        snackbar.show()
    }


}