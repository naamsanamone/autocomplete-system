package com.autocomplete.service;

import com.autocomplete.datastructure.Trie;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Initializes the Trie with a curated corpus of ~500 technology
 * and programming terms on application startup.
 *
 * <p>Each term has a realistic frequency score reflecting its
 * real-world popularity. This ensures the demo is immediately
 * impressive without requiring external data loading.</p>
 *
 * <h3>Categories:</h3>
 * <ul>
 *   <li>Programming Languages</li>
 *   <li>Frameworks & Libraries</li>
 *   <li>Cloud & DevOps</li>
 *   <li>Databases</li>
 *   <li>DSA & Algorithms</li>
 *   <li>System Design Concepts</li>
 *   <li>Tools & Platforms</li>
 *   <li>Web Technologies</li>
 *   <li>Software Engineering</li>
 * </ul>
 *
 * @author Sai Venkat
 */
@Component
public class CorpusInitializer {

    private static final Logger log = LoggerFactory.getLogger(CorpusInitializer.class);

    private final Trie trie;
    private final AutocompleteService autocompleteService;

    public CorpusInitializer(Trie trie, AutocompleteService autocompleteService) {
        this.trie = trie;
        this.autocompleteService = autocompleteService;
    }

    @PostConstruct
    public void initializeCorpus() {
        long start = System.currentTimeMillis();

        // ─── Programming Languages ────────────────────────────
        addTerms(new String[][]{
                {"java", "9500"}, {"python", "9200"}, {"javascript", "9100"},
                {"typescript", "7500"}, {"c++", "6800"}, {"c#", "6500"},
                {"go", "5500"}, {"rust", "5200"}, {"kotlin", "5000"},
                {"swift", "4800"}, {"scala", "3200"}, {"ruby", "3800"},
                {"php", "4200"}, {"dart", "3500"}, {"r", "3000"},
                {"perl", "1800"}, {"haskell", "1500"}, {"lua", "1600"},
                {"elixir", "1400"}, {"clojure", "1200"}, {"groovy", "1100"},
                {"objective-c", "2800"}, {"assembly", "1000"}, {"matlab", "2200"},
                {"julia", "1300"}, {"fortran", "800"}, {"cobol", "600"},
                {"erlang", "900"}, {"f#", "1100"}, {"ocaml", "700"}
        });

        // ─── Java Ecosystem ──────────────────────────────────
        addTerms(new String[][]{
                {"spring boot", "8500"}, {"spring framework", "7200"}, {"spring security", "6500"},
                {"spring data jpa", "5800"}, {"spring cloud", "5200"}, {"spring mvc", "5000"},
                {"spring webflux", "3800"}, {"spring batch", "3200"}, {"spring actuator", "3000"},
                {"hibernate", "6000"}, {"maven", "5500"}, {"gradle", "5200"},
                {"junit", "4800"}, {"mockito", "4200"}, {"lombok", "4000"},
                {"jackson", "3500"}, {"log4j", "3200"}, {"slf4j", "3000"},
                {"tomcat", "4500"}, {"jetty", "2800"}, {"netty", "3000"},
                {"jpa", "5500"}, {"jdbc", "4000"}, {"jvm", "5000"},
                {"java streams", "4200"}, {"java collections", "4500"}, {"java concurrency", "3800"},
                {"java generics", "3200"}, {"java multithreading", "4000"}, {"java lambda", "3500"},
                {"thymeleaf", "3800"}, {"flyway", "2800"}, {"liquibase", "2500"},
                {"apache kafka", "6200"}, {"apache spark", "4800"}, {"apache maven", "5500"}
        });

        // ─── Web Frameworks ──────────────────────────────────
        addTerms(new String[][]{
                {"react", "8800"}, {"angular", "7200"}, {"vue.js", "6500"},
                {"next.js", "7000"}, {"nuxt.js", "4500"}, {"svelte", "4200"},
                {"express.js", "6000"}, {"node.js", "8200"}, {"django", "5500"},
                {"flask", "4800"}, {"fastapi", "5200"}, {"rails", "3800"},
                {"laravel", "4500"}, {"asp.net", "5000"}, {"nest.js", "4000"},
                {"gatsby", "2800"}, {"remix", "3000"}, {"astro", "2500"},
                {"tailwind css", "6500"}, {"bootstrap", "5500"}, {"material ui", "5000"},
                {"redux", "5200"}, {"zustand", "3000"}, {"react query", "4000"},
                {"webpack", "4500"}, {"vite", "4800"}, {"babel", "3500"},
                {"jquery", "4000"}, {"sass", "3800"}, {"less", "2000"}
        });

        // ─── Cloud & DevOps ──────────────────────────────────
        addTerms(new String[][]{
                {"aws", "8500"}, {"azure", "7500"}, {"google cloud", "7000"},
                {"docker", "8000"}, {"kubernetes", "7500"}, {"terraform", "5500"},
                {"jenkins", "5000"}, {"github actions", "5200"}, {"gitlab ci", "4000"},
                {"ansible", "4200"}, {"puppet", "2800"}, {"chef", "2500"},
                {"prometheus", "4000"}, {"grafana", "3800"}, {"datadog", "3500"},
                {"nginx", "5500"}, {"apache", "4500"}, {"load balancer", "4200"},
                {"aws lambda", "5500"}, {"aws ec2", "6000"}, {"aws s3", "6200"},
                {"aws rds", "4800"}, {"aws ecs", "4000"}, {"aws sqs", "4200"},
                {"aws dynamodb", "4500"}, {"aws cloudformation", "3500"}, {"aws iam", "4000"},
                {"helm", "3200"}, {"istio", "2800"}, {"envoy", "2500"},
                {"ci/cd", "6000"}, {"devops", "6500"}, {"infrastructure as code", "4500"},
                {"containerization", "5000"}, {"microservices", "6800"}, {"serverless", "5200"},
                {"service mesh", "3500"}, {"cloud native", "4500"}, {"site reliability", "4000"}
        });

        // ─── Databases ───────────────────────────────────────
        addTerms(new String[][]{
                {"postgresql", "7200"}, {"mysql", "6800"}, {"mongodb", "6500"},
                {"redis", "6800"}, {"elasticsearch", "5500"}, {"cassandra", "4200"},
                {"oracle", "5000"}, {"sql server", "4800"}, {"sqlite", "4000"},
                {"neo4j", "3200"}, {"couchdb", "2000"}, {"dynamodb", "4500"},
                {"firebase", "5000"}, {"supabase", "3800"}, {"cockroachdb", "2500"},
                {"mariadb", "3500"}, {"memcached", "3800"}, {"influxdb", "2800"},
                {"timescaledb", "2200"}, {"planetscale", "2500"}, {"neon", "2000"},
                {"database indexing", "4500"}, {"database sharding", "4200"},
                {"database replication", "4000"}, {"database normalization", "3800"},
                {"acid properties", "3500"}, {"cap theorem", "4000"},
                {"sql joins", "5000"}, {"sql subqueries", "3800"},
                {"nosql", "5500"}, {"graph database", "3000"}
        });

        // ─── DSA & Algorithms ────────────────────────────────
        addTerms(new String[][]{
                {"binary search", "6500"}, {"merge sort", "5500"}, {"quick sort", "5200"},
                {"depth first search", "5800"}, {"breadth first search", "5800"},
                {"dynamic programming", "7000"}, {"greedy algorithm", "4500"},
                {"dijkstra algorithm", "5000"}, {"a star algorithm", "4200"},
                {"binary tree", "6000"}, {"binary search tree", "5500"},
                {"balanced bst", "4000"}, {"avl tree", "3500"}, {"red black tree", "3800"},
                {"hash map", "6500"}, {"hash table", "5800"}, {"hash set", "4500"},
                {"linked list", "5500"}, {"doubly linked list", "4500"},
                {"stack", "5000"}, {"queue", "5000"}, {"priority queue", "5200"},
                {"heap", "5500"}, {"min heap", "4800"}, {"max heap", "4500"},
                {"trie", "5000"}, {"trie data structure", "4800"},
                {"graph", "5500"}, {"adjacency list", "4000"}, {"adjacency matrix", "3800"},
                {"topological sort", "4200"}, {"minimum spanning tree", "4000"},
                {"kruskal algorithm", "3500"}, {"prim algorithm", "3200"},
                {"two pointer", "5000"}, {"sliding window", "5200"},
                {"backtracking", "4800"}, {"recursion", "5500"},
                {"time complexity", "5500"}, {"space complexity", "5000"},
                {"big o notation", "5800"}, {"amortized analysis", "3200"},
                {"segment tree", "3800"}, {"fenwick tree", "3200"},
                {"union find", "4000"}, {"disjoint set", "3500"},
                {"bellman ford", "3800"}, {"floyd warshall", "3500"},
                {"knapsack problem", "4200"}, {"longest common subsequence", "3800"},
                {"kadane algorithm", "4000"}, {"rabin karp", "3000"},
                {"kmp algorithm", "3200"}, {"boyer moore", "2500"}
        });

        // ─── System Design ───────────────────────────────────
        addTerms(new String[][]{
                {"system design", "7500"}, {"api design", "5500"}, {"rest api", "6800"},
                {"graphql", "5500"}, {"grpc", "4200"}, {"websocket", "5000"},
                {"rate limiter", "5200"}, {"rate limiting", "5000"},
                {"load balancing", "5500"}, {"caching", "6000"}, {"cdn", "5000"},
                {"message queue", "5200"}, {"pub sub", "4500"}, {"event driven", "4800"},
                {"distributed systems", "6000"}, {"consistency", "4500"},
                {"availability", "4200"}, {"partition tolerance", "3800"},
                {"eventual consistency", "4000"}, {"strong consistency", "3800"},
                {"horizontal scaling", "5000"}, {"vertical scaling", "4200"},
                {"database partitioning", "4500"}, {"consistent hashing", "4800"},
                {"circuit breaker", "4200"}, {"bulkhead pattern", "3000"},
                {"api gateway", "5000"}, {"reverse proxy", "4500"},
                {"service discovery", "4000"}, {"leader election", "3500"},
                {"distributed cache", "4500"}, {"write ahead log", "3800"},
                {"bloom filter", "3500"}, {"lru cache", "5500"}, {"lfu cache", "4200"},
                {"url shortener", "5000"}, {"notification system", "4500"},
                {"chat system", "4200"}, {"search engine", "5000"},
                {"recommendation engine", "4800"}, {"autocomplete system", "5500"},
                {"news feed", "4000"}, {"video streaming", "4200"},
                {"file storage", "3800"}, {"payment system", "4500"},
                {"design patterns", "5800"}, {"solid principles", "5500"},
                {"singleton pattern", "4000"}, {"factory pattern", "4200"},
                {"observer pattern", "4000"}, {"strategy pattern", "4200"},
                {"builder pattern", "3800"}, {"adapter pattern", "3200"},
                {"decorator pattern", "3500"}, {"proxy pattern", "3000"}
        });

        // ─── Tools & Platforms ───────────────────────────────
        addTerms(new String[][]{
                {"git", "8500"}, {"github", "8000"}, {"gitlab", "5500"},
                {"bitbucket", "3800"}, {"jira", "5000"}, {"confluence", "3500"},
                {"slack", "4200"}, {"vs code", "7500"}, {"intellij idea", "6500"},
                {"postman", "6000"}, {"swagger", "5200"}, {"openapi", "4500"},
                {"linux", "7000"}, {"ubuntu", "5500"}, {"centos", "3000"},
                {"bash", "5000"}, {"powershell", "3500"}, {"terminal", "4500"},
                {"npm", "6000"}, {"yarn", "4500"}, {"pnpm", "3000"},
                {"pip", "4500"}, {"conda", "3500"}, {"homebrew", "3000"},
                {"figma", "5000"}, {"sketch", "2500"}, {"adobe xd", "2800"},
                {"vercel", "4500"}, {"netlify", "4000"}, {"heroku", "3500"},
                {"digitalocean", "3500"}, {"linode", "2500"}, {"fly.io", "2200"}
        });

        // ─── Software Engineering ────────────────────────────
        addTerms(new String[][]{
                {"agile", "5500"}, {"scrum", "5000"}, {"kanban", "3800"},
                {"test driven development", "4500"}, {"unit testing", "5200"},
                {"integration testing", "4800"}, {"end to end testing", "4000"},
                {"code review", "5000"}, {"pair programming", "3800"},
                {"continuous integration", "5500"}, {"continuous deployment", "5000"},
                {"refactoring", "4500"}, {"clean code", "5000"}, {"clean architecture", "4500"},
                {"domain driven design", "4200"}, {"hexagonal architecture", "3200"},
                {"monolith", "3500"}, {"microservice architecture", "5500"},
                {"api versioning", "3800"}, {"feature flags", "3500"},
                {"monitoring", "4800"}, {"logging", "5000"}, {"alerting", "3800"},
                {"observability", "4200"}, {"distributed tracing", "3800"},
                {"security", "5500"}, {"oauth2", "5000"}, {"jwt", "5500"},
                {"cors", "4000"}, {"xss", "3500"}, {"sql injection", "4000"},
                {"encryption", "4500"}, {"hashing", "4800"}, {"ssl tls", "4200"}
        });

        // ─── AI & Data Science ───────────────────────────────
        addTerms(new String[][]{
                {"machine learning", "7500"}, {"deep learning", "6800"},
                {"artificial intelligence", "7200"}, {"neural network", "6000"},
                {"natural language processing", "5500"}, {"computer vision", "5200"},
                {"tensorflow", "6000"}, {"pytorch", "5800"}, {"scikit learn", "4500"},
                {"pandas", "5500"}, {"numpy", "5000"}, {"matplotlib", "4000"},
                {"large language model", "6500"}, {"generative ai", "6000"},
                {"transformer", "5000"}, {"gpt", "6500"}, {"bert", "4200"},
                {"reinforcement learning", "4000"}, {"supervised learning", "4500"},
                {"unsupervised learning", "4000"}, {"feature engineering", "3800"},
                {"data pipeline", "4500"}, {"etl", "4000"}, {"data warehouse", "3800"},
                {"big data", "5000"}, {"hadoop", "3500"}, {"spark", "4500"}
        });

        long elapsed = System.currentTimeMillis() - start;

        // Update max frequency after loading
        trie.getAllWords().stream()
                .map(w -> trie.searchNode(w))
                .filter(n -> n != null)
                .mapToInt(n -> n.getFrequency())
                .max()
                .ifPresent(autocompleteService::updateMaxFrequency);

        log.info("Corpus initialized: {} words, {} nodes, {} max depth in {}ms",
                trie.size(), trie.getNodeCount(), trie.getMaxDepth(), elapsed);
    }

    /**
     * Batch helper to add terms efficiently.
     */
    private void addTerms(String[][] terms) {
        for (String[] term : terms) {
            trie.insert(term[0], Integer.parseInt(term[1]));
        }
    }
}
