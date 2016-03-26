/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014-2015 ForgeRock AS.
 */

package org.forgerock.openam.oauth2;

import static org.forgerock.oauth2.core.OAuth2Constants.CoreTokenParams.AUDIT_TRACKING_ID;
import static org.forgerock.oauth2.core.Utils.*;

import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Set;
import org.forgerock.json.JsonValue;
import org.forgerock.oauth2.core.OAuth2Constants;
import org.forgerock.oauth2.core.RefreshToken;
import org.forgerock.oauth2.core.exceptions.InvalidGrantException;

/**
 * Models a OpenAM OAuth2 Refresh Token.
 *
 * @since 12.0.0
 */
public class OpenAMRefreshToken extends RefreshToken {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("OAuth2CoreToken");

    /**
     * Constructs a new OpenAMRefreshToken backed with the data in the specified JsonValue.
     *
     * @param token The JsonValue of the token.
     * @throws InvalidGrantException If the given token is not a Refresh Token.
     */
    public OpenAMRefreshToken(JsonValue token) throws InvalidGrantException {
        super(token);
    }

    /**
     * Constructs a new OpenAMRefreshToken.
     *
     * @param id The token id.
     * @param resourceOwnerId The resource owner's id.
     * @param clientId The client's id.
     * @param redirectUri The redirect uri.
     * @param scope The scope.
     * @param expiryTime The expiry time.
     * @param tokenType The token type.
     * @param tokenName The token name.
     * @param grantType The grant type.
     * @param authModules The pipe-separated list of auth modules.
     * @param realm The realm.
     * @param auditId The audit id, used for tracking tokens throughout the audit logs.
     */
    public OpenAMRefreshToken(String id, String resourceOwnerId, String clientId, String redirectUri, Set<String> scope,
            long expiryTime, String tokenType, String tokenName, String grantType, String realm,
            String authModules, String acr, String auditId, String ssoTokenId) {
        super(id, resourceOwnerId, clientId, redirectUri, scope, expiryTime, tokenType, tokenName, grantType,
                authModules, acr);
        setRealm(realm);
        setAuditId(auditId);
        setSsoTokenId(ssoTokenId);
    }

    /**
     * Sets the requested claims.
     *
     * @param claims Requested claims
     */
    public void setClaims(String claims) {
        setStringProperty(OAuth2Constants.Custom.CLAIMS, claims);
    }

    /**
     * Gets the requested claims.
     *
     * @return The claims.
     */
    public String getClaims() {
        return getStringProperty(OAuth2Constants.Custom.CLAIMS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setScope(Set<String> scope) {
        put(OAuth2Constants.CoreTokenParams.SCOPE, scope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setExpiryTime(long expiryTime) {
        put(OAuth2Constants.CoreTokenParams.EXPIRE_TIME, stringToSet(String.valueOf(expiryTime)));
    }

    /**
     * Sets the realm.
     *
     * @param realm The realm.
     */
    private void setRealm(String realm) {
        if (realm == null || realm.isEmpty()) {
            this.setStringProperty(OAuth2Constants.CoreTokenParams.REALM, "/");
        } else {
            this.setStringProperty(OAuth2Constants.CoreTokenParams.REALM, realm);
        }
    }

    /**
     * Gets the realm.
     */
    public String getRealm() {
        return this.getStringProperty(OAuth2Constants.CoreTokenParams.REALM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getResourceString(String s) {
        return RESOURCE_BUNDLE.getString(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getScope() {
        final Set<String> value = getParameter(OAuth2Constants.CoreTokenParams.SCOPE);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getExpiryTime() {
        final Set<String> value = getParameter(OAuth2Constants.CoreTokenParams.EXPIRE_TIME);
        if (value != null && !value.isEmpty()) {
            return Long.parseLong(value.iterator().next());
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setStringProperty(String key, String value) {
        put(key, stringToSet(value));
    }

    @Override
    protected String getStringProperty(String key) {
        final Set<String> value = getParameter(key);
        if (value != null && !value.isEmpty()) {
            return value.iterator().next();
        }
        return null;

    }

    /**
     * Gets the specified parameter from the JsonValue.
     *
     * @param paramName The parameter name.
     * @return A {@code Set} of the parameter values.
     */
    private Set<String> getParameter(String paramName) {
        final JsonValue param = get(paramName);
        if (param != null) {
            return (Set<String>) param.getObject();
        }
        return null;
    }

    /**
     * Sets the audit id.
     *
     * @param auditId The audit id.
     */
    protected void setAuditId(String auditId) {
        setStringProperty(AUDIT_TRACKING_ID, auditId);
    }

    /**
     * Sets the token id of the session.
     *
     *  @param ssoTokenId The token id of the session.
     */
    private void setSsoTokenId(String ssoTokenId) {
        setStringProperty(OAuth2Constants.Custom.SSO_TOKEN_ID, ssoTokenId);
    }
    
    public String getSsoTokenId() {
        return getStringProperty(OAuth2Constants.Custom.SSO_TOKEN_ID);
    }

    /**
     * Gets the audit id.
     *
     * @return The audit id.
     */
    public String getAuditId() {
        return getStringProperty(AUDIT_TRACKING_ID);
    }
}
