package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LogRepository logRepository;

    @Autowired
    MemberService memberService;

    @Test
    @DisplayName("정상 흐름")
    void outerTxOff_success() {
        //given
        String username = "outerTxOff_success";
        //when
        memberService.joinV1(username);
        //then: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    @Test
    @DisplayName("각각 별도의 트랜잭션 로그만 런타임 예외 터져서 롤백")
    void outerTxOff_fail() {

        String username = "로그예외_outerTxOff_fail";

        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    @Test
    @DisplayName("트랜잭션 전파 예제 모두 같은 커넥션 사용")
    void outerTxOn_success() {
        //given
        String username = "outerTxOn_success";
        //when
        memberService.joinV1(username);
        //then: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    @Test
    @DisplayName("런타임 예외가 서비스단 까지 올라왔는데, 예외 처리 안하는 경우. 모두 롤백됨")
    void outerTxOn_fail() {

        String username = "로그예외_outerTxOn_fail";

        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);


        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    @Test
    @DisplayName("런타임 예외를 서비스 에서 처리. 이 경우도 모두 롤백, 서비스단은 UnexpectedRollbackException던짐")
    void recoverException_fail() {
        //given
        String username = "로그예외_recoverException_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        //then
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    @Test
    void recoverException_success() {
        //given
        String username = "로그예외_recoverException_success";
        //when
        memberService.joinV2(username);
        //then: member 저장, log 롤백
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

}