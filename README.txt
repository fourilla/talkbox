수정사항
ALTER TABLE APP_USER MODIFY (Password VARCHAR2(100)); -> 비밀번호를 해시값으로 저장하기 위해 100 자로 바꿈
Video 에 Title CLOB 이면 검색이 힘들어져서 VARCHAR2(4000) 으로 변경 -> 유튜브 제목 길이제한 100자
통일성 문제로 나머지 VARCHAR 전부 VARCHAR2 로 변경

1. src/main/resources/application.properties 를 본인 환경에 맞게 수정
2. maven 프로젝트라서 maven install 해줘야함

현재 테이블 구조
DROP TABLE REPLY CASCADE CONSTRAINTS;
DROP TABLE USER_COMMENT CASCADE CONSTRAINTS;
DROP TABLE GUESTBOOK CASCADE CONSTRAINTS;
DROP TABLE THREAD CASCADE CONSTRAINTS;
DROP TABLE VIDEO CASCADE CONSTRAINTS;
DROP TABLE CHANNEL CASCADE CONSTRAINTS;
DROP TABLE APP_USER CASCADE CONSTRAINTS;
DROP TABLE REACTION CASCADE CONSTRAINTS;

CREATE TABLE APP_USER(
    User_id VARCHAR2(29) NOT NULL,
    Login_id VARCHAR2(50) NOT NULL,
    Email VARCHAR2(50),
    Password VARCHAR2(100) NOT NULL,
    PRIMARY KEY(User_id)
);

CREATE TABLE CHANNEL(
    Channel_id VARCHAR2(24) NOT NULL,
    Name VARCHAR2(100) NOT NULL,
    Description CLOB,
    PRIMARY KEY(Channel_id)
);

CREATE TABLE VIDEO(
    Video_id VARCHAR2(11) NOT NULL,
    Channel_id VARCHAR2(24) NOT NULL,
    Title VARCHAR2(4000) NOT NULL,
    Description CLOB,
    Uploaded_at DATE NOT NULL,
    Like_count NUMBER NOT NULL,
    Dislike_count NUMBER NOT NULL,
    Comment_count NUMBER NOT NULL,
    PRIMARY KEY(Video_id)
);

ALTER TABLE VIDEO
    ADD CONSTRAINT VIDEO_REF_CHANNEL
    FOREIGN KEY(Channel_id) REFERENCES CHANNEL(Channel_id);

CREATE TABLE THREAD(
    Thread_id VARCHAR2(11) NOT NULL,
    Created_at DATE NOT NULL,
    Participant_count NUMBER,
    PRIMARY KEY(Thread_id)
);

ALTER TABLE THREAD
    ADD CONSTRAINT THREAD_REF_VIDEO
    FOREIGN KEY(Thread_id) REFERENCES VIDEO(Video_id);

CREATE TABLE GUESTBOOK(
    Guestbook_id VARCHAR2(29) NOT NULL,
    Created_at DATE NOT NULL,
    PRIMARY KEY(Guestbook_id)
);

ALTER TABLE GUESTBOOK
    ADD CONSTRAINT GUESTBOOK_REF_USER
    FOREIGN KEY(Guestbook_id) REFERENCES APP_USER(User_id);

CREATE TABLE USER_COMMENT(
    Comment_id VARCHAR2(26) NOT NULL,
    User_id VARCHAR2(29) NOT NULL,
    Thread_id VARCHAR2(11),
    Guestbook_id VARCHAR2(29),
    Content CLOB NOT NULL,
    Created_at DATE NOT NULL,
    Updated_at DATE,
    Like_count NUMBER NOT NULL,
    Dislike_count NUMBER NOT NULL,
    PRIMARY KEY(Comment_id)
);

ALTER TABLE USER_COMMENT
    ADD CONSTRAINT UC_REF_USER
    FOREIGN KEY(User_id) REFERENCES APP_USER(User_id);

ALTER TABLE USER_COMMENT
    ADD CONSTRAINT UC_REF_THREAD
    FOREIGN KEY(Thread_id) REFERENCES THREAD(Thread_id);

ALTER TABLE USER_COMMENT
    ADD CONSTRAINT UC_REF_GUESTBOOK
    FOREIGN KEY(Guestbook_id) REFERENCES GUESTBOOK(Guestbook_id);

CREATE TABLE REPLY(
    Reply_id VARCHAR2(50) NOT NULL,
    Comment_id VARCHAR2(26) NOT NULL,
    User_id VARCHAR2(29) NOT NULL,
    Content CLOB NOT NULL,
    Created_at DATE NOT NULL,
    Updated_at DATE,
    Like_count NUMBER NOT NULL,
    Dislike_count NUMBER NOT NULL,
    PRIMARY KEY(Reply_id)
);

ALTER TABLE REPLY
    ADD CONSTRAINT REPLY_REF_USERCOMMENT
    FOREIGN KEY(Comment_id) REFERENCES USER_COMMENT(Comment_id);

ALTER TABLE REPLY
    ADD CONSTRAINT REPLY_REF_USER
    FOREIGN KEY(User_id) REFERENCES APP_USER(User_id);


CREATE TABLE REACTION (
    User_id    VARCHAR2(29) NOT NULL,
    Target_id  VARCHAR2(50) NOT NULL,  -- Comment_id 또는 Reply_id
    Reaction_type CHAR(1) NOT NULL,    -- 'L' = Like, 'D' = Dislike
    Created_at DATE NOT NULL,
    PRIMARY KEY(User_id, Target_id)
);

ALTER TABLE REACTION
    ADD CONSTRAINT REACTION_REF_USER
    FOREIGN KEY(User_id) REFERENCES APP_USER(User_id);

====================================================================

1. 패키지(폴더)별 역할

1.1 config (설정)
   - DataSourceConfig.java
     역할: 데이터베이스 연결 및 트랜잭션 관리 설정
     주요 기능: TransactionAwareDataSourceProxy를 사용하여 @Transactional이 제대로 작동하도록 설정
     중요: DAO에서 getConnection() 호출 시 트랜잭션 컨텍스트의 Connection을 재사용하도록 보장
   
   - SecurityConfig.java
     역할: Spring Security 설정 (인증, 권한 관리)
     주요 기능: 로그인/로그아웃, API 엔드포인트 접근 권한 설정

1.2 controller (HTTP 요청 처리)
   - AdminController.java
     역할: 관리자 페이지 및 관리자 전용 API 처리
     주요 기능: 통계 조회, 사용자 관리, 콘텐츠 관리
     보안: "admin" 아이디만 접근 가능
   
   - AuthController.java
     역할: 인증 관련 페이지 라우팅 (로그인, 회원가입 페이지)
   
   - AuthApiController.java
     역할: 인증 관련 REST API (회원가입, 회원정보 수정, 중복 체크)
     주요 기능: 회원가입, 회원정보 수정, 아이디/이메일 중복 확인
   
   - CommentRestController.java
     역할: 댓글/답글/반응 관련 REST API
     주요 기능: 댓글/답글 CRUD, 좋아요/싫어요 토글, 반응 상태 조회
     보안: 관리자도 모든 댓글/답글 삭제 가능
   
   - GuestbookController.java
     역할: 방명록 페이지 라우팅
   
   - HomeController.java
     역할: 메인 페이지 라우팅
   
   - SearchController.java
     역할: 검색 결과 페이지 라우팅
   
   - ThreadController.java
     역할: 스레드(비디오 토론) 페이지 라우팅

1.3 dao (데이터베이스 접근 계층)
   - ChannelDao.java
     역할: CHANNEL 테이블 CRUD 작업
     주요 기능: 채널 정보 저장, 조회
   
   - GuestbookDao.java
     역할: GUESTBOOK 테이블 CRUD 작업
     주요 기능: 방명록 생성, 조회
   
   - ReactionDao.java
     역할: REACTION 테이블 CRUD 작업
     주요 기능: 좋아요/싫어요 저장, 조회, 삭제, 카운트 조회
   
   - ReplyDao.java
     역할: REPLY 테이블 CRUD 작업
     주요 기능: 답글 저장, 조회, 수정, 삭제, 좋아요/싫어요 카운트 업데이트
   
   - ThreadDao.java
     역할: THREAD 테이블 CRUD 작업
     주요 기능: 스레드 저장, 조회, 인기 토론 조회
   
   - UserCommentDao.java
     역할: USER_COMMENT 테이블 CRUD 작업
     주요 기능: 댓글 저장, 조회, 수정, 삭제, 좋아요/싫어요 카운트 업데이트, 댓글+답글 통합 조회
   
   - UserDao.java
     역할: APP_USER 테이블 CRUD 작업
     주요 기능: 사용자 저장, 조회, 수정, 삭제, 중복 체크, 통계 조회
   
   - VideoDao.java
     역할: VIDEO 테이블 CRUD 작업
     주요 기능: 비디오 저장, 조회, 검색, 통계 조회

1.4 domain (엔티티 클래스)
   - Channel.java: 채널 정보 엔티티
   - Guestbook.java: 방명록 엔티티
   - Reaction.java: 좋아요/싫어요 반응 엔티티
   - Reply.java: 답글 엔티티
   - ThreadEntity.java: 스레드 엔티티
   - User.java: 사용자 엔티티
   - UserComment.java: 댓글 엔티티
   - Video.java: 비디오 엔티티

1.5 dto (데이터 전송 객체)
   - CommentView.java: 댓글/답글 통합 뷰 객체
   - JoinRequest.java: 회원가입 요청 DTO
   - LoginRequest.java: 로그인 요청 DTO (현재 미사용)
   - PageResponse.java: 페이지네이션 응답 객체
   - TrendingThread.java: 인기 토론 정보 DTO

1.6 exception (예외 처리)
   - GlobalExceptionHandler.java: 전역 예외 처리
   - YoutubeApiException.java: YouTube API 관련 예외

1.7 service (비즈니스 로직 계층)
   - AdminService.java
     역할: 관리자 기능 비즈니스 로직
     주요 기능: 통계 조회, 사용자/댓글 관리
   
   - CommentService.java
     역할: 댓글 관련 비즈니스 로직
     주요 기능: 댓글 조회, 추가, 수정, 삭제
     트랜잭션: @Transactional 적용 (추가, 수정, 삭제)
   
   - CustomUserDetailsService.java
     역할: Spring Security 사용자 인증 처리
     주요 기능: 로그인 ID로 사용자 정보 로드
   
   - ReactionService.java
     역할: 좋아요/싫어요 반응 비즈니스 로직
     주요 기능: 반응 토글, 카운트 업데이트
     트랜잭션: @Transactional 적용 (반응 토글 시 카운트 업데이트 포함)
   
   - ReplyService.java
     역할: 답글 관련 비즈니스 로직
     주요 기능: 답글 조회, 추가, 수정, 삭제
     트랜잭션: @Transactional 적용 (추가, 수정, 삭제)
   
   - SearchService.java
     역할: 검색 비즈니스 로직
     주요 기능: 제목으로 비디오 검색
   
   - ThreadService.java
     역할: 스레드 관련 비즈니스 로직
     주요 기능: 스레드 생성/조회, 인기 토론 조회
     트랜잭션: @Transactional 적용 (스레드 생성 시 Video, Channel, Thread 저장)
   
   - UserService.java
     역할: 사용자 관련 비즈니스 로직
     주요 기능: 회원가입, 회원정보 수정, 삭제, 검증
     트랜잭션: 회원가입 시 User와 Guestbook 동시 생성 (트랜잭션 필요)
   
   - YoutubeService.java
     역할: YouTube API 연동
     주요 기능: 비디오/채널 정보 조회

================================================================================
2. 주요 파일별 상세 역할

2.1 ReactionService.java
   - toggleReaction(): 좋아요/싫어요 토글
     REACTION 테이블에 반응 저장/삭제/수정
     USER_COMMENT 또는 REPLY 테이블의 카운트 업데이트
     @Transactional로 원자성 보장
   
   - updateCounts(): 카운트 업데이트
     댓글인지 답글인지 확인 후 해당 테이블의 카운트 업데이트
     GREATEST(0, ...) 함수로 음수 방지

2.2 CommentService.java
   - getCommentsWithReplies(): 댓글과 답글 통합 조회 (페이지네이션, 정렬 지원)
   - addThreadComment(): 스레드 댓글 추가 (@Transactional)
   - addGuestbookComment(): 방명록 댓글 추가 (@Transactional)
   - updateComment(): 댓글 수정 (@Transactional)
   - deleteComment(): 댓글 삭제 (@Transactional)

2.3 ReplyService.java
   - getRepliesByCommentId(): 답글 목록 조회
   - addReply(): 답글 추가 (@Transactional)
   - updateReply(): 답글 수정 (@Transactional)
   - deleteReply(): 답글 삭제 (@Transactional)

2.4 UserService.java
   - register(): 회원가입 (이메일/아이디 검증, 중복 체크, Guestbook 자동 생성)
   - updateUser(): 회원정보 수정 (이메일 검증, 중복 체크)
   - existsByLoginId(): 아이디 중복 확인
   - existsByEmail(): 이메일 중복 확인 (자기 자신 제외 옵션)

2.5 ThreadService.java
   - getOrCreateThreadData(): 스레드 생성/조회 (@Transactional)
     비디오가 없으면 YouTube API 호출 후 Video, Channel, Thread 생성
     여러 사용자가 동시에 접속해도 중복 생성 방지



