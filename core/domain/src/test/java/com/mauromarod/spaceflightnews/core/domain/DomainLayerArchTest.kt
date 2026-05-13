package com.mauromarod.spaceflightnews.core.domain

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.Test

class DomainLayerArchTest {
    private val domainClasses =
        ClassFileImporter()
            .importPackages("com.mauromarod.spaceflightnews.core.domain")

    @Test
    fun `domain layer must not import android framework classes`() {
        noClasses()
            .that().resideInAPackage("com.mauromarod.spaceflightnews.core.domain..")
            .should().accessClassesThat().resideInAPackage("android..")
            .check(domainClasses)
    }

    @Test
    fun `domain layer must not import data layer classes`() {
        noClasses()
            .that().resideInAPackage("com.mauromarod.spaceflightnews.core.domain..")
            .should().accessClassesThat().resideInAPackage("com.mauromarod.spaceflightnews.core.data..")
            .check(domainClasses)
    }

    @Test
    fun `domain layer must not import network layer classes`() {
        noClasses()
            .that().resideInAPackage("com.mauromarod.spaceflightnews.core.domain..")
            .should().accessClassesThat().resideInAPackage("com.mauromarod.spaceflightnews.core.network..")
            .check(domainClasses)
    }
}
