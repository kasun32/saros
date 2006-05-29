package de.fu_berlin.inf.dpp.test.stubs;

import java.io.InputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.internal.InvitationProcess.InvitationException;
import de.fu_berlin.inf.dpp.xmpp.JID;

public class IncomingInvitationProcessStub implements IIncomingInvitationProcess {
    private int seconds;
    
    public IncomingInvitationProcessStub() {
    }
    
    public IncomingInvitationProcessStub(int seconds) {
        this.seconds = seconds;
    }
    
    public FileList requestRemoteFileList(IProgressMonitor monitor) {
        if (seconds > 0) 
            waitWithProgressMonitor(monitor);
        
        return new FileList();
    }

    public void accept(IProject localProject, String newProjectName, 
        IProgressMonitor monitor) throws InvitationException {
        
        if (seconds > 0) 
            waitWithProgressMonitor(monitor);
    }

    public FileList getRemoteFileList() {
        return new FileList();
    }

    public Exception getException() {
        return null;
    }

    public State getState() {
        return null;
    }

    public JID getPeer() {
        return new JID("jid@test.org");
    }

    public String getDescription() {
        return "test description";
    }

    public void fileListReceived(JID from, FileList fileList) {
    }

    public void fileListRequested(JID from) {
    }

    public void joinReceived(JID from) {
    }

    public void resourceSent(IPath path) {
    }

    public void resourceReceived(JID from, IPath path, InputStream input) {
    }
    
    private void waitWithProgressMonitor(IProgressMonitor monitor) {
        monitor.beginTask("test wait", IProgressMonitor.UNKNOWN);
        
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + seconds * 1000 ) {
            try {
                Thread.sleep(200);
                monitor.worked(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        monitor.done();
    }
}
