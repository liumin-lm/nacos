package com.alibaba.nacos.console.service;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.auth.common.AuthConfigs;
import com.alibaba.nacos.common.http.HttpClientBeanHolder;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.config.server.utils.RequestUtil;
import com.alibaba.nacos.console.security.nacos.JwtTokenManager;
import com.alibaba.nacos.console.security.nacos.users.NacosUser;
import com.alibaba.nacos.console.utils.OidcUtil;
import com.alibaba.nacos.core.utils.Loggers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

/**
 * Service for OIDC.
 *
 * @author Kicey
 */
@Service
public class OidcService {
    
    private final NacosRestTemplate restTemplate = HttpClientBeanHolder.getNacosRestTemplate(Loggers.AUTH);
    
    @Autowired
    private JwtTokenManager tokenManager;
    
    @Autowired
    private AuthConfigs authConfigs;
    
    /**
     * Get OIDC authorize url with params.
     *
     * @param authUrl     OIDC authorize url
     * @param clientId    client id
     * @param redirectUrl redirect url
     * @param scopes      scopes, will be joined by ' '
     * @param stateBase64 state base64 encoded
     * @return the complete authorize url in String
     */
    public String completeAuthUriWithParameters(String authUrl, String clientId, String redirectUrl,
            List<String> scopes, String stateBase64) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(authUrl);
        uriBuilder.queryParam(OidcUtil.RESPONSE_TYPE, OidcUtil.CODE);
        uriBuilder.queryParam(OidcUtil.CLIENT_ID, clientId);
        uriBuilder.queryParam(OidcUtil.REDIRECT_URI, redirectUrl);
        
        StringBuilder scopesString = new StringBuilder();
        // separate scopes by ' ' in default
        if (scopes != null && scopes.size() > 0) {
            scopes.forEach(scope -> scopesString.append(scope).append(" "));
        }
        if (scopes != null && scopes.size() > 0) {
            uriBuilder.queryParam(OidcUtil.SCOPE, scopesString.toString());
        }
        uriBuilder.queryParam(OidcUtil.STATE, stateBase64);
        return uriBuilder.encode().toUriString();
    }
    
    /**
     * Get the access token by code.
     *
     * @param oidp the oidp key
     * @param code the authorization code
     * @return the result including access token
     * @throws SocketTimeoutException   when timeout
     * @throws IllegalArgumentException fail to get access token because of content returned by the oidp
     * @throws Exception                other exception
     */
    public HttpRestResult<String> exchangeTokenWithCodeThroughPostForm(String oidp, String code)
            throws SocketTimeoutException, IllegalArgumentException, Exception {
        String completeExchangeTokenUrl = OidcUtil.getCompletedExchangeTokenUrl(oidp);
        Header tokenHeader = OidcUtil.getExchangeTokenHeader();
        Map<String, String> params = OidcUtil.getExchangeTokenParams(oidp, code);
        return restTemplate.postForm(completeExchangeTokenUrl, tokenHeader, params, String.class);
    }
    
    /**
     * get the userinfo with access token.
     *
     * @param oidp oidp key
     * @param accessToken the access token returned after the user interact with the oidp
     * @return the result including the user info
     * @throws SocketTimeoutException when timeout
     * @throws IllegalArgumentException fail to get userinfo because of content returned by the oidp
     * @throws Exception other exception
     */
    public HttpRestResult<String> getUserinfoWithAccessToken(String oidp, String accessToken)
            throws IllegalArgumentException, Exception {
        String userInfoUrl = OidcUtil.getCompletedUserinfoUrl(oidp);
        Header userInfoHeader = OidcUtil.getHeaderWithAccessToken(accessToken);
        return restTemplate.get(userInfoUrl, userInfoHeader, null, String.class);
    }
    
    /**
     * user jsonpath to get the username.
     *
     * @param userinfo the userinfo in json form returned by the oidp
     * @return the username from the userinfo returned by the oidp
     */
    public static String getUsernameFromUserinfo(String oidp, String userinfo) {
        String jsonpath = OidcUtil.getJsonpath(oidp);
        return JsonPath.read(userinfo, jsonpath);
    }
    
    /**
     * user username to create a Nacos internal token for the user.
     * @param username the username in Nacos
     * @param request request's session will be added a result
     * @return the Nacos internal token
     * @throws JsonProcessingException fail to map the result to a json as the token
     */
    public  String createNacosInternalToken(String username, HttpServletRequest request)
            throws JsonProcessingException {
        String token = tokenManager.createToken(username);
        Authentication authentication = tokenManager.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        NacosUser nacosUser = new NacosUser();
        nacosUser.setUserName(username);
        nacosUser.setToken(token);
        
        request.getSession().setAttribute(RequestUtil.NACOS_USER_KEY, nacosUser);
        
        ObjectNode result = JacksonUtils.createEmptyJsonNode();
        result.put(Constants.ACCESS_TOKEN, nacosUser.getToken());
        result.put(Constants.TOKEN_TTL, authConfigs.getTokenValidityInSeconds());
        result.put(Constants.GLOBAL_ADMIN, nacosUser.isGlobalAdmin());
        result.put(Constants.USERNAME, nacosUser.getUserName());
        
        return new ObjectMapper().writeValueAsString(result);
    }
    
}
