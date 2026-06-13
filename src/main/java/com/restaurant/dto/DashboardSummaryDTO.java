package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    private long totalOrdersToday;
    private long pendingOrders;
    private long preparingOrders;
    private long completedOrders;
    private BigDecimal revenueToday;
    private List<ItemStat> popularItems;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemStat {
        private String itemName;
        private long orderCount;
    }
}
