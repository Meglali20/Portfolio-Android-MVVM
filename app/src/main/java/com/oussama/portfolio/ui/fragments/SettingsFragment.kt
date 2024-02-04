package com.oussama.portfolio.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.oussama.portfolio.BaseApplication
import com.oussama.portfolio.databinding.FragmentSettingsBinding
import com.oussama.portfolio.utils.COLORSCHEME_DATASTORE_KEY
import com.oussama.portfolio.utils.COLORSCHEME_DYNAMIC
import com.oussama.portfolio.utils.COLORSCHEME_RANDOM
import com.oussama.portfolio.utils.LOCALE_DATASTORE_KEY
import com.oussama.portfolio.utils.LocaleHelper
import com.oussama.portfolio.utils.THEME_DATASTORE_KEY
import com.oussama.portfolio.utils.Utils
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {
    override fun getViewBinding() = FragmentSettingsBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selectAppliedLanguage()
        selectAppliedTheme()
        selectAppliedColorScheme()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setupListeners() {
        super.setupListeners()
        binding.themeRadioGrp.setOnCheckedChangeListener { radioGroup, i ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(i)
            if (!checkedRadioButton.isPressed) return@setOnCheckedChangeListener
            when (radioGroup.checkedRadioButtonId) {
                binding.autoRadio.id -> applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                binding.nightRadio.id -> applyTheme(AppCompatDelegate.MODE_NIGHT_YES)
                binding.lightRadio.id -> applyTheme(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        binding.languageRadioGrp.setOnCheckedChangeListener { radioGroup, i ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(i)
            if (!checkedRadioButton.isPressed) return@setOnCheckedChangeListener
            when (radioGroup.checkedRadioButtonId) {
                binding.enRadio.id -> applyLocale(Locale.UK.language)
                binding.frRadio.id -> applyLocale(Locale.FRANCE.language)
            }
        }

        binding.systemDynamicColorsPie.setOnClickListener {
            applyColorScheme(COLORSCHEME_DYNAMIC)
        }

        binding.randomColorsPie.setOnClickListener {
            applyColorScheme(COLORSCHEME_RANDOM)
        }
    }


    private fun selectAppliedLanguage() {
        lifecycleScope.launch {
            when (BaseApplication.INSTANCE.dataStoreRepository.getInt(THEME_DATASTORE_KEY)) {
                AppCompatDelegate.MODE_NIGHT_YES -> binding.nightRadio.isChecked = true
                AppCompatDelegate.MODE_NIGHT_NO -> binding.lightRadio.isChecked = true
                else -> binding.autoRadio.isChecked = true
            }
        }
    }

    private fun selectAppliedTheme() {
        lifecycleScope.launch {
            when (BaseApplication.INSTANCE.dataStoreRepository.getString(LOCALE_DATASTORE_KEY)) {
                "fr" -> binding.frRadio.isChecked = true
                else -> binding.enRadio.isChecked = true
            }
        }
    }

    private fun selectAppliedColorScheme() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            binding.colorSchemeSettingsContainer.visibility = View.GONE
            return
        }
        val isNightMode = Utils.isNightMode(requireContext())
        val colorPrimary =
            ContextCompat.getColor(
                requireContext(),
                if (isNightMode) android.R.color.system_accent1_200 else android.R.color.system_accent1_600
            )
        val colorSecondary =
            ContextCompat.getColor(
                requireContext(),
                if (isNightMode) android.R.color.system_accent2_200 else android.R.color.system_accent2_600
            )
        val colorTertiary =
            ContextCompat.getColor(
                requireContext(),
                if (isNightMode) android.R.color.system_accent3_200 else android.R.color.system_accent3_600
            )
        binding.systemDynamicColorsPie.setColors(
            colorPrimary = colorPrimary,
            colorSecondary = colorSecondary,
            colorTertiary = colorTertiary
        )
        binding.colorSchemeSettingsContainer.visibility = View.VISIBLE

    }


    private fun applyLocale(localeString: String) {
        lifecycleScope.launch {
            BaseApplication.INSTANCE.dataStoreRepository.putString(
                LOCALE_DATASTORE_KEY,
                localeString
            )
            BaseApplication.INSTANCE.configChanged = true
            LocaleHelper.onAttach(requireActivity(), localeString)
            requireActivity().recreate()
        }
    }

    private fun applyColorScheme(colorSchemeValue: Int) {
        lifecycleScope.launch {
            BaseApplication.INSTANCE.dataStoreRepository.putInt(COLORSCHEME_DATASTORE_KEY, colorSchemeValue)
            BaseApplication.INSTANCE.configChanged = true
            requireActivity().recreate()
        }
    }

    private fun applyTheme(theme: Int) {
        lifecycleScope.launch {
            BaseApplication.INSTANCE.dataStoreRepository.putInt(THEME_DATASTORE_KEY, theme)
            BaseApplication.INSTANCE.configChanged = true
            AppCompatDelegate.setDefaultNightMode(theme)
        }
    }
}