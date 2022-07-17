package eu.deysouvik.projectkaro.FireBase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import eu.deysouvik.projectkaro.Object.Constants
import eu.deysouvik.projectkaro.R
import eu.deysouvik.projectkaro.activity.Activities.MainActivity
import eu.deysouvik.projectkaro.activity.Activities.WelcomeActivity
import java.util.*

class MyFirebaseMessagingService:FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if(remoteMessage.data.isNotEmpty()){
            val title=remoteMessage.data[Constants.FCM_KEY_TITLE]!!
            val message=remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!
            sendNotification(message,title)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("Refreshed Token",token)
        sendRegistrationToServer(token)
    }

    fun sendRegistrationToServer(token:String?){
        //TODO
    }

    private fun sendNotification(message:String,title:String){
        val intent= if(!FireStore().getUserId().isNullOrEmpty()){
                Intent(this,MainActivity::class.java)
            }else{
                Intent(this,WelcomeActivity::class.java)
            }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)
        val chanelId="PROJECTKARO_NOTIFICATION_CHANEL_ID"
        val defaultSoundUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder=NotificationCompat.Builder(this, chanelId,)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setWhen(Calendar.getInstance().timeInMillis)
            .setContentIntent(pendingIntent)
        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel=NotificationChannel(chanelId,"Project Karo title",NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())
    }

}