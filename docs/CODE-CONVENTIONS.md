# 코딩 컨벤션

## 1. 목적
이 문서는 Guardrail 프로젝트의 Java/Spring 코드 작성 기준을 정의한다.

코드는 `PACKAGE-STRUCTURE.md`, `DOMAIN-MODEL.md`, `API-SPEC.md`의 기준을 따른다.

## 2. 기본 원칙
- 클래스와 패키지는 역할이 드러나도록 작성한다.
- Entity를 controller 응답으로 직접 반환하지 않는다.
- DTO는 외부 입출력 또는 계층 간 전달 목적으로만 사용한다.
- Repository는 데이터 저장 및 조회 책임만 가진다.
- Service는 유스케이스 흐름 조합과 비즈니스 처리를 담당한다.
- Entity는 자기 상태 변경에 필요한 최소 규칙만 가질 수 있다.
- 모든 엔티티의 PK는 UUID를 사용한다.
- 테이블 간 객체 연관관계 매핑은 사용하지 않는다.
- 컬렉션은 null 대신 빈 컬렉션을 사용한다.
- 민감 정보는 로그에 출력하지 않는다.

## 3. Java 네이밍 규칙
| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `UserService` |
| 메서드 | camelCase | `createUser` |
| 변수 | camelCase | `userId` |
| 상수 | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| enum 값 | UPPER_SNAKE_CASE | `ACTIVE` |
| 패키지 | 소문자 | `com.hyeon.guardrail.user` |

## 4. Entity 작성 규칙
- Entity는 `{domain}.domain` 패키지에 둔다.
- Entity 클래스명은 단수 명사를 사용한다.
- 모든 Entity의 PK 필드는 `id`로 작성한다.
- PK 타입은 UUID를 사용한다.
- soft delete가 필요한 Entity는 `SoftDeleteEntity`를 상속한다.
- soft delete가 필요 없는 Entity는 `BaseEntity`를 상속한다.
- Entity 선언 필드는 `@Column(name = "...")`을 명시한다.
- 다른 Entity를 객체 연관관계로 참조하지 않는다.
- 다른 Entity 참조가 필요한 경우 `{domain}Id` 형태의 UUID 필드로 관리한다.
- Entity를 API 응답으로 직접 반환하지 않는다.
- Entity의 equals/hashCode는 기본적으로 직접 구현하지 않는다.
- Entity 생성은 `@AllArgsConstructor`를 사용한다.
- Entity 생성 흐름과 유스케이스 판단은 Service가 담당한다.
- Entity는 상태 전이와 soft delete 같은 자기 상태 변경 메서드만 둘 수 있다.
- Entity에 범용적인 전체 수정 메서드를 두지 않는다.
- Entity에 `email`, `name`, `role` 같은 기본 정보 변경 메서드를 두지 않는다.

예:
- `User`
- `Product`
- `Category`
- `FileAttachment`

## 5. Enum 작성 규칙
- enum은 `{domain}.domain` 패키지에 둔다.
- enum 클래스명은 의미가 드러나도록 작성한다.
- enum 값은 대문자 스네이크 케이스를 사용한다.

예:
- `UserRole`
- `UserStatus`
- `ProductStatus`
- `CategoryStatus`

## 6. Repository 작성 규칙
- Repository는 `{domain}.repository` 패키지에 둔다.
- Spring Data JPA Repository는 `~Repository`로 끝낸다.
- Querydsl 조회 클래스는 `~RepositoryQuery`로 끝낸다.
- `~RepositoryQuery`는 단일 클래스로 작성한다.
- Repository에는 비즈니스 로직을 작성하지 않는다.
- Service는 필요에 따라 `~Repository`와 `~RepositoryQuery`를 함께 호출할 수 있다.

예:
- `UserRepository`
- `UserRepositoryQuery`
- `ProductRepository`
- `ProductRepositoryQuery`

## 7. Service 작성 규칙
- Service는 `{domain}.service` 패키지에 둔다.
- Service 클래스는 `~Service`로 끝낸다.
- Service는 유스케이스 흐름 조합과 비즈니스 처리를 담당한다.
- Controller 또는 Repository에 비즈니스 로직을 두지 않는다.
- Service 메서드는 기본적으로 `@Transactional`을 사용한다.
- 트랜잭션 경계는 Service에서 관리한다.
- 조회 전용 메서드는 가능한 읽기 전용 트랜잭션을 사용한다.
- 사용자 생성처럼 다른 도메인을 함께 조합하는 흐름은 Service에서 명시적으로 처리한다.
- 작업 범위에 없는 다른 도메인 클래스는 임의 생성하지 않는다.

예:
- `UserService`
- `AuthService`
- `ProductService`

## 8. Controller 작성 규칙
- Controller는 `{domain}.controller` 패키지에 둔다.
- Controller 클래스는 `~Controller`로 끝낸다.
- Controller는 request/response 처리와 Service 호출만 담당한다.
- Controller에는 비즈니스 로직을 작성하지 않는다.
- Entity를 응답으로 직접 반환하지 않는다.
- API 경로는 `/api/v1` prefix를 사용한다.
- 성공 응답은 기본적으로 `BaseResponseEntity<T>`를 사용한다.
- Controller는 `ResponseEntity`를 직접 반환하지 않는다.
- 성공 상태, 메시지, 코드는 `BaseResponseStatus`를 통해 관리한다.

예:
- `UserController`
- `AuthController`
- `ProductController`

## 9. DTO 작성 규칙
- DTO는 `{domain}.dto` 패키지에 둔다.
- 요청 DTO는 `Request`로 끝낸다.
- 응답 DTO는 `Response`로 끝낸다.
- DTO는 Entity를 직접 노출하지 않는다.
- DTO는 API 목적이 드러나도록 이름을 작성한다.
- request DTO에는 필요한 validation annotation을 작성한다.
- request DTO는 controller 진입 시점의 기본 입력 검증을 담당한다.
- service 계층은 도메인 상태, 중복, 권한 등 비즈니스 검증을 담당한다.
- 자동 생성 값은 request DTO로 직접 받지 않는다.
- 초기 비밀번호 같은 1회 자동 생성 값은 request DTO에 포함하지 않는다.
- 자동 생성 값이 필요한 유스케이스는 service에서 생성하고 응답 또는 후속 전달 흐름으로만 노출한다.

예:
- `UserCreateRequest`
- `UserUpdateRequest`
- `UserResponse`
- `ProductStatusUpdateRequest`
- `ProductContentApproveRequest`
- `ProductResponse`

## 10. 예외 처리 규칙
- 예외는 공통 예외 구조를 사용한다.
- 비즈니스 예외는 `BaseException`을 사용한다.
- 예외 상태, 메시지, 코드는 `BaseResponseStatus`로 관리한다.
- Controller에서 예외 응답을 직접 만들지 않는다.
- 공통 예외 핸들러와 필터에서 에러 응답을 생성한다.

예:
- `BaseException`
- `BaseExceptionHandler`
- `BaseExceptionHandlerFilter`

## 11. Lombok 사용 기준
- Lombok은 필요한 범위에서만 사용한다.
- Entity에는 무분별한 setter를 만들지 않는다.
- Entity 생성은 정적 팩터리 메서드, 생성자, builder 중 도메인에 적합한 방식을 선택한다.
- builder 사용은 필수가 아니며, 필드가 많거나 생성 의도를 명확히 드러내야 할 때 사용한다.
- `@Data`는 사용하지 않는다.
- DTO와 공통 응답 구조에는 `record`를 사용하지 않는다.

## 12. 주석 작성 기준
- 모든 클래스에는 한 줄 Javadoc을 작성한다.
- public 메서드 중 외부 계약, 설정 진입점, 공통 응답/예외 유틸에는 한 줄 Javadoc을 작성한다.
- 단순 getter, 생성자에는 Javadoc을 작성하지 않는다.
- 메서드 Javadoc은 복잡한 정책, 상태 전이, 보안 판단, 외부 공개 API에만 작성한다.
- Javadoc은 역할과 의도를 간단한 명사형으로 작성한다.
- 복잡한 정책, 상태 전이, 보안 판단에는 필요한 경우 추가 설명을 작성한다.

## 13. Optional 및 컬렉션 사용 기준
- Optional은 반환 타입으로만 사용한다.
- Optional을 필드나 파라미터로 사용하지 않는다.
- 컬렉션 반환 시 null 대신 빈 컬렉션을 반환한다.
- 컬렉션 필드는 null 대신 빈 컬렉션으로 초기화한다.

## 14. 로그 작성 기준
- 민감 정보는 로그에 출력하지 않는다.
- 비밀번호, 토큰, 인증 헤더, 개인정보는 로그 대상에서 제외한다.
- 예외 로그는 원인 파악에 필요한 수준으로 작성한다.

## 15. 테스트 네이밍 기준
- 테스트 클래스는 `~Test`로 끝낸다.
- 테스트 메서드는 의도가 드러나도록 작성한다.
- 성공 케이스와 실패 케이스를 분리한다.

예:
- `UserServiceTest`
- `ProductServiceTest`
