package studip_uni_passau.femtopedia.de.unipassaustudip;

public class OAuthData {

    public static class OAuthAccessData {
        public String accessToken;
        public String verifier;

        public OAuthAccessData(String accessToken, String verifier) {
            this.accessToken = accessToken;
            this.verifier = verifier;
        }

    }

    public static class OAuthSaveData {
        public String accessToken;
        public String accessTokenSecret;

        public OAuthSaveData(String accessToken, String accessTokenSecret) {
            this.accessToken = accessToken;
            this.accessTokenSecret = accessTokenSecret;
        }

    }

}
