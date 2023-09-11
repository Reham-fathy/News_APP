package com.example.news.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.adapter.NewsAdapter
import com.example.news.ui.NewsActivity
import com.example.news.ui.NewsViewModel
import com.example.news.util.Constant
import com.example.news.util.Constant.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.news.util.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlinx.android.synthetic.main.fragment_breaking_news.paginationProgressBar
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNews : Fragment(R.layout.fragment_search_news) {
    lateinit var newsAdapter: NewsAdapter
    lateinit var viewModel: NewsViewModel
    val TAG="SearchNewsFragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle=Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(
                R.id.action_searchNews_to_article,
                bundle
            )
        }
        var job: Job?=null
        etSearch.addTextChangedListener { editable->
        job?.cancel()
        job= MainScope().launch {
            delay(SEARCH_NEWS_TIME_DELAY)
            if (editable.toString().isNotEmpty()){
                viewModel.searchNews(editable.toString())
            }
        }

        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response->
            when(response){
                is Resource.Sucess ->{
                    hideProgressBar()
                    response.data?.let {newsResponse->
                        newsAdapter.differ.submitList(newsResponse.articles)
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages=newsResponse.totalResults/ Constant.QUERY_PAGE_SIZE +2
                        isLastPage=viewModel.searchNewsPage==totalPages
                        if (isLastPage) {
                        rvSearchNews.setPadding(0,0,0,0)
                    }
                    }
                }
                is Resource.Error->{
                    hideProgressBar()
                    response.message?.let { message->
                        Toast.makeText(activity,"An error occured : $message ", Toast.LENGTH_SHORT).show()
                }
                }
                is Resource.Loading->{
                    showProgressBar()
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
            if (newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
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
            val isTotalMoreThanVisible =totalItemCount>= Constant.QUERY_PAGE_SIZE
            val shouldPaginate =isNotLoadingAndNotLastPage&&isAtLastItem && isNottBeginning &&
                    isTotalMoreThanVisible &&isScrolling
            if(shouldPaginate){
                viewModel.searchNews(etSearch.text.toString())
                isScrolling=false
            }

        }
    }
    private fun setupRecyclerView()
    {
        newsAdapter= NewsAdapter()
        rvSearchNews.apply {
            adapter=newsAdapter
            layoutManager=LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNews.scrollListener)
        }
    }

}