package de.fu_berlin.inf.dpp.whiteboard.standalone;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import de.fu_berlin.inf.dpp.whiteboard.gef.editor.WhiteboardEditor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "de.fu_berlin.inf.dpp.whiteboard.standalone.perspective";

	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	@Override
	public void postStartup() {

		try {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			page.openEditor(new DummyEditorInput("empty"), WhiteboardEditor.ID,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class DummyEditorInput implements IEditorInput {

		public String name = null;

		public DummyEditorInput(String name) {
			this.name = name;
		}

		// @Override
		@Override
		public boolean exists() {
			return (this.name != null);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof DummyEditorInput))
				return false;
			return ((DummyEditorInput) o).getName().equals(getName());
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return ImageDescriptor.getMissingImageDescriptor();
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return this.name;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return null;
		}
	}
}