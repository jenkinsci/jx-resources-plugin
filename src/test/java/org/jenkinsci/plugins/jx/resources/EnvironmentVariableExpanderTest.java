package org.jenkinsci.plugins.jx.resources;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

public class EnvironmentVariableExpanderTest {

    private final Map<String, String> MAP = ImmutableMap.of(
            "COMPOUND", "$BUILD_ID-$OTHER",
            "DELEGATE_MISSING", "$FOO",
            "COMPOUND_MISSING", "One-$FOO-Two",
            "BUILD_ID", "123",
            "OTHER", "A");

    @Test
    public void testGetEnv() {
        assertEquals("BUILD_ID should resolve to 123",
                "123", EnvironmentVariableExpander.getenv("BUILD_ID", MAP));
        assertEquals("COMPOUND should resolve to $BUILD_ID-$OTHER, which should in turn resolve to 123-A",
                "123-A", EnvironmentVariableExpander.getenv("COMPOUND", MAP));
        assertNull("NOTHING should resolve to null as it is not in the map", EnvironmentVariableExpander.getenv("NOTHING", MAP));
        assertEquals("DELEGATE_MISSING should resolve to $FOO, which itself cannot be resolved, so the output should be $FOO",
                "$FOO", EnvironmentVariableExpander.getenv("DELEGATE_MISSING", MAP));
        assertEquals("COMPOUND_MISSING should resolve to One-$FOO-Two, and as $FOO cannot be resolved, the final resolution should be One-$FOO-Two",
                "One-$FOO-Two", EnvironmentVariableExpander.getenv("COMPOUND_MISSING", MAP));
    }

    @Test
    public void testExpandIfNecessary() {
        assertEquals("123", EnvironmentVariableExpander.expandIfNecessary("123", MAP));
        assertEquals("123", EnvironmentVariableExpander.expandIfNecessary("$BUILD_ID", MAP));
        assertEquals("123-A", EnvironmentVariableExpander.expandIfNecessary("$BUILD_ID-$OTHER", MAP));
    }
}