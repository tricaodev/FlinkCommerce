package Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Data
public class Transaction {
    private UUID transactionId;
    private String productId;
    private String productName;
    private String productCategory;
    private float productPrice;
    private int productQuantity;
    private String productBrand;
    private String currency;
    private String customerId;
    private LocalDateTime transactionDate;
    private String paymentMethod;
}
