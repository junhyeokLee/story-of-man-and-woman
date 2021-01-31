package com.dev_sheep.story_of_man_and_woman.view.Fragment

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.dev_sheep.story_of_man_and_woman.R
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Feed
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Tag
import com.dev_sheep.story_of_man_and_woman.data.remote.APIService.FEED_SERVICE
import com.dev_sheep.story_of_man_and_woman.view.activity.AlarmActivity
import com.dev_sheep.story_of_man_and_woman.view.activity.FeedActivity
import com.dev_sheep.story_of_man_and_woman.view.adapter.FeedAdapter
import com.dev_sheep.story_of_man_and_woman.view.adapter.FeedCardAdapter
import com.dev_sheep.story_of_man_and_woman.view.adapter.FeedRankAdapter
import com.dev_sheep.story_of_man_and_woman.view.adapter.Test_tag_Adapter
import com.dev_sheep.story_of_man_and_woman.viewmodel.FeedViewModel
import com.dev_sheep.story_of_man_and_woman.viewmodel.MemberViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import eu.micer.circlesloadingindicator.CirclesLoadingIndicator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home.*
import me.relex.circleindicator.CircleIndicator
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        fun newInstance(): HomeFragment {
            val args = Bundle()
            val fragment = HomeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val feedViewModel: FeedViewModel by viewModel()
    private val memberViewModel: MemberViewModel by viewModel()

    private lateinit var m_seq : String
    private var recyclerView : RecyclerView? = null
    private var recyclerViewTag : RecyclerView? = null
    private var viewpager : ViewPager? = null
    private var viewpager_card : ViewPager? = null
    private var indicators: CircleIndicator? = null
    private var tollbar : androidx.appcompat.widget.Toolbar? = null
    private var progressBar : CirclesLoadingIndicator? = null
    private var mNestedScrollView : NestedScrollView? = null
    lateinit var contexts : Context
    lateinit var mFeedAdapter: FeedAdapter
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var mTagAdapter: Test_tag_Adapter
    private var mRankAdapter: FeedRankAdapter? = null
    lateinit var mFeedCardAdater: FeedCardAdapter
    lateinit var mShimmerViewContainer: ShimmerFrameLayout
    lateinit var mProgressBar: ProgressBar

    private var limit: Int = 20
    private var offset: Int = 0
    private var previousTotal = 0
    private var isLoadData = false
    private var visibleThreshold = 2
    private var firstVisibleItem = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var lastVisibleItemPosition = 0
    private lateinit var linearLayoutManager: LinearLayoutManager // 태그 자동스크롤 위한 초기화 제한



    private var handler = Handler()
    private var delay : Long = 5000
    private var page : Int = 0
    var scrollCount: Int = 0
    private lateinit var layoutManagers: LinearLayoutManager // 태그 자동스크롤 위한 초기화 제한

    private var array_feed : List<Feed>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, null)
        contexts = view.context

        setHasOptionsMenu(true);

        recyclerView = view.findViewById<View>(R.id.recyclerView) as RecyclerView?
        tollbar = view.findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar?
        mProgressBar = view.findViewById(R.id.progressBar) as ProgressBar
        recyclerViewTag = view.findViewById<View>(R.id.recyclerView_tag) as RecyclerView?
        mSwipeRefreshLayout = view.findViewById<View>(R.id.sr_refresh) as SwipeRefreshLayout
        mShimmerViewContainer = view.findViewById<View>(R.id.shimmer_view_container) as ShimmerFrameLayout
        viewpager = view.findViewById<View>(R.id.vp) as ViewPager?
        viewpager_card = view.findViewById<View>(R.id.vp_feed_card) as ViewPager
        mNestedScrollView = view.findViewById(R.id.nestedScrollView) as NestedScrollView
        indicators = view.findViewById<CircleIndicator>(R.id.indicator)
        // 프래그먼트에 toolbar 세팅
        (activity as AppCompatActivity).setSupportActionBar(tollbar)

        mSwipeRefreshLayout.setOnRefreshListener(this)
        mSwipeRefreshLayout.setColorSchemeResources(
            R.color.main_Accent
        );

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // my_m_seq 가져오기
        val preferences: SharedPreferences = context!!.getSharedPreferences(
            "m_seq",
            Context.MODE_PRIVATE
        )
        m_seq = preferences.getString("inputMseq", "")

        memberViewModel.getNotificationCount(m_seq,iv_alarm,iv_alarm_dot,contexts)

        initData()

    }

    private fun initData(){

        if(feedViewModel == null){
            return
        }
//        setTagAdapter()
        // 전체보기
        // display loading indicator
        val handlerFeed: Handler = Handler(Looper.myLooper())
        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        val single = FEED_SERVICE.getListScroll(offset, limit)
        single.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.size > 0) {

                    mFeedAdapter =
                        FeedAdapter(it, contexts,feedViewModel, object : FeedAdapter.OnClickViewListener {
                            override fun OnClickFeed(
                                feed: Feed,
                                tv: TextView,
                                iv: ImageView,
                                cb: CheckBox,
                                cb2: CheckBox,
                                position: Int
                            ) {
                                feedViewModel.increaseViewCount(feed.feed_seq)

                                val lintent = Intent(context, FeedActivity::class.java)
                                lintent.putExtra("feed_seq", feed.feed_seq)
                                lintent.putExtra("checked" + feed.feed_seq, cb.isChecked)
                                lintent.putExtra("creater_seq", feed.creater_seq)
                                lintent.putExtra("feed_title",feed.title)
                                lintent.putExtra("bookmark_checked" + feed.feed_seq, cb2.isChecked)
                                lintent.putExtra(FeedActivity.EXTRA_POSITION, position)
//                        context.transitionName = position.toString()
                                (context as Activity).startActivity(lintent)
                                (context as Activity).overridePendingTransition(
                                    R.anim.fragment_fade_in,
                                    R.anim.fragment_fade_out
                                )

                            }
                        }, object : FeedAdapter.OnClickLikeListener {
                            override fun OnClickFeed(feed: Feed, boolean_value: String) {
                                feedViewModel.increaseLikeCount(feed.feed_seq, boolean_value)
                                if(boolean_value.equals("true")) {
                                    memberViewModel.addNotifiaction(
                                        m_seq,
                                        feed.creater_seq!!,
                                        feed.feed_seq,
                                        "피드알림",
                                        "님이 '\' "
                                                + feed.title +
                                                " '\' 를 좋아합니다."
                                    )
                                }
                                }

                        }, object : FeedAdapter.OnClickBookMarkListener {
                            override fun OnClickBookMark(
                                m_seq: String,
                                feed_seq: Int,
                                boolean_value: String
                            ) {
                                feedViewModel.onClickBookMark(m_seq, feed_seq, boolean_value)
                            }

                        }, object : FeedAdapter.OnClickProfileListener {
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
                        }, object : FeedAdapter.OnClickDeleteFeedListener {
                            override fun OnClickDeleted(feed_seq: Int) {
                                showDeletePopup(feedViewModel, feed_seq)
                                onResume()

                            }

                        })
                    handlerFeed.postDelayed({
                        // stop animating Shimmer and hide the layout
                        mShimmerViewContainer.stopShimmerAnimation()
                        mShimmerViewContainer.visibility = View.GONE
//                    progressBar?.visibility = View.GONE
                        recyclerView?.apply {
                            this.layoutManager = linearLayoutManager
                            this.itemAnimator = DefaultItemAnimator()
                            this.setHasFixedSize(true)
                            this.adapter = mFeedAdapter


                            setViewPager(it)


                        }
                    }, 1000)
                }

            }, {
                Log.e("feed 보기 실패함", "" + it.message)
            })

        val single_tag = FEED_SERVICE.getTagList()
        single_tag.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.size > 0) {

                    recyclerViewTag?.apply {
                        var recycler_this = this
                        layoutManagers = object : LinearLayoutManager(context) {
                            override fun smoothScrollToPosition(
                                recyclerView: RecyclerView,
                                state: RecyclerView.State?,
                                position: Int
                            ) {
                                val smoothScroller = object : LinearSmoothScroller(context) {
                                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                                        return 10.0f;
                                    }
                                }
                                smoothScroller.targetPosition = position
                                startSmoothScroll(smoothScroller)
                            }
                        }
                        mTagAdapter = object : Test_tag_Adapter(it, contexts) {
                            override fun load() {
                                if (layoutManagers.findFirstVisibleItemPosition() > 1) {
                                    mTagAdapter?.notifyItemMoved(0, it.size - 1)
                                }
                            }
                        }
                        layoutManagers.orientation = LinearLayoutManager.HORIZONTAL
                        recycler_this.layoutManager = layoutManagers
                        recycler_this.setHasFixedSize(true)
                        recycler_this.setItemViewCacheSize(10)
                        recycler_this.isDrawingCacheEnabled = true
                        recycler_this.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_LOW
                        recycler_this.adapter = mTagAdapter
                        autoScroll(it, mTagAdapter)

                    }
                }
            }, {
                Log.e("feed 보기2 실패함", "" + it.message)
            })


//        // alarm click
//        iv_alarm.setOnClickListener {
//            val lintent = Intent(context, AlarmActivity::class.java)
//            (context as Activity).startActivity(lintent)
//        }

        // 무한스크
        mNestedScrollView!!.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(
                v: NestedScrollView?,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
                if (v?.getChildAt(v.getChildCount() - 1) != null) {
                    if (scrollY >= v.getChildAt(v.getChildCount() - 1)
                            .getMeasuredHeight() - v.getMeasuredHeight() &&
                        scrollY > oldScrollY
                    ) {
                        visibleItemCount = linearLayoutManager.getChildCount()
                        totalItemCount = linearLayoutManager.getItemCount()
                        lastVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
//                        if (isLoadData()) {
                            if (visibleItemCount + lastVisibleItemPosition >= totalItemCount) {
                                // 마지막 스크롤에 프로그래스바 보여주기 및 무한스크롤
//                                isLoadData = true

                                LoadMoreData()
//                        Load Your Data
                            }
//                        }
                    }
                }
            }


        })

    }

    private fun LoadMoreData() {

//        if(isLoadData == true) {
//            mProgressBar.visibility = View.VISIBLE
//        }else{
//            mProgressBar.visibility = View.GONE
//        }

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        Handler().postDelayed({
            val single = FEED_SERVICE.getListScroll(offset, addLimit())
            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    mFeedAdapter =
                        FeedAdapter(it, contexts,feedViewModel, object : FeedAdapter.OnClickViewListener {
                            override fun OnClickFeed(
                                feed: Feed,
                                tv: TextView,
                                iv: ImageView,
                                cb: CheckBox,
                                cb2: CheckBox,
                                position: Int
                            ) {
                                feedViewModel.increaseViewCount(feed.feed_seq)

                                val lintent = Intent(context, FeedActivity::class.java)
                                lintent.putExtra("feed_seq", feed.feed_seq)
                                lintent.putExtra("checked" + feed.feed_seq, cb.isChecked)
                                lintent.putExtra("creater_seq", feed.creater_seq)
                                lintent.putExtra("feed_title",feed.title)
                                lintent.putExtra("bookmark_checked" + feed.feed_seq, cb2.isChecked)
                                lintent.putExtra(FeedActivity.EXTRA_POSITION, position)
                                lintent.putExtra(FeedActivity.EXTRA_POSITION, position)
//                        context.transitionName = position.toString()
                                (context as Activity).startActivity(lintent)
                                (context as Activity).overridePendingTransition(
                                    R.anim.fragment_fade_in,
                                    R.anim.fragment_fade_out
                                )

                            }
                        }, object : FeedAdapter.OnClickLikeListener {
                         override fun OnClickFeed(feed: Feed, boolean_value: String) {
                                feedViewModel.increaseLikeCount(feed.feed_seq, boolean_value)
                                if(boolean_value.equals("true")) {
                                    memberViewModel.addNotifiaction(
                                        m_seq,
                                        feed.creater_seq!!,
                                        feed.feed_seq,
                                        "피드알림",
                                        "님이 '\' "
                                                + feed.title +
                                                " '\' 를 좋아합니다."
                                    )
                                }
                                }

                        }, object : FeedAdapter.OnClickBookMarkListener {
                            override fun OnClickBookMark(
                                m_seq: String,
                                feed_seq: Int,
                                boolean_value: String
                            ) {
                                feedViewModel.onClickBookMark(m_seq, feed_seq, boolean_value)
                            }

                        }, object : FeedAdapter.OnClickProfileListener {
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
                        }, object : FeedAdapter.OnClickDeleteFeedListener {
                            override fun OnClickDeleted(feed_seq: Int) {
                                showDeletePopup(feedViewModel, feed_seq)
                                onResume()

                            }

                        })
                    recyclerView?.apply {
//                            var linearLayoutMnager = LinearLayoutManager(this.context)
                        this.layoutManager = linearLayoutManager
                        this.itemAnimator = DefaultItemAnimator()
                        this.adapter = mFeedAdapter
                    }

                }, {
                    Log.e("스크롤 보기 실패함", "" + it.message)
                })
            // 스크롤후 리스트 생성시 프로그래스바 없애기
//            isLoadData = false


        }, 500)
    }

    private fun setViewPager(feed: List<Feed>){

        if(!isAdded()) return

        mRankAdapter = FeedRankAdapter(childFragmentManager)
        viewpager?.apply {
            this.adapter = mRankAdapter

            this.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    page = position
                }
            })
            indicators?.setViewPager(this)

        }

        mFeedCardAdater = FeedCardAdapter(contexts)
        viewpager_card?.apply {
            this.adapter = mFeedCardAdater
            this.setPadding(72, 0, 72, 0);

            this.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {

                }
            })

        }
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.menu_search, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when(item.itemId){

        }
        return true
    }

    override fun onResume() {
        super.onResume()
        initData()
        mShimmerViewContainer.startShimmerAnimation()
        handler.postDelayed(runnable, delay)

    }

    override fun onPause() {
        super.onPause()
        mShimmerViewContainer.stopShimmerAnimation()
        handler.removeCallbacks(runnable)
    }

    // 태그 자동스크
    private fun autoScroll(tag: List<Tag>, adapter: Test_tag_Adapter) {
        scrollCount = 0;
        var speedScroll: Long = 1200;
        val runnable = object : Runnable {
            override fun run() {
                if (layoutManagers.findFirstVisibleItemPosition() >= tag.size / 2) {
                    adapter.load()
//                    Log.e(TAG, "run: load $scrollCount")
                }
                recyclerViewTag?.smoothScrollToPosition(scrollCount++)
//                Log.e(TAG, "run: $scrollCount")
                handler.postDelayed(this, speedScroll)
            }
        }
        handler.postDelayed(runnable, speedScroll)
    }

    // autoscroll Viewpager
    var runnable: Runnable = object : Runnable {
        override fun run() {
            if(mRankAdapter != null){
                if (mRankAdapter!!.getCount() === page) {
                    page = 0
                } else {
                    page++
                }
            }
            viewpager!!.setCurrentItem(page, true)
            handler.postDelayed(this, delay.toLong())
        }
    }
    private fun showDeletePopup(feedViewmodel: FeedViewModel, feed_seq: Int) {
        val fragment = HomeFragment()

        val inflater =
            context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)

        textView.text = "이 게시물을 삭제 하시겠습니까?"

        val alertDialog = AlertDialog.Builder(context!!)
            .setTitle("삭제")
            .setPositiveButton("네") { dialog, which ->
                feedViewmodel.deleteFeed(feed_seq)
                mFeedAdapter.notifyDataSetChanged()
                activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.replace(
                        R.id.frameLayout,
                        fragment
                    )
                    ?.commit()
            }

            .setNegativeButton("아니요", DialogInterface.OnClickListener { dialog, which ->

            })
            .create()

        alertDialog.setView(view)
        alertDialog.show()

        val btn_color : Button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        val btn_color_cancel : Button = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)

        if(btn_color != null){
            btn_color.setTextColor(resources.getColor(R.color.main_Accent))
        }
        if(btn_color_cancel != null){
            btn_color_cancel.setTextColor(resources.getColor(R.color.main_Accent))
        }
    }
    override fun onRefresh() {
        initData()
        mSwipeRefreshLayout.setRefreshing(false)
    }


    private fun addLimit() : Int{
        limit += 20
    return limit
    }

    private fun addOffset(): Int{
        offset += 10
        return offset
    }


}