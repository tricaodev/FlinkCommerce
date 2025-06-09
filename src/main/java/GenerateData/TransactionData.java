package GenerateData;

import Entity.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.datafaker.Faker;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.time.LocalDateTime;
import java.util.*;

public class TransactionData {
    private static final Faker faker = new Faker();
    private static final Random random = new Random(42);
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);;

    private static final List<String> productIdList = new ArrayList<>(Arrays.asList("product1", "product2", "product3", "product4", "product5", "product6"));
    private static final List<String> productNameList = new ArrayList<>(Arrays.asList("laptop", "mobile", "tablet", "watch", "headphone", "speaker"));
    private static final List<String> productCategoryList = new ArrayList<>(Arrays.asList("electronic", "fashion", "grocery", "home", "beauty", "sports"));
    private static final List<String> productBrandList = new ArrayList<>(Arrays.asList("apple", "samsung", "oneplus", "mi", "boat", "sony"));
    private static final List<String> currencyList = new ArrayList<>(Arrays.asList("USD", "GBP"));
    private static final List<String> paymentMethodList = new ArrayList<>(Arrays.asList("credit_card", "debit_card", "online_transfer"));


    private static Transaction generateTransaction() {
        return new Transaction(
                UUID.randomUUID(),
                productIdList.get(random.nextInt(productIdList.size())),
                productNameList.get(random.nextInt(productNameList.size())),
                productCategoryList.get(random.nextInt(productCategoryList.size())),
                (float) Math.round((10 + random.nextFloat() * 990) * 100) / 100,
                1 + random.nextInt(10),
                productBrandList.get(random.nextInt(productBrandList.size())),
                currencyList.get(random.nextInt(currencyList.size())),
                faker.internet().username(),
                LocalDateTime.now(),
                paymentMethodList.get(random.nextInt(paymentMethodList.size()))
        );
    }
    public static void main(String[] args) throws Exception {
        LocalDateTime start_time = LocalDateTime.now();
        String topic = "financial_transaction";
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        Producer<String, String> producer = new KafkaProducer<>(properties);

        while (true) {
            // Generate data within 10 minutes
            LocalDateTime current_time = LocalDateTime.now();
            if (current_time.isAfter(start_time.plusMinutes(10))) break;

            // Produce data to kafka
            try {
                Transaction transaction = generateTransaction();
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, transaction.getTransactionId().toString(), mapper.writeValueAsString(transaction));
                producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (e == null) {
                            System.out.printf("Message delivered to topic %s[%d:%d]%n", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
                        }
                        else {
                            System.out.printf("Message delivery failed: %s", e);
                        }
                    }
                });

                Thread.sleep(1000);
            }
            catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }

        producer.flush();
        producer.close();
    }
}
