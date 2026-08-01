package com.example.springreact;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@DisplayName("Architecture Tests")
class ArchitectureTests {

  private static JavaClasses importedClasses;

  @BeforeAll
  static void setup() {
    // Solo importamos clases del código de producción, excluyendo tests
    importedClasses =
        new ClassFileImporter()
            .withImportOption(location -> !location.contains("Test.class"))
            .importPackages("com.example.springreact");
  }

  @Test
  @DisplayName("JwtService debe estar en paquete service, no en security")
  void testJwtServiceLocation() {
    ArchRule rule =
        classes().that().haveSimpleName("JwtService").should().resideInAPackage("..service..");

    rule.check(importedClasses);
  }

  @Test
  @DisplayName("Debe respetar arquitectura de capas: Controller -> Service -> Repository -> Model")
  void testLayeredArchitecture() {
    // Nota: ArchUnit detectaría violaciones transitivasde tipos genéricos que son técnicamente
    // necesarios
    // (ej: Repository<User> contiene referencia a User). Este test se enfoca en
    // importaciones directas e inyecciones.
    ArchRule controllerToService =
        noClasses()
            .that()
            .resideInAPackage("..controller..")
            .and()
            // RoleController es una excepción: endpoint simple sin lógica compleja
            .doNotHaveSimpleName("RoleController")
            .and()
            .doNotHaveSimpleName("InfoController")
            .and()
            .doNotHaveSimpleName("SpaController")
            .should()
            .accessClassesThat()
            .resideInAPackage("..repository..");

    controllerToService.check(importedClasses);
  }

  @Test
  @DisplayName("Clases con @Service deben estar en paquete .service")
  void testServiceAnnotationNamingConvention() {
    ArchRule rule =
        classes().that().areAnnotatedWith(Service.class).should().resideInAPackage("..service..");

    rule.check(importedClasses);
  }

  @Test
  @DisplayName("Clases con @Repository deben estar en paquete .repository")
  void testRepositoryAnnotationNamingConvention() {
    ArchRule rule =
        classes()
            .that()
            .areAnnotatedWith(Repository.class)
            .should()
            .resideInAPackage("..repository..");

    rule.check(importedClasses);
  }

  @Test
  @DisplayName("Clases con @RestController o @Controller deben estar en paquete .controller")
  void testControllerAnnotationNamingConvention() {
    ArchRule rule =
        classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .or()
            .areAnnotatedWith(Controller.class)
            .should()
            .resideInAPackage("..controller..");

    rule.check(importedClasses);
  }

  @Test
  @DisplayName("Controllers deben tener nombres terminados en 'Controller'")
  void testControllerNamingConvention() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..controller..")
            .should()
            .haveSimpleNameEndingWith("Controller");

    rule.check(importedClasses);
  }

  @Test
  @DisplayName("Services deben tener nombres terminados en 'Service'")
  void testServiceNamingConvention() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..service..")
            .should()
            .haveSimpleNameEndingWith("Service");

    rule.check(importedClasses);
  }

  @Test
  @DisplayName("Repositories deben tener nombres terminados en 'Repository'")
  void testRepositoryNamingConvention() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..repository..")
            .should()
            .haveSimpleNameEndingWith("Repository");

    rule.check(importedClasses);
  }

  @Test
  @DisplayName(
      "No se deben importar clases de .repository en paquetes .controller excepto en casos específicos")
  void testDirectRepositoryAccessInControllers() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..controller..")
            .and()
            .doNotHaveSimpleName("RoleController")
            .should()
            .accessClassesThat()
            .resideInAPackage("..repository..");

    rule.check(importedClasses);
  }
}
