package ua.com.expert.validator.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.hall_list_item.view.container
import kotlinx.android.synthetic.main.session_list_item.view.*
import okhttp3.OkHttpClient
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.common.Consts.APP_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.CONNECT_SERVER_URL
import ua.com.expert.validator.common.Consts.GET_IMAGE_PATTERN_URL
import ua.com.expert.validator.common.Consts.SERVER
import ua.com.expert.validator.common.Consts.TOKEN_HEADER
import ua.com.expert.validator.common.Consts.TYPE_CONNECTION
import ua.com.expert.validator.model.dto.CinemaSession
import ua.com.expert.validator.utils.SharedStorage
import java.util.*


class SessionsListAdapter(val mContext: Context, private val clickListener: ClickListener) : RecyclerView.Adapter<SessionsListAdapter.ViewHolder>() {

    private var list: List<CinemaSession> = ArrayList()
    private var layoutInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val picasso = getPicassoLoader()
    private val imagePath : String = TYPE_CONNECTION +
            SharedStorage.getString(mContext, APP_SETTINGS_PREFS, SERVER, CONNECT_SERVER_URL)+ "/" + GET_IMAGE_PATTERN_URL + "?id="

    fun setList(list: List<CinemaSession>?) {
        if(list==null){
            return
        }
        this.list = list
        this.notifyDataSetChanged()
    }

    fun clear() {
        this.list = ArrayList()
        this.notifyDataSetChanged()
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view){


        val container = view.container!!
        val name = view.name!!
        val start = view.start!!
        val end = view.end!!
        val icon = view.icon!!

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(layoutInflater!!.inflate(R.layout.session_list_item, viewGroup, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {

        val item = getSelectedItem(p1)
        if(item!=null) {
            holder.container.setOnClickListener { clickListener.onItemClick(p1) }
            holder.name?.text = item.filmName
            holder.start?.text = item.dateStart.toString()
            holder.end?.text = item.dateFinish.toString()

            picasso.load(imagePath + item.filmID)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .resizeDimen(R.dimen.session_image_width, R.dimen.session_image_height)
                    .centerCrop()
                    .into(holder.icon)
        }
    }


    override fun getItemCount(): Int {
        return list!!.size
    }

    fun getSelectedItem(position: Int): CinemaSession? {
        return list!![position]
    }

    private fun getPicassoLoader() : Picasso{
         return Picasso.Builder(mContext)
                .downloader(OkHttp3Downloader(OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val newRequest = chain.request().newBuilder()
                                    .addHeader(TOKEN_HEADER,
                                            SharedStorage.getString(mContext, Consts.APP_CASH_TOKEN_PREFS, Consts.TOKEN, ""))
                                    .build()
                            chain.proceed(newRequest)
                        }
                        .build()))
                .build()
    }

    interface ClickListener {
        fun onItemClick(position: Int)
    }

}