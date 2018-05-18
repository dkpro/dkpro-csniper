---
layout: page-fullwidth
title: "Installation"
permalink: "/documentation/installation/"
---

<div class="row">
<div class="medium-4 medium-push-8 columns" markdown="1">
<div class="panel radius" markdown="1">
**Content**
{: #toc }
*  TOC
{:toc}
</div>
</div><!-- /.medium-4.columns -->

<div class="medium-8 medium-pull-4 columns" markdown="1">

Installation of CSniper requires some prerequisites and some configuration steps which are outlined below.

## Prerequisites

Before you start installing and configuring CSniper, you need to install following software:

- a Java application server (tested with Tomcat 6)
- a MySQL compatible RMDBS (tested with MySQL 5.5)
	- an empty database (let's call it *csniper*)
- IMS Corpus Workbench ((http://cwb.sourceforge.net/download.php)[http://cwb.sourceforge.net/download.php])
- (optionally) an installation of Tgrep2 ([http://tedlab.mit.edu/~dr/Tgrep2/](http://tedlab.mit.edu/~dr/Tgrep2/))

For sake of simplicity, this guide assumes that you are using Tomcat 6 and Mysql 5.5 on Ubuntu 12.


## Deployment

Just copy the `csniper.war` into your Tomcat working directory (e.g. `/var/lib/tomcat6/webapps`) and restart Tomcat. Per default Tomcat settings this will unpack your `csniper.war`. CSniper is not yet configured though, the next step is mandatory.


## Configuration

As a first step, you need to create two directories:

- one configuration directory for CSniper, for this guide at `/srv/csniper`
- one directory for your corpora, e.g. `/srv/csniper/corpora`

To let Tomcat know where our configuration is located, set the environment variable 

	CATALINA_OPTS="-Dcsniper.home=/srv/csniper"

either in `/etc/environment` or at the start of `/usr/share/tomcat6/bin/startup.sh`.
Copy the `csniper.properties.example` to `/srv/csniper`, and rename it to `csniper.properties`. Then you need to specify the options:

* First enter your database credentials:

		database.dialect=org.hibernate.dialect.MySQL5Dialect
		database.driver=com.mysql.jdbc.Driver
		database.url=jdbc:mysql://localhost:3306/csniper (or whatever your database is called)
		database.username=username
		database.password=password

* Then configure the path to your cqp executable, which comes with the installation of CWB:

		engine.cqp.executable=path_to_your_cqp_executable_file
		engine.cqp.macrosLocation=classpath:/BNC_macros.txt

* Optionally you can configure CSniper to use tgrep2, in which case you need to specify the executable here:

		engine.tgrep.executable=path_to_your_tgrep2_executable_file

* You also need to specify the path to the (converted) corpora you want to use:

		corpus.service.info.file=corpus.properties
		corpus.service.path=/srv/csniper/corpora

CSniper uses opennlp components for tokenization and parsing. If you want to use other components (e.g. Stanford Parser), you can specfiy these options in `segmenter.properties` and `parser.properties` respectively (examples can be found in the wiki). These need to be placed into /srv/csniper as well. The components themselves need to reside in the same directory as the sql connector above.

After you have saved this configuration, you want to restart tomcat again.

CSniper is (almost) ready to be used now, and can be found at http://localhost:8080/csniper (default Tomcat configuration). But without data, CSniper doesn't make a whole lot of sense.

Which means: it's time to [convert a corpus for CSniper](../conversion/).

</div><!-- /.medium-8.columns -->
</div><!-- /.row -->
