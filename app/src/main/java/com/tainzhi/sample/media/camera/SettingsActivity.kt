package com.tainzhi.sample.media.camera

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.camera.util.SettingsManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            findPreference<Preference>(this@SettingsFragment.requireContext().getString(R.string.settings_key_photo_zsl))?.apply {
                val enableZsl = SettingsManager.getInstance()!!
                        .getBoolean(SettingsManager.KEY_PHOTO_ZSL, SettingsManager.PHOTO_ZSL_DEFAULT_VALUE)
                setDefaultValue(enableZsl)
                onPreferenceChangeListener = this@SettingsFragment
            }
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
            val key = preference.key
            when(key) {
                this@SettingsFragment.requireContext().getString(R.string.settings_key_photo_zsl) -> {
                    Log.d(TAG, "onPreferenceChange: $key changed $newValue")
                    SettingsManager.getInstance()!!
                            .setBoolean(SettingsManager.KEY_PHOTO_ZSL, newValue as Boolean)
                    return true
                }
            }
            return false
        }

        companion object {
            private val TAG = SettingsFragment::class.java.simpleName
        }
    }
}