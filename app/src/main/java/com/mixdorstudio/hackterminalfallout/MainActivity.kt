package com.mixdorstudio.hackterminalfallout

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mixdorstudio.hackterminalfallout.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "PREFERENCES_APP")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val processWord = ProcessWords()


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putString("lvlCiencia",binding.lvlCiencia.text.toString())
        savedInstanceState.putString("lvlHacker",binding.lvlHacker.text.toString())
        savedInstanceState.putInt("viewLayoutRequisitos", binding.layoutRequisitos.visibility)
        savedInstanceState.putInt("viewTxtFallout4", binding.txtFallout4.visibility)
        savedInstanceState.putInt("viewLvlHacker", binding.lvlHacker.visibility)

        savedInstanceState.putInt("viewLayoutPruebas", binding.layoutPruebas.visibility)
        savedInstanceState.putBoolean("txtEnablePalabras",binding.textPalabras.isEnabled)
        savedInstanceState.putString("strPalabraPosible",binding.textPalabraPosible.text.toString())

        savedInstanceState.putInt("viewBtnSiCorrecto", binding.btnSiCorrecto.visibility)
        savedInstanceState.putInt("viewBtnNoCorrecta",binding.btnNoCorrecta.visibility)
        savedInstanceState.putInt("viewLayoutCoincidencias",binding.layoutCoincidencias.visibility)
        savedInstanceState.putInt("viewBtnOkCoincidencias",binding.btnOkCoincidencias.visibility)

        savedInstanceState.putInt("viewLayoutAtencion", binding.layoutAtencion.visibility)
        savedInstanceState.putInt("viewLayoutPalabraEliminada",binding.layoutPalabraEliminada.visibility)
        savedInstanceState.putInt("viewTextQuestionWord",binding.textQuestionWord.visibility)
        savedInstanceState.putInt("viewBtnOkEliminada",binding.btnOkEliminada.visibility)

        savedInstanceState.putString("wordList",processWord.getText())
        savedInstanceState.putInt("intentos",processWord.getIntentos())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        binding.lvlCiencia.text = savedInstanceState.getString("lvlCiencia","")
        binding.lvlHacker.text = savedInstanceState.getString("lvlHacker","")
        binding.layoutRequisitos.visibility = savedInstanceState.getInt("viewLayoutRequisitos",8)
        binding.txtFallout4.visibility = savedInstanceState.getInt("viewTxtFallout4",8)
        binding.lvlHacker.visibility = savedInstanceState.getInt("viewLvlHacker",8)

        binding.layoutPruebas.visibility = savedInstanceState.getInt("viewLayoutPruebas",8)
        binding.textPalabras.isEnabled = savedInstanceState.getBoolean("txtEnablePalabras",true)
        binding.textPalabraPosible.text = savedInstanceState.getString("strPalabraPosible","No Found")

        binding.btnSiCorrecto.visibility = savedInstanceState.getInt("viewBtnSiCorrecto",8)
        binding.btnNoCorrecta.visibility = savedInstanceState.getInt("viewBtnNoCorrecta",8)
        binding.layoutCoincidencias.visibility = savedInstanceState.getInt("viewLayoutCoincidencias",8)
        binding.btnOkCoincidencias.visibility = savedInstanceState.getInt("viewBtnOkCoincidencias",8)

        binding.layoutAtencion.visibility = savedInstanceState.getInt("viewLayoutAtencion",8)
        binding.layoutPalabraEliminada.visibility = savedInstanceState.getInt("viewLayoutPalabraEliminada",8)
        binding.textQuestionWord.visibility = savedInstanceState.getInt("viewTextQuestionWord",8)
        binding.btnOkEliminada.visibility = savedInstanceState.getInt("viewBtnOkEliminada",8)

        savedInstanceState.getString("wordList")?.let { processWord.setText(it) }
        processWord.setIntentos(savedInstanceState.getInt("intentos"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            dataStore.data.first()
            // You should also handle IOExceptions here.
        }

        var themeID: Int

        runBlocking {
            themeID = getValues().theme
            setTheme(themeID)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when (themeID){
            R.style.Theme_HackTerminalFallout_azul -> binding.btnThemeAzul.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_azul_on))
            R.style.Theme_HackTerminalFallout_verde -> binding.btnThemeVerde.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_verde_on))
            R.style.Theme_HackTerminalFallout_naranja -> binding.btnThemeNaranja.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_naranja_on))
            R.style.Theme_HackTerminalFallout_morado -> binding.btnThemeMorado.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_morado_on))
        }

        binding.btnDesbloq.setOnClickListener {
            binding.layoutPruebas.visibility = View.VISIBLE
            binding.textPalabras.isEnabled = false

            //Procesar primera palabra
            processWord.setText(binding.textPalabras.text.toString().uppercase())

            val strWord = processWord.getRandomWord()
            if (strWord == "No Found"){
                dialogNoFoundWord(processWord)
            }
            binding.textPalabraPosible.text = strWord
        }

        binding.btnSiCorrecto.setOnClickListener {
            processWord.setIntentos(4)
            binding.textPalabras.isEnabled = true
            binding.layoutPruebas.visibility = View.GONE
            binding.textPalabras.setText("")
            binding.txtCoincidencias.setText("")
        }

        binding.btnNoCorrecta.setOnClickListener {
            binding.btnSiCorrecto.visibility = View.GONE
            binding.btnNoCorrecta.visibility = View.GONE
            binding.layoutCoincidencias.visibility = View.VISIBLE
            binding.btnOkCoincidencias.visibility = View.VISIBLE
            binding.textInferior.text = getString(R.string.howMatches)
        }

        binding.btnOkCoincidencias.setOnClickListener {

            if(binding.txtCoincidencias.text.toString()!=""){

                binding.textInferior.text = getString(R.string.IsCorrectWord)
                binding.btnSiCorrecto.visibility = View.VISIBLE
                binding.btnNoCorrecta.visibility = View.VISIBLE
                binding.layoutCoincidencias.visibility = View.GONE
                binding.btnOkCoincidencias.visibility = View.GONE

                //Procesar las coincidencias

                if(processWord.getIntentos()>1||processWord.getListSize()==1){
                    val palabra = binding.textPalabraPosible.text.toString()
                    val coindencias = binding.txtCoincidencias.text.toString().toInt()

                    val strWord = processWord.getWordByCoincidence(palabra, coindencias)
                    if (strWord == "No Found"){
                        dialogNoFoundWord(processWord)
                    }

                    binding.textPalabraPosible.text = strWord
                }
                else{
                    binding.layoutAtencion.visibility = View.VISIBLE
                    binding.layoutPruebas.visibility = View.GONE
                }

                binding.txtCoincidencias.setText("")
            }
        }

        binding.btnPalabraEliminada.setOnClickListener {
            binding.textQuestionWord.visibility = View.VISIBLE
            binding.btnOkEliminada.visibility = View.VISIBLE
            binding.layoutPalabraEliminada.visibility = View.VISIBLE

            binding.PalabraEliminada.setText(getString(R.string.wordDelete))

            //Eliminar palabra
            val listaEliminatoria : MutableList<String> = mutableListOf()
            listaEliminatoria.add("Ninguna")
            for(i in processWord.getMutableList()){
                listaEliminatoria.add(i)
            }

            val adapter = ArrayAdapter(this, R.layout.list_item, listaEliminatoria)
            binding.PalabraEliminada.setAdapter(adapter)

        }

        binding.btnOkEliminada.setOnClickListener {
            val selectWord : String = binding.PalabraEliminada.text.toString()
            if (selectWord != "Ninguna") processWord.deleteWord(selectWord)

            binding.textQuestionWord.visibility = View.GONE
            binding.btnOkEliminada.visibility = View.GONE
            binding.layoutPalabraEliminada.visibility = View.GONE

            if(processWord.getIntentos()>1||processWord.getListSize()<=1){
                binding.layoutAtencion.visibility = View.GONE
                binding.layoutPruebas.visibility = View.VISIBLE
                val strWord = processWord.getRandomWord()
                if (strWord == "No Found"){
                    dialogNoFoundWord(processWord)
                }
                binding.textPalabraPosible.text = strWord
            }
            else{
                binding.layoutAtencion.visibility = View.VISIBLE
                binding.layoutPruebas.visibility = View.GONE
            }
        }

         binding.btnIntentRestart.setOnClickListener {
             binding.layoutAtencion.visibility = View.GONE
             binding.layoutPruebas.visibility = View.VISIBLE
             processWord.setIntentos(4)
             val strWord = processWord.getRandomWord()
             if (strWord == "No Found"){
                 dialogNoFoundWord(processWord)
             }
             binding.textPalabraPosible.text = strWord
         }

        binding.btnThemeAzul.setOnClickListener {
            binding.btnThemeAzul.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_azul_on))
            lifecycleScope.launch(Dispatchers.IO) {
                saveValues(R.style.Theme_HackTerminalFallout_azul)
                withContext(Dispatchers.Main){
                    recreate()
                }
            }
        }

        binding.btnThemeMorado.setOnClickListener {
            binding.btnThemeMorado.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_morado_on))
            lifecycleScope.launch(Dispatchers.IO) {
                saveValues(R.style.Theme_HackTerminalFallout_morado)
                withContext(Dispatchers.Main){
                    recreate()
                }
            }
        }

        binding.btnThemeNaranja.setOnClickListener {
            binding.btnThemeNaranja.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_naranja_on))
            lifecycleScope.launch(Dispatchers.IO) {
                saveValues(R.style.Theme_HackTerminalFallout_naranja)
                withContext(Dispatchers.Main){
                    recreate()
                }
            }
        }

        binding.btnThemeVerde.setOnClickListener {
            binding.btnThemeVerde.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_verde_on))
            lifecycleScope.launch(Dispatchers.IO) {
                saveValues(R.style.Theme_HackTerminalFallout_verde)
                withContext(Dispatchers.Main){
                    recreate()
                }
            }
        }

    }

    private fun dialogNoFoundWord(processWord : ProcessWords) {
        processWord.setIntentos(4)
        binding.textPalabras.isEnabled = true
        binding.layoutPruebas.visibility = View.GONE
        binding.txtCoincidencias.setText("")

        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage("No se ha encontrado ninguna palabra que cumpla con las condiciones")
            .setPositiveButton("Cambiar Palabras"){ _, _ ->

            }
            .setNegativeButton("Limpiar Palabras"){ _, _ ->
                binding.textPalabras.setText("")
            }
            .show()
    }

    override fun onResume() {
        super.onResume()

        val items = listOf(
            getString(R.string.dificultad1),
            getString(R.string.dificultad2),
            getString(R.string.dificultad3),
            getString(R.string.dificultad4),
            getString(R.string.dificultad5))

        val adapter = ArrayAdapter(this, R.layout.list_item, items)
        binding.autoCompleteDificultad.setAdapter(adapter)

        binding.autoCompleteDificultad.setOnItemClickListener { _, _, position, _ ->

            textDifLayout()
            when(position){
                0 -> textDifNivel1()
                1 -> textDifNivel2()
                2 -> textDifNivel3()
                3 -> textDifNivel4()
                4 -> textDifNivel5()
            }
        }
    }

    private fun textDifLayout(){
        binding.layoutRequisitos.visibility = View.VISIBLE
        binding.txtFallout4.visibility = View.VISIBLE
        binding.lvlHacker.visibility = View.VISIBLE
    }

    private fun textDifNivel1(){
        binding.lvlCiencia.text = getString(R.string.lvlCiencia1)
        binding.lvlHacker.text = getString(R.string.lvlHacker1)
    }
    private fun textDifNivel2(){
        binding.lvlCiencia.text = getString(R.string.lvlCiencia2)
        binding.lvlHacker.text = getString(R.string.lvlHacker2)
    }
    private fun textDifNivel3(){
        binding.lvlCiencia.text = getString(R.string.lvlCiencia3)
        binding.lvlHacker.text = getString(R.string.lvlHacker3)
    }
    private fun textDifNivel4(){
        binding.lvlCiencia.text = getString(R.string.lvlCiencia4)
        binding.lvlHacker.text = getString(R.string.lvlHacker4)
    }
    private fun textDifNivel5(){
        binding.layoutRequisitos.visibility = View.VISIBLE
        binding.lvlCiencia.text = getString(R.string.lvlCiencia4)
        binding.lvlHacker.visibility = View.GONE
        binding.txtFallout4.visibility = View.GONE
    }

    private suspend fun saveValues(refTheme:Int){
        dataStore.edit {
            it[intPreferencesKey("theme")] = refTheme
        }
    }

    private suspend fun getValues() = dataStore.data.map { prefer ->
        ThemeProfile(
            theme = prefer[intPreferencesKey("theme")] ?: R.style.Theme_HackTerminalFallout_verde
        )
    }.first()

}