package org.travelbot.java;

import java.util.List;

import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.joo.scorpius.Bootstrap;
import org.joo.scorpius.support.bootstrap.AbstractBootstrap;
import org.joo.scorpius.support.bootstrap.CompositionBootstrap;
import org.joo.scorpius.support.builders.contracts.IdGenerator;
import org.joo.scorpius.support.builders.id.TimeBasedIdGenerator;
import org.joo.scorpius.support.typesafe.TriggerTypeSafeBootstrap;
import org.joo.scorpius.support.typesafe.TypeSafeBootstrap;
import org.travelbot.java.support.logging.AnnotatedGelfJsonAppender;
import org.travelbot.java.support.serializers.ConfigValueSerializer;
import org.travelbot.java.support.utils.AsyncTaskRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.messenger4j.Messenger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import io.vertx.core.VertxOptions;

public class MessengerBootstrap extends CompositionBootstrap {

    static {
        PluginManager.addPackage(AnnotatedGelfJsonAppender.class.getPackage().getName());
    }

    protected void configureBootstraps(List<Bootstrap> bootstraps) {
        MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;

        bootstraps.add(AbstractBootstrap.from(() -> configureOverridens(msgApplicationContext)));
        bootstraps.add(new TypeSafeBootstrap());
        bootstraps.add(new TriggerTypeSafeBootstrap());
        bootstraps.add(new TriggerConfigurator());
        bootstraps.add(new MessengerVertxBootstrap(new VertxOptions().setEventLoopPoolSize(8),
                msgApplicationContext.getPort()));
    }

    private void configureOverridens(MessengerApplicationContext msgApplicationContext) {
        msgApplicationContext.override(IdGenerator.class, new TimeBasedIdGenerator());
        msgApplicationContext.override(Messenger.class, configureMessenger());
        msgApplicationContext.override(Config.class, configureConfiguration());
        msgApplicationContext.override(ObjectMapper.class, configureObjectMapper());
        msgApplicationContext.override(AsyncTaskRunner.class,
                new AsyncTaskRunner(msgApplicationContext.getConfig().getInt("executors.task_runner_threads")));
    }

    private Messenger configureMessenger() {
        return Messenger.create(MessengerApplicationContext.ACCESS_TOKEN, MessengerApplicationContext.APP_SECRET,
                MessengerApplicationContext.VERIFY_TOKEN);
    }

    private Config configureConfiguration() {
        Config config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference());
        return config;
    }

    private ObjectMapper configureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        final SimpleModule module = new SimpleModule();
        module.addSerializer(ConfigValue.class, new ConfigValueSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}