package com.kafka.test;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;

/**
 * <pre>
 * </pre>
 */
public class KafkaProducer {

	public static void main(String[] args) throws InterruptedException {

		Properties properties = new Properties();
		properties.put("zk.connect", "192.168.157.128:2181");
		properties.put("metadata.broker.list", "192.168.157.128:9092,192.168.157.128:9093,192.168.157.128:9094");

		properties.put("serializer.class", "kafka.serializer.StringEncoder");

		ProducerConfig producerConfig = new ProducerConfig(properties);
		Producer<String, String> producer = new Producer<String, String>(
				producerConfig);

		// 构建消息体
		KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>(
				"test-topic", "test-message");
		producer.send(keyedMessage);

		Thread.sleep(1000);

		producer.close();
	}

}