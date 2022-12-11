package com.mixdorstudio.hackterminalfallout

class ProcessWords() {

    private lateinit var wordsList : MutableList<String>
    private var intentosTotales = 4

    fun setText(texto:String){
        wordsList = texto.split(Regex("\\s")).toMutableList()
    }

    fun getRandomWord() : String{

        var first = "No Found"
        if (wordsList.isNotEmpty()) {
            first = wordsList.random()
            wordsList.remove(first)
            intentosTotales--
        }

        return first
    }

    fun getWordByCoincidence(word:String, coincidences:Int) : String {

        var result = "No Found"
        val coincidenList : MutableList<String> = mutableListOf()

        for (wordTest in wordsList){
            var coincidence = 0
            for (j in word.indices){
                if (wordTest[j]==word[j]){
                    coincidence++
                }
            }
            if (coincidence==coincidences){
                coincidenList.add(wordTest)
            }

        }

        wordsList = coincidenList

        if (wordsList.isNotEmpty()){
            result = wordsList.random()
            wordsList.remove(result)
            intentosTotales--
        }

        return result
    }

    fun getIntentos () : Int {
        return intentosTotales
    }

    fun setIntentos(intentos : Int){
        intentosTotales = intentos
    }

    fun getListSize() : Int {
        return wordsList.size
    }

    fun getMutableList() : MutableList<String>{
        return wordsList
    }

    fun deleteWord(word: String){
        wordsList.remove(word)
    }

}