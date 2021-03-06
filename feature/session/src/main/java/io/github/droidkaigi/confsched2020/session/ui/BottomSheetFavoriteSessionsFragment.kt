package io.github.droidkaigi.confsched2020.session.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.transition.TransitionManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.ViewHolder
import dagger.Module
import dagger.Provides
import dagger.android.support.DaggerFragment
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import io.github.droidkaigi.confsched2020.di.PageScope
import io.github.droidkaigi.confsched2020.ext.assistedActivityViewModels
import io.github.droidkaigi.confsched2020.model.ExpandFilterState
import io.github.droidkaigi.confsched2020.model.SessionPage
import io.github.droidkaigi.confsched2020.session.R
import io.github.droidkaigi.confsched2020.session.databinding.FragmentBottomSheetSessionsBinding
import io.github.droidkaigi.confsched2020.session.ui.item.SessionItem
import io.github.droidkaigi.confsched2020.session.ui.viewmodel.SessionTabViewModel
import io.github.droidkaigi.confsched2020.session.ui.viewmodel.SessionsViewModel
import io.github.droidkaigi.confsched2020.util.autoCleared
import javax.inject.Inject
import javax.inject.Provider

class BottomSheetFavoriteSessionsFragment : DaggerFragment() {

    private var binding: FragmentBottomSheetSessionsBinding by autoCleared()

    @Inject
    lateinit var sessionsViewModelProvider: Provider<SessionsViewModel>
    private val sessionsViewModel: SessionsViewModel by assistedActivityViewModels {
        sessionsViewModelProvider.get()
    }
    @Inject
    lateinit var sessionTabViewModelProvider: Provider<SessionTabViewModel>
    private val sessionTabViewModel: SessionTabViewModel by assistedActivityViewModels({
        SessionPage.Favorite.title
    }) {
        sessionTabViewModelProvider.get()
    }

    @Inject
    lateinit var sessionItemFactory: SessionItem.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_bottom_sheet_sessions,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val groupAdapter = GroupAdapter<ViewHolder<*>>()
        binding.sessionRecycler.adapter = groupAdapter
        binding.startFilter.setOnClickListener {
            sessionTabViewModel.toggleExpand()
        }
        binding.expandLess.setOnClickListener {
            sessionTabViewModel.toggleExpand()
        }
        binding.sessionRecycler.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(bottom = insets.systemWindowInsetBottom + initialState.paddings.bottom)
        }

        sessionTabViewModel.uiModel.observe(viewLifecycleOwner) { uiModel ->
            TransitionManager.beginDelayedTransition(binding.sessionRecycler.parent as ViewGroup)
            binding.sessionRecycler.isVisible = when (uiModel.expandFilterState) {
                ExpandFilterState.EXPANDED, ExpandFilterState.CHANGING ->
                    true
                else ->
                    false
            }
            binding.startFilter.visibility = when (uiModel.expandFilterState) {
                ExpandFilterState.EXPANDED, ExpandFilterState.CHANGING ->
                    View.VISIBLE
                else ->
                    View.INVISIBLE
            }
            binding.expandLess.isVisible = when (uiModel.expandFilterState) {
                ExpandFilterState.COLLAPSED ->
                    true
                else ->
                    false
            }
        }

        sessionsViewModel.uiModel.observe(viewLifecycleOwner) { uiModel: SessionsViewModel.UiModel ->
            val sessions = uiModel.favoritedSessions
            val count = sessions.filter { it.shouldCountForFilter }.count()
            // For Android Lint
            @Suppress("USELESS_CAST")
            binding.filteredSessionCount.text = getString(
                R.string.applicable_session,
                count as Int
            )
            binding.filteredSessionCount.isVisible = uiModel.filters.isFiltered()
            groupAdapter.update(sessions.map {
                sessionItemFactory.create(it, sessionsViewModel)
            })
        }
    }

    companion object {
        fun newInstance(): BottomSheetFavoriteSessionsFragment {
            return BottomSheetFavoriteSessionsFragment()
        }
    }
}

@Module
abstract class BottomSheetFavoriteSessionsFragmentModule {
    @Module
    companion object {
        @PageScope
        @JvmStatic
        @Provides
        fun providesLifecycleOwnerLiveData(
            mainBottomSheetFavoriteSessionsFragment: BottomSheetFavoriteSessionsFragment
        ): LiveData<LifecycleOwner> {
            return mainBottomSheetFavoriteSessionsFragment.viewLifecycleOwnerLiveData
        }
    }
}
