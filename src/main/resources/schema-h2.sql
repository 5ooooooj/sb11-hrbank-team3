-- 1. departments 테이블 생성
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    established_date DATE NOT NULL,
    department_mail VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_department_name ON departments (name);
CREATE INDEX IF NOT EXISTS idx_department_desc ON departments (description);

-- 2. files 테이블 생성
CREATE TABLE IF NOT EXISTS files (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    size BIGINT NOT NULL,
    storage_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 3. employees 테이블 생성
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_number VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    department_id BIGINT NOT NULL,
    position VARCHAR(50) NOT NULL,
    status VARCHAR(10) NOT NULL,
    hire_date DATE NOT NULL,
    profile_image_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_profile_image FOREIGN KEY (profile_image_id) REFERENCES files(id)
);
CREATE INDEX IF NOT EXISTS idx_emp_name ON employees (name);
CREATE INDEX IF NOT EXISTS idx_emp_email ON employees (email);
CREATE INDEX IF NOT EXISTS idx_emp_department ON employees (department_id);
CREATE INDEX IF NOT EXISTS idx_emp_position ON employees (position);
CREATE INDEX IF NOT EXISTS idx_employ_number ON employees (employee_number);
CREATE INDEX IF NOT EXISTS idx_emp_status_hire_date ON employees (status, hire_date DESC);

-- 4. employee_audit_histories 테이블 생성
CREATE TABLE IF NOT EXISTS employee_audit_histories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    audit_type VARCHAR(20) NOT NULL,
    target_employee_no VARCHAR(50) NOT NULL,
    changed_content TEXT NOT NULL,
    memo VARCHAR(255),
    ip_address VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_target_employee_no ON employee_audit_histories (target_employee_no);
CREATE INDEX IF NOT EXISTS idx_memo ON employee_audit_histories (memo);
CREATE INDEX IF NOT EXISTS idx_ip_address ON employee_audit_histories (ip_address);
CREATE INDEX IF NOT EXISTS idx_audit_type_created_at ON employee_audit_histories (audit_type, created_at DESC);

-- 5. backup_histories 테이블 생성
CREATE TABLE IF NOT EXISTS backup_histories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    worker VARCHAR(45) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL,
    file_id BIGINT,
    CONSTRAINT fk_backup_file FOREIGN KEY (file_id) REFERENCES files(id)
);
CREATE INDEX IF NOT EXISTS idx_backup_histories_status_started_at ON backup_histories (status, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_worker ON backup_histories (worker);

-- 6. notifications 테이블 생성
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    department_id BIGINT,
    recipient_email VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_notification_department FOREIGN KEY (department_id) REFERENCES departments(id)
    );
-- 입사 알림만, 백업 알림만 조회
CREATE INDEX IF NOT EXISTS idx_notification_event_type ON notifications (event_type);
-- 특정 부서 발송된 알림 조회, FK 컬럼이며 조인시 성능 향상
CREATE INDEX IF NOT EXISTS idx_notification_department_id ON notifications (department_id);
-- FAILED 알림만 조회 후 재발송시 유용
CREATE INDEX IF NOT EXISTS idx_notification_status ON notifications (status);
-- 최근 발송된 알림 순
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON notifications (created_at DESC);