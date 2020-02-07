package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import io.sentry.core.Sentry;
import io.sentry.core.protocol.User;

public class SentryUtil {

    public static void setUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        Sentry.setUser(user);
    }

    public static void logError(Throwable t) {
        Sentry.captureException(t);
    }

    public static void logError(Throwable t, Object hint) {
        Sentry.captureException(t, hint);
    }

}
