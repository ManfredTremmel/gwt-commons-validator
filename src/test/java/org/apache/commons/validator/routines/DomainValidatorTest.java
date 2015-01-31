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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Tests for the DomainValidator.
 *
 * @version $Revision: 1649720 $
 */
public class DomainValidatorTest extends TestCase {

    private DomainValidator validator;

    public void setUp() {
        validator = DomainValidator.getInstance();
    }

    public void testValidDomains() {
        assertTrue("apache.org should validate", validator.isValid("apache.org"));
        assertTrue("www.google.com should validate", validator.isValid("www.google.com"));

        assertTrue("test-domain.com should validate", validator.isValid("test-domain.com"));
        assertTrue("test---domain.com should validate", validator.isValid("test---domain.com"));
        assertTrue("test-d-o-m-ain.com should validate", validator.isValid("test-d-o-m-ain.com"));
        assertTrue("two-letter domain label should validate", validator.isValid("as.uk"));

        assertTrue("case-insensitive ApAchE.Org should validate", validator.isValid("ApAchE.Org"));

        assertTrue("single-character domain label should validate", validator.isValid("z.com"));

        assertTrue("i.have.an-example.domain.name should validate", validator.isValid("i.have.an-example.domain.name"));
    }

    public void testInvalidDomains() {
        assertFalse("bare TLD .org shouldn't validate", validator.isValid(".org"));
        assertFalse("domain name with spaces shouldn't validate", validator.isValid(" apache.org "));
        assertFalse("domain name containing spaces shouldn't validate", validator.isValid("apa che.org"));
        assertFalse("domain name starting with dash shouldn't validate", validator.isValid("-testdomain.name"));
        assertFalse("domain name ending with dash shouldn't validate", validator.isValid("testdomain-.name"));
        assertFalse("domain name starting with multiple dashes shouldn't validate", validator.isValid("---c.com"));
        assertFalse("domain name ending with multiple dashes shouldn't validate", validator.isValid("c--.com"));
        assertFalse("domain name with invalid TLD shouldn't validate", validator.isValid("apache.rog"));

        assertFalse("URL shouldn't validate", validator.isValid("http://www.apache.org"));
        assertFalse("Empty string shouldn't validate as domain name", validator.isValid(" "));
        assertFalse("Null shouldn't validate as domain name", validator.isValid(null));
    }

    public void testTopLevelDomains() {
        // infrastructure TLDs
        assertTrue(".arpa should validate as iTLD", validator.isValidInfrastructureTld(".arpa"));
        assertFalse(".com shouldn't validate as iTLD", validator.isValidInfrastructureTld(".com"));

        // generic TLDs
        assertTrue(".name should validate as gTLD", validator.isValidGenericTld(".name"));
        assertFalse(".us shouldn't validate as gTLD", validator.isValidGenericTld(".us"));

        // country code TLDs
        assertTrue(".uk should validate as ccTLD", validator.isValidCountryCodeTld(".uk"));
        assertFalse(".org shouldn't validate as ccTLD", validator.isValidCountryCodeTld(".org"));

        // case-insensitive
        assertTrue(".COM should validate as TLD", validator.isValidTld(".COM"));
        assertTrue(".BiZ should validate as TLD", validator.isValidTld(".BiZ"));

        // corner cases
        assertFalse("invalid TLD shouldn't validate", validator.isValid(".nope")); // TODO this is not guaranteed invalid forever
        assertFalse("empty string shouldn't validate as TLD", validator.isValid(""));
        assertFalse("null shouldn't validate as TLD", validator.isValid(null));
    }

    public void testAllowLocal() {
       DomainValidator noLocal = DomainValidator.getInstance(false);
       DomainValidator allowLocal = DomainValidator.getInstance(true);

       // Default is false, and should use singletons
       assertEquals(noLocal, validator);

       // Default won't allow local
       assertFalse("localhost.localdomain should validate", noLocal.isValid("localhost.localdomain"));
       assertFalse("localhost should validate", noLocal.isValid("localhost"));

       // But it may be requested
       assertTrue("localhost.localdomain should validate", allowLocal.isValid("localhost.localdomain"));
       assertTrue("localhost should validate", allowLocal.isValid("localhost"));
       assertTrue("hostname should validate", allowLocal.isValid("hostname"));
       assertTrue("machinename should validate", allowLocal.isValid("machinename"));

       // Check the localhost one with a few others
       assertTrue("apache.org should validate", allowLocal.isValid("apache.org"));
       assertFalse("domain name with spaces shouldn't validate", allowLocal.isValid(" apache.org "));
    }

    public void testIDN() {
       assertTrue("b\u00fccher.ch in IDN should validate", validator.isValid("www.xn--bcher-kva.ch"));
    }

    public void testIDNJava6OrLater() {
        String version = System.getProperty("java.version");
        if (version.compareTo("1.6") < 0) {
            System.out.println("Cannot run Unicode IDN tests");
            return; // Cannot run the test
        } // xn--d1abbgf6aiiy.xn--p1ai http://президент.рф
       assertTrue("b\u00fccher.ch should validate", validator.isValid("www.b\u00fccher.ch"));
       assertTrue("xn--d1abbgf6aiiy.xn--p1ai should validate", validator.isValid("xn--d1abbgf6aiiy.xn--p1ai"));
       assertTrue("президент.рф should validate", validator.isValid("президент.рф"));
       assertFalse("www.\uFFFD.ch FFFD should fail", validator.isValid("www.\uFFFD.ch"));
    }

    // RFC2396: domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
    public void testRFC2396domainlabel() { // use fixed valid TLD
        assertTrue("a.ch should validate", validator.isValid("a.ch"));
        assertTrue("9.ch should validate", validator.isValid("9.ch"));
        assertTrue("az.ch should validate", validator.isValid("az.ch"));
        assertTrue("09.ch should validate", validator.isValid("09.ch"));
        assertTrue("9-1.ch should validate", validator.isValid("9-1.ch"));
        assertFalse("91-.ch should not validate", validator.isValid("91-.ch"));
        assertFalse("-.ch should not validate", validator.isValid("-.ch"));
    }

    // RFC2396 toplabel = alpha | alpha *( alphanum | "-" ) alphanum
    public void testRFC2396toplabel() {
        // These tests use non-existent TLDs so currently need to use a package protected method
        assertTrue("a.c (alpha) should validate", validator.isValidDomainSyntax("a.c"));
        assertTrue("a.cc (alpha alpha) should validate", validator.isValidDomainSyntax("a.cc"));
        assertTrue("a.c9 (alpha alphanum) should validate", validator.isValidDomainSyntax("a.c9"));
        assertTrue("a.c-9 (alpha - alphanum) should validate", validator.isValidDomainSyntax("a.c-9"));
        assertTrue("a.c-z (alpha - alpha) should validate", validator.isValidDomainSyntax("a.c-z"));

        assertFalse("a.9c (alphanum alpha) should fail", validator.isValidDomainSyntax("a.9c"));
        assertFalse("a.c- (alpha -) should fail", validator.isValidDomainSyntax("a.c-"));
        assertFalse("a.- (-) should fail", validator.isValidDomainSyntax("a.-"));
        assertFalse("a.-9 (- alphanum) should fail", validator.isValidDomainSyntax("a.-9"));
    }

    public void testDomainNoDots() {// rfc1123
        assertTrue("a (alpha) should validate", validator.isValidDomainSyntax("a"));        
        assertTrue("9 (alphanum) should validate", validator.isValidDomainSyntax("9"));        
        assertTrue("c-z (alpha - alpha) should validate", validator.isValidDomainSyntax("c-z"));

        assertFalse("c- (alpha -) should fail", validator.isValidDomainSyntax("c-"));
        assertFalse("-c (- alpha) should fail", validator.isValidDomainSyntax("-c"));
        assertFalse("- (-) should fail", validator.isValidDomainSyntax("-"));
    }

    public void testValidator297() {
        assertTrue("xn--d1abbgf6aiiy.xn--p1ai should validate", validator.isValid("xn--d1abbgf6aiiy.xn--p1ai")); // This uses a valid TLD
     }

    // labels are a max of 63 chars and domains 253
    public void testValidator306() {
        final String longString = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz0123456789A";
        assertEquals(63, longString.length()); // 26 * 2 + 11
        
        assertTrue("63 chars label should validate", validator.isValidDomainSyntax(longString+".com"));
        assertFalse("64 chars label should fail", validator.isValidDomainSyntax(longString+"x.com"));

        assertTrue("63 chars TLD should validate", validator.isValidDomainSyntax("test."+longString));
        assertFalse("64 chars TLD should fail", validator.isValidDomainSyntax("test.x"+longString));
        
        final String longDomain = 
                longString
                + "." + longString
                + "." + longString
                + "." + longString.substring(0,61)
                ; 
        assertEquals(253, longDomain.length());
        assertTrue("253 chars domain should validate", validator.isValidDomainSyntax(longDomain));
        assertFalse("254 chars domain should fail", validator.isValidDomainSyntax(longDomain+"x"));
    }

    // Check array is sorted and is lower-case
    public void test_INFRASTRUCTURE_TLDS_sortedAndLowerCase() throws Exception {
        final boolean sorted = isSortedLowerCase("INFRASTRUCTURE_TLDS");
        assertTrue(sorted);
    }

    // Check array is sorted and is lower-case
    public void test_COUNTRY_CODE_TLDS_sortedAndLowerCase() throws Exception {
        final boolean sorted = isSortedLowerCase("COUNTRY_CODE_TLDS");
        assertTrue(sorted);
    }

    // Check array is sorted and is lower-case
    public void test_GENERIC_TLDS_sortedAndLowerCase() throws Exception {
        final boolean sorted = isSortedLowerCase("GENERIC_TLDS");
        assertTrue(sorted);
    }

    // Check array is sorted and is lower-case
    public void test_LOCAL_TLDS_sortedAndLowerCase() throws Exception {
        final boolean sorted = isSortedLowerCase("LOCAL_TLDS");
        assertTrue(sorted);
    }

    // Download and process local copy of http://data.iana.org/TLD/tlds-alpha-by-domain.txt
    // Check if the internal TLD table is up to date
    // Check if the internal TLD tables have any spurious entries
    public static void main(String a[]) throws Exception {
        Set ianaTlds = new HashSet(); // keep for comparison with array contents
        DomainValidator dv = DomainValidator.getInstance();;
        File txtFile = new File("target/tlds-alpha-by-domain.txt");
        long timestamp = download(txtFile, "http://data.iana.org/TLD/tlds-alpha-by-domain.txt", 0L);
        final File htmlFile = new File("target/tlds-alpha-by-domain.html");
        download(htmlFile,"http://www.iana.org/domains/root/db", timestamp);

        BufferedReader br = new BufferedReader(new FileReader(txtFile));
        String line;
        final String header;
        line = br.readLine(); // header
        if (line.startsWith("# Version ")) {
            header = line.substring(2);
        } else {
            br.close();
            throw new IOException("File does not have expected Version header");
        }
        final boolean generateUnicodeTlds = false; // Change this to generate Unicode TLDs as well
        final Method toUnicode = getIDNMethod();
        if (toUnicode == null) {
            if (generateUnicodeTlds) {
                System.err.println("Cannot convert XN-- entries (no access to java.net.IDN)");
            }
        }

        // Parse html page to get entries
        Map htmlInfo = getHtmlInfo(htmlFile);
        Map missingTLD = new TreeMap(); // stores entry and comments as String[]
        Map missingCC = new TreeMap();
        while((line = br.readLine()) != null) {
            if (!line.startsWith("#")) {
                final String unicodeTld; // only different from asciiTld if that was punycode
                final String asciiTld = line.toLowerCase(Locale.ENGLISH);
                if (line.startsWith("XN--")) {
                    if (toUnicode != null) {
                        unicodeTld = toUnicode(toUnicode, line);
                    } else {
                        // allow the code to check for missing ASCII TLDs
                        if (generateUnicodeTlds) {
                            continue; // No translation possible
                        } else {
                            unicodeTld = "";
                        }
                    }
                } else {
                    unicodeTld = asciiTld;
                }
                if (!dv.isValidTld(asciiTld)) {
                    String [] info = (String[]) htmlInfo.get(asciiTld);
                    if (info != null) {
                        String type = info[0];
                        String comment = info[1];
                        if ("country-code".equals(type)) { // Which list to use?
                            missingCC.put(asciiTld, unicodeTld + " " + comment);
                            if (generateUnicodeTlds) {
                                missingCC.put(unicodeTld, asciiTld + " " + comment);
                            }
                        } else {
                            missingTLD.put(asciiTld, unicodeTld + " " + comment);
                            if (generateUnicodeTlds) {
                                missingTLD.put(unicodeTld, asciiTld + " " + comment);
                            }
                        }
                    } else {
                        System.err.println("Expected to find info for "+ asciiTld);
                    }
                }
                ianaTlds.add(asciiTld);
                // Don't merge these conditions; generateUnicodeTlds is final so needs to be separate to avoid a warning
                if (generateUnicodeTlds) {
                    if (!unicodeTld.equals(asciiTld)) {
                        ianaTlds.add(unicodeTld);                    
                    }                    
                }
            }
        }
        br.close();
        if (!missingTLD.isEmpty()) {
            printMap(header, missingTLD, "TLD");
        }
        if (!missingCC.isEmpty()) {
            printMap(header, missingCC, "CC");
        }
        // Check if internal tables contain any additional entries
        isInIanaList("INFRASTRUCTURE_TLDS", ianaTlds);
        isInIanaList("COUNTRY_CODE_TLDS", ianaTlds);
        isInIanaList("GENERIC_TLDS", ianaTlds);
        // Don't check local TLDS isInIanaList("LOCAL_TLDS", ianaTlds);
        System.out.println("Finished checks");
    }

    private static void printMap(final String header, Map map, String string) {
        System.out.println("Entries missing from "+ string +" List\n");
        if (header != null) {
            System.out.println("        // Taken from " + header);
        }
        Iterator it = map.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry me = (Map.Entry)it.next();
            System.out.println("        \"" + me.getKey() + "\", // " + me.getValue());
        }
        System.out.println("\nDone");
    }

    private static Map getHtmlInfo(final File f) throws IOException {
        final Map info = new HashMap();

//        <td><span class="domain tld"><a href="/domains/root/db/ax.html">.ax</a></span></td>
        final Pattern domain = Pattern.compile(".*<a href=\"/domains/root/db/([^.]+)\\.html");
//        <td>country-code</td>
        final Pattern type = Pattern.compile("\\s+<td>([^<]+)</td>");
//        <!-- <td>Åland Islands<br/><span class="tld-table-so">Ålands landskapsregering</span></td> </td> -->
//        <td>Ålands landskapsregering</td>
        final Pattern comment = Pattern.compile("\\s+<td>([^<]+)</td>");

        final BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while((line=br.readLine())!=null){
            Matcher m = domain.matcher(line);
            if (m.lookingAt()) {
                String dom = m.group(1);
                String typ = "??";
                String com = "??";
                line = br.readLine();
                Matcher t = type.matcher(line);
                if (t.lookingAt()) {
                    typ = t.group(1);
                    line = br.readLine();
                    if (line.matches("\\s+<!--.*")) {
                        while(!line.matches(".*-->.*")){
                            line = br.readLine();
                        }
                        line = br.readLine();
                    }
                    // Should have comment; is it wrapped?
                    while(!line.matches(".*</td>.*")){
                        line += " " +br.readLine();
                    }
                    Matcher n = comment.matcher(line);
                    if (n.lookingAt()) {
                        com = n.group(1);
                    }
                    info.put(dom.toLowerCase(Locale.ENGLISH), new String[]{typ, com});
//                    System.out.println(dom + " " + typ + " " +com);
                }
            }
        }
        br.close();
        return info;
    }

    /*
     * Download a file if it is more recent than our cached copy.
     * Unfortunately the server does not seem to honour If-Modified-Since for the
     * Html page, so we check if it is newer than the txt file and skip download if so
     */
    private static long download(File f, String tldurl, long timestamp) throws IOException {
        if (timestamp > 0 && f.canRead()) {
            long modTime = f.lastModified();            
            if (modTime > timestamp) {
                System.out.println("Skipping download - found recent " + f);
                return modTime;
            }
        }
        HttpURLConnection hc = (HttpURLConnection) new URL(tldurl).openConnection();
        if (f.canRead()) {
            long modTime = f.lastModified();            
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");//Sun, 06 Nov 1994 08:49:37 GMT
            String since = sdf.format(new Date(modTime));
            hc.addRequestProperty("If-Modified-Since", since);
            System.out.println("Found " + f + " with date " + since);
        }
        if (hc.getResponseCode() == 304) {
            System.out.println("Already have most recent " + tldurl);
        } else {
            System.out.println("Downloading " + tldurl);
            byte buff[] = new byte[1024];
            InputStream is = hc.getInputStream();

            FileOutputStream fos = new FileOutputStream(f);
            int len;
            while((len=is.read(buff)) != -1) {
                fos.write(buff, 0, len);
            }
            fos.close();
            is.close();
            System.out.println("Done");
        }
        return f.lastModified();
    }

    private static String toUnicode(Method m, String line) {
        try {
            return (String) m.invoke(null, new String[]{line.toLowerCase(Locale.ENGLISH)});
        } catch (Exception e) {
        }
        return line;
    }

    private static Method getIDNMethod() {
        try {
            Class clazz = Class.forName("java.net.IDN", false, DomainValidatorTest.class.getClassLoader());
            return clazz.getDeclaredMethod("toUnicode", new Class[]{String.class});
        } catch (Exception e) {
          return null;
        }
    }

    // isInIanaList and isSorted are split into two methods.
    // If/when access to the arrays is possible without reflection, the intermediate
    // methods can be dropped
    private static boolean isInIanaList(String arrayName, Set ianaTlds) throws Exception {
        Field f = DomainValidator.class.getDeclaredField(arrayName);
        final boolean isPrivate = Modifier.isPrivate(f.getModifiers());
        if (isPrivate) {
            f.setAccessible(true);
        }
        String[] array = (String[]) f.get(null);
        try {
            return isInIanaList(arrayName, array, ianaTlds);
        } finally {
            if (isPrivate) {
                f.setAccessible(false);
            }
        }
    }

    private static boolean isInIanaList(String name, String [] array, Set ianaTlds) {
        for(int i = 0; i < array.length; i++) {
            if (!ianaTlds.contains(array[i])) {
                System.out.println(name + " contains unexpected value: " + array[i]);
            }
        }
        return true;
    }

    private boolean isSortedLowerCase(String arrayName) throws Exception {
        Field f = DomainValidator.class.getDeclaredField(arrayName);
        final boolean isPrivate = Modifier.isPrivate(f.getModifiers());
        if (isPrivate) {
            f.setAccessible(true);
        }
        String[] array = (String[]) f.get(null);
        try {
            return isSortedLowerCase(arrayName, array);
        } finally {
            if (isPrivate) {
                f.setAccessible(false);
            }
        }
    }

    private static boolean isLowerCase(String string) {
        return string.equals(string.toLowerCase(Locale.ENGLISH));
    }

    // Check if an array is strictly sorted - and lowerCase
    private static boolean isSortedLowerCase(String name, String [] array) {
        boolean sorted = true;
        boolean strictlySorted = true;
        final int length = array.length;
        boolean lowerCase = isLowerCase(array[length-1]); // Check the last entry
        for(int i = 0; i < length-1; i++) { // compare all but last entry with next
            final String entry = array[i];
            final String nextEntry = array[i+1];
            final int cmp = entry.compareTo(nextEntry);
            if (cmp > 0) { // out of order
                System.out.println("Out of order entry: " + entry + " < " + nextEntry + " in " + name);
                sorted = false;
            } else if (cmp == 0) {
                strictlySorted = false;
                System.out.println("Duplicated entry: " + entry + " in " + name);
            }
            if (!isLowerCase(entry)) {
                System.out.println("Non lowerCase entry: " + entry + " in " + name);
                lowerCase = false;
            }
        }
        return sorted && strictlySorted && lowerCase;
    }
}
