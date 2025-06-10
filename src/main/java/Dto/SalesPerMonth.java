package Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SalesPerMonth {
    private int Year;
    private int Month;
    private double TotalSales;
}
