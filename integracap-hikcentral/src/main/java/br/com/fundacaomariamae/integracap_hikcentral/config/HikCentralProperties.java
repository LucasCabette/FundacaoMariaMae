package br.com.fundacaomariamae.integracap_hikcentral.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hikcentral.integracao")
public class HikCentralProperties {
    private boolean enabled;
    private String baseUrl = "";
    private String apiKey = "";
    private String apiSecret = "";
    private String orgIndexCode = "";
    private String privilegeGroupId = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
    public String getOrgIndexCode() { return orgIndexCode; }
    public void setOrgIndexCode(String orgIndexCode) { this.orgIndexCode = orgIndexCode; }
    public String getPrivilegeGroupId() { return privilegeGroupId; }
    public void setPrivilegeGroupId(String privilegeGroupId) { this.privilegeGroupId = privilegeGroupId; }
}
