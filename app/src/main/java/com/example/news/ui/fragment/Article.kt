package com.example.news.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.example.news.R
import com.example.news.ui.NewsActivity
import com.example.news.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_article.*

class Article : Fragment(R.layout.fragment_article) {

    lateinit var viewModel: NewsViewModel
    val args:ArticleArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        val article =args.article
        webView.apply {
            webViewClient= WebViewClient()
            loadUrl(article.url)
        }

fab.setOnClickListener {
    viewModel.saveArticle(article)
    Snackbar.make(view,"Article saved successfully ",Snackbar.LENGTH_SHORT).show()
}
    }


}