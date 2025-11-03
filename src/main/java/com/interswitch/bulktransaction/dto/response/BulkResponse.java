package com.interswitch.bulktransaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


/**
 * Represents the response returned after processing a bulk transaction request.
 * <p>
 * This class encapsulates the overall batch identifier and the outcome
 * of each individual transaction within that batch.
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * List<BulkResponse.TransactionResult> results = List.of(
 *     new BulkResponse.TransactionResult("TXN001", "SUCCESS", null),
 *     new BulkResponse.TransactionResult("TXN002", "FAILED", "Insufficient funds")
 * );
 *
 * BulkResponse response = new BulkResponse("BATCH-20251102-001", results);
 * }</pre>
 *
 * <p><b>Example JSON:</b></p>
 * <pre>{@code
 * {
 *   "batchId": "BATCH-20251102-001",
 *   "results": [
 *     {
 *       "transactionId": "TXN001",
 *       "status": "SUCCESS",
 *       "reason": null
 *     },
 *     {
 *       "transactionId": "TXN002",
 *       "status": "FAILED",
 *       "reason": "Insufficient funds"
 *     }
 *   ]
 * }
 * }</pre>
 */

@Data
@AllArgsConstructor
public class BulkResponse {
   /**
    * A unique identifier assigned to the bulk transaction batch.
    * Used to track and correlate all individual transaction results.
    */
   private String batchId;

   /**
    * A list of transaction results representing the processing outcome
    * of each transaction in the batch.
    */
   private List<TransactionResult> results;



   /**
    * Represents the result of processing a single transaction within a bulk request.
    */
   @Data
   @AllArgsConstructor
   public static class TransactionResult {
      /**
       * Unique identifier for the individual transaction.
       */
      private String transactionId;
      /**
       * Indicates the transaction outcome.
       * <p>Possible values:</p>
       * <ul>
       *   <li><b>SUCCESS</b> – Transaction processed successfully.</li>
       *   <li><b>FAILED</b> – Transaction failed during processing.</li>
       * </ul>
       */
      private String status; // SUCCESS or FAILED

      /**
       * Provides a descriptive message or failure reason when the transaction fails.
       * This field may be {@code null} or empty for successful transactions.
       */
      private String reason;
   }
}
