package de.fu_berlin.inf.dpp.intellij.preferences;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.intellij.ide.util.PropertiesComponent;

@PrepareForTest({ PropertiesComponent.class })
@RunWith(PowerMockRunner.class)
public class PropertiesComponentAdapterTest {

    private final class PropertiesComponentStub extends PropertiesComponent {

        final Properties properties = new Properties();

        @Override
        public String getValue(String name) {
            return properties.getProperty(name);
        }

        @Override
        public String[] getValues(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isValueSet(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setValue(String name, String value) {
            properties.put(name, value);
        }

        @Override
        public void setValue(String name, String value, String defaultValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setValues(String name, String[] values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unsetValue(String arg0) {
            throw new UnsupportedOperationException();
        }
    }

    private PropertiesComponent propertiesComponentStub;

    @Before
    public void setup() {

        propertiesComponentStub = new PropertiesComponentStub();

        PowerMock.mockStatic(PropertiesComponent.class);

        EasyMock.expect(PropertiesComponent.getInstance()).andStubReturn(
            propertiesComponentStub);

        PowerMock.replay(PropertiesComponent.class);
    }

    @Test
    public void testGetDefaultDefaultValues() {
        final PropertiesComponentAdapter adapter = new PropertiesComponentAdapter();

        assertEquals(PropertiesComponentAdapter.DEFAULT_BOOLEAN,
            adapter.getDefaultBoolean("foo"));

        assertEquals(PropertiesComponentAdapter.DEFAULT_STRING,
            adapter.getDefaultString("foo"));

        assertEquals(PropertiesComponentAdapter.DEFAULT_INT,
            adapter.getDefaultInt("foo"));

        assertEquals(PropertiesComponentAdapter.DEFAULT_LONG,
            adapter.getDefaultLong("foo"));
    }

    @Test
    public void testGetValuesWithNoValuesSet() {
        final PropertiesComponentAdapter adapter = new PropertiesComponentAdapter();

        assertEquals(PropertiesComponentAdapter.DEFAULT_BOOLEAN,
            adapter.getBoolean("foo"));

        assertEquals(PropertiesComponentAdapter.DEFAULT_STRING,
            adapter.getString("foo"));

        assertEquals(PropertiesComponentAdapter.DEFAULT_INT,
            adapter.getInt("foo"));

        assertEquals(PropertiesComponentAdapter.DEFAULT_LONG,
            adapter.getLong("foo"));
    }

    @Test
    public void testGetValuesWithValuesSet() {
        final PropertiesComponentAdapter adapter = new PropertiesComponentAdapter();

        adapter.setValue("foo.boolean", "true");
        adapter.setValue("foo.integer", Integer.MAX_VALUE);
        adapter.setValue("foo.long", Long.MAX_VALUE);
        adapter.setValue("foo.string", "foo");

        assertEquals(true, adapter.getBoolean("foo.boolean"));
        assertEquals(Integer.MAX_VALUE, adapter.getInt("foo.integer"));
        assertEquals(Long.MAX_VALUE, adapter.getLong("foo.long"));
        assertEquals("foo", adapter.getString("foo.string"));
    }

    @Test
    public void testAssertProperDefaultValuesOnInvalidValueData() {
        final PropertiesComponentAdapter adapter = new PropertiesComponentAdapter();

        adapter.setValue("foo.boolean", "FILE_NOT_FOUND");
        adapter.setValue("foo.integer", "###0xABCDEF###");
        adapter.setValue("foo.long", "###0xABCDEF###");

        assertEquals(false, adapter.getBoolean("foo.boolean"));

        assertEquals(PropertiesComponentAdapter.DEFAULT_INT,
            adapter.getInt("foo.integer"));

        assertEquals(PropertiesComponentAdapter.DEFAULT_LONG,
            adapter.getLong("foo.long"));
    }
}
