Aggregated Access Log

:: Description
Custom Tomcat valve that aggregates access counts and data transfer by virtual host. The aggregated
values are published as mbeans.

Example:
<Valve className="com.viskan.tomcat.valve.AggregatedAccessLogValve" />

:: Preqs
Maven to build and test.

:: Thanks
The initial code was based on the code for
https://github.com/xlson/tomcat-valves by Leonard Axelsson, https://github.com/xlson

:: License
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
