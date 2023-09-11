package com.example.news.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.adapter.NewsAdapter
import com.example.news.ui.NewsActivity
import com.example.news.ui.NewsViewModel
import com.example.news.util.Constant.Companion.QUERY_PAGE_SIZE
import com.example.news.util.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*

class BreakingNews : Fragment(R.layout.fragment_breaking_news) {
    lateinit var viewModel: NewsViewModel

    lateinit var newsAdapter: NewsAdapter
    val TAG = "BreakingNewsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       viewModel = (activity as NewsActivity).viewModel
        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle=Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(
                R.id.action_breakingNews_to_article,
                bundle
            )
        }
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response->
            when(response){
                is Resource.Sucess ->{
                    hideProgressBar()
                    response.data?.let {newsResponse->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages=newsResponse.totalResults/ QUERY_PAGE_SIZE +2
                        isLastPage=viewModel.breakingNewsPage==totalPages
                        if (isLastPage) {
                        rvBreakingNews.setPadding(0,0,0,0)

                    }
                    }
                }
                is Resource.Error->{
                    hideProgressBar()
                    response.message?.let { message->
                        imgNoConnection.visibility=View.VISIBLE
            Toast.makeText(activity,"An error occured : $message ",Toast.LENGTH_SHORT).show()                    }
                }
                is Resource.Loading->{
                    showProgressBar()
                    imgNoConnection.visibility=View.GONE
                }
            }

        })
    }
private fun hideProgressBar(){
    paginationProgressBar.visibility=View.INVISIBLE
    isLoading=false
}
    private fun showProgressBar(){
        paginationProgressBar.visibility=View.VISIBLE
        isLoading=true
    }


    var isLoading=false
    var isLastPage= false
    var isScrolling=false
    val scrollListener =object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState==AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling=true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager=recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition=layoutManager.findFirstVisibleItemPosition()
            val totalItemCount=layoutManager.itemCount
            val visibleItemCount=layoutManager.childCount


            val isNotLoadingAndNotLastPage=!isLoading&&!isLastPage
            val isAtLastItem =firstVisibleItemPosition +visibleItemCount>=totalItemCount
            val isNottBeginning=firstVisibleItemPosition>=0
            val isTotalMoreThanVisible =totalItemCount>=QUERY_PAGE_SIZE
            val shouldPaginate =isNotLoadingAndNotLastPage&&isAtLastItem && isNottBeginning &&
                    isTotalMoreThanVisible &&isScrolling
            if(shouldPaginate){
                viewModel.getBreakingNews("us")
                isScrolling=false
            }

        }
    }

private fun setupRecyclerView()
{
    newsAdapter= NewsAdapter()
    rvBreakingNews.apply {
        adapter=newsAdapter
        layoutManager=LinearLayoutManager(activity)
        addOnScrollListener(this@BreakingNews.scrollListener)
    }
}
}