package com.interswitch.bulktransaction.controller.constants;

import lombok.Getter;

public class ApplicationUrl {
    public static final String BaseUrl = "api/v1";

    public static final String SubmitBulkUrl = "submit/bulk-transactions";

    public static final String ViewBulkStatusUrl = "bulk-transactions/status";

    public static final String ClientLogin = "authenticate";

    public static final String StatusUrl=   "/bulk-transactions/{batchId}/status";

}
