package com.orm.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.orm.R
import com.orm.data.model.recycler.RecyclerViewButtonItem
import com.orm.viewmodel.UserViewModel

class ProfileButtonAdapter(private var items: List<RecyclerViewButtonItem>) :
    RecyclerView.Adapter<ProfileButtonAdapter.ProfileButtonViewHolder>() {
    private lateinit var itemClickListener: OnItemClickListener
    private lateinit var type: String
    private lateinit var userId: String
    private lateinit var managerId: String

    inner class ProfileButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        val tvMain: TextView = itemView.findViewById(R.id.tv_main)
        val tvSub: TextView = itemView.findViewById(R.id.tv_sub)
        val btnUp: Button = itemView.findViewById(R.id.btn_accept)
        val btnDown: Button = itemView.findViewById(R.id.btn_reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileButtonViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.profile_button, parent, false)
        return ProfileButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileButtonViewHolder, position: Int) {
        items[position].imageSrc.getNetworkImage(
            holder.itemView.context,
            holder.ivThumbnail
        )
        holder.tvMain.text = items[position].title
        holder.tvSub.text = items[position].subTitle

        if (this.type == "member") {
            val curId = items[position].id
            if (curId == userId.toInt() && userId != managerId) {
                holder.btnDown.visibility = View.GONE
                holder.btnUp.text = "탈퇴"
            } else if (curId != userId.toInt() && userId == managerId) {
                holder.btnUp.visibility = View.GONE
                holder.btnDown.text = "추방"
            } else if (curId == managerId.toInt()) {
                holder.btnUp.visibility = View.GONE
                holder.btnDown.text = "모임장"
            } else {
                holder.btnUp.visibility = View.GONE
                holder.btnDown.visibility = View.GONE
            }
            holder.tvSub.visibility = View.GONE
        }


        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
        holder.btnUp.setOnClickListener {
            itemClickListener.onClickBtnUp(it, position)
        }
        holder.btnDown.setOnClickListener {
            itemClickListener.onClickBtnDown(it, position)
        }
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
        fun onClickBtnUp(v: View, position: Int)
        fun onClickBtnDown(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    fun setType(type: String) {
        this.type = type
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }

    fun setManagerId(managerId: String) {
        this.managerId = managerId
    }

    fun removeItem(position: Int) {
        val mutableItems = items.toMutableList()
        mutableItems.removeAt(position)
        items = mutableItems
        notifyItemRemoved(position)
    }

    fun addItem(item: RecyclerViewButtonItem, position: Int) {
        val mutableItems = items.toMutableList()
        mutableItems.add(position, item)
        items = mutableItems
        notifyItemInserted(position)
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

