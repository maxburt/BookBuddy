package edu.utap.bookbuddy

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class ReaderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reader, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Hide Android toolbar and status bar
        requireActivity().actionBar?.hide()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()
        val window = requireActivity().window
        val controller = window.insetsController
        controller?.hide(
            android.view.WindowInsets.Type.statusBars() or
                    android.view.WindowInsets.Type.navigationBars()
        )
        controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val webView = view.findViewById<WebView>(R.id.epubWebView)
        val bookId = ReaderFragmentArgs.fromBundle(requireArguments()).bookId
        val epubFile = File(requireContext().filesDir, "$bookId.epub")
        val epubBytes = epubFile.readBytes()
        val epubBase64 = Base64.encodeToString(epubBytes, Base64.NO_WRAP)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 🔗 JavaScript bridge to handle save + back
        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun saveProgress(cfi: String) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("library")
                    .document(bookId)
                    .update("progress", cfi)
                    .addOnSuccessListener {
                        Log.d("ReaderFragment", "Progress saved: $cfi")
                    }
                    .addOnFailureListener {
                        Log.e("ReaderFragment", "Failed to save progress", it)
                    }
            }

            @android.webkit.JavascriptInterface
            fun onBackPressed() {
                requireActivity().runOnUiThread {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }, "AndroidInterface")

        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()

        val html = requireContext().assets.open("reader.html")
            .bufferedReader().use { it.readText() }

        // 🔄 Fetch saved progress from Firestore
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("library")
            .document(bookId)
            .get()
            .addOnSuccessListener { document ->
                val savedProgress = document.getString("progress") ?: ""
                Log.d("ReaderFragment", "Loaded saved progress: $savedProgress")

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val js = if (savedProgress.isNotBlank()) {
                            "loadBookBase64('$epubBase64', '$savedProgress');"
                        } else {
                            "loadBookBase64('$epubBase64');"
                        }
                        webView.evaluateJavascript(js, null)
                    }
                }

                webView.loadDataWithBaseURL(
                    "file:///android_asset/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
            .addOnFailureListener {
                Log.e("ReaderFragment", "Failed to load saved progress", it)

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val js = "loadBookBase64('$epubBase64');"
                        webView.evaluateJavascript(js, null)
                    }
                }

                webView.loadDataWithBaseURL(
                    "file:///android_asset/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.show()
        val window = requireActivity().window
        val controller = window.insetsController
        controller?.show(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
    }
}