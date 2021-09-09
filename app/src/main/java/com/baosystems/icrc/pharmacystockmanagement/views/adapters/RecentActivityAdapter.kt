package com.baosystems.icrc.pharmacystockmanagement.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.baosystems.icrc.pharmacystockmanagement.data.TransactionType
import com.baosystems.icrc.pharmacystockmanagement.data.models.UserActivity
import com.baosystems.icrc.pharmacystockmanagement.databinding.ListItemRecentActivityBinding
import java.time.format.DateTimeFormatter

class ViewHolder private constructor(val binding: ListItemRecentActivityBinding):
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UserActivity) {
        binding.transactionTypeTextview.text = item.type.name
        binding.creationDateTextview.text = item.date.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )

        if(item.type == TransactionType.DISTRIBUTION) {
            binding.distributedToTextview.text = item.distributedTo?.name ?: ""
        }
    }

    companion object {
        fun from(parent: ViewGroup): ViewHolder {
            val view = ListItemRecentActivityBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            return ViewHolder(view)
        }
    }
}

class RecentActivityAdapter:
    ListAdapter<UserActivity, ViewHolder> (RecentActivityItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)

        // TODO: add listeners on the properties as required

        viewHolder.bind(item)
        Log.d("RAA", "Bind item: " + item.distributedTo)
    }
}

private class RecentActivityItemDiffCallback: DiffUtil.ItemCallback<UserActivity>() {
    override fun areItemsTheSame(oldItem: UserActivity, newItem: UserActivity) = oldItem == newItem

    override fun areContentsTheSame(oldItem: UserActivity, newItem: UserActivity) =
        oldItem.type == newItem.type && oldItem.distributedTo == newItem.distributedTo &&
                oldItem.date == newItem.date
}