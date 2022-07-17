package eu.deysouvik.projectkaro.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import eu.deysouvik.projectkaro.Models.Task
import eu.deysouvik.projectkaro.Models.User
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.item_member.view.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MemberListAdapter(private val context: Context, private val list:ArrayList<User>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_member,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if(holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.photo)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.itemView.iv_member_image)

            holder.itemView.tv_member_name.text=model.name
            holder.itemView.tv_member_email.text=model.email
            holder.itemView.tv_member_phone.text=model.number.toString()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
}