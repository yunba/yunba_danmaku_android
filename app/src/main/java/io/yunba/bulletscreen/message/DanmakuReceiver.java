package io.yunba.bulletscreen.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.yunba.android.manager.YunBaManager;
import io.yunba.bulletscreen.Constant;

/**
 * Created by miao on 2016/12/21.
 */

public class DanmakuReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (YunBaManager.MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {

            String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
            String msg = intent.getStringExtra(YunBaManager.MQTT_MSG);


            StringBuilder showMsg = new StringBuilder();
            showMsg.append("Received message from server: ").append(YunBaManager.MQTT_TOPIC)
                    .append(" = ").append(topic).append(" ")
                    .append(YunBaManager.MQTT_MSG).append(" = ").append(msg);
            Intent intent2 = new Intent();
            intent2.putExtra(Constant.PARAMS_MESSAGE_BODY, msg);
            intent2.setAction(Constant.MESSAGE_RECEIVED_ACTION);
            processCustomMessage(context, intent2);
            // send msg to app

        } else if (YunBaManager.PRESENCE_RECEIVED_ACTION.equals(intent.getAction())) {
            //msg from presence.
            String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
            String payload = intent.getStringExtra(YunBaManager.MQTT_MSG);

            try {
                JSONObject res = new JSONObject(payload);
                String action = res.optString("action", null);
                String alias = res.optString("alias", null);
                Intent presenceIntent = new Intent();
                presenceIntent.putExtra(Constant.PARAMS_PRESENCE_ACTION, action);
                presenceIntent.putExtra(Constant.PARAMS_PRESENCE_ALIAS, alias);
                presenceIntent.putExtra(Constant.PARAMS_PRESENCE_TOPIC, topic);
                presenceIntent.setAction(Constant.PRESENCE_RECIVED_ACTION);
                processCustomMessage(context, presenceIntent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            StringBuilder showMsg = new StringBuilder();
            showMsg.append("Received message presence: ").append(YunBaManager.MQTT_TOPIC)
                    .append(" = ").append(topic).append(" ")
                    .append(YunBaManager.MQTT_MSG).append(" = ").append(payload);
            Log.d("DemoReceiver", showMsg.toString());

        }
    }

    private void processCustomMessage(Context context, Intent intent) {
//        intent.setAction(MainActivity.MESSAGE_RECEIVED_ACTION);
//        intent.addCategory(context.getPackageName());
        context.sendBroadcast(intent);
    }

}
