package exoaartest.research.josht.exoplayerlibrarytest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import 	android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class TCPClientService extends Service implements TCPClient.CallbackListener{

    public static final String TAG = TCPClientService.class.getSimpleName();
    private IBinder mBinder = new MyBinder();
    TCPClient tcpClientThread;

    public TCPClientService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("JOSH","In onBind");
        Bundle extras = intent.getExtras();
        String serverIp = extras.getString("serverIp");
        int serverPort  = extras.getInt("serverPort");
        String mpdUrls = extras.getString("mpdUrls");
        runClient(serverIp, serverPort, mpdUrls);
        Log.d(TAG, "Starting service");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopClient();
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                tcpClientThread.sendMessage(message);
            }
        }).start();
    }

    public void stopClient(){
        tcpClientThread.stopClient();
    }


    public void runClient(String serverIp, int serverPort, String mpdUrls) {
        tcpClientThread = new TCPClient(serverIp, serverPort, mpdUrls, this);
        tcpClientThread.start();
    }

    private void sendMessageToActivity(String serverMessage) {
        Intent intent = new Intent("tcp-message");
        intent.putExtra("message", serverMessage);
        getApplicationContext().sendBroadcast(intent);
    }

    @Override
    public void onCommandReceived(String command) {
        sendMessageToActivity(command);
    }

    public class MyBinder extends Binder {
        public TCPClientService getService() {
            return TCPClientService.this;
        }
    }
}
