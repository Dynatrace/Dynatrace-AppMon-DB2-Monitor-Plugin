<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>DB2 Monitor Plugin</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
    <meta content="Scroll Wiki Publisher" name="generator"/>
    <link type="text/css" rel="stylesheet" href="css/blueprint/liquid.css" media="screen, projection"/>
    <link type="text/css" rel="stylesheet" href="css/blueprint/print.css" media="print"/>
    <link type="text/css" rel="stylesheet" href="css/content-style.css" media="screen, projection, print"/>
    <link type="text/css" rel="stylesheet" href="css/screen.css" media="screen, projection"/>
    <link type="text/css" rel="stylesheet" href="css/print.css" media="print"/>
</head>
<body>
                <h1>DB2 Monitor Plugin</h1>
    <div class="section-2"  id="83492948_DB2MonitorPlugin-Overview"  >
        <h2>Overview</h2>
    <p>
            <img src="images_community/download/attachments/83492948/db2-large.png" alt="images_community/download/attachments/83492948/db2-large.png" class="" />
        <br/>            <img src="images_community/download/attachments/83492948/DB2_dashboard.jpg" alt="images_community/download/attachments/83492948/DB2_dashboard.jpg" class="" />
            </p>
    <p>
The DB2 plugin enables monitoring behavior metrics provided in a DB2 database.The plugin uses JDBC to connect to the DB2 Database and queries key performance  metrics. Having these measures in dynaTrace enables quick correlation of database related performance issues such as high I/O or too many database connections to application transaction performance problems such as long running transactions or slow database queries.    </p>
    <p>
Since this plugin is currently in BETA, please follow the best practice of having a separate collector for your monitoring plugins.    </p>
    </div>
    <div class="section-2"  id="83492948_DB2MonitorPlugin-PluginDetails"  >
        <h2>Plugin Details</h2>
    <div class="tablewrap">
        <table>
<thead class=" "></thead><tfoot class=" "></tfoot><tbody class=" ">    <tr>
            <td rowspan="1" colspan="1">
        <p>
Plug-In Files    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
<strong class=" ">dynaTrace 4.x+</strong>:<br/><a href="attachments_88440947_1_com.dynatrace.diagnostics.plugins.DB2Plugin_0.8.0.jar">DB2 Monitor Plugin 0.8.0</a><br/><a href="attachments_88440945_1_DB2_Overview.dashboard.xml">DB2 Monitor Dashboard </a>    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Author    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
Eric Burns (Eric.burns@compuware.com)    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
dynaTrace Versions    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
4.1+    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
License    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
<a href="attachments_5275722_2_dynaTraceBSD.txt">dynaTrace BSD</a>    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Support    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
<a href="https://community/display/DL/Support+Levels">Not Supported</a>    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Known Problems    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Release History    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
2012-07-12 0.8.0 Initial BETA Release    </p>
            </td>
        </tr>
</tbody>        </table>
            </div>
    </div>
    <div class="section-2"  id="83492948_DB2MonitorPlugin-ProvidedMeasures"  >
        <h2>Provided Measures</h2>
    <p>
The following image shows the metrics that the monitor provides:    </p>
    <p>
            <img src="images_community/download/attachments/83492948/DB2_monitor_metrics.jpg" alt="images_community/download/attachments/83492948/DB2_monitor_metrics.jpg" class="confluence-embedded-image" />
            </p>
    </div>
    <div class="section-2"  id="83492948_DB2MonitorPlugin-ConfigurationDB2Monitor"  >
        <h2>Configuration DB2 Monitor</h2>
    <p>
The monitor requires the following configuration settings:    </p>
<ul class=" "><li class=" ">    <p>
hostName: Host name of the DB2 Database Instance    </p>
</li><li class=" ">    <p>
dbName: Database Instance Name (SID) or Service name    </p>
</li><li class=" ">    <p>
dbUsername: Username that is used to access the database. User may needs to have query rights to a specific tables    </p>
</li><li class=" ">    <p>
dbPassword: Password that is used to access the database    </p>
</li><li class=" ">    <p>
dbPort: DB2 Database Port for JDBC Connections (default: 50001)    </p>
</li></ul>    </div>
    <div class="section-2"  id="83492948_DB2MonitorPlugin-Installation"  >
        <h2>Installation</h2>
    <p>
Import the Plugin into the dynaTrace Server via the dynaTrace Server Settings menu -&gt; Plugins -&gt; Install Plugin. For details how to do this please refer to the <a href="https://apmcommunity.compuware.com/community/display/DOCDT42/Plugins">dynaTrace documentation</a>.    </p>
    <p>
To use the provided dashboard please leave the default name of the Monitor as &quot;RepositoryDB&quot;, then open the Dashboard and set the Data Source accordingly.    </p>
    </div>
    <div class="section-2"  id="83492948_DB2MonitorPlugin-AccessRequirements"  >
        <h2>Access Requirements</h2>
    <p>
This plugin accesses the following tables, so it must be able to connect and have SELECT privileges:    </p>
<ul class=" "><li class=" ">    <p>
sysibmadm.applications    </p>
</li><li class=" ">    <p>
sysibmadm.snapdb    </p>
</li></ul>    </div>
    <div class="section-2"  id="83492948_DB2MonitorPlugin-UsageNotes"  >
        <h2>Usage Notes</h2>
    <p>
This release is very much in Beta.  Please send feedback and help to improve it.    </p>
    <p>
Metric Groups will be changed/altered in a later release.  Would like input from DB2 DBAs.    </p>
    </div>
            </div>
        </div>
        <div class="footer">
        </div>
    </div>
</body>
</html>
