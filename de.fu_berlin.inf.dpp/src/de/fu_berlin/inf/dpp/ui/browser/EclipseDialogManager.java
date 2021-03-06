package de.fu_berlin.inf.dpp.ui.browser;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.view_parts.BrowserPage;

/**
 * Eclipse side implementation of the IDialogManager interface
 */
public class EclipseDialogManager implements IDialogManager {

    private static final Logger LOG = Logger
        .getLogger(EclipseDialogManager.class);

    private Map<String, Shell> openDialogs = new HashMap<String, Shell>();

    private final EclipseBrowserCreator browserCreator;

    public EclipseDialogManager(EclipseBrowserCreator browserCreator) {
        this.browserCreator = browserCreator;
    }

    @Override
    public void showDialogWindow(final BrowserPage browserPage) {
        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
            @Override
            public void run() {
                if (dialogIsOpen(browserPage)) {
                    // If the user try to open a dialog that is already open,
                    // the dialog should get active and in the foreground to
                    // help the
                    // user find it.

                    // TODO: Add this method to IDialog have a common behavior
                    // for
                    // Saros/E and Saros/J
                    reopenDialogWindow(browserPage);
                    return;
                }

                final Shell activeShell = SWTUtils.getShell();

                // should only happen if Eclipse shuts down
                if (activeShell == null)
                    return;

                final Shell browserShell = new Shell(activeShell);

                // TODO WebPage#getTitle() ?
                browserShell.setText(browserPage.getWebpage());
                browserShell.setLayout(new FillLayout());
                browserShell.setSize(640, 480);

                browserCreator.createBrowser(browserShell, SWT.NONE,
                    browserPage);

                browserShell.addShellListener(new ShellAdapter() {

                    @Override
                    public void shellClosed(ShellEvent e) {
                        openDialogs.remove(browserPage.getWebpage());
                        LOG.debug(browserPage.getWebpage() + " is closed");
                    }

                });

                browserShell.open();
                browserShell.pack();

                centerShellRelativeToParent(browserShell);

                openDialogs.put(browserPage.getWebpage(), browserShell);
            }
        });
    }

    @Override
    public void closeDialogWindow(final BrowserPage browserPage) {
        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
            @Override
            public void run() {
                if (!dialogIsOpen(browserPage)) {
                    LOG.warn(browserPage.getWebpage() + "could not be found");
                    return;
                }

                // shell is removed in the ShellLister
                openDialogs.get(browserPage.getWebpage()).close();
            }
        });
    }

    /**
     * Set the location of a given dialog to the center of the eclipse instance.
     * If the given browserPage is not currently displayed in a shell/dialog
     * this does nothing.
     *
     * @param browserPage
     *            the BrowserPage
     */
    public void reopenDialogWindow(BrowserPage browserPage) {
        if (!dialogIsOpen(browserPage)) {
            LOG.warn(browserPage.getWebpage() + "could not be found");
            return;
        }

        final Shell browserShell = openDialogs.get(browserPage.getWebpage());

        centerShellRelativeToParent(browserShell);
        browserShell.setActive();
        browserShell.open();

    }

    /**
     * @param browserPage
     * @return true if the browserPage is currently displayed in a shell/dialog
     */
    public boolean dialogIsOpen(BrowserPage browserPage) {
        return openDialogs.containsKey(browserPage.getWebpage());
    }

    private void centerShellRelativeToParent(final Shell shell) {

        final Composite composite = shell.getParent();

        if (!(composite instanceof Shell))
            return;

        final Shell parent = (Shell) composite;

        final Rectangle parentShellBounds = parent.getBounds();
        final Point shellSize = shell.getSize();

        shell.setLocation(parentShellBounds.x
            + (parentShellBounds.width - shellSize.x) / 2, parentShellBounds.y
            + (parentShellBounds.height - shellSize.y) / 2);
    }
}
