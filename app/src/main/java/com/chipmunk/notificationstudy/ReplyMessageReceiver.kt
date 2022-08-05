package com.chipmunk.notificationstudy

import android.app.NotificationManager
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class ReplyMessageReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = ReplyMessageReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {

        //获取回复消息内容
        val inputContent = RemoteInput
            .getResultsFromIntent(intent)?.getCharSequence("key_text_reply").toString()
        Log.d(TAG, "onReceive: $inputContent")

        if (inputContent.isEmpty()) {
            Log.e(TAG, "onReceive: 没有回复消息！")
            return
        }

        //构建回复消息通知
        val replyNotification = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, "reply")
        } else {
            NotificationCompat.Builder(context)
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle("1008666")
            setContentText("消息发送成功")
        }.build()

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //发送通知
        notificationManager.notify(2, replyNotification)
        //一秒后取消
        Timer().schedule(object : TimerTask() {
            override fun run() {
                notificationManager.cancel(2)
            }
        }, 1000)

    }
}