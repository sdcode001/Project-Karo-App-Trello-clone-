package eu.deysouvik.projectkaro.FireBase

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import eu.deysouvik.projectkaro.Adapters.CardListAdapter
import eu.deysouvik.projectkaro.Adapters.CardMemberListFirstLevelAdapter
import eu.deysouvik.projectkaro.Models.Board
import eu.deysouvik.projectkaro.Models.Task
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.activity.Activities.*
import kotlinx.android.synthetic.main.item_card.view.*

class FireStore{

 private val DBfireStore=FirebaseFirestore.getInstance()

    fun addUser(activity:SignupActivity,user: User){
          DBfireStore.collection("Users").document(getUserId()!!).set(user, SetOptions.merge()).addOnCompleteListener {
              task->
              if(task.isSuccessful){
                  activity.cancel_progressBar()
                  Toast.makeText(activity, "${user.name} you have registered with email ${user.email}", Toast.LENGTH_LONG).show()
                  FirebaseAuth.getInstance().signOut()
                  activity.finish()
              }
              else{
                  Toast.makeText(activity, task.exception!!.message, Toast.LENGTH_SHORT).show()
              }
          }
    }

    fun addBoard(activity:CreateBoardActivity,board: Board){
        DBfireStore.collection("Boards").document().set(board, SetOptions.merge()).addOnCompleteListener {
                task->
            if(task.isSuccessful){
                activity.onSuccessBoardCreate()
                Toast.makeText(activity, "Board created successfully", Toast.LENGTH_LONG).show()
            }
            else{
                activity.hide_progressBar()
                Toast.makeText(activity, task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getBoardsList(activity:HomeActivity){
         DBfireStore.collection("Boards").whereArrayContains("assignTo",getUserId()!!).get().addOnSuccessListener {
             document->
             val list:ArrayList<Board> = ArrayList()
             for(i in document.documents){
                 val board=i.toObject(Board::class.java)!!
                 board.documentId=i.id
                 list.add(board)
             }
             activity.cancel_progressBar()
             activity.fillBoardsInRecyclerView(list)

         }.addOnFailureListener {
             e->
             activity.cancel_progressBar()
             Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
         }
    }

    fun getBoardDetail(activity:TaskListActivity,doucmentID:String){
        DBfireStore.collection("Boards").document(doucmentID).get().addOnSuccessListener {
            document->
            val board=document.toObject(Board::class.java)!!
            board.documentId=document.id
            activity.fillBoardDetails(board)

        }.addOnFailureListener {
            e->
            activity.hide_progressbar()
            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun updateBoardTaskList(activity:Activity,boardId:String,task_list:ArrayList<Task>){
        val hashMap= HashMap<String,Any>()
        hashMap["taskList"]=task_list
        DBfireStore.collection("Boards").document(boardId).update(hashMap).addOnSuccessListener {
            task->
            if(activity is TaskListActivity){activity.addUpdateTaskListSuccess()}
            else if(activity is CardDetailActivity){activity.onSuccessCardDelete()}

        }.addOnFailureListener {
            e->
            if(activity is TaskListActivity){ activity.cancel_progressBar()}
            else if(activity is CardDetailActivity){activity.cancel_progressBar()}

            Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun updateUserData(activity:Activity,dataHashMap:HashMap<String,Any>,result:Boolean){
        DBfireStore.collection("Users").document(getUserId()!!).update(dataHashMap).addOnSuccessListener {
            task->
            if(activity is MyProfileActivity){
                Toast.makeText(activity, "Profile Update Successful", Toast.LENGTH_LONG).show()
                activity.onSuccessUpdating()
            }
            else if(activity is HomeActivity){
                activity.cancel_progressBar()
                activity.onSuccessTokenUpdate(result)
            }
        }.addOnFailureListener {
            exception->
            Toast.makeText(activity, exception.message, Toast.LENGTH_LONG).show()
            if(activity is MyProfileActivity){activity.hide_progressbar()}
            else if(activity is HomeActivity){activity.cancel_progressBar()}

        }
    }

    fun getUser(activity: Activity,readBoardList:Boolean=false){
        DBfireStore.collection("Users").document(getUserId()!!).get().addOnSuccessListener { data->

            val userDetail=data.toObject(User::class.java)

            when(activity){
                is LogInActivity->{
                    if (userDetail != null) { activity.onSuccessSignIn(userDetail) }
                }
                is HomeActivity->{
                    activity.cancel_progressBar()
                    activity.updateNavDrawerUserInfo(userDetail!!,readBoardList)
                }
                is MyProfileActivity->{
                    activity.fillUserData(userDetail!!)
                }
            }

        }.addOnFailureListener { exception->
            BaseActivity().cancel_progressBar()
            Toast.makeText(activity,exception.message,Toast.LENGTH_LONG).show()
        }
    }

    fun getMembersOfBoard(activity:Activity,assignTo:ArrayList<String>){
        if(assignTo.size>0){
            DBfireStore.collection("Users").whereIn("id",assignTo).get().addOnSuccessListener {
                    document->
                val list:ArrayList<User> = ArrayList()
                for(i in document.documents){
                    val user=i.toObject(User::class.java)!!
                    list.add(user)
                }
                if(activity is MembersActivity){activity.onSuccessMembersListLoading(list)}
                else if(activity is TaskListActivity){activity.onSuccessGetMembers(list)}
                else if(activity is CardDetailActivity){activity.getAssignedMemberList(list)}

            }.addOnFailureListener {
                    e->
                if(activity is MembersActivity){activity.cancel_progressBar()}
                else if(activity is TaskListActivity){activity.cancel_progressBar()}
                else if(activity is CardDetailActivity){activity.cancel_progressBar()}

                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        else{
            val list:ArrayList<User> = ArrayList()
            if(activity is MembersActivity){activity.onSuccessMembersListLoading(list)}
            else if(activity is TaskListActivity){activity.onSuccessGetMembers(list)}
            else if(activity is CardDetailActivity){activity.getAssignedMemberList(list)}
        }

    }

    fun getMembersOfCard(context: CardListAdapter, assignto:ArrayList<String>,holder:RecyclerView.ViewHolder){

        DBfireStore.collection("Users").whereIn("id",assignto).get().addOnSuccessListener {
                document->
            val list:ArrayList<User> = ArrayList()
            for(i in document.documents){
                val user=i.toObject(User::class.java)!!
                list.add(user)
            }
            context.onGetMembersSuccess(list,holder)

        }.addOnFailureListener {
                e->
            holder.itemView.rvCardMemberList.visibility= View.GONE
        }

    }


    fun checkForUser(activity:MembersActivity,email:String){
        DBfireStore.collection("Users").whereEqualTo("email",email).get().addOnSuccessListener {
            document->
            if(document.documents.size>0){
                val user=document.documents[0].toObject(User::class.java)!!
                activity.onSuccessUserFind(user)
            }
            else{
                activity.cancel_progressBar()
                Toast.makeText(activity, "No user found with this email! Please register user first.", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            e->
            activity.cancel_progressBar()
            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun updatingMemberListOfBoard(activity:MembersActivity,assignTo:ArrayList<String>,boardId:String){
        val hashMap = HashMap<String,Any>()
        hashMap["assignTo"]=assignTo
        DBfireStore.collection("Boards").document(boardId).update(hashMap).addOnSuccessListener {
            task->
            activity.onSuccessMemberAdded()
        }.addOnFailureListener {
            e->
            activity.cancel_progressBar()
            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
        }
    }



    fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
}