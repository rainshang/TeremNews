package com.xyx.terem.news

import android.os.Build
import android.os.Bundle
import android.support.transition.*
import android.support.transition.TransitionSet.ORDERING_TOGETHER
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.xyx.terem.news.fragment.NewsDetailFragment
import com.xyx.terem.news.fragment.NewsListFragment
import com.xyx.terem.news.net.ItemBean
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NewsListFragment.NewsListAdapter.OnItemClickListener {

    private lateinit var listFragment: NewsListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        listFragment = NewsListFragment().setOnItemClickListener(this)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, listFragment)
            .commit()
    }

    override fun onItemClick(view: View, itemBean: ItemBean) {
        ViewCompat.setTransitionName(view, resources.getString(R.string.transition_name_detail))
        supportFragmentManager
            .beginTransaction()
            .addSharedElement(view, resources.getString(R.string.transition_name_detail))
            .replace(R.id.fragmentContainer, NewsDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(NewsDetailFragment.PARAM_ITEM_BEAN, itemBean)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sharedElementEnterTransition = TransitionSet().apply {
                        ordering = ORDERING_TOGETHER
                        addTransition(ChangeBounds()).addTransition(ChangeTransform())
                            .addTransition(ChangeImageTransform())
                    }
                    enterTransition = Fade()
                    exitTransition = Fade()
                    sharedElementReturnTransition = TransitionSet().apply {
                        ordering = ORDERING_TOGETHER
                        addTransition(ChangeBounds()).addTransition(ChangeTransform())
                            .addTransition(ChangeImageTransform())
                    }
                }
            })
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
