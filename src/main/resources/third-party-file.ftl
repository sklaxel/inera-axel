<#--

    Copyright (C) 2013 Inera AB (http://www.inera.se)

    This file is part of Inera Axel (http://code.google.com/p/inera-axel).

    Inera Axel is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Inera Axel is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>

-->
<#-- To render the third-party file.
 Available context :

 - dependencyMap a collection of Map.Entry with
   key are dependencies (as a MavenProject) (from the maven project)
   values are licenses of each dependency (array of string)

 - licenseMap a collection of Map.Entry with
   key are licenses of each dependency (array of string)
   values are all dependencies using this license
-->
<#function licenseFormat licenses>
    <#assign result = ""/>
    <#list licenses as license>
        <#assign result = result + license/>
        <#if license_has_next><#assign result = result + ", "/></#if>
    </#list>
    <#assign result = result + ";"/>
    <#return result>
</#function>
<#function artifactFormat p>
    <#if p.name?index_of('Unnamed') &gt; -1>
        <#return p.artifactId + ";" + p.groupId + ":" + p.artifactId + ":" + p.version + ";" + (p.url!"no url defined")>
    <#else>
        <#return p.name + ";" + p.groupId + ":" + p.artifactId + ":" + p.version + ";" + (p.url!"no url defined")>
    </#if>
</#function>

<#if dependencyMap?size == 0>
The project has no dependencies.
<#else>
Lists of ${dependencyMap?size} third-party dependencies.
    <#list dependencyMap as e>
        <#assign project = e.getKey()/>
        <#assign licenses = e.getValue()/>
    ${licenseFormat(licenses)} ${artifactFormat(project)}
    </#list>
</#if>
