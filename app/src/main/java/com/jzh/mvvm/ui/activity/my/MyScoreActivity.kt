package com.jzh.mvvm.ui.activity.my

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.jzh.mvvm.R
import com.jzh.mvvm.base.BaseViewModelActivity
import com.jzh.mvvm.constant.Constant
import com.jzh.mvvm.mvvm.viewModel.MyScoreActivityViewModel
import com.jzh.mvvm.ui.adapter.ScoreAdapter
import com.jzh.mvvm.utils.AnimatorUtils
import com.jzh.mvvm.utils.MyMMKV.Companion.mmkv
import kotlinx.android.synthetic.main.activity_my_score.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.lang.Exception

class MyScoreActivity : BaseViewModelActivity<MyScoreActivityViewModel>() {

    private val mAdapter: ScoreAdapter by lazy { ScoreAdapter() }
    private val linearLayoutManager by lazy { LinearLayoutManager(this) }

    override fun providerVMClass() = MyScoreActivityViewModel::class.java

    override fun getLayoutId() = R.layout.activity_my_score

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun initData() {
        setTop("我的积分", R.drawable.paihangbang)
        toolbar_subtitle_image.setOnClickListener {
            startActivity(Intent(this, RankActivity::class.java))
        }
    }

    override fun initView() {
        recyclerView1_score.run {
            layoutManager = linearLayoutManager
            adapter = mAdapter
        }
        mAdapter.run {
            recyclerView = recyclerView1_score
            loadMoreModule.setOnLoadMoreListener {
                getScoreList(data.size / pageSize)
            }
        }
    }

    override fun startHttp() {
        showLoading()
        getScoreList(0)
        if (mmkv.decodeInt(Constant.SCORE_UNM, 0) == 0) {
            getUserInfo()
        } else {
            AnimatorUtils.doIntAnimator(tv_score, 0, mmkv.decodeInt(Constant.SCORE_UNM), 1000)
        }
    }

    private fun getUserInfo() {
        viewModel.getUserInfo().observe(this, {
            mmkv.encode(Constant.SCORE_UNM, it.coinCount)
            AnimatorUtils.doIntAnimator(tv_score, 0, it.coinCount, 1000)
        })
    }

    private fun getScoreList(page: Int) {
        //该接口page从1开始
        viewModel.getScoreList(page + 1).observe(this, {
            hideLoading()
            it.datas.let { scoreList ->
                mAdapter.run {
                    setList(scoreList)
                    if (it.over) loadMoreModule.loadMoreEnd()
                    else loadMoreModule.loadMoreComplete()
                }
            }
        })
    }

    override fun requestError(it: Exception?) {
        super.requestError(it)
        if (mAdapter.loadMoreModule.isLoading) {
            mAdapter.loadMoreModule.loadMoreFail()
        }
    }
}