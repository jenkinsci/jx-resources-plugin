package org.jenkinsci.plugins.jx.resources;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * Utility class that behaves like {@link System#getenv(String)} but it will perform further expansion if the
 * environment variable would expand to another dollar-notation, eg $VAL_NAME
 *
 * Variable references $(VAR_NAME) are expanded using the previous defined environment variables in the container and
 * any service environment variables. If a variable cannot be resolved, the reference in the input string will be
 * unchanged. The $(VAR_NAME) syntax can be escaped with a double $$, ie: $$(VAR_NAME). Escaped references will never
 * be expanded, regardless of whether the variable exists or not. Defaults to "".
 */
public class EnvironmentVariableExpander {
    private static Map<String, String> envVars = System.getenv();

    public static String getenv(String name) {
        return getenv(name, envVars);
    }

    static String getenv(String name, Map<String, String> substitutions) {
        String value = substitutions.get(name);

        return StringUtils.isBlank(value) ?
                value : expandIfNecessary(value, substitutions);
    }

    static String expandIfNecessary(String input, Map<String, String> substitutions) {
        try {
            // Is the value a $VALUE - if so, we need to expand that too
            String pattern =
                    "\\$([A-Z_]+)"; // Note, this will not do any escaping (eg, $$FOO will get resolved as $BAR rather than leaving it unresolved)
            Pattern expr = Pattern.compile(pattern);
            Matcher matcher = expr.matcher(input);
            while (matcher.find()) {
                String envValue = substitutions.get(matcher.group(1).toUpperCase());
                if (envValue != null) {
                    envValue = envValue.replace("\\", "\\\\");
                    Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
                    input = subexpr.matcher(input).replaceAll(envValue);
                }
            }

            return input;
        } catch (Throwable t) {
            // Just in case anything went wrong, return the original string
            return input;
        }
    }
}
