수정사항
ALTER TABLE APP_USER MODIFY (Password VARCHAR2(100)); -> 비밀번호를 해시값으로 저장하기 위해 100 자로 바꿈
Video 에 Title CLOB 이면 검색이 힘들어져서 VARCHAR2(4000) 으로 변경 -> 유튜브 제목 길이제한 100자라 ㄱㅊ
통일성 문제로 나머지 VARCHAR 전부 VARCHAR2 로 변경

1. src/main/resources/application.properties 를 본인 환경에 맞게 수정
2. maven 프로젝트라서 maven install 해줘야함

패키지별 역할
config -> 설정, Spring Security 가 사용중, API 엔드포인트 권한 설정 포함
controller -> HTTP 요청 처리 (요청 받고, service 호출, 응답 주기)
  - CommentRestController: 댓글/답글/반응 관련 REST API 처리
dao -> DB 와 통신 처리 == repository 랑 같은말
  - ReactionDao: REACTION 테이블 CRUD 작업
  - UserCommentDao: USER_COMMENT 테이블 작업 (좋아요/싫어요 카운트 업데이트 포함)
  - ReplyDao: REPLY 테이블 작업 (좋아요/싫어요 카운트 업데이트 포함)
domain -> DB 에서 가져온걸 객체로 바꾸기 위해 사용 (Entity 들이 모여있음)
  - Reaction: 사용자 반응 정보 (좋아요/싫어요)
dto -> JSON 통신 시 객체로 받기 위해 사용
  - TrendingThread: 인기 토론 정보 전달용
exception -> 사용자 정의 예외, 쓸일없음
service -> 메소드 모아놓은곳, 백엔드 연산 수행, Controller 에 의해 호출되는 복잡한 로직을 모아둠
  - ReactionService: 좋아요/싫어요 토글 로직, 카운트 업데이트 처리
  - ThreadService: 인기 토론 조회 기능 포함


@Autowired 쓰지마셈
Controller 에서 Service 를 써야할때 생성자로 주입해줘야함
다른거 어떻게 구현되어있는지 참고


내 방명록 확인
회원정보 수정


회원가입,탈퇴, 로그인, 로그아웃 구현, 동영상 ID 로 입장, 댓글 작성 구현, 동영상 검색
탈퇴시 redirect:/logout 안되는는 현상 존재 -> 나중에 고칠꺼임

주요 기능
- 좋아요/싫어요 기능: 댓글과 답글에 좋아요/싫어요 버튼 제공, 실시간 카운트 업데이트
- 인라인 편집: 댓글과 답글을 페이지에서 직접 수정 가능 (prompt 대신 textarea 사용)
- 답글 입력창: 유튜브 스타일의 확장 애니메이션, 답글달기 버튼 클릭 시 아래로 표시
- 인기 토론: 메인 페이지에 최근 24시간 내 댓글이 많은 상위 5개 토론 표시
- UI/UX 개선: 아바타 표시, 타임스탬프 위치 조정, 버튼 스타일 개선, 그라데이션 배경 적용

대략적인 구조
메인 페이지 -> 검색창과 인기 토론 목록 표시, TubeTalk 로고 클릭 시 메인으로 이동
검색 결과 페이지 -> 키워드로 검색 했을때, 검색 결과를 보여줄 페이지, Thread 로 이동 가능
Thread 페이지 -> 파라미터로 v=[VideoId] 받음, 없는 Id 라면 생성후 리턴, 영상 정보와 댓글 창 있음
  - 댓글/답글 작성, 수정, 삭제 기능 (로그인 후 가능)
  - 좋아요/싫어요 기능
  - 인라인 편집 기능
  - 답글 입력창 유튜브 스타일
로그인 페이지, 회원가입 페이지 -> 중앙 정렬 카드 스타일, 그라데이션 배경
방명록 페이지 -> 사용자별 방명록, 댓글/답글 기능, Thread 페이지와 동일한 UI/UX
마이페이지 -> 회원정보를 수정할수 있도록 할것, 로그인 성공시 우측위 로그인 버튼 대신 마이페이지 링크 버튼을 보여줌

API 엔드포인트
- POST /api/comments/{targetId}/reaction -> 좋아요/싫어요 토글 (targetId는 Comment_id 또는 Reply_id)
- GET /api/comments/{targetId}/reaction -> 현재 사용자의 반응 상태 조회
- POST /api/comments -> 댓글 작성
- PUT /api/comments/{commentId} -> 댓글 수정
- DELETE /api/comments/{commentId} -> 댓글 삭제
- POST /api/replies -> 답글 작성
- PUT /api/replies/{replyId} -> 답글 수정
- DELETE /api/replies/{replyId} -> 답글 삭제



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

참고사항
- REACTION 테이블: 사용자가 댓글 또는 답글에 남긴 좋아요/싫어요 정보 저장
- USER_COMMENT와 REPLY 테이블의 Like_count, Dislike_count는 GREATEST(0, ...) 함수로 음수 방지
- 한 사용자는 하나의 타겟(댓글/답글)에 대해 하나의 반응만 가능 (복합 기본키)
- 좋아요/싫어요 토글 시: 없음 -> 좋아요, 좋아요 -> 없음, 싫어요 -> 좋아요 (자동 변경)



