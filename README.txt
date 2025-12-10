================================
Phase3 대비 테이블 변경 사항
================================

APP_USER.Password 컬럼 길이 → 100으로 확장
 - 비밀번호 해시값을 저장하기 위함

VIDEO.Title → VARCHAR2(4000)로 변경
 - CLOB 사용 시 검색 기능에 제약이 있어 VARCHAR2 로 변경

전체 테이블의 VARCHAR → VARCHAR2 로 통일
 - 일관성 목적

================================
프로젝트 개발환경
================================

운영체제: Windows
Backend: Spring Framework
Frontend: Thymeleaf, HTML, CSS, JavaScript
DB: Oracle
SQL 툴: SQLDeveloper
프로젝트 빌드: Maven (pom.xml 기반 라이브러리 관리)

================================
프로젝트 실행 방법
================================

1. DB 준비
 - createTables.sql, inserts.sql 을 SQLDeveloper에서 실행
 - 테이블 생성 및 테스트 데이터 삽입

2. 환경 설정 수정
 - src/main/resources/application.properties 파일 수정 (api 키가 포함되어있기에 github 에는 올라가있지 않음)
 - spring.datasource.url, username, password → 본인 환경에 맞게 설정 (예시 : jdbc:oracle:thin:@localhost:1521:orcl)
 - youtube.api.key: 제공된 값 사용 가능 (일일 10,000회 제한)

3. 서버 실행
 - Team6-Phase4.zip 압축을 해제한뒤 talkbox 디렉토리를 Existing Maven Projects 로 Import (이클립스 기준)
 - TubetalkApplication.java 실행
 - 기본 포트: http://localhost:8080

4. maven 오류 발생 시
 - mvn install 실행
 - pom.xml 내 모든 dependency 자동 설치

================================
주요 제공 기능 설명
================================

1. 인기 영상 노출
 - 최근 24시간 동안 댓글이 가장 많이 달린 스레드를 메인화면에 표시

2. 검색 기능
 - 키워드 기반 스레드 검색
 - 유튜브 주소 입력 시 존재하는 스레드 이동 또는 신규 스레드 생성

3. 스레드 및 방명록 기능
 - 각 유튜브 영상에 대응하는 댓글 스레드 제공
 - 댓글이 막힌 영상도 스레드 생성 가능
 - 영상 embed는 정책에 따라 표시되지 않을 수 있으나 댓글 작성 가능
 - 사용자별 댓글 공간인 방명록 기능 제공

4. 회원 기능
 - 로그인/로그아웃/회원가입/정보 수정/탈퇴 지원
 - 내 방명록 바로가기 기능 제공

5. 댓글 및 답글 기능
 - 로그인 시 댓글/답글 작성 가능
 - 좋아요/싫어요 반응 기능
 - 우선순위에 따른 정렬 기능
 - 사용자 ID 클릭 → 해당 유저 방명록 이동

6. 관리자(Admin) 페이지
 - ID를 admin 으로 가입 시 자동으로 관리자 권한 부여
 - /admin 페이지에서 통계 및 관리 기능 제공

================================
유튜브 데모 영상 링크
================================

https://www.youtube.com/watch?v=APHYcJZry5U

================================
프로젝트 구현 설명
================================
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
