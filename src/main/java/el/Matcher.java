package el;


import util.Patterns;
import util.Strings;

public class Matcher {

    public static boolean matches(String pattern, String string) {
        if (Strings.isNotBlank(string)) {
            return Patterns.find(pattern, string);
        }

        return false;
    }

    private Matcher() {
    }
}
