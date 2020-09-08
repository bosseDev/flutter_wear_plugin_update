package com.mjohnsullivan.flutterwear.wear

import android.os.Bundle
import androidx.core.view.ViewCompat.requestApplyInsets
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

private const val CHANNEL_NAME = "wear"

fun getChannel(messenger: BinaryMessenger?): MethodChannel {
    return MethodChannel(messenger, CHANNEL_NAME)
}

class WearPlugin : FlutterPlugin, ActivityAware, MethodCallHandler {

    private var mActivityBinding: ActivityPluginBinding? = null
    private var mMethodChannel: MethodChannel? = null
    private var mAmbientController: AmbientModeSupport.AmbientController? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        mMethodChannel = MethodChannel(binding.binaryMessenger, CHANNEL_NAME)
        mMethodChannel?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        mMethodChannel?.setMethodCallHandler(this)
        mMethodChannel = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        assert(mMethodChannel != null)
        mActivityBinding = binding
        mAmbientController = AmbientModeSupport.attach(binding.activity as FragmentActivity)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        assert(mMethodChannel != null)
        mActivityBinding = binding
        mAmbientController = AmbientModeSupport.attach(binding.activity as FragmentActivity)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        mMethodChannel?.setMethodCallHandler(null)
        mActivityBinding = null
    }

    override fun onDetachedFromActivity() {
        mMethodChannel?.setMethodCallHandler(null)
        mActivityBinding = null
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getShape" -> handleShapeMethodCall(result)
            else -> result.notImplemented()
        }
    }

    private fun handleShapeMethodCall(result: Result) {
        val view = mActivityBinding?.activity?.window?.decorView
        if (view == null) {
            result.error("No View", null, null)
            return
        }
        setOnApplyWindowInsetsListener(view) { _, insets: WindowInsetsCompat? ->
            if (insets?.isRound == true) {
                result.success(0)
            } else {
                result.success(1)
            }
            WindowInsetsCompat(insets)
        }
        requestApplyInsets(view)
    }
}

/*
 * Pass ambient callback back to Flutter
 */
class FlutterAmbientCallback(private val channel: MethodChannel) : AmbientModeSupport.AmbientCallback() {

    override fun onEnterAmbient(ambientDetails: Bundle) {
        channel.invokeMethod("enter", null)
        super.onEnterAmbient(ambientDetails)
    }

    override fun onExitAmbient() {
        channel.invokeMethod("exit", null)
        super.onExitAmbient()
    }

    override fun onUpdateAmbient() {
        channel.invokeMethod("update", null)
        super.onUpdateAmbient()
    }
}
