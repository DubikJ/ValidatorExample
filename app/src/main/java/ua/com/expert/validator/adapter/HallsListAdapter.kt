package ua.com.expert.validator.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.hall_list_item.view.*
import ua.com.expert.validator.R
import ua.com.expert.validator.model.dto.CinemaPlace
import java.util.*

class HallsListAdapter(var mContext: Context, private val clickListener: ClickListener) : RecyclerView.Adapter<HallsListAdapter.ViewHolder>() {

    var list: List<CinemaPlace> = ArrayList()
    private var layoutInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun clear() {
        this.list = ArrayList()
        this.notifyDataSetChanged()
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view){


        val container = view.container
        val name = view.hall_list_name
        val count = view.hall_list_count

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.hall_list_item, viewGroup, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {

        val item = getSelectedItem(p1)
        if(item!=null) {
            holder.container.setOnClickListener { clickListener.onItemClick(p1) }
            holder.name?.text = item.name
            holder.count?.text = item.placeCount.toString()+" "+mContext.resources.getString(R.string.places)
        }
    }



    override fun getItemCount(): Int {
        return list.size
    }

    fun getSelectedItem(position: Int): CinemaPlace? {
        return list[position]
    }

    interface ClickListener {
        fun onItemClick(position: Int)
    }

}