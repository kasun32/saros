package de.fu_berlin.inf.dpp.filesystem;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

public class EclipseFileImpl extends EclipseResourceImpl implements IFile {

    EclipseFileImpl(org.eclipse.core.resources.IFile delegate) {
        super(delegate);
    }

    @Override
    public String getCharset() throws IOException {
        try {
            return getDelegate().getCharset();
        } catch (CoreException e) {
            throw new IOException(e);
        }
    }

    @Override
    public InputStream getContents() throws IOException {
        try {
            return getDelegate().getContents();
        } catch (CoreException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void setContents(InputStream input, boolean force,
        boolean keepHistory) throws IOException {
        try {
            getDelegate().setContents(input, force, keepHistory, null);
        } catch (CoreException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void create(InputStream input, boolean force) throws IOException {
        try {
            getDelegate().create(input, force, null);
        } catch (CoreException e) {
            throw new IOException(e);
        }
    }

    @Override
    public IPath getLocation() {
        org.eclipse.core.runtime.IPath location = getDelegate().getLocation();

        if (location == null)
            return null;

        return new EclipsePathImpl(location);
    }

    /**
     * Returns the original {@link org.eclipse.core.resources.IFile IFile}
     * object.
     * 
     * @return
     */
    @Override
    public org.eclipse.core.resources.IFile getDelegate() {
        return (org.eclipse.core.resources.IFile) delegate;
    }
}
