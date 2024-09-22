package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    @Transactional
    public void joinV1(String username) {

        Member member = new Member(username);
        Log log = new Log(username);

        memberRepository.save(member);
        logRepository.save(log);

    }

    @Transactional
    public void joinV2(String username) {

        Member member = new Member(username);
        Log logMessage = new Log(username);

        memberRepository.save(member);
        try {
            logRepository.save(logMessage);
        } catch (IllegalStateException e) {
            log.info("log 저장에 실패했습니다. logMessage={}", logMessage.getMessage());
            log.info("정상 흐름으로 전환 예시");
        }
    }

}
