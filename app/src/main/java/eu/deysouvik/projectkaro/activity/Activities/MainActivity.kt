package eu.deysouvik.projectkaro.activity.Activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //to hide the actionbar and title bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val font:Typeface= Typeface.createFromAsset(assets,"carbon bl.ttf")
        tv_app_name.typeface=font
        tv_app_creator_name.typeface=font

       Handler().postDelayed({
           if(FirebaseAuth.getInstance().currentUser!=null){
               startActivity(Intent(this, HomeActivity::class.java))
               finish()
           }
           else{
               startActivity(Intent(this, WelcomeActivity::class.java))
               finish()
           }

       },2500)





    }


}