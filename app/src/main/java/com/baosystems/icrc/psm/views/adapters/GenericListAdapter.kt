package com.baosystems.icrc.psm.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import org.hisp.dhis.android.core.common.BaseIdentifiableObject

class GenericListAdapter<T: BaseIdentifiableObject>(context: Context,
                                                    private val layoutResource: Int,
                                                    options: MutableList<T>
) : ArrayAdapter<T>(context, layoutResource, options) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView.let { convertView } ?:
        inflater.inflate(layoutResource, parent, false)

        val textView: TextView = view as TextView
        val objModel: T? = getItem(position)
        if (objModel != null) {
            textView.text = objModel.displayName()
        }

        return view
    }

    private val modelFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            val suggestions: MutableList<T> = ArrayList()
            if (constraint.isEmpty()) {
                suggestions.addAll(options)
            } else {
                val filterPattern = constraint.toString().lowercase().trim()
                for (item in options) {
                    if (item.displayName()?.lowercase()?.contains(filterPattern) == true) {
                        suggestions.add(item)
                    }
                }
            }
            results.values = suggestions
            results.count = suggestions.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }

        override fun convertResultToString(resultValue: Any): CharSequence {
            val objName = (resultValue as T).displayName()
            return objName?.subSequence(0, objName.length) ?:
            super.convertResultToString(resultValue)
        }
    }

    override fun getFilter(): Filter {
        return modelFilter
    }
}