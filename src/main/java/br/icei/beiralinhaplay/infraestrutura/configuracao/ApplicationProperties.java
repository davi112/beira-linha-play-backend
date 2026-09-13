package br.icei.beiralinhaplay.infraestrutura.configuracao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private String allowedHosts;
    private Jwt jwt = new Jwt();
    private Cookie cookie = new Cookie();

    public String getAllowedHosts() {
        return allowedHosts;
    }

    public void setAllowedHosts(String allowedHosts) {
        this.allowedHosts = allowedHosts;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    public static class Jwt {
        private String secret;
        private long accessTokenMinutos = 15;
        private long refreshTokenDias = 7;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessTokenMinutos() {
            return accessTokenMinutos;
        }

        public void setAccessTokenMinutos(long accessTokenMinutos) {
            this.accessTokenMinutos = accessTokenMinutos;
        }

        public long getRefreshTokenDias() {
            return refreshTokenDias;
        }

        public void setRefreshTokenDias(long refreshTokenDias) {
            this.refreshTokenDias = refreshTokenDias;
        }
    }

    public static class Cookie {
        private boolean secure;
        private String sameSite;
        private String accessName;
        private String refreshName;

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }

        public String getSameSite() {
            return sameSite;
        }

        public void setSameSite(String sameSite) {
            this.sameSite = sameSite;
        }

        public String getAccessName() {
            return accessName;
        }

        public void setAccessName(String accessName) {
            this.accessName = accessName;
        }

        public String getRefreshName() {
            return refreshName;
        }

        public void setRefreshName(String refreshName) {
            this.refreshName = refreshName;
        }
    }
}
