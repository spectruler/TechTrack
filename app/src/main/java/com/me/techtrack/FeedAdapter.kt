package com.me.techtrack

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ViewHolder(v:View){
    val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
    val tvPubDate = v.findViewById<TextView>(R.id.textViewDate)
}

class FeedAdapter(context: Context, private val resourceId: Int,
                  private val applications: List<FeedEntry>):
    ArrayAdapter<FeedEntry>(context,resourceId){
    private val TAG = "FeedAdapter"
    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        Log.d(TAG,"getView: called")
        val view: View
        val viewHolder: ViewHolder
        if(convertView == null){
            view = inflater.inflate(resourceId,parent,false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        }else{
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val currentApp = applications[position]
        viewHolder.tvTitle.text = currentApp.title
        viewHolder.tvPubDate.text = currentApp.pubDate
        return view

    }

    override fun getCount(): Int {
        return applications.size
    }
}