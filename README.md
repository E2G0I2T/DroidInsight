DroidInsight: 안드로이드 시스템 모니터링 앱
DroidInsight는 안드로이드 기기의 핵심 상태(배터리, 네트워크, 메모리, 앱 사용량)를 실시간으로 모니터링하고 시각화하는 대시보드 애플리케이션입니다.
Jetpack Compose, Hilt, Coroutines Flow, Room, WorkManager, Glance Widget을 활용하여 개발되었습니다.

주요 기능
1. 실시간 대시보드
배터리: 충전 상태, 전압, 온도를 실시간 Flow로 구독하여 표시.
시스템 리소스: RAM 및 내부 저장소 사용량을 직관적인 게이지 바로 시각화.
기술 포인트: BroadcastReceiver를 callbackFlow로 변환하여 Reactive하게 데이터를 수집.

2. 네트워크 트래픽 모니터링 & 벤치마크
실시간 차트: TrafficStats API를 활용하여 업로드/다운로드 속도를 Canvas로 직접 그린 라인 차트로 표현.
속도 측정: 실제 더미 파일을 다운로드하여 최대/평균 속도를 측정하는 벤치마크 기능.
기술 포인트: Canvas API를 활용한 커스텀 뷰 구현, OkHttp 스트리밍 처리.

3. 앱 사용 통계
권한 관리: PACKAGE_USAGE_STATS 권한 흐름(Flow)을 매끄럽게 처리.
통계 시각화: 오늘 가장 많이 사용한 앱 Top 5를 막대 그래프로 시각화.
기술 포인트: 복잡한 시스템 데이터를 Domain Model로 매핑하여 UI에 전달.

4. 홈 화면 위젯
Jetpack Glance: XML 없이 코틀린 코드로 위젯 UI 구성.
백그라운드 갱신: WorkManager를 사용하여 15분마다 데이터 갱신 보장.
인터랙션: 위젯 클릭 시 모드 변경(배터리 ↔ RAM) 및 즉시 새로고침 기능.

Troubleshooting & Challenges
프로젝트 진행 중 마주친 기술적 문제와 해결 과정입니다.

1. BroadcastReceiver의 Flow 변환 (Callback Hell 탈출)
문제: 배터리 상태 변경을 감지하기 위해 BroadcastReceiver를 사용해야 했으나, 이를 ViewModel에서 직접 관리하니 콜백 지옥과 메모리 누수 위험이 있었습니다.
해결: callbackFlow를 사용하여 콜백 기반의 API를 Coroutine Flow 스트림으로 변환했습니다. awaitClose에서 리시버를 해제하여 메모리 누수를 원천 차단했습니다.

2. 백그라운드 위젯 갱신 문제
문제: 앱이 종료되면 위젯의 배터리 정보가 갱신되지 않는 현상이 발생했습니다.
해결: WorkManager의 PeriodicWorkRequest를 도입하여 시스템이 허용하는 최소 주기(15분)마다 위젯을 강제 업데이트하도록 구현했습니다. 또한, 앱 실행 시 lifecycleScope에서 즉시 갱신을 요청하여 UX를 개선했습니다.

3. 커스텀 차트 성능 최적화
문제: 네트워크 차트를 그릴 때마다 수많은 객체가 생성되어 GC(Garbage Collection) 오버헤드가 우려되었습니다.
해결: Canvas 내부에서 객체 할당을 최소화하고, remember를 사용하여 데이터가 변하지 않았을 때의 불필요한 재계산을 방지했습니다. 또한 Path 객체를 재사용하는 구조로 리팩토링했습니다.
