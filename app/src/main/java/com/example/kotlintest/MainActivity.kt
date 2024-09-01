package com.example.kotlintest

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.net.VpnService
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.kotlintest.databinding.ActivityMainBinding
import com.example.kotlintest.incomingnotification.MyNotificationListenerService
import com.example.kotlintest.service.BootReceiver
import com.example.kotlintest.service.MyBackgroundService
import com.example.kotlintest.vpn.Interfaces.ChangeServer
import com.example.kotlintest.vpn.Server
import com.example.kotlintest.vpn.model.MainViewModelServer
import com.example.kotlintest.vpn.model.ServerModel
import com.example.kotlintest.vpn.util.CheckInternetConnection
import com.example.kotlintest.vpn.util.SharedPreference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.text.DecimalFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var i = 0
    var userEnteredSize: Double? = null
    var userEnteredUnitImages: String? = null
    var userEnteredUnitAudio: String? = null
    var userEnteredUnitVideo: String? = null
    var userEnteredUnitDocuments: String? = null
    private var notificationIdCounter = 0
    var preferences:SharedPreferences?=null
    val items = arrayOf("KB", "GB", "MB")
    private val foregroundServicePermissionCode = 101
    private val PERMISSION_REQUEST_CODE = 123
    private  val OVERLAY_PERMISSION_REQUEST_CODE = 123
    private val UNINSTALL_PERMISSION_REQUEST_CODE = 101
    val PERMISSION_REQUEST_SMS = 1
    private  val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    private val MANAGE_EXTERNAL_STORAGE_PERMISSION_CODE = 200

    private var server: Server? = null
    private var connection: CheckInternetConnection? = null
    private var changeServer: ChangeServer? = null
    private var flag: Boolean = false
    var vpnStart = false
    private var preference: SharedPreference? = null
    var hostName: String? = ""

    var serverLists: ArrayList<Server> = ArrayList()
    private val bootReceiver = BootReceiver()

    private var isFirstTime = false

    val model: MainViewModel by viewModels()

    companion object {
        var stat = StatFs(Environment.getExternalStorageDirectory().absolutePath)

        var totalImageSize = 0L
        var totalVideoSize = 0L
        var totalAudioSize = 0L
        var totalAppsSize = 0L
        var totalDocimentsSize=0L

        var totalDocumentsSize = 0L
        var imageList = ArrayList<String>()
        var videoList = ArrayList<String>()
        var audioList = ArrayList<String>()
        var appsList = ArrayList<String>()
        var documentsList = ArrayList<String>()
        const val PERMISSION_REQUEST_CODE_MANAGE = 1012

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val model: MainViewModelServer by viewModels()
        model.populateServerList().observe(this) {
            serverLists = it
            Log.d("TAG", "onCreate all serverLists: $serverLists")
            server = serverLists[2]
            Log.d("prepareVpn", "onCreate:server $server ")
            Log.d("prepareVpn", "onCreate:server ${serverLists.get(2).ovpn} ")
        }
        init()
        prepareVpn()

        isServiceRunning()
        try {
            VpnStatus.initLogCache(this.cacheDir)
        } catch (e: Exception) {
        }
        recieveIntent()
        Handler()
            .postDelayed(Runnable {
                if (getInternetStatus()) {
                    try {
                        flag = true
                      /*  binding!!.txtFindingServer.visibility = View.GONE
                        binding!!.layoutCurrentSever.visibility = View.VISIBLE*/
                        server = preference!!.getServer()
                        if (!this.isDestroyed) {
                         /*   Glide.with(this)
                                .load(server!!.flagUrl)
                                .into(binding!!.imgServerFlag)*/
                        }
//                        binding!!.txtServerName.text = server!!.country
                        Log.d("TAG", "onCreate: ${server!!.country}")
                    } catch (e: Exception) {
                    }
                } else {
//                    binding!!.txtFindingServer.text = "Please Check Your Internet Connection"

                }

            }, 2000)

        startBackgroundService()
        if (checkPermission()){
            Log.d("TAG", "onCreate: -----")
        }else{
            checkAndRequestPermissions()
        }
//        checkAndRequestPermissions()

        checkOverlayPermission()
        if (!isNotificationListenerEnabled()) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }


        val hasPermission = PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_DELETE_PACKAGES)

        if (!hasPermission) {
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.REQUEST_DELETE_PACKAGES),
                UNINSTALL_PERMISSION_REQUEST_CODE
            )
        } else {
            // You already have the permission, proceed with your logic
        }


        loadModelData()
        spinnerAdapterData()

         preferences = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        val savedImagesSize = preferences?.getLong("ImagesSize", -1)
        val savedImagesUnit = preferences?.getString("ImagesUnit", "")

        val savedAudioSize = preferences?.getLong("AudioSize", -1)
        val savedAudioUnit = preferences?.getString("AudioUnit", "")

        val savedVideoSize = preferences?.getLong("VideoSize", -1)
        val savedVideoUnit = preferences?.getString("VideoUnit", "")

        val savedDocumentsSize = preferences?.getLong("DocumentsSize", -1)
        val savedDocumentsUnit = preferences?.getString("DocumentsUnit", "")

        if (savedImagesSize != -1L) {
            /*if (savedImagesSize!! < totalImageSize) {
                val message = "Images limit exceeded"
                showNotification(message)
                Toast.makeText(this, "Images limit exceeded", Toast.LENGTH_LONG).show()
            }*/

            // Set the saved size and unit in the EditText and Spinner
            binding.etSetSizeImages.setText(savedImagesSize.toString())
            val unitPosition = items.indexOf(savedImagesUnit)
            binding.spinnerImages.setSelection(unitPosition)
        }
        if (savedAudioSize != -1L) {
           /* if (savedAudioSize!! < totalAudioSize) {
                val message = "Audio limit exceeded"
                showNotification(message)
                Toast.makeText(this, "Audio limit exceeded", Toast.LENGTH_LONG).show()
            }
*/
            // Set the saved size and unit in the EditText and Spinner
            binding.etSetSizeAudio.setText(savedAudioSize.toString())
            val unitPosition = items.indexOf(savedAudioUnit)
            binding.spinnerAudio.setSelection(unitPosition)
        }
        if (savedVideoSize != -1L) {
          /*  if (savedVideoSize!! < totalVideoSize) {
                val message = "Video limit exceeded"
                showNotification(message)
                Toast.makeText(this, "Video limit exceeded", Toast.LENGTH_LONG).show()
            }*/

            // Set the saved size and unit in the EditText and Spinner
            binding.etSetSizeVideo.setText(savedVideoSize.toString())
            val unitPosition = items.indexOf(savedVideoUnit)
            binding.spinnerVideo.setSelection(unitPosition)
        }
        if (savedDocumentsSize != -1L) {
            /*if (savedDocumentsSize!! < totalDocimentsSize) {
                val message = "Documents limit exceeded"
                showNotification(message)
                Toast.makeText(this, "Documents limit exceeded", Toast.LENGTH_LONG).show()
            }*/


            binding.etSetSizeDocuments.setText(savedDocumentsSize.toString())
            val unitPosition = items.indexOf(savedDocumentsUnit)
            binding.spinnerDocuments.setSelection(unitPosition)
        }

        binding.cardConfirm.setOnClickListener {

            Toast.makeText(this, "Size limit set ", Toast.LENGTH_LONG).show()
            preferences = getSharedPreferences(packageName, Context.MODE_PRIVATE)
            val editor = preferences?.edit()

            val useretSetSizeImages = binding.etSetSizeImages.text.toString()
            val useretSetSizeAudio = binding.etSetSizeAudio.text.toString()
            val useretSetSizeVideo = binding.etSetSizeVideo.text.toString()
            val useretSetSizeDocuments = binding.etSetSizeDocuments.text.toString()

            val userEnteredSizeImages: Long? = useretSetSizeImages.toLongOrNull()
            val userEnteredSizeAudio: Long? = useretSetSizeAudio.toLongOrNull()
            val userEnteredSizeVideo: Long? = useretSetSizeVideo.toLongOrNull()
            val userEnteredSizeDocuments: Long? = useretSetSizeDocuments.toLongOrNull()

            if (userEnteredSizeImages != null) {
                val totalSizeImageBytes = totalImageSize
                val userEnteredSizeBytes = when (userEnteredUnitImages) {

                    "KB" -> userEnteredSizeImages * 1024L
                    "MB" -> userEnteredSizeImages * 1024L * 1024L
                    "GB" -> userEnteredSizeImages * 1024L * 1024L * 1024L
                    else -> userEnteredSizeImages
                }

              /*  if (userEnteredSizeBytes > totalSizeImageBytes) {
                    Toast.makeText(this, "Images limit remaining", Toast.LENGTH_LONG).show()
                } else {
                    val message ="Images limit exceeded"
                    showNotification(message)
                    Toast.makeText(this, "Images limit exceeded", Toast.LENGTH_LONG).show()
                }*/


                editor?.putLong("ImagesSize", userEnteredSizeImages)
                editor?.putString("ImagesUnit", userEnteredUnitImages)
                Log.d("preferences", "onCreate: putImageSize $userEnteredSizeImages")



            } else {

                Toast.makeText(this, " Enter Images Size", Toast.LENGTH_LONG).show()
            }
            if (userEnteredSizeAudio != null) {
                val totalSizeAudioBytes = totalAudioSize


                val userEnteredSizeBytes = when (userEnteredUnitAudio) {

                    "KB" -> userEnteredSizeAudio * 1024L
                    "MB" -> userEnteredSizeAudio * 1024L * 1024L
                    "GB" -> userEnteredSizeAudio * 1024L * 1024L * 1024L
                    else -> userEnteredSizeAudio
                }

                Log.d("TAG", "onCreate:  userEnteredUnit  ${userEnteredUnitAudio}")
                Log.d("TAG", "onCreate: endsize user ${userEnteredSizeBytes}")
                Log.d("TAG", "onCreate: endsize total ${totalSizeAudioBytes}")
               /* if (userEnteredSizeBytes > totalSizeAudioBytes) {

                    Toast.makeText(this, "Audio limit remaining", Toast.LENGTH_LONG).show()
                } else {
                    val message ="Audio limit exceeded"
                    showNotification(message)
                    Toast.makeText(this, "Audio limit exceeded", Toast.LENGTH_LONG).show()
                }*/

                editor?.putLong("AudioSize", userEnteredSizeAudio)
                editor?.putString("AudioUnit", userEnteredUnitAudio)

            } else {
                Toast.makeText(this, "Enter Audio Size", Toast.LENGTH_LONG).show()
            }

            if (userEnteredSizeVideo != null) {
                val totalSizeVideoBytes = totalVideoSize


                val userEnteredSizeBytes = when (userEnteredUnitVideo) {

                    "KB" -> userEnteredSizeVideo * 1024L
                    "MB" -> userEnteredSizeVideo * 1024L * 1024L
                    "GB" -> userEnteredSizeVideo * 1024L * 1024L * 1024L
                    else -> userEnteredSizeVideo
                }

                Log.d("TAG", "onCreate:  userEnteredUnit  ${userEnteredUnitVideo}")
                Log.d("TAG", "onCreate: endsize user ${userEnteredSizeBytes}")
                Log.d("TAG", "onCreate: endsize total ${totalSizeVideoBytes}")
               /* if (userEnteredSizeBytes > totalSizeVideoBytes) {

                    Toast.makeText(this, "Video limit remaining", Toast.LENGTH_LONG).show()
                } else {
                    val message ="Video limit exceeded"
                    showNotification(message)
                    Toast.makeText(this, "Video limit exceeded", Toast.LENGTH_LONG).show()
                }*/
                editor?.putLong("VideoSize", userEnteredSizeVideo)
                editor?.putString("VideoUnit", userEnteredUnitVideo)
            } else {
                Toast.makeText(this, "Enter Video Size", Toast.LENGTH_LONG).show()
            }

            if (userEnteredSizeDocuments != null) {
                val totalSizeDocumentsBytes = totalDocimentsSize


                val userEnteredSizeBytes = when (userEnteredUnitDocuments) {

                    "KB" -> userEnteredSizeDocuments * 1024L
                    "MB" -> userEnteredSizeDocuments * 1024L * 1024L
                    "GB" -> userEnteredSizeDocuments * 1024L * 1024L * 1024L
                    else -> userEnteredSizeDocuments
                }

                Log.d("TAG", "onCreate:  userEnteredUnit doc ${userEnteredUnitDocuments}")
                Log.d("TAG", "onCreate: endsize user doc ${userEnteredSizeBytes}")
                Log.d("TAG", "onCreate: endsize totaldoc ${totalSizeDocumentsBytes}")
               /* if (userEnteredSizeBytes > totalSizeDocumentsBytes) {
                    Toast.makeText(this, "Documents limit remaining", Toast.LENGTH_LONG).show()
                } else {
                    val message = "Documents limit exceeded"
                    showNotification(message)
                    Toast.makeText(this, "Documents limit exceeded", Toast.LENGTH_LONG).show()
                }*/
                editor?.putLong("DocumentsSize", userEnteredSizeDocuments)
                editor?.putString("DocumentsUnit", userEnteredUnitDocuments)
            } else {

                Toast.makeText(this, "Enter Documents Size", Toast.LENGTH_LONG).show()
            }
            editor?.apply()

            startBackgroundServiceForComparison()
        }

    }

    private fun startBackgroundServiceForComparison() {
        val serviceIntent = Intent(this, MyBackgroundService::class.java)
        serviceIntent.putExtra("compareSizes", true)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    private fun showNotification(message: String) {
        val notificationId = notificationIdCounter++

        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setSmallIcon(R.drawable.ic_baseline_circle_notifications_24)
            .setContentTitle("Storage Limit Exceeded")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
    private fun loadModelData() {
        //doc
        val totalDocumentSize = getTotalDocumentSize(this)
        var totalDocimentsSizeHere = formatSize(totalDocumentSize)
        binding.textViewgetDocumentsSize.text = totalDocimentsSizeHere
        totalDocimentsSize= totalDocumentSize

        val mTotal = getTotalDirectorySize(2)
        val mFree = bytesToHuman(getTotalUSedStorageSize(3), 2)
        binding.txtTotalSpace.text = "Total " + getTotalDirectorySize(1)
        val usedStorage = "${DecimalFormat("##.##").format(mTotal.toFloat() - mFree.toFloat())} GB"
        val usedStorage1 = DecimalFormat("##").format(mTotal.toFloat() - mFree.toFloat())
        binding.txtUsedSpace.text = "Used " + usedStorage

        val storagePercentage = ((usedStorage1.toFloat() * 100) / mTotal.toFloat())

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {

                if (i <= storagePercentage) {
                    binding.percentageTextView.text = "${i}%"
                    binding.circularProgressBar.progress = i

                    i++
                    handler.postDelayed(this, 30)
                } else {
                    handler.removeCallbacks(this)
                }
            }
        }, 100)

        model.getAllImage(contentResolver).observe(this) { images ->
            Log.d("Imagesize", "onCreate: size ${images.size}")
            imageList = images as ArrayList<String>
            binding.txtSetImgSize.text = formatSize(totalImageSize)
            Log.d("Audiosize", "getAllImage: size ${images.size}")


        }
        model.getVideo(contentResolver).observe(this) { video ->
            Log.d("Videosize", "onCreate: size ${video.size}")
            videoList = video as ArrayList<String>
            binding.textViewgetVideoSize.text = formatSize(totalVideoSize)
            Log.d("Audiosize", "getVideo: size ${video.size}")

        }
        model.getAudio(contentResolver).observe(this) { audio ->
            Log.d("Audiosize", "onCreate: size ${audio.size}")
            audioList = audio as ArrayList<String>
            binding.textViewgetAudioSize.text = formatSize(totalAudioSize)
            Log.d("Audiosize", "getAudio: size ${audio.size}")

        }
        model.getApps(this).observe(this) { apps ->
            Log.d("Audiosize", "onCreate: size ${apps.size}")
            appsList = apps as ArrayList<String>

            Log.d("TAG", "onCreate: apps ${apps.size}")

        }
        model.getPdfList(contentResolver).observe(this) { doc ->
            Log.d("Audiosize", "onCreate: size ${doc.size}")
            documentsList = doc as ArrayList<String>
//            binding.textViewgetDocumentsSize.text = formatSize(totalDocumentsSize)
            Log.d("Audiosize", "getPdfList: size ${doc.size}")

        }

    }
    private fun spinnerAdapterData(){
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(R.layout.simple_spinner_item)

        binding.spinnerImages.adapter = adapter
        binding.spinnerAudio.adapter = adapter
        binding.spinnerVideo.adapter = adapter
        binding.spinnerDocuments.adapter = adapter

        binding.spinnerImages.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = items[position]
                Log.d("SpinnerExample", "Selected item: $selectedItem")
                binding.spinnerImages.post {
                    binding.spinnerImages.setSelection(position)
                    userEnteredUnitImages = selectedItem
                    Log.d("TAG", "onItemSelected: unit $selectedItem")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected
            }
        }
        binding.spinnerAudio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = items[position]
                Log.d("SpinnerExample", "Selected item: $selectedItem")
                binding.spinnerAudio.post {
                    binding.spinnerAudio.setSelection(position)
                    userEnteredUnitAudio = selectedItem

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected
            }
        }
        binding.spinnerVideo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = items[position]
                Log.d("SpinnerExample", "Selected item: $selectedItem")
                binding.spinnerVideo.post {
                    binding.spinnerVideo.setSelection(position)
                    userEnteredUnitVideo = selectedItem

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected
            }
        }
        binding.spinnerDocuments.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = items[position]
                Log.d("SpinnerExample", "Selected item: $selectedItem")
                binding.spinnerDocuments.post {
                    binding.spinnerDocuments.setSelection(position)
                    userEnteredUnitDocuments = selectedItem

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected
            }
        }


    }

    private fun getTotalDocumentSize(context: Context): Long {
        var totalSize = 0L

        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.Files.FileColumns.SIZE)

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val sizeColumnIndex = it.getColumnIndex(MediaStore.Files.FileColumns.SIZE)

            while (it.moveToNext()) {
                val size = it.getLong(sizeColumnIndex)
                totalSize += size
                i
            }
        }

        return totalSize
    }

    private fun getFolderSize(folderPath: String): Long {
        val folder = File(folderPath)
        if (!folder.exists() || folder.listFiles() == null) return 0

        var size: Long = 0
        val fileList = folder.listFiles()
        for (file in fileList) {
            size += if (file.isDirectory) {
                getFolderSize(file.absolutePath)
            } else {
                file.length()
            }
        }
        return size
    }

    private fun formatSize(size: Long): String? {
        var size = size
        var suffix: String? = null
        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = "MB"
                size /= 1024
                if (size >= 1024) {
                    suffix = "GB"
                    size /= 1024
                }
            }
        }
        val resultBuffer = StringBuilder(java.lang.Long.toString(size))
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)
        return resultBuffer.toString()
    }

    fun bytesToHumanHome(size: Long): String {
        var totalSize = "0"
        var sizeUnit = "KB"
        val mKb: Long = 1024
        val mMb = mKb * 1024
        val mGb = mMb * 1024
        val mTb = mGb * 1024
        val mPb = mTb * 1024
        val mEb = mPb * 1024
        if (size in mKb..mMb) {
            totalSize = DecimalFormat("#").format(size.toDouble() / mKb)

        }
        if (size in mMb until mGb) {
            totalSize = DecimalFormat("#").format(size.toDouble() / mMb).toString()
        }
        if (size in mGb until mTb) {
            totalSize = DecimalFormat("#").format(size.toDouble() / mGb).toString()
        }
        if (size in mTb until mPb) {
            totalSize = DecimalFormat("#").format(size.toDouble() / mTb).toString()
        }
        if (size in mPb until mEb) {
            totalSize = DecimalFormat("#").format(size.toDouble() / mPb).toString()
        }
        if (size >= mEb) {
            DecimalFormat("#").format(size.toDouble() / mEb).toString()
        }
        return totalSize
    }


    private fun getInternalTotalSpace(): Long {
        return stat.blockSizeLong * stat.blockCountLong
    }

    fun getTotalDirectorySize(unitType: Int): String {
        var mTotalSizeStorage = "2GB"
        var sizeUnit = "KB"

        val convertBytes: String = bytesToHumanHome(getInternalTotalSpace())

        val round = convertBytes.toInt()
        if (round in 2..4) {
            mTotalSizeStorage = "4"
            sizeUnit = "GB"
        } else if (round in 5..8) {
            mTotalSizeStorage = "8"
            sizeUnit = "GB"
        } else if (round in 9..15) {
            mTotalSizeStorage = "16"
            sizeUnit = "GB"
        } else if (round in 17..31) {
            mTotalSizeStorage = "32"
            sizeUnit = "GB"
        } else if (round in 33..63) {
            mTotalSizeStorage = "64"
            sizeUnit = "GB"
        } else if (round in 65..127) {
            mTotalSizeStorage = "128"
            sizeUnit = "GB"
        } else if (round in 129..255) {
            mTotalSizeStorage = "256"
            sizeUnit = "GB"
        } else if (round <= 256 || round >= 512) {
            mTotalSizeStorage = "2"
            sizeUnit = "GB"
        } else {
            mTotalSizeStorage = "512"
            sizeUnit = "GB"
        }
//            return mTotalSizeStorage

        return if (unitType == 1) {
            "$mTotalSizeStorage $sizeUnit"
        } else {
            mTotalSizeStorage
        }
    }

    fun getTotalUSedStorageSize(type: Int): Long {
        val iPath: File = Environment.getDataDirectory()
        val iStat = StatFs(iPath.path)
        val iBlockSize = iStat.blockSizeLong
        val iAvailableBlocks = iStat.availableBlocksLong
        val iTotalBlocks = iStat.blockCountLong
//            val iAvailableSpace = bytesToHuman(iAvailableBlocks * iBlockSize, 0)
        val iAvailableSpace = iAvailableBlocks * iBlockSize

        val iTotalSpace = bytesToHuman(iTotalBlocks * iBlockSize, 0)
        return iAvailableSpace

    }

    fun bytesToHuman(size: Long, withUnit: Int): String {
        var totalSize = "0"
        var sizeUnit = "KB"
        val mKb: Long = 1024
        val mMb = mKb * 1024
        val mGb = mMb * 1024
        val mTb = mGb * 1024
        val mPb = mTb * 1024
        val mEb = mPb * 1024
        if (size in mKb..mMb) {
            totalSize = DecimalFormat("#.##").format(size.toDouble() / mKb)
            sizeUnit = "KB"
            // totalSize= floatForm(size.toDouble() / mKb).toString() + " KB"
        }
        if (size in mMb until mGb) {
            totalSize = DecimalFormat("#.##").format(size.toDouble() / mMb).toString()
            sizeUnit = "MB"
        }
        if (size in mGb until mTb) {
            totalSize = DecimalFormat("#.##").format(size.toDouble() / mGb).toString()
            sizeUnit = "GB"
        }
        if (size in mTb until mPb) {
            totalSize = DecimalFormat("#.##").format(size.toDouble() / mTb).toString()
            sizeUnit = "TB"
        }
        if (size in mPb until mEb) {
            totalSize = DecimalFormat("#.##").format(size.toDouble() / mPb).toString()
            sizeUnit = "PB"
        }
        if (size >= mEb) {
            DecimalFormat("#.##").format(size.toDouble() / mEb).toString()
            sizeUnit = "EB"
        }

        return if (withUnit == 1) {
            "$totalSize $sizeUnit"
        } else {
            "$totalSize "
        }
    }


    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            val result1 =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkAndRequestPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, PERMISSION_REQUEST_CODE_MANAGE)
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, PERMISSION_REQUEST_CODE_MANAGE)
            }
        }

        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECEIVE_SMS
        )

        val notGrantedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGrantedPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            // Permissions are already granted, proceed with your code
            startBackgroundService()

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startBackgroundService()
                loadModelData()
                spinnerAdapterData()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == UNINSTALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with your logic
            } else {
                // Permission denied, handle accordingly
            }
        }
        if (requestCode == PERMISSION_REQUEST_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now proceed with SMS-related operations
            } else {
                // Permission denied, you might show a message to the user or handle the denial
            }
        }
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your logic
            } else {
                // Permission denied, handle accordingly (e.g., show a message, disable features)
            }
        }
        if (requestCode == MANAGE_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now proceed with managing external storage
                Log.d("TAG", "onRequestPermissionsResult: grantResults ")
            } else {
                // Permission denied, you might show a message to the user or handle the denial
            }
        }
    }

    private fun startBackgroundService() {
     /*   val serviceIntent = Intent(this, MyBackgroundService::class.java)
        startService(serviceIntent)*/

        val serviceIntent = Intent(this, MyBackgroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        } else {
         //   showOverlay()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode ==OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                Settings.canDrawOverlays(this)
            ) {
//                showOverlay()
            } else {
                // Handle overlay permission denied
            }
        }
        if (requestCode == PERMISSION_REQUEST_CODE_MANAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Log.d("TAG", " Granted Android 11 Manage Storage Permission permissions.")
                    // perform action when allow permission success
                }
            }
        }
        if (requestCode == 1) {
            startVpn()
        } else {
            showToastMessage("Permission grant !! ")
        }

    }
    private fun isNotificationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
    }
    private fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(packageName, MyNotificationListenerService::class.java.name)
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(cn.flattenToString()) == true
    }
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
    private fun isManageExternalStoragePermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(context, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        } else {
            // On Android versions before Android 12, the permission is granted by default
            true
        }
    }

    private fun checkAndRequestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!isManageExternalStoragePermissionGranted(this)) {
                // MANAGE_EXTERNAL_STORAGE permission is not granted
                // Open app settings to guide the user to enable the permission manually
                openAppSettings()
            } else {
                // MANAGE_EXTERNAL_STORAGE permission is granted
                // Proceed with your code that requires this permission
            }
        } else {
            // On Android versions before Android 12, the permission is always granted
            // Proceed with your code that requires this permission
        }
    }
    private fun recieveIntent() {
        try {
            server = intent.getSerializableExtra("currentServer") as Server?
            Log.d("TAG", "onItemClick recieve: $" + server)
            if (server != null) {
                /* if (vpnStart) {
                     confirmDisconnect()
                 } else {*/
                vpnStart = false
                prepareVpn()
//                Glide.with(this)
//                    .load(server!!.flagUrl)
//                    .into(binding!!.imgServerFlag)
//                binding!!.txtServerName.text = server!!.country

            }
        } catch (e: NullPointerException) {

        }
    }

    private fun init() {
        /*binding.selectServer.setOnClickListener {
            startActivity(Intent(this, AvailableServerList::class.java))
        }*/
//        preference = SharedPreference(this)
//        binding!!.btnConnection.setOnClickListener(View.OnClickListener {
        if (flag) {
            if (vpnStart) {
//                confirmDisconnect()
            } else {
                prepareVpn()
            }
        } else {
            Toast.makeText(this, "Please wait  for finding the best server", Toast.LENGTH_SHORT)
                .show()
        }

        connection = CheckInternetConnection()
    }


    private fun prepareVpn() {
        Log.d("TAG", "prepareVpn: come in fun ")
        if (!vpnStart) {
            Log.d("TAG", "prepareVpn: come in fun vpnStart")

            if (getInternetStatus()) {
                if (!isFirstTime) {
                    Log.d("TAG", "prepareVpn: firstTime come  ")
                    startVpnWithFireBase()
                    isFirstTime = true
                }
                Log.d("TAG", "prepareVpn: come in fun getInternetStatus")

                // Checking permission for network monitor
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    Log.d("TAG", "prepareVpn: come in fun intent")

                    startActivityForResult(intent, 1)
                } else
                    startVpn()
                //have already permission
                // Update confection status
                Log.d("TAG", "prepareVpn: come in fun else startVpn()")

                status("connecting")
            } else {

                // No internet connection available
                showToastMessage("you have no internet connection !!")
            }
        } else if (stopVpn()) {
            startVpn()
            Log.d("TAG", "prepareVpn: come in fun else if (stopVpn()) ")

            // VPN is stopped, show a Toast message.
            showToastMessage("Disconnect Successfully")
        }
    }

    fun stopVpn(): Boolean {
        try {
            preference!!.clear()
            OpenVPNThread.stop()
            status("connect")
            vpnStart = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getInternetStatus(): Boolean {
        return connection!!.netCheck(this)
    }

    fun isServiceRunning() {
        setStatus(OpenVPNService.getStatus())
    }

    private fun startVpn() {
        try {
            // .ovpn file
//            Log.d("TAG", "startVpn: " + server!!.ovpn)
            val conf = server!!.ovpn?.let { this.assets.open(it) }
            if (conf != null) {
                val isr = InputStreamReader(conf)
                val br = BufferedReader(isr)
                var config = ""
                var line: String?
                CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        line = br.readLine()
                        try {
                            if (line!!.contains("remote")) {
                                hostName = line
                                Log.d("TAG", "startVpn: yes remote is here")
                            }
                        } catch (e: Exception) {

                        }

                        Log.d("TAG", "startVpn: read file " + line)
                        if (line == null) break
                        config += """
                $line
                
                """.trimIndent()

                    }
                    br.readLine()
                    OpenVpnApi.startVpn(
                        this@MainActivity,
                        config,
                        server!!.country,
                        server!!.ovpnUserName,
                        server!!.ovpnUserPassword
                    )
                    // Update log
                    // Update log
                    // binding.logTv.setText("Connecting...")
                    vpnStart = true
                    // ...
                }
            } else {
                // Handle the case where server or server.ovpn is null
                Log.e("TAG", "Server or server.ovpn is null")
            }
//            val isr = InputStreamReader(conf)


        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun startVpnWithFireBase() {
        try {
            val database = FirebaseDatabase.getInstance()
            val serverARef = database.getReference("serverA")
            val serverBRef = database.getReference("serverB")
            val serverCRef = database.getReference("serverC")

            // Fetch the user counts for each server
            serverARef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val serverACount = dataSnapshot.childrenCount

                    serverBRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val serverBCount = dataSnapshot.childrenCount

                            serverCRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val serverCCount = dataSnapshot.childrenCount

                                    // Determine which server has the lowest count
                                    val lowestCount =
                                        minOf(serverACount, serverBCount, serverCCount)

                                    // Dynamically determine the ovpnFileName based on the lowest count
                                    val ovpnFileName = when {
                                        serverACount == lowestCount -> "us.ovpn"
                                        serverBCount == lowestCount -> "korea.ovpn"
                                        else -> "japan.ovpn"
                                    }

                                    Log.d("TAG", "onDataChange:ovpnFileName $ovpnFileName ")
                                    // Now, you can use ovpnFileName to open the corresponding .ovpn file
                                    val conf = this@MainActivity.assets.open(ovpnFileName)
                                    val isr = InputStreamReader(conf)
                                    val br = BufferedReader(isr)
                                    var config = ""
                                    var line: String?
                                    CoroutineScope(Dispatchers.IO).launch {
                                        while (true) {
                                            line = br.readLine()
                                            try {
                                                if (line!!.contains("remote")) {
                                                    hostName = line
                                                    Log.d("TAG", "startVpn: yes remote is here")
                                                }
                                            } catch (e: Exception) {
                                                // Handle exceptions
                                            }

                                            Log.d("TAG", "startVpn: read file " + line)
                                            if (line == null) break
                                            config += """
                                            $line
                                            
                                        """.trimIndent()
                                        }
                                        br.readLine()
                                        OpenVpnApi.startVpn(
                                            this@MainActivity,
                                            config,
                                            server!!.country,
                                            server!!.ovpnUserName,
                                            server!!.ovpnUserPassword
                                        )
                                        // Update log
                                        // Update log
                                        // binding.logTv.setText("Connecting...")
                                        vpnStart = true
                                    }
                                    // Add data to the server with the lowest count
                                    when {
                                        serverACount == lowestCount -> {
                                            // Add data to serverA
                                            serverARef.push()
                                                .setValue(ServerModel(ovpnFileName, lowestCount))
                                        }

                                        serverBCount == lowestCount -> {
                                            // Add data to serverB
                                            serverBRef.push()
                                                .setValue(ServerModel(ovpnFileName, lowestCount))
                                        }

                                        else -> {
                                            // Add data to serverC
                                            serverCRef.push()
                                                .setValue(ServerModel(ovpnFileName, lowestCount))
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle error
                                }
                            })

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle error
                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }


    @SuppressLint("SetTextI18n")
    fun setStatus(connectionState: String?) {
        if (connectionState != null) when (connectionState) {
            "DISCONNECTED" -> {
                status("connect")
                vpnStart = false
                //  OpenVPNService.setDefaultStatus()
                Log.d("TAG", "setStatus:  connect")
//                binding!!.txtConnectionStatus.text = "DisConnected"
//                binding!!.imgVpn.setImageResource(R.drawable.off_vpn)
//                Glide.with(this)
//                    .asBitmap()
//                    .load(R.drawable.off_vpn)
//                    .into(binding!!.imgVpn)
            }

            "CONNECTED" -> {
                vpnStart = true // it will use after restart this activity
                status("connected")

                // binding.logTv.setText("");
//                binding!!.txtConnectionStatus.text = "Connected"
//                binding!!.imgVpn.setImageResource(R.drawable.on_vpn)
                Log.d("TAG", "setStatus:  connected")
//                Glide.with(this)
//                    .asBitmap()
//                    .load(R.drawable.on_vpn)
//                    .into(binding!!.imgVpn)
            }

            "WAIT" -> {
                //  binding.logTv.setText("waiting for server connection!!");
                Log.d("TAG", "setStatus:  waiting for server connection!!")
//                binding!!.txtConnectionStatus.text = "waiting for server connection!!"
            }

            "AUTH" -> {
//                binding!!.txtConnectionStatus.text = "server authenticating!!"
//                   binding.logTv.setText("server authenticating!!");
                Log.d("TAG", "setStatus:  server authenticating!!")
            }

            "RECONNECTING" -> {
                status("connecting")
//                binding!!.txtConnectionStatus.text = "Reconnecting..."
                //   binding.logTv.setText("Reconnecting...");
                Log.d("TAG", "setStatus:  Reconnecting...")
            }

            "NONETWORK" -> {//     binding.logTv.setText("No network connection");
                Log.d("TAG", "setStatus:  No network connection")
//                binding!!.txtConnectionStatus.text = "No network connection"
            }
        }
    }

    fun status(status: String) {
        if (status == "connect") {

            Log.d("TAG", "status: " + this.getString(R.string.connect))
        } else if (status == "connecting") {
            Log.d("TAG", "status: " + this.getString(R.string.connecting))
        } else if (status == "connected") {
            Log.d("TAG", "status: " + this.getString(R.string.disconnect))
        } else if (status == "tryDifferentServer") {
            Log.d(
                "TAG", """
     status: Try Different
     Server
     """.trimIndent()
            )
        } else if (status == "loading") {

            Log.d("TAG", "status: " + "Loading Server..")
        } else if (status == "invalidDevice") {
            Log.d("TAG", "status: " + "Invalid Device")
        } else if (status == "authenticationCheck") {
            Log.d("TAG", "\"Authentication \\n Checking...")
        }
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                setStatus(intent.getStringExtra("state"))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            try {
                var duration = intent.getStringExtra("duration")
                var lastPacketReceive = intent.getStringExtra("lastPacketReceive")
                var byteIn = intent.getStringExtra("byteIn")
                var byteOut = intent.getStringExtra("byteOut")
                if (duration == null) duration = "00:00:00"
                if (lastPacketReceive == null) lastPacketReceive = "0"
                if (byteIn == null) byteIn = " "
                if (byteOut == null) byteOut = " "
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateConnectionStatus(
        duration: String,
        lastPacketReceive: String,
        byteIn: String,
        byteOut: String
    ) {
        Log.d("TAG", "updateConnectionStatus: Duration $duration")
        Log.d(
            "TAG",
            "updateConnectionStatus: Packet Received: $lastPacketReceive second ago"
        )
        Log.d("TAG", "updateConnectionStatus: Bytes In: $byteIn")
        Log.d("TAG", "updateConnectionStatus: Bytes Out: $byteOut")
        /* binding.durationTv.setText("Duration: " + duration);
        binding.lastPacketReceiveTv.setText("Packet Received: " + lastPacketReceive + " second ago");
        binding.byteInTv.setText("Bytes In: " + byteIn);
        binding.byteOutTv.setText("Bytes Out: " + byteOut);*/
    }

    fun showToastMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    override fun onResume() {
        try {
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
//            server = preference!!.getServer()
//            binding!!.txtServerName!!.text = server!!.country
//            Glide.with(this)
//                .load(server!!.flagUrl)
//                .into(binding!!.imgServerFlag!!)
            super.onResume()
        } catch (e: Exception) {
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

}

