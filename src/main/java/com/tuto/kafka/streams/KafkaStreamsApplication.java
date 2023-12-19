package com.tuto.kafka.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

@SpringBootApplication
public class KafkaStreamsApplication {

	public static final String LOCALHOST_9092 = "127.0.0.1:9094";
	public static final String MY_TOPIC = "myTopic";

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(KafkaStreamsApplication.class, args);
		Properties streamsConfiguration = new Properties();
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-live-test");
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, LOCALHOST_9092);
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

		StreamsBuilder streamsBuilder = new StreamsBuilder();
		KStream<String, String> textLines = streamsBuilder.stream(MY_TOPIC);
		Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

		KTable<String, Long> wordCounts = textLines
				.flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())))
				.groupBy((key, word) -> word)
				.count();

		wordCounts.toStream()
				.foreach((word, count) -> System.out.println("word: " + word + " -> " + count));


		Topology topology = streamsBuilder.build();
		KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration);
		streams.start();

		Thread.sleep(30000);
		streams.close();
	}

}
