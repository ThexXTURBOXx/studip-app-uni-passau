package studip_uni_passau.femtopedia.de.unipassaustudip.api;

import de.femtopedia.studip.shib.Pair;

public class OAuthData {

    public String accessToken;
    public String accessTokenSecret;

    public OAuthData(Pair<String, String> tokenPair) {
        this(tokenPair.getKey(), tokenPair.getValue());
    }

    public OAuthData(String accessToken, String accessTokenSecret) {
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
    }

}
