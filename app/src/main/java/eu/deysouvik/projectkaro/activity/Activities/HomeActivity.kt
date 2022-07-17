package eu.deysouvik.projectkaro.activity.Activities

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessaging
import eu.deysouvik.projectkaro.Adapters.BoardItemAdapter
import eu.deysouvik.projectkaro.FireBase.FireStore
import eu.deysouvik.projectkaro.Models.Board
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.Object.Constants
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*

class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var username=""
    companion object{
        private val NEW_BOARD_ADDED_CODE=1
    }

    private lateinit var  mSharedPref:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_menu_icon)
         toolbar_main_activity.setNavigationOnClickListener {
             toggelDrawer()
         }

        nav_view.setNavigationItemSelectedListener(this)

        mSharedPref=this.getSharedPreferences(Constants.PROJECTKARO_PREF, MODE_PRIVATE)

        showProgressBar("Loading...")
        FireStore().getUser(this,true)


        fab_create_board.setOnClickListener {
            val intent=Intent(this,CreateBoardActivity::class.java)
            intent.putExtra("USERNAME",username)
            startActivityForResult(intent, NEW_BOARD_ADDED_CODE)
        }


    }

    override fun onResume() {
        super.onResume()
        FireStore().getUser(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK && requestCode== NEW_BOARD_ADDED_CODE){
            showProgressBar("Loading")
            FireStore().getBoardsList(this)
        }
    }


    fun updateNavDrawerUserInfo(user:User,readBoardList:Boolean){
        cancel_progressBar()
        username=user.name
        Glide
            .with(this)
            .load(user.photo)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_user_image)

        tv_username.text=user.name

        val isUpdatedToken=mSharedPref.getBoolean(Constants.FCM_TOKEN_UPDATED,false)
        if(isUpdatedToken){
            if(readBoardList){
                showProgressBar("Loading...")
                FireStore().getBoardsList(this)
            }
        }else{
             FirebaseMessaging.getInstance().token.addOnSuccessListener { token->
                 updateTokenToFirestore(token,readBoardList)
             }.addOnFailureListener {e->
                 if(readBoardList){
                     showProgressBar("Loading...")
                     FireStore().getBoardsList(this)
                 }
                 Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
             }
        }




    }


    fun toggelDrawer(){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else{
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile->{
              startActivity(Intent(this,MyProfileActivity::class.java))
            }
            R.id.nav_sign_out->{
                val dialog= AlertDialog.Builder(this)
                dialog.setMessage("Are you want to sign out?")
                dialog.setIcon(R.drawable.ic_alert)
                dialog.setPositiveButton("Yes"){dialoginterface,which->
                    dialoginterface.dismiss()
                    FirebaseAuth.getInstance().signOut()
                    mSharedPref.edit().clear().apply()
                    val intent=Intent(this,WelcomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                dialog.setNegativeButton("No"){dialoginterface,which->
                    dialoginterface.dismiss()
                }
                val alertdialog=dialog.create()
                alertdialog.setCancelable(false)
                alertdialog.show()

            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }




    fun fillBoardsInRecyclerView(list:ArrayList<Board>){
        cancel_progressBar()
        if(list.size>0){
            rv_boards_list.visibility=View.VISIBLE
            tv_no_boards_available.visibility=View.GONE

            rv_boards_list.layoutManager= LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)
            val adapter=BoardItemAdapter(this,list)
            rv_boards_list.adapter=adapter

            adapter.setOnClickListener(object: BoardItemAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent=Intent(this@HomeActivity,TaskListActivity::class.java)
                    intent.putExtra("DOCUMENT_ID",model.documentId)
                   startActivity(intent)
                }
            })
        }
        else{
            rv_boards_list.visibility=View.GONE
            tv_no_boards_available.visibility=View.VISIBLE
        }
    }


    fun onSuccessTokenUpdate(readBoardList:Boolean){
        cancel_progressBar()
        val editor=mSharedPref.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        if(readBoardList){
            showProgressBar("Loading...")
            FireStore().getBoardsList(this)
        }
    }

    private fun updateTokenToFirestore(token:String,readBoardList:Boolean){
        val hashmap:HashMap<String,Any> = HashMap()
        hashmap["fmctoken"]=token
        FireStore().updateUserData(this,hashmap,readBoardList)
    }



}