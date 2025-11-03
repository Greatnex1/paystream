package com.interswitch.bulktransaction.dto;

//import com.interswitch.bulktransaction.dto.request.TransactionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class BulkRequestDto {
    @NotEmpty
   private String batchId;

   private List<TransactionRequest> transactions;


   @Data
   @AllArgsConstructor
   public static class TransactionRequest {
      @NotBlank
      private String transactionId;
      @NotEmpty
      private String fromAccount;
      @NotEmpty
      private String toAccount;
      @NotNull
      @Positive
      private BigDecimal amount;
   }


}
