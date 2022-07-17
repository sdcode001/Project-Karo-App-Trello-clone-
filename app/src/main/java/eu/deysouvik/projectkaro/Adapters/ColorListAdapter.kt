package eu.deysouvik.projectkaro.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eu.deysouvik.projectkaro.R
import kotlinx.android.synthetic.main.item_label_color.view.*

class ColorListAdapter(private val context: Context,
                       private val list:ArrayList<String>,
                       private val selectedColor:String):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_label_color,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val color=list[position]
        if(holder is MyViewHolder){
            holder.itemView.view_main.setBackgroundColor(Color.parseColor(color))
            if(color==selectedColor){holder.itemView.iv_selected_color.visibility=View.VISIBLE}
            else{holder.itemView.iv_selected_color.visibility=View.GONE}

            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                    onClickListener!!.onClick(position,color)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener{
        fun onClick(position:Int,color: String)
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onClickListener=onClickListener
    }

    private class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
}