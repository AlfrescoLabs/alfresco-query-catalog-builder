package org.alfresco.rest.client.bean;

import java.util.Map;

/**
 * Represents a query string associated to the search syntax used for it
 */
public class SearchQuery {

    // fts-alfresco, lucene, x-path, selectnodes, cmis-strict, cmis-alfresco
    String lang;
    // Query string
    String query;

    /**
     QUERY Audit Entry sample (key=value):
     /search/QUERY/searchParameters/value = {
       lang=cmis-alfresco,
       query=SELECT * FROM cmis:document, stores=[workspace://SpacesStore],
       defaultFTSOp=OR,
       defaultFTSFieldOp=OR
     }

     SELECT Audit Entry sample (key=value):
       /search/SELECT/contextNodeRef/value=a58ca143-2e8e-4daa-88c4-d439975a2672,
       /search/SELECT/parameters/value=null,
       /search/SELECT/xpath/value=./app:company_home/st:sites,
       /search/SELECT/followAllParentLinks/value=false,
       /search/SELECT/language/value=xpath
    */
    public SearchQuery(Map<String, String> valuesMap) {
        String searchParameters = valuesMap.get("/search/QUERY/searchParameters/value");
        if (searchParameters != null) {
            String[] pairs = searchParameters.substring(1, searchParameters.length() - 1).split(",");
            for (String entry : pairs) {
                String[] parts = entry.trim().split("=");
                if (parts[0].equals("lang")) {
                    this.lang = parts[1];
                }
                if (parts[0].equals("query")) {
                    this.query = parts[1].replace("\n", " ").replace("\r", " ");
                }
            }
        } else {
            lang = valuesMap.get("/search/SELECT/language/value");
            query = valuesMap.get("/search/SELECT/xpath/value");
        }
    }

    public String getLang() {
        return lang;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "QUERY{" +
                "lang='" + lang + '\'' +
                ", query='" + query + '\'' +
                '}';
    }
}
