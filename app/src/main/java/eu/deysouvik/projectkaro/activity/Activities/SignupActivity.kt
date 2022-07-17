package eu.deysouvik.projectkaro.activity.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import eu.deysouvik.projectkaro.FireBase.FireStore
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

      setupActionBar()

        btn_sign_up.setOnClickListener {
            SignUpUser()
        }


    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_up_activity)
        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }

        toolbar_sign_up_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    private fun SignUpUser(){
        val name=et_name.text.toString().trim{it<=' '}
        val email=et_email.text.toString().trim{it<=' '}
        val password=et_password.text.toString().trim{it<=' '}

        if(validateForm(name,email,password)){
            showProgressBar("Please Wait")
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task->
                if(task.isSuccessful){
                    val userDetail: FirebaseUser? =task.result.user
                    val user= User(userDetail!!.uid,name,email,password)
                    FireStore().addUser(this,user)

                }
                else{
                    cancel_progressBar()
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }
        }

    }



    private fun validateForm(name:String,email:String,password:String):Boolean{
        return when{
            TextUtils.isEmpty(name)->{
                showError("Please enter your name")
                return false
            }
            TextUtils.isEmpty(email)->{
                showError("Please enter your email")
                return false
            }
            TextUtils.isEmpty(password)->{
                showError("Please enter password")
                return false
            }
            else-> {true}
        }

    }
}