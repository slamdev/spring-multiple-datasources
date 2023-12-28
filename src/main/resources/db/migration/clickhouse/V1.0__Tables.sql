CREATE TABLE product
(
    id   String,
    name String
) ENGINE = MergeTree ORDER BY id;
