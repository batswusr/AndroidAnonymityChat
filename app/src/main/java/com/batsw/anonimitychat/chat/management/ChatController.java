package com.batsw.anonimitychat.chat.management;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.batsw.anonimitychat.appManagement.AppController;
import com.batsw.anonimitychat.chat.ChatActivity;
import com.batsw.anonimitychat.chat.constants.ChatModelConstants;
import com.batsw.anonimitychat.chat.listener.IIncomingConnectionListener;
import com.batsw.anonimitychat.chat.management.connection.ChatConnectionManagerImpl;
import com.batsw.anonimitychat.chat.management.connection.IChatConnectionManager;
import com.batsw.anonimitychat.chat.message.IMessageReceivedListener;
import com.batsw.anonimitychat.chat.persistence.PersistenceManager;
import com.batsw.anonimitychat.chat.util.ConnectionType;
import com.batsw.anonimitychat.mainScreen.entities.ContactEntity;
import com.batsw.anonimitychat.persistence.entities.DBContactEntity;
import com.batsw.anonimitychat.tor.connections.ITorConnection;

/**
 * Created by tudor on 2/10/2017.
 */

public class ChatController implements IIncomingConnectionListener {

    private static final String CHAT_CONTROLLER_LOG = ChatController.class.getSimpleName();

    private PersistenceManager mPersistenceManager;

    private IChatConnectionManager mChatConnectionManager;

    private static ChatController mInstance;

    private static Activity mCurrentActivityContext = null;

    private static boolean isIncomingChatConnection = false;

    private String mMyTorAddress = "";

    private ChatController() {

        init();
    }

    public static synchronized ChatController getInstance() {
        if (mInstance == null) {
            mInstance = new ChatController();
        }
        return mInstance;
    }

    /**
     * preparing the concurrent resources
     */
    private void init() {
        Log.i(CHAT_CONTROLLER_LOG, "init -> ENTER");

        mPersistenceManager = new PersistenceManager();

        mChatConnectionManager = new ChatConnectionManagerImpl();

        Log.i(CHAT_CONTROLLER_LOG, "init -> LEAVE");
    }

    public void initializeChatConnectionManagement() {
        Log.i(CHAT_CONTROLLER_LOG, "initializeChatConnectionManagement -> ENTER");

        mChatConnectionManager.initializeConnectionManagement();

        Log.i(CHAT_CONTROLLER_LOG, "initializeChatConnectionManagement -> LEAVE");
    }

    public ChatDetail establishConnectionToPartner(IMessageReceivedListener messageReceivedListener, long sessionId) {
        Log.i(CHAT_CONTROLLER_LOG, "establishConnectionToPartner -> ENTER messageReceivedListener=" + messageReceivedListener + " sessionId=" + sessionId);
        ChatDetail chatDetail = this.getChatDetail(sessionId);

        ITorConnection partnerConnection = mChatConnectionManager.getConnection(chatDetail, messageReceivedListener);
        if (partnerConnection != null && partnerConnection.isAlive()) {
            chatDetail.setIsAlive(true);
            chatDetail.setTorConnection(partnerConnection);
        } else {
            //        TODO: what happens if it is a test address and user tries to connect to it .....
//            the its false
        }

        Log.i(CHAT_CONTROLLER_LOG, "establishConnectionToPartner -> LEAVE chatDetail=" + chatDetail);
        return chatDetail;
    }

    public void sendMessage(long sessionId, String message) {
        Log.i(CHAT_CONTROLLER_LOG, "sendMessage -> ENTER sessionId=" + sessionId + " ; message=" + message);

        ChatDetail chatDetail = this.getChatDetail(sessionId);
        if (chatDetail.isAlive()) {

            chatDetail.getTorConnection().sendMessage(message);
        }

        Log.i(CHAT_CONTROLLER_LOG, "sendMessage -> LEAVE");
    }

    private ChatDetail getChatDetailForChatAction(String partnerAddress) {
        Log.i(CHAT_CONTROLLER_LOG, "getChatDetailForChatAction -> ENTER partnerAddress=" + partnerAddress);
        ChatDetail retVal = null;

        if (mPersistenceManager.isPartnerInTheList(partnerAddress)) {
            retVal = mPersistenceManager.getPartnerDetail(partnerAddress);
            retVal.setmConnectionType(ConnectionType.NO_CONNECTION);
            retVal.setIsAlive(false);
        } else {

            // means that the contact is new and it will be added with DEFAULT prameters in the Contacts List
            // default nickName for address is the address itself
            //TODO: differentiate between the two connection types

            DBContactEntity dbContactEntity = AppController.getInstanceParameterized(null).getContactEntity(partnerAddress);

            ChatDetail newChatDetail = null;

//            Means that the contact does not exist (chat request from new partner)
            if (dbContactEntity == null) {
                dbContactEntity = new DBContactEntity();
                dbContactEntity.setName(partnerAddress.substring(0, 6));
                dbContactEntity.setNickName(partnerAddress);
                dbContactEntity.setAddress(partnerAddress);

                boolean insertSuccessfull = AppController.getInstanceParameterized(null).addNewContact(dbContactEntity);

                if (insertSuccessfull) {
                    ContactEntity newContactEntity = new ContactEntity(
                            dbContactEntity.getNickName(),
                            dbContactEntity.getSessionId());
                    AppController.getInstanceParameterized(null).addNewContactToTab(newContactEntity);
                }

//                I need the sessionId generated by the AppController
                dbContactEntity = AppController.getInstanceParameterized(null).getContactEntity(partnerAddress);
                newChatDetail = new ChatDetail(partnerAddress, dbContactEntity.getNickName(), null, ConnectionType.NO_CONNECTION, dbContactEntity.getSessionId(), false);
            } else {

                newChatDetail = new ChatDetail(partnerAddress, dbContactEntity.getNickName(), null, ConnectionType.NO_CONNECTION, dbContactEntity.getSessionId(), false);
            }

            mPersistenceManager.addPartnerToList(newChatDetail);

            retVal = newChatDetail;
        }

        if (!isIncomingChatConnection) {
            retVal.setmConnectionType(ConnectionType.USER);
        } else {
            retVal.setmConnectionType(ConnectionType.PARTNER);

            isIncomingChatConnection = false;
        }

        mPersistenceManager.updatePartnerDetails(retVal);

        Log.i(CHAT_CONTROLLER_LOG, "getChatDetailForChatAction -> LEAVE retVal=" + retVal);
        return retVal;
    }

    public ChatDetail getChatDetail(long sessionId) {
        Log.i(CHAT_CONTROLLER_LOG, "getChatDetail -> ENTER sessionId=" + sessionId);

        ChatDetail retVal = null;

        if (mPersistenceManager.isPartnerInTheList(sessionId)) {
            retVal = mPersistenceManager.getPartnerDetail(sessionId);
        }

        Log.i(CHAT_CONTROLLER_LOG, "getChatDetail -> LEAVE retVal=" + retVal);
        return retVal;
    }

    public void stoppedChatActivity(IMessageReceivedListener messageReceivedListener, long sessionId) {
        Log.i(CHAT_CONTROLLER_LOG, "stoppedChatActivity -> ENTER");

        ChatDetail chatDetail = this.getChatDetail(sessionId);

//        if chatDetail.isAlive is false --- nothing to close
        mChatConnectionManager.closeConnection(chatDetail);

        Log.i(CHAT_CONTROLLER_LOG, "stoppedChatActivity -> LEAVE");
    }

    /**
     * This method is starting a ChatActivity from whatever Activity
     *
     * @param currentContext
     */
    public void startChatActivity(Context currentContext, String partnerHostName) {
        Log.i(CHAT_CONTROLLER_LOG, "startChatActivity -> ENTER currentContext=" + currentContext + " ,partnerHostName=" + partnerHostName);

        Intent chatActivityIntent = ChatActivity.makeIntent(currentContext);

        chatActivityIntent.putExtra(ChatModelConstants.CHAT_ACTIVITY_INTENT_EXTRA_KEY, ChatController.getInstance().getChatDetailForChatAction(partnerHostName).getSessionId());

        chatActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        currentContext.startActivity(chatActivityIntent);
        Log.i(CHAT_CONTROLLER_LOG, "startChatActivity -> LEAVE");
    }

    @Override
    public void triggerIncomingPartnerConnectionEvent(final String partnerHostName) {
        Log.i(CHAT_CONTROLLER_LOG, "triggerIncomingPartnerConnectionEvent -> ENTER partnerHostName=" + partnerHostName);

        //TODO: trigger the creation of a POP-up on the screen to let the USER decide whether to continue the
        if (partnerHostName != null || !partnerHostName.isEmpty()) {

            isIncomingChatConnection = true;

//            if (!(mCurrentActivityContext instanceof MainScreenActivity)) {
////TODO: fdo something to show the user that another contact si looking for him
//                Log.i(CHAT_CONTROLLER_LOG, "some activity already open -> ---");
//
//            } else {
            ChatController.getInstance().startChatActivity(mCurrentActivityContext, partnerHostName);
//            }
        }
        Log.i(CHAT_CONTROLLER_LOG, "triggerIncomingPartnerConnectionEvent -> LEAVE");
    }

    public void setCurrentActivityContext(Activity context) {
        Log.i(CHAT_CONTROLLER_LOG, "setCurrentActivityContext -> ENTER context=" + context);

        mCurrentActivityContext = context;

        Log.i(CHAT_CONTROLLER_LOG, "setCurrentActivityContext -> LEAVE");
    }

    public void setMyAddress(String myTorAddress) {
        Log.i(CHAT_CONTROLLER_LOG, "setMyAddress -> ENTER myTorAddress=" + myTorAddress);
        mMyTorAddress = myTorAddress;
        Log.i(CHAT_CONTROLLER_LOG, "setMyAddress -> LEAVE");
    }

    public String getMyAddress() {
        Log.i(CHAT_CONTROLLER_LOG, "getMyAddress -> ENTER");
        String retVal = mMyTorAddress;
        Log.i(CHAT_CONTROLLER_LOG, "getMyAddress -> LEAVE retVal=" + retVal);
        return retVal;
    }

    public static void cleanUp() {
        Log.i(CHAT_CONTROLLER_LOG, "cleanUp -> ENTER");
        if (mInstance != null) {
            mInstance.destroy();
            mInstance = null;
        }
        Log.i(CHAT_CONTROLLER_LOG, "cleanUp -> LEAVE");
    }

    private void destroy() {
        Log.i(CHAT_CONTROLLER_LOG, "destroy -> ENTER");

        mPersistenceManager = null;

        mChatConnectionManager.clearResources();
        mChatConnectionManager = null;

        Log.i(CHAT_CONTROLLER_LOG, "destroy -> LEAVE");
    }
}
