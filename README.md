HDFS Content Repository Properties
===================================

This content repository uses the Hadoop FileSystem API to store FlowFile content. Because of this, it can be used to store content on the local disk and/or in one or more distinct HDFS clusters. It also has four different operating modes which are described below in the nifi.content.repository.hdfs.operating.mode property.

**NOTE:** This is an experimental content repository. Although it has been thoroughly unit tested, it has not be extensively tested with a representative test or production environment and should be used at your own risk.


<br/>

Supported FileSystemRepository Properties:
----------------------------------------------

All of the properties File System Content Repository Properties still apply (see the NiFi Administration Guide for details):

- `nifi.content.repository.implementation`
- `nifi.content.repository.directory.*` - repository directory locations
- `nifi.content.repository.archive.max.usage.percentage`
- `nifi.content.repository.archive.enabled`
- `nifi.content.repository.always.sync`
- `nifi.content.claim.max.appendable.size`
- `nifi.content.claim.max.flow.files`

The equivalent default local content repository directory would be specified with:

    nifi.content.repository.directory.default=file:content_repository

An HDFS content repository directory would be specified with:

    nifi.content.repository.directory.default=hdfs://localhost:9000/content_repository


<br/>

Example Minimal Configuration:
----------------------------------------------

    nifi.content.repository.implementation=org.apache.nifi.hdfs.repository.HdfsContentRepository
    nifi.content.repository.directory.default=file:content_repository
    nifi.content.repository.hdfs.core.site=./conf/core-site.xml


<br/>

HDFS Content Repository Specific Properties:
--------------------------------------------------

| Property | Description |
|----------|-------------|
| nifi.content.repository.implementation | The Java class name of the content repository. It must be: <br/> `org.apache.nifi.hdfs.repository.HdfsContentRepository` |
| nifi.content.repository.hdfs.core.site | The default Hadoop core-site.xml file to configure file systems with. <br/><br/>**NOTE:** This isn't actually required as long as each location specifies its own core-site.xml, however each directory is required to have a core-site.xml defined either with this property, or as described below. <br/><br/>**For example:**<br/><pre>**Assume the following two locations:**<br/>nifi.content.repository.directory.default1=uri://path/to/dir1</br>nifi.content.repository.directory.default2=uri://path/to/dir2<br/><br/>**Then the following two properties may also be provided:** <br/>nifi.content.repository.hdfs.core.site.default1=/path/to/core-site-1.xml<br/>nifi.content.repository.hdfs.core.site.default2=/path/to/core-site-2.xml</pre>|
| nifi.content.repository.hdfs.primary | A comma separated list of location names to treat as the primary storage group for the `CapacityFallback` and `FailureFallback` operating modes. <br/><br/>**Example:** <br/> `nifi.content.repository.hdfs.primary=disk1,disk2,disk3` |
| nifi.content.repository.hdfs.archive | A comma separated list of location names to store archived content in. See the 'Archive' operating mode below. <br/><br/>**Example:**<br/> `nifi.content.repository.hdfs.archive=archive1,archive2,archive3` |
| nifi.content.repository.hdfs.operating.mode | A comma separated list of operating modes that governs the behavior of the content repository. **Default:** `Normal`. See the 'Operating Modes' section below for possible values. |
| nifi.content.repository.hdfs.full.percentage | The percentage (`##%`) of a location's capacity that must be occupied before treating the location as 'full'. <br/><br/>**Note:** Once a location is full, all writes will stop for that location. If all locations are full and there is no fallback, claim creation will stop until space becomes available. <br/><br/>**Default:** `95%` |
| nifi.content.repository.hdfs.failure.timeout | The amount of time to wait when a failure occurs for a location before attempting to use that location again for writing. <br/><br/>**Default:** *disabled*. <br/><br/>**Example:** `1 minute` |
| nifi.content.repository.hdfs.wait.active.containers.timeout | The amount of time to wait for an active location to be available before giving up and throwing an exception. <br/><br/>**Default:** *indefinite*. <br/><br/>**Example:** `5 minutes` |
| nifi.content.repository.hdfs.sections.per.container | The number of subdirectories per location. This is primarily used to avoid too many content claim files within a single directory. <br/><br/>**Default:** `1024` |

<br/>

Operating Modes
----------------

| Mode | Description |
|------|-------------|
| Normal | No special fallback handling is made during failure. Each configured location is written to as normal until they are full. Once all locations are full, writes will block until space becomes available. Note: This is **default** operating mode if one isn't specified in the nifi.properties file. |
| CapacityFallback | The locations in the 'primary' group are filled first and the rest are only filled once all locations in the primary group are full. Once space becomes available again for at least a minute, the primary group will become active again. This mode **cannot** be used with the `FailureFallback` mode. The 'primary' group is specified with the following property where each location id is comma separated: `nifi.content.repository.hdfs.primary` |
| FailureFallback | The configured locations 'primary' group are filled as normal until they are full. Once they are full, writes will block until space becomes available. If a write failure occurs within all primary locations, the remaining non-primary locations are written to until a configured time period has elapsed. This mode **cannot** be used with the `CapacityFallback` mode. |
| Archive | All locations are written to and filled as described in the other modes. As files are moved to the archive, they are copied to the locations in the 'archive' group and then deleted. This can be combined with **any** of the other three modes. If this is the only mode specified, `Normal` is also assumed. The 'archive' group is specified with the `nifi.content.repository.hdfs.archive` property where each location id is comma separated. |