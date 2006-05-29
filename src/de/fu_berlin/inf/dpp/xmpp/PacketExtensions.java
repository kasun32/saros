/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitšt Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.xmpp;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.ProviderManager;

public class PacketExtensions {
    public static final String  NAMESPACE        = "de.fu_berlin.inf.dpp";

    // elements
    private static final String INVITATION       = "invite";
    private static final String JOIN             = "join";
    private static final String LEAVE            = "leave";
    private static final String CLOSE            = "close";
    private static final String REQUEST_FOR_LIST = "requestList";

    // attributes
    public static final String  DESCRIPTION      = "description";
    
    public static void hookExtensionProviders() {
        ProviderManager.addExtensionProvider(
            ActivitiesPacketExtension.ELEMENT, NAMESPACE,new ActivitiesProvider());
    }
    
    public static PacketExtension createInviteExtension(String description) {
        DefaultPacketExtension extension = new DefaultPacketExtension(INVITATION, NAMESPACE);
        extension.setValue(DESCRIPTION, description);
        return extension;
    }
    
    public static PacketExtension createRequestForFileListExtension() {
        return createExtension(REQUEST_FOR_LIST);
    }
    
    public static PacketExtension createJoinExtension() {
        return createExtension(JOIN);
    }
    
    public static PacketExtension createLeaveExtension() {
        return createExtension(LEAVE);
    }
    
    public static PacketExtension createCloseExtension() {
        return createExtension(CLOSE);
    }
    
    /**
     * The invite extension has a description field.
     */
    public static DefaultPacketExtension getInviteExtension(Message message) {
        return getExtension(INVITATION, message);
    }

    public static DefaultPacketExtension getJoinExtension(Message message) {
        return getExtension(JOIN, message);
    }
    
    public static DefaultPacketExtension getLeaveExtension(Message message) {
        return getExtension(LEAVE, message);
    }
    
    public static DefaultPacketExtension getCloseExtension(Message message) {
        return getExtension(CLOSE, message);
    }
    
    public static DefaultPacketExtension getRequestExtension(Message message) {
        return getExtension(REQUEST_FOR_LIST, message);
    }
    
    public static ActivitiesPacketExtension getActvitiesExtension(Message message) {
        return (ActivitiesPacketExtension)message.getExtension(
            ActivitiesPacketExtension.ELEMENT, NAMESPACE); 
    }
    
    private static DefaultPacketExtension createExtension(String element) {
        DefaultPacketExtension extension = new DefaultPacketExtension(element, NAMESPACE);
        extension.setValue(element, "");
        return extension;
    }

    private static DefaultPacketExtension getExtension(String element, Message message) {
        return (DefaultPacketExtension)message.getExtension(element, NAMESPACE);
    }
}
