package io.flutter.plugins.camera;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;

import java.util.HashMap;
import java.util.Map;

class DartMessenger {
    @Nullable
    private EventChannel.EventSink eventSink;
    private Handler handler;

    enum EventType {
        ERROR,
        CAMERA_CLOSING,
    }

    DartMessenger(BinaryMessenger messenger, long eventChannelId) {
        new EventChannel(messenger, "flutter.io/cameraPlugin/cameraEvents" + eventChannelId)
                .setStreamHandler(
                        new EventChannel.StreamHandler() {
                            @Override
                            public void onListen(Object arguments, EventChannel.EventSink sink) {
                                eventSink = sink;
                                handler = new Handler(Looper.getMainLooper());
                            }

                            @Override
                            public void onCancel(Object arguments) {
                                eventSink = null;
                                handler.removeCallbacksAndMessages(null);
                                handler = null;
                            }
                        });
    }

    void sendCameraClosingEvent() {
        send(EventType.CAMERA_CLOSING, null);
    }

    void send(EventType eventType, @Nullable String description) {
        if (eventSink == null || handler == null) {
            return;
        }

        Map<String, String> event = new HashMap<>();
        event.put("eventType", eventType.toString().toLowerCase());
        // Only errors have a description.
        if (eventType == EventType.ERROR && !TextUtils.isEmpty(description)) {
            event.put("errorDescription", description);
        }

        handler.post(() -> eventSink.success(event));
    }
}