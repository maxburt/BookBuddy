package edu.utap.bookbuddy

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
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
        //Hide android toolbar
        requireActivity().actionBar?.hide()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()

        //Hide android status bar
        val window = requireActivity().window
        val controller = window.insetsController
        if (controller != null) {
            controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }


        val webView = view.findViewById<WebView>(R.id.epubWebView)
        val bookId = ReaderFragmentArgs.fromBundle(requireArguments()).bookId
        val epubFile = File(requireContext().filesDir, "$bookId.epub")

        val epubBytes = epubFile.readBytes()
        val epubBase64 = Base64.encodeToString(epubBytes, Base64.NO_WRAP)

        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()

        // Enable calling back from JS
        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun onBackPressed() {
                requireActivity().runOnUiThread {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }, "AndroidInterface")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val js = "loadBookBase64('$epubBase64');"
                webView.evaluateJavascript(js, null)
            }
        }

        // Load the local HTML viewer file
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
    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.show()
        val window = requireActivity().window
        val controller = window.insetsController
        controller?.show(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
    }
}