package com.visioncoders.i220795_i210611

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

class VideoCallActivity : AppCompatActivity() {

    private var rtcEngine: RtcEngine? = null
    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)

    private val appId = "a17f58f10a7d4a85940ca6f623a80aff"
    private var channelName: String? = null
    private var token: String? = null
    private var isVideoCall: Boolean = true

    private lateinit var localVideoContainer: FrameLayout
    private lateinit var remoteVideoContainer: FrameLayout

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread { Toast.makeText(applicationContext, "Joined Channel: $channel", Toast.LENGTH_SHORT).show() }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            if (isVideoCall) {
                runOnUiThread {
                    setupRemoteVideo(uid)
                    localVideoContainer.removeAllViews()

                    setupLocalVideo()

                    localVideoContainer.bringToFront()
                    findViewById<android.widget.Button>(R.id.end_call_button).bringToFront()
                }
            }

        }

        override fun onUserOffline(uid: Int, reason: Int) {
            if (isVideoCall) {
                runOnUiThread { onRemoteUserLeft() }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        channelName = intent.getStringExtra("CHANNEL_ID")
        isVideoCall = intent.getBooleanExtra("IS_VIDEO_CALL", true)

        if (channelName == null) {
            finish()
            return
        }

        localVideoContainer = findViewById(R.id.local_video_view_container)
        remoteVideoContainer = findViewById(R.id.remote_video_view_container)

        if (!isVideoCall) {
            localVideoContainer.visibility = View.GONE
            remoteVideoContainer.visibility = View.GONE
        }

        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        } else {
            initializeAndJoinChannel()
        }

        findViewById<android.widget.Button>(R.id.end_call_button).setOnClickListener { finish() }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED
    }

    private fun initializeAndJoinChannel() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            rtcEngine = RtcEngine.create(config)
        } catch (e: Exception) {
            throw RuntimeException("Check the error: ${e.message}")
        }

        if (isVideoCall) {
            rtcEngine?.enableVideo()
            setupLocalVideo()
            localVideoContainer.bringToFront()
            rtcEngine?.startPreview()
        } else {
            rtcEngine?.enableAudio()
        }

        val options = io.agora.rtc2.ChannelMediaOptions()
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        rtcEngine?.joinChannel(token, channelName, 0, options)
    }

    private fun setupLocalVideo() {
        val surfaceView = SurfaceView(this)

        surfaceView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        localVideoContainer.addView(surfaceView)
        rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun setupRemoteVideo(uid: Int) {
        val surfaceView = SurfaceView(baseContext)
        remoteVideoContainer.addView(surfaceView)
        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    private fun onRemoteUserLeft() {
        remoteVideoContainer.removeAllViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (channelName != null) {
            FirebaseDatabase.getInstance().getReference("calls").child(channelName!!).removeValue()
        }

        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeAndJoinChannel()
            } else {
                Toast.makeText(this, "Camera & Microphone permissions are required for calls.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}