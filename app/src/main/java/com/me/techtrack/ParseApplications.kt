package com.me.techtrack

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class ParseApplications {
    private val TAG = "ParseApplications"
    val applications = ArrayList<FeedEntry>()

    fun parse(xmlData:String):Boolean{
        Log.d(TAG,"parse called with $xmlData")
        var status= true
        var isItem= false
        var textValue = ""
        try{
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(xmlData.reader())
            var eventType = xpp.eventType
            var currentRecord = FeedEntry()
            while(eventType !=  XmlPullParser.END_DOCUMENT){
                var tagName = xpp.name?.toLowerCase()
                when(eventType){
                    XmlPullParser.START_TAG ->{
                        Log.d(TAG,"parse: starting tag, $tagName")
                        if(tagName == "item"){
                            isItem = true
                        }
                    }
                    XmlPullParser.TEXT -> textValue = xpp.text
                    XmlPullParser.END_TAG -> {
                        Log.d(TAG,"Parse, Ending tag for $tagName")
                        if(isItem) {
                            when (tagName) {
                                "item" -> {
                                    applications.add(currentRecord)
                                    isItem = false
                                    currentRecord = FeedEntry()
                                }
                                "title" -> currentRecord.title = textValue
                                "pubdate" -> currentRecord.pubDate = textValue
                                "creator" -> currentRecord.creater = textValue
                                "description" -> currentRecord.description = textValue
                                "content" -> currentRecord.content = textValue
                                "link" -> currentRecord.link = textValue
                                "category" -> currentRecord.category?.add(textValue)
                            }
                        }
                    }
                }
                eventType = xpp.next()
            }
            for(app in applications){
                Log.d(TAG,"*****************")
                Log.d(TAG,app.toString())
            }
        }catch(e:Exception){
            e.printStackTrace()
            status = false
        }
        return status
    }
}