package eu.deysouvik.projectkaro.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.item_board_member.view.*


class BoardAndCardMemberListAdapter(private val context: Context,
                                    private val boardMemberList:ArrayList<User>,
                                    private val assignMemberList:ArrayList<User>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener?=null
    private var hashmap:HashMap<String,Int> = HashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_board_member,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        fillHashMap(assignMemberList)
        val model=boardMemberList[position]
        if(holder is MyViewHolder){
            if(hashmap.containsKey(model.id)){holder.itemView.iv_card_selected_member.visibility=View.VISIBLE}
            else{holder.itemView.iv_card_selected_member.visibility=View.GONE}
            Glide
                .with(context)
                .load(model.photo)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.itemView.iv_board_member_image)
            holder.itemView.tv_board_member_name.text=model.name
            holder.itemView.tv_board_member_email.text=model.email
            holder.itemView.tv_board_member_phone.text=model.number.toString()

            holder.itemView.setOnClickListener {
                if(hashmap.containsKey(model.id)==false){
                    if(onClickListener!=null){
                        onClickListener!!.onClick(position,model)
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return boardMemberList.size
    }

    interface OnClickListener{
        fun onClick(position:Int,member:User)
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onClickListener=onClickListener
    }

    private fun fillHashMap(list:ArrayList<User>){
        for(i in list){
            hashmap.put(i.id,1)
        }
    }

    private class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
}