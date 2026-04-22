CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 1. departments 테이블 생성
CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    established_date DATE NOT NULL,
    department_mail VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
-- 이름 또는 설명 (부분 일치 조건으로 인덱스 생성)
CREATE INDEX IF NOT EXISTS idx_department_name_desc ON departments USING gin(name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_department_description ON departments USING gin(description gin_trgm_ops);

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
-- 이름 또는 이메일 (부분 일치 조건으로 인덱스 생성)
CREATE INDEX IF NOT EXISTS idx_name ON employees USING gin(name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_email ON employees USING gin(email gin_trgm_ops);
-- 부서ID (특정 부서 직원 검색(WHERE), 참조 무결성 연산(CASCADE/SET NULL) 및  nested loop 조인 성능 최적화)
CREATE INDEX IF NOT EXISTS idx_emp_department_id ON employees (department_id);
-- 직함 (부분 일치)
CREATE INDEX IF NOT EXISTS idx_position ON employees (position);
-- 사원번호 (부분 일치)
CREATE INDEX IF NOT EXISTS idx_employee_number ON employees USING gin(employee_number gin_trgm_ops);
-- 상태 및 입사일 (완전 일치인 상태를 앞에 두어 상태를 기준으로 먼저 정렬 후 입사일 범위 조건)
CREATE INDEX IF NOT EXISTS idx_status_hire_date ON employees (status, hire_date DESC);

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
-- 대상 직원 사번 (부분 일치)
CREATE INDEX IF NOT EXISTS idx_target_employee_no ON employee_audit_histories USING gin(target_employee_no gin_trgm_ops);
-- 메모 (부분 일치)
CREATE INDEX IF NOT EXISTS idx_memo ON employee_audit_histories USING gin(memo gin_trgm_ops);
-- IP주소 (부분 일치)
CREATE INDEX IF NOT EXISTS idx_ip_address ON employee_audit_histories USING gin(ip_address gin_trgm_ops);
-- 유형 및 시간 (완전 일치인 유형을 앞에 두어 유형을 기준으로 먼저 정렬 후 시간 범위 조건)
CREATE INDEX IF NOT EXISTS idx_audit_type_created_at ON employee_audit_histories (audit_type, created_at DESC);

-- 5. backup_histories 테이블 생성
CREATE TABLE IF NOT EXISTS backup_histories (
    id BIGSERIAL PRIMARY KEY,
    worker VARCHAR(45) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    file_id BIGINT,
    CONSTRAINT fk_backup_file FOREIGN KEY (file_id) REFERENCES files(id)
);
-- 상태 및 시작 시간
CREATE INDEX IF NOT EXISTS idx_backup_histories_status_started_at ON backup_histories (status, started_at DESC);
-- 작업자 (부분 일치)
CREATE INDEX IF NOT EXISTS idx_worker ON backup_histories USING gin(worker gin_trgm_ops);

-- 6. notifications 테이블 생성
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    department_id BIGINT,
    recipient_email VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_notification_department FOREIGN KEY (department_id) REFERENCES departments(id)
);
-- 부서ID (조인 성능 향상)
CREATE INDEX IF NOT EXISTS idx_notification_department_id ON notifications (department_id);
-- 이벤트 유형 및 생성 시간
CREATE INDEX IF NOT EXISTS idx_notification_event_type_created_at ON notifications (event_type, created_at DESC);
-- 상태 (발송 실패 재처리 시)
CREATE INDEX IF NOT EXISTS idx_notification_status ON notifications (status);
-- 최근 발송된 알림 순
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON notifications (created_at DESC);