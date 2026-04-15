-- 1. departments 테이블 생성
CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    established_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

-- 2. files 테이블 생성
CREATE TABLE IF NOT EXISTS files (
    id BIGSERIAL PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    size BIGINT NOT NULL,
    storage_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

-- 3. employees 테이블 생성
CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    employee_number VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    department_id BIGINT NOT NULL,
    position VARCHAR(50) NOT NULL,
    status VARCHAR(10) NOT NULL,
    hire_date DATE NOT NULL,
    profile_image_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_profile_image FOREIGN KEY (profile_image_id) REFERENCES files(id)
);

-- 4. employee_audit_histories 테이블 생성
CREATE TABLE IF NOT EXISTS employee_audit_histories (
    id BIGSERIAL PRIMARY KEY,
    audit_type VARCHAR(20) NOT NULL,
    target_employee_no VARCHAR(50) NOT NULL,
    changed_content JSONB NOT NULL,
    memo VARCHAR(255),
    ip_address VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

-- 5. backup_histories 테이블 생성
CREATE TABLE IF NOT EXISTS backup_histories (
    id BIGSERIAL PRIMARY KEY,
    worker VARCHAR(45) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    status VARCHAR(10) NOT NULL,
    file_id BIGINT,
    CONSTRAINT fk_backup_file FOREIGN KEY (file_id) REFERENCES files(id)
);