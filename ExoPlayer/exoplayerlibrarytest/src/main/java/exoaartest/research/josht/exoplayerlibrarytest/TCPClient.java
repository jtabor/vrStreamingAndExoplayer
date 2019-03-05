package exoaartest.research.josht.exoplayerlibrarytest;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Establishes a connection with the TCP Server on the router
 */

public class TCPClient extends Thread {
    public static final String TAG = TCPClient.class.getSimpleName();

    private String serverMessage;
    private final String SERVER_IP;
    private final int SERVER_PORT;
    private final String MPD_FILE_URLS;
    private CallbackListener mMessageListener = null;
    private boolean mRun = false;

    private PrintWriter out;

    public TCPClient(
            String serverIp,
            int serverPort,
            String mpdFileUrls,
            CallbackListener listener) {
        SERVER_IP = serverIp;
        SERVER_PORT = serverPort;
        MPD_FILE_URLS = mpdFileUrls;
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
            Log.d(TAG, "Message sent: " + message);
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.d("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);

            try {

                //send the message to the server
                out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        socket.getOutputStream())),
                        true);

                if (MPD_FILE_URLS != null) {
                        sendMessage("download~" + MPD_FILE_URLS);
                    }

                Log.d(TAG, "All Urls Sent.");

                //receive the message which the server sends back
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                Log.d(TAG, "Run: " + mRun);
                while (mRun) {
                    serverMessage = in.readLine();
                    Log.d(TAG, "Message from server: " + serverMessage);
                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.onCommandReceived(serverMessage);
                        Log.d(TAG,"RESPONSE FROM SERVER: '" + serverMessage + "'");
                    }
                    serverMessage = null;
                }
            } catch (Exception e) {

                Log.e(TAG, "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

//    private void parseMessageReceived(String message) {
//        if (message != null && !message.isEmpty()) {
//            if (message.equals(TCPServerCommands.VIDEO_READY)) {
//                    mMessageListener.onVideoReadyForPlayback();
//            }
//            else if (message.equals(TCPServerCommands.BUFFER_WARNING)) {
//                mMessageListener.onTilesUnavailable();
//            } else if (message.contains(TCPServerCommands.DOWNLOAD_NOTIFICATION)) {
//                parseDownloadedMessage(serverMessage);
//            }
//        }
//    }
//
//    private void parseDownloadedMessage(String serverMessage) {
//        String[] messages = serverMessage.split(":");
//        if (messages.length == 2) {
//            String[] info = messages[1].split(",");
//            if (info.length == 2) {
//                int tileId = Integer.parseInt(info[0]);
//                int chunkId = Integer.parseInt(info[1]);
//            }
//        }
//    }

    public interface CallbackListener {
        void onCommandReceived(String command);

    }

}
