package com.dev_sheep.story_of_man_and_woman.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev_sheep.story_of_man_and_woman.R
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Tag
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Test
import com.dev_sheep.story_of_man_and_woman.view.activity.FeedSearchActivity
import kotlinx.android.synthetic.main.adpater_tag.view.*

class Test_Searchtag_Adapter(
    private val list: List<Tag>,
    private val context: Context
) : RecyclerView.Adapter<Test_Searchtag_Adapter.ViewHolder>() {



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(item: Tag) {

            itemView.tag_name.text = item.tag_name+"   "

            itemView.tag_name.setOnClickListener {
                val intent = Intent(itemView.context, FeedSearchActivity::class.java)
                intent.putExtra("tag_seq",item.tag_seq.toString())
                intent.putExtra("tag_name",item.tag_name)
                itemView.context.startActivity(intent)

            }

//1 . # 모든사연
//2 . # 잡담
//3 . # 남과 여
//4 . # 고민/질문
//5 . # 남자 이야기
//6 . # 여자 이야기
//7 . # Yes or No

//            val radius = itemView.resources.getDimensionPixelSize(R.dimen.corner_radius)



        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_search_tag, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.bindView(item)
    }

    override fun getItemCount(): Int {
        return list.size   // 7개 까지만
    }


}
