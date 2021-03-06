package de.fu_berlin.inf.dpp.example;

import com.intellij.codeHighlighting.HighlightingPass;
import com.thoughtworks.xstream.XStream;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import org.junit.Assert;
import org.junit.Test;

public class FirstTest {

    @Test
    public void canReferenceClassesAcrossDependencies() {
        Assert
            .assertNotNull(TextEditActivity.class); // from saros core classpath
        Assert.assertNotNull(XStream.class); // from saros core libs
        Assert.assertNotNull(
            HighlightingPass.class); // from IntelliJ dependencies
    }
}
