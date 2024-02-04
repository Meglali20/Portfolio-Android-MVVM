package com.oussama.portfolio.ui.activities


import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.oussama.portfolio.BaseApplication
import com.oussama.portfolio.R
import com.oussama.portfolio.databinding.ActivityMainBinding
import com.oussama.portfolio.ui.components.bottomnavigationbar.BottomNavigationInterface
import com.oussama.portfolio.ui.fragments.AboutFragment
import com.oussama.portfolio.ui.fragments.ContactFragment
import com.oussama.portfolio.ui.fragments.ExperienceFragment
import com.oussama.portfolio.ui.fragments.HomeFragment
import com.oussama.portfolio.ui.fragments.PortfolioFragment
import com.oussama.portfolio.ui.fragments.SettingsFragment
import com.oussama.portfolio.utils.COLORSCHEME_DATASTORE_KEY
import com.oussama.portfolio.utils.COLORSCHEME_RANDOM
import com.oussama.portfolio.utils.LOCALE_DATASTORE_KEY
import com.oussama.portfolio.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import timber.log.Timber


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val homeFragment by lazy { HomeFragment() }
    private val aboutFragment by lazy { AboutFragment() }
    private val experienceFragment by lazy { ExperienceFragment() }
    private val portfolioFragment by lazy { PortfolioFragment() }
    private val contactFragment by lazy { ContactFragment() }
    private val settingsFragment by lazy { SettingsFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyColorScheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (BaseApplication.INSTANCE.configChanged) {
            showFragment(settingsFragment)
            binding.bottomNavigation.setActiveItem(5)
            BaseApplication.INSTANCE.configChanged = false
        } else {
            //showDemoFragment(DemoImagePiecesFragment())
            showFragment(homeFragment)
        }
        binding.bottomNavigation.setBottomNavigationClickListener(object :
            BottomNavigationInterface {
            override fun onBottomNavigationItemClick(itemId: Int) {
                when (itemId) {
                    R.id.homeNavMenu -> showFragment(homeFragment)
                    R.id.aboutNavMenu -> showFragment(aboutFragment)
                    R.id.experienceNavMenu -> showFragment(experienceFragment)
                    R.id.portfolioNavMenu -> showFragment(portfolioFragment)
                    R.id.contactNavMenu -> showFragment(contactFragment)
                    R.id.settingsNavMenu -> showFragment(settingsFragment)
                }
            }
        })
    }

    override fun attachBaseContext(newBase: Context?) {
        runBlocking {
            val savedLocale = BaseApplication.INSTANCE.dataStoreRepository.getString(
                LOCALE_DATASTORE_KEY
            )
            super.attachBaseContext(LocaleHelper.onAttach(newBase!!, savedLocale!!))
        }
    }
    private fun applyColorScheme() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val chosenSchemeResId = selectRandomOverlay()
            theme.applyStyle(chosenSchemeResId, true)
            BaseApplication.INSTANCE.updateColorCombination(theme, chosenSchemeResId)
            return
        }
        val startTime = System.currentTimeMillis()
        runBlocking {
            val savedColorScheme = BaseApplication.INSTANCE.dataStoreRepository.getInt(
                COLORSCHEME_DATASTORE_KEY
            )
            if (savedColorScheme!! == COLORSCHEME_RANDOM) {
                val chosenSchemeResId = selectRandomOverlay()
                theme.applyStyle(chosenSchemeResId, true)
                BaseApplication.INSTANCE.updateColorCombination(theme, chosenSchemeResId)
            }
            else{
                BaseApplication.INSTANCE.updateToDynamicColorCombination()
            }
        }
        Timber.w("Took ${System.currentTimeMillis() - startTime}")
    }

    @SuppressLint("DiscouragedApi")
    private fun selectRandomOverlay(): Int {
        val stylesResIds = (0..17).map { index ->
            val styleName = "OverlayTheme$index"
            val styleResId = resources.getIdentifier(styleName, "style", packageName)
            styleResId
        }

        return stylesResIds.random()
    }

    private fun showFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        if (!fragment.isAdded) {
            transaction.add(R.id.nav_host_fragment, fragment)
        }
        for (existingFragment in supportFragmentManager.fragments) {
            if (existingFragment != fragment && existingFragment.isVisible) {
                transaction.hide(existingFragment)
            }
        }
        transaction.show(fragment)
        transaction.commit()
    }

    private fun showDemoFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        if (!fragment.isAdded) {
            transaction.add(R.id.fullFragmentContainer, fragment)
        }
        for (existingFragment in supportFragmentManager.fragments) {
            if (existingFragment != fragment && existingFragment.isVisible) {
                transaction.hide(existingFragment)
            }
        }
        transaction.show(fragment)
        transaction.commit()
    }

}