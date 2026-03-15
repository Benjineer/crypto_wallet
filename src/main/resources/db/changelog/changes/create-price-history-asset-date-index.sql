CREATE INDEX idx_price_history_asset_date ON price_history (asset_id, price_timestamp DESC);

CREATE INDEX idx_asset_symbol ON asset(symbol);