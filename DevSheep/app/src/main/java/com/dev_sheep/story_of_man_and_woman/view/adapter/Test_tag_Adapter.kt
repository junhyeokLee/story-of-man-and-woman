package com.dev_sheep.story_of_man_and_woman.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dev_sheep.story_of_man_and_woman.R
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Feed
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Tag
import com.dev_sheep.story_of_man_and_woman.view.activity.FeedSearchActivity
import com.dev_sheep.story_of_man_and_woman.view.activity.MystoryActivity
import kotlinx.android.synthetic.main.adpater_tag.view.*

abstract class Test_tag_Adapter(
    private val list: List<Tag>,
    private val context: Context
) : RecyclerView.Adapter<Test_tag_Adapter.ViewHolder>() {


    abstract fun load()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("ResourceAsColor")
        fun bindView(item: Tag) {

            itemView.tag_name.text = "  # "+item.tag_name+"   "

//            Shader textShader=new LinearGradient(0, 0, 0, 20, new int[]{
//                R.color.colorAccent,R.color.colorPrimary
//            },new float[]{0, 1}, TileMode.CLAMP);
//            textView.getPaint().setShader(textShader);

            val paint =  itemView.tag_name.paint
            val width = paint.measureText(itemView.tag_name.text.toString())
            val textShader: Shader = LinearGradient(0f, 0f, width, itemView.tag_name.textSize, intArrayOf(
                Color.parseColor("#ec6674"),
//                Color.parseColor("#429BED"),
//                Color.parseColor("#64B678"),
//                Color.parseColor("#478AEA"),
                Color.parseColor("#7A5DC7")
            ), null, Shader.TileMode.REPEAT)

            itemView.tag_name.paint.shader = textShader


            itemView.tag_name.setOnClickListener {

                val intent = Intent(itemView.context, FeedSearchActivity::class.java)
                intent.putExtra("tag_seq",item.tag_seq.toString())
                intent.putExtra("tag_name",item.tag_name)
                itemView.context.startActivity(intent)

            }

//1 . # 모든사연
//2 . # 잡담
//3 . # 남과 여
//4 . # 고민상담
//5 . # 남자 이야기
//6 . # 여자 이야기
//7 . # 사랑 이야기
//8 . # 연애 이야기
//9 . # 아이들 이야기
//10. # 가족 이야기


            // 오늘의 사연, 이달의 사연, 많이 본 사연 , 많은 응원중.., 공지사항

            // 오늘의 베스트 top5, 이달의 베스트 top5


//            val radius = itemView.resources.getDimensionPixelSize(R.dimen.corner_radius)



        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.adpater_tag, parent, false)
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


