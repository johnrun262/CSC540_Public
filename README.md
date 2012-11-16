CSC540 Database Systems
======
- Matthew Marum
- Aaron Averill
- Rob Parsons
- John Lloyd

Project Reports
----
https://drive.google.com/a/ncsu.edu/#folders/0B1Ivyt8MAVJKSEVOTFdrS2Y0dkU

Getting started with github
---------------

- Installing git, other github documentation 
- https://help.github.com/articles/set-up-git#platform-all


- git command documentation
- http://git-scm.com/documentation


Using this Project
====================

Prereqs
-------
You need Apache Ant installed with the `ant` command on your PATH.

A shell script is used, so we're assuming that you will run command on a Linux/MacOSX system.
I think it is fair to assume that TAs and Prof will be using Linux.

Build
-----
Run the ant command from the same directory that contains `build.xml`
	$ ant
	...
	BUILD SUCCESSFUL
	Total time: 1 second
	
Running
-------
Once you've done a successful build, you can run the command as follows
	$ cd dist
	$ book.sh arg1 arg2 etc...


