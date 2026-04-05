package com.vk.denis.keyValueStrore;

import com.vk.denis.keyValueStrore.repository.MockRepository;
import com.vk.denis.keyValueStrore.repository.Repository;
import com.vk.denis.keyValueStrore.repository.TarantoolRepository;
import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.client.factory.TarantoolFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MainConfiguration {

    @Profile("test")
    @Bean
    public Repository repository() {
        return new MockRepository();
    }

    @Profile("prod")
    @Bean
    public TarantoolBoxClient tarantoolClient(
            @Value("${tarantool.host}") String host,
            @Value("${tarantool.port}") int port,
            @Value("${tarantool.username}") String username,
            @Value("${tarantool.password}") String password
            ) throws Exception {
        if ("guest".equals(username)) {
            password = null;
        }
        return TarantoolFactory.box()
                .withHost(host)
                .withPort(port)
                .withUser(username)
                .withPassword(password)
                .build();
    }

    @Profile("prod")
    @Bean
    public Repository prodRepository(TarantoolBoxClient tarantoolClient) {
        return new TarantoolRepository(tarantoolClient);
    }



    @Bean
    public KeyValueService keyValueService(Repository repository) {
        return new KeyValueService(repository);
    }

}
