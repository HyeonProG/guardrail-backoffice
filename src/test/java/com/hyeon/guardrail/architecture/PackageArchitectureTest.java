package com.hyeon.guardrail.architecture;

import com.hyeon.guardrail.common.domain.BaseEntity;
import com.hyeon.guardrail.common.domain.SoftDeleteEntity;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.lang.annotation.Annotation;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

@AnalyzeClasses(
    packages = "com.hyeon.guardrail",
    importOptions = ImportOption.DoNotIncludeTests.class)
class PackageArchitectureTest {

  // controller는 요청/응답 처리만 담당하고 repository를 직접 호출하지 않는다.
  @ArchTest
  static final ArchRule CONTROLLER_SHOULD_NOT_ACCESS_REPOSITORY =
      ArchRuleDefinition.noClasses()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..repository..")
          .as("controller 계층은 repository 계층을 직접 접근할 수 없다")
          .allowEmptyShould(true);

  // controller는 entity 등 domain 내부 구현을 응답으로 직접 노출하지 않는다.
  @ArchTest
  static final ArchRule CONTROLLER_SHOULD_NOT_ACCESS_DOMAIN =
      ArchRuleDefinition.noClasses()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..domain..")
          .as("controller 계층은 domain 계층을 직접 접근할 수 없다")
          .allowEmptyShould(true);

  // repository는 데이터 저장/조회 책임만 가지며 상위 계층을 참조하지 않는다.
  @ArchTest
  static final ArchRule REPOSITORY_SHOULD_NOT_ACCESS_CONTROLLER_OR_SERVICE =
      ArchRuleDefinition.noClasses()
          .that()
          .resideInAPackage("..repository..")
          .should()
          .accessClassesThat()
          .resideInAnyPackage("..controller..", "..service..")
          .as("repository 계층은 controller 또는 service 계층을 접근할 수 없다")
          .allowEmptyShould(true);

  // controller 패키지의 클래스는 역할이 드러나도록 Controller suffix를 사용한다.
  @ArchTest
  static final ArchRule CONTROLLER_NAME_SHOULD_END_WITH_CONTROLLER =
      ArchRuleDefinition.classes()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .haveSimpleNameEndingWith("Controller")
          .as("controller 패키지 클래스명은 Controller로 끝나야 한다")
          .allowEmptyShould(true);

  // service 패키지의 클래스는 역할이 드러나도록 Service suffix를 사용한다.
  @ArchTest
  static final ArchRule SERVICE_NAME_SHOULD_END_WITH_SERVICE =
      ArchRuleDefinition.classes()
          .that()
          .resideInAPackage("..service..")
          .should()
          .haveSimpleNameEndingWith("Service")
          .as("service 패키지 클래스명은 Service로 끝나야 한다")
          .allowEmptyShould(true);

  // Spring Data JPA repository는 Repository suffix를 사용한다.
  @ArchTest
  static final ArchRule REPOSITORY_NAME_SHOULD_END_WITH_REPOSITORY =
      ArchRuleDefinition.classes()
          .that()
          .resideInAPackage("..repository..")
          .and()
          .haveSimpleNameNotEndingWith("RepositoryQuery")
          .should()
          .haveSimpleNameEndingWith("Repository")
          .as("repository 패키지 클래스명은 Repository로 끝나야 한다")
          .allowEmptyShould(true);

  // Querydsl 전용 조회 클래스는 RepositoryQuery suffix를 사용한다.
  @ArchTest
  static final ArchRule QUERYDSL_REPOSITORY_NAME_SHOULD_END_WITH_REPOSITORY_QUERY =
      ArchRuleDefinition.classes()
          .that()
          .resideInAPackage("..repository..")
          .and()
          .haveSimpleNameContaining("Query")
          .should()
          .haveSimpleNameEndingWith("RepositoryQuery")
          .as("Querydsl 조회 클래스명은 RepositoryQuery로 끝나야 한다")
          .allowEmptyShould(true);

  // dto 패키지는 외부 입출력 모델만 두며 Request 또는 Response suffix를 사용한다.
  @ArchTest
  static final ArchRule DTO_NAME_SHOULD_END_WITH_REQUEST_OR_RESPONSE =
      ArchRuleDefinition.classes()
          .that()
          .resideInAPackage("..dto..")
          .and()
          .resideOutsideOfPackage("..common.ai.dto..")
          .should(simpleNameEndingWithRequestOrResponse())
          .as("dto 패키지 클래스명은 Request 또는 Response로 끝나야 한다")
          .allowEmptyShould(true);

  // dto는 record를 사용하지 않고 일반 클래스로 작성한다.
  @ArchTest
  static final ArchRule DTO_SHOULD_NOT_BE_RECORD =
      ArchRuleDefinition.classes()
          .that()
          .resideInAPackage("..dto..")
          .should(notBeRecordType())
          .as("dto 패키지 클래스는 record를 사용할 수 없다")
          .allowEmptyShould(true);

  // JPA entity는 공통 식별자와 감사 필드를 사용하기 위해 BaseEntity를 상속한다.
  @ArchTest
  static final ArchRule ENTITY_SHOULD_EXTEND_BASE_ENTITY =
      ArchRuleDefinition.classes()
          .that()
          .areAnnotatedWith(Entity.class)
          .should()
          .beAssignableTo(BaseEntity.class)
          .as("JPA entity는 BaseEntity 또는 SoftDeleteEntity를 상속해야 한다")
          .allowEmptyShould(true);

  // auth 도메인 entity는 soft delete가 아니라 보존/상태 정책을 사용한다.
  @ArchTest
  static final ArchRule AUTH_ENTITY_SHOULD_NOT_EXTEND_SOFT_DELETE_ENTITY =
      ArchRuleDefinition.classes()
          .that()
          .areAnnotatedWith(Entity.class)
          .and()
          .resideInAPackage("..auth.domain..")
          .should()
          .notBeAssignableTo(SoftDeleteEntity.class)
          .as("auth 도메인 entity는 SoftDeleteEntity를 상속할 수 없다")
          .allowEmptyShould(true);

  // controller public 메서드는 BaseResponseEntity만 반환하고 ResponseEntity를 직접 사용하지 않는다.
  @ArchTest
  static final ArchRule CONTROLLER_SHOULD_NOT_RETURN_RESPONSE_ENTITY =
      ArchRuleDefinition.classes()
          .that()
          .resideInAPackage("..controller..")
          .should(notDeclareResponseEntityReturnType())
          .as("controller public 메서드는 ResponseEntity를 직접 반환할 수 없다")
          .allowEmptyShould(true);

  // controller의 기본 경로는 /api/v1 prefix를 사용한다.
  @ArchTest
  static final ArchRule CONTROLLER_SHOULD_USE_API_V1_PREFIX =
      ArchRuleDefinition.classes()
          .that()
          .resideInAPackage("..controller..")
          .should(haveApiV1RequestMapping())
          .as("controller 기본 경로는 /api/v1 prefix를 사용해야 한다")
          .allowEmptyShould(true);

  // entity 필드는 명시적인 컬럼명을 가진 @Column을 사용한다.
  @ArchTest
  static final ArchRule ENTITY_FIELDS_SHOULD_DECLARE_COLUMN_NAME =
      ArchRuleDefinition.classes()
          .that()
          .areAnnotatedWith(Entity.class)
          .should(haveExplicitColumnName())
          .as("entity 선언 필드는 @Column(name = ...)을 명시해야 한다")
          .allowEmptyShould(true);

  // entity는 선언 필드 기준 all-args 생성자를 제공한다.
  @ArchTest
  static final ArchRule ENTITY_SHOULD_HAVE_ALL_ARGS_CONSTRUCTOR =
      ArchRuleDefinition.classes()
          .that()
          .areAnnotatedWith(Entity.class)
          .should(haveAllArgsConstructor())
          .as("entity는 선언 필드 기준 all-args 생성자를 가져야 한다")
          .allowEmptyShould(true);

  // entity는 테이블 간 객체 연관관계 매핑을 사용하지 않는다.
  @ArchTest
  static final ArchRule ENTITY_SHOULD_NOT_USE_JPA_RELATIONSHIP_MAPPING =
      ArchRuleDefinition.classes()
          .that()
          .areAnnotatedWith(Entity.class)
          .should(notDeclareJpaRelationshipField())
          .as("entity는 JPA 연관관계 매핑을 사용할 수 없다")
          .allowEmptyShould(true);

  // entity 생성 흐름은 service에서 담당하므로 정적 create 메서드를 두지 않는다.
  @ArchTest
  static final ArchRule ENTITY_SHOULD_NOT_DECLARE_STATIC_CREATE_METHOD =
      ArchRuleDefinition.classes()
          .that()
          .areAnnotatedWith(Entity.class)
          .should(notDeclareStaticCreateMethod())
          .as("entity는 정적 create 메서드를 가질 수 없다")
          .allowEmptyShould(true);

  private static ArchCondition<JavaClass> simpleNameEndingWithRequestOrResponse() {
    return new ArchCondition<>("클래스명이 Request 또는 Response로 끝나야 한다") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        boolean matches =
            item.getSimpleName().endsWith("Request") || item.getSimpleName().endsWith("Response");

        events.add(
            new SimpleConditionEvent(
                item, matches, item.getName() + " 클래스명은 Request 또는 Response로 끝나야 한다"));
      }
    };
  }

  private static ArchCondition<JavaClass> notBeRecordType() {
    return new ArchCondition<>("record 타입이면 안 된다") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        boolean isRecord = item.reflect().isRecord();
        events.add(
            new SimpleConditionEvent(item, !isRecord, item.getName() + " 클래스는 record를 사용할 수 없다"));
      }
    };
  }

  private static ArchCondition<JavaClass> notDeclareResponseEntityReturnType() {
    return new ArchCondition<>("ResponseEntity 반환 메서드를 가지면 안 된다") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        List<JavaMethod> methods =
            item.getMethods().stream()
                .filter(method -> method.getOwner().equals(item))
                .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                .toList();

        boolean valid =
            methods.stream()
                .noneMatch(
                    method -> method.getRawReturnType().isEquivalentTo(ResponseEntity.class));

        events.add(
            new SimpleConditionEvent(
                item, valid, item.getName() + " controller는 ResponseEntity를 직접 반환할 수 없다"));
      }
    };
  }

  private static ArchCondition<JavaClass> haveExplicitColumnName() {
    return new ArchCondition<>("선언 필드에 @Column(name = ...)이 있어야 한다") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        List<JavaField> fields =
            item.getFields().stream()
                .filter(field -> field.getOwner().equals(item))
                .filter(field -> !field.getModifiers().contains(JavaModifier.STATIC))
                .toList();

        boolean valid =
            fields.stream()
                .allMatch(
                    field -> {
                      Column column = field.reflect().getAnnotation(Column.class);
                      return column != null && !column.name().isBlank();
                    });

        events.add(
            new SimpleConditionEvent(
                item, valid, item.getName() + " entity 필드는 @Column(name = ...)을 명시해야 한다"));
      }
    };
  }

  private static ArchCondition<JavaClass> haveAllArgsConstructor() {
    return new ArchCondition<>("선언 필드 수와 일치하는 생성자가 있어야 한다") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        long fieldCount =
            item.getFields().stream()
                .filter(field -> field.getOwner().equals(item))
                .filter(field -> !field.getModifiers().contains(JavaModifier.STATIC))
                .count();

        boolean hasAllArgsConstructor =
            item.getConstructors().stream()
                .filter(constructor -> constructor.getOwner().equals(item))
                .anyMatch(constructor -> hasFieldSizedParameters(constructor, fieldCount));

        events.add(
            new SimpleConditionEvent(
                item, hasAllArgsConstructor, item.getName() + " entity는 all-args 생성자를 가져야 한다"));
      }
    };
  }

  private static boolean hasFieldSizedParameters(JavaConstructor constructor, long fieldCount) {
    return constructor.getRawParameterTypes().size() == fieldCount;
  }

  private static ArchCondition<JavaClass> notDeclareJpaRelationshipField() {
    return new ArchCondition<>("JPA 연관관계 필드를 선언하면 안 된다") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        List<JavaField> fields =
            item.getFields().stream()
                .filter(field -> field.getOwner().equals(item))
                .filter(field -> !field.getModifiers().contains(JavaModifier.STATIC))
                .toList();

        boolean valid =
            fields.stream()
                .noneMatch(
                    field ->
                        field.isAnnotatedWith(ManyToOne.class)
                            || field.isAnnotatedWith(OneToOne.class)
                            || field.isAnnotatedWith(OneToMany.class)
                            || field.isAnnotatedWith(ManyToMany.class));

        events.add(
            new SimpleConditionEvent(item, valid, item.getName() + " entity는 JPA 연관관계를 사용할 수 없다"));
      }
    };
  }

  private static ArchCondition<JavaClass> notDeclareStaticCreateMethod() {
    return new ArchCondition<>("정적 create 메서드를 선언하면 안 된다") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        boolean valid =
            item.getMethods().stream()
                .filter(method -> method.getOwner().equals(item))
                .noneMatch(
                    method ->
                        method.getName().equals("create")
                            && method.getModifiers().contains(JavaModifier.STATIC));

        events.add(
            new SimpleConditionEvent(
                item, valid, item.getName() + " entity는 정적 create 메서드를 가질 수 없다"));
      }
    };
  }

  private static ArchCondition<JavaClass> haveApiV1RequestMapping() {
    return new ArchCondition<>("기본 RequestMapping 경로가 /api/v1로 시작해야 한다") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        RequestMapping requestMapping = getAnnotation(item, RequestMapping.class);
        boolean valid =
            requestMapping != null
                && requestMapping.value().length > 0
                && requestMapping.value()[0].startsWith("/api/v1");

        events.add(
            new SimpleConditionEvent(
                item, valid, item.getName() + " controller 기본 경로는 /api/v1로 시작해야 한다"));
      }
    };
  }

  private static <T extends Annotation> T getAnnotation(JavaClass item, Class<T> annotationType) {
    return item.reflect().getAnnotation(annotationType);
  }
}
