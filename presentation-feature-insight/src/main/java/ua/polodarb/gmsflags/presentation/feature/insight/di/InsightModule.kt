package ua.polodarb.gmsflags.presentation.feature.insight.di
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.presentation.feature.insight.ui.InsightViewModel
val presentationFeatureInsightModule = module { viewModel { InsightViewModel() } }
