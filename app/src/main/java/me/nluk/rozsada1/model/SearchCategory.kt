package me.nluk.rozsada1.model

data class SearchCategory(
    private val map : Map<String, Any?>
){
    val id : Int by map
    val name : String by map
    val imageUrl : String by map
}

class SearchCategoryData(
    private val categories : Map<String, List<SearchCategory>>
){
    fun getCategories(languageCode : String) = categories[languageCode]
}
