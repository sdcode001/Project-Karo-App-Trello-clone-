package eu.deysouvik.projectkaro.activity.Activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import eu.deysouvik.projectkaro.Adapters.MemberListAdapter
import eu.deysouvik.projectkaro.FireBase.FireStore
import eu.deysouvik.projectkaro.Models.Board
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.Object.Constants
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.dialog_search_member.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {

    private lateinit var mBoard:Board
    private lateinit var memberlist:ArrayList<User>
    private var isChangesMade=false
    private lateinit var new_member:User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if(intent.hasExtra("Board")){
            mBoard=intent.getParcelableExtra<Board>("Board")!!
        }
        showProgressBar("Loading...")
        FireStore().getMembersOfBoard(this,mBoard.assignTo)

    }

    fun onSuccessMembersListLoading(list:ArrayList<User>){
        memberlist=list
        cancel_progressBar()
        setupActionBar("Members")
        setRecyclerViewData(memberlist)
    }

    fun setRecyclerViewData(list:ArrayList<User>){
        rv_members_list.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        rv_members_list.setHasFixedSize(true)
        val adapter=MemberListAdapter(this,list)
        rv_members_list.adapter=adapter
    }

    fun setupActionBar(title:String){
        setSupportActionBar(toolbar_members_activity)
        val actionbar=supportActionBar
        if (actionbar != null) {
            actionbar.title=title
            actionbar!!.setDisplayHomeAsUpEnabled(true)
            actionbar!!.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }
        toolbar_members_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member->{
                val dialog=Dialog(this)
                dialog.setContentView(R.layout.dialog_search_member)
                dialog.tv_add.setOnClickListener {
                    val email=dialog.et_email_search_member.text.toString()
                    if(!email.isEmpty()){
                        dialog.dismiss()
                        showProgressBar("Searching...")
                        FireStore().checkForUser(this,email.toString())
                    }
                    else{
                        Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.tv_cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun onSuccessUserFind(user:User){
        cancel_progressBar()
        new_member=user
        showProgressBar("Loading...")
        val user_id=user.id
        memberlist.add(user)
        mBoard.assignTo.add(user_id)
        FireStore().updatingMemberListOfBoard(this,mBoard.assignTo,mBoard.documentId)

    }

    fun onSuccessMemberAdded(){
        cancel_progressBar()
        setRecyclerViewData(memberlist)
        isChangesMade=true
        sendNotificationToUserAsyncTask(new_member.fmctoken).execute()
    }

    override fun onBackPressed() {
        if(isChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }



    private inner class sendNotificationToUserAsyncTask(private val token:String): AsyncTask<Any,Void,String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressBar("Sending Notification...")
        }
        override fun doInBackground(vararg p0: Any?): String {
            var result:String
            var connection:HttpURLConnection?=null
            try{
                val url=URL(Constants.BASE_URL)
                connection=url.openConnection() as HttpURLConnection
                connection.doInput=true
                connection.doOutput=true
                connection.instanceFollowRedirects=false
                connection.requestMethod="POST"
                //header file properties
                connection.setRequestProperty("Content-Type","application/json")
                connection.setRequestProperty("charset","utf-8")
                connection.setRequestProperty("Accept","application/json")
                connection.setRequestProperty(Constants.FCM_AUTHORIZATION,"${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}")
                connection.useCaches=false
                //preparing the json data file
                val writedata=DataOutputStream(connection.outputStream)
                val jsonFile=JSONObject()
                val dataobject=JSONObject()
                dataobject.put(Constants.FCM_KEY_TITLE,"Assigned to the Board ${mBoard.name} in Project Karo.")
                dataobject.put(Constants.FCM_KEY_MESSAGE,"You have been assigned to the Board by ${memberlist[0].name}")
                jsonFile.put(Constants.FCM_KEY_DATA,dataobject)
                jsonFile.put(Constants.FCM_KEY_TO,token)

                writedata.writeBytes(jsonFile.toString())
                writedata.flush()
                writedata.close()

                val httpResult=connection.responseCode
                if(httpResult==HttpURLConnection.HTTP_OK){
                    val inputStream=connection.inputStream
                    val reader=BufferedReader(InputStreamReader(inputStream))
                    val sb=StringBuilder()
                    var line:String?
                    try{
                        while(reader.readLine().also {line=it}!=null){
                            sb.append(line+"\n")
                        }
                    }catch (e:IOException){
                        e.printStackTrace()
                    }finally{
                        try{
                            inputStream.close()
                        }catch (e:IOException){
                            e.printStackTrace()
                        }
                    }
                    result=sb.toString()
                }else{
                    result=connection.responseMessage
                }
            }catch (e:SocketTimeoutException){
                result="Connection timeout"
            }catch (e:Exception){
                result="Error : "+e.message
            }finally {
                connection?.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancel_progressBar()
        }

    }



}