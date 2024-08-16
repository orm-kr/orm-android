package com.orm.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.orm.R
import com.orm.data.model.recycler.RecyclerViewCommentItem

class ProfileCommentAdapter(
    private var items: List<RecyclerViewCommentItem>,
    private val currentUserId: String,
    private val onEditClick: (RecyclerViewCommentItem) -> Unit,
    private val onDeleteClick: (RecyclerViewCommentItem) -> Unit
) : RecyclerView.Adapter<ProfileCommentAdapter.ProfileCommentViewHolder>() {

    inner class ProfileCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val tvUserNickname: TextView = itemView.findViewById(R.id.tv_userNickname)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.tv_createdAt)
        val tvEdit: TextView = itemView.findViewById(R.id.tv_edit)
        val tvDelete: TextView = itemView.findViewById(R.id.tv_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileCommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_comment, parent, false)
        return ProfileCommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileCommentViewHolder, position: Int) {
        val item = items[position]
        holder.tvContent.text = item.content
        holder.tvUserNickname.text = item.userNickname
        holder.tvCreatedAt.text = "${item.createdAt}   "

        // 현재 사용자의 ID와 댓글 작성자의 ID를 비교
        if (currentUserId == item.userId.toString()) {
            holder.tvEdit.visibility = View.VISIBLE
            holder.tvDelete.visibility = View.VISIBLE

            holder.tvEdit.setOnClickListener {
                onEditClick(item)
            }

            holder.tvDelete.setOnClickListener {
                onDeleteClick(item)
            }
        } else {
            holder.tvEdit.visibility = View.GONE
            holder.tvDelete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(updatedItems: List<RecyclerViewCommentItem>) {
        items = updatedItems
        notifyDataSetChanged()
    }

    fun getItems(): List<RecyclerViewCommentItem> {
        return items
    }
}