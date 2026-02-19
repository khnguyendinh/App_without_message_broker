package com.example.receiver.config;

import com.example.receiver.store.DatabaseMapStore;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    private final ApplicationContext applicationContext;

    public HazelcastConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setInstanceName("receiver-hz-instance");
        config.setClusterName("spring-hz-cluster");

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(5701);
        networkConfig.setPortAutoIncrement(true);

        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig()
                .setEnabled(true)
                .addMember("127.0.0.1:5701")
                .addMember("127.0.0.1:5702");

        DatabaseMapStore mapStore = new DatabaseMapStore(applicationContext);

        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setImplementation(mapStore);
        mapStoreConfig.setEnabled(true);
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setWriteDelaySeconds(0);

        MapConfig mapConfig = new MapConfig("shared-messages");
        mapConfig.setMapStoreConfig(mapStoreConfig);
        mapConfig.setBackupCount(1);
        mapConfig.getEvictionConfig()
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                .setSize(10000);
        mapConfig.setTimeToLiveSeconds(3600);

        config.addMapConfig(mapConfig);

        return Hazelcast.newHazelcastInstance(config);
    }
}
