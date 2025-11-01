CREATE TABLE bulk_bah (
                            id BIGSERIAL PRIMARY KEY,
                            batch_id TEXT UNIQUE NOT NULL,
                            status TEXT NOT NULL,
                            submitter_id TEXT,
                             date_created TIMESTAMP WITH TIME ZONE DEFAULT now()
);