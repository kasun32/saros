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
package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.listeners.ISessionListener;
import de.fu_berlin.inf.dpp.listeners.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.xmpp.JID;

public class TakeDriverRoleAction extends Action 
    implements ISharedProjectListener, ISessionListener {

    public TakeDriverRoleAction() {
        super("Take user driver role");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/user_edit.png"));
        setToolTipText("Take driver role");
        
        Saros.getDefault().getSessionManager().addSessionListener(this);
        updateEnablement();
    }

    @Override
    public void run() {
        getSharedProject().setDriver(getSharedProject().getHost(), false);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
        session.addListener(this);
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
        session.removeListener(this);
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void driverChanged(JID driver, boolean replicated) {
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void driverPathChanged(IPath path, boolean replicated) {
        // ignore
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userJoined(JID user) {
        // ignore
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userLeft(JID user) {
        // ignore
    }

    private void updateEnablement() {
        ISharedProject project = getSharedProject();
        setEnabled(project != null && project.isHost() && !project.isDriver());
    }
    
    private ISharedProject getSharedProject() {
        return Saros.getDefault().getSessionManager().getSharedProject();
    }
}
