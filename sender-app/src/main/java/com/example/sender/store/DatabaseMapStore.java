package com.example.sender.store;

import com.example.common.model.SharedMessage;
import com.example.sender.repository.MessageRepository;
import com.hazelcast.map.MapLoaderLifecycleSupport;
import com.hazelcast.map.MapStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * NOT a Spring @Component — instantiated manually by HazelcastConfig
 * to avoid circular dependency.
 */
public class DatabaseMapStore implements MapStore<String, SharedMessage>, MapLoaderLifecycleSupport {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMapStore.class);

    private final ApplicationContext applicationContext;
    private MessageRepository repository;

    public DatabaseMapStore(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void init(com.hazelcast.core.HazelcastInstance hazelcastInstance,
            Properties properties, String mapName) {
        // Lazily resolve the repository AFTER Spring context is fully initialized
        this.repository = applicationContext.getBean(MessageRepository.class);
        log.info("DatabaseMapStore initialized for map: {}", mapName);
    }

    @Override
    public void destroy() {
        log.info("DatabaseMapStore destroyed");
    }

    @Override
    public void store(String key, SharedMessage value) {
        log.debug("Storing message to DB: id={}, status={}", key, value.getStatus());
        repository.save(value);
    }

    @Override
    public void storeAll(Map<String, SharedMessage> map) {
        log.debug("Storing {} messages to DB", map.size());
        repository.saveAll(map.values());
    }

    @Override
    public void delete(String key) {
        log.debug("Deleting message from DB: id={}", key);
        repository.deleteById(key);
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        log.debug("Deleting {} messages from DB", keys.size());
        repository.deleteAllById(keys);
    }

    @Override
    public SharedMessage load(String key) {
        log.debug("Loading message from DB: id={}", key);
        return repository.findById(key).orElse(null);
    }

    @Override
    public Map<String, SharedMessage> loadAll(Collection<String> keys) {
        log.debug("Loading {} messages from DB", keys.size());
        return repository.findAllById(keys).stream()
                .collect(Collectors.toMap(SharedMessage::getId, msg -> msg));
    }

    @Override
    public Iterable<String> loadAllKeys() {
        log.info("Loading PENDING message keys from DB (LAZY mode)");
        return repository.findIdsByStatus("PENDING");
    }
}
