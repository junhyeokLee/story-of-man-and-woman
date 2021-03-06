package com.dev_sheep.story_of_man_and_woman.view.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dev_sheep.story_of_man_and_woman.R

class HomeRankFragment(position: Int) : Fragment() {
    private val ARG_PARAM1 = "param1"
    private var mParam1 = 0
    private var position = position

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rank,null)
        val tv:TextView = view.findViewById(R.id.tv_number)
        val rank_img: ImageView = view.findViewById(R.id.iv_rank_img)
        val rank_tag_img: ImageView = view.findViewById(R.id.iv_rank_tag_img)

        // 오늘의 사연, 이달의 사연, 많이 본 사연 , 많은 응원중.., 공지사항

        // 오늘의 베스트 top5, 이달의 베스트 top5

        if (arguments != null) {
            mParam1 = arguments!!.getInt(ARG_PARAM1)
        }
        if(position == 1){
            tv.setText("오늘의 사연")
            rank_tag_img.setBackgroundResource(0)
//            rank_tag_img.setBackgroundResource(R.drawable.ic_thumbs_up)
            rank_img.setBackgroundResource(R.drawable.couple)
        }else if(position == 2){
            tv.setText("이달의 사연")
            rank_tag_img.setBackgroundResource(0)
//            rank_tag_img.setBackgroundResource(R.drawable.ic_first)
            rank_img.setBackgroundResource(R.drawable.bike)
        }
        else if(position == 3){
            tv.setText("화제의 사연")
            rank_tag_img.setBackgroundResource(0)
//            rank_tag_img.setBackgroundResource(R.drawable.ic_offer)
            rank_img.setBackgroundResource(R.drawable.christmas)
        }
        else if(position == 4){
            tv.setText("동이들의 선택")
            rank_tag_img.setBackgroundResource(0)
//            rank_tag_img.setBackgroundResource(R.drawable.ic_crown)
            rank_img.setBackgroundResource(R.drawable.ic_thumbs_up)
        }
        else if(position == 5){
            tv.setText("공지사항")
            rank_tag_img.setBackgroundResource(0)
            rank_img.setBackgroundResource(R.drawable.bike)
        }

//                chefImage.drawable.setColorFilter(
//                    ContextCompat.getColor(view.context, R.color.colorPrimary),
//                    PorterDuff.Mode.DST_ATOP)
//                experience.text = view.context.getString(R.string.chef_years_of_experience)

//                messageButton.setOnClickListener { presenter.onMessageChef(this) }
//
//                setFavoriteDrawable(chefModel.favorited,view,favoriteButton)
//                favoriteButton.setOnClickListener {
//                    val favorited = !chefModel.favorited
//                    setFavoriteDrawable(favorited)
//                    presenter.onFavoriteChef(this, favorited)
//                }
//
//                setBookmarkDrawable(chefModel.bookmarked)
//                bookmarkButton.setOnClickListener {
//                    val bookmarked = !chefModel.bookmarked
//                    setBookmarkDrawable(bookmarked,view,bookmarkButton)
//                    presenter.onBookmarkChef(this, bookmarked)
//                }
//
//            view.setOnClickListener { presenter.onChefSelected(chefModel.getName()) }


        return view
    }
}