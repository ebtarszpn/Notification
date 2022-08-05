package com.chipmunk.notificationstudy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.chipmunk.notificationstudy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding

    //渠道id
    private val channelId = "test"

    //渠道名
    private val channelName = "测试通知"

    //渠道重要等级
    private val importance = NotificationManagerCompat.IMPORTANCE_HIGH

    //通知id
    private val notificationId = 1

    //通知管理者
    private lateinit var notificationManager: NotificationManager

    //通知
    private lateinit var notification: Notification


    //回复通知
    private val replyNotificationId = 2
    private lateinit var replyNotification: Notification

    //横幅通知
    private val bannerNotificationId = 3
    private lateinit var bannerNotification: Notification
    private val bannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Log.d("TAG", "返回结果 ")
            }else{
                Log.d("TAG", "横幅通知权限申请失败 ")
            }
        }

    private fun openBannerNotification() = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
        val bannerImportance =
            notificationManager.getNotificationChannel("banner").importance
        Log.e("TAG", "openBannerNotification: ${notificationManager.getNotificationChannel("banner").importance}" )
        if (bannerImportance == NotificationManager.IMPORTANCE_DEFAULT) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, "banner")
            bannerLauncher.launch(intent)
            false
        } else true
    } else true

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createBannerNotificationChannel(
        channelId: String,
        channelName: String,
        importance: Int
    ) = notificationManager.createNotificationChannel(
        NotificationChannel(channelId, channelName, importance).apply {
            description = "提醒式通知"
            enableLights(true)//闪光灯
            lightColor = Color.BLUE//闪光灯颜色
            enableVibration(true)//开启震动
            vibrationPattern = longArrayOf(0, 1000, 500, 1000)//振动模式
            setSound(null, null)//没有提示音
        }
    )


    //自定义通知
    private lateinit var customNotification: Notification

    //自定义通知Id
    private val customNotificationId = 5


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        //获取系统通知服务
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        //初始化通知
        initNotification()

        //显示通知
        mainBinding.btnShow.setOnClickListener {

            notificationManager.notify(notificationId, notification)
        }

        initReplyNotification()
        mainBinding.btnShowReply.setOnClickListener {
            notificationManager.notify(replyNotificationId, replyNotification)
        }

        //显示横幅通知
        initBannerNotification()
        mainBinding.btnShowBanner.setOnClickListener {
            if (openBannerNotification()) {
                notificationManager.notify(bannerNotificationId, bannerNotification)
            }
        }

        //显示常驻通知
        mainBinding.btnShowPermanent.setOnClickListener {
            showPermanentNotification()
        }

        //显示自定义通知
        initCustomNotification()
        mainBinding.btnShowCustom.setOnClickListener {
            notificationManager.notify(customNotificationId, customNotification)
        }

    }

    private fun initNotification() {

        val title = "打工人"
        val content = "我要搞钱！！！富强、明主、文明、和谐、自由、平等、公正、法治、爱国、敬业、诚信、友善"
        val intent = Intent(this, DetailsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", title)
            putExtra("content", content)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


        notification = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            createNotificationChannel(channelId, channelName, importance)
            NotificationCompat.Builder(this, channelId)
        } else {
            NotificationCompat.Builder(this)
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            setContentTitle("打工人")
            setContentText("我要搞钱！")
            setContentIntent(pendingIntent)
            setAutoCancel(true)//自动取消
            setStyle(NotificationCompat.BigTextStyle().bigText(content))//通知文字内容过长自动伸缩
        }.build()
    }

    private fun initReplyNotification() {
        val remoteInput = RemoteInput
            .Builder("key_text_reply")
            .setLabel("快速回复")
            .build()
        val replyIntent = Intent(this, ReplyMessageReceiver::class.java)
        val pendingIntent = PendingIntent
            .getBroadcast(this, 0, replyIntent, PendingIntent.FLAG_ONE_SHOT)
        val action = NotificationCompat.Action
            .Builder(0, "回复", pendingIntent)
            .addRemoteInput(remoteInput)
            .build()

        replyNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("reply", "回复消息", importance)
            NotificationCompat.Builder(this, "reply")
        } else {
            NotificationCompat.Builder(this)
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))//大图标（显示在通知上）
            setContentTitle("1008666")//标题
            setContentText("你的账号已欠费2000元！")//内容
            addAction(action)
        }.build()
    }

    private fun initBannerNotification() {

        bannerNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createBannerNotificationChannel("banner", "提醒消息", NotificationManagerCompat.IMPORTANCE_HIGH)
            NotificationCompat.Builder(this, "banner")
        } else {
            NotificationCompat.Builder(this)
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))//大图标（显示在通知上）
            setContentTitle("落魄Android在线炒粉")//标题
            setContentText("不要9块9，不要6块9，只要3块9。")//内容
            setWhen(System.currentTimeMillis())//通知显示时间
            setAutoCancel(true)//设置自动取消
        }.build()
    }

    private fun showPermanentNotification() {
        val permanentIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, permanentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val permanentNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("permanent", "常驻通知", importance)
            NotificationCompat.Builder(this, "permanent")
        } else {
            NotificationCompat.Builder(this)
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))//大图标（显示在通知上）
            setContentTitle("你在努力些什么")//标题
            setContentText("搞钱！搞钱！还是搞钱！")//内容
            setWhen(System.currentTimeMillis())//通知显示时间
            setContentIntent(pendingIntent)
        }.build()
        permanentNotification.flags = Notification.FLAG_ONGOING_EVENT
        notificationManager.notify(4, permanentNotification)
    }

    private fun initCustomNotification() {
        val remoteViews = RemoteViews(packageName, R.layout.layout_custom_notification)
        val bigRemoteViews = RemoteViews(packageName, R.layout.layout_custom_notification_big)
        customNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("custom", "自定义通知", importance)
            NotificationCompat.Builder(this, "custom")
        } else {
            NotificationCompat.Builder(this)
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
//            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setCustomContentView(remoteViews)
            setCustomBigContentView(bigRemoteViews)
            setOnlyAlertOnce(true)
            setOngoing(true)
        }.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        channelId: String,
        channelName: String,
        importance: Int
    ) {
        notificationManager.createNotificationChannel(
            NotificationChannel(channelId, channelName, importance)
        )
    }
}
