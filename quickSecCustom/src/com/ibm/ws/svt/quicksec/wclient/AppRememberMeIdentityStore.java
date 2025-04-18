
/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.svt.quicksec.wclient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.CallerPrincipal;
import jakarta.security.enterprise.credential.RememberMeCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.RememberMeIdentityStore;

//FOR TESTING ONLY!!! NEVER DO THIS FROM A REAL REMEMBERME IDENTITY STORE. IT NEEDS TOKEN SIGNATURE AND EXPIRATION POLICY.
@ApplicationScoped
public class AppRememberMeIdentityStore implements RememberMeIdentityStore {

    private Map<String, CredentialValidationResult> tokenCache = new HashMap<String, CredentialValidationResult>();

    @Override
    public String generateLoginToken(CallerPrincipal callerPrincipal, Set<String> groups) {
        String token = null;
        try {
            token = createToken(callerPrincipal, groups);
            String tokenHash = getTokenHash(token);
            CredentialValidationResult result = new CredentialValidationResult(callerPrincipal, groups);
            tokenCache.put(tokenHash, result);
            System.out.println("Inside the generateLoginToken" + token);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return token;
    }

    private String createToken(CallerPrincipal callerPrincipal, Set<String> groups) throws NoSuchAlgorithmException {
        String token;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(callerPrincipal.getName().getBytes());

        for (String group : groups) {
            md.update(group.getBytes());
        }

        byte[] digestBytes = md.digest();
        token = Base64.getEncoder().encodeToString(digestBytes);
        System.out.println("Inside the createToken" + token);
        return token;
    }

    private String getTokenHash(String token) throws NoSuchAlgorithmException {
        MessageDigest tokenHashMessageDigest = MessageDigest.getInstance("SHA-512");
        return new String(tokenHashMessageDigest.digest(token.getBytes()));
    }

    @Override
    public void removeLoginToken(String token) {
        tokenCache.remove(token);
    }

    @Override
    public CredentialValidationResult validate(RememberMeCredential credential) {
        CredentialValidationResult result = null;
        try {
            result = tokenCache.get(getTokenHash(credential.getToken()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        if (result == null) {
            result = CredentialValidationResult.INVALID_RESULT;
        }
        System.out.println("Inside the validate" + result);
        return result;
    }
}
