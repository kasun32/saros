package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponent;

public class IsEditorContentsSame extends DefaultCondition {

    private EditorComponent editor;
    private String otherContent;
    private String[] filePath;

    IsEditorContentsSame(EditorComponent editor, String otherConent,
        String... filePath) {
        this.editor = editor;
        this.otherContent = otherConent;
        this.filePath = filePath;

    }

    public String getFailureMessage() {
        return null;
    }

    public boolean test() throws Exception {
        return editor.getTextOfEditor(filePath).equals(otherContent);
    }
}
