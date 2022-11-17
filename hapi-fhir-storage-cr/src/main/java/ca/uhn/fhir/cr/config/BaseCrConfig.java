package ca.uhn.fhir.cr.config;

/*-
 * #%L
 * HAPI FHIR - Clinical Reasoning
 * %%
 * Copyright (C) 2014 - 2022 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.cr.common.CodeCacheResourceChangeListener;
import ca.uhn.fhir.cr.common.CqlForkJoinWorkerThreadFactory;
import ca.uhn.fhir.cr.common.IDataProviderFactory;
import ca.uhn.fhir.cr.common.ElmCacheResourceChangeListener;
import ca.uhn.fhir.cr.common.IFhirDalFactory;
import ca.uhn.fhir.cr.common.HapiFhirDal;
import ca.uhn.fhir.cr.common.HapiFhirRetrieveProvider;
import ca.uhn.fhir.cr.common.HapiLibrarySourceProvider;
import ca.uhn.fhir.cr.common.HapiTerminologyProvider;
import ca.uhn.fhir.cr.common.ILibraryLoaderFactory;
import ca.uhn.fhir.cr.common.ILibraryManagerFactory;
import ca.uhn.fhir.cr.common.ILibrarySourceProviderFactory;
import ca.uhn.fhir.cr.common.PreExpandedValidationSupportLoader;
import ca.uhn.fhir.cr.common.ITerminologyProviderFactory;
import ca.uhn.fhir.cr.common.interceptor.CqlExceptionHandlingInterceptor;
import ca.uhn.fhir.cr.common.provider.CrProviderFactory;
import ca.uhn.fhir.cr.common.provider.CrProviderLoader;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDaoValueSet;
import ca.uhn.fhir.jpa.cache.IResourceChangeListenerRegistry;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.model.Model;
import org.cqframework.cql.cql2elm.quick.FhirLibrarySourceProvider;
import org.hl7.cql.model.ModelIdentifier;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.fhir.model.Dstu3FhirModelResolver;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.evaluator.CqlOptions;
import org.opencds.cqf.cql.evaluator.builder.Constants;
import org.opencds.cqf.cql.evaluator.builder.DataProviderComponents;
import org.opencds.cqf.cql.evaluator.builder.EndpointInfo;
import org.opencds.cqf.cql.evaluator.cql2elm.model.CacheAwareModelManager;
import org.opencds.cqf.cql.evaluator.cql2elm.util.LibraryVersionSelector;
import org.opencds.cqf.cql.evaluator.engine.execution.CacheAwareLibraryLoaderDecorator;
import org.opencds.cqf.cql.evaluator.engine.execution.TranslatingLibraryLoader;
import org.opencds.cqf.cql.evaluator.engine.model.CachingModelResolverDecorator;
import org.opencds.cqf.cql.evaluator.engine.retrieve.BundleRetrieveProvider;
import org.opencds.cqf.cql.evaluator.fhir.adapter.AdapterFactory;
import org.opencds.cqf.cql.evaluator.measure.MeasureEvaluationOptions;
import org.opencds.cqf.cql.evaluator.spring.fhir.adapter.AdapterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@Import(AdapterConfiguration.class)
@Configuration
public abstract class BaseCrConfig {

	private static final Logger log = LoggerFactory.getLogger(BaseCrConfig.class);


	@Bean
	CrProviderFactory cqlProviderFactory() {
		return new CrProviderFactory();
	}

	@Bean
	CrProviderLoader cqlProviderLoader() {
		return new CrProviderLoader();
	}

	@Bean
	public CrProperties crProperties() {
		return new CrProperties();
	}

	@Bean
	public CrProperties.CqlProperties cqlProperties(CrProperties crProperties) {
		return crProperties.getCql();
	}

	@Bean
	public CrProperties.MeasureProperties measureProperties(CrProperties crProperties) {
		return crProperties.getMeasure();
	}

	@Bean
	public MeasureEvaluationOptions measureEvaluationOptions(CrProperties crProperties) {
		return crProperties.getMeasure().getMeasureEvaluation();
	}

	@Bean
	public CqlOptions cqlOptions(CrProperties crProperties) {
		return crProperties.getCql().getOptions();
	}

	@Bean
	public CqlExceptionHandlingInterceptor cqlExceptionHandlingInterceptor() {
		return new CqlExceptionHandlingInterceptor();
	}

	@Bean
	public CqlTranslatorOptions cqlTranslatorOptions(FhirContext fhirContext, CrProperties.CqlProperties cqlProperties) {
		CqlTranslatorOptions options = cqlProperties.getOptions().getCqlTranslatorOptions();

		if (fhirContext.getVersion().getVersion().isOlderThan(FhirVersionEnum.R4)
			&& (options.getCompatibilityLevel().equals("1.5") || options.getCompatibilityLevel().equals("1.4"))) {
			log.warn("{} {} {}",
				"This server is configured to use CQL version > 1.4 and FHIR version <= DSTU3.",
				"Most available CQL content for DSTU3 and below is for CQL versions 1.3.",
				"If your CQL content causes translation errors, try setting the CQL compatibility level to 1.3");
		}

		return options;
	}

	@Bean
	public ModelManager modelManager(
		Map<ModelIdentifier, Model> globalModelCache) {
		return new CacheAwareModelManager(globalModelCache);
	}

	@Bean
	public ILibraryManagerFactory libraryManagerFactory(
		ModelManager modelManager) {
		return (providers) -> {
			LibraryManager libraryManager = new LibraryManager(modelManager);
			for (LibrarySourceProvider provider : providers) {
				libraryManager.getLibrarySourceLoader().registerProvider(provider);
			}
			return libraryManager;
		};
	}

	@Bean
	public SearchParameterResolver searchParameterResolver(FhirContext fhirContext) {
		return new SearchParameterResolver(fhirContext);
	}

	@Bean
	IFhirDalFactory fhirDalFactory(DaoRegistry daoRegistry) {
		return rd -> new HapiFhirDal(daoRegistry, rd);
	}

	@Bean
    IDataProviderFactory dataProviderFactory(ModelResolver modelResolver, DaoRegistry daoRegistry,
                                             SearchParameterResolver searchParameterResolver) {
		return (rd, t) -> {
			HapiFhirRetrieveProvider provider = new HapiFhirRetrieveProvider(daoRegistry, searchParameterResolver, rd);
			if (t != null) {
				provider.setTerminologyProvider(t);
				provider.setExpandValueSets(true);
				provider.setMaxCodesPerQuery(500);
				provider.setModelResolver(modelResolver);
			}
			return new CompositeDataProvider(modelResolver, provider);
		};
	}

	@Bean
	org.opencds.cqf.cql.evaluator.builder.DataProviderFactory builderDataProviderFactory(FhirContext fhirContext, ModelResolver modelResolver) {
		return new org.opencds.cqf.cql.evaluator.builder.DataProviderFactory() {
			@Override
			public DataProviderComponents create(EndpointInfo endpointInfo) {
				// to do implement endpoint
				return null;
			}

			@Override
			public DataProviderComponents create(IBaseBundle dataBundle) {
				return new DataProviderComponents(Constants.FHIR_MODEL_URI, modelResolver,
					new BundleRetrieveProvider(fhirContext, dataBundle));
			}
		};

	}

	@Bean
	public HapiFhirRetrieveProvider fhirRetrieveProvider(DaoRegistry daoRegistry,
																			  SearchParameterResolver searchParameterResolver) {
		return new HapiFhirRetrieveProvider(daoRegistry, searchParameterResolver);
	}

	@SuppressWarnings("unchecked")
	@Bean
	IFhirResourceDaoValueSet<IBaseResource> valueSetDao(DaoRegistry daoRegistry) {
		return (IFhirResourceDaoValueSet<IBaseResource>) daoRegistry
			.getResourceDao("ValueSet");
	}

	@Bean
	public ITerminologyProviderFactory terminologyProviderFactory(
																					 IValidationSupport theValidationSupport,
																					 Map<org.cqframework.cql.elm.execution.VersionedIdentifier, List<Code>> globalCodeCache) {
		return rd -> new HapiTerminologyProvider(theValidationSupport, globalCodeCache,
			rd);
	}

	@Bean
	ILibrarySourceProviderFactory librarySourceProviderFactory(DaoRegistry daoRegistry) {
		return rd -> new HapiLibrarySourceProvider(daoRegistry, rd);
	}

	@Bean
	ILibraryLoaderFactory libraryLoaderFactory(
		Map<org.cqframework.cql.elm.execution.VersionedIdentifier, org.cqframework.cql.elm.execution.Library> globalLibraryCache,
		ModelManager modelManager, CqlTranslatorOptions cqlTranslatorOptions, CrProperties.CqlProperties cqlProperties) {
		return lcp -> {

			if (cqlProperties.getOptions().useEmbeddedLibraries()) {
				lcp.add(new FhirLibrarySourceProvider());
			}

			return new CacheAwareLibraryLoaderDecorator(
				new TranslatingLibraryLoader(modelManager, lcp, cqlTranslatorOptions), globalLibraryCache) {
				// TODO: This is due to a bug with the ELM annotations which prevent options
				// from matching the way they should
				@Override
				protected Boolean translatorOptionsMatch(org.cqframework.cql.elm.execution.Library library) {
					return true;
				}
			};
		};
	}

	// TODO: Use something like caffeine caching for this so that growth is limited.
	@Bean
	public Map<org.cqframework.cql.elm.execution.VersionedIdentifier, org.cqframework.cql.elm.execution.Library> globalLibraryCache() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	public Map<org.cqframework.cql.elm.execution.VersionedIdentifier, List<Code>> globalCodeCache() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	public Map<ModelIdentifier, Model> globalModelCache() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	@Primary
	public ElmCacheResourceChangeListener elmCacheResourceChangeListener(
		IResourceChangeListenerRegistry resourceChangeListenerRegistry, DaoRegistry daoRegistry,
		Map<org.cqframework.cql.elm.execution.VersionedIdentifier, org.cqframework.cql.elm.execution.Library> globalLibraryCache) {
		ElmCacheResourceChangeListener listener = new ElmCacheResourceChangeListener(daoRegistry, globalLibraryCache);
		resourceChangeListenerRegistry.registerResourceResourceChangeListener("Library",
			SearchParameterMap.newSynchronous(), listener, 1000);
		return listener;
	}

	@Bean
	@Primary
	public CodeCacheResourceChangeListener codeCacheResourceChangeListener(
		IResourceChangeListenerRegistry resourceChangeListenerRegistry, DaoRegistry daoRegistry,
		Map<org.cqframework.cql.elm.execution.VersionedIdentifier, List<Code>> globalCodeCache) {
		CodeCacheResourceChangeListener listener = new CodeCacheResourceChangeListener(daoRegistry, globalCodeCache);
		resourceChangeListenerRegistry.registerResourceResourceChangeListener("ValueSet",
			SearchParameterMap.newSynchronous(), listener, 1000);
		return listener;
	}

	@Bean
	public ModelResolver modelResolver(FhirContext fhirContext) {
		switch(fhirContext.getVersion().getVersion()) {
			case R4: return new CachingModelResolverDecorator(new R4FhirModelResolver());
			case DSTU3: return new CachingModelResolverDecorator(new Dstu3FhirModelResolver());
			default: throw new IllegalStateException("CQL support not yet implemented for this FHIR version. Please change versions or disable the CQL plugin.");
		}
	}

	@Bean
	public LibraryVersionSelector libraryVersionSelector(AdapterFactory adapterFactory) {
		return new LibraryVersionSelector(adapterFactory);
	}

	@Bean(name = "cqlExecutor")
	public Executor cqlExecutor() {
		CqlForkJoinWorkerThreadFactory factory = new CqlForkJoinWorkerThreadFactory();
		ForkJoinPool myCommonPool = new ForkJoinPool(Math.min(32767, Runtime.getRuntime().availableProcessors()),
			factory,
			null, false);

		return new DelegatingSecurityContextExecutor(myCommonPool,
			SecurityContextHolder.getContext());
	}

	@Bean
	public PreExpandedValidationSupportLoader preExpandedValidationSupportLoader(IValidationSupport supportChain,
																										  FhirContext fhirContext) {
		return new PreExpandedValidationSupportLoader(supportChain, fhirContext);
	}
}