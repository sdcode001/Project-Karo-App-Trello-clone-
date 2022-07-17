package eu.deysouvik.projectkaro.activity.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import eu.deysouvik.projectkaro.FireBase.FireStore
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.activity_log_in.*


class LogInActivity : BaseActivity() {

    var userDetails:User=User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        btn_sign_in.setOnClickListener {
            SignInUser()
        }


    }


    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_in_activity)
        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }

        toolbar_sign_in_activity.setNavigationOnClickListener {
            startActivity(Intent(this,WelcomeActivity::class.java))
            finish()
        }
    }


    private fun SignInUser(){
        val email=et_email_login.text.toString().trim{it<=' '}
        val password=et_password_login.text.toString().trim{it<=' '}

        if(validateForm(email,password)){
             showProgressBar("Please Wait")
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener { task->

                if(task.isSuccessful){
                    FireStore().getUser(this)
                }
                else{
                    cancel_progressBar()
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    startActivity(Intent(this,WelcomeActivity::class.java))
                    finish()
                }
            }

        }

    }

    fun onSuccessSignIn(user:User){
        userDetails=user
        cancel_progressBar()
        startActivity(Intent(this,HomeActivity::class.java))
        finish()
    }


    private fun validateForm(email:String,password:String):Boolean{
        return when{
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