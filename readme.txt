Custom Tomcat Valves for Tomcat 6

:: Description
Custom Tomcat 6 valves. Version 1.0 contains a valve to block access for one or
several paths using comma separated regular expressions for path, allowed ip and
denied ip. 

Example:
<Valve className="se.qbranch.tomcat.valve.BlockAccessByPathValve" path="/manager/.*" allow="127\.0\.0\.1"/>

:: Preqs
Uses gradle 0.8 or maven to build and test.
Download gradle from http://gradle.org/

:: Gradle Build Targets
Build: gradle build
Test: gradle test
Generate eclipse project: gradle eclipse
