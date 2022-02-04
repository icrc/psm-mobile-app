package com.baosystems.icrc.psm.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.persistence.UserActivity
import com.baosystems.icrc.psm.databinding.ListItemRecentActivityBinding
import com.baosystems.icrc.psm.utils.DateUtils

class ViewHolder private constructor(val binding: ListItemRecentActivityBinding):
    RecyclerView.ViewHolder(binding.root) {
    fun bindTo(item: UserActivity) {
        val transactionMessageResource = when (item.type) {
            TransactionType.DISTRIBUTION -> {
                R.string.distribution
            }
            TransactionType.CORRECTION -> {
                R.string.correction
            }
            else -> {
                R.string.discard
            }
        }

        binding.raTransactionTypeTextView.text = binding.root.context.getText(transactionMessageResource)
        binding.raCreationDateTextView.text = item.date.format(DateUtils.getDateTimePattern())

        if(item.type == TransactionType.DISTRIBUTION) {
            binding.raDistributedToTextView.text = item.distributedTo ?: ""
            binding.raDirectionalArrowImageView.visibility = View.VISIBLE
        }

        when (item.type) {
            TransactionType.DISTRIBUTION -> {
                binding.raDistributedToTextView.text = item.distributedTo ?: ""
                binding.raDirectionalArrowImageView.visibility = View.VISIBLE
            } else -> {
                binding.raDirectionalArrowImageView.visibility = View.GONE
            }
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

class RecentActivityAdapter: ListAdapter<UserActivity, ViewHolder> (DIFF_CALLBACK) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UserActivity>() {
            override fun areItemsTheSame(oldItem: UserActivity, newItem: UserActivity) = oldItem == newItem

            override fun areContentsTheSame(oldItem: UserActivity, newItem: UserActivity) =
                oldItem.type == newItem.type && oldItem.distributedTo == newItem.distributedTo &&
                        oldItem.date == newItem.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.bindTo(item)
    }
}