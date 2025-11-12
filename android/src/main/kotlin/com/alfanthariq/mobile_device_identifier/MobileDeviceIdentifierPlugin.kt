package com.alfanthariq.mobile_device_identifier

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.security.MessageDigest
import java.math.BigInteger

import android.media.MediaDrm
import java.util.UUID

class MobileDeviceIdentifierPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "mobile_device_identifier")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getDeviceId") {
            val devId = getDeviceId()
            result.success(devId)
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun getDeviceId(): String? {
        val wideVineUuid = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
        val wvDrm = MediaDrm(wideVineUuid)
        return try {
            val wideVineId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
            generateMD5(wideVineId)
        } catch (e: java.lang.Exception) {
            null
        } finally {
            wvDrm.close()  // ✅ 确保释放资源
        }
    }

    private fun generateMD5(input: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(input)

        // Convert byte array to signum representation
        val no = BigInteger(1, messageDigest)

        // Convert message digest to hex value
        var hashText = no.toString(16)

        // Add preceding 0s to pad the hash to make it 32-bit
        while (hashText.length < 32) {
            hashText = "0$hashText"
        }
        return hashText
    }
}
