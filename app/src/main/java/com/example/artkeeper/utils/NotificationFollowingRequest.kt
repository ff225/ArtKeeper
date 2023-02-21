package com.example.artkeeper.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.artkeeper.R
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

class NotificationFollowingRequest : Service() {
    private lateinit var pendingReqQuery: DatabaseReference

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        pendingReqQuery =
            Constants.databaseRef.getReference("users").child(Constants.firebaseAuth.uid!!)
                .child("pendingRequestFrom")
        pendingReqQuery.addChildEventListener(listener)

        return START_NOT_STICKY
    }

    private val listener = object : ChildEventListener {

        override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
            Log.d("ServiceNotification", "onChildAdded")
            sendNotification(applicationContext, "Hai ricevuto una richiesta di amicizia")
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ServiceNotification", "onChildChanged")
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d("ServiceNotification", "onChildRemoved")
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ServiceNotification", "onChildMoved")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d("ServiceNotificationa", "onCancelled")
        }
    }

    override fun onDestroy() {
        pendingReqQuery.removeEventListener(listener)
        stopSelf()
    }

    private fun sendNotification(context: Context, message: String?) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = getString(R.string.channel_id)
        val channelName = getString(R.string.channel_name)
        val importance = NotificationManager.IMPORTANCE_HIGH

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(mChannel)
        }

        val mBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, mBuilder.build())
    }
}

