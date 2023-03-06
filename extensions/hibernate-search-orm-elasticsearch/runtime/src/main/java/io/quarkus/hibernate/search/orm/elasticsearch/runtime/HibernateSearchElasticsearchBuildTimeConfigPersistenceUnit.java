package io.quarkus.hibernate.search.orm.elasticsearch.runtime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.backend.elasticsearch.ElasticsearchVersion;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class HibernateSearchElasticsearchBuildTimeConfigPersistenceUnit {

    /**
     * Default backend
     */
    @ConfigItem(name = "elasticsearch")
    @ConfigDocSection
    public ElasticsearchBackendBuildTimeConfig defaultBackend;

    /**
     * Named backends
     */
    @ConfigItem(name = "elasticsearch")
    @ConfigDocSection
    ElasticsearchNamedBackendsBuildTimeConfig namedBackends;

    /**
     * A xref:hibernate-search-orm-elasticsearch.adoc#bean-reference-note-anchor[bean reference] to a component
     * that should be notified of any failure occurring in a background process
     * (mainly index operations).
     *
     * The referenced bean must implement `FailureHandler`.
     *
     * [NOTE]
     * ====
     * Instead of setting this configuration property,
     * you can simply annotate your custom `FailureHandler` implementation with `@SearchExtension`
     * and leave the configuration property unset: Hibernate Search will use the annotated implementation automatically.
     * If this configuration property is set, it takes precedence over any `@SearchExtension` annotation.
     * ====
     *
     * @asciidoclet
     */
    @ConfigItem
    public Optional<String> backgroundFailureHandler;

    /**
     * Configuration for coordination between threads or application instances.
     */
    @ConfigItem
    public CoordinationConfig coordination;

    public Map<String, ElasticsearchBackendBuildTimeConfig> getAllBackendConfigsAsMap() {
        Map<String, ElasticsearchBackendBuildTimeConfig> map = new LinkedHashMap<>();
        if (defaultBackend != null) {
            map.put(null, defaultBackend);
        }
        if (namedBackends != null) {
            map.putAll(namedBackends.backends);
        }
        return map;
    }

    @ConfigGroup
    public static class ElasticsearchNamedBackendsBuildTimeConfig {

        /**
         * Named backends
         */
        @ConfigDocMapKey("backend-name")
        public Map<String, ElasticsearchBackendBuildTimeConfig> backends;

    }

    @ConfigGroup
    public static class ElasticsearchBackendBuildTimeConfig {
        /**
         * The version of Elasticsearch used in the cluster.
         *
         * As the schema is generated without a connection to the server, this item is mandatory.
         *
         * It doesn't have to be the exact version (it can be `7` or `7.1` for instance) but it has to be sufficiently precise
         * to choose a model dialect (the one used to generate the schema) compatible with the protocol dialect (the one used
         * to communicate with Elasticsearch).
         *
         * There's no rule of thumb here as it depends on the schema incompatibilities introduced by Elasticsearch versions. In
         * any case, if there is a problem, you will have an error when Hibernate Search tries to connect to the cluster.
         *
         * @asciidoclet
         */
        @ConfigItem
        public Optional<ElasticsearchVersion> version;

        /**
         * Configuration for the index layout.
         */
        @ConfigItem
        public LayoutConfig layout;

        /**
         * The default configuration for the Elasticsearch indexes.
         */
        @ConfigItem(name = ConfigItem.PARENT)
        public ElasticsearchIndexBuildTimeConfig indexDefaults;

        /**
         * Per-index specific configuration.
         */
        @ConfigItem
        @ConfigDocMapKey("index-name")
        public Map<String, ElasticsearchIndexBuildTimeConfig> indexes;
    }

    @ConfigGroup
    public static class ElasticsearchIndexBuildTimeConfig {
        /**
         * Configuration for automatic creation and validation of the Elasticsearch schema:
         * indexes, their mapping, their settings.
         */
        @ConfigItem
        public SchemaManagementConfig schemaManagement;

        /**
         * Configuration for full-text analysis.
         */
        @ConfigItem
        public AnalysisConfig analysis;
    }

    @ConfigGroup
    public static class SchemaManagementConfig {

        // @formatter:off
        /**
         * Path to a file in the classpath holding custom index settings to be included in the index definition
         * when creating an Elasticsearch index.
         *
         * The provided settings will be merged with those generated by Hibernate Search, including analyzer definitions.
         * When analysis is configured both through an analysis configurer and these custom settings, the behavior is undefined;
         * it should not be relied upon.
         *
         * See https://docs.jboss.org/hibernate/stable/search/reference/en-US/html_single/#backend-elasticsearch-configuration-index-settings[this section of the reference documentation]
         * for more information.
         *
         * @asciidoclet
         */
        // @formatter:on
        @ConfigItem
        public Optional<String> settingsFile;

        // @formatter:off
        /**
         * Path to a file in the classpath holding a custom index mapping to be included in the index definition
         * when creating an Elasticsearch index.
         *
         * The file does not need to (and generally shouldn't) contain the full mapping:
         * Hibernate Search will automatically inject missing properties (index fields) in the given mapping.
         *
         * See https://docs.jboss.org/hibernate/stable/search/reference/en-US/html_single/#backend-elasticsearch-mapping-custom[this section of the reference documentation]
         * for more information.
         *
         * @asciidoclet
         */
        // @formatter:on
        @ConfigItem
        public Optional<String> mappingFile;

    }

    @ConfigGroup
    public static class AnalysisConfig {
        /**
         * A xref:hibernate-search-orm-elasticsearch.adoc#bean-reference-note-anchor[bean reference] to the component
         * used to configure full text analysis (e.g. analyzers, normalizers).
         *
         * The referenced bean must implement `ElasticsearchAnalysisConfigurer`.
         *
         * See xref:hibernate-search-orm-elasticsearch.adoc#analysis-configurer[Setting up the analyzers] for more
         * information.
         *
         * [NOTE]
         * ====
         * Instead of setting this configuration property,
         * you can simply annotate your custom `ElasticsearchAnalysisConfigurer` implementation with `@SearchExtension`
         * and leave the configuration property unset: Hibernate Search will use the annotated implementation automatically.
         * If this configuration property is set, it takes precedence over any `@SearchExtension` annotation.
         * ====
         *
         * @asciidoclet
         */
        @ConfigItem
        public Optional<String> configurer;
    }

    @ConfigGroup
    public static class LayoutConfig {
        /**
         * A xref:hibernate-search-orm-elasticsearch.adoc#bean-reference-note-anchor[bean reference] to the component
         * used to configure layout (e.g. index names, index aliases).
         *
         * The referenced bean must implement `IndexLayoutStrategy`.
         *
         * Available built-in implementations:
         *
         * `simple`::
         * The default, future-proof strategy: if the index name in Hibernate Search is `myIndex`,
         * this strategy will create an index named `myindex-000001`, an alias for write operations named `myindex-write`,
         * and an alias for read operations named `myindex-read`.
         * `no-alias`::
         * A strategy without index aliases, mostly useful on legacy clusters:
         * if the index name in Hibernate Search is `myIndex`,
         * this strategy will create an index named `myindex`, and will not use any alias.
         *
         * See
         * link:{hibernate-search-doc-prefix}#backend-elasticsearch-indexlayout[this section of the reference documentation]
         * for more information.
         *
         * [NOTE]
         * ====
         * Instead of setting this configuration property,
         * you can simply annotate your custom `IndexLayoutStrategy` implementation with `@SearchExtension`
         * and leave the configuration property unset: Hibernate Search will use the annotated implementation automatically.
         * If this configuration property is set, it takes precedence over any `@SearchExtension` annotation.
         * ====
         *
         * @asciidoclet
         */
        @ConfigItem
        public Optional<String> strategy;
    }

    @ConfigGroup
    public static class CoordinationConfig {

        /**
         * The strategy to use for coordinating between threads or even separate instances of the application,
         * in particular in automatic indexing.
         *
         * See xref:hibernate-search-orm-elasticsearch.adoc#coordination[coordination] for more information.
         *
         * @asciidoclet
         */
        @ConfigItem(defaultValue = "none")
        public Optional<String> strategy;
    }

}
