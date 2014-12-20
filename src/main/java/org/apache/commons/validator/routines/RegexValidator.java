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
import java.util.regex.Pattern;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * <b>Regular Expression</b> validation (using JDK 1.4+ regex support).
 * <p>
 * Construct the validator either for a single regular expression or a set (array) of
 * regular expressions. By default validation is <i>case sensitive</i> but constructors
 * are provided to allow  <i>case in-sensitive</i> validation. For example to create
 * a validator which does <i>case in-sensitive</i> validation for a set of regular
 * expressions:
 * <pre>
 *         String[] regexs = new String[] {...};
 *         RegexValidator validator = new RegexValidator(regexs, false);
 * </pre>
 * <p>
 * <ul>
 *   <li>Validate <code>true</code> or <code>false</code>:</li>
 *   <ul>
 *     <li><code>boolean valid = validator.isValid(value);</code></li>
 *   </ul>
 *   <li>Validate returning an aggregated String of the matched groups:</li>
 *   <ul>
 *     <li><code>String result = validator.validate(value);</code></li>
 *   </ul>
 *   <li>Validate returning the matched groups:</li>
 *   <ul>
 *     <li><code>String[] result = validator.match(value);</code></li>
 *   </ul>
 * </ul>
 * <p>
 * Cached instances pre-compile and re-use {@link Pattern}(s) - which according
 * to the {@link Pattern} API are safe to use in a multi-threaded environment.
 *
 * @version $Revision: 1227719 $ $Date: 2012-01-05 18:45:51 +0100 (Thu, 05 Jan 2012) $
 * @since Validator 1.4
 */
public class RegexValidator implements Serializable {

    private static final long serialVersionUID = -8832409930574867162L;

    private final RegExp[] patterns;

    /**
     * Construct a <i>case sensitive</i> validator for a single
     * regular expression.
     *
     * @param regex The regular expression this validator will
     * validate against
     */
    public RegexValidator(String regex) {
        this(regex, true);
    }

    /**
     * Construct a validator for a single regular expression
     * with the specified case sensitivity.
     *
     * @param regex The regular expression this validator will
     * validate against
     * @param caseSensitive when <code>true</code> matching is <i>case
     * sensitive</i>, otherwise matching is <i>case in-sensitive</i>
     */
    public RegexValidator(String regex, boolean caseSensitive) {
        this(new String[] {regex}, caseSensitive);
    }

    /**
     * Construct a <i>case sensitive</i> validator that matches any one
     * of the set of regular expressions.
     *
     * @param regexs The set of regular expressions this validator will
     * validate against
     */
    public RegexValidator(String[] regexs) {
        this(regexs, true);
    }

    /**
     * Construct a validator that matches any one of the set of regular
     * expressions with the specified case sensitivity.
     *
     * @param regexs The set of regular expressions this validator will
     * validate against
     * @param caseSensitive when <code>true</code> matching is <i>case
     * sensitive</i>, otherwise matching is <i>case in-sensitive</i>
     */
    public RegexValidator(String[] regexs, boolean caseSensitive) {
        if (regexs == null || regexs.length == 0) {
            throw new IllegalArgumentException("Regular expressions are missing");
        }
        patterns = new RegExp[regexs.length];
        for (int i = 0; i < regexs.length; i++) {
            if (regexs[i] == null || regexs[i].length() == 0) {
                throw new IllegalArgumentException("Regular expression[" + i + "] is missing");
            }
            if (caseSensitive) {
            	patterns[i] =  RegExp.compile(regexs[i]);
            } else {
            	patterns[i] =  RegExp.compile(regexs[i], "i");
            }
        }
    }

    /**
     * Validate a value against the set of regular expressions.
     *
     * @param value The value to validate.
     * @return <code>true</code> if the value is valid
     * otherwise <code>false</code>.
     */
    public boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        for (int i = 0; i < patterns.length; i++) {
        	final MatchResult result = patterns[i].exec(value);
        	// if (result != null && result.getGroupCount() == 1 && value.equals(result.getGroup(0))) {
        	if (result != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate a value against the set of regular expressions
     * returning the array of matched groups.
     *
     * @param value The value to validate.
     * @return String array of the <i>groups</i> matched if
     * valid or <code>null</code> if invalid
     */
    public String[] match(String value) {
        if (value == null) {
            return null;
        }
        for (int i = 0; i < patterns.length; i++) {
        	MatchResult matcher = patterns[i].exec(value);
            if (matcher != null) {
                int count = matcher.getGroupCount() - 1;
                String[] groups = new String[count];
                for (int j = 0; j < count; j++) {
                    groups[j] = matcher.getGroup(j+1);
                }
                return groups;
            }
        }
        return null;
    }


    /**
     * Validate a value against the set of regular expressions
     * returning a String value of the aggregated groups.
     *
     * @param value The value to validate.
     * @return Aggregated String value comprised of the
     * <i>groups</i> matched if valid or <code>null</code> if invalid
     */
    public String validate(String value) {
        if (value == null) {
            return null;
        }
        for (int i = 0; i < patterns.length; i++) {
        	MatchResult matcher = patterns[i].exec(value);
            if (matcher != null) {
                int count = matcher.getGroupCount() - 1;
                if (count == 1) {
                    return matcher.getGroup(1);
                }
                StringBuffer buffer = new StringBuffer();
                for (int j = 0; j < count; j++) {
                    String component = matcher.getGroup(j+1);
                    if (component != null) {
                        buffer.append(component);
                    }
                }
                return buffer.toString();
            }
        }
        return null;
    }

    /**
     * Provide a String representation of this validator.
     * @return A String representation of this validator
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("RegexValidator{");
        for (int i = 0; i < patterns.length; i++) {
            if (i > 0) {
                buffer.append(",");
            }
            buffer.append(patterns[i].getSource());
        }
        buffer.append("}");
        return buffer.toString();
    }

}
