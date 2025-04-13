package edu.utap.bookbuddy

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import java.io.File

class ReaderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reader, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val webView = view.findViewById<WebView>(R.id.epubWebView)
        val bookId = ReaderFragmentArgs.fromBundle(requireArguments()).bookId
        val epubFile = File(requireContext().filesDir, "$bookId.epub")

        val epubBytes = epubFile.readBytes()
        val epubBase64 = Base64.encodeToString(epubBytes, Base64.NO_WRAP)

        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val js = "loadBookBase64('$epubBase64');"
                webView.evaluateJavascript(js, null)
            }
        }

        val html = requireContext().assets.open("reader.html")
            .bufferedReader().use { it.readText() }

        webView.loadDataWithBaseURL(
            "file:///android_asset/",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }
}