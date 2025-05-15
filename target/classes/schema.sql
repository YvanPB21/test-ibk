CREATE TABLE IF NOT EXISTS PRODUCT (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    version BIGINT DEFAULT 0 NOT NULL,
    CONSTRAINT CHK_ProductStock CHECK (stock >= 0)
    );

CREATE TABLE IF NOT EXISTS ORDERS (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      date TIMESTAMP NOT NULL,
                                      total_gross DECIMAL(10, 2) NULL,
    total_final DECIMAL(10, 2) NULL,
    state VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS ORDERS_ITEM (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           orders_id BIGINT NOT NULL,
                                           product_id BIGINT NOT NULL,
                                           quantity INT NOT NULL,
                                           unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (orders_id) REFERENCES ORDERS(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES PRODUCT(id)
    );

