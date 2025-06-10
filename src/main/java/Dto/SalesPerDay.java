package Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@AllArgsConstructor
@Data
public class SalesPerDay {
    private LocalDate TransactionDate;
    private double TotalSales;
}
