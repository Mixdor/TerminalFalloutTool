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

        val processWord = ProcessWords()

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
            binding.textInferior.text = "¿Cuantas coincidencias encontradas?"
        }

        binding.btnOkCoincidencias.setOnClickListener {

            if(binding.txtCoincidencias.text.toString()!=""){

                binding.textInferior.text = "¿Es la palabra correcta"
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

            binding.PalabraEliminada.setText("Palabra Eliminada")

            //Eliminar palabra
            val listaEliminatoria : MutableList<String> = mutableListOf()
            listaEliminatoria.add("Ninguna")
            for(i in processWord.getMutableList()){
                listaEliminatoria.add(i)
            }

            val adapter = ArrayAdapter(this, R.layout.list_item, listaEliminatoria)
            binding.PalabraEliminada.setAdapter(adapter)

        }

        binding.PalabraEliminada.setOnItemClickListener { parent, view, position, id ->

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
                    resetActivity()
                }
            }
        }

        binding.btnThemeMorado.setOnClickListener {
            binding.btnThemeMorado.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_morado_on))
            lifecycleScope.launch(Dispatchers.IO) {
                saveValues(R.style.Theme_HackTerminalFallout_morado)
                withContext(Dispatchers.Main){
                    resetActivity()
                }
            }
        }

        binding.btnThemeNaranja.setOnClickListener {
            binding.btnThemeNaranja.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_naranja_on))
            lifecycleScope.launch(Dispatchers.IO) {
                saveValues(R.style.Theme_HackTerminalFallout_naranja)
                withContext(Dispatchers.Main){
                    resetActivity()
                }
            }
        }

        binding.btnThemeVerde.setOnClickListener {
            binding.btnThemeVerde.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bom_verde_on))
            lifecycleScope.launch(Dispatchers.IO) {
                saveValues(R.style.Theme_HackTerminalFallout_verde)
                withContext(Dispatchers.Main){
                    resetActivity()
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

    private fun resetActivity() {
        val intent = this.intent
        this.finish()
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        val items = listOf(
            "Muy Facil / Principiante",
            "Facil / Avanzado",
            "Medio / Experto",
            "Difícil / Maestro",
            "Muy difícil")
        val adapter = ArrayAdapter(this, R.layout.list_item, items)
        binding.autoCompleteDificultad.setAdapter(adapter)

        binding.autoCompleteDificultad.setOnItemClickListener { parent, view, position, id ->

            when(position){
                0 -> {
                    binding.layoutRequisitos.visibility = View.VISIBLE
                    binding.txtFallout4.visibility = View.VISIBLE
                    binding.lvlHacker.visibility = View.VISIBLE
                    binding.lvlCiencia.text = "Ciencia 15"
                    binding.lvlHacker.text = "Extra Hacker 0"
                }
                1 -> {
                    binding.layoutRequisitos.visibility = View.VISIBLE
                    binding.txtFallout4.visibility = View.VISIBLE
                    binding.lvlHacker.visibility = View.VISIBLE
                    binding.lvlCiencia.text = "Ciencia 25"
                    binding.lvlHacker.text = "Extra Hacker 1"
                }
                2 -> {
                    binding.layoutRequisitos.visibility = View.VISIBLE
                    binding.txtFallout4.visibility = View.VISIBLE
                    binding.lvlHacker.visibility = View.VISIBLE
                    binding.lvlCiencia.text = "Ciencia 50"
                    binding.lvlHacker.text = "Extra Hacker 2"
                }
                3 -> {
                    binding.layoutRequisitos.visibility = View.VISIBLE
                    binding.txtFallout4.visibility = View.VISIBLE
                    binding.lvlHacker.visibility = View.VISIBLE
                    binding.lvlCiencia.text = "Ciencia 75"
                    binding.lvlHacker.text = "Extra Hacker 3"
                }
                4 -> {
                    binding.layoutRequisitos.visibility = View.VISIBLE
                    binding.lvlCiencia.text = "Ciencia 100"
                    binding.lvlHacker.visibility = View.GONE
                    binding.txtFallout4.visibility = View.GONE
                }
            }
        }

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