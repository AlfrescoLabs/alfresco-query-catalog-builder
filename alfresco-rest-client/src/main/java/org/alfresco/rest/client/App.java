package org.alfresco.rest.client;

import info.debatty.java.stringsimilarity.JaroWinkler;
import org.alfresco.core.handler.AuditApi;
import org.alfresco.core.model.AuditEntry;
import org.alfresco.core.model.AuditEntryEntry;
import org.alfresco.rest.client.bean.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * Command line tool that gets Audit information from Repository to build a catalog
 * of different queries associated with the search syntax.
 *
 * Audit app "AUDIT_APP_ID" must be configured in Repository, mapping the following paths:
 * - /alfresco-api/post/SearchService/query/args
 * - /alfresco-api/post/SearchService/selectNodes/args
 */
@Configuration
@SpringBootApplication
public class App implements CommandLineRunner {

    static final Logger LOG = LoggerFactory.getLogger(App.class);

    @Value("${audit.app.id:search}")
    String AUDIT_APP_ID;
    @Value("${audit.api.max.items:100}")
    Integer MAX_ITEMS;
    @Value("${audit.query.similarity.threshold:0.95}")
    Double SIMILARITY_THRESHOLD;

    @Autowired
    AuditApi auditApi;

    public static void main(String... args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) {

        Map<String, Set<String>> queryCatalog = new HashMap<>();

        int skipCount = 0;
        List<AuditEntryEntry> auditEntries =
                auditApi.listAuditEntriesForAuditApp(AUDIT_APP_ID, skipCount, false, null, MAX_ITEMS, null, null, null).getBody().getList().getEntries();
        LOG.info("{} audit entries found for app {} with skipCount {}", auditEntries.size(), AUDIT_APP_ID, skipCount);

        while (auditEntries.size() > 0) {

            auditEntries.forEach(entryId -> {

                AuditEntry auditEntry = auditApi.getAuditEntry(AUDIT_APP_ID, entryId.getEntry().getId(), null).getBody().getEntry();
                SearchQuery searchQuery = new SearchQuery((Map<String, String>) auditEntry.getValues());
                if (queryCatalog.get(searchQuery.getLang()) == null)
                    queryCatalog.put(searchQuery.getLang(), new TreeSet<>());
                if (isNewQuery(queryCatalog.get(searchQuery.getLang()), searchQuery.getQuery())) {
                    queryCatalog.get(searchQuery.getLang()).add(searchQuery.getQuery());
                }

                LOG.debug("Audit Entry {} has been processed with lang {} and query {}", entryId, searchQuery.getLang(), searchQuery.getQuery());

            });

            skipCount = skipCount + MAX_ITEMS;
            auditEntries =
                    auditApi.listAuditEntriesForAuditApp(AUDIT_APP_ID, skipCount, false, null, MAX_ITEMS, null, null, null).getBody().getList().getEntries();
            LOG.info("{} audit entries found for app {} with skipCount {}", auditEntries.size(), AUDIT_APP_ID, skipCount);

        }

        queryCatalog.keySet().forEach(key -> {
            LOG.info("SYNTAX {}", key);
            for (String query : queryCatalog.get(key)) {
                LOG.info(query);
            }
            LOG.info("-----------------");
        });

    }

    private boolean isNewQuery(Set<String> queries, String candidateQuery) {
        JaroWinkler jw = new JaroWinkler();
        for (String query : queries) {
            if (jw.similarity(candidateQuery, query) > SIMILARITY_THRESHOLD) {
                LOG.debug("'{}' candidate query is similar to '{}' and has been discarded", candidateQuery, query);
                return false;
            }
        }
        return true;
    }

}
