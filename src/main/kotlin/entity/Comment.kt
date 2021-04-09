package entity

data class Comment(
    val id : Long,
    val author : Author,
    val content : String,
    val publisher : Long,
    val likeByme : Boolean,
    val likes : Int = 0,
)
