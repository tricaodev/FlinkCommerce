package Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Transaction {
    private UUID TransactionId;
    private String ProductId;
    private String ProductName;
    private String ProductCategory;
    private double ProductPrice;
    private int ProductQuantity;
    private double TotalSales;
    private String ProductBrand;
    private String Currency;
    private String CustomerId;
    private LocalDateTime TransactionDate;
    private String PaymentMethod;

    public Transaction(
            UUID transactionId,
            String productId,
            String productName,
            String productCategory,
            double productPrice,
            int productQuantity,
            String productBrand,
            String currency,
            String customerId,
            LocalDateTime transactionDate,
            String paymentMethod) {
        TransactionId = transactionId;
        ProductId = productId;
        ProductName = productName;
        ProductCategory = productCategory;
        ProductPrice = productPrice;
        ProductQuantity = productQuantity;
        TotalSales = productPrice * productQuantity;
        ProductBrand = productBrand;
        Currency = currency;
        CustomerId = customerId;
        TransactionDate = transactionDate;
        PaymentMethod = paymentMethod;
    }
}
