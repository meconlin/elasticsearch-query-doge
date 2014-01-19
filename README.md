Doge query syntax plugin for ElasticSearch
==================================

so cool, much querying, wow fast, amaze syntax, very doge, such clustering

Works just like regular Elasticsearch queries only 
This Term List plugin will do simple matching against the complete term list found directly in Lucene.

1. Build this plugin:

        mvn clean compile test package 
        # this will create a file here: target/releases/elasticsearch-query-doge-0.0.1-SNAPSHOT.zip
        PLUGIN_PATH=`pwd`/target/releases/elasticsearch-query-doge-0.0.1-SNAPSHOT.zip

2. Install the PLUGIN

        cd $ELASTICSEARCH_HOME
        ./bin/plugin -url file:/$PLUGIN_PATH -install elasticsearch-query-doge

3. Updating the plugin

        cd $ELASTICSEARCH_HOME
        ./bin/plugin -remove elasticsearch-query-doge
        ./bin/plugin -url file:/$PLUGIN_PATH -install elasticsearch-query-doge


Elasticsearch Version
===================
1.0.0.RC.1


Usage
==========

You can send a doge enhanced query to any of the following end points:

```
/_dogesearch
/_amazesearch
/_sosearch
/_muchsearch
```

Contain your doge enhanced query excitement to these doge word prefixes:
```
so_
much_
wow_
amaze_
very_
such_
```

You may doge enhance any key of a valid es query:

much simple!
```
GET /_all/_dogesearch
{
   "so_query": {
      "much_match_all": {}
   }
}
```

OR so complex!

```
 { 
     "very_query": {
       "match_all": {}
     },
     "so_facets":{
         "f1":{
	         "amaze_facet_filter":{
	            "such_exists":{
	               "field":"name"
	            }
	         },
	         "so_terms":{
	            "field":"name",
	            "size":100
	         }
	      },
	      "f2":{
	         "much_facet_filter":{
	            "exists":{
	               "field":"gender"
	            }
	         },
	         "amaze_terms":{
	            "field":"gender",
	            "size":100
	         }
	      }
 	}
 }

```





