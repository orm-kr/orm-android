package com.orm.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.orm.R
import com.orm.data.model.recycler.RecyclerViewNotificationItem

class ProfileNotificationAdapter(private val items: List<RecyclerViewNotificationItem>) :
    RecyclerView.Adapter<ProfileNotificationAdapter.ProfileNotificationViewHolder>() {
    private lateinit var itemClickListener: OnItemClickListener

    inner class ProfileNotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumbnail = itemView.findViewById<ImageView>(R.id.iv_thumbnail)
        val tvMain = itemView.findViewById<TextView>(R.id.tv_main)
        val tvSub = itemView.findViewById<TextView>(R.id.tv_sub)
        val tvDate = itemView.findViewById<TextView>(R.id.tv_date)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProfileNotificationViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_notification, parent, false)
        return ProfileNotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileNotificationViewHolder, position: Int) {
        items[position].imageSrc.getNetworkImage(
            holder.itemView.context,
            holder.ivThumbnail
        )
        holder.tvMain.text = items[position].title
        holder.tvSub.text = items[position].subTitle
        holder.tvDate.text = items[position].date

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
        holder.itemView.setOnLongClickListener {
            itemClickListener.onLongClick(it, position)
            true
        }
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
        fun onLongClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private fun String.getNetworkImage(context: Context, view: ImageView) {
        Glide.with(context)
            .load(this)
            .error(R.drawable.img_orm_1000)
            .placeholder(R.drawable.img_orm_1000)
            .centerCrop()
            .into(view)
    }
}

