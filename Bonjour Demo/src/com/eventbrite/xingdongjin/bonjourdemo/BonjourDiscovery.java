package com.eventbrite.xingdongjin.bonjourdemo;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class BonjourDiscovery extends Activity {
   private static final String SERVICE_TYPE = "_http._tcp.local.";
   // private static final String SERVICE_TYPE = "_http._tcp";

   private MulticastLock lock;
   private Handler handler = new Handler();

   private JmDNS jmdns;
   private ServiceListener listener;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      handler.postDelayed(new Runnable() {
         @Override
         public void run() {
            startBonjour();
         }
      }, 1000);

   }

   private void startBonjour() {
      WifiManager wifi = (WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
      lock = wifi.createMulticastLock("lock");
      lock.setReferenceCounted(true);
      lock.acquire();
      try {
         jmdns = JmDNS.create();
         jmdns.addServiceListener(SERVICE_TYPE,
               listener = new ServiceListener() {
            @Override
            public void serviceResolved(ServiceEvent ev) {
               listService("\nService resolved: "
                     + ev.getInfo().getQualifiedName()
                     + "\nip: "
                     + ev.getInfo().getInet4Addresses()[0]
                           .getHostAddress().toString() + "\nport: "
                           + ev.getInfo().getPort());

            }

            @Override
            public void serviceRemoved(ServiceEvent ev) {
               listService("\nService removed: " + ev.getName());
            }

            @Override
            public void serviceAdded(ServiceEvent event) {
               jmdns.requestServiceInfo(event.getType(), event.getName(),
                     1);
            }
         });

         // serviceInfo = ServiceInfo.create("_gatekeeper._tcp.local.",
         // "Gatekeeper Service", 0, "Gatekeeper service from android");
         // jmdns.registerService(serviceInfo);
      } catch (IOException e) {
         e.printStackTrace();
         return;
      }
   }

   private void listService(final String msg) {
      handler.postDelayed(new Runnable() {
         @Override
         public void run() {
            TextView t = (TextView) findViewById(R.id.textview_main_item);
            t.setText(msg + "\n============== " + t.getText());
         }
      }, 10);
   }

   @Override
   protected void onStop() {
      if (jmdns != null) {
         if (listener != null) {
            jmdns.removeServiceListener(SERVICE_TYPE, listener);
            listener = null;
         }
         jmdns.unregisterAllServices();
         try {
            jmdns.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
         jmdns = null;
      }

      lock.release();
      super.onStop();
   }
}