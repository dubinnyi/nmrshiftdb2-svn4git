insert into TURBINE_PERMISSION values(11,"labgroupworker",null),(12,"labgroupuser",null);
INSERT INTO TURBINE_PERMISSION VALUES(13,'labgroupmember',NULL),INSERT INTO TURBINE_PERMISSION VALUES(14,'labgroup_leader',null);
insert into TURBINE_ROLE values (7,"labgroup_user",null);
insert into TURBINE_ROLE values (8,"labgroup_worker",null);
INSERT INTO TURBINE_ROLE VALUES(9,'labgroup_member',NULL);
Insert into TURBINE_ROLE_PERMISSION values (7,12),(8,11);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(9,13);
insert into TURBINE_USER values(112,"nmruser","nmruser","lableader","lableader","lableader","lableader","lableader","lableader","lableader","lableader","lableader","lableader@web.de","CONFIRMED",'2004-1-1','2004-1-1','2004-1-1','aced0005737200136a6176612e7574696c2e486173687461626c6513bb0f25214ae4b803000246000a6c6f6164466163746f724900097468726573686f6c6478703f400000000000037708000000050000000274000a4c4153545f4c4f47494e7372000e6a6176612e7574696c2e44617465686a81014b5974190300007870770800000000000000007874000f5f6163636573735f636f756e746572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b0200007870000000a778',null, null,null,'2004-1-1',"false",1,NULL,null,"2010-04-15 15:27:06","F",'',null,'2004-1-1');
INSERT INTO USER_HISTORY values (112,112,'2013-01-01',null,1,'apache','jetspeed');
insert into TURBINE_USER_GROUP_ROLE values(112,1,1),(112,1,3),(112,1,4),(112,1,6),(112,1,8),(112,1,9);
insert into TURBINE_USER_GROUP_ROLE values(112,1,11),(112,1,13),(112,1,14);
insert into LABGROUP values (1,"testgroup",112);
insert into JETSPEED_USER_PROFILE values(103,'nmruser','html', NULL, NULL, 'default.psml', '<?xml version="1.0"?>
<portlets xmlns="http://www.apache.org/2000/02/CVS">
  <controller name="CardPortletController">
    <parameter name="parameter" value="pane"/>
  </controller>
  <control name="TabControl"/>
  <skin name="nmrshiftdb"/>
  <portlets id="P-NMR lab administration">
    <metainfo>
      <title>NMR lab administration</title>
    </metainfo>
    <entry parent="Order fullfillment">
      <control name="ClearPortletControl"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Home">
    <metainfo>
      <title>Home</title>
    </metainfo>
    <entry parent="Home">
      <control name="ClearPortletControl"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Search">
    <controller name="TwoColumns"/>
    <metainfo>
      <title>Search</title>
    </metainfo>
    <entry parent="SearchBySpectrum">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="SearchByName">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="1"/>
      </layout>
    </entry>
    <entry parent="SearchByStructure">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="2"/>
      </layout>
    </entry>
    <entry parent="Keywords">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="4"/>
      </layout>
    </entry>
    <entry parent="measuredornot">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="6"/>
      </layout>
    </entry>
    <entry parent="Spectrum type selection">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="7"/>
      </layout>
    </entry>
    <entry parent="SearchHistory">
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="SearchByCondition">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="5"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Results">
    <controller name="TwoColumnsRight"/>
    <metainfo>
      <title>Results</title>
    </metainfo>
    <entry parent="Details">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="1"/>
      </layout>
    </entry>
    <entry parent="Bookmarks">
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="Results">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets  id="P-Predict">
    <metainfo>
      <title>Predict</title>
    </metainfo>
    <entry parent="Predict">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Submit">
    <metainfo>
      <title>Submit</title>
    </metainfo>
    <entry parent="Submit">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Review">
    <metainfo>
      <title>Review</title>
    </metainfo>
    <entry parent="Review">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Help">
    <controller name="TwoColumnsRight"/>
    <metainfo>
      <title>Help</title>
    </metainfo>
    <entry parent="WebPagePortletWithURLFromRequest">
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="Help index">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
</portlets>');
insert into TURBINE_USER values(111,"testuser","testuser","testuser","testuser","testuser","testuser","testuser","testuser","testuser","testuser","testuser","testuser@web.de","CONFIRMED",'2004-1-1','2004-1-1','2004-1-1','aced0005737200136a6176612e7574696c2e486173687461626c6513bb0f25214ae4b803000246000a6c6f6164466163746f724900097468726573686f6c6478703f400000000000037708000000050000000274000a4c4153545f4c4f47494e7372000e6a6176612e7574696c2e44617465686a81014b5974190300007870770800000000000000007874000f5f6163636573735f636f756e746572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b0200007870000000a778',null, null,null,'2004-1-1',"false",1,NULL,null,"2010-04-15 15:27:06","F",'',null,'2004-1-1');
INSERT INTO USER_HISTORY values (111,111,'2013-01-01',null,1,'apache','jetspeed');
insert into TURBINE_USER_GROUP_ROLE values(111,1,1),(111,1,3),(111,1,7),(111,1,9);
insert into TURBINE_USER_GROUP_ROLE values(112,1,11),(112,1,12);
insert into JETSPEED_USER_PROFILE values(100,'testuser','html', NULL, NULL, 'default.psml', '<?xml version="1.0"?>
<portlets xmlns="http://www.apache.org/2000/02/CVS">
  <controller name="CardPortletController">
    <parameter name="parameter" value="pane"/>
  </controller>
  <control name="TabControl"/>
  <skin name="nmrshiftdb"/>
  <portlets id="P-NMR lab administration">
    <metainfo>
      <title>NMR lab administration</title>
    </metainfo>
    <entry parent="Order handling">
      <control name="ClearPortletControl"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Home">
    <metainfo>
      <title>Home</title>
    </metainfo>
    <entry parent="Home">
      <control name="ClearPortletControl"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Search">
    <controller name="TwoColumns"/>
    <metainfo>
      <title>Search</title>
    </metainfo>
    <entry parent="SearchBySpectrum">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="SearchByName">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="1"/>
      </layout>
    </entry>
    <entry parent="SearchByStructure">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="2"/>
      </layout>
    </entry>
    <entry parent="Keywords">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="4"/>
      </layout>
    </entry>
    <entry parent="measuredornot">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="6"/>
      </layout>
    </entry>
    <entry parent="Spectrum type selection">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="7"/>
      </layout>
    </entry>
    <entry parent="SearchHistory">
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="SearchByCondition">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="5"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Results">
    <controller name="TwoColumnsRight"/>
    <metainfo>
      <title>Results</title>
    </metainfo>
    <entry parent="Details">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="1"/>
      </layout>
    </entry>
    <entry parent="Bookmarks">
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="Results">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets  id="P-Predict">
    <metainfo>
      <title>Predict</title>
    </metainfo>
    <entry parent="Predict">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Submit">
    <metainfo>
      <title>Submit</title>
    </metainfo>
    <entry parent="Submit">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Review">
    <metainfo>
      <title>Review</title>
    </metainfo>
    <entry parent="Review">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Help">
    <controller name="TwoColumnsRight"/>
    <metainfo>
      <title>Help</title>
    </metainfo>
    <entry parent="WebPagePortletWithURLFromRequest">
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="Help index">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
</portlets>');
insert into TURBINE_USER values(113,"testworker","testworker","testworker","testworker","testworker","testworker","testworker","testworker","testworker","testworker","testworker","testuser@web.de","CONFIRMED",'2004-1-1','2004-1-1','2004-1-1','aced0005737200136a6176612e7574696c2e486173687461626c6513bb0f25214ae4b803000246000a6c6f6164466163746f724900097468726573686f6c6478703f400000000000037708000000050000000274000a4c4153545f4c4f47494e7372000e6a6176612e7574696c2e44617465686a81014b5974190300007870770800000000000000007874000f5f6163636573735f636f756e746572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b0200007870000000a778',null, null,null,'2004-1-1',"false",1,NULL,null,"2010-04-15 15:27:06","F",'',null,'2004-1-1');
insert into TURBINE_USER_GROUP_ROLE values(113,1,1),(113,1,3),(113,1,8),(113,1,9);
insert into TURBINE_USER_GROUP_ROLE values(112,1,11),(112,1,13);
INSERT INTO USER_HISTORY values (113,113,'2013-01-01',null,1,'apache','jetspeed');
insert into JETSPEED_USER_PROFILE values(101,'testworker','html', NULL, NULL, 'default.psml', '<?xml version="1.0"?>
<portlets xmlns="http://www.apache.org/2000/02/CVS">
  <controller name="CardPortletController">
    <parameter name="parameter" value="pane"/>
  </controller>
  <control name="TabControl"/>
  <skin name="nmrshiftdb"/>
  <portlets id="P-NMR lab administration">
    <metainfo>
      <title>NMR lab administration</title>
    </metainfo>
    <entry parent="Order fullfillment">
      <control name="ClearPortletControl"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Home">
    <metainfo>
      <title>Home</title>
    </metainfo>
    <entry parent="Home">
      <control name="ClearPortletControl"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Search">
    <controller name="TwoColumns"/>
    <metainfo>
      <title>Search</title>
    </metainfo>
    <entry parent="SearchBySpectrum">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="SearchByName">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="1"/>
      </layout>
    </entry>
    <entry parent="SearchByStructure">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="2"/>
      </layout>
    </entry>
    <entry parent="Keywords">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="4"/>
      </layout>
    </entry>
    <entry parent="measuredornot">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="6"/>
      </layout>
    </entry>
    <entry parent="Spectrum type selection">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="7"/>
      </layout>
    </entry>
    <entry parent="SearchHistory">
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="SearchByCondition">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="5"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Results">
    <controller name="TwoColumnsRight"/>
    <metainfo>
      <title>Results</title>
    </metainfo>
    <entry parent="Details">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="1"/>
      </layout>
    </entry>
    <entry parent="Bookmarks">
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="Results">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets  id="P-Predict">
    <metainfo>
      <title>Predict</title>
    </metainfo>
    <entry parent="Predict">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Submit">
    <metainfo>
      <title>Submit</title>
    </metainfo>
    <entry parent="Submit">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Review">
    <metainfo>
      <title>Review</title>
    </metainfo>
    <entry parent="Review">
      <parameter name="_display" value="normal"/>
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
  <portlets id="P-Help">
    <controller name="TwoColumnsRight"/>
    <metainfo>
      <title>Help</title>
    </metainfo>
    <entry parent="WebPagePortletWithURLFromRequest">
      <layout>
        <property name="column" value="1"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
    <entry parent="Help index">
      <layout>
        <property name="column" value="0"/>
        <property name="row" value="0"/>
      </layout>
    </entry>
  </portlets>
</portlets>');
insert into `NMR_CONDITION` values (191,"250",3,1);
insert into MACHINE values(1,"ac250",191,"true",1,1,"true","true","true","false","false","false","true","true","true","true","true","true","worker","worker","false","","","true","true","true","true","true","false","false",5,0,18,0,5,0,18,0,5,0,18,0,5,0,18,0,5,0,18,0,5,0,18,0,5,0,18,0,60,'','','','','','false',0,'','false',60);
insert into EXPERIMENT_CATEGORY values (1,"proton",1);
insert into EXPERIMENT_REPORT_CATEGORY values (1,"1_D",1);
insert into EXPERIMENT value(1,"1H standard spectrum","true",1,1,1,1,"",5,null,null,null,1,null);
insert into EXPERIMENT_MACHINE values (1,1,1,"1h for iconnmr","true","true",5,"false",null,null,null,null);
