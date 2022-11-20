package com.knear.android

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment

class NearWalletLoginDialog : DialogFragment() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var url: String
    private lateinit var onReceiveUri: OnReceiveUri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_near_wallet_login_dialog, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView=view.findViewById(R.id.loginWebView)
        progressBar=view.findViewById(R.id.progressBar)
        webView.settings.allowFileAccess=true
        webView.settings.allowContentAccess=true
        webView.settings.loadsImagesAutomatically=true
        webView.settings.javaScriptEnabled=true
        webView.settings.domStorageEnabled=true
        webView.settings.setSupportZoom(false)
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                Log.d("LOAD URL",request?.url.toString())
                if (!request?.url.toString().startsWith("http://") || !request?.url.toString().startsWith("https://")) {
                    request?.run {
                        onReceiveUri.onReceive(this.url)
                        dismiss()
                    }
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.isVisible=true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.isVisible=false
            }
        }
        webView.loadUrl(this.url)
    }

    fun login(loginUrl: String, onReceiveUri: OnReceiveUri){
        this.url=loginUrl
        this.onReceiveUri=onReceiveUri
    }
}