package com.example.kotlintest.drawoverlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.kotlintest.R
import java.util.*

interface OverlayDismissListener {
    fun onOverlayDismissed()
}
interface OverlayDeleteListener {
    fun onOverlayDelete()
}interface OverlaySuspesiousLinkListener {
    fun onOverlayLink()
}
class OverlayDialog() {
    companion object {
        private var isOverlayShown = false
        fun showOverlayDialog(context: Context, dismissListener: OverlayDismissListener) {
            if (isOverlayShown) {
                // Overlay is already shown, no need to show it again
                return
            }
            isOverlayShown = true
            val overlayParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val overlayView = inflater.inflate(R.layout.overlay_dialog, null) as CardView

            val marginStartEnd = context.resources.getDimensionPixelSize(R.dimen.overlay_margin_start_end)

            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginStart = marginStartEnd
            layoutParams.marginEnd = marginStartEnd
            overlayView.layoutParams = layoutParams

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.addView(overlayView, overlayParams)

            val closeButton = overlayView.findViewById<ImageView>(R.id.imageViewClose)
            closeButton.setOnClickListener {
                isOverlayShown = false
                windowManager.removeView(overlayView)
                dismissListener.onOverlayDismissed()
            }
        }
        private var isDeleteDialogShown = false
        fun showOverlayDeleteDialog(context: Context, deleteListener: OverlayDeleteListener) {
            if (isDeleteDialogShown) {

                return
            }
            isDeleteDialogShown = true
            isOverlayShown=true
            val overlayParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val overlayView = inflater.inflate(R.layout.overlay_delete_dialog, null) as CardView

            val marginStartEnd = context.resources.getDimensionPixelSize(R.dimen.overlay_margin_start_end)

            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginStart = marginStartEnd
            layoutParams.marginEnd = marginStartEnd
            overlayView.layoutParams = layoutParams

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.addView(overlayView, overlayParams)

            val closeButton = overlayView.findViewById<ImageView>(R.id.imageViewCloseDelete)
            closeButton.setOnClickListener {
                isDeleteDialogShown = false
                isOverlayShown = false
                windowManager.removeView(overlayView)
            }
            val DeleteButton = overlayView.findViewById<CardView>(R.id.cardDelte)
            DeleteButton.setOnClickListener {
                isDeleteDialogShown = false
                isOverlayShown = false
                windowManager.removeView(overlayView)
                deleteListener.onOverlayDelete()
            }
        }
        private var isSuspesiousLinkDialogShown = false
        fun showOverlaySuspesiousLinkDialog(context: Context,sender: String, link: String) {
            if (isSuspesiousLinkDialogShown) {

                return
            }
            isSuspesiousLinkDialogShown = true
            isOverlayShown=true
            val overlayParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val overlayView = inflater.inflate(R.layout.overlay_suspecious_link_dialog, null) as CardView

            val marginStartEnd = context.resources.getDimensionPixelSize(R.dimen.overlay_margin_start_end)

            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginStart = marginStartEnd
            layoutParams.marginEnd = marginStartEnd
            overlayView.layoutParams = layoutParams

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.addView(overlayView, overlayParams)

            val closeButton = overlayView.findViewById<ImageView>(R.id.imageViewCloseLink)
            closeButton.setOnClickListener {
                isSuspesiousLinkDialogShown = false
                isOverlayShown = false
                windowManager.removeView(overlayView)
            }
            val etLink = overlayView.findViewById<TextView>(R.id.etLink)
            etLink.setText(link)
        }
        private var isSuspesiousNotificationLinkDialogShown = false
        fun showOverlaySuspesiousNotificationLinkDialog(context: Context, link: List<String>) {
            if (isSuspesiousNotificationLinkDialogShown) {

                return
            }
            isSuspesiousNotificationLinkDialogShown = true
            isOverlayShown=true
            val overlayParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val overlayView = inflater.inflate(R.layout.overlay_suspecious_notification_link_dialog, null) as CardView

            val marginStartEnd = context.resources.getDimensionPixelSize(R.dimen.overlay_margin_start_end)

            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginStart = marginStartEnd
            layoutParams.marginEnd = marginStartEnd
            overlayView.layoutParams = layoutParams

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.addView(overlayView, overlayParams)

            val closeButton = overlayView.findViewById<ImageView>(R.id.imageViewCloseNotificationLink)
            closeButton.setOnClickListener {
                isSuspesiousNotificationLinkDialogShown = false
                isOverlayShown = false
                windowManager.removeView(overlayView)
            }
            val etNotificationLink = overlayView.findViewById<TextView>(R.id.etNotificationLink)
            etNotificationLink.setText(link.joinToString("\n"))
        }
    }
}
