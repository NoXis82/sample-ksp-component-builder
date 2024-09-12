package com.noxis.ksp_component_builder

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate


/**
 * KSPLogger
 * 1. Получить все файлы, помеченные аннотацией @GenerateComponentFactory.
 * 2. отфильтруем список валидных объектов по типу KSClassDeclaration
 *
 */
class ComponentFactoryProcessor(
    private val logger: KSPLogger,
    private val fileGenerator: ComponentFactoryFileGenerator,
) : SymbolProcessor {
    private var round: Int = 0

    override fun process(resolver: Resolver): List<KSAnnotated> {
        log("=====================")
        log("Round ${++round}")
        val annotatedSymbol = resolver
            .getSymbolsWithAnnotation("com.noxis.core.GenerateComponentFactory")
            .groupBy { it.validate() }

        val validSymbols = annotatedSymbol[true].orEmpty()
        val symbolsForReprocessing = annotatedSymbol[false].orEmpty()
        log("Valid symbols count: ${validSymbols.size}")
        log("Invalid symbols count: ${symbolsForReprocessing.size}")
        val markedClassDeclarations = validSymbols.filterIsInstance<KSClassDeclaration>()
        log("Classes with annotation: $markedClassDeclarations")
        val componentsData = getComponentsData(markedClassDeclarations)
        for (componentData in componentsData) {
            log("Generate file for ${componentData.componentDeclaration}")
            fileGenerator.generateFile(componentData)
        }
        return symbolsForReprocessing
    }

    /**
     * собираем информацию в подготовленный заранее контейнер
     */
    private fun getComponentAnnotationData(componentAnnotation: KSAnnotation): ComponentAnnotationData {
        val dependencies =
            getComponentAnnotationParamValue(componentAnnotation, paramName = "dependencies")
        val providableDependencies = getProvidableDependencies(dependencies)
        val requiredDependencies = dependencies - providableDependencies

        val modules = getComponentAnnotationParamValue(componentAnnotation, paramName = "modules")
        val requiredModules = getRequiredModules(modules)

        return ComponentAnnotationData(
            providableDependencies = providableDependencies,
            requiredDependencies = requiredDependencies,
            requiredModules = requiredModules,
        )
    }


    /**
     *  Анализируя каждый модуль
     */
    private fun getRequiredModules(allModules: List<KSType>): List<ComponentAnnotationData.RequiredModule> {
        val requiredModules = mutableListOf<ComponentAnnotationData.RequiredModule>()
        for (moduleKSType in allModules) {
            val moduleDeclaration = moduleKSType.declaration
            if (moduleDeclaration !is KSClassDeclaration || moduleDeclaration.classKind != ClassKind.CLASS) {
                continue
            }
            //отсеять классы с пустыми конструкторами.
            // Их поведение невозможно модифицировать, поэтому можно положиться на Dagger.
            val constructors = moduleDeclaration.getConstructors()
            val isRequired = constructors.any { constructor -> constructor.parameters.isNotEmpty() }

            if (!isRequired) {
                continue
            }

            //определение наличия конструктора с параметрами по умолчанию
            val hasDefaultConstructor = constructors.any { constructor ->
                constructor.parameters.all { it.hasDefault }
            }
            requiredModules += ComponentAnnotationData.RequiredModule(
                type = moduleKSType,
                hasDefaultConstructor = hasDefaultConstructor,
            )
        }
        return requiredModules
    }

    /**
     * получим список классов из параметра dependencies и
     * попробуем выделить среди них те, которые можно получить из DependenciesProvider
     */
    private fun getProvidableDependencies(allDependencies: List<KSType>): List<KSType> {
        return allDependencies
            .filter { dependencyType ->
                //Опираясь на требования аннотации @dagger.Component мы можем смело делать каст поля declaration к KSClassDeclaration
                val declaration = dependencyType.declaration as KSClassDeclaration
                //getAllSuperTypes() для рекурсивного получения всех супертипов класса
                declaration.getAllSuperTypes()
                    .map { it.declaration }
                    .filterIsInstance<KSClassDeclaration>()
                    .any { superTypeDeclaration ->
                        superTypeDeclaration.qualifiedName?.asString() == "com.example.core.ProvidableDependency"
                    }
            }
    }

    /**
     * функцию, которая сможет получить Class: dependencies и modules.
     *
     *
     */
    private fun getComponentAnnotationParamValue(
        annotation: KSAnnotation,
        paramName: String
    ): List<KSType> {
        //У аннотации есть список аргументов
        //Массиву объектов Class соответствует список KSType

        val annotationArgument = annotation.arguments
            .find { argument -> argument.name?.asString() == paramName }
        val annotationArgumentValue = annotationArgument?.value as? List<KSType>

        return annotationArgumentValue.orEmpty().distinct()
    }

    /**
     * получить все объявления классов внутри компонента и проверить,
     * нет ли среди них помеченного аннотацией @dagger.Component.Factory
     * Каждый объект, который может иметь вложенные объекты,
     * реализует интерфейс KSDeclarationContainer, у которого есть свойство declarations
     */
    private fun hasComponentFactory(componentDeclaration: KSClassDeclaration): Boolean {
        //declarations всех вложенных объектов: классов, функций, свойств и так далее
        return componentDeclaration.declarations
            //нам нужны только интерфейсы, поэтому отфильтруем список по типу KSClassDeclaration
            .filterIsInstance<KSClassDeclaration>()
            .any { childDeclaration ->
                childDeclaration.annotations
                    .any { annotation ->
                        annotation.annotationType
                            .resolve()
                            .declaration
                            .qualifiedName
                            ?.asString() == "dagger.Component.Factory"
                    }
            }
    }

    /**
     * получить саму аннотацию @dagger.Component со всем содержимым.
     * У объектов, реализующих KSAnnotated, есть свойство annotations, содержащее ссылки на типы аннотаций объекта
     */
    private fun getDaggerComponentAnnotation(componentDeclaration: KSAnnotated): KSAnnotation? {
        return componentDeclaration.annotations
            //Отфильтруем другие аннотации, как минимум @GenerateComponentFactory, используя свойство KSAnnotated.shortName
            .filter { annotation -> annotation.shortName.asString() == "Component" }
            .find { annotation ->
                annotation.annotationType
                    //Чтобы получить полную информацию об аннотации, нужно из ссылки на тип получить сам тип, то есть вызвать функцию resolve()
                    //Чтобы окончательно убедиться, что аннотация с именем Component принадлежит Dagger, нужно получить KSType из ссылки на него
                    .resolve()
                    .declaration
                    .qualifiedName
                    ?.asString() == "dagger.Component"
            }
    }

    //анализировать полученный список
    //пропускать объекты без файла
    private fun getComponentsData(markedClassDeclarations: List<KSClassDeclaration>): List<ComponentData> {
        val componentsData = mutableListOf<ComponentData>()
        for (componentDeclaration in markedClassDeclarations) {
            val containingFile = componentDeclaration.containingFile
            if (containingFile == null) {
                log("There is no containing file for $componentDeclaration")
                continue
            }
            val componentAnnotation = getDaggerComponentAnnotation(componentDeclaration)
            if (componentAnnotation == null) {
                log("There is no component annotation")
                continue
            }

            val hasComponentFactory = hasComponentFactory(componentDeclaration)
            if (hasComponentFactory) {
                val componentDeclarationName = componentDeclaration.qualifiedName?.asString()
                    ?: componentDeclaration.simpleName.asString()
                logger.error("Remove @Component.Factory from '$componentDeclarationName'")
                continue
            }

            val annotationData = getComponentAnnotationData(componentAnnotation)
            log("Annotation data gathered: $annotationData")

            componentsData += ComponentData(
                containingFile = containingFile,
                componentDeclaration = componentDeclaration,
                annotationData = annotationData,
            )
        }
        return componentsData
    }


    private fun log(msg: String) {
        logger.warn("[${Thread.currentThread().name}] $msg")
    }

}
