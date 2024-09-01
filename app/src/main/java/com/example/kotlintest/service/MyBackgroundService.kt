package com.example.kotlintest.service

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import android.util.Pair
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.kotlintest.MainActivity.Companion.totalAudioSize
import com.example.kotlintest.MainActivity.Companion.totalDocimentsSize
import com.example.kotlintest.MainActivity.Companion.totalImageSize
import com.example.kotlintest.MainActivity.Companion.totalVideoSize
import com.example.kotlintest.R
import com.example.kotlintest.api.ApiModel
import com.example.kotlintest.api.RetrofitBaseUrl
import com.example.kotlintest.drawoverlay.OverlayDeleteListener
import com.example.kotlintest.drawoverlay.OverlayDialog
import com.example.kotlintest.drawoverlay.OverlayDismissListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MyBackgroundService : Service() {

    companion object {
        const val ACTION_PROCESS_SMS = "com.example.kotlintest.action.PROCESS_SMS"
        const val EXTRA_SMS_MESSAGE = "extra_sms_message"
        const val EXTRA_SMS_SENDER = "extra_sms_sender"
        const val ACTION_PROCESS_SMS_WITH_LINKS =
            "com.example.kotlintest.action.PROCESS_SMS_WITH_LINKS"
        const val EXTRA_SMS_LINKS = "extra_sms_links"

        private var imagesSizeBytes: Long = 0
        private var audioSizeBytes: Long = 0
        private var videoSizeBytes: Long = 0
        private var documentsSizeBytes: Long = 0

        const val NOTIFICATION_CHANNEL_ID = "BackgroundServiceChannel"
        private var notificationIdCounter = 0
        private val NOTIFICATION_ID = 1

        private val notificationHandler = Handler(Looper.getMainLooper())
        private val smsReceiver = SmsReceiver()

        private val allowedExtensions =
            listOf(
                ".doc",
                ".docx",
                ".pptx",
                ".pdf",
                ".xls",
                ".xlsx",
                ".ppt",
                ".mp3",
                ".m4a",
                ".mp4",
                ".3gp",
                ".jpg",
                ".jpeg",
                ".png",
                ".webp",
                ".jpg",
                ".jpeg",
                ".png",
                ".gif",
                ".tiff",
                ".tif",
                ".svg",
                ".webp",
                ".mp4",
                ".mov",
                ".mkv",
                ".wmv",
                ".webm",
                ".mpg",
                ".mpeg",
                ".3gp",
                ".mp3",
                ".wav",
                ".m4a",
                ".mid",
                ".midi",
                ".aiff",
                ".aif",
                ".doc",
                ".docx",
                ".pdf",
                ".txt",
                ".xlsx",
                ".xls",
                ".ppt",
                ".pptx",
                ".rtf",
                ".csv",
                ".pages",
                ".zip",
                ".rar",
                ".7z",
                ".tar",
                ".gz",
                ".apk", ".obb", ".ota", ".img", ".bin", ".ipsw", ".plist", ".ipa", ".tar", ".gz"

            )


        private val suspiciousKeywords = setOf(
            "com.koderspoint.virusapp",
            "spy",
            "hack",
            "cheat",
            "infected",
            "stealth",
            "fraudulent",
            "malicious",
            "dangerous",
            "unauthorized",
            "exploitative",
            "compromised",
            "infiltrator",
            "spammy",
            "deceptive",
            "counterfeit",
            "hijack",
            "scammer",
            "decoy",
            "phisher",
            "intruder",
            "suspicion",
            "threatening",
            "evil",
            "keybagd",
            "fs.db",
            "arm64e.encrypted",
            "Shortcuts.realm",
            "Shortcuts.realm.lock",
            "jsPayload.js.encrypted",
            "jskey.txt",
            "predkey.txt",
            "com.xyz",
            "xyz",
            "skulls.sis",
            "info.sis",
            "DoomBoot.a",
            "DoomBoot.b",
            "DoomBoot.c",
            "Android.Trojan.FakeApp",
            "Android.Trojan.GinMaster",
            "Android.Trojan.SMSsend",
            "Android.Adware.Ewind",
            "Android.Adware.WalkFree",
            "Android.Adware.Gmobi",
            "gmobi.apk",
            "g.mobogenie.markets.apk",
            "Android.Adware.Fictus",
            "qtrun.apk",
            "Ks.app.maker.gy",
            "Android.Ransomware.LockScreen",
            "Android.Ransomware.WannaLocker",
            "Android.Banker.A2f8a",
            "Android.Banker.B2ec",
            "com.android.browser-1.apk",
            "com.google.provider",
            "com.android.vending-1.apk",
            "com.google.update-2.apk",
            "Andr/Clickr-FG",
            "Andr/Candre.A!tr",
            "Andr/Agent.DV",
            "howto.prg",
            "com.android.service.secure",
            "com.google.protection",
            "com.wifi.network.gol",
            "com.mobile.secure.browser",
            "com.mobile.security.antivirus",
            "com.mobile.security.protector",
            "com.intelexxa.lite",
            "com.intelexxa.main",
            "com.intelexxa.mmp",
            "com.intelexxa.pro",
            "com.intelexxa.update",
            "com.intelexxa.zxy",
            "com.intelexxa.prestige",
            "com.intelexxa.system",
            "com.intelexxa.utils"
/*
            "com.superking.ludo.star"
*/
        )

        private val deletedFilesList: MutableList<Pair<String, String>> = mutableListOf()

    }

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.NOTIFICATION_LINK_RECEIVED") {


                val links = intent.getStringArrayListExtra("links")
                if (links != null) {
                    for (link in links) {
                        OverlayDialog.showOverlaySuspesiousNotificationLinkDialog(
                            this@MyBackgroundService,
                            links
                        )
                    }
                }

            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerSmsReceiver()
        registerReceiver(
            notificationReceiver,
            IntentFilter("com.example.NOTIFICATION_LINK_RECEIVED")
        )
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun registerSmsReceiver() {
        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, filter)
    }

    fun processIncomingSms(message: String, sender: String) {
        // Process the incoming SMS here
        Toast.makeText(
            this,
            "Received SMS from: $sender\n" + "Message: $message",
            Toast.LENGTH_SHORT
        ).show()
        Log.d("MyBackgroundService", "Received SMS from: $sender\nMessage: $message")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED == intent.action) {

            startForegroundService()
//            checkForSuspiciousApps()
            notificationHandler.postDelayed(notificationRunnable, 30000)
            notificationHandler.postDelayed(suspiciousAppCheckRunnable, 30000)

            Log.d("TAG", "onStartCommand:reboot come again ")


        } else {

        }
        if (intent?.action == ACTION_PROCESS_SMS) {
            Log.d("TAG", "onStartCommand:ACTION_PROCESS_SMS ")
            val message = intent.getStringExtra(EXTRA_SMS_MESSAGE)
            val sender = intent.getStringExtra(EXTRA_SMS_SENDER)

            if (message != null && sender != null) {
//                processIncomingSms(message, sender)
            }
        }
        if (intent?.action == ACTION_PROCESS_SMS_WITH_LINKS) {
            Log.d("TAG", "onStartCommand:ACTION_PROCESS_SMS_WITH_LINKS ")
            val message = intent.getStringExtra(EXTRA_SMS_MESSAGE)
            val sender = intent.getStringExtra(EXTRA_SMS_SENDER)
            val links = intent.getStringArrayListExtra(EXTRA_SMS_LINKS)

            if (message != null && sender != null && links != null) {
                processIncomingSmsWithLinks(message, sender, links)
            }
        }

        Thread {
            while (true) {
                deletePdfFiles()
                checkForSuspiciousApps()
//                deletePdfFilesXYZ()

                Log.d("TAG", "onStartCommand:Thread aya ")
                Thread.sleep(30000)
            }
        }.start()

        if (intent?.getBooleanExtra("compareSizes", false) == true) {

            Log.d("TAG", "onStartCommand: compareSizes ")
            val preferences = getSharedPreferences(packageName, Context.MODE_PRIVATE)

            val imagesSize = preferences.getLong("ImagesSize", -1)
            val imagesUnit = preferences.getString("ImagesUnit", "")

            val audioSize = preferences.getLong("AudioSize", -1)
            val audioUnit = preferences.getString("AudioUnit", "")

            val videoSize = preferences.getLong("VideoSize", -1)
            val videoUnit = preferences.getString("VideoUnit", "")

            val documentsSize = preferences.getLong("DocumentsSize", -1)
            val documentsUnit = preferences.getString("DocumentsUnit", "")

            // Calculate the user-entered sizes in bytes
            imagesSizeBytes = convertToBytes(imagesSize, imagesUnit)
            audioSizeBytes = convertToBytes(audioSize, audioUnit)
            videoSizeBytes = convertToBytes(videoSize, videoUnit)
            documentsSizeBytes = convertToBytes(documentsSize, documentsUnit)


        }
        notificationHandler.postDelayed(notificationRunnable, 30000)
        notificationHandler.postDelayed(suspiciousAppCheckRunnable, 30000)


        return START_NOT_STICKY
    }

    private val notificationRunnable = object : Runnable {
        override fun run() {
            if (imagesSizeBytes > 0) {
                compareAndShowNotification("Images", imagesSizeBytes, totalImageSize)
            }
            if (audioSizeBytes > 0) {
                compareAndShowNotification("Audio", audioSizeBytes, totalAudioSize)
            }
            if (videoSizeBytes > 0) {
                compareAndShowNotification("Video", videoSizeBytes, totalVideoSize)
            }
            if (documentsSizeBytes > 0) {
                compareAndShowNotification("Documents", documentsSizeBytes, totalDocimentsSize)
            }

            notificationHandler.removeCallbacks(this)
            notificationHandler.postDelayed(this, 30000)
        }
    }

    private fun processIncomingSmsWithLinks(message: String, sender: String, links: List<String>) {
        // Process the incoming SMS with links here
        for (link in links) {
            OverlayDialog.showOverlaySuspesiousLinkDialog(this, sender, link)

        }
    }

    private fun createNotification(): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, "NOTIFICATION_CHANNEL_ID")
            .setSmallIcon(R.drawable.stop)
            .setContentTitle("Snitch")
            .setContentText("Hunting Spyware is running in the background")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "NOTIFICATION_CHANNEL_ID",
                "Background Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return notificationBuilder.build()
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun deletePdfFiles() {
        runBlocking {
            launch(Dispatchers.IO) {
                val externalStorageVolumes: Array<out File> =
                    ContextCompat.getExternalFilesDirs(applicationContext, null)

                for (file in externalStorageVolumes) {
                    if (file != null) {
                        val storagePath = file.absolutePath.split("/Android")[0]
                        val storageDirectory = File(storagePath)
                        deletePdfFilesInDirectory(storageDirectory)
                    }
                }
            }
        }
    }

    private fun deletePdfFilesInDirectory(directory: File) {
        val fileList = directory.listFiles()
        Log.d("delete", "fileList: $fileList")
        var counter = 1

        fileList?.forEach { file ->
            if (file.isDirectory) {
                deletePdfFilesInDirectory(file)
            } else {
//                      val fileExtension = getFileExtension(file.name)
                //  if (!allowedExtension.contains(fileExtension)) {
                val fileExtension = getFileExtension(file.name)
                if (!allowedExtensions.contains(fileExtension)) {
//                    if (fileExtension == ".xyz") {
                    val fileName = file.name
                    val filePath = file.absolutePath
                    val deleted = file.delete()
                    if (deleted) {
                        val fileName1 = "dummy$counter.txt" // Generate unique file name
                        counter++
                        val filePath1 = file.absolutePath
                        val customFilePath = filePath1.replaceAfterLast("/", fileName1) // Change the file name

                        val customFileInputStream = applicationContext.assets.open("dummy.txt")
                        val customFileOutputStream = FileOutputStream(customFilePath)
                        customFileInputStream.use { input ->
                            customFileOutputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        customFileOutputStream.close()

                        // Rename the file
                        val newFile = File(customFilePath)
                        file.renameTo(newFile)


                        notificationHandler.postDelayed({
                            OverlayDialog.showOverlayDialog(
                                this,
                                object : OverlayDismissListener {
                                    override fun onOverlayDismissed() {

                                    }

                                })
                            showDeleteNotification(fileName, filePath)
                        }, 30000)
                        val fileInfo = Pair(fileName, filePath)
                        deletedFilesList.add(fileInfo)
                        Log.d("MyBackgroundService", "Deleted: ${file.absolutePath}")
                    } else {
                        Log.e("MyBackgroundService", "Failed to delete: ${file.absolutePath}")
                    }
//                    }
                }
            }
        }
    }


      private fun deletePdfFilesXYZ() {
          runBlocking {
              launch(Dispatchers.IO) {
                  val externalStorageVolumes: Array<out File> =
                      ContextCompat.getExternalFilesDirs(applicationContext, null)
                  Log.d("TAG", "deletePdfFilesXYZ: externalStorageVolumes $externalStorageVolumes")
                  for (file in externalStorageVolumes) {
                      if (file != null) {
                          Log.d("TAG", "deletePdfFilesXYZ: file null ni ha")

                          val storagePath = file.absolutePath.split("/Android")[0]
                          Log.d("TAG", "deletePdfFilesXYZ: storagePath $storagePath")

                          val storageDirectory = File(storagePath)
                          deletePdfFilesInDirectoryXYZ(storageDirectory)
                      }
                  }
              }
          }
      }

      private fun deletePdfFilesInDirectoryXYZ(directory: File) {
          Log.d("TAG", "deletePdfFilesInDirectoryXYZ: aya ")
          val fileList = directory.listFiles()
          Log.d("delete", "deletePdfFilesInDirectoryXYZ:list $fileList")
          var counter = 1
          fileList?.forEach { file ->
              if (file.isDirectory) {
                  deletePdfFilesInDirectoryXYZ(file)
              } else {
  //                  val fileExtension = getFileExtension(file.name)
                  //  if (!allowedExtension.contains(fileExtension)) {
                  val fileExtension = getFileExtension(file.name)
                  Log.d("TAG", "deletePdfFilesInDirectoryXYZ: $fileExtension")

  //                if (!allowedExtensions.contains(fileExtension)) {
                  if (fileExtension.equals(".xyz", ignoreCase = true)) {
                      Log.d("TAG", "deletePdfFilesInDirectoryXYZ: fileExtension png me aya")
                      val fileName = file.name
                      Log.d("TAG", "deletePdfFilesInDirectoryXYZ: fileName $fileName")

                      val filePath = file.absolutePath
                      Log.d("TAG", "deletePdfFilesInDirectoryXYZ: filePath $filePath")

                      val deleted = file.delete()
                      if (deleted) {
                          val fileName1 = "dummy$counter.txt" // Generate unique file name
                          counter++
                          val filePath1 = file.absolutePath
                          val customFilePath = filePath1.replaceAfterLast("/", fileName1) // Change the file name

                          val customFileInputStream = applicationContext.assets.open("dummy.txt")
                          val customFileOutputStream = FileOutputStream(customFilePath)
                          customFileInputStream.use { input ->
                              customFileOutputStream.use { output ->
                                  input.copyTo(output)
                              }
                          }
                          customFileOutputStream.close()

                          // Rename the file
                          val newFile = File(customFilePath)
                          file.renameTo(newFile)

                          notificationHandler.postDelayed({
                              OverlayDialog.showOverlayDialog(
                                  this,
                                  object : OverlayDismissListener {
                                      override fun onOverlayDismissed() {

                                      }

                                  })
                              showDeleteNotification(fileName, filePath)
                          }, 30000)
                          val fileInfo = Pair(fileName, filePath)
                          deletedFilesList.add(fileInfo)
                          Log.d("MyBackgroundService", "Deleted: ${file.absolutePath}")
                      } else {
                          Log.e("MyBackgroundService", "Failed to delete: ${file.absolutePath}")
                      }
                  }
              }
          }
      }


    private val suspiciousAppCheckRunnable = object : Runnable {
        override fun run() {
            checkForSuspiciousApps()
            notificationHandler.postDelayed(this, 30000)
        }
    }

    fun checkForSuspiciousApps() {
        val packageManager = this.packageManager
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        for (packageInfo in installedPackages) {
            val packageName = packageInfo.packageName
            val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()

            if (isSuspicious(packageName)) {
                Log.d(
                    "onStartCommand",
                    "Suspicious app detected: appName:: $appName packageName:: ($packageName)"
                )

                notificationHandler.postDelayed({

                    OverlayDialog.showOverlayDeleteDialog(this@MyBackgroundService,
                        object : OverlayDeleteListener {
                            override fun onOverlayDelete() {
                                uninstallApp(packageName)
                            }

                        })


                }, 30000)


            } else {
                Log.d("onStartCommand", "No Suspicious app detected:")

            }
        }
    }

    private fun isSuspicious(packageName: String): Boolean {
        for (keyword in suspiciousKeywords) {
            if (packageName.contains(keyword, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun getFileExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0 && dotIndex < fileName.length - 1) {
            fileName.substring(dotIndex)
        } else {
            ""
        }
    }

    private fun showNotification(message: String) {
        val notificationId = notificationIdCounter++

        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setSmallIcon(R.drawable.stop)
            .setContentTitle("Storage Limit Exceeded")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id",
                "Snitch",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun showDeleteNotification(fileName: String, filePath: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Snitch",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val soundUri = Uri.parse("android.resource://${packageName}/${R.raw.warning}")
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("File Deleted")
            .setContentText("$fileName\nPath: $filePath")
            .setSmallIcon(R.drawable.stop)
            .setSound(soundUri)
            .build()

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }

    private fun compareAndShowNotification(category: String, userSize: Long, totalSize: Long) {
        if (userSize != -1L && userSize < totalSize) {

            val message = "$category limit exceeded"
            showNotification(message)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri =
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/raw/virus")
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Snitch",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setSound(soundUri, audioAttributes)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "NOTIFICATION_CHANNEL_ID",
                "Background Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "NOTIFICATION_CHANNEL_ID")
            .setContentTitle("Snitch")
            .setContentText("Hunting Spyware is running in the background")
            .setSmallIcon(R.drawable.stop)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun uninstallApp(packageName: String) {
        Log.d("TAG", "onStartCommand:packageName $packageName ")
        val packageUri = Uri.parse("package:$packageName")
        val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
        uninstallIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        Log.d("TAG", "onStartCommand:packageName startActivity ")
        startActivity(uninstallIntent)
    }

    private fun convertToBytes(size: Long, unit: String?): Long {
        return when (unit) {
            "KB" -> size * 1024L
            "MB" -> size * 1024L * 1024L
            "GB" -> size * 1024L * 1024L * 1024L
            else -> size
        }
    }

    private fun ApiData() {

        val request = ApiModel("Your text to send")
        val call = RetrofitBaseUrl.apiService.sendText(request)
        call.enqueue(object : Callback<ApiModel> {
            override fun onResponse(call: Call<ApiModel>, response: Response<ApiModel>) {
                if (response.isSuccessful) {
                    val apiModel = response.body()
                    Log.d("TAG", "onResponse: $apiModel")
                } else {
                    Log.d("TAG", "onResponse: faild ")
                }
            }

            override fun onFailure(call: Call<ApiModel>, t: Throwable) {
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })

    }
}


