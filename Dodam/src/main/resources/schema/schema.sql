-- Dodam Pay 상품 도메인 스키마 정의
-- H2 Database용 DDL

-- 카테고리 테이블
CREATE TABLE IF NOT EXISTS CATEGORY (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    parent_category_id BIGINT,
    category_path VARCHAR(100),
    display_order INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES CATEGORY(category_id)
);

-- 브랜드 테이블
CREATE TABLE IF NOT EXISTS BRAND (
    brand_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_name VARCHAR(50) NOT NULL UNIQUE,
    brand_logo_url VARCHAR(200),
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 상품 테이블
CREATE TABLE IF NOT EXISTS PRODUCT (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT,
    price DECIMAL(15,2) NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES CATEGORY(category_id),
    CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES BRAND(brand_id),
    CONSTRAINT chk_product_price CHECK (price >= 0),
    CONSTRAINT chk_product_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'DELETED'))
);

-- 상품 상세 정보 테이블
CREATE TABLE IF NOT EXISTS PRODUCT_DETAIL (
    detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    description TEXT,
    specifications TEXT,
    features TEXT,
    care_instructions VARCHAR(1000),
    warranty_info VARCHAR(500),
    origin_country VARCHAR(50),
    material VARCHAR(200),
    dimensions VARCHAR(100),
    weight VARCHAR(50),
    
    CONSTRAINT fk_product_detail_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id) ON DELETE CASCADE
);

-- 상품 옵션 테이블
CREATE TABLE IF NOT EXISTS PRODUCT_OPTION (
    option_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    option_type VARCHAR(20) NOT NULL,
    option_name VARCHAR(50) NOT NULL,
    option_value VARCHAR(100) NOT NULL,
    additional_price DECIMAL(15,2) DEFAULT 0,
    stock_quantity INT,
    display_order INT,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_product_option_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id) ON DELETE CASCADE,
    CONSTRAINT chk_option_type CHECK (option_type IN ('COLOR', 'SIZE', 'MATERIAL', 'CAPACITY', 'STYLE', 'OTHER')),
    CONSTRAINT chk_option_price CHECK (additional_price >= 0),
    CONSTRAINT chk_option_stock CHECK (stock_quantity >= 0)
);

-- 상품 이미지 테이블
CREATE TABLE IF NOT EXISTS PRODUCT_IMAGE (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_type VARCHAR(20) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    image_order INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    file_size BIGINT,
    width INT,
    height INT,
    
    CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id) ON DELETE CASCADE,
    CONSTRAINT chk_image_type CHECK (image_type IN ('THUMBNAIL', 'DETAIL', 'GALLERY', 'OPTION')),
    CONSTRAINT chk_image_order CHECK (image_order >= 0)
);

-- 재고 테이블
CREATE TABLE IF NOT EXISTS INVENTORY (
    inventory_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT DEFAULT 10,
    version BIGINT DEFAULT 0,
    last_restocked_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id) ON DELETE CASCADE,
    CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_reserved CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_inventory_available CHECK (available_quantity >= 0),
    CONSTRAINT chk_inventory_consistency CHECK (available_quantity = quantity - reserved_quantity)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_product_name ON PRODUCT(product_name);
CREATE INDEX IF NOT EXISTS idx_product_category ON PRODUCT(category_id);
CREATE INDEX IF NOT EXISTS idx_product_brand ON PRODUCT(brand_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON PRODUCT(status);
CREATE INDEX IF NOT EXISTS idx_product_composite ON PRODUCT(category_id, brand_id, status);

CREATE INDEX IF NOT EXISTS idx_category_parent ON CATEGORY(parent_category_id);
CREATE INDEX IF NOT EXISTS idx_category_path ON CATEGORY(category_path);

CREATE INDEX IF NOT EXISTS idx_brand_name ON BRAND(brand_name);
CREATE INDEX IF NOT EXISTS idx_brand_active ON BRAND(is_active);

CREATE INDEX IF NOT EXISTS idx_product_option_product ON PRODUCT_OPTION(product_id);
CREATE INDEX IF NOT EXISTS idx_product_option_type ON PRODUCT_OPTION(option_type);

CREATE INDEX IF NOT EXISTS idx_product_image_product ON PRODUCT_IMAGE(product_id);
CREATE INDEX IF NOT EXISTS idx_product_image_type ON PRODUCT_IMAGE(image_type);

CREATE INDEX IF NOT EXISTS idx_inventory_product ON INVENTORY(product_id);