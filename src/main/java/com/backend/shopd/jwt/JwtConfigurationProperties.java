package com.backend.shopd.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfigurationProperties {
    
    private Signing signing = new Signing();
    private Token token = new Token();
    private Get get = new Get();
    private Refresh refresh = new Refresh();
    private Http http = new Http();
    
    public Signing getSigning() {
        return signing;
    }
    
    public void setSigning(Signing signing) {
        this.signing = signing;
    }
    
    public Token getToken() {
        return token;
    }
    
    public void setToken(Token token) {
        this.token = token;
    }
    
    public Get getGet() {
        return get;
    }
    
    public void setGet(Get get) {
        this.get = get;
    }
    
    public Refresh getRefresh() {
        return refresh;
    }
    
    public void setRefresh(Refresh refresh) {
        this.refresh = refresh;
    }
    
    public Http getHttp() {
        return http;
    }
    
    public void setHttp(Http http) {
        this.http = http;
    }
    
    public static class Signing {
        private Key key = new Key();
        
        public Key getKey() {
            return key;
        }
        
        public void setKey(Key key) {
            this.key = key;
        }
        
        public static class Key {
            private String secret;
            
            public String getSecret() {
                return secret;
            }
            
            public void setSecret(String secret) {
                this.secret = secret;
            }
        }
    }
    
    public static class Token {
        private Expiration expiration = new Expiration();
        private String uri;
        
        public Expiration getExpiration() {
            return expiration;
        }
        
        public void setExpiration(Expiration expiration) {
            this.expiration = expiration;
        }
        
        public String getUri() {
            return uri;
        }
        
        public void setUri(String uri) {
            this.uri = uri;
        }
        
        public static class Expiration {
            private In in = new In();
            
            public In getIn() {
                return in;
            }
            
            public void setIn(In in) {
                this.in = in;
            }
            
            public static class In {
                private Long seconds;
                
                public Long getSeconds() {
                    return seconds;
                }
                
                public void setSeconds(Long seconds) {
                    this.seconds = seconds;
                }
            }
        }
    }
    
    public static class Get {
        private Token token = new Token();
        
        public Token getToken() {
            return token;
        }
        
        public void setToken(Token token) {
            this.token = token;
        }
        
        public static class Token {
            private String uri;
            
            public String getUri() {
                return uri;
            }
            
            public void setUri(String uri) {
                this.uri = uri;
            }
        }
    }
    
    public static class Refresh {
        private Token token = new Token();
        
        public Token getToken() {
            return token;
        }
        
        public void setToken(Token token) {
            this.token = token;
        }
        
        public static class Token {
            private String uri;
            
            public String getUri() {
                return uri;
            }
            
            public void setUri(String uri) {
                this.uri = uri;
            }
        }
    }
    
    public static class Http {
        private Request request = new Request();
        
        public Request getRequest() {
            return request;
        }
        
        public void setRequest(Request request) {
            this.request = request;
        }
        
        public static class Request {
            private String header;
            
            public String getHeader() {
                return header;
            }
            
            public void setHeader(String header) {
                this.header = header;
            }
        }
    }
}