수정사항
ALTER TABLE APP_USER MODIFY (Password VARCHAR2(100)); -> 비밀번호를 해시값으로 저장하기 위해 100 자로 바꿈
Video 에 Title CLOB 이면 검색이 힘들어져서 VARCHAR2(4000) 으로 변경 -> 유튜브 제목 길이제한 100자라 ㄱㅊ
통일성 문제로 나머지 VARCHAR 전부 VARCHAR2 로 변경

1. src/main/resources/application.properties 를 본인 환경에 맞게 수정
2. maven 프로젝트라서 maven install 해줘야함

패키지별 역할
config -> 설정, Spring Security 가 사용중, 우린 쓸일 없음
controller -> HTTP 요청 처리 (요청 받고, service 호출, 응답 주기)
dao -> DB 와 통신 처리 == repository 랑 같은말
domain -> DB 에서 가져온걸 객체로 바꾸기 위해 사용 (Entity 들이 모여있음)
dto -> JSON 통신 시 객체로 받기 위해 사용
exception -> 사용자 정의 예외, 쓸일없음
service -> 메소드 모아놓은곳, 백엔드 연산 수행, Controller 에 의해 호출되는 복잡한 로직을 모아둠


@Autowired 쓰지마셈
Controller 에서 Service 를 써야할때 생성자로 주입해줘야함
다른거 어떻게 구현되어있는지 참고


내 방명록 확인
회원정보 수정


회원가입,탈퇴, 로그인, 로그아웃 구현, 동영상 ID 로 입장, 댓글 작성 구현, 동영상 검색
탈퇴시 redirect:/logout 안되는는 현상 존재 -> 나중에 고칠꺼임

대략적인 구조
메인 페이지 -> Google 처럼 단순한 UI, 한개의 검색창 (키워드 또는 VideoID 로 Thread 검색, 입장 가능), 우측 위 로그인 버튼
검색 결과 페이지 -> 키워드로 검색 했을때, 검색 결과를 보여줄 페이지, Thread 로 이동 가능
Thread 페이지 -> 파라미터로 v=[VideoId] 받음, 없는 Id 라면 생성후 리턴, 영상 정보와 댓글 창 있음, 댓글작성은 로그인 후 가능
로그인 페이지, 회원가입 페이지, 회원탈퇴 페이지 -> 기존과 같음
마이페이지 -> 회원정보를 수정할수 있도록 할것, 로그인 성공시 우측위 로그인 버튼 대신 마이페이지 링크 버튼을 보여줌



현재 테이블 구조
DROP TABLE REPLY CASCADE CONSTRAINTS;
DROP TABLE USER_COMMENT CASCADE CONSTRAINTS;
DROP TABLE GUESTBOOK CASCADE CONSTRAINTS;
DROP TABLE THREAD CASCADE CONSTRAINTS;
DROP TABLE VIDEO CASCADE CONSTRAINTS;
DROP TABLE CHANNEL CASCADE CONSTRAINTS;
DROP TABLE APP_USER CASCADE CONSTRAINTS;

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


