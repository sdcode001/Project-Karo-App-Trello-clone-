package eu.deysouvik.projectkaro.activity.Activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)




        //to hide the actionbar and title bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val font: Typeface = Typeface.createFromAsset(assets,"carbon bl.ttf")
        tv_app_name_intro.typeface=font



        btn_sign_in_intro.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }

        btn_sign_up_intro.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }



    }


}