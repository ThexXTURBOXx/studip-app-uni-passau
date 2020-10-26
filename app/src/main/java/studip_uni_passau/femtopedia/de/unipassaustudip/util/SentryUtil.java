package studip_uni_passau.femtopedia.de.unipassaustudip.util;


import io.sentry.Sentry;

public class SentryUtil {

    public static void logError(Throwable t) {
        Sentry.captureException(t);
    }

    public static void logError(Throwable t, Object hint) {
        Sentry.captureException(t, hint);
    }

}
