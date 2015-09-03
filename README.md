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

###Initiate a crawl

