package com.hyeon.guardrail.architecture;

import com.hyeon.guardrail.common.domain.BaseEntity;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import jakarta.persistence.Entity;

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
          .should(simpleNameEndingWithRequestOrResponse())
          .as("dto 패키지 클래스명은 Request 또는 Response로 끝나야 한다")
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
}
