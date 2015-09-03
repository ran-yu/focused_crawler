# focused_crawler

The crawling process is initiated by first providing a seed list that encodes the information need.
The seed list is in the form of entity URIs, usually coming from publicly available knowledge bases (i.e. DBpedia). 
The individual methods are accessible under the REST API and by the specific methods names, please find the details in the Methods section.

## Prerequisites
To start the REST API, the web service enviroment is required. Our API runs under [Tomcat](http://tomcat.apache.org/).

## Installation

To setup the API follow these steps:

```js
> git clone https://github.com/bfetahu/focused_crawler.git
> cd focused_crawler
> mvn compile
> mvn war:war
```
This will build the war file in the target directory.
Copy the war into do the deployment directory of your installed Java Servlet (e.g. apache-tomcat/webapps/) container.

##Methods
The individual methods are accessible through URL once the REST API setup succeeded. 

The URL is formatted as: 

http://route to the api/CrawlAPI/method_name?parameter_1=value_1e&parameter_2=value_2

###Initiate a crawl
Method name: crawl

Parameters: {seeds, user, depth}

Output: The crawl identification number, which is later used to perform other operations implemented in the focused crawling module.

###Filter crawled entity candidates

Method: filterCrawlCandidates

Parameters: {crawl_id, crawl_filter}

Output: Confirmation that the particular candidate entities are filtered out from the candidate entity set.

###Export crawl data to SDAS
Method: exportToSDA

Parameters: {crawl_id}

Output: A message confirming that the crawled entity candidates are exported
successfully into the SDA.

###Delete a crawl
Method: deleteCrawl

Parameters: {crawl_id}

Output: A message confirming that all information (crawled candidates and meta-data regarding the crawl) from a specific crawl are deleted.

###Load a crawl
Method: loadCrawl

Parameters: {crawl_id}

Output: A ranked list of crawled candidate entities.

###Load the list of running crawls
Method: loadAllRegisteredCrawls

Parameters: {N/A}

Output: The list of all running/finished crawls, with the detailed crawl metadata.

###Load the list of finished crawls.
Method: loadFinishedCrawls

Parameters: {N/A}

The list of all finished crawls, with the detailed crawl metadata.

###Load the list of all crawls initiated by a specific user
Method: loadCrawlsByUser

Parameters: {user}

Output: The list of all crawls initiated by a specific user, with the detailed crawl
metadata.

###Load the list of all crawls containing a specific seed entity
Method: loadCrawlsBySeed

Parameters: {seeds}

Output: The list of all crawls containing a specific seed entity, with the detailed
crawl metadata.

##Parameters Description
| parameter     | methods             | description |
| --------|---------|-------|
| user  | crawl, loadCrawlsByUser   | The user ID or username as part of an authentication system within the DURAARK WorkbenchUI.|
| depth| crawl | The maximal depth from which we follow links into the Linked Open Data graph from the initial seed entities.  |
| seeds  | crawl, loadCrawlsBySeed   | Seed entities coming from a specific knowledge base (e.g. DBpedia) from which we initiate a focused crawling, or in the case of loadCrawlsBySeed list all crawls that contain a specific seed entity.    |
| crawl_id  |filterCrawlCandidates, deleteCrawl, exportToSDA, loadCrawl | The crawl identification number that is used to delete, export or load the candidate entities from a specific crawl.    |
| crawl_filter  | filterCrawlCandidates | The filter condition that is used to delete already crawled candidate entities from a specific crawl.  |
