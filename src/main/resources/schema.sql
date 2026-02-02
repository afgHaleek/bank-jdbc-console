-- customers
CREATE TABLE IF NOT EXISTS customers (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(150) UNIQUE NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- Accounts
CREATE TABLE IF NOT EXISTS accounts(
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    account_no  VARCHAR(20) UNIQUE NOT NULL,
    type        VARCHAR(20) NOT NULL CHECK (type IN ('CHECKING', 'SAVINGS')),
    balance     NUMERIC(15,2) NOT NULL CHECK (balance >= 0),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Transaction ledger
CREATE TABLE IF NOT EXISTS transactions(
    id          BIGSERIAL PRIMARY KEY,
    account_id  BIGINT NOT NULL REFERENCES accounts(id),
    tx_type     VARCHAR(30) NOT NULL,
    amount      NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    reference   TEXT,
    transfer_id UUID,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);