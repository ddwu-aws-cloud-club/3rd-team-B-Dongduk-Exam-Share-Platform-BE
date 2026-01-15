----- 해당 스크립트 관련 DB 명세서: https://docs.google.com/spreadsheets/d/1J5KTlDi9ryr6KpPXNnzBmpov41ECWbR65TL9poOEeCg/edit?usp=sharing

------ 기존 테이블(or 컬럼)이 있을 시, 삭제 ------
DROP TABLE IF EXISTS downloads CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS point_history CASCADE;
ALTER TABLE public.users
DROP COLUMN IF EXISTS points CASCADE;

------ posts 테이블 생성 스크립트 ------
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    subject VARCHAR(50) NOT NULL,
    professor VARCHAR(20) NOT NULL,
    major VARCHAR(50) NOT NULL,

    -- 파일 관련 정보
    pdf_url VARCHAR(1000) NOT NULL,
    pdf_key VARCHAR(500), -- s3키 추가
    original_filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,

    -- 작성자 정보
    uploader_id BIGINT NOT NULL REFERENCES users(id),
    uploader_email VARCHAR(255) NOT NULL,

    -- 통계 및 포인트
    download_count INT DEFAULT 0,
    points INT DEFAULT 50,

    -- 시간 정보
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,       --이슈 예시에는 created_at으로 되어있는데, 호환이 안 돼서 이름 변경
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

------ downloads 테이블 생성 스크립트 ------
CREATE TABLE downloads (
    id BIGSERIAL PRIMARY KEY,

    --User, Post
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,

    -- 언제 다운로드했는지: downloadDate
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 외래키 연걸 (기록 동시 삭제)
    CONSTRAINT fk_downloads_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_downloads_post FOREIGN KEY (post_id) REFERENCES public.posts(id) ON DELETE CASCADE,

    -- 중복 구매 방지
    CONSTRAINT uk_downloads_user_post UNIQUE (user_id, post_id)
);

-- 인덱스 (조회용)
CREATE INDEX idx_downloads_user_date ON downloads(user_id, created_at DESC);

------ point history 테이블 생성 -------
CREATE TABLE point_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_id BIGINT,

    amount INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    balance_after INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 (조회용)
CREATE INDEX idx_point_history_user_id ON point_history(user_id);

----- Users 테이블 Points 컬럼 생성 스크립트 -----
ALTER TABLE public.users
ADD COLUMN points INT NOT NULL DEFAULT 0;

----- 권한 문제가 생길 시 가이드라인 ----
-- pgAdmin에서 위의 스크립트를 실행하고 인텔리제이에서 프로젝트 실행 시,
-- 스프링 부트가 테이블 권한이 없다고 판단해 오류를 낼 수 있습니다.
-- 해결 시에 두가지 방법이 있는데,
-- 1. 우선은 테이블 권한을 다시 스프링 부트에게 주는 것입니다.
-- 이 방법은 여러분의 DB가 명세서와 달라질 수 있으니 주의를 요합니다.
ALTER TABLE point_history OWNER TO somshare_user;
ALTER TABLE downloads OWNER TO somshare_user;
ALTER TABLE posts OWNER TO somshare_user;
-- 2. 만약 스프링 부트가 테이블을 수정하는 것을 막고 싶을 때는, 위를 입력하지 말고
-- yaml 파일에서 jpa hibernate ddl-auto를 validate로 바꿔주셔야 합니다!
-- 그리고 아래와 같이 작성해주셔야, 스키마 변경 권한은 주지 않고 이용 가능합니다.
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO somshare_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO somshare_user;