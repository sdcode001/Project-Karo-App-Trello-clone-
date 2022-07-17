package eu.deysouvik.projectkaro.activity.Activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import eu.deysouvik.projectkaro.Adapters.TaskItemAdapter
import eu.deysouvik.projectkaro.FireBase.FireStore
import eu.deysouvik.projectkaro.Models.Board
import eu.deysouvik.projectkaro.Models.Card
import eu.deysouvik.projectkaro.Models.Task
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {
    var documentId=""
    private lateinit var mBoardDetail:Board
    private lateinit var memberList:ArrayList<User>
    companion object{
        val MEMBER_ADD_TO_BOARD_CODE=10
        val CARD_UPDATED_TO_BOARD_CODE=10
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if(intent.hasExtra("DOCUMENT_ID")){
            documentId = intent.getStringExtra("DOCUMENT_ID")!!
        }
        showProgressBar("Loading...")
        FireStore().getBoardDetail(this,documentId)
    }

    fun fillBoardDetails(board:Board){
        mBoardDetail=board
        cancel_progressBar()
        setupActionBar(board.name)
        val task= Task("Add List")
        mBoardDetail.taskList.add(task)

        rv_task_list.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        rv_task_list.setHasFixedSize(true)
        val adapter=TaskItemAdapter(this,mBoardDetail.taskList)
        rv_task_list.adapter=adapter

        showProgressBar("Loading...")
        FireStore().getMembersOfBoard(this,mBoardDetail.assignTo)
    }

    fun setupActionBar(title:String){
        setSupportActionBar(toolbar_task_list_activity)
        val actionbar=supportActionBar
        if (actionbar != null) {
            actionbar.title=title
            actionbar!!.setDisplayHomeAsUpEnabled(true)
            actionbar!!.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }
        toolbar_task_list_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun addNewTaskToBoard(name:String){
        val new_task=Task(name,getUserId()!!)
        val new_task_list=mBoardDetail.taskList
        new_task_list.add(0,new_task)
        new_task_list.removeAt(new_task_list.size-1)
        showProgressBar("Adding new task")
        FireStore().updateBoardTaskList(this,mBoardDetail.documentId,new_task_list)
    }

    fun updateTask(position:Int,name:String,prev_task:Task){
        val task=Task(name,prev_task.createdBy,prev_task.cards)
        val tasklist=mBoardDetail.taskList
        tasklist[position]=task
        tasklist.removeAt(tasklist.size-1)
        showProgressBar("Updating task")
        FireStore().updateBoardTaskList(this,mBoardDetail.documentId,tasklist)
    }
    fun deleteTask(position:Int){
        mBoardDetail.taskList.removeAt(position)
        mBoardDetail.taskList.removeAt(mBoardDetail.taskList.size-1)
        showProgressBar("Deleting task")
        FireStore().updateBoardTaskList(this,mBoardDetail.documentId,mBoardDetail.taskList)
    }


    fun addCardToTaskList(position:Int,name:String){
        val assignto:ArrayList<String> =ArrayList()
        assignto.add(getUserId()!!)
        val new_Card= Card(name,getUserId()!!,assignto)
        val tasklist=mBoardDetail.taskList
        tasklist.removeAt(tasklist.size-1)
        tasklist[position].cards.add(new_Card)
        showProgressBar("Adding Card to task")
        FireStore().updateBoardTaskList(this,mBoardDetail.documentId,tasklist)
    }

    fun addUpdateTaskListSuccess(){
        cancel_progressBar()
        showProgressBar("Loading...")
        FireStore().getBoardDetail(this,mBoardDetail.documentId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members->{
                val intent= Intent(this,MembersActivity::class.java)
                intent.putExtra("Board",mBoardDetail)
                startActivityForResult(intent, MEMBER_ADD_TO_BOARD_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if((resultCode==Activity.RESULT_OK && requestCode== MEMBER_ADD_TO_BOARD_CODE) || (resultCode==Activity.RESULT_OK && requestCode== CARD_UPDATED_TO_BOARD_CODE)){
            showProgressBar("Loading...")
            FireStore().getBoardDetail(this,documentId)
        }

    }

    fun gotoCardDetailActivity(task_position:Int,card_position:Int){
        val intent=Intent(this,CardDetailActivity::class.java)
        intent.putExtra("CARD_POSITION",card_position)
        intent.putExtra("TASK_POSITION",task_position)
        intent.putExtra("BOARD_MEMBERS",memberList)
        intent.putExtra("BOARD",mBoardDetail)
        startActivityForResult(intent, CARD_UPDATED_TO_BOARD_CODE)
    }

    fun updateCardListInTask(tasklist_position:Int,cards:ArrayList<Card>){
        mBoardDetail.taskList.removeAt(mBoardDetail.taskList.size-1)
        mBoardDetail.taskList[tasklist_position].cards=cards
        showProgressBar("Updating...")
        FireStore().updateBoardTaskList(this,mBoardDetail.documentId,mBoardDetail.taskList)
    }

    fun onSuccessGetMembers(list:ArrayList<User>){
        memberList=list
        cancel_progressBar()
    }


    fun hide_progressbar(){
        cancel_progressBar()
    }


}