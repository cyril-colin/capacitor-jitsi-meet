package com.capacitor.jitsi.plugin;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import android.content.IntentFilter;
import android.util.Log;
import android.content.Intent;
import android.Manifest;

@NativePlugin(
    permissions={
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
      }
  )
public class Jitsi extends Plugin {
    private static final String TAG = "CapacitorJitsiMeet";

    @PluginMethod()
    public void joinConference(PluginCall call) {
        String url = call.getString("url");
        String roomName = call.getString("roomName");
        String jwt = call.getString("jwt");
        Boolean startWithAudioMuted = call.getBoolean("startWithAudioMuted");
        Boolean startWithVideoMuted = call.getBoolean("startWithVideoMuted");
        Boolean chatEnabled = call.getBoolean("chatEnabled");
        Boolean inviteEnabled = call.getBoolean("inviteEnabled");

        JitsiBroadcastReceiver receiver = new JitsiBroadcastReceiver();
        receiver.setModule(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("onConferenceWillJoin");
        filter.addAction("onConferenceJoined");
        filter.addAction("onConferenceLeft"); // intentionally uses the obsolete onConferenceLeft in order to be consistent with iOS deployment and broadcast to JS listeners
        getContext().registerReceiver(receiver, filter);

        if(url == null) {
            call.reject("Must provide an url");
            return;
        }
        if(roomName == null) {
            call.reject("Must provide an conference room name");
            return;
        }
        if(startWithAudioMuted == null) {
            startWithAudioMuted = false;
        }
        if(startWithVideoMuted == null) {
            startWithVideoMuted = false;
        }
        if(chatEnabled == null) {
            chatEnabled = true;
        }
        if(inviteEnabled == null) {
            chatEnabled = true;
        }
        Log.v(TAG, "display url: " + url);

        Intent intent = new Intent(getActivity(), JitsiActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("jwt", jwt);
        intent.putExtra("roomName",roomName);
        intent.putExtra("startWithAudioMuted", startWithAudioMuted);
        intent.putExtra("startWithVideoMuted", startWithVideoMuted);
        intent.putExtra("chatEnabled", chatEnabled);
        intent.putExtra("inviteEnabled", inviteEnabled);

        getActivity().startActivity(intent);

        JSObject ret = new JSObject();
        ret.put("success", true);
        call.success(ret);
    }

    public void onEventReceived(String eventName) {
        bridge.triggerWindowJSEvent(eventName);
    }
}
