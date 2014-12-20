/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.validator.routines;

import java.io.Serializable;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * <p>Perform email validations.</p>
 * <p>
 * This class is a Singleton; you can retrieve the instance via the getInstance() method.
 * </p>
 * <p>
 * Based on a script by <a href="mailto:stamhankar@hotmail.com">Sandeep V. Tamhankar</a>
 * http://javascript.internet.com
 * </p>
 * <p>
 * This implementation is not guaranteed to catch all possible errors in an email address.
 * For example, an address like nobody@noplace.somedog will pass validator, even though there
 * is no TLD "somedog"
 * </p>.
 *
 * @version $Revision: 1227719 $ $Date: 2012-01-05 18:45:51 +0100 (Thu, 05 Jan 2012) $
 * @since Validator 1.4
 */
public class EmailValidator implements Serializable {

    private static final long serialVersionUID = 1705927040799295880L;

    private static final String SPECIAL_CHARS = "\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]";
    private static final String VALID_CHARS = "[^\\s" + SPECIAL_CHARS + "]";
    private static final String QUOTED_USER = "(\"[^\"]*\")";
    private static final String WORD = "((" + VALID_CHARS + "|')+|" + QUOTED_USER + ")";

    private static final String LEGAL_ASCII_REGEX = "^\\p{ASCII}+$";
    private static final String EMAIL_REGEX = "^\\s*?(.+)@(.+?)\\s*$";
    private static final String IP_DOMAIN_REGEX = "^\\[(.*)\\]$";
    private static final String USER_REGEX = "^\\s*" + WORD + "(\\." + WORD + ")*$";

    private static final RegExp MATCH_ASCII_PATTERN = RegExp.compile(LEGAL_ASCII_REGEX);
    private static final RegExp EMAIL_PATTERN = RegExp.compile(EMAIL_REGEX);
    private static final RegExp IP_DOMAIN_PATTERN = RegExp.compile(IP_DOMAIN_REGEX);
    private static final RegExp USER_PATTERN = RegExp.compile(USER_REGEX);

    private final boolean allowLocal;

    /**
     * Singleton instance of this class, which
     *  doesn't consider local addresses as valid.
     */
    private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator(false);

    /**
     * Singleton instance of this class, which does
     *  consider local addresses valid.
     */
    private static final EmailValidator EMAIL_VALIDATOR_WITH_LOCAL = new EmailValidator(true);

    /**
     * Returns the Singleton instance of this validator.
     *
     * @return singleton instance of this validator.
     */
    public static EmailValidator getInstance() {
        return EMAIL_VALIDATOR;
    }

    /**
     * Returns the Singleton instance of this validator,
     *  with local validation as required.
     *
     * @param allowLocal Should local addresses be considered valid?
     * @return singleton instance of this validator
     */
    public static EmailValidator getInstance(boolean allowLocal) {
        if(allowLocal) {
           return EMAIL_VALIDATOR_WITH_LOCAL;
        }
        return EMAIL_VALIDATOR;
    }

    /**
     * Protected constructor for subclasses to use.
     *
     * @param allowLocal Should local addresses be considered valid?
     */
    protected EmailValidator(boolean allowLocal) {
        super();
        this.allowLocal = allowLocal;
    }

    /**
     * <p>Checks if a field has a valid e-mail address.</p>
     *
     * @param email The value validation is being performed on.  A <code>null</code>
     *              value is considered invalid.
     * @return true if the email address is valid.
     */
    public boolean isValid(String email) {
        if (email == null) {
            return false;
        }

        MatchResult asciiMatcher = MATCH_ASCII_PATTERN.exec(email);
        if (asciiMatcher == null) {
            return false;
        }

        // Check the whole email address structure
        MatchResult emailMatcher = EMAIL_PATTERN.exec(email);
        if (emailMatcher == null) {
            return false;
        }

        if (email.endsWith(".")) {
            return false;
        }

        if (!isValidUser(emailMatcher.getGroup(1))) {
            return false;
        }

        if (!isValidDomain(emailMatcher.getGroup(2))) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the domain component of an email address is valid.
     *
     * @param domain being validated.
     * @return true if the email address's domain is valid.
     */
    protected boolean isValidDomain(String domain) {
        // see if domain is an IP address in brackets
    	MatchResult ipDomainMatcher = IP_DOMAIN_PATTERN.exec(domain);

        if (ipDomainMatcher != null) {
            InetAddressValidator inetAddressValidator =
                    InetAddressValidator.getInstance();
            return inetAddressValidator.isValid(ipDomainMatcher.getGroup(1));
        } else {
            // Domain is symbolic name
            DomainValidator domainValidator =
                    DomainValidator.getInstance(allowLocal);
            return domainValidator.isValid(domain);
        }
    }

    /**
     * Returns true if the user component of an email address is valid.
     *
     * @param user being validated
     * @return true if the user name is valid.
     */
    protected boolean isValidUser(String user) {
        return USER_PATTERN.exec(user) != null;
    }

}
