package com.dev_sheep.story_of_man_and_woman.view.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dev_sheep.story_of_man_and_woman.R
import kotlinx.android.synthetic.main.fragment_notification.*

class NotificationFragment :  Fragment()  {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification,null)
        // ToolBar를 ActionBar로 설정해줘야 합니다.
        (activity as AppCompatActivity).setSupportActionBar(app_toolbar)
        return view
    }
}