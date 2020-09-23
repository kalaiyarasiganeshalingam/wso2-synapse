/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.endpoints.oauth;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.resolvers.ResolverFactory;

import javax.xml.namespace.QName;

/**
 * Helper class to build OAuth handlers using the synapse configuration of the endpoints
 */
public class OAuthUtils {

    private static final Log log = LogFactory.getLog(OAuthUtils.class);

    /**
     * This method will return an OAuthHandler instance depending on the oauth configs
     *
     * @param httpElement Element containing http configs
     * @return OAuthHandler object
     * @throws OAuthException throw exception for invalid oauth configs
     */
    public static OAuthHandler getOAuthHandler(OMElement httpElement) throws OAuthException {

        if (httpElement != null) {
            OMElement authElement = httpElement.getFirstChildWithName(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "authentication"));

            if (authElement != null) {
                OMElement oauthElement = authElement.getFirstChildWithName(
                        new QName(SynapseConstants.SYNAPSE_NAMESPACE, "oauth"));

                if (oauthElement != null) {

                    OAuthHandler oAuthHandler = getSpecificOAuthHandler(oauthElement);
                    if (oAuthHandler != null) {
                        return oAuthHandler;
                    } else {
                        throw new OAuthException("Invalid OAuth configuration");
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method will return an OAuthHandler instance depending on the oauth configs
     *
     * @param oauthElement Element containing OAuth configs
     * @return OAuthHandler object
     */
    private static OAuthHandler getSpecificOAuthHandler(OMElement oauthElement) {

        OAuthHandler oAuthHandler = null;

        OMElement authCodeElement = oauthElement.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE, "authorizationCode"));

        OMElement clientCredentialsElement = oauthElement.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE, "clientCredentials"));

        if (authCodeElement != null && clientCredentialsElement != null) {
            if (log.isDebugEnabled()) {
                log.error("Invalid OAuth configuration: AuthorizationCode and ClientCredentials grants are not " +
                        "allowed together");
            }
            return null;
        }

        if (authCodeElement != null) {
            oAuthHandler = getAuthorizationCodeHandler(authCodeElement);
        }

        if (clientCredentialsElement != null) {
            oAuthHandler = getClientCredentialsHandler(clientCredentialsElement);
        }
        return oAuthHandler;
    }

    /**
     * Method to get a AuthorizationCodeHandler
     *
     * @param authCodeElement Element containing authorization code configs
     * @return AuthorizationCodeHandler object
     */
    private static AuthorizationCodeHandler getAuthorizationCodeHandler(OMElement authCodeElement) {

        String clientId = getChildValue(authCodeElement, OAuthConstants.OAUTH_CLIENT_ID);
        String clientSecret = getChildValue(authCodeElement, OAuthConstants.OAUTH_CLIENT_SECRET);
        String refreshToken = getChildValue(authCodeElement, OAuthConstants.OAUTH_REFRESH_TOKEN);
        String tokenApiUrl = getChildValue(authCodeElement, OAuthConstants.TOKEN_API_URL);

        if (clientId == null || clientSecret == null || refreshToken == null || tokenApiUrl == null) {
            if (log.isDebugEnabled()) {
                log.error("Invalid AuthorizationCode configuration");
            }
            return null;
        }

        return new AuthorizationCodeHandler(getRandomOAuthHandlerID(), tokenApiUrl, clientId,
                clientSecret,
                refreshToken);

    }

    /**
     * Method to get a ClientCredentialsHandler
     *
     * @param clientCredentialsElement Element containing client credentials configs
     * @return ClientCredentialsHandler object
     */
    private static ClientCredentialsHandler getClientCredentialsHandler(
            OMElement clientCredentialsElement) {

        String clientId = getChildValue(clientCredentialsElement, OAuthConstants.OAUTH_CLIENT_ID);
        String clientSecret = getChildValue(clientCredentialsElement, OAuthConstants.OAUTH_CLIENT_SECRET);
        String tokenApiUrl = getChildValue(clientCredentialsElement, OAuthConstants.TOKEN_API_URL);

        if (clientId == null || clientSecret == null || tokenApiUrl == null) {
            if (log.isDebugEnabled()) {
                log.error("Invalid ClientCredentials configuration");
            }
            return null;
        }

        return new ClientCredentialsHandler(getRandomOAuthHandlerID(), tokenApiUrl, clientId,
                clientSecret);

    }

    /**
     * Method to get the value inside a child element
     *
     * @param parentElement Parent OMElement
     * @param childName     name of the child
     * @return String containing the value of the child
     */
    private static String getChildValue(OMElement parentElement, String childName) {

        OMElement clientIdElement = parentElement.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE, childName));

        if (clientIdElement != null && clientIdElement.getText() != null) {
            return ResolverFactory.getInstance().getResolver(clientIdElement.getText().trim()).resolve();
        }
        return null;
    }

    /**
     * Method to generate a random id for each OAuth handler
     *
     * @return String containing random id
     */
    private static String getRandomOAuthHandlerID() {

        String uuid = UIDGenerator.generateUID();
        return OAuthConstants.OAUTH_PREFIX + uuid;
    }

}
