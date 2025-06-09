package Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SalesPerCategory {
    private String Category;
    private double TotalSales;
}
