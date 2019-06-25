package com.teknhe.androidkotlinmusiclist

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest
import android.support.v4.app.ActivityCompat
import android.util.Log

// Initialize a new data class to hold music data
data class Music(val id:Long, val title:String)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Important : handle the runtime permission
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            // Check runtime permission to read external storage
            setupPermissions()
        }

        // Button click listener
        button.setOnClickListener{
            // Get the external storage/sd card music files list
            val list:MutableList<Music> = musicFiles()

            // Get the sd card music titles list
            val titles = mutableListOf<String>()
            for (music in list){titles.add(music.title)}

            // Display external storage music files list on list view
            val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,titles)
            list_view.adapter = adapter
        }

    }

    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            RECORD_REQUEST_CODE)
    }

    // Extension method to get all music files list from external storage/sd card
    fun Context.musicFiles():MutableList<Music>{
        // Initialize an empty mutable list of music
        val list:MutableList<Music> = mutableListOf()

        // Get the external storage media store audio uri
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //val uri: Uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI

        // IS_MUSIC : Non-zero if the audio file is music
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"

        // Sort the musics
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        //val sortOrder = MediaStore.Audio.Media.TITLE + " DESC"

        // Query the external storage for music files
        val cursor: Cursor = this.contentResolver.query(
            uri, // Uri
            null, // Projection
            selection, // Selection
            null, // Selection arguments
            sortOrder // Sort order
        )

        // If query result is not empty
        if (cursor!= null && cursor.moveToFirst()){
            val id:Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val title:Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)

            // Now loop through the music files
            do {
                val audioId:Long = cursor.getLong(id)
                val audioTitle:String = cursor.getString(title)

                // Add the current music to the list
                list.add(Music(audioId,audioTitle))
            }while (cursor.moveToNext())
        }

        // Finally, return the music files list
        return  list
    }
}
