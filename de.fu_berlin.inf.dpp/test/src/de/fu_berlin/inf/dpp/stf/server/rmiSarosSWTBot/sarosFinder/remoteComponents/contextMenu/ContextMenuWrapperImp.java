package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTableItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class ContextMenuWrapperImp extends EclipseComponentImp implements
    ContextMenuWrapper {

    protected static TeamCImp teamC;
    protected static RefactorCImp reafactorC;

    protected STFBotTreeItem treeItem;
    protected STFBotTree tree;
    protected treeItemType type;

    protected STFBotTableItem tableItem;

    public void setTreeItem(STFBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    public void setTableItem(STFBotTableItem tableItem) {
        this.tableItem = tableItem;
    }

    public void setTreeItemType(treeItemType type) {
        this.type = type;
    }

    public void setTree(STFBotTree tree) {
        this.tree = tree;
    }

    public void open() throws RemoteException {
        treeItem.contextMenu(CM_OPEN).click();
    }

    public void copy() throws RemoteException {
        treeItem.contextMenu(MENU_COPY).click();
    }

    public void paste(String target) throws RemoteException {
        tree.contextMenu(MENU_PASTE).click();
        switch (type) {
        case PROJECT:
            STFBotShell shell = bot().shell(SHELL_COPY_PROJECT);
            shell.activate();
            shell.bot().textWithLabel("Project name:").setText(target);
            shell.bot().button(OK).click();
            bot().waitsUntilShellIsClosed(SHELL_COPY_PROJECT);
            bot().sleep(1000);
            break;
        default:
            break;
        }
    }

    public void openWith(String editorType) throws RemoteException {
        treeItem.contextMenu(CM_OPEN_WITH, CM_OTHER).click();
        bot().waitUntilShellIsOpen(SHELL_EDITOR_SELECTION);
        STFBotShell shell_bob = bot().shell(SHELL_EDITOR_SELECTION);
        shell_bob.activate();
        shell_bob.bot().table().getTableItem(editorType).select();
        shell_bob.bot().button(OK).waitUntilIsEnabled();
        shell_bob.confirm(OK);
    }

    public void delete() throws RemoteException {
        treeItem.contextMenu(CM_DELETE).click();
        switch (type) {
        case PROJECT:
            bot().shell(SHELL_DELETE_RESOURCE).confirmWithCheckBox(OK, true);
            bot().waitsUntilShellIsClosed(SHELL_DELETE_RESOURCE);
            break;
        case JAVA_PROJECT:
            bot().shell(SHELL_DELETE_RESOURCE).confirmWithCheckBox(OK, true);
            bot().waitsUntilShellIsClosed(SHELL_DELETE_RESOURCE);
            break;
        default:
            bot().waitUntilShellIsOpen(CONFIRM_DELETE);
            bot().shell(CONFIRM_DELETE).activate();
            bot().shell(CONFIRM_DELETE).bot().button(OK).click();
            bot().sleep(300);
            break;
        }
        tree.waitUntilItemNotExists(treeItem.getText());
    }

    public TeamC team() throws RemoteException {
        teamC.setTreeItem(treeItem);
        return teamC;
    }

    public RefactorC refactor() throws RemoteException {
        reafactorC.setTreeItem(treeItem);
        reafactorC.setTreeItemType(type);
        return reafactorC;
    }
}