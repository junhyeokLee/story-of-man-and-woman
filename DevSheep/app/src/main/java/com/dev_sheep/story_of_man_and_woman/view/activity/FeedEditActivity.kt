package com.dev_sheep.story_of_man_and_woman.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dev_sheep.story_of_man_and_woman.R
import com.dev_sheep.story_of_man_and_woman.data.database.entity.Feed
import com.dev_sheep.story_of_man_and_woman.data.remote.APIService.FEED_SERVICE
import com.dev_sheep.story_of_man_and_woman.viewmodel.FeedViewModel
import com.dev_sheep.story_of_man_and_woman.viewmodel.MemberViewModel
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_feed_edit.img_profile
import kotlinx.android.synthetic.main.activity_feed_edit.richwysiwygeditor
import kotlinx.android.synthetic.main.activity_feed_edit.tv_age
import kotlinx.android.synthetic.main.activity_feed_edit.tv_creater
import kotlinx.android.synthetic.main.activity_feed_edit.tv_gender
import kotlinx.android.synthetic.main.activity_feed_write.toolbar_write
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FeedEditActivity : AppCompatActivity() {
    var FEED_SEQ : Int? = null
    var TAG_SEQ : Int? = null
    private val feedViewModel: FeedViewModel by viewModel()
    private val memberViewModel : MemberViewModel by viewModel()
    lateinit var EMAIL : String
    lateinit var M_SEQ: String
    private var count: Int = 0

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_edit)
        toolbar_write.setTitle("")
        setSupportActionBar(toolbar_write)

        //추가된 소스코드, Toolbar의 왼쪽에 버튼을 추가하고 버튼의 아이콘을 바꾼다.
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // 저장된 m_seq 가져오기
        val getM_seq = getSharedPreferences("m_seq", AppCompatActivity.MODE_PRIVATE)
        M_SEQ = getM_seq.getString("inputMseq", null)

        val getEMAIL = getSharedPreferences("autoLogin", AppCompatActivity.MODE_PRIVATE)
        EMAIL = getEMAIL.getString("inputEmail", null)

        getExtraData()

        memberViewModel.getMember(M_SEQ)
        memberViewModel.memberLivedata.observe(this, androidx.lifecycle.Observer {
            tv_creater.text = it.nick_name
            tv_gender.text = it.gender
            tv_age.text = it.age
            if(it.profile_img.toString().equals("null")){
                Glide.with(this)
                    .load("http://storymaw.com/data/member/user.png")
                    .apply(RequestOptions().circleCrop())
                    .placeholder(android.R.color.transparent)
                    .error(R.drawable.error_loading)
                    .into(img_profile)
            }else {
                Glide.with(this)
                    .load(it.profile_img)
                    .apply(RequestOptions().circleCrop())
                    .placeholder(android.R.color.transparent)
                    .error(R.drawable.error_loading)
                    .into(img_profile)
            }
        })

        initData()


    }

    fun getExtraData(){
        if(intent.hasExtra("feed_seq")) {
            val feed_seq = intent.getIntExtra("feed_seq", 0)
            FEED_SEQ = feed_seq
        }
        if(intent.hasExtra("tag_seq")) {
            val tag_seq = intent.getIntExtra("tag_seq", 0)
            TAG_SEQ = tag_seq
        }

        if(intent.hasExtra("type")) {
            richwysiwygeditor.typeValue = intent.getStringExtra("type")

            if(richwysiwygeditor.typeValue == "public"){
                richwysiwygeditor.layout_iv_lock_open.visibility = View.VISIBLE
                richwysiwygeditor.layout_iv_lock.visibility = View.GONE
                richwysiwygeditor.layout_iv_lock_sub_open.visibility = View.GONE

            }else if(richwysiwygeditor.typeValue == "subscriber"){
                richwysiwygeditor.layout_iv_lock_sub_open.visibility = View.VISIBLE
                richwysiwygeditor.layout_iv_lock.visibility = View.GONE
                richwysiwygeditor.layout_iv_lock_open.visibility = View.GONE
            }else if(richwysiwygeditor.typeValue == "private"){
                richwysiwygeditor.layout_iv_lock.visibility = View.VISIBLE
                richwysiwygeditor.layout_iv_lock_open.visibility = View.GONE
                richwysiwygeditor.layout_iv_lock_sub_open.visibility = View.GONE

            }
        }
    }

    @SuppressLint("CheckResult")
    fun initData(){

        // 글 작성할때는 이미지 클릭 제어
        richwysiwygeditor.content.feedActivityType = 999



//        val items = resources.getStringArray(R.array.date_public)
//        var spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
////        spinner_public.dropDownWidth(android.R.layout.simple_spinner_dropdown_item)android.R.layout.simple_spinner_item
//        spinner_public.adapter = spinnerAdapter
//
//        spinner_public.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작하게 됩니다.
//                when(position) {
//                    0   ->  {
//                        TYPE = "public"
//                    }
//                    1   ->  {
//                        TYPE = "subscriber"
//
//                    }
//                    2 ->{
//                        TYPE = "private"
//                    }
//                    //...
//                    else -> {
//
//                    }
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//
//            }
//        }


        val single = FEED_SERVICE.getFeed(FEED_SEQ!!)
        single.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // Editable 스트링으로 변경해서 가져오기
                val editable: Editable = SpannableStringBuilder(it.title)

                richwysiwygeditor.headlineEditText.text = editable
                richwysiwygeditor.content.html = it.content
                richwysiwygeditor.tagName.text = "#"+it.tag_name
            },{

            })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val images =
                ImagePicker.getImages(data)

            insertImages(images)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun insertImages(images: List<Image>?) {

        // 저장된 m_seq 가져오기
        val preferences: SharedPreferences = getSharedPreferences("m_seq", Context.MODE_PRIVATE)
        val m_seq = preferences.getString("inputMseq", "")

        if (images == null) return
        val stringBuffer = StringBuilder()
        var i = 0
        val l = images.size
        while (i < l) {

            //파일 생성
            //img_url은 이미지의 경로
            //파일 생성
            //img_url은 이미지의 경로
            val file = File(images.get(i).path)
            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body =
                MultipartBody.Part.createFormData("uploaded_file", file.name, requestFile)
            val resultCall: Call<Feed> = FEED_SERVICE.uploadImage(EMAIL,body)
            resultCall.enqueue(object : Callback<Feed?> {
                override fun onResponse(
                    call: Call<Feed?>,
                    response: Response<Feed?>
                ) {
                    Log.e("성공함",response.toString())
                    Log.e("filename",file.name)
                    if(response.isSuccessful) {
                        stringBuffer.append(file.name).append("\n")
                        richwysiwygeditor.getContent().insertImage(
                            "http://www.storymaw.com/data/feed/" + EMAIL + "/" + file.name,
                            file.name
                        )
//                        increaseCount()
                    }
                }
                override fun onFailure(
                    call: Call<Feed?>,
                    t: Throwable
                ) {
                    Toast.makeText(applicationContext, "이미지 업로드 실패.", Toast.LENGTH_SHORT).show()

                    Log.e("에러",t.message)
                }
            })


            i++
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_write, menu)
        val positionOfMenuItem = 0 // or whatever...

        val item = menu!!.getItem(positionOfMenuItem)
        val s = SpannableString("수정")
        s.setSpan(ForegroundColorSpan(resources.getColor(R.color.text_subscrib_color)), 0, s.length, 0)
        item.setTitle(s)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.next -> {

                Toast.makeText(applicationContext, "완료.", Toast.LENGTH_SHORT).show()

                if(richwysiwygeditor.getHeadlineEditText().getText().toString() == ""){
                    Toast.makeText(applicationContext, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return false
                }
                if(richwysiwygeditor.getContent().getHtml().toString() == ""){
                    Toast.makeText(applicationContext, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return false
                }

                feedViewModel.updateFeed(
                    FEED_SEQ!!,
                    richwysiwygeditor.getHeadlineEditText().getText().toString(),
                    richwysiwygeditor.getContent().getHtml().plus("<br>"),
                    richwysiwygeditor.typeValue,
                    richwysiwygeditor.tagSeq
                )
                finish()

                Log.i(
                    "Rich Wysiwyg Headline",
                    richwysiwygeditor.getHeadlineEditText().getText().toString()
                )
                if (richwysiwygeditor.getContent().getHtml() != null) Log.i(
                    "Rich Wysiwyg",
                    richwysiwygeditor.getContent().getHtml()
                )
                true


            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getCount():Int{
        return this.count
    }
    private fun increaseCount(){
        this.count += 1


    }
}