package com.orm.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.orm.R
import com.orm.data.model.recycler.RecyclerViewBoardItem

class ProfileBoardAdapter(private val items: List<RecyclerViewBoardItem>) :
    RecyclerView.Adapter<ProfileBoardAdapter.ProfileBoardViewHolder>() {
    private lateinit var itemClickListener: OnItemClickListener

        inner class ProfileBoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
            val tvUserNickname = itemView.findViewById<TextView>(R.id.tv_userNickname)
            val tvHit = itemView.findViewById<TextView>(R.id.tv_hit)
            val tvCreatdAt = itemView.findViewById<TextView>(R.id.tv_createdAt)
            val tvCommentCount = itemView.findViewById<TextView>(R.id.tv_commentCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileBoardViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.profile_board, parent, false)
            return ProfileBoardViewHolder(view)
        }

        override fun onBindViewHolder(holder: ProfileBoardViewHolder, position: Int) {
            holder.tvTitle.text = items[position].title
            holder.tvUserNickname.text = items[position].userNickname
            holder.tvHit.text = items[position].hit.toString()
            holder.tvCommentCount.text = items[position].commentCount.toString()
            holder.tvCreatdAt.text = items[position].createdAt
            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, position)
            }
        }

        override fun getItemCount(): Int {
            return items.count()
        }

        interface OnItemClickListener {
            fun onClick(v: View, position: Int)
        }

        fun setItemClickListener(onItemClickListener: OnItemClickListener) {
            this.itemClickListener = onItemClickListener
        }

    }

