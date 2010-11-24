package de.fu_berlin.inf.dpp.editor.internal;

import org.apache.log4j.Logger;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * This Document provider tries tell others that files are not editable if not a
 * driver.
 */
@Component(module = "util")
public class SharedDocumentProvider extends TextFileDocumentProvider {

    private static final Logger log = Logger
        .getLogger(SharedDocumentProvider.class.getName());

    protected ISarosSession sarosSession;

    @Inject
    protected SarosSessionManager sessionManager;

    protected boolean isDriver;

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sarosSession = newSarosSession;
            isDriver = sarosSession.isDriver();
            sarosSession.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            assert sarosSession == oldSarosSession;
            sarosSession.removeListener(sharedProjectListener);
            sarosSession = null;
        }
    };

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user) {
            if (sarosSession != null) {
                isDriver = sarosSession.isDriver();
            } else {
                log.warn("Internal error: Shared project null in roleChanged!");
            }
        }
    };

    public SharedDocumentProvider(SarosSessionManager sessionManager) {
        this.sessionManager = sessionManager;

        if (sessionManager.getSarosSession() != null) {
            sessionListener.sessionStarted(sessionManager.getSarosSession());
        }
        sessionManager.addSarosSessionListener(sessionListener);
    }

    /**
     * This constructor is necessary when Eclipse creates a
     * SharedDocumentProvider.
     */
    public SharedDocumentProvider() {

        log.debug("SharedDocumentProvider created by Eclipse");

        Saros.reinject(this);

        if (sessionManager.getSarosSession() != null) {
            sessionListener.sessionStarted(sessionManager.getSarosSession());
        }
        sessionManager.addSarosSessionListener(sessionListener);
    }

    @Override
    public boolean isReadOnly(Object element) {
        return super.isReadOnly(element);
    }

    @Override
    public boolean isModifiable(Object element) {
        if (!isInSharedProject(element)) {
            return super.isModifiable(element);
        }

        return this.isDriver && super.isModifiable(element);
    }

    @Override
    public boolean canSaveDocument(Object element) {
        return super.canSaveDocument(element);
    }

    @Override
    public boolean mustSaveDocument(Object element) {
        return super.mustSaveDocument(element);
    }

    private boolean isInSharedProject(Object element) {

        if (sarosSession == null)
            return false;

        IFileEditorInput fileEditorInput = (IFileEditorInput) element;

        return sarosSession.isShared(fileEditorInput.getFile().getProject());
    }
}