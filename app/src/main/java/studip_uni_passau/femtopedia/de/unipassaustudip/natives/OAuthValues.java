package studip_uni_passau.femtopedia.de.unipassaustudip.natives;

public class OAuthValues {

    static {
        System.loadLibrary("oauth-values");
    }

    public static native String getConsumerKey();

    public static native String getConsumerSecret();

}
