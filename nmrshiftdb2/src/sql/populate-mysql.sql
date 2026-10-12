INSERT INTO TURBINE_PERMISSION VALUES(1,'view',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(2,'customize',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(3,'maximize',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(4,'minimize',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(5,'personalize',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(6,'info',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(7,'close',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(8,'detach',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(9,'submit',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(10,'review',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(11,'labgroupworker',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(12,'labgroupuser',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(13,'labgroupmember',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(14,'publish',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(15,'admineventreceiv',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(16,'edit',NULL);
INSERT INTO TURBINE_PERMISSION VALUES(17,'review-organometals',NULL);
INSERT INTO TURBINE_ROLE VALUES(1,'user',NULL);
INSERT INTO TURBINE_ROLE VALUES(2,'admin',NULL);
INSERT INTO TURBINE_ROLE VALUES(3,'contributor',NULL);
INSERT INTO TURBINE_ROLE VALUES(4,'reviewer',NULL);
INSERT INTO TURBINE_ROLE VALUES(5,'admineventreceiver',NULL);
INSERT INTO TURBINE_ROLE VALUES(6,'assign_reviews',NULL);
INSERT INTO TURBINE_ROLE VALUES(7,'labgroup_user',NULL);
INSERT INTO TURBINE_ROLE VALUES(8,'labgroup_worker',NULL);
INSERT INTO TURBINE_ROLE VALUES(9,'labgroup_member',NULL);
INSERT INTO TURBINE_ROLE VALUES(10,'publisher',NULL);
INSERT INTO TURBINE_ROLE VALUES(11,'editor',NULL);
INSERT INTO TURBINE_GROUP VALUES(1,'Jetspeed',NULL);
INSERT INTO TURBINE_GROUP VALUES(2,'apache',NULL);
INSERT INTO TURBINE_USER VALUES(1,'admin','jetspeed','Raphael','Admin','','','','','','','','raphael@apache.org','CONFIRMED','2004-1-1','2004-1-1','2004-1-1','aced0005737200136a6176612e7574696c2e486173687461626c6513bb0f25214ae4b803000246000a6c6f6164466163746f724900097468726573686f6c6478703f400000000000037708000000050000000274000a4c4153545f4c4f47494e7372000e6a6176612e7574696c2e44617465686a81014b5974190300007870770800000000000000007874000f5f6163636573735f636f756e746572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b0200007870000000a778',NULL,NULL,NULL,'2004-1-1',"false",0,NULL,NULL,'2004-1-1','F','',null,'2004-1-1');
INSERT INTO USER_HISTORY values (1,1,'2013-01-01',null,null,'apache','jetspeed');
INSERT INTO TURBINE_USER VALUES(2,'turbine','turbine','David','Turbine','','','','','','','','taylor@apache.org','CONFIRMED','2004-1-1','2004-1-1','2004-1-1','aced0005737200136a6176612e7574696c2e486173687461626c6513bb0f25214ae4b803000246000a6c6f6164466163746f724900097468726573686f6c6478703f400000000000037708000000050000000274000a4c4153545f4c4f47494e7372000e6a6176612e7574696c2e44617465686a81014b5974190300007870770800000000000000007874000f5f6163636573735f636f756e746572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000007478',NULL,NULL,NULL,'2004-1-1',"false",0,NULL,NULL,'2004-1-1','F','',null,'2004-1-1');
INSERT INTO USER_HISTORY values (2,2,'2013-01-01',null,null,'apache','jetspeed');
INSERT INTO TURBINE_USER VALUES(3,'anon','anon','Anonymous','User','','','','','','','','taylor@apache.org','CONFIRMED','2004-1-1','2004-1-1','2004-1-1','aced0005737200136a6176612e7574696c2e486173687461626c6513bb0f25214ae4b803000246000a6c6f6164466163746f724900097468726573686f6c6478703f400000000000037708000000050000000274000a4c4153545f4c4f47494e7372000e6a6176612e7574696c2e44617465686a81014b5974190300007870770800000000000000007874000f5f6163636573735f636f756e746572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000007478',NULL,NULL,NULL,'2004-1-1',"false",0,NULL,NULL,'2004-1-1','F','',null,'2004-1-1');
INSERT INTO USER_HISTORY values (3,3,'2013-01-01',null,null,'apache','jetspeed');
INSERT INTO TURBINE_USER VALUES(100,'contrib','contrib','contrib','contrib','','','','','','','','shk3@localhost','CONFIRMED','2004-1-1','2004-1-1','2002-02-12 17:20:05.073','aced0005737200136a6176612e7574696c2e486173687461626c6513bb0f25214ae4b803000246000a6c6f6164466163746f724900097468726573686f6c6478703f400000000000027708000000030000000174000f5f6163636573735f636f756e746572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000001278',NULL,NULL,NULL,'2004-1-1',"true",0,NULL,NULL,'2004-1-1','F','',null,'2004-1-1');
INSERT INTO USER_HISTORY values (4,100,'2013-01-01',null,null,'apache','jetspeed');
INSERT INTO TURBINE_USER VALUES(110,'rev','rev','rev','rev','','','','','','','','shk3@localhost','CONFIRMED','2004-1-1','2004-1-1','2002-02-12 17:20:36.49','aced0005737200136a6176612e7574696c2e486173687461626c6513bb0f25214ae4b803000246000a6c6f6164466163746f724900097468726573686f6c6478703f400000000000027708000000030000000174000f5f6163636573735f636f756e746572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000d78',NULL,NULL,NULL,'2004-1-1',"true",0,NULL,NULL,'2004-1-1','F','',null,'2004-1-1');
INSERT INTO USER_HISTORY values (5,110,'2013-01-01',null,null,'apache','jetspeed');

insert into JETSPEED_USER_PROFILE VALUES(119,"anon","html", NULL, NULL,"default.psml","<?xml version='1.0'?>
<portlets xmlns='http://www.apache.org/2000/02/CVS'>
  <controller name='CardPortletController'>
    <parameter name='parameter' value='pane'/>
  </controller>
  <control name='TabControl'/>
  <skin name='nmrshiftdb'/>
  <portlets id='P-Home'>
    <metainfo>
      <title>Home</title>
    </metainfo>
    <entry parent='Home'>
      <control name='ClearPortletControl'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Search'>
    <controller name='TwoColumns'/>
    <metainfo>
      <title>Search</title>
    </metainfo>
    <entry parent='SearchBySpectrum'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='SearchByName'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='1'/>
      </layout>
    </entry>
    <entry parent='SearchByStructure'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='2'/>
      </layout>
    </entry>
    <entry parent='Keywords'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='4'/>
      </layout>
    </entry>
    <entry parent='measuredornot'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='6'/>
      </layout>
    </entry>
    <entry parent='SearchHistory'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='SearchByCondition'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='5'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Results'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Results</title>
    </metainfo>
    <entry parent='Details'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='1'/>
      </layout>
    </entry>
    <entry parent='Bookmarks'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Results'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets  id='P-Predict'>
    <metainfo>
      <title>Predict</title>
    </metainfo>
    <entry parent='Predict'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Submit'>
    <metainfo>
      <title>Submit</title>
    </metainfo>
    <entry parent='Submit'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Review'>
    <metainfo>
      <title>Review</title>
    </metainfo>
    <entry parent='Review'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Wishlist=Guestbook'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Wishlist-Guestbook</title>
    </metainfo>
    <entry parent='Wishlist'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Guestbook'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Help'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Help</title>
    </metainfo>
    <entry parent='WebPagePortletWithURLFromRequest'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Help index'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
</portlets>");
insert into JETSPEED_USER_PROFILE VALUES(118,"contrib","html", NULL, NULL,"default.psml","<?xml version='1.0'?>
<portlets xmlns='http://www.apache.org/2000/02/CVS'>
  <controller name='CardPortletController'>
    <parameter name='parameter' value='pane'/>
  </controller>
  <control name='TabControl'/>
  <skin name='nmrshiftdb'/>
  <portlets id='P-Home'>
    <metainfo>
      <title>Home</title>
    </metainfo>
    <entry parent='Home'>
      <control name='ClearPortletControl'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Search'>
    <controller name='TwoColumns'/>
    <metainfo>
      <title>Search</title>
    </metainfo>
    <entry parent='SearchBySpectrum'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='SearchByName'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='1'/>
      </layout>
    </entry>
    <entry parent='SearchByStructure'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='2'/>
      </layout>
    </entry>
    <entry parent='Keywords'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='4'/>
      </layout>
    </entry>
    <entry parent='measuredornot'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='6'/>
      </layout>
    </entry>
    <entry parent='SearchHistory'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='SearchByCondition'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='5'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Results'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Results</title>
    </metainfo>
    <entry parent='Details'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='1'/>
      </layout>
    </entry>
    <entry parent='Bookmarks'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Results'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets  id='P-Predict'>
    <metainfo>
      <title>Predict</title>
    </metainfo>
    <entry parent='Predict'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Submit'>
    <metainfo>
      <title>Submit</title>
    </metainfo>
    <entry parent='Submit'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Review'>
    <metainfo>
      <title>Review</title>
    </metainfo>
    <entry parent='Review'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Wishlist=Guestbook'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Wishlist-Guestbook</title>
    </metainfo>
    <entry parent='Wishlist'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Guestbook'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Help'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Help</title>
    </metainfo>
    <entry parent='WebPagePortletWithURLFromRequest'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Help index'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
</portlets>");
insert into JETSPEED_USER_PROFILE VALUES(117,"rev","html", NULL, NULL,"default.psml","<?xml version='1.0'?>
<portlets xmlns='http://www.apache.org/2000/02/CVS'>
  <controller name='CardPortletController'>
    <parameter name='parameter' value='pane'/>
  </controller>
  <control name='TabControl'/>
  <skin name='nmrshiftdb'/>
  <portlets id='P-Home'>
    <metainfo>
      <title>Home</title>
    </metainfo>
    <entry parent='Home'>
      <control name='ClearPortletControl'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Search'>
    <controller name='TwoColumns'/>
    <metainfo>
      <title>Search</title>
    </metainfo>
    <entry parent='SearchBySpectrum'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='SearchByName'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='1'/>
      </layout>
    </entry>
    <entry parent='SearchByStructure'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='2'/>
      </layout>
    </entry>
    <entry parent='Keywords'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='4'/>
      </layout>
    </entry>
    <entry parent='measuredornot'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='6'/>
      </layout>
    </entry>
    <entry parent='SearchHistory'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='SearchByCondition'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='5'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Results'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Results</title>
    </metainfo>
    <entry parent='Details'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='1'/>
      </layout>
    </entry>
    <entry parent='Bookmarks'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Results'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets  id='P-Predict'>
    <metainfo>
      <title>Predict</title>
    </metainfo>
    <entry parent='Predict'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Submit'>
    <metainfo>
      <title>Submit</title>
    </metainfo>
    <entry parent='Submit'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Review'>
    <metainfo>
      <title>Review</title>
    </metainfo>
    <entry parent='Review'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Wishlist=Guestbook'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Wishlist-Guestbook</title>
    </metainfo>
    <entry parent='Wishlist'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Guestbook'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Help'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Help</title>
    </metainfo>
    <entry parent='WebPagePortletWithURLFromRequest'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Help index'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-NMR lab administration'>
    <metainfo>
      <title>NMR lab administration</title>
    </metainfo>
    <entry parent='Order fullfillment'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
</portlets>");
insert into JETSPEED_USER_PROFILE VALUES(116,"turbine","html", NULL, NULL,"default.psml","<?xml version='1.0'?>
<portlets xmlns='http://www.apache.org/2000/02/CVS'>
  <controller name='CardPortletController'>
    <parameter name='parameter' value='pane'/>
  </controller>
  <control name='TabControl'/>
  <skin name='nmrshiftdb'/>
  <portlets id='P-Home'>
    <metainfo>
      <title>Home</title>
    </metainfo>
    <entry parent='Home'>
      <control name='ClearPortletControl'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Search'>
    <controller name='TwoColumns'/>
    <metainfo>
      <title>Search</title>
    </metainfo>
    <entry parent='SearchBySpectrum'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='SearchByName'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='1'/>
      </layout>
    </entry>
    <entry parent='SearchByStructure'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='2'/>
      </layout>
    </entry>
    <entry parent='Keywords'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='4'/>
      </layout>
    </entry>
    <entry parent='measuredornot'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='6'/>
      </layout>
    </entry>
    <entry parent='SearchHistory'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='SearchByCondition'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='5'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Results'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Results</title>
    </metainfo>
    <entry parent='Details'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='1'/>
      </layout>
    </entry>
    <entry parent='Bookmarks'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Results'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets  id='P-Predict'>
    <metainfo>
      <title>Predict</title>
    </metainfo>
    <entry parent='Predict'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Submit'>
    <metainfo>
      <title>Submit</title>
    </metainfo>
    <entry parent='Submit'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Review'>
    <metainfo>
      <title>Review</title>
    </metainfo>
    <entry parent='Review'>
      <parameter name='_display' value='normal'/>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Wishlist=Guestbook'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Wishlist-Guestbook</title>
    </metainfo>
    <entry parent='Wishlist'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Guestbook'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
  <portlets id='P-Help'>
    <controller name='TwoColumnsRight'/>
    <metainfo>
      <title>Help</title>
    </metainfo>
    <entry parent='WebPagePortletWithURLFromRequest'>
      <layout>
        <property name='column' value='1'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
    <entry parent='Help index'>
      <layout>
        <property name='column' value='0'/>
        <property name='row' value='0'/>
      </layout>
    </entry>
  </portlets>
</portlets>");

INSERT INTO TURBINE_ROLE_PERMISSION VALUES(1,1);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(1,2);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(1,3);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(1,4);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(1,5);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(1,6);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(2,1);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(2,2);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(2,3);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(2,4);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(2,5);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(2,6);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(2,7);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(3,1);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(3,2);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(3,3);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(3,4);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(3,5);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(3,6);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(3,9);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(4,1);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(4,2);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(4,3);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(4,4);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(4,5);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(4,6);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(4,9);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(4,10);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(7,12);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(8,11);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(9,13);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(10,14);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(10,17);
INSERT INTO TURBINE_ROLE_PERMISSION VALUES(5,15);
insert into TURBINE_ROLE_PERMISSION values(11,16);
INSERT INTO TURBINE_USER_GROUP_ROLE VALUES(2,1,1);
INSERT INTO TURBINE_USER_GROUP_ROLE VALUES(1,1,1);
INSERT INTO TURBINE_USER_GROUP_ROLE VALUES(1,1,2);
INSERT INTO TURBINE_USER_GROUP_ROLE VALUES(1,1,5);
INSERT INTO TURBINE_USER_GROUP_ROLE VALUES(100,1,1);
INSERT INTO TURBINE_USER_GROUP_ROLE VALUES(100,1,3);
INSERT INTO TURBINE_USER_GROUP_ROLE VALUES(110,1,1);
INSERT INTO TURBINE_USER_GROUP_ROLE VALUES(110,1,4);
INSERT INTO TURBINE_USER_GROUP_ROLE VALUES(100,1,7);
INSERT INTO TURBINE_USER_GROUP_ROLE VALUES(110,1,8);
INSERT INTO ID_TABLE VALUES(1,'TURBINE_PERMISSION',100,10);
INSERT INTO ID_TABLE VALUES(2,'TURBINE_ROLE',120,10);
INSERT INTO ID_TABLE VALUES(3,'TURBINE_GROUP',100,10);
INSERT INTO ID_TABLE VALUES(4,'TURBINE_USER',120,10);
INSERT INTO ID_TABLE VALUES(5,'TURBINE_SCHEDULED_JOB',100,10);
INSERT INTO ID_TABLE VALUES(6,'TURBINE_ROLE_PERMISSION',100,10);
INSERT INTO ID_TABLE VALUES(7,'TURBINE_USER_GROUP_ROLE',100,10);
INSERT INTO ID_TABLE VALUES(8,'NMR_CONDITION',100,10);
INSERT INTO ID_TABLE VALUES(9,'SPECTRUM_TYPE',100,10);
INSERT INTO ID_TABLE VALUES(10,'SPECTRUM',100,10);
INSERT INTO ID_TABLE VALUES(11,'NMR_SIGNAL',100,10);
INSERT INTO ID_TABLE VALUES(12,'MOLECULE',100,10);
INSERT INTO ID_TABLE VALUES(13,'ATOM',100,10);
INSERT INTO ID_TABLE VALUES(14,'BOND',100,10);
INSERT INTO ID_TABLE VALUES(15,'CONDITION_TYPE',100,10);
INSERT INTO ID_TABLE VALUES(16,'KEYWORD',100,10);
INSERT INTO ID_TABLE VALUES(17,'REVIEWGROUP',100,10);
INSERT INTO ID_TABLE VALUES(18,'JETSPEED_USER_PROFILE',100,10);
INSERT INTO ID_TABLE VALUES(19,'JETSPEED_GROUP_PROFILE',100,10);
INSERT INTO ID_TABLE VALUES(20,'JETSPEED_ROLE_PROFILE',100,10);
INSERT INTO ID_TABLE VALUES(21,'JETSPEED_ANON_PROFILE',100,10);
INSERT INTO ID_TABLE VALUES(22,'LITERATURE',100,10);
INSERT INTO ID_TABLE VALUES(23,'AUTHOR',100,10);
INSERT INTO ID_TABLE VALUES(24,'PUBLISHER',100,10);
INSERT INTO ID_TABLE VALUES(25,'JOURNAL',100,10);
INSERT INTO ID_TABLE VALUES(26,'JOURNAL_VOLUME',100,10);
insert into ID_TABLE values (27,"GUESTBOOK",100,10);
insert into ID_TABLE values (28,"WISHLIST",100,10);
insert into ID_TABLE values (29,"CANONICAL_NAME_TYPE",100,10);
insert into ID_TABLE values (30,"SAMPLE",100,10);
insert into ID_TABLE values (31,"RAW_FILE",100,10);
insert into ID_TABLE values (32,"COORDINATE_SET_TYPE",100,10);
insert into ID_TABLE values (33,"MACHINE",100,10);
insert into ID_TABLE values (34,"EXPERIMENT_CATEGORY",100,1);
insert into ID_TABLE values (35,"EXPERIMENT",100,1);
insert into ID_TABLE values (36,"EXPERIMENT_REPORT_CATEGORY",100,1);
insert into ID_TABLE values (37,"MIXTURE",100,1);
insert into ID_TABLE values (38,"SUBSTANCE",100,1);
insert into ID_TABLE values (39,"USER_HISTORY",100,1);
insert into ID_TABLE values (40,"QUICK_CHECK",100,1);
insert into ID_TABLE values (41,"DATASET_DOI",100,1);
insert into ID_TABLE values (42,"DATASET_REVIEW_KEY",100,1);
insert into ID_TABLE values (43,"EXPERIMENT_MACHINE",100,1);

INSERT INTO SPECTRUM_TYPE VALUES(1, 1, '13C', 1, 4,'true', NULL, NULL, NULL);
INSERT INTO SPECTRUM_TYPE VALUES(2, 1, '1H', 1, 1,'true', NULL, NULL, NULL);
INSERT INTO SPECTRUM_TYPE VALUES(3, 1, '15N', 1, 1,'true', NULL, NULL, NULL);
insert into SPECTRUM_TYPE values (99,1,"13C/1H correlation", 1, 1,"false", NULL, NULL, NULL);
INSERT INTO SPECTRUM_TYPE VALUES(100, 1, '1H svm', 1, 0,'true', NULL, NULL, NULL);
INSERT INTO SPECTRUM_TYPE VALUES(101, 1, 'All heavy atoms', 1, 0,'true', NULL, NULL, NULL);
INSERT INTO ISOTOPE VALUES(1, 1, 'C', 13);
INSERT INTO ISOTOPE VALUES(2, 1, 'H', 1);
INSERT INTO ISOTOPE VALUES(3, 1, 'N', 15);
INSERT INTO CONDITION_TYPE VALUES(1, 'Temperature [K]', null, 'm', 'float','cml:temp','cml:k','condition','','true','true');
INSERT INTO CONDITION_TYPE VALUES(2, 'Solvent', null, 'm', 'String','cml:solvent','cml:solvent','substance','','true','true');
INSERT INTO CONDITION_TYPE VALUES(3, 'Field Strength [MHz]', 'Spectrometer Frequency for 1H [MHz]', 'm', 'float','cml:field','siUnits:megahertz','condition','','true','true');
INSERT INTO CONDITION_TYPE VALUES(4, 'Program', null, 'c', 'String','','','','','true','true');
INSERT INTO CONDITION_TYPE VALUES(5, 'NMRMethod', null, 'c', 'String','','','','','true','true');
INSERT INTO CONDITION_TYPE VALUES(6, 'GeomModel', null, 'c', 'String','','','','','true','true');
INSERT INTO CONDITION_TYPE VALUES(7, 'GeomBasisSet', null, 'c', 'String','','','','','true','true');
INSERT INTO CONDITION_TYPE VALUES(8, 'NMRModel', null, 'c', 'String','','','','','true','true');
INSERT INTO CONDITION_TYPE VALUES(9, 'NMRBasisSet', null, 'c', 'String','','','','','true','true');
INSERT INTO CONDITION_TYPE VALUES(10, 'NMRStandard', null, 'c', 'String','','','','','true','true');
INSERT INTO CONDITION_TYPE VALUES(11, 'Assignment Method', null, 'm', 'String','nmr:assignmentMethod','','metadata','','true','true');
INSERT INTO CONDITION_TYPE VALUES(12, 'Program', null, 'p' , 'String','','','','false','true','true');
INSERT INTO `NMR_CONDITION` VALUES(1,'Unknown',11,1);
INSERT INTO `NMR_CONDITION` VALUES(2,'Unreported',11,1);
INSERT INTO `NMR_CONDITION` VALUES(3,'COSY+Hetcor+longrange Hetcor',11,1);
INSERT INTO `NMR_CONDITION` VALUES(4,'COSY+Hetcor',11,1);
INSERT INTO `NMR_CONDITION` VALUES(5,'Hetcor+longrange Hetcor',11,1);
INSERT INTO `NMR_CONDITION` VALUES(6,'COSY+longrange Hetcor',11,1);
INSERT INTO `NMR_CONDITION` VALUES(7,'COSY',11,1);
INSERT INTO `NMR_CONDITION` VALUES(8,'Hetcor',11,1);
INSERT INTO `NMR_CONDITION` VALUES(9,'longrange Hetcor',11,1);
INSERT INTO `NMR_CONDITION` VALUES(10,'1D shift positions',11,1);
INSERT INTO `NMR_CONDITION` VALUES(11,'CSEARCH',12,1);
INSERT INTO SESSIONS VALUES(0,NULL,0,'2002-02-12 17:20:36.49');
INSERT INTO CANONICAL_NAME_TYPE VALUES(1,"Autonom");
INSERT INTO CANONICAL_NAME_TYPE VALUES(4,"INChI");
INSERT INTO COORDINATE_SET_TYPE VALUES(1,"User input");
INSERT INTO COORDINATE_SET_TYPE VALUES(2,"Corina generated");
INSERT INTO COORDINATE_SET_TYPE VALUES(3,"CDK generated with mm2");
INSERT INTO COORDINATE_SET_TYPE VALUES(4,"CDK generated with mmff94");
alter table ATOM change IS_VISIBLE IS_VISIBLE enum("false","true") not null;
alter table ATOM change HETERO HETERO enum("false","true") not null default 'false';
alter table ATOM change IS_AROMATIC IS_AROMATIC enum("false","true") not null;
alter table SPECTRUM change REVIEW_FLAG REVIEW_FLAG enum("false","true","rejected","edited",'change','hidden','private') not null;
alter table SPECTRUM change PUBLISHED PUBLISHED enum("false","true") not null;
alter table BOND change IS_CONFIGURATION_SPECIFIED IS_CONFIGURATION_SPECIFIED enum("false","true") not null;
alter table BOND change IS_AROMATIC IS_AROMATIC enum("false","true") not null;
alter table LITERATURE change ET_AL ET_AL enum("false","true") not null;
alter table SAMPLE change FINISHED FINISHED enum("false","true","pending") not null;
alter table GUESTBOOK change VALID VALID enum("false","true") not null default "false";
alter table CONDITION_TYPE change CML_ENTRY_TYPE CML_ENTRY_TYPE enum("","metadata","substance","condition") not null default "";
alter table SAMPLE change PROCESS PROCESS enum("self","worker","robot","barcode") not null default "self";
alter table RAW_FILE change ASSIGNED ASSIGNED enum("false","true") not null default "false";
alter table MACHINE change VALID VALID enum("false","true") not null;
alter table MACHINE change MEASURE_SELF MEASURE_SELF enum("false","true") not null;
alter table MACHINE change MEASURE_OPERATOR MEASURE_OPERATOR enum("false","true") not null;
alter table MACHINE change MEASURE_CHANGER MEASURE_CHANGER enum("false","true") not null;
alter table MACHINE change MEASURE_BARCODE MEASURE_BARCODE enum("false","true") not null;
alter table MACHINE change MEASURE_SELF_NIGHT MEASURE_SELF_NIGHT enum("false","true") not null;
alter table MACHINE change MEASURE_OPERATOR_NIGHT MEASURE_OPERATOR_NIGHT enum("false","true") not null;
alter table MACHINE change MEASURE_CHANGER_NIGHT MEASURE_CHANGER_NIGHT enum("false","true") not null;
alter table MACHINE change MEASURE_BARCODE_NIGHT MEASURE_BARCODE_NIGHT enum("false","true") not null;
alter table MACHINE change MEASURE_OPERATOR_ENFORCE_STRUCTURE MEASURE_OPERATOR_ENFORCE_STRUCTURE enum("false","true") not null;
alter table MACHINE change MEASURE_CHANGER_ENFORCE_STRUCTURE MEASURE_CHANGER_ENFORCE_STRUCTURE enum("false","true") not null;
alter table MACHINE change MEASURE_SELF_ENFORCE_STRUCTURE MEASURE_SELF_ENFORCE_STRUCTURE enum("false","true") not null;
alter table MACHINE change MEASURE_BARCODE_ENFORCE_STRUCTURE MEASURE_BARCODE_ENFORCE_STRUCTURE enum("false","true") not null;
alter table MACHINE change DEFAULT_MODE DEFAULT_MODE enum("self","worker","robot","barcode") not null default "self";
alter table MACHINE change ICONNMR_INTEGRATION ICONNMR_INTEGRATION enum("false","true") not null default "false";
alter table MACHINE change SHIFT_OPERATION_MON SHIFT_OPERATION_MON enum("false","true") not null default "false";
alter table MACHINE change SHIFT_OPERATION_TUE SHIFT_OPERATION_TUE enum("false","true") not null default "false";
alter table MACHINE change SHIFT_OPERATION_WED SHIFT_OPERATION_WED enum("false","true") not null default "false";
alter table MACHINE change SHIFT_OPERATION_THU SHIFT_OPERATION_THU enum("false","true") not null default "false";
alter table MACHINE change SHIFT_OPERATION_FRI SHIFT_OPERATION_FRI enum("false","true") not null default "false";
alter table MACHINE change SHIFT_OPERATION_SAT SHIFT_OPERATION_SAT enum("false","true") not null default "false";
alter table MACHINE change SHIFT_OPERATION_SUN SHIFT_OPERATION_SUN enum("false","true") not null default "false";
alter table MACHINE change FORCE_WEEKEND FORCE_WEEKEND enum("false","true") not null default "false";
alter table `CONDITION_TYPE` change REQUIRED REQUIRED enum("false","true") not null default "false";
alter table EXPERIMENT change VALID VALID enum("false","true") not null default "false";
alter table EXPERIMENT_MACHINE change VALID_DAY VALID_DAY enum("false","true") not null default "false";
alter table EXPERIMENT_MACHINE change VALID_NIGHT VALID_NIGHT enum("false","true") not null default "false";
alter table EXPERIMENT_MACHINE change COMPULSORY COMPULSORY enum("false","true") not null default "false";
alter table CONDITION_TYPE change FOR_1D FOR_1D enum("false","true") not null;
alter table CONDITION_TYPE change FOR_2D FOR_2D enum("false","true") not null;
alter table EXPERIMENT_PARAMETER change VISIBLE VISIBLE enum("false","true") not null;
alter table SPECTRUM_TYPE change PEAKS_MANDATORY PEAKS_MANDATORY enum("false","true") not null;
alter table MACHINE change USE_NIGHT_IN_AUTOMATION_FILE USE_NIGHT_IN_AUTOMATION_FILE enum("false","true") not null default "false";
alter table COUPLING add CONSTRAINT COUPLIG_CONSTRAINT UNIQUE (SPECTRUM_ID, ATOM_ID_1, ATOM_ID_2, SIGNAL_ID);
alter table MACHINE change HAS_LOW_PRIO HAS_LOW_PRIO enum("false","true") not null;
alter table SAMPLE change LOW_PRIO LOW_PRIO enum("false","true") not null;
alter table MOLECULE change fp0 fp0 BIGINT UNSIGNED;
alter table MOLECULE change fp1 fp1 BIGINT UNSIGNED;
alter table MOLECULE change fp2 fp2 BIGINT UNSIGNED;
alter table MOLECULE change fp3 fp3 BIGINT UNSIGNED;
alter table MOLECULE change fp4 fp4 BIGINT UNSIGNED;
alter table MOLECULE change fp5 fp5 BIGINT UNSIGNED;
alter table MOLECULE change fp6 fp6 BIGINT UNSIGNED;
alter table MOLECULE change fp7 fp7 BIGINT UNSIGNED;
alter table MOLECULE change fp8 fp8 BIGINT UNSIGNED;
alter table MOLECULE change fp9 fp9 BIGINT UNSIGNED;
alter table MOLECULE change fp10 fp10 BIGINT UNSIGNED;
alter table MOLECULE change fp11 fp11 BIGINT UNSIGNED;
alter table MOLECULE change fp12 fp12 BIGINT UNSIGNED;
alter table MOLECULE change fp13 fp13 BIGINT UNSIGNED;
alter table MOLECULE change fp14 fp14 BIGINT UNSIGNED;
alter table MOLECULE change fp15 fp15 BIGINT UNSIGNED;

drop table if exists STATISTICS;
create table STATISTICS(EARLIEST DATETIME, LATEST DATETIME, PREDICTION int(11), EXACT_SUBSTRUCTURE int(11),LINE_SEARCH int(11),SUBSTRUCTURE_FUZZY_SEARCH int(11),WEIGHT_SEARCH int(11), OTHER_SEARCH int(11));
insert into STATISTICS values(0,0,0,0,0,0,0,0);

create function tanimoto_coefficient RETURNS REAL SONAME "udf_nmrshiftdb.so";
create function spectrumsimilarity RETURNS REAL SONAME "spectrumsimilarity.so";
create function spectrumsimilarity2d RETURNS REAL SONAME "spectrumsimilarity.so";
CREATE FUNCTION fingerprint_compare RETURNS STRING SONAME "udf_nmrshiftdb.so";
CREATE FUNCTION fingerprint_compare64 RETURNS STRING SONAME "udf_nmrshiftdb.so";








