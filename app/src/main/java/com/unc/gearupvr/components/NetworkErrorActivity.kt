package com.unc.gearupvr.components

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toDrawable
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.withAlpha


class NetworkErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = RelativeLayout(this)
        val linLayoutParam =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        rootLayout.background = Color.BLACK.withAlpha(0x4D).toDrawable()
        setContentView(rootLayout, linLayoutParam)
        val cardView = CardView(this)
        val cardViewParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cardViewParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        cardViewParams.marginEnd = 30
        cardViewParams.marginStart = 30
        cardView.layoutParams = cardViewParams
        cardView.radius = 15f
        cardView.cardElevation = 30f
        cardView.setCardBackgroundColor(Color.WHITE)
        cardView.layoutParams = cardViewParams
        val parentLayout = RelativeLayout(this)
        val parentLayoutView =
            RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        parentLayout.layoutParams = parentLayoutView
        parentLayoutView.addRule(RelativeLayout.CENTER_IN_PARENT)
        parentLayoutView.marginEnd = 25
        parentLayoutView.marginStart = 25
        parentLayout.background = Color.WHITE.toDrawable()
        cardView.addView(parentLayout)
        rootLayout.addView(cardView)
        val txtViewParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        txtViewParams.leftMargin = 20
        txtViewParams.rightMargin = 20
        txtViewParams.bottomMargin = 50
        txtViewParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        txtViewParams.addRule(RelativeLayout.ALIGN_PARENT_START)

        val errorMsg = TextView(this)
        errorMsg.text = "Unable to connect to internet. Please check your network settings."
        errorMsg.textColor = Color.BLACK
        errorMsg.id = View.generateViewId()
        parentLayout.addView(errorMsg, txtViewParams)


        val okButtonParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        okButtonParams.addRule(RelativeLayout.ALIGN_PARENT_END)
        okButtonParams.addRule(RelativeLayout.BELOW, errorMsg.id)
        okButtonParams.topMargin = 25
        val okButton = Button(this)
        okButton.text = "Ok"
        parentLayout.addView(okButton, okButtonParams)
        okButton.onClick { onBackPressed() }
    }

    companion object {
        fun createIntent(context: Context?): Intent {
            val intent = Intent(context, NetworkErrorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }
}
