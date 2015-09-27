package com.kafka.test;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.consumer.Whitelist;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

/**
 * <pre>
 * Created by zhaoming on 14-5-4 下午3:32
 * </pre>
 */
public class KafkaConsumer {

	public static void main(String[] args) throws InterruptedException,
			UnsupportedEncodingException {

		Properties properties = new Properties();
		properties.put("zookeeper.connect", "172.18.0.43,172.18.0.44,172.18.0.46,172.18.0.47,172.18.0.48");
		properties.put("auto.commit.enable", "true");
		properties.put("auto.commit.interval.ms", "60000");
		properties.put("group.id", "test-consumer-group");

		ConsumerConfig consumerConfig = new ConsumerConfig(properties);

		ConsumerConnector javaConsumerConnector = Consumer
				.createJavaConsumerConnector(consumerConfig);

		// topic的过滤器
		Whitelist whitelist = new Whitelist("ftptest");
		List<KafkaStream<byte[], byte[]>> partitions = javaConsumerConnector
				.createMessageStreamsByFilter(whitelist);

		if(partitions.isEmpty()){
			System.out.println("empty!");
			TimeUnit.SECONDS.sleep(1);
		}

		// 消费消息
		for (KafkaStream<byte[], byte[]> partition : partitions) {

			ConsumerIterator<byte[], byte[]> iterator = partition.iterator();
			while (iterator.hasNext()) {
				MessageAndMetadata<byte[], byte[]> next = iterator.next();
				System.out.println("partiton:" + next.partition());
				System.out.println("offset:" + next.offset());
				System.out.println("message:"
						+ new String(next.message(), "utf-8"));
			}

		}

	}
}