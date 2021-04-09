package entity

data class Post(
    val id : Long,
    val author : Author,
    val content : String,
    val publisher : Long,
    val likeByMe : Boolean,
    val likes : Int = 0,
)
