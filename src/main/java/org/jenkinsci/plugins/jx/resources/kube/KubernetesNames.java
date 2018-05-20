package org.jenkinsci.plugins.jx.resources.kube;

/**
 */
public class KubernetesNames {
    protected static final char DIGIT_PREFIX = 'n';

    /**
     * Lets convert the string to btw a valid kubernetes resource name
     *
     * @return the converted name
     * @param text the text to convert
     * @param allowDots whether or not to allow dots in the name
     */
    public static String convertToKubernetesName(String text, boolean allowDots) {
        String lower = text.toLowerCase();
        StringBuilder builder = new StringBuilder();
        boolean started = false;
        char lastCh = ' ';
        for (int i = 0, last = lower.length() - 1; i <= last; i++) {
            char ch = lower.charAt(i);
            boolean digit = ch >= '0' && ch <= '9';
            // names cannot start with a digit so lets add a prefix
            if (digit && builder.length() == 0) {
                builder.append(DIGIT_PREFIX);
            }
            if (!(ch >= 'a' && ch <= 'z') && !digit) {
                if (ch == '/') {
                    ch = '.';
                } else if (ch != '.' && ch != '-') {
                    ch = '-';
                }
                if (!allowDots && ch == '.') {
                    ch = '-';
                }
                if (!started || lastCh == '-' || lastCh == '.' || i == last) {
                    continue;
                }
            }
            builder.append(ch);
            started = true;
            lastCh = ch;
        }
        return builder.toString();
    }
}
