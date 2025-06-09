package FlinkCommerce;

import Deserializer.JSONValueDeserializationSchema;
import Dto.Transaction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class DataStreamJob {
	private static final String jdbcUrl = "jdbc:postgresql://postgres:5432/postgres";
	private static final String username = "admin";
	private static final String password = "postgres";

	public static void main(String[] args) throws Exception {
		// Sets up the execution environment, which is the main entry point
		// to building Flink applications.
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		JdbcExecutionOptions executionOptions = new JdbcExecutionOptions.Builder()
				.withBatchIntervalMs(200)
				.withBatchSize(1000)
				.withMaxRetries(5)
				.build();
		JdbcConnectionOptions connectionOptions = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
				.withUrl(jdbcUrl)
				.withDriverName("org.postgresql.Driver")
				.withUsername(username)
				.withPassword(password)
				.build();

		String topic = "financial_transaction";
		KafkaSource<Transaction> source = KafkaSource.<Transaction>builder()
				.setBootstrapServers("broker:29092")
				.setGroupId("flink-commerce")
				.setTopics(topic)
				.setStartingOffsets(OffsetsInitializer.earliest())
				.setValueOnlyDeserializer(new JSONValueDeserializationSchema())
				.build();

		DataStream<Transaction> transactionDataStream = env.fromSource(
				source,
				WatermarkStrategy.noWatermarks(),
				"Kafka Source"
		);

		transactionDataStream.print();

		// Create transaction table in postgresql
		transactionDataStream.addSink(JdbcSink.sink(
				"CREATE TABLE IF NOT EXISTS transactions (" +
						"transactionId UUID PRIMARY KEY," +
						"productId VARCHAR(255)," +
						"productName VARCHAR(255)," +
						"productCategory VARCHAR(255)," +
						"productPrice DOUBLE PRECISION," +
						"productQuantity INTEGER," +
						"productBrand VARCHAR(255)," +
						"currency VARCHAR(255)," +
						"customerId VARCHAR(255)," +
						"transactionDate TIMESTAMP," +
						"paymentMethod VARCHAR(255)" +
						")",
				(preparedStatement, transaction) -> {

				},
				executionOptions,
				connectionOptions
		)).name("Create transaction table");

		// Insert transaction data to transaction table
		transactionDataStream.addSink(JdbcSink.sink(
				"INSERT INTO transaction" +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
						"ON CONFLICT transactionId DO UPDATE SET" +
						"productId = EXCLUDED.productId," +
						"productName = EXCLUDED.productName," +
						"productCategory = EXCLUDED.productCategory," +
						"productPrice = EXCLUDED.productPrice," +
						"productQuantity = EXCLUDED.productQuantity," +
						"productBrand = EXCLUDED.productBrand," +
						"currency = EXCLUDED.currency," +
						"customerId = EXCLUDED.customerId," +
						"transactionDate = EXCLUDED.transactionDate," +
						"paymentMethod = EXCLUDED.paymentMethod" +
						"WHERE transactionId = EXCLUDED.transactionId",
				(preparedStatement, transaction) -> {
					preparedStatement.setObject(1, transaction.getTransactionId());
					preparedStatement.setString(2, transaction.getProductId());
					preparedStatement.setString(3, transaction.getProductName());
					preparedStatement.setString(4, transaction.getProductCategory());
					preparedStatement.setDouble(5, transaction.getProductPrice());
					preparedStatement.setInt(6, transaction.getProductQuantity());
					preparedStatement.setString(7, transaction.getProductBrand());
					preparedStatement.setString(8, transaction.getCurrency());
					preparedStatement.setString(9, transaction.getCustomerId());
					preparedStatement.setTimestamp(10, Timestamp.valueOf(transaction.getTransactionDate()));
					preparedStatement.setString(11, transaction.getPaymentMethod());
				},
				executionOptions,
				connectionOptions
		)).name("Insert transaction data to transaction table");

		// Execute program, beginning computation.
		env.execute("Flink Ecommerce Realtime Streaming");
	}
}
