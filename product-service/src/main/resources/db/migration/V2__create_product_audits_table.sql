CREATE TABLE product_audits (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT,
    action VARCHAR(255),
    created_at DATETIME(6),

    PRIMARY KEY (id)
);