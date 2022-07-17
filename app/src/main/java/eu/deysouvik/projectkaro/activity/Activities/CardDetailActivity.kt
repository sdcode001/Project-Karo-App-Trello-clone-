package eu.deysouvik.projectkaro.activity.Activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import eu.deysouvik.projectkaro.Adapters.AssignedMemberListAdapter
import eu.deysouvik.projectkaro.Adapters.BoardAndCardMemberListAdapter
import eu.deysouvik.projectkaro.Adapters.ColorListAdapter
import eu.deysouvik.projectkaro.FireBase.FireStore
import eu.deysouvik.projectkaro.Models.Board
import eu.deysouvik.projectkaro.Models.Card
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.Object.Constants
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.activity_card_detail.*
import kotlinx.android.synthetic.main.activity_task_list.*
import kotlinx.android.synthetic.main.dialog_color_list.*
import kotlinx.android.synthetic.main.dialog_member_list.*
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
import java.util.*
import kotlin.collections.ArrayList

class CardDetailActivity : BaseActivity() {
    private var task_position=-1
    private var card_position=-1
    lateinit var myBoard:Board
    lateinit var myCard:Card
    private var selectedColor=""
    private lateinit var memberList:ArrayList<User>
    private  var dueDateinMills:Long=0
    private lateinit var assignedMemberList:ArrayList<User>
    private lateinit var assignedMemberIdList:ArrayList<String>
    private var new_member:User?=null
    private var removed_member:User?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_detail)

        if(intent.hasExtra("BOARD")){
            myBoard=intent.getParcelableExtra<Board>("BOARD")!!
        }
        if(intent.hasExtra("CARD_POSITION")){
            card_position=intent.getIntExtra("CARD_POSITION",0)
        }
        if(intent.hasExtra("TASK_POSITION")){
            task_position=intent.getIntExtra("TASK_POSITION",0)
        }
        if(intent.hasExtra("BOARD_MEMBERS")){
            memberList=intent.getParcelableArrayListExtra<User>("BOARD_MEMBERS")!!
        }

        myCard=myBoard.taskList[task_position].cards[card_position]

        assignedMemberList=ArrayList()
        assignedMemberIdList= ArrayList()

        assignedMemberIdList=myCard.assignTo
        showProgressBar("Loading...")
        FireStore().getMembersOfBoard(this,myCard.assignTo)

        tv_select_label_color.setOnClickListener {
            showColorListDialog()
        }

        btn_update_card_details.setOnClickListener {
            updateCardToFirebase()
        }

        tv_select_due_date.setOnClickListener {
           showDatePickerDialog()
        }


    }

    private fun fillCardDetails(){
        setupActionBar(myCard.name)
        et_name_card_details.setText(myCard.name)
        selectedColor=myCard.color
        dueDateinMills=myCard.dueDate
        if(selectedColor!=""){
            tv_select_label_color.setBackgroundColor(Color.parseColor(selectedColor))
            tv_select_label_color.text=""
        }
        if(dueDateinMills>0){
            val sdf=java.text.SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val date=sdf.format(Date(dueDateinMills))
            tv_select_due_date.text=date
        }
        val dumi_user=User()
        assignedMemberList.add(dumi_user)

        setupAssignedMemberListRecyclerView(assignedMemberList)

    }

    fun setupActionBar(title:String){
        setSupportActionBar(toolbar_card_details_activity)
        val actionbar=supportActionBar
        if (actionbar != null) {
            actionbar.title= title
            actionbar!!.setDisplayHomeAsUpEnabled(true)
            actionbar!!.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }
        toolbar_card_details_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card->{
                val dialog= AlertDialog.Builder(this)
                dialog.setMessage("Are you sure to delete Card ${myCard.name} ?")
                dialog.setIcon(R.drawable.ic_alert)
                dialog.setPositiveButton("Yes"){dialoginterface,which->
                    dialoginterface.dismiss()
                    val tasklist=myBoard.taskList
                    tasklist[task_position].cards.removeAt(card_position)
                    tasklist.removeAt(tasklist.size-1)
                    showProgressBar("Deleting Card...")
                    FireStore().updateBoardTaskList(this,myBoard.documentId,tasklist)
                }
                dialog.setNegativeButton("No"){dialoginterface,which->
                    dialoginterface.dismiss()
                }
                val alertdialog=dialog.create()
                alertdialog.setCancelable(false)
                alertdialog.show()

                return true
            }

        }

        return super.onOptionsItemSelected(item)
    }


    fun onSuccessCardDelete(){
        cancel_progressBar()
        if(new_member!=null){
            val title="Assinged to the card ${myCard.name} in board ${myBoard.name}"
            val message="You have been assigned to the card ${myCard.name} in board ${myBoard.name} by ${memberList[0].name}"
            sendNotificationToUserAsyncTask(new_member!!.fmctoken,title,message).execute()
        }
        if(removed_member!=null){
           val title="Removed from card ${myCard.name} in board ${myBoard.name}"
            val message="You have been removed from card ${myCard.name} in board ${myBoard.name} by ${memberList[0].name}"
            sendNotificationToUserAsyncTask(removed_member!!.fmctoken,title,message).execute()
        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun showColorListDialog(){
        val dialog=Dialog(this)
        dialog.setContentView(R.layout.dialog_color_list)
        dialog.rvColorList.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        dialog.rvColorList.setHasFixedSize(true)
        val adapter=ColorListAdapter(this,getColorList(),selectedColor)
        dialog.rvColorList.adapter=adapter
        adapter.setOnClickListener(object: ColorListAdapter.OnClickListener{
            override fun onClick(position: Int, color: String) {
                dialog.dismiss()
                selectedColor=color
                tv_select_label_color.setBackgroundColor(Color.parseColor(selectedColor))
                tv_select_label_color.text=""
            }

        })
        dialog.show()
    }

    private fun getColorList():ArrayList<String>{
        val list:ArrayList<String> = ArrayList()
        list.add("#ff0000")
        list.add("#ffff00")
        list.add("#40ff00")
        list.add("#00ffff")
        list.add("#0040ff")
        list.add("#bf00ff")
        list.add("#ff0080")
        list.add("#b94646")
        list.add("#808080")
        list.add("#cc9900")
        return list
    }

    fun updateCardToFirebase(){
        val card_name=et_name_card_details.text.toString()
        if(!card_name.isEmpty()){
            val new_card=Card(card_name,myCard.createdBy,assignedMemberIdList,selectedColor,dueDateinMills)
            val taskList=myBoard.taskList
            taskList[task_position].cards[card_position]=new_card
            taskList.removeAt(taskList.size-1)
            showProgressBar("Updating...")
            FireStore().updateBoardTaskList(this,myBoard.documentId,taskList)
        }
        else{
            Toast.makeText(this, "Please enter card name", Toast.LENGTH_SHORT).show()}
    }


    fun showDatePickerDialog(){
        val cal=Calendar.getInstance()
        val y=cal.get(Calendar.YEAR)
        val m=cal.get(Calendar.MONTH)
        val d=cal.get(Calendar.DAY_OF_MONTH)
        val dpd=DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                  val sDay=if(day<10) "0$day" else "$day"
                  val sMonth=if(month+1<10) "0${month+1}" else "${month+1}"
                  val selectedDate="$sDay/$sMonth/$year"
                    tv_select_due_date.text=selectedDate
                    val sdf=java.text.SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                    val thedate=sdf.parse(selectedDate)
                    dueDateinMills=thedate!!.time
                }, y, m, d)
        dpd.show()
    }

    private fun setupAssignedMemberListRecyclerView(list:ArrayList<User>){
        rvSelectedMemberList.layoutManager=GridLayoutManager(this,4)
        val adapter = AssignedMemberListAdapter(this,list)
        rvSelectedMemberList.adapter=adapter
    }

    fun getAssignedMemberList(list:ArrayList<User>){
        assignedMemberList=list
        cancel_progressBar()
        fillCardDetails()
    }

    fun deleteAssignMember(position:Int){
        val member=assignedMemberList[position]
        val dialog= AlertDialog.Builder(this)
        dialog.setMessage("Are you sure to remove ${member.name} from this card ?")
        dialog.setIcon(R.drawable.ic_alert)
        dialog.setPositiveButton("Yes"){dialoginterface,which->
            dialoginterface.dismiss()
            assignedMemberIdList.removeAt(position)
            removed_member=assignedMemberList[position]
            assignedMemberList.removeAt(position)
            setupAssignedMemberListRecyclerView(assignedMemberList)
        }
        dialog.setNegativeButton("No"){dialoginterface,which->
            dialoginterface.dismiss()
        }
        val alertdialog=dialog.create()
        alertdialog.setCancelable(false)
        alertdialog.show()
    }

    fun showDialogForAddNewMemberToCard(){
        val dialog=Dialog(this)
        dialog.setContentView(R.layout.dialog_member_list)
        dialog.rvBoardMemberList.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        dialog.rvBoardMemberList.setHasFixedSize(true)
        val adapter=BoardAndCardMemberListAdapter(this,memberList,assignedMemberList)
        dialog.rvBoardMemberList.adapter=adapter
        adapter.setOnClickListener(object: BoardAndCardMemberListAdapter.OnClickListener{
            override fun onClick(position: Int, member: User) {
                dialog.dismiss()
                new_member=member
                val new_member=member.id
                assignedMemberIdList.add(new_member)
                assignedMemberList.add(0,member)
                setupAssignedMemberListRecyclerView(assignedMemberList)
            }
        })
        dialog.show()
    }


    private inner class sendNotificationToUserAsyncTask(private val token:String,private val title:String,private val message:String): AsyncTask<Any, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            //showProgressBar("Sending Notification...")
        }
        override fun doInBackground(vararg p0: Any?): String {
            var result:String
            var connection: HttpURLConnection?=null
            try{
                val url= URL(Constants.BASE_URL)
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
                val writedata= DataOutputStream(connection.outputStream)
                val jsonFile= JSONObject()
                val dataobject= JSONObject()
                dataobject.put(Constants.FCM_KEY_TITLE,title)
                dataobject.put(Constants.FCM_KEY_MESSAGE,message)
                jsonFile.put(Constants.FCM_KEY_DATA,dataobject)
                jsonFile.put(Constants.FCM_KEY_TO,token)

                writedata.writeBytes(jsonFile.toString())
                writedata.flush()
                writedata.close()

                val httpResult=connection.responseCode
                if(httpResult== HttpURLConnection.HTTP_OK){
                    val inputStream=connection.inputStream
                    val reader= BufferedReader(InputStreamReader(inputStream))
                    val sb= StringBuilder()
                    var line:String?
                    try{
                        while(reader.readLine().also {line=it}!=null){
                            sb.append(line+"\n")
                        }
                    }catch (e: IOException){
                        e.printStackTrace()
                    }finally{
                        try{
                            inputStream.close()
                        }catch (e: IOException){
                            e.printStackTrace()
                        }
                    }
                    result=sb.toString()
                }else{
                    result=connection.responseMessage
                }
            }catch (e: SocketTimeoutException){
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
           // cancel_progressBar()
        }

    }









}