DROP TABLE IF EXISTS bulk_trx;
CREATE TABLE  bulk_trx(
                                      id BIGSERIAL PRIMARY KEY,
                                  batch_id TEXT NOT NULL REFERENCES bulk_bah(batch_id),
                                  transaction_id TEXT NOT NULL,
                                  from_account TEXT NOT NULL,
                                  to_account TEXT NOT NULL,
                                  amount NUMERIC(18,2) NOT NULL,
                                  status TEXT NOT NULL,
                                  failure_reason TEXT,
                                  attempts INT DEFAULT 0,
                                  date_created TIMESTAMP WITH TIME ZONE DEFAULT now(),
                                  UNIQUE (batch_id, transaction_id)
);