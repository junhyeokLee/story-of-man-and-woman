package com.dev_sheep.story_of_man_and_woman.view.Fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev_sheep.story_of_man_and_woman.R
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Feed
import com.dev_sheep.story_of_man_and_woman.data.remote.APIService
import com.dev_sheep.story_of_man_and_woman.data.remote.APIService.FEED_SERVICE
import com.dev_sheep.story_of_man_and_woman.view.activity.FeedActivity
import com.dev_sheep.story_of_man_and_woman.view.adapter.FeedAdapter
import com.dev_sheep.story_of_man_and_woman.viewmodel.FeedViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_profile_bookmark.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileBookMarkFragment: Fragment() {
    private val feedViewModel: FeedViewModel by viewModel()
    private var recyclerView: RecyclerView? = null
    lateinit var mFeedAdapter: FeedAdapter
    lateinit var contexts: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_bookmark,container,false)
        contexts = view.context
        recyclerView = view.findViewById<View>(R.id.recyclerView) as RecyclerView?
        initData()
        return view
    }

    private fun initData() {
        // 저장된 m_seq 가져오기
        val preferences: SharedPreferences =
            context!!.getSharedPreferences("m_seq", Context.MODE_PRIVATE)
        val m_seq = preferences.getString("inputMseq", "")

        // 전체보기
        val single = FEED_SERVICE.getBookMark(m_seq)
        single.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { progressBarBookmark?.visibility = View.GONE }
            .subscribe({
                mFeedAdapter = FeedAdapter(
                    it,
                    contexts,
                    object : FeedAdapter.OnClickViewListener {
                        override fun OnClickFeed(feed: Feed,tv:TextView,iv: ImageView,cb:CheckBox,cb2:CheckBox,position:Int) {
                            feedViewModel.increaseViewCount(feed.feed_seq)

                            val lintent = Intent(context, FeedActivity::class.java)
                            lintent.putExtra("feed_seq", feed.feed_seq)
                            lintent.putExtra("checked" + feed.feed_seq, cb.isChecked)
                            lintent.putExtra("creater_seq", feed.creater_seq)
                            lintent.putExtra("bookmark_checked" + feed.feed_seq, cb2.isChecked)
                            lintent.putExtra(FeedActivity.EXTRA_POSITION, position)

//                        context.transitionName = position.toString()
                            context!!.startActivity(lintent)
                            (context as Activity).overridePendingTransition(R.anim.fragment_fade_in, R.anim.fragment_fade_out)

                        }
                    },
                    object : FeedAdapter.OnClickLikeListener {
                        override fun OnClickFeed(feed_seq: Int, boolean_value: String) {
                            feedViewModel.increaseLikeCount(feed_seq, boolean_value)
                        }

                    },object : FeedAdapter.OnClickBookMarkListener{
                        override fun OnClickBookMark(m_seq: String, feed_seq: Int, boolean_value: String) {
                            feedViewModel.onClickBookMark(m_seq,feed_seq,boolean_value)
                        }

                    }, object : FeedAdapter.OnClickProfileListener{
                        override fun OnClickProfile(feed: Feed, tv: TextView, iv: ImageView) {
                            val trId = ViewCompat.getTransitionName(tv).toString()
                            val trId1 = ViewCompat.getTransitionName(iv).toString()
                            if (feed.creater_seq == m_seq) {
                                activity?.supportFragmentManager
                                    ?.beginTransaction()
                                    ?.addSharedElement(tv, trId)
                                    ?.addSharedElement(iv, trId1)
                                    ?.addToBackStack("ProfileImg")
                                    ?.replace(
                                        R.id.frameLayout,
                                        ProfileFragment.newInstance(feed, trId, trId1)
                                    )
                                    ?.commit()
                            } else {
                                activity?.supportFragmentManager
                                    ?.beginTransaction()
                                    ?.addSharedElement(tv, trId)
                                    ?.addSharedElement(iv, trId1)
                                    ?.addToBackStack("ProfileImg")
                                    ?.replace(
                                        R.id.frameLayout,
                                        ProfileUsersFragment.newInstance(feed, trId, trId1)
                                    )
                                    ?.commit()
                            }
                        }

                    })
                recyclerView?.apply {
                    var linearLayoutMnager = LinearLayoutManager(this.context)
                    this.layoutManager = linearLayoutMnager
                    this.itemAnimator = DefaultItemAnimator()
                    this.adapter = mFeedAdapter
                }
//                if (it.isNotEmpty()) {
//                    progressBar?.visibility = View.GONE
//                } else {
//                    progressBar?.visibility = View.VISIBLE
//                }

            }, {
                Log.e("feed 보기 실패함", "" + it.message)
            })

    }

    override fun onResume() {
        super.onResume()
        initData()
    }
}