gwt-commons-validator
=====================

A compatible GWT port of the apache commons validator, which provides most of the functionality also on client (browser) side.

The Package itself has to be includes as binary and source package. I've done it this way, to give you the possibility to take the original apache commons binary package (full compatible) and combine it with the gwt-commons version of the source package for the gwt compiler, this is useful, if you do have the binary package already in the dependency list.

```
    <dependency>
      <groupId>de.knightsoft-net</groupId>
      <artifactId>gwt-commons-validator</artifactId>
      <version>1.5.1-0</version>
    </dependency>
```

GWT Integration
---------------

Add this inherit command into your project .gwt.xml file:

```
<inherits name="org.apache.commons.GWTCommonsValidator" />
```
