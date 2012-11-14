package de.fu_berlin.inf.dpp.communication.chat.muc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;

import de.fu_berlin.inf.dpp.communication.chat.AbstractChat;
import de.fu_berlin.inf.dpp.communication.chat.ChatElement;
import de.fu_berlin.inf.dpp.communication.chat.ChatElement.ChatElementType;
import de.fu_berlin.inf.dpp.communication.chat.IChatListener;
import de.fu_berlin.inf.dpp.communication.chat.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.communication.chat.muc.states.MUCStateManager;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * This class encapsulates Smacks {@link MultiUserChat} and offers
 * {@link ChatState}s that are normally only provided by Smack for {@link Chat}
 * s.
 * 
 * @author bkahlert
 */
public class MultiUserChat extends AbstractChat {
    private final Logger log = Logger.getLogger(MultiUserChat.class);

    /**
     * {@link Connection} this {@link MultiUserChat} uses
     */
    private Connection connection;

    /**
     * {@link MUCSessionPreferences} this {@link MultiUserChat} uses
     */
    private MUCSessionPreferences preferences;

    /**
     * Saves whether this {@link MultiUserChat} instance created the
     * {@link MultiUserChat}. This information is used for the decision whether
     * to destroy or simply leave the {@link MultiUserChat} on disconnection.
     */
    private boolean createdRoom;

    /**
     * The encapsulated {@link MultiUserChat}
     */
    private org.jivesoftware.smackx.muc.MultiUserChat muc;

    /**
     * {@link JID}s taking part at the running {@link MultiUserChat} with their
     * current {@link ChatState}
     */
    private HashMap<JID, ChatState> participants = new HashMap<JID, ChatState>();

    /**
     * {@link MUCStateManager} used for {@link ChatState} events
     */
    private MUCStateManager mucStateManager;

    /**
     * {@link IChatListener} used for {@link ChatState} propagation
     */
    private IChatListener mucStateListener = new IChatListener() {
        public void stateChanged(JID jid, ChatState state) {
            log.debug("stateChanged fired with state: " + state.toString());

            if (!participants.containsKey(jid) && state == ChatState.active) {
                /*
                 * joined notification
                 */
                participants.put(jid, state);
                addHistoryEntry(new ChatElement(jid, new Date(),
                    ChatElementType.JOIN));
                MultiUserChat.this.notifyJIDConnected(jid);
                return;
            } else if (participants.containsKey(jid) && state == ChatState.gone) {
                /*
                 * left notification
                 */
                participants.remove(jid);
                addHistoryEntry(new ChatElement(jid, new Date(),
                    ChatElementType.LEAVE));
                MultiUserChat.this.notifyJIDDisconnected(jid);
                return;
            } else {
                /*
                 * state changed notification
                 */
                participants.put(jid, state);
                addHistoryEntry(new ChatElement(jid, new Date(), state));
                MultiUserChat.this.notifyJIDStateChanged(jid, state);
            }
        }

        @Override
        public void messageReceived(JID sender, String message) {
            /* do nothing */
        }

        @Override
        public void connected(JID jid) {
            /* do nothing */
        }

        @Override
        public void disconnected(JID jid) {
            /* do nothing */
        }

        @Override
        public void joined(JID jid) {
            /* do nothing */
        }

        @Override
        public void left(JID jid) {
            /* do nothing */
        }
    };

    /**
     * {@link PacketListener} for processing incoming messages
     */
    private PacketListener packetListener = new PacketListener() {
        public void processPacket(Packet packet) {
            log.debug("processPacket called");

            if (packet instanceof Message) {
                Message message = (Message) packet;
                if (message.getBody() == null || message.getBody().equals("")) {
                    return;
                }

                JID sender = JID
                    .createFromServicePerspective(message.getFrom());
                addHistoryEntry(new ChatElement(message, new Date()));
                MultiUserChat.this.notifyJIDMessageReceived(sender,
                    message.getBody());
            }
        }
    };

    /**
     * Creates a new {@link MultiUserChat}. You need to call
     * {@link MultiUserChat#connect()} in order to effectively create and join
     * the {@link MultiUserChat}.
     * 
     * @param connection
     * @param communicationPreferences
     */
    public MultiUserChat(Connection connection,
        MUCSessionPreferences communicationPreferences) {
        this.connection = connection;
        this.preferences = communicationPreferences;
    }

    /**
     * Connects to a {@link MultiUserChat} on the base of the passed
     * {@link MUCSessionPreferences}.
     * 
     * @return true if the room has been created and joined; false if it only
     *         has been joined
     * @throws XMPPException
     *             TODO connect should be split into create and join; bkahlert
     *             2010/11/23
     */
    @Override
    public boolean connect() throws XMPPException {
        if (preferences == null)
            throw new IllegalStateException("No comPrefs found!");
        this.createdRoom = createAndJoinMUC();
        this.mucStateManager = MUCStateManager
            .getInstance(this.connection, muc);
        this.mucStateManager.addMUCStateListener(mucStateListener);
        this.muc.addMessageListener(packetListener);
        return this.createdRoom;
    }

    /**
     * Disconnects from a {@link MultiUserChat}
     * 
     * @return TODO disconnect should be split into leave and destroy; bkahlert
     *         2010/11/23
     */
    @Override
    public boolean disconnect() {
        if (this.muc == null)
            return this.createdRoom;
        this.muc.removeMessageListener(packetListener);
        this.mucStateManager.removeMUCStateListener(mucStateListener);

        /*
         * Because no ChatState changes can be received anymore we need to
         * manually propagate them locally.
         */
        MultiUserChat.this.notifyJIDDisconnected(this.getJID());
        this.setCurrentState(ChatState.gone);
        this.mucStateManager = null;
        clearHistory();

        if (this.createdRoom) {
            try {
                muc.destroy(null, null);
            } catch (XMPPException e) {
                log.debug("Could not destroy room: " + preferences.getRoom(), e);
            }
        } else {
            muc.leave();
        }
        this.muc = null;
        return this.createdRoom;
    }

    /**
     * Creates and joins the {@link MultiUserChat} on the base of the passed
     * {@link MUCSessionPreferences}.
     * 
     * @throws XMPPException
     *             TODO connect should be split into create and join; bkahlert
     *             2010/11/23
     */
    private boolean createAndJoinMUC() throws XMPPException {
        /*
         * Connect to a room
         */
        org.jivesoftware.smackx.muc.MultiUserChat muc = new org.jivesoftware.smackx.muc.MultiUserChat(
            connection, preferences.getRoom());
        boolean joined = false;
        this.createdRoom = false;

        XMPPException exception = null;

        /*
         * Try to create and then join the room TODO: Check whether the case
         * happens that the room was not joined, that is: No room creation is
         * ever necessary.
         */
        try {
            log.debug("Trying to create room on server "
                + this.preferences.getService());
            muc.create(connection.getUser());
            this.createdRoom = true;
            joined = true;
        } catch (XMPPException e) {
            exception = e;
        }

        /*
         * Join the room
         */
        /*
         * TODO: Notice: Some chat server implementations implicitly create a
         * room on the first join. Therefore it would be better to force the
         * user to explicitly call create
         */
        if (!joined) {
            try {
                muc.join(connection.getUser(), preferences.getPassword());
                joined = true;
            } catch (XMPPException e) {
                throw exception != null ? exception : e;
            }
        }

        this.muc = muc;

        if (this.createdRoom) {
            configureRoom();
        }

        log.debug("MUC " + ((createdRoom) ? "created and " : "")
            + "joined. Server: " + preferences.getService() + " Room: "
            + preferences.getRoomName() + " Password "
            + preferences.getPassword());

        return createdRoom;
    }

    /**
     * Create and dispatch configuration for a multi user chat room.
     */
    private void configureRoom() {
        try {
            log.debug("Configuring room");
            // Get the the room's configuration form
            Form form = muc.getConfigurationForm();

            // Insert fields which are missing or have a required value
            Map<String, Boolean> booleanConfigs = new HashMap<String, Boolean>();
            booleanConfigs.put("muc#roomconfig_moderatedroom", false);
            booleanConfigs.put("muc#roomconfig_publicroom", false);
            booleanConfigs.put("muc#roomconfig_allowinvites", true);
            booleanConfigs.put("muc#roomconfig_persistentroom", false);
            booleanConfigs.put("muc#roomconfig_passwordprotectedroom", true);

            FormField formOption;
            for (String fieldName : booleanConfigs.keySet()) {
                formOption = new FormField(fieldName);
                formOption.setType(FormField.TYPE_BOOLEAN);
                form.addField(formOption);
            }
            formOption = new FormField("muc#roomconfig_roomsecret");
            formOption.setType(FormField.TYPE_TEXT_PRIVATE);
            form.addField(formOption);

            // Create an answer form
            Form submitForm = form.createAnswerForm();

            // Add default answers to the form to submit
            for (Iterator<FormField> fields = form.getFields(); fields
                .hasNext();) {
                FormField field = fields.next();
                if (!FormField.TYPE_HIDDEN.equals(field.getType())
                    && (field.getVariable() != null)) {
                    // Sets the default value as the answer
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }

            // Add our custom fields to the form
            for (Map.Entry<String, Boolean> field : booleanConfigs.entrySet()) {
                submitForm.setAnswer(field.getKey(), field.getValue());
            }
            submitForm.setAnswer("muc#roomconfig_roomsecret",
                preferences.getPassword());

            // Send the completed form to the server to configure the room
            muc.sendConfigurationForm(submitForm);
        } catch (XMPPException e) {
            log.debug(e);
        }
    }

    /**
     * Sets the own {@link ChatState} and notifies all participants
     * 
     * @param state
     */
    @Override
    public void setCurrentState(ChatState state) {
        if (muc == null) {
            log.error("MUC does not exist");
            return;
        }

        if (state == null)
            return;

        try {
            mucStateManager.setState(state);
        } catch (XMPPException e) {
            log.error("Error sending state to: " + getPreferences().getRoom());
        }
    }

    /**
     * Returns the {@link MUCSessionPreferences} used for this
     * {@link MultiUserChat}
     * 
     * @return
     */
    public MUCSessionPreferences getPreferences() {
        return this.preferences;
    }

    /**
     * Returns the {@link JID} used for connection
     * 
     * @return
     */
    @Override
    public JID getJID() {
        if (this.connection == null)
            return null;
        return new JID(this.connection.getUser());
    }

    /**
     * Returns true if the {@link JID} has joined the {@link MultiUserChat}
     * 
     * @param jid
     * @return
     */
    public boolean isJoined(JID jid) {
        return this.participants.get(jid) != null;
    }

    /**
     * Returns true if the {@link JID} used for connection has joined the
     * {@link MultiUserChat}
     * 
     * @return
     */
    public boolean isJoined() {
        return muc != null && muc.isJoined();
    }

    /**
     * Return the {@link ChatState} of a participant
     * 
     * @param jid
     *            the participant
     * @return null if {@link JID} is no participant of this
     *         {@link MultiUserChat}
     */
    public ChatState getState(JID jid) {
        return this.participants.get(jid);
    }

    /**
     * Returns the {@link ChatState} of all participants but oneself.
     * 
     * @return
     */
    public List<ChatState> getForeignStates() {
        JID self = this.getJID();
        List<ChatState> foreignStates = new ArrayList<ChatState>();
        for (JID jid : this.participants.keySet()) {
            if (jid.equals(self))
                continue;
            foreignStates.add(this.participants.get(jid));
        }
        return foreignStates;
    }

    /**
     * Returns the number of foreign {@link ChatState} that are equal to the
     * provided {@link ChatState}.
     * 
     * @param filterState
     * @return
     * 
     * @see #getForeignStates()
     */
    public int getForeignStatesCount(ChatState filterState) {
        int count = 0;
        for (ChatState chatState : getForeignStates()) {
            if (chatState == filterState) {
                count++;
            }
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return muc.getRoom();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<JID> getParticipants() {
        return this.participants.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(String body) throws XMPPException {
        Message message = muc.createMessage();
        message.setBody(body);

        this.sendMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Message message) throws XMPPException {
        if (muc == null) {
            log.error("MUC does not exist");
            return;
        }

        if (message == null)
            return;

        this.muc.sendMessage(message);
    }

    /**
     * {@inheritDoc}
     * 
     * @return <code>null</code>
     */
    @Override
    public String getThreadID() {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @return <code>true</code> if Smack is connected and the room has been
     *         joined
     */
    @Override
    public boolean isConnected() {
        return connection.isConnected() && isJoined();
    }

}