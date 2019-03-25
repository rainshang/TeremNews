package com.xyx.terem.news.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xyx.terem.news.R
import com.xyx.terem.news.net.ItemBean
import com.xyx.terem.news.net.NewsSever
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_news_detail.view.*
import kotlinx.android.synthetic.main.fragment_list.*
import java.util.*


class NewsListFragment : Fragment() {

    private lateinit var listAdapter: NewsListAdapter
    private lateinit var mOnItemClickListener: NewsListAdapter.OnItemClickListener


    fun setOnItemClickListener(onItemClickListener: NewsListAdapter.OnItemClickListener): NewsListFragment {
        mOnItemClickListener = onItemClickListener
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!::listAdapter.isInitialized) {
            listAdapter = NewsListAdapter(mOnItemClickListener)
            refreshNews()
        }
        newsList.adapter = listAdapter
        newsList.layoutManager = LinearLayoutManager(context)

        refreshBtn.setOnClickListener { refreshNews() }
    }

    private fun refreshNews() {
        NewsSever.instance
            .getNewStories()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { t ->
                    newsList.scrollToPosition(0)
                    listAdapter.update(t)
                },
                { t ->
                    Snackbar.make(refreshBtn, t.message.toString(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }
            )!!
    }


    class NewsListAdapter(private var onItemClickListener: NewsListAdapter.OnItemClickListener) :
        RecyclerView.Adapter<NewsListAdapter.ViewHolder>(), View.OnClickListener {


        private var mIds: Array<Int>? = null
        private var mItems: Array<ItemBean?>? = null

        fun update(ids: Array<Int>) {
            mIds = ids
            mItems = arrayOfNulls(ids.size)
            notifyDataSetChanged()
        }

        override fun onClick(v: View?) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v!!, v!!.tag as ItemBean)
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NewsListAdapter.ViewHolder {
            return ViewHolder(
                LayoutInflater.from(p0.context).inflate(R.layout.layout_news_item, p0, false),
                this
            )
        }

        override fun getItemCount(): Int {
            return if (mIds != null) {
                mIds!!.size
            } else {
                0
            }
        }

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            if (mItems == null) {
                mItems = arrayOfNulls(mIds!!.size)
            }
            if (mItems!![p1] != null) {
                val itemBean = mItems!![p1]!!
                p0.itemView.tag = itemBean
                val spannable = SpannableString(itemBean.type + " " + itemBean.title).apply {
                    setSpan(
                        ForegroundColorSpan(Color.GREEN),
                        0, itemBean.type.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        RelativeSizeSpan(0.5f),
                        0, itemBean.type.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                p0.itemView.itemTitle.text = spannable
                p0.itemView.itemAuthor.text = itemBean.by
                p0.itemView.itemDate.text = Date(itemBean.time * 1000L).toLocaleString()
                if (TextUtils.isEmpty(itemBean.text)) {
                    p0.itemView.itemText.visibility = View.GONE
                } else {
                    p0.itemView.itemText.visibility = View.VISIBLE
                    p0.itemView.itemText.text = itemBean.text
                }
                if (TextUtils.isEmpty(itemBean.url)) {
                    p0.itemView.itemUrl.visibility = View.GONE
                } else {
                    p0.itemView.itemUrl.visibility = View.VISIBLE
                    p0.itemView.itemUrl.text = itemBean.url
                }
                return
            } else {
                NewsSever.instance
                    .getItem(mIds!![p1])
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { t ->
                            if (mItems != null) {
                                mItems!![p1] = t
                                notifyItemChanged(p1)
                            }
                        },
                        { t ->
                            System.err.println(t.message)
                        }
                    )!!
            }
            p0.resetLoader()
        }

        class ViewHolder(itemView: View, onClickListener: View.OnClickListener) : RecyclerView.ViewHolder(itemView) {

            init {
                itemView.setOnClickListener(onClickListener)
            }

            fun resetLoader() {
                itemView.tag = null

                itemView.itemTitle.resetLoader()
                itemView.itemAuthor.resetLoader()
                itemView.itemDate.resetLoader()
                itemView.itemText.visibility = View.VISIBLE
                itemView.itemText.resetLoader()
                itemView.itemUrl.visibility = View.VISIBLE
                itemView.itemUrl.resetLoader()
            }
        }

        interface OnItemClickListener {
            fun onItemClick(view: View, itemBean: ItemBean)
        }
    }
}