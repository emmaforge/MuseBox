package io.nekohasekai.sagernet.ui.profile

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import io.nekohasekai.sagernet.Key
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.preference.EditTextPreferenceModifiers
import io.nekohasekai.sagernet.fmt.musess.MusessBean
import io.nekohasekai.sagernet.ktx.applyDefaultValues
import moe.matsuri.nb4a.proxy.PreferenceBinding
import moe.matsuri.nb4a.proxy.PreferenceBindingManager
import moe.matsuri.nb4a.proxy.Type
import moe.matsuri.nb4a.utils.listByLineOrComma

class MusessSettingsActivity : ProfileSettingsActivity<MusessBean>() {
    override fun createEntity() = MusessBean().applyDefaultValues()

    private val pbm = PreferenceBindingManager()
    private val name = pbm.add(PreferenceBinding(Type.Text, "name"))
    private val serverAddress = pbm.add(PreferenceBinding(Type.Text, "serverAddress"))
    private val serverPort = pbm.add(PreferenceBinding(Type.TextToInt, "serverPort"))
    private val uuid = pbm.add(PreferenceBinding(Type.Text, "uuid"))
    private val secret = pbm.add(PreferenceBinding(Type.Text, "secret"))
    private val publicKey = pbm.add(PreferenceBinding(Type.Text, "publicKey"))
    private val serverName = pbm.add(PreferenceBinding(Type.Text, "serverName"))
    private val mux = pbm.add(PreferenceBinding(Type.Bool, "mux"))
    private val muxAdaptiveBypass = pbm.add(PreferenceBinding(Type.Bool, "muxAdaptiveBypass"))
    private val deshell = pbm.add(PreferenceBinding(Type.Bool, "deshell"))
    private val fec = pbm.add(PreferenceBinding(Type.Text, "fec"))

    // warmPool (tri-state: null = core default 2) and muxBypass (list) have
    // no PreferenceBinding type, so they are synced with the cache store
    // by hand using the same keys as their preferences.
    override fun MusessBean.init() {
        pbm.writeToCacheAll(this)
        DataStore.profileCacheStore.putString("warmPool", warmPool?.toString() ?: "")
        DataStore.profileCacheStore.putString("muxBypass", muxBypass.joinToString("\n"))
    }

    override fun MusessBean.serialize() {
        pbm.fromCacheAll(this)
        warmPool = DataStore.profileCacheStore.getString("warmPool")?.toIntOrNull()
        muxBypass = DataStore.profileCacheStore.getString("muxBypass")
            ?.listByLineOrComma() ?: emptyList()
    }

    override fun PreferenceFragmentCompat.createPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        addPreferencesFromResource(R.xml.musess_preferences)

        findPreference<EditTextPreference>(Key.SERVER_PORT)!!.apply {
            setOnBindEditTextListener(EditTextPreferenceModifiers.Port)
        }
        findPreference<EditTextPreference>("secret")!!.apply {
            summaryProvider = PasswordSummaryProvider
        }
        findPreference<EditTextPreference>("warmPool")!!.apply {
            setOnBindEditTextListener(EditTextPreferenceModifiers.Number)
        }
    }
}
