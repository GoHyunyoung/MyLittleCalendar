package pl.polidea.asl.termproject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by admin on 2015-12-10.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //GCMIntentServise 시작
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        //서비스 시작. 어플이 강제종료되지 않는한 항상 깨워있도록 유지
        startWakefulService(context, (intent.setComponent(comp)));

        setResultCode(Activity.RESULT_OK);
    }
}
