CREATE TABLE vnstock.foreign_trading (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	symbol VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	buy_volume BIGINT UNSIGNED NOT NULL,
	sell_volume BIGINT UNSIGNED NOT NULL,
	buy_value BIGINT UNSIGNED NOT NULL,
	sell_value BIGINT UNSIGNED NOT NULL,
	trading_date DATE NOT NULL,
	hash_date varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;


CREATE INDEX idx_symbol_hash_date ON foreign_trading (symbol, hash_date);

CREATE UNIQUE INDEX index_hash_date ON foreign_trading (hash_date);  

-- INSERT INTO vnstock.foreign_trading (symbol, buy_volume, sell_volume, buy_value, sell_value, trading_date, hash_date)
-- VALUES
--     ('ABC', 1000, 800, 50000, 40000, '2023-12-13', 'some_hash_value'),
--     ('XYZ', 1500, 1200, 75000, 60000, '2023-12-14', 'another_hash_value');

CREATE TABLE proprietary_trading (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	symbol VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	buy_volume BIGINT UNSIGNED NOT NULL,
	sell_volume BIGINT UNSIGNED NOT NULL,
	buy_value BIGINT UNSIGNED NOT NULL,
	sell_value BIGINT UNSIGNED NOT NULL,
	trading_date DATE NOT NULL,
	hash_date varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;


CREATE INDEX idx_symbol_hash_date ON proprietary_trading (symbol, hash_date);
CREATE UNIQUE INDEX index_hash_date ON proprietary_trading (hash_date);  

CREATE TABLE intraday_order (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	symbol VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	buy_order BIGINT UNSIGNED NOT NULL,
	sell_order BIGINT UNSIGNED NOT NULL,
	buy_volume BIGINT UNSIGNED NOT NULL,
	sell_volume	 BIGINT UNSIGNED NOT NULL,
	trading_date DATE NOT NULL,
	hash_date varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;


CREATE INDEX idx_symbol_hash_date ON intraday_order (symbol, hash_date);
CREATE UNIQUE INDEX index_hash_date ON intraday_order (hash_date);  


CREATE TABLE order_book (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	symbol VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	buy_order BIGINT UNSIGNED NOT NULL,
	sell_order BIGINT UNSIGNED NOT NULL,
	buy_volume BIGINT UNSIGNED NOT NULL,
	sell_volume	 BIGINT UNSIGNED NOT NULL,
	small_buy_order BIGINT UNSIGNED NOT NULL,
	small_sell_order BIGINT UNSIGNED NOT NULL,
	medium_buy_order BIGINT UNSIGNED NOT NULL,
	medium_sell_order BIGINT UNSIGNED NOT NULL,
	large_buy_order BIGINT UNSIGNED NOT NULL,
	large_sell_order BIGINT UNSIGNED NOT NULL,
	trading_date DATE NOT NULL,
	hash_date varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;


CREATE INDEX idx_symbol_hash_date ON order_book (symbol, hash_date);
CREATE UNIQUE INDEX index_hash_date ON order_book (hash_date);  

CREATE TABLE stock_price (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	symbol VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	highest_price INT UNSIGNED NOT NULL,
	lowest_price INT UNSIGNED NOT NULL,
	open_price INT UNSIGNED NOT NULL,
	close_price INT UNSIGNED NOT NULL,
	percentage_change VARCHAR(10) NOT NULL,
	price_change INT NULL,
	price_range VARCHAR(10) NULL,
	total_volume BIGINT UNSIGNED NOT NULL,
	trading_date DATE NOT NULL,
	hash_date varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;


CREATE INDEX idx_symbol_hash_date ON stock_price (symbol, hash_date);
CREATE UNIQUE INDEX index_hash_date ON stock_price (hash_date);

CREATE TABLE derivatives_trading (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	symbol VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	foreign_buy_volume INT UNSIGNED NULL,
	foreign_sell_volume INT UNSIGNED NULL,
	foreign_net_volume INT NULL,
	foreign_buy_value BIGINT UNSIGNED NULL,
	foreign_sell_value BIGINT UNSIGNED NULL,
	foreign_net_value BIGINT  NULL,
	proprietary_buy_volume INT UNSIGNED NULL,
	proprietary_sell_volume INT UNSIGNED NULL,
	proprietary_net_volume INT NULL,
	proprietary_buy_value BIGINT UNSIGNED NULL,
	proprietary_sell_value BIGINT UNSIGNED NULL,
	proprietary_net_value BIGINT NULL,
	total_volume BIGINT UNSIGNED NOT NULL,
	open_interest BIGINT UNSIGNED NULL,
	percentage_change VARCHAR(10) NOT NULL,
	price_change INT NOT NULL,
	trading_date DATE NOT NULL,
	hash_date varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;

CREATE INDEX idx_symbol_hash_date ON derivatives_trading (symbol, hash_date);
CREATE UNIQUE INDEX index_hash_date ON derivatives_trading (hash_date);  

CREATE TABLE income_sheet (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	ticker VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	net_revenue BIGINT NOT NULL,
	gross_profit BIGINT NOT NULL,
	cogs BIGINT NOT NULL,
	finance_charge BIGINT NULL,
	interest_cost BIGINT NULL,
	selling_expenses BIGINT NULL,
	operating_expenses BIGINT NULL,
	financial_activities_income BIGINT NULL,
	other_income BIGINT NULL,
	earnings_before_taxes BIGINT NOT NULL,
	corporate_income_tax BIGINT NOT NULL,
	profit_after_taxes BIGINT NOT NULL,
	net_income_attributable_to_parent BIGINT NOT NULL,
	quarter VARCHAR(10) NOT NULL,
	hash_quarter varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;

CREATE INDEX idx_ticker_hash_quarter ON income_sheet (ticker, hash_quarter);
CREATE UNIQUE INDEX index_hash_quarter ON income_sheet (hash_quarter);  

CREATE TABLE balance_sheet (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	ticker VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	equity BIGINT UNSIGNED NOT NULL,
	total_assets BIGINT UNSIGNED NOT NULL,
	short_term_assets BIGINT UNSIGNED NOT NULL,
	long_term_assets BIGINT UNSIGNED NOT NULL,
	liabilities BIGINT UNSIGNED NOT NULL,
	current_liabilities BIGINT UNSIGNED NOT NULL,
	long_term_liabilities BIGINT UNSIGNED NOT NULL,
	cash_and_cash_equivalents BIGINT UNSIGNED NOT NULL,
	short_term_investments BIGINT UNSIGNED NOT NULL,
	accounts_receivable BIGINT UNSIGNED NOT NULL,
	inventory BIGINT UNSIGNED NOT NULL,
	quarter VARCHAR(10) NOT NULL,
	hash_quarter varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;

CREATE INDEX idx_ticker_hash_quarter ON balance_sheet (ticker, hash_quarter);
CREATE UNIQUE INDEX index_hash_quarter ON balance_sheet (hash_quarter);  


CREATE TABLE cash_flow (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	ticker VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	net_cash_flow_from_operating_activities BIGINT NOT NULL,
	net_cash_flow_from_investing_activities BIGINT NOT NULL,
	net_cash_flow_from_financing_activities BIGINT NOT NULL,
	net_cash_flow_for_the_period BIGINT NOT NULL,
	beginning_cash_and_cash_equivalents BIGINT NOT NULL,
	ending_cash_and_cash_equivalents BIGINT NOT NULL,
	quarter VARCHAR(10) NOT NULL,
	hash_quarter varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;

CREATE INDEX idx_ticker_hash_quarter ON cash_flow (ticker, hash_quarter);
CREATE UNIQUE INDEX index_hash_quarter ON cash_flow (hash_quarter);  

CREATE TABLE index_statistic (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	symbol VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	percentage_taken_on_index VARCHAR(10) NULL,
	volume BIGINT UNSIGNED NOT NULL,
	total_volume BIGINT UNSIGNED NOT NULL,
	trading_date DATE NOT NULL,
	hash_date varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;

CREATE INDEX idx_symbol_hash_date ON index_statistic (symbol, hash_date);
CREATE UNIQUE INDEX index_hash_date ON index_statistic (hash_date);

ALTER TABLE proprietary_trading 
ADD total_net_value bigint(20);


ALTER TABLE foreign_trading 
ADD total_net_value bigint(20);


CREATE TABLE foreign_statistic (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	symbol VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	1m_highest_buy_value BIGINT  NULL,
	1m_highest_buy_trading_date DATE NULL,
	1m_highest_sell_value BIGINT NULL,
	1m_highest_sell_trading_date DATE NULL,
	3m_highest_buy_value BIGINT NULL,
	3m_highest_buy_trading_date DATE NULL,
	3m_highest_sell_value BIGINT NULL,
	3m_highest_sell_trading_date DATE NULL,
	6m_highest_buy_value BIGINT NULL,
	6m_highest_buy_trading_date DATE NULL,
	6m_highest_sell_value BIGINT NULL,
	6m_highest_sell_trading_date DATE NULL,
	12m_highest_buy_value BIGINT NULL,
	12m_highest_buy_trading_date DATE NULL,
	12m_highest_sell_value BIGINT NULL,
	12m_highest_sell_trading_date DATE NULL,
	highest_buy_value BIGINT NULL,
	highest_buy_trading_date DATE NULL,
	highest_sell_value BIGINT NULL,
	highest_sell_trading_date DATE NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;


CREATE TABLE proprietary_statistic (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	symbol VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
	1m_highest_buy_value BIGINT  NULL,
	1m_highest_buy_trading_date DATE NULL,
	1m_highest_sell_value BIGINT NULL,
	1m_highest_sell_trading_date DATE NULL,
	3m_highest_buy_value BIGINT NULL,
	3m_highest_buy_trading_date DATE NULL,
	3m_highest_sell_value BIGINT NULL,
	3m_highest_sell_trading_date DATE NULL,
	6m_highest_buy_value BIGINT NULL,
	6m_highest_buy_trading_date DATE NULL,
	6m_highest_sell_value BIGINT NULL,
	6m_highest_sell_trading_date DATE NULL,
	12m_highest_buy_value BIGINT NULL,
	12m_highest_buy_trading_date DATE NULL,
	12m_highest_sell_value BIGINT NULL,
	12m_highest_sell_trading_date DATE NULL,
	highest_buy_value BIGINT NULL,
	highest_buy_trading_date DATE NULL,
	highest_sell_value BIGINT NULL,
	highest_sell_trading_date DATE NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;

CREATE TABLE trading_date (
	id BIGINT UNSIGNED auto_increment NOT NULL,
	trading_date DATE NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;