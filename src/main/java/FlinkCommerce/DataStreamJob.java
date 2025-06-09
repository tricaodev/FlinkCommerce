package FlinkCommerce;

import Deserializer.JSONValueDeserializationSchema;
import Dto.SalesPerCategory;
import Dto.Transaction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.sql.Timestamp;

public class DataStreamJob {
	private static final String jdbcUrl = "jdbc:postgresql://postgres:5432/postgres";
	private static final String username = "admin";
	private static final String password = "postgres";

	public static void main(String[] args) throws Exception {
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
				.setGroupId("flink-ecommerce")
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
				"CREATE TABLE IF NOT EXISTS transactions ( " +
						"transaction_id UUID PRIMARY KEY, " +
						"product_id VARCHAR(255), " +
						"product_name VARCHAR(255), " +
						"product_category VARCHAR(255), " +
						"product_price DOUBLE PRECISION, " +
						"product_quantity INTEGER, " +
						"total_sales DOUBLE PRECISION, " +
						"product_brand VARCHAR(255), " +
						"currency VARCHAR(255), " +
						"customer_id VARCHAR(255), " +
						"transaction_date TIMESTAMP, " +
						"payment_method VARCHAR(255) " +
						") ",
				(preparedStatement, transaction) -> {

				},
				executionOptions,
				connectionOptions
		)).name("Create transactions table");

		// Insert transaction data to transaction table
		transactionDataStream.addSink(JdbcSink.sink(
				"INSERT INTO transactions " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
						"ON CONFLICT (transaction_id) DO UPDATE SET " +
						"product_id = EXCLUDED.product_id, " +
						"product_name = EXCLUDED.product_name, " +
						"product_category = EXCLUDED.product_category, " +
						"product_price = EXCLUDED.product_price, " +
						"product_quantity = EXCLUDED.product_quantity, " +
						"total_sales = EXCLUDED.total_sales, " +
						"product_brand = EXCLUDED.product_brand, " +
						"currency = EXCLUDED.currency, " +
						"customer_id = EXCLUDED.customer_id, " +
						"transaction_date = EXCLUDED.transaction_date, " +
						"payment_method = EXCLUDED.payment_method ",
				(preparedStatement, transaction) -> {
					preparedStatement.setObject(1, transaction.getTransactionId());
					preparedStatement.setString(2, transaction.getProductId());
					preparedStatement.setString(3, transaction.getProductName());
					preparedStatement.setString(4, transaction.getProductCategory());
					preparedStatement.setDouble(5, transaction.getProductPrice());
					preparedStatement.setInt(6, transaction.getProductQuantity());
					preparedStatement.setDouble(7, transaction.getTotalSales());
					preparedStatement.setString(8, transaction.getProductBrand());
					preparedStatement.setString(9, transaction.getCurrency());
					preparedStatement.setString(10, transaction.getCustomerId());
					preparedStatement.setTimestamp(11, Timestamp.valueOf(transaction.getTransactionDate()));
					preparedStatement.setString(12, transaction.getPaymentMethod());
				},
				executionOptions,
				connectionOptions
		)).name("Insert transaction data to transactions table");

		// Create sales_per_category table
		transactionDataStream.addSink(JdbcSink.sink(
				"CREATE TABLE IF NOT EXIST sales_per_category ( " +
						"category VARCHAR(255) PRIMARY KEY, " +
						"total_sales DOUBLE PRECISION " +
						")",
				(preparedStatement, transaction) -> {

				},
				executionOptions,
				connectionOptions
		)).name("Create sales_per_category table");

		// Insert data to sales_per_category table
//		transactionDataStream.map(transaction -> new SalesPerCategory(transaction.getProductCategory(), transaction.getTotalSales()))
//				.keyBy(SalesPerCategory::getCategory)
//				.reduce((value1, value2) -> {
//					value1.setTotalSales(value1.getTotalSales() + value2.getTotalSales());
//					return value1;
//				}).addSink(JdbcSink.sink(
//						"INSERT"
//				))

		// Create sales_per_day table
		transactionDataStream.addSink(JdbcSink.sink(
				"",
				(preparedStatement, transaction) -> {

				},
				executionOptions,
				connectionOptions
		)).name("Create sales_per_day table");

		// Create sales_per_month table
		transactionDataStream.addSink(JdbcSink.sink(
				"",
				(preparedStatement, transaction) -> {

				},
				executionOptions,
				connectionOptions
		)).name("Create sales_per_month table");

		// Execute program, beginning computation.
		env.execute("Flink Ecommerce Realtime Streaming");
	}
}
