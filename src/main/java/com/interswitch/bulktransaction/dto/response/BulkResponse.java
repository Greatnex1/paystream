package com.interswitch.bulktransaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkResponse {
   private String batchId;
   private List<TransactionResult> results;

   @Data
   @AllArgsConstructor
   public static class TransactionResult {
      private String transactionId;
      private String status; // SUCCESS or FAILED
      private String reason;
   }
}
