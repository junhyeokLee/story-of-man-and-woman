package com.dev_sheep.story_of_man_and_woman.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev_sheep.story_of_man_and_woman.R
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Feed
import com.dev_sheep.story_of_man_and_woman.data.remote.APIService.FEED_SERVICE
import com.dev_sheep.story_of_man_and_woman.utils.OnLoadMoreListener
import com.dev_sheep.story_of_man_and_woman.view.adapter.FeedAdapterRank
import com.dev_sheep.story_of_man_and_woman.viewmodel.FeedViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_feed_rank.*
import kotlinx.android.synthetic.main.activity_feed_rank.nestedScrollView
import kotlinx.android.synthetic.main.activity_feed_rank.progressBar
import kotlinx.android.synthetic.main.activity_feed_search.recyclerView
import kotlinx.android.synthetic.main.activity_feed_search.shimmer_view_container
import kotlinx.android.synthetic.main.activity_feed_search.sr_refresh
import kotlinx.android.synthetic.main.activity_feed_search.toolbar
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.sign

class FeedRankActivity: AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener{

    //    lateinit var tag_seq: String
    lateinit var tv_name: String
    lateinit var mFeedAdapterRank: FeedAdapterRank
    private val feedViewModel: FeedViewModel by viewModel()
    private lateinit var m_seq : String
    private lateinit var my_Age : String
    private var single : Single<List<Feed>>? = null
    private var limit: Int = 10
    private var offset: Int = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var lastVisibleItemPosition = 0
    private lateinit var linearLayoutManager: LinearLayoutManager
//    private var isLoading = false
    private var isFirstTime = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_rank)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);

        sr_refresh.setOnRefreshListener(this)
        sr_refresh.setColorSchemeResources(
            R.color.main_Accent
        )
        // my_m_seq 가져오기
        val preferences: SharedPreferences = this!!.getSharedPreferences(
            "m_seq",
            Context.MODE_PRIVATE
        )
        m_seq = preferences.getString("inputMseq", "")
        my_Age = preferences.getString("inputAge", "")

        if(intent.hasExtra("tv_title")) {
            tv_name = intent.getStringExtra("tv_title")
            tv_tag_rank_name.text = tv_name
        }

        initData()
    }

    @SuppressLint("CheckResult")
    private fun initData(){


        // display loading indicator
        val handlerFeed: Handler = Handler(Looper.myLooper())
        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        // tag_search
        Log.e("tv_name",""+tv_name)

        if(tv_name.equals("오늘의 관심사")) {
            single = FEED_SERVICE.getTodayList(offset,limit)
        } else if(tv_name.equals(my_Age+" 여성들이 좋아하는")) {
         single = FEED_SERVICE.getAgeWomanRecommendList(my_Age,offset,limit)
        } else if(tv_name.equals(my_Age+" 남성들이 좋아하는")) {
        single = FEED_SERVICE.getAgeManRecommendList(my_Age,offset,limit)
        } else if(tv_name.equals("가장 많이 읽은 카드")) {
            single = FEED_SERVICE.getViewRecommendList(offset,limit)
        } else if(tv_name.equals("가장 많이 좋아한 카드")) {
            single = FEED_SERVICE.getLikeRecommendList(offset,limit)
        }

        single?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                if (it.size == 0) {
                    shimmer_view_container?.visibility = View.GONE
                } else {
                    Log.e("FeedList", "" + it.get(0).content)

                    mFeedAdapterRank = FeedAdapterRank(it, this, object : FeedAdapterRank.OnClickViewListener {
                        override fun OnClickFeed(
                            feed: Feed,
                            tv: TextView,
                            iv: ImageView,
                            cb: CheckBox,
                            cb2: CheckBox,
                            position: Int
                        ) {
                            feedViewModel.increaseViewCount(feed.feed_seq)

                            val lintent = Intent(applicationContext, FeedActivity::class.java)
                            lintent.putExtra("feed_seq", feed.feed_seq)
                            lintent.putExtra("checked" + feed.feed_seq, cb.isChecked)
                            lintent.putExtra("creater_seq", feed.creater_seq)
                            lintent.putExtra("bookmark_checked" + feed.feed_seq, cb2.isChecked)
                            lintent.putExtra(FeedActivity.EXTRA_POSITION, position)
//                        context.transitionName = position.toString()
                            startActivity(lintent)
                            overridePendingTransition(
                                R.anim.fragment_fade_in,
                                R.anim.fragment_fade_out
                            )

                        }
                    }, object : FeedAdapterRank.OnClickLikeListener {
                        override fun OnClickFeed(feed_seq: Int, boolean_value: String) {
                            feedViewModel.increaseLikeCount(feed_seq, boolean_value)
                        }

                    }, object : FeedAdapterRank.OnClickBookMarkListener {
                        override fun OnClickBookMark(
                            m_seq: String,
                            feed_seq: Int,
                            boolean_value: String
                        ) {
                            feedViewModel.onClickBookMark(m_seq, feed_seq, boolean_value)
                        }

                    })

                    handlerFeed.postDelayed({
                        // stop animating Shimmer and hide the layout
                        shimmer_view_container.stopShimmerAnimation()
                        shimmer_view_container.visibility = View.GONE
                        recyclerView?.apply {
//                            linearLayoutManager = LinearLayoutManager(this.context)
                            this.layoutManager = linearLayoutManager
                            this.itemAnimator = DefaultItemAnimator()
                            this.adapter = mFeedAdapterRank

                        }
                    }, 1000)
                }

            }, {
                Log.e("feed 보기 실패함", "" + it.message)
            })

        // 무한스크롤
        nestedScrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(
                v: NestedScrollView?,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {

                if (v?.getChildAt(v.getChildCount() - 1) != null) {
                    progressBar.visibility = View.VISIBLE

                    if (scrollY >= v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight() && scrollY > oldScrollY) {
                        visibleItemCount = linearLayoutManager.getChildCount()
                        totalItemCount = linearLayoutManager.getItemCount()
                        lastVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
                            if (visibleItemCount + lastVisibleItemPosition >= totalItemCount) {
//                                Handler().postDelayed({
                                    LoadMoreData()
//                                },1000)

                            }else{
                                progressBar.visibility = View.GONE
                            }
                    }
                }
            }
        })

    }

    private fun LoadMoreData() {
        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        Handler().postDelayed({
            val single = FEED_SERVICE.getListScroll(offset, addLimit())
            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    mFeedAdapterRank =
                        FeedAdapterRank(it, this, object : FeedAdapterRank.OnClickViewListener {
                            override fun OnClickFeed(
                                feed: Feed,
                                tv: TextView,
                                iv: ImageView,
                                cb: CheckBox,
                                cb2: CheckBox,
                                position: Int
                            ) {
                                feedViewModel.increaseViewCount(feed.feed_seq)

                                val lintent = Intent(this as Activity, FeedActivity::class.java)
                                lintent.putExtra("feed_seq", feed.feed_seq)
                                lintent.putExtra("checked" + feed.feed_seq, cb.isChecked)
                                lintent.putExtra("creater_seq", feed.creater_seq)
                                lintent.putExtra("feed_title",feed.title)
                                lintent.putExtra("bookmark_checked" + feed.feed_seq, cb2.isChecked)
                                lintent.putExtra(FeedActivity.EXTRA_POSITION, position)
//                        context.transitionName = position.toString()
                                (this as Activity).startActivity(lintent)
                                (this as Activity).overridePendingTransition(
                                    R.anim.fragment_fade_in,
                                    R.anim.fragment_fade_out
                                )

                            }
                        }, object : FeedAdapterRank.OnClickLikeListener {
                            override fun OnClickFeed(feed_seq: Int, boolean_value: String) {
                                feedViewModel.increaseLikeCount(feed_seq, boolean_value)
                            }

                        }, object : FeedAdapterRank.OnClickBookMarkListener {
                            override fun OnClickBookMark(
                                m_seq: String,
                                feed_seq: Int,
                                boolean_value: String
                            ) {
                                feedViewModel.onClickBookMark(m_seq, feed_seq, boolean_value)
                            }

                        })
                    recyclerView?.apply {
//                            var linearLayoutMnager = LinearLayoutManager(this.context)
                        this.layoutManager = linearLayoutManager
                        this.itemAnimator = DefaultItemAnimator()
                        this.adapter = mFeedAdapterRank
                    }

                }, {
                    Log.d("스크롤 보기 실패함", "" + it.message)
                })
            progressBar.visibility = View.GONE
        }, 1000)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        when(item?.itemId){
            android.R.id.home ->{
                finish()
                return true
            }
        }
        return true
    }

    override fun onRefresh() {
        initData()
        sr_refresh.setRefreshing(false)
    }

    override fun onResume() {
        super.onResume()
        initData()
        shimmer_view_container.startShimmerAnimation()

    }

    override fun onPause() {
        super.onPause()
        initData()
        shimmer_view_container.stopShimmerAnimation()
    }

    private fun addLimit() : Int{
        limit += 10
        return limit
    }

    private fun addOffset(): Int{
        offset += 10
        return offset
    }

//    fun getLoad(): Boolean{
//        return isLoading
//    }



}