package studip_uni_passau.femtopedia.de.unipassaustudip.api;

public class OAuthData {

    public String accessToken;
    public String accessTokenSecret;

    public OAuthData(String accessToken, String accessTokenSecret) {
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
    }

}
