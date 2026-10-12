<%--
Copyright 2004 The Apache Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%-- This screen be shown (using "include file") by other screens if they can't find the ScreenData hashtable  --%>

<table border=1 cellpadding="5"> 

  <tr> 
    <td>
      <br>
      <h2>The screen tmplate couldn't find its ScreenData in the context!</h2>
      It seems as if the PrepareScreen action hasn't been executed. Please check if the requested URL is correct.
    </td>
  </tr> 

</table>