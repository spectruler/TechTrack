package com.me.techtrack

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import java.io.IOException
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import kotlin.properties.Delegates
import kotlinx.android.synthetic.main.activity_main.*

class FeedEntry{
    var title:String = ""
    var link: String =""
    var pubDate: String= ""
    var description: String= ""
    var content = ""
    var category: ArrayList<String>? = ArrayList<String>()
    var creater = ""

    override fun toString():String{
        return """
            title: $title
            link: $link
            pubDate: $pubDate
            description: $description
            content: $content
            category: $category
            creater: $creater
        """.trimIndent()
    }
}

class MainActivity : AppCompatActivity() {

    private val TAG = "TECHTRACK"

    private val STATE_URL = "STATE URL"

    private val TechCrunchUrl = "https://techcrunch.com/feed/"
    private val TECHMEME_URL = "https://www.techmeme.com/feed.xml"

    // MIT URLS techreviews
    private val BASE_MIT_URL = "https://www.technologyreview.com/"
    private val TOP_MIT_URL = "${BASE_MIT_URL}topnews.rss"
    private val STORIES_MIT_URL = "${BASE_MIT_URL}stories.rss"
    private val VIEWS_MIT_URL = "${BASE_MIT_URL}views/rss/"
     // MIT CATEGORIES
    private val BUSINESS_MIT_URL = "${BASE_MIT_URL}c/business/rss/"
    private val BIO_MIT_URL = "${BASE_MIT_URL}c/biomedicine/rss/" // biomedical
    private val COM_MIT_URL = "${BASE_MIT_URL}c/biomedicine/rss/" //computing
    private val ENERGY_MIT_URL = "${BASE_MIT_URL}c/energy/rss/"
    private val MOBILE_MIT_URL = "${BASE_MIT_URL}c/mobile/rss/"
    private val ROBOTICS_MIT_URL = "${BASE_MIT_URL}c/robotics/rss/"

    private var downloadData: DownloadData? = null
    private var feedUrl = TechCrunchUrl
    private var feedCachedUrl = "INVALIDATED"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(savedInstanceState!=null){
            feedUrl = savedInstanceState.getString(STATE_URL,TechCrunchUrl)
        }
        downloadURL(feedUrl)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu!!)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mnuTechCrunch -> feedUrl = TechCrunchUrl
            R.id.mnuTechMeme -> feedUrl = TECHMEME_URL
            R.id.mnuMitTop -> {
                feedUrl = TOP_MIT_URL
                item.isChecked = true
            }
            R.id.mnuStories -> {
                item.isChecked = true
                feedUrl = STORIES_MIT_URL
            }
            R.id.mnuMitViews -> {
                item.isChecked = true
                feedUrl = VIEWS_MIT_URL
            }
            R.id.mnuMitBio -> {
                item.isChecked = true
                feedUrl = BIO_MIT_URL
            }
            R.id.mnuMitBusiness -> {
                item.isChecked = true
                feedUrl = BUSINESS_MIT_URL
            }
            R.id.mnuMitComputing -> {
                item.isChecked = true
                feedUrl = COM_MIT_URL
            }
            R.id.mnuMitEnergy -> {
                item.isChecked = true
                feedUrl = ENERGY_MIT_URL
            }
            R.id.mnuMitMobile -> {
                item.isChecked = true
                feedUrl = MOBILE_MIT_URL
            }
            R.id.mnuMitRobotics -> {
                item.isChecked = true
                feedUrl = ROBOTICS_MIT_URL
            }
            R.id.mnuRefresh -> feedCachedUrl = "INVALIDATED"
            else -> return super.onOptionsItemSelected(item)
        }
        downloadURL(feedUrl)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_URL,feedUrl)
    }

    private fun downloadURL(urlPath: String){
        if(feedUrl != feedCachedUrl){
            Log.d(TAG,"downloadURL starting AsyncTask")
            downloadData = DownloadData(this,listViewRss)
            downloadData?.execute(urlPath)
            feedCachedUrl = feedUrl
            Log.d(TAG,"downloadURL: done")
        }else{
            Log.d(TAG,"downloadURL: URL not changed")
        }
    }

    companion object{
        private class DownloadData(context: Context,
                                   listView: ListView
        ):
            AsyncTask<String, Void, String>(){

            var propContext: Context by Delegates.notNull()
            var propListView by Delegates.notNull<ListView>()

            init{
                propContext = context
                propListView = listView
            }

            private val TAG = "DownloadData"
            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                Log.d(TAG,"onPostExecute: parameter is $result")
                val parseApplications = ParseApplications()
                parseApplications.parse(result!!)
                val feedAdapter = FeedAdapter(propContext,R.layout.item_record,parseApplications.applications)
                propListView.adapter = feedAdapter
            }

            override fun doInBackground(vararg params: String?): String {
                Log.d(TAG,"doInBackground() called")
                val rssFeed =  downloadXML(params[0])
                if(rssFeed.isEmpty()){
                    Log.e(TAG,"doInBackground() error downloading")
                }
                return rssFeed
            }

            private fun downloadXML(urlPath: String?):String{
                var xmlResult = StringBuilder()
                try {
                    var url = URL(urlPath)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    val response = connection.responseCode
                    Log.d(TAG, "The response code is $response")
                    connection.inputStream.buffered().reader().use {
                        xmlResult.append(it.readText())
                    }
                    Log.d(TAG, "Received ${xmlResult.length} bytes")
                    return xmlResult.toString()
                }catch(e:Exception){
                    val errorMessage: String = when(e){
                        is MalformedURLException -> "downloadXML: Invalid URL ${e.message}"
                        is SecurityException -> {
                            e.printStackTrace()
                            "downloadXML: Security Exception. Need permissions? ${e.message}"
                        }
                        is IOException -> "downloadXML: IO exception ${e.message}"
                        else -> "downloadXML: Unknown exception ${e.message}"
                    }
                }
                return ""
            }
        }
    }
}
