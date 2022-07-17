package eu.deysouvik.projectkaro.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.R
import eu.deysouvik.projectkaro.activity.Activities.CardDetailActivity
import kotlinx.android.synthetic.main.item_card_selected_member.view.*
import kotlinx.android.synthetic.main.item_member.view.*

class AssignedMemberListAdapter(private val context: Context, private val list:ArrayList<User>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card_selected_member,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item=list[position]
        if(holder is MyViewHolder){
            if(position==list.size-1){
                holder.itemView.ll_selected_member.visibility=View.GONE
                holder.itemView.iv_add_member.visibility=View.VISIBLE
            }
            else{
                holder.itemView.ll_selected_member.visibility=View.VISIBLE
                holder.itemView.iv_add_member.visibility=View.GONE

                Glide
                    .with(context)
                    .load(item.photo)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.itemView.iv_selected_member_image)

                holder.itemView.tv_card_member_name.text=item.name
            }


            holder.itemView.ll_selected_member.setOnClickListener {
                if(context is CardDetailActivity){
                    context.deleteAssignMember(position)
                }
            }
            holder.itemView.iv_add_member.setOnClickListener {
                if(context is CardDetailActivity){
                    context.showDialogForAddNewMemberToCard()
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
}