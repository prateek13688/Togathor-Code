package com.uf.togathor.utils.appservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by alok on 1/28/15.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // use this to start and trigger a service

        /*if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            Intent i = new Intent(context, CheckInService.class);
            i.putExtra("inapp", false);
            context.startService(i);
        }
        else    {
            Intent i = new Intent(context, CheckInServiceRetro.class);
            i.putExtra("inapp", false);
            context.startService(i);
        }*/

        Intent eventServiceIntent = new Intent(context, EventService.class);
        context.startService(eventServiceIntent);

    }

}
