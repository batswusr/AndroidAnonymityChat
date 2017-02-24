package com.batsw.anonimitychat.tor.connections;

import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import com.batsw.anonimitychat.chat.ChatActivity;
import com.batsw.anonimitychat.chat.constants.ChatModelConstants;
import com.batsw.anonimitychat.chat.message.ChatMessage;
import com.batsw.anonimitychat.chat.message.ChatMessageType;
import com.batsw.anonimitychat.chat.message.MessageReceivedListenerManager;
import com.batsw.anonimitychat.tor.bundle.TorConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import socks.Socks5Proxy;
import socks.SocksSocket;

/**
 * Created by tudor on 12/16/2016.
 */

public class TorPublisher implements ITorConnection, Serializable {

    private static final String LOG = TorPublisher.class.getSimpleName();

    public String mDestinationAddress = "";
    public MessageReceivedListenerManager mMessageReceivedListenerManager;
    private long mSessionId;

    private Socket mSocketConnection;
    private Thread mMessageReceivingThread = null;

    private OutputStream mOutputStream;
    private DataOutputStream mDataOutputStream;
    private DataInputStream mDataInputStream;

    boolean mIsConnected = false;

    public TorPublisher(MessageReceivedListenerManager messageReceivedListenerManager, String partnerHostName, long sessionId) {
        mMessageReceivedListenerManager = messageReceivedListenerManager;
        mDestinationAddress = partnerHostName;
        mSessionId = sessionId;
    }

    @Override
    public void createConnection() {
        Log.i(LOG, "getConnection -> ENTER");

        mIsConnected = establishConnectionToPartner();
        if (mIsConnected == true) {
            startMessageReceivingThread();
        }

        Log.i(LOG, "getConnection -> LEAVE");
    }

    @Override
    public void sendMessage(String message) {
        Log.i(LOG, "sendMessage -> ENTER message=" + message);

        String preparedMessage = message + ChatModelConstants.MESSAGE_EOL;

        try {
            mDataOutputStream.writeUTF(preparedMessage);
            mDataOutputStream.flush();
        } catch (IOException ioException) {
            Log.e(LOG, "error when sending message: " + ioException.getMessage(), ioException);
        }

        Log.i(LOG, "sendMessage -> LEAVE");
    }

    @Override
    public void closeConnection() {
        Log.i(LOG, "closeConnection -> ENTER");

        Log.i(LOG, "Not implemented yet!");

        Log.i(LOG, "closeConnection -> LEAVE");
    }

    @Override
    public boolean getIsAlive() {
        return mIsConnected;
    }

    @Override
    public Thread getMessageReceivingThread() {
        return mMessageReceivingThread;
    }

    private boolean establishConnectionToPartner() {
        Log.i(LOG, "establishConnectionToPartner -> ENTER");
        boolean retVal = false;
        try {
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            Socks5Proxy socks5Proxy = new Socks5Proxy("127.0.0.1", TorConstants.TOR_BUNDLE_INTERNAL_SOCKS_PORT);
            socks5Proxy.resolveAddrLocally(false);
            mSocketConnection = new SocksSocket(socks5Proxy, mDestinationAddress, TorConstants.TOR_BUNDLE_EXTERNAL_PORT);
            Log.i(LOG, "Connected to target address");

            mOutputStream = mSocketConnection.getOutputStream();
            mDataOutputStream = new DataOutputStream(mOutputStream);

            mDataInputStream = new DataInputStream(mSocketConnection.getInputStream());

        } catch (UnknownHostException unknownHost) {
            Log.e(LOG, "You are trying to connect to an unknown host! " + unknownHost.getStackTrace().toString(), unknownHost);

            retVal = false;

        } catch (IOException ioException) {
            Log.e(LOG, "error: " + ioException.getMessage(), ioException);

            retVal = false;
        }

        if ((mDataInputStream != null) && (mDataOutputStream != null)) {
            retVal = true;
        }

        Log.i(LOG, "establishConnectionToPartner -> LEAVE retVal=" + retVal);
        return retVal;
    }

    private void startMessageReceivingThread() {
        mMessageReceivingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                createMessageReceivingLoop();
            }
        });
        mMessageReceivingThread.start();
    }

    /**
     *
     */
    private void closeCommunication() {
        Log.i(LOG, "closeCommunication -> ENTER");

//        if (mMessageReceiverThread.isAlive()) mMessageReceiverThread.stop();

        try {
            if (mDataInputStream != null) mDataInputStream.close();
            if (mDataOutputStream != null) mDataOutputStream.close();
            if (mOutputStream != null) mOutputStream.close();
            if (mSocketConnection != null) mSocketConnection.close();

        } catch (IOException e) {
            Log.e(LOG, "error when closing the communication" + e.getMessage(), e);
        }
        Log.i(LOG, "closeCommunication -> LEAVE");
    }

    /**
     * This is the actual message receiver thread
     */
    //TODO: I need to implement a "finished" message to break the infinite loop.
    //this would happen when the USER has finished chatting and he wants to exit this activity
    // DO i need it? I will shut down the thread when closing the activity ....
    private void createMessageReceivingLoop() {
        String incomingMessage = "";
        while (true) {
            try {
                if (mDataInputStream != null) {
                    incomingMessage = mDataInputStream.readUTF();
                    Log.i(LOG, "Message Receved___" + incomingMessage);

                    mMessageReceivedListenerManager.messageReceived(incomingMessage, mSessionId);

//                    mChatActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mChatActivity.addPartnerMessageToMessageList(receivedChatMessage);
//                        }
//                    });

                    // notify that a message came
                    /// i need a signature of the message to print it on the right chat .....

//                    if (incomingMessage.equals("something...."))
//                        break;
                }
            } catch (IOException e) {
                //TODO : I don't think the connection should be closed . Only when exiting the Activity
                Log.e(LOG, "error when receiving a message" + e.getMessage(), e);
                try {
                    mSocketConnection.close();
                    break;
                } catch (IOException e1) {
                    Log.e(LOG, "error when closing the connection" + e1.getMessage(), e1);
                    break;
                }
            }
        }
    }


}
