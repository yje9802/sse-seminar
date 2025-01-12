package com.example.demo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

  /**
   * HTTP 1.1, 2.0 버전에서는 브라우저당 여러 개의 SSE 커넥션을 유지할 수 있기 때문에 동일 사용자가 사용하는 서로 다른 Emitter들도 구분할 필요가 있다.
   * 이를 위해 ConcurrentHashMap으로 관리한다.
   */

  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
  private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

  @Override
  public SseEmitter save(String emitterId, SseEmitter sseEmitter) { // SseEmitter를 저장
    emitters.put(emitterId, sseEmitter);
    return sseEmitter;
  }

  @Override
  public void saveEventCache(String eventCacheId, Object event) { // 이벤트를 저장
    eventCache.put(eventCacheId, event);
  }

  @Override
  public Map<String, SseEmitter> findAllEmitterStartWithByMemberId(String memberId) { // 해당 회원과 관련된 모든 이벤트를 찾음
    return emitters.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(memberId))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public Map<String, Object> findAllEventCacheStartWithByMemberId(String memberId) {
    return eventCache.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(memberId))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public void deleteById(String id) { // emitter를 지움
    emitters.remove(id);
  }

  @Override
  public void deleteAllEmitterStartWithId(String memberId) { // 해당 회원과 관련된 모든 emitter를 지움
    emitters.forEach(
        (key, emitter) -> {
          if (key.startsWith(memberId)) {
            emitters.remove(key);
          }
        }
    );
  }

  @Override
  public void deleteAllEventCacheStartWithId(String memberId) { // 해당 회원과 관련된 모든 이벤트를 지움
    eventCache.forEach(
        (key, emitter) -> {
          if (key.startsWith(memberId)) {
            eventCache.remove(key);
          }
        }
    );
  }
}
