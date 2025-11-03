package com.interswitch.bulktransaction.controller.constants;

import lombok.Getter;

public class ApplicationUrl {
    public static final String BaseUrl = "/api/v1/bulk-transactions";

    public static final String SubmitBulkUrl = "/api/v1/submit/bulk-transactions";

    public static final String ViewBulkStatusUrl = "/api/v1/bulk-transactions/status";

    public static final String ClientLogin = "/api/v1/authenticate";

    public static final String StatusUrl=   "/api/v1/bulk-transactions/{batchId}/status";

}
