package eu.deysouvik.projectkaro.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.deysouvik.projectkaro.FireBase.FireStore
import eu.deysouvik.projectkaro.Models.Board
import eu.deysouvik.projectkaro.Models.Card
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.item_card.view.*
import java.util.*
import kotlin.collections.ArrayList


class CardListAdapter(private val context:Context,private val list:ArrayList<Card>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_card: Int) {
         val model:Card=list[position_card]
        holder.itemView.tv_card_name.text=model.name
        if(model.color!=""){
            holder.itemView.view_label_color.visibility=View.VISIBLE
            holder.itemView.view_label_color.setBackgroundColor(Color.parseColor(model.color))
        }else{
            holder.itemView.view_label_color.visibility=View.GONE
        }
        if(model.dueDate>0){
            holder.itemView.tv_due_date.visibility=View.VISIBLE
            val sdf=java.text.SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val date=sdf.format(Date(model.dueDate))
            holder.itemView.tv_due_date.text="Due date- $date"
        }
        else{holder.itemView.tv_due_date.visibility=View.GONE}
        if(model.assignTo.size>0){
            holder.itemView.rvCardMemberList.visibility=View.VISIBLE
            FireStore().getMembersOfCard(this,model.assignTo,holder)
        }
        else{
            holder.itemView.rvCardMemberList.visibility=View.GONE
        }

        holder.itemView.setOnClickListener {
            if(onClickListener!=null){
                onClickListener!!.onClick(position_card)
            }
        }
        holder.itemView.rvCardMemberList.setOnClickListener {
            if(onClickListener!=null){
                onClickListener!!.onClick(position_card)
            }
        }

    }

    override fun getItemCount(): Int {
       return list.size
    }

    interface OnClickListener{
        fun onClick(position_card:Int)
    }

    fun setOnClickListener(onClickListener: CardListAdapter.OnClickListener){
        this.onClickListener=onClickListener
    }


    fun onGetMembersSuccess(list:ArrayList<User>,holder: RecyclerView.ViewHolder){
        if(list.size>0){
            holder.itemView.rvCardMemberList.layoutManager=GridLayoutManager(context,4)
            val adapter=CardMemberListFirstLevelAdapter(context,list)
            holder.itemView.rvCardMemberList.adapter=adapter

        }
        else{
            holder.itemView.rvCardMemberList.visibility=View.GONE
        }
    }

    private class MyViewHolder(view: View):RecyclerView.ViewHolder(view)


}