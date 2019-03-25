package com.xyx.terem.news.fragment

import android.graphics.Color
import android.os.Bundle
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
import kotlinx.android.synthetic.main.layout_comment_item.view.*
import java.util.*

class NewsDetailFragment : Fragment() {

    companion object {
        const val PARAM_ITEM_BEAN = "PARAM_ITEM_BEAN"
    }

    private lateinit var listAdapter: CommentsListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return RecyclerView(inflater.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val itemBean = arguments?.getParcelable<ItemBean>(PARAM_ITEM_BEAN)!!
        if (!::listAdapter.isInitialized) {
            listAdapter = CommentsListAdapter(itemBean)
//            refreshNews()
        }
        (view as RecyclerView).adapter = listAdapter
        view.layoutManager = LinearLayoutManager(context)
    }


    class CommentsListAdapter(val itemBean: ItemBean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val TYPE_HEADER = 0
        private val TYPE_COMMENT = 1

        private var mComments: Array<ItemBean?>? = null

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) {
                TYPE_HEADER
            } else {
                TYPE_COMMENT
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            return when (p1) {
                TYPE_HEADER -> HeaderVH(LayoutInflater.from(p0.context).inflate(R.layout.layout_news_detail, p0, false))
                TYPE_COMMENT -> CommentVH(
                    LayoutInflater.from(p0.context).inflate(
                        R.layout.layout_comment_item,
                        p0,
                        false
                    )
                )
                else -> throw Exception("Not supported ViewHolder type!")
            }
        }

        override fun getItemCount(): Int {
            return if (itemBean.kids == null) {
                1
            } else {
                1 + itemBean.kids.size
            }
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            when (p0) {
                is HeaderVH -> {
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
                }
                is CommentVH -> {
                    if (mComments == null) {
                        mComments = arrayOfNulls(itemBean.kids!!.size)
                    }
                    if (mComments!![p1 - 1] != null) {
                        val comment = mComments!![p1 - 1]!!
                        p0.itemView.commentAuthor.text = comment.by + ':'
                        p0.itemView.commentText.text = comment.text
                        p0.itemView.commentDate.text = Date(comment.time * 1000L).toLocaleString()
                    } else {
                        p0.resetLoader()
                        NewsSever.instance
                            .getItem(itemBean.kids!![p1 - 1])
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                { t ->
                                    if (mComments != null) {
                                        mComments!![p1 - 1] = t
                                        notifyItemChanged(p1)
                                    }
                                },
                                { t ->
                                    System.err.println(t.message)
                                }
                            )!!
                    }
                }
            }
        }


        class HeaderVH(itemView: View) : RecyclerView.ViewHolder(itemView)

        class CommentVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun resetLoader() {
                itemView.commentAuthor.resetLoader()
                itemView.commentText.resetLoader()
                itemView.commentDate.resetLoader()
            }
        }
    }
}